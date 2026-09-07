/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.fx.component.visualizer.render;

import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkAccessFlags;
import club.doki7.vulkan.bitmask.VkImageAspectFlags;
import club.doki7.vulkan.bitmask.VkPipelineStageFlags;
import club.doki7.vulkan.command.VkDeviceCommands;
import club.doki7.vulkan.datatype.VkBufferImageCopy;
import club.doki7.vulkan.datatype.VkClearValue;
import club.doki7.vulkan.datatype.VkCommandBufferAllocateInfo;
import club.doki7.vulkan.datatype.VkCommandBufferBeginInfo;
import club.doki7.vulkan.datatype.VkFenceCreateInfo;
import club.doki7.vulkan.datatype.VkMemoryBarrier;
import club.doki7.vulkan.datatype.VkRect2D;
import club.doki7.vulkan.datatype.VkRenderPassBeginInfo;
import club.doki7.vulkan.datatype.VkSubmitInfo;
import club.doki7.vulkan.datatype.VkViewport;
import club.doki7.vulkan.enumtype.VkCommandBufferLevel;
import club.doki7.vulkan.enumtype.VkImageLayout;
import club.doki7.vulkan.enumtype.VkSubpassContents;
import club.doki7.vulkan.handle.VkCommandBuffer;
import club.doki7.vulkan.handle.VkFence;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Scene;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Viewport;

import java.lang.foreign.Arena;
import java.nio.ByteBuffer;

/**
 * Renders a {@link Scene} into an offscreen image and reads it back to host memory. Frames are
 * fully serialized with a fence, which is what lets a single command buffer and a single render
 * target be reused without any further synchronization.
 *
 * <p>Everything here happens on the JavaFX application thread, since the renderables read the
 * JavaFX scene graph and the settings while drawing.
 */
public final class VulkanFrameRenderer implements AutoCloseable {
    private final Arena arena = Arena.ofShared();
    private final VulkanDevice vulkanDevice;
    private final VkDeviceCommands commands;
    private final VulkanScenePipeline pipeline;
    private final VulkanRenderContext context;
    private final VkCommandBuffer commandBuffer;
    private final VkFence fence;
    private final VkFence.Ptr fenceHandle;
    private final VkCommandBufferBeginInfo commandBufferBeginInfo;
    private final VkClearValue.Ptr clearValues;
    private final VkRenderPassBeginInfo renderPassBeginInfo;
    private final VkViewport.Ptr viewport;
    private final VkRect2D.Ptr scissor;
    private final VkSubmitInfo submitInfo;
    private final VkBufferImageCopy.Ptr copyRegion;
    private final VkMemoryBarrier.Ptr hostReadBarrier;
    private VulkanRenderTarget renderTarget;

    public VulkanFrameRenderer() {
        vulkanDevice = new VulkanDevice();
        commands = vulkanDevice.commands();
        pipeline = new VulkanScenePipeline(vulkanDevice);
        context = new VulkanRenderContext(vulkanDevice, pipeline);
        commandBuffer = allocateCommandBuffer();
        fence = createFence();
        fenceHandle = VkFence.Ptr.allocateV(arena, fence);
        commandBufferBeginInfo = VkCommandBufferBeginInfo.allocate(arena);
        clearValues = VkClearValue.allocate(arena, 2);
        clearValues.at(1).depthStencil(depth -> depth.depth(1.0f).stencil(0));
        renderPassBeginInfo = VkRenderPassBeginInfo.allocate(arena)
                .renderPass(pipeline.renderPass())
                .clearValueCount(2)
                .pClearValues(clearValues);
        viewport = VkViewport.allocate(arena, 1);
        viewport.at(0).x(0).y(0).minDepth(0).maxDepth(1);
        scissor = VkRect2D.allocate(arena, 1);
        submitInfo = VkSubmitInfo.allocate(arena)
                .commandBufferCount(1)
                .pCommandBuffers(VkCommandBuffer.Ptr.allocateV(arena, commandBuffer));
        copyRegion = VkBufferImageCopy.allocate(arena, 1);
        copyRegion.at(0)
                .bufferOffset(0)
                .bufferRowLength(0)
                .bufferImageHeight(0)
                .imageSubresource(subresource -> subresource
                        .aspectMask(VkImageAspectFlags.COLOR)
                        .mipLevel(0)
                        .baseArrayLayer(0)
                        .layerCount(1));
        hostReadBarrier = VkMemoryBarrier.allocate(arena, 1);
        hostReadBarrier.at(0)
                .srcAccessMask(VkAccessFlags.TRANSFER_WRITE)
                .dstAccessMask(VkAccessFlags.HOST_READ);
        setBackgroundColor(0, 0, 0);
    }

    public String deviceName() {
        return vulkanDevice.deviceName();
    }

    public int sampleCount() {
        return vulkanDevice.sampleCount();
    }

    /**
     * The context renderables upload through. Valid for uploads and releases at any time; draws
     * only while {@link #renderFrame} runs.
     */
    public RenderContext context() {
        return context;
    }

    public void setBackgroundColor(float red, float green, float blue) {
        clearValues.at(0).color(color -> {
            color.float32().write(0, red);
            color.float32().write(1, green);
            color.float32().write(2, blue);
            color.float32().write(3, 1.0f);
        });
    }

    /**
     * Renders one frame and returns the buffer holding it as tightly packed BGRA rows. The
     * returned buffer is a new instance whenever the size changed, so callers must re-wrap it
     * rather than caching it across resizes.
     */
    public ByteBuffer renderFrame(Scene scene, Camera camera, Viewport size) {
        if (scene.context() != context) {
            throw new IllegalArgumentException("The scene was created for another renderer");
        }
        resizeRenderTarget(size.width(), size.height());
        camera.setViewport(size);
        recordCommandBuffer(scene, camera, size);
        Vk.check(commands.queueSubmit(vulkanDevice.queue(), 1, submitInfo, fence), "vkQueueSubmit");
        Vk.check(commands.waitForFences(vulkanDevice.device(), 1, fenceHandle, VkConstants.TRUE, Long.MAX_VALUE),
                "vkWaitForFences");
        Vk.check(commands.resetFences(vulkanDevice.device(), 1, fenceHandle), "vkResetFences");
        context.flushReleases();
        return renderTarget.pixels();
    }

    private void resizeRenderTarget(int width, int height) {
        if (renderTarget != null && renderTarget.width() == width && renderTarget.height() == height) {
            return;
        }
        commands.deviceWaitIdle(vulkanDevice.device());
        if (renderTarget != null) {
            renderTarget.close();
        }
        renderTarget = new VulkanRenderTarget(vulkanDevice, pipeline.renderPass(), width, height);
        renderPassBeginInfo
                .framebuffer(renderTarget.framebuffer())
                .renderArea(area -> area.extent(extent -> extent.width(width).height(height)));
        viewport.at(0).width(width).height(height);
        scissor.at(0).extent(extent -> extent.width(width).height(height));
        copyRegion.at(0).imageExtent(extent -> extent.width(width).height(height).depth(1));
    }

    private void recordCommandBuffer(Scene scene, Camera camera, Viewport size) {
        Vk.check(commands.resetCommandBuffer(commandBuffer, 0), "vkResetCommandBuffer");
        Vk.check(commands.beginCommandBuffer(commandBuffer, commandBufferBeginInfo), "vkBeginCommandBuffer");
        commands.cmdBeginRenderPass(commandBuffer, renderPassBeginInfo, VkSubpassContents.INLINE);
        commands.cmdSetViewport(commandBuffer, 0, 1, viewport);
        commands.cmdSetScissor(commandBuffer, 0, 1, scissor);
        context.beginFrame(commandBuffer, camera, size);
        try {
            scene.render();
        } finally {
            context.endFrame();
        }
        commands.cmdEndRenderPass(commandBuffer);
        commands.cmdCopyImageToBuffer(commandBuffer, renderTarget.resolvedImage(),
                VkImageLayout.TRANSFER_SRC_OPTIMAL, renderTarget.readbackBuffer(), 1, copyRegion);
        commands.cmdPipelineBarrier(commandBuffer, VkPipelineStageFlags.TRANSFER, VkPipelineStageFlags.HOST,
                0, 1, hostReadBarrier, 0, null, 0, null);
        Vk.check(commands.endCommandBuffer(commandBuffer), "vkEndCommandBuffer");
    }

    private VkCommandBuffer allocateCommandBuffer() {
        try (Arena temporary = Arena.ofConfined()) {
            VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.allocate(temporary)
                    .commandPool(vulkanDevice.commandPool())
                    .level(VkCommandBufferLevel.PRIMARY)
                    .commandBufferCount(1);
            VkCommandBuffer.Ptr handle = VkCommandBuffer.Ptr.allocate(temporary);
            Vk.check(commands.allocateCommandBuffers(vulkanDevice.device(), allocateInfo, handle),
                    "vkAllocateCommandBuffers");
            return handle.read();
        }
    }

    private VkFence createFence() {
        try (Arena temporary = Arena.ofConfined()) {
            VkFence.Ptr handle = VkFence.Ptr.allocate(temporary);
            Vk.check(commands.createFence(vulkanDevice.device(), VkFenceCreateInfo.allocate(temporary), null, handle),
                    "vkCreateFence");
            return handle.read();
        }
    }

    @Override
    public void close() {
        commands.deviceWaitIdle(vulkanDevice.device());
        if (renderTarget != null) {
            renderTarget.close();
        }
        context.close();
        commands.destroyFence(vulkanDevice.device(), fence, null);
        pipeline.close();
        arena.close();
        vulkanDevice.close();
    }
}
