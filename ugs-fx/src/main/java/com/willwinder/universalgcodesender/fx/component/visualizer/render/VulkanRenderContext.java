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

import club.doki7.ffm.ptr.LongPtr;
import club.doki7.ffm.ptr.PointerPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkAccessFlags;
import club.doki7.vulkan.bitmask.VkBufferUsageFlags;
import club.doki7.vulkan.bitmask.VkCommandBufferUsageFlags;
import club.doki7.vulkan.bitmask.VkDescriptorPoolCreateFlags;
import club.doki7.vulkan.bitmask.VkImageAspectFlags;
import club.doki7.vulkan.bitmask.VkImageUsageFlags;
import club.doki7.vulkan.bitmask.VkMemoryPropertyFlags;
import club.doki7.vulkan.bitmask.VkPipelineStageFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.bitmask.VkShaderStageFlags;
import club.doki7.vulkan.command.VkDeviceCommands;
import club.doki7.vulkan.datatype.VkBufferImageCopy;
import club.doki7.vulkan.datatype.VkClearAttachment;
import club.doki7.vulkan.datatype.VkClearRect;
import club.doki7.vulkan.datatype.VkCommandBufferAllocateInfo;
import club.doki7.vulkan.datatype.VkCommandBufferBeginInfo;
import club.doki7.vulkan.datatype.VkDescriptorImageInfo;
import club.doki7.vulkan.datatype.VkDescriptorPoolCreateInfo;
import club.doki7.vulkan.datatype.VkDescriptorPoolSize;
import club.doki7.vulkan.datatype.VkDescriptorSetAllocateInfo;
import club.doki7.vulkan.datatype.VkImageMemoryBarrier;
import club.doki7.vulkan.datatype.VkRect2D;
import club.doki7.vulkan.datatype.VkSamplerCreateInfo;
import club.doki7.vulkan.datatype.VkSubmitInfo;
import club.doki7.vulkan.datatype.VkViewport;
import club.doki7.vulkan.datatype.VkWriteDescriptorSet;
import club.doki7.vulkan.enumtype.VkBorderColor;
import club.doki7.vulkan.enumtype.VkCommandBufferLevel;
import club.doki7.vulkan.enumtype.VkDescriptorType;
import club.doki7.vulkan.enumtype.VkFilter;
import club.doki7.vulkan.enumtype.VkFormat;
import club.doki7.vulkan.enumtype.VkImageLayout;
import club.doki7.vulkan.enumtype.VkPipelineBindPoint;
import club.doki7.vulkan.enumtype.VkSamplerAddressMode;
import club.doki7.vulkan.enumtype.VkSamplerMipmapMode;
import club.doki7.vulkan.handle.VkBuffer;
import club.doki7.vulkan.handle.VkCommandBuffer;
import club.doki7.vulkan.handle.VkDescriptorPool;
import club.doki7.vulkan.handle.VkDescriptorSet;
import club.doki7.vulkan.handle.VkDescriptorSetLayout;
import club.doki7.vulkan.handle.VkPipeline;
import club.doki7.vulkan.handle.VkSampler;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Mat4;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.TextureHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Viewport;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The Vulkan implementation of {@link RenderContext}. Owns every vertex buffer uploaded
 * through it and records draws into the command buffer of the current frame.
 *
 * <p>Frames are fully serialized by {@link VulkanFrameRenderer}, so a buffer released outside
 * a frame is destroyed at once; one released while a frame is being recorded may still be
 * referenced by that frame and is destroyed after it has been waited for.
 */
final class VulkanRenderContext implements RenderContext, AutoCloseable {
    private static final int COLOR_OFFSET = Mat4.ELEMENTS;
    private static final int COMPLETED_COLOR_OFFSET = COLOR_OFFSET + 4;
    private static final int PARAMS_OFFSET = COMPLETED_COLOR_OFFSET + 4;
    private static final int FLAGS_OFFSET = PARAMS_OFFSET + 4;
    private static final float[] BLACK = {0, 0, 0, 1};
    private static final float[] NONE = {0, 0, 0, 0};
    /** Widths at or below this are drawn with the hardware line rasterizer. */
    private static final float THIN_LINE_PIXELS = 1.05f;
    /** Textures alive at once; each raster in the design uses one. */
    private static final int MAX_TEXTURES = 256;
    private static final int TEXTURE_FORMAT = VkFormat.R8G8B8A8_UNORM;

    private final VulkanDevice vulkanDevice;
    private final VkDeviceCommands commands;
    private final VulkanScenePipeline pipelines;
    private final Arena arena = Arena.ofShared();
    private final MemorySegment pushConstants;
    private final VkBuffer.Ptr boundVertexBuffer;
    private final LongPtr boundVertexBufferOffset;
    private final VkViewport.Ptr subViewport;
    private final VkRect2D.Ptr subScissor;
    private final VkClearAttachment.Ptr depthClear;
    private final VkClearRect.Ptr depthClearRect;
    private final Set<VulkanMesh> meshes = new HashSet<>();
    private final List<VulkanMesh> pendingReleases = new ArrayList<>();
    private final VkSampler sampler;
    private final VkDescriptorPool descriptorPool;
    private final VkDescriptorSet.Ptr boundDescriptorSet;
    private final Set<VulkanTexture> textures = new HashSet<>();
    private final List<VulkanTexture> pendingTextureReleases = new ArrayList<>();

    private VkCommandBuffer commandBuffer;
    private Camera camera;
    private Viewport viewport = Viewport.EMPTY;
    private float[] cameraViewProjection = Mat4.identity();
    private float[] viewProjection = Mat4.identity();
    private VkPipeline boundPipeline;
    private boolean depthTest = true;
    private boolean inSubViewport;

    VulkanRenderContext(VulkanDevice vulkanDevice, VulkanScenePipeline pipelines) {
        this.vulkanDevice = vulkanDevice;
        this.commands = vulkanDevice.commands();
        this.pipelines = pipelines;
        this.pushConstants = arena.allocate(VulkanScenePipeline.PUSH_CONSTANT_BYTES, Float.BYTES);
        this.boundVertexBuffer = VkBuffer.Ptr.allocate(arena, 1);
        this.boundVertexBufferOffset = LongPtr.allocateV(arena, 0L);
        this.subViewport = VkViewport.allocate(arena, 1);
        this.subViewport.at(0).minDepth(0).maxDepth(1);
        this.subScissor = VkRect2D.allocate(arena, 1);
        this.depthClear = VkClearAttachment.allocate(arena, 1);
        this.depthClear.at(0)
                .aspectMask(VkImageAspectFlags.DEPTH)
                .clearValue(value -> value.depthStencil(depth -> depth.depth(1.0f).stencil(0)));
        this.depthClearRect = VkClearRect.allocate(arena, 1);
        this.depthClearRect.at(0).baseArrayLayer(0).layerCount(1);
        this.sampler = createSampler();
        this.descriptorPool = createDescriptorPool();
        this.boundDescriptorSet = VkDescriptorSet.Ptr.allocate(arena, 1);
    }

    private VkSampler createSampler() {
        try (Arena temporary = Arena.ofConfined()) {
            VkSamplerCreateInfo createInfo = VkSamplerCreateInfo.allocate(temporary)
                    .magFilter(VkFilter.LINEAR)
                    .minFilter(VkFilter.LINEAR)
                    .mipmapMode(VkSamplerMipmapMode.NEAREST)
                    .addressModeU(VkSamplerAddressMode.CLAMP_TO_EDGE)
                    .addressModeV(VkSamplerAddressMode.CLAMP_TO_EDGE)
                    .addressModeW(VkSamplerAddressMode.CLAMP_TO_EDGE)
                    .maxAnisotropy(1)
                    .borderColor(VkBorderColor.FLOAT_OPAQUE_BLACK);
            VkSampler.Ptr handle = VkSampler.Ptr.allocate(temporary);
            Vk.check(commands.createSampler(vulkanDevice.device(), createInfo, null, handle), "vkCreateSampler");
            return handle.read();
        }
    }

    private VkDescriptorPool createDescriptorPool() {
        try (Arena temporary = Arena.ofConfined()) {
            VkDescriptorPoolSize.Ptr sizes = VkDescriptorPoolSize.allocate(temporary, 1);
            sizes.at(0).type(VkDescriptorType.COMBINED_IMAGE_SAMPLER).descriptorCount(MAX_TEXTURES);
            VkDescriptorPoolCreateInfo createInfo = VkDescriptorPoolCreateInfo.allocate(temporary)
                    .flags(VkDescriptorPoolCreateFlags.FREE_DESCRIPTOR_SET)
                    .maxSets(MAX_TEXTURES)
                    .poolSizeCount(1)
                    .pPoolSizes(sizes);
            VkDescriptorPool.Ptr handle = VkDescriptorPool.Ptr.allocate(temporary);
            Vk.check(commands.createDescriptorPool(vulkanDevice.device(), createInfo, null, handle),
                    "vkCreateDescriptorPool");
            return handle.read();
        }
    }

    void beginFrame(VkCommandBuffer commandBuffer, Camera camera, Viewport viewport) {
        this.commandBuffer = commandBuffer;
        this.camera = camera;
        this.viewport = viewport;
        this.cameraViewProjection = camera.viewProjection();
        this.viewProjection = cameraViewProjection;
        this.boundPipeline = null;
        this.depthTest = true;
        this.inSubViewport = false;
    }

    void endFrame() {
        commandBuffer = null;
        boundPipeline = null;
    }

    /**
     * Destroys the buffers released during the last frame. Call only once that frame has been
     * waited for.
     */
    void flushReleases() {
        pendingReleases.forEach(mesh -> vulkanDevice.destroyBuffer(mesh.buffer()));
        pendingReleases.clear();
        pendingTextureReleases.forEach(this::destroyTexture);
        pendingTextureReleases.clear();
    }

    @Override
    public Camera camera() {
        return camera;
    }

    @Override
    public Viewport viewport() {
        return viewport;
    }

    @Override
    public MeshHandle upload(float[] vertices, VertexLayout layout) {
        int vertexCount = vertices.length / layout.floatsPerVertex();
        long size = Math.max((long) vertices.length * Float.BYTES, Float.BYTES);
        VulkanDevice.AllocatedBuffer buffer = vulkanDevice.createBuffer(size,
                VkBufferUsageFlags.VERTEX_BUFFER,
                VkMemoryPropertyFlags.HOST_VISIBLE | VkMemoryPropertyFlags.HOST_COHERENT);
        if (vertices.length > 0) {
            try (Arena temporary = Arena.ofConfined()) {
                PointerPtr pointer = PointerPtr.allocate(temporary);
                Vk.check(commands.mapMemory(vulkanDevice.device(), buffer.memory(), 0, VkConstants.WHOLE_SIZE, 0, pointer),
                        "vkMapMemory");
                MemorySegment mapped = pointer.read().reinterpret(size);
                MemorySegment.copy(vertices, 0, mapped, ValueLayout.JAVA_FLOAT, 0, vertices.length);
                commands.unmapMemory(vulkanDevice.device(), buffer.memory());
            }
        }
        VulkanMesh mesh = new VulkanMesh(buffer, vertexCount, layout);
        meshes.add(mesh);
        return mesh;
    }

    @Override
    public void release(MeshHandle handle) {
        VulkanMesh mesh = toVulkanMesh(handle);
        if (!meshes.remove(mesh)) {
            return;
        }
        if (commandBuffer != null) {
            pendingReleases.add(mesh);
        } else {
            vulkanDevice.destroyBuffer(mesh.buffer());
        }
    }

    @Override
    public void drawLines(MeshHandle mesh, float[] model, float[] rgba, float widthPx) {
        drawLineMesh(mesh, model, rgba, widthPx, -1, NONE);
    }

    @Override
    public void drawColoredLines(MeshHandle mesh, float[] model, float widthPx) {
        drawLineMesh(mesh, model, null, widthPx, -1, NONE);
    }

    @Override
    public void drawToolpath(MeshHandle mesh, float[] model, float widthPx, int completedCommand, float[] completedRgba) {
        drawLineMesh(mesh, model, null, widthPx, completedCommand, completedRgba);
    }

    /**
     * Copies the pixels through a staging buffer into a device local image with a one time
     * command submission, waited for on the spot. Textures change rarely, so the stall is
     * cheaper than tracking their lifetime across frames.
     */
    @Override
    public TextureHandle uploadTexture(int width, int height, int[] argb) {
        if (width <= 0 || height <= 0 || argb.length < width * height) {
            throw new IllegalArgumentException("The pixels do not cover a " + width + "x" + height + " texture");
        }
        long size = (long) width * height * 4;
        VulkanDevice.AllocatedBuffer staging = vulkanDevice.createBuffer(size,
                VkBufferUsageFlags.TRANSFER_SRC,
                VkMemoryPropertyFlags.HOST_VISIBLE | VkMemoryPropertyFlags.HOST_COHERENT);
        VulkanDevice.AllocatedImage image = vulkanDevice.createImage(width, height, TEXTURE_FORMAT,
                VkSampleCountFlags._1, VkImageUsageFlags.SAMPLED | VkImageUsageFlags.TRANSFER_DST,
                VkImageAspectFlags.COLOR);
        try (Arena temporary = Arena.ofConfined()) {
            PointerPtr pointer = PointerPtr.allocate(temporary);
            Vk.check(commands.mapMemory(vulkanDevice.device(), staging.memory(), 0, VkConstants.WHOLE_SIZE, 0, pointer),
                    "vkMapMemory");
            MemorySegment.copy(toRgbaBytes(argb, width * height), 0, pointer.read().reinterpret(size),
                    ValueLayout.JAVA_BYTE, 0, (int) size);
            commands.unmapMemory(vulkanDevice.device(), staging.memory());

            VkImageMemoryBarrier.Ptr barrier = VkImageMemoryBarrier.allocate(temporary, 1);
            barrier.at(0)
                    .srcQueueFamilyIndex(VkConstants.QUEUE_FAMILY_IGNORED)
                    .dstQueueFamilyIndex(VkConstants.QUEUE_FAMILY_IGNORED)
                    .image(image.image())
                    .subresourceRange(range -> range
                            .aspectMask(VkImageAspectFlags.COLOR)
                            .baseMipLevel(0)
                            .levelCount(1)
                            .baseArrayLayer(0)
                            .layerCount(1));
            VkBufferImageCopy.Ptr region = VkBufferImageCopy.allocate(temporary, 1);
            region.at(0)
                    .imageSubresource(subresource -> subresource
                            .aspectMask(VkImageAspectFlags.COLOR)
                            .mipLevel(0)
                            .baseArrayLayer(0)
                            .layerCount(1))
                    .imageExtent(extent -> extent.width(width).height(height).depth(1));

            submitOnce(temporary, uploadCommands -> {
                barrier.at(0)
                        .srcAccessMask(0)
                        .dstAccessMask(VkAccessFlags.TRANSFER_WRITE)
                        .oldLayout(VkImageLayout.UNDEFINED)
                        .newLayout(VkImageLayout.TRANSFER_DST_OPTIMAL);
                commands.cmdPipelineBarrier(uploadCommands, VkPipelineStageFlags.TOP_OF_PIPE,
                        VkPipelineStageFlags.TRANSFER, 0, 0, null, 0, null, 1, barrier);
                commands.cmdCopyBufferToImage(uploadCommands, staging.buffer(), image.image(),
                        VkImageLayout.TRANSFER_DST_OPTIMAL, 1, region);
                barrier.at(0)
                        .srcAccessMask(VkAccessFlags.TRANSFER_WRITE)
                        .dstAccessMask(VkAccessFlags.SHADER_READ)
                        .oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
                        .newLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL);
                commands.cmdPipelineBarrier(uploadCommands, VkPipelineStageFlags.TRANSFER,
                        VkPipelineStageFlags.FRAGMENT_SHADER, 0, 0, null, 0, null, 1, barrier);
            });
        } finally {
            vulkanDevice.destroyBuffer(staging);
        }
        VulkanTexture texture = new VulkanTexture(image, allocateDescriptorSet(image), width, height);
        textures.add(texture);
        return texture;
    }

    private static byte[] toRgbaBytes(int[] argb, int pixels) {
        byte[] bytes = new byte[pixels * 4];
        for (int i = 0; i < pixels; i++) {
            int pixel = argb[i];
            bytes[i * 4] = (byte) (pixel >> 16);
            bytes[i * 4 + 1] = (byte) (pixel >> 8);
            bytes[i * 4 + 2] = (byte) pixel;
            bytes[i * 4 + 3] = (byte) (pixel >>> 24);
        }
        return bytes;
    }

    private void submitOnce(Arena temporary, Consumer<VkCommandBuffer> recorder) {
        VkCommandBufferAllocateInfo allocateInfo = VkCommandBufferAllocateInfo.allocate(temporary)
                .commandPool(vulkanDevice.commandPool())
                .level(VkCommandBufferLevel.PRIMARY)
                .commandBufferCount(1);
        VkCommandBuffer.Ptr handle = VkCommandBuffer.Ptr.allocate(temporary);
        Vk.check(commands.allocateCommandBuffers(vulkanDevice.device(), allocateInfo, handle), "vkAllocateCommandBuffers");
        VkCommandBuffer uploadCommands = handle.read();
        try {
            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.allocate(temporary)
                    .flags(VkCommandBufferUsageFlags.ONE_TIME_SUBMIT);
            Vk.check(commands.beginCommandBuffer(uploadCommands, beginInfo), "vkBeginCommandBuffer");
            recorder.accept(uploadCommands);
            Vk.check(commands.endCommandBuffer(uploadCommands), "vkEndCommandBuffer");
            VkSubmitInfo submitInfo = VkSubmitInfo.allocate(temporary)
                    .commandBufferCount(1)
                    .pCommandBuffers(handle);
            Vk.check(commands.queueSubmit(vulkanDevice.queue(), 1, submitInfo, null), "vkQueueSubmit");
            Vk.check(commands.queueWaitIdle(vulkanDevice.queue()), "vkQueueWaitIdle");
        } finally {
            commands.freeCommandBuffers(vulkanDevice.device(), vulkanDevice.commandPool(), 1, handle);
        }
    }

    private VkDescriptorSet allocateDescriptorSet(VulkanDevice.AllocatedImage image) {
        try (Arena temporary = Arena.ofConfined()) {
            VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.allocate(temporary)
                    .descriptorPool(descriptorPool)
                    .descriptorSetCount(1);
            allocateInfo.pSetLayoutsRaw(VkDescriptorSetLayout.Ptr.allocateV(temporary, pipelines.textureSetLayout()).segment());
            VkDescriptorSet.Ptr handle = VkDescriptorSet.Ptr.allocate(temporary);
            Vk.check(commands.allocateDescriptorSets(vulkanDevice.device(), allocateInfo, handle),
                    "vkAllocateDescriptorSets");
            VkDescriptorSet set = handle.read();

            VkDescriptorImageInfo.Ptr imageInfo = VkDescriptorImageInfo.allocate(temporary, 1);
            imageInfo.at(0)
                    .sampler(sampler)
                    .imageView(image.view())
                    .imageLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL);
            VkWriteDescriptorSet.Ptr write = VkWriteDescriptorSet.allocate(temporary, 1);
            write.at(0)
                    .dstSet(set)
                    .dstBinding(0)
                    .dstArrayElement(0)
                    .descriptorCount(1)
                    .descriptorType(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
                    .pImageInfo(imageInfo);
            commands.updateDescriptorSets(vulkanDevice.device(), 1, write, 0, null);
            return set;
        }
    }

    @Override
    public void release(TextureHandle handle) {
        VulkanTexture texture = toVulkanTexture(handle);
        if (!textures.remove(texture)) {
            return;
        }
        if (commandBuffer != null) {
            pendingTextureReleases.add(texture);
        } else {
            destroyTexture(texture);
        }
    }

    private void destroyTexture(VulkanTexture texture) {
        try (Arena temporary = Arena.ofConfined()) {
            commands.freeDescriptorSets(vulkanDevice.device(), descriptorPool, 1,
                    VkDescriptorSet.Ptr.allocateV(temporary, texture.descriptorSet()));
        }
        vulkanDevice.destroyImage(texture.image());
    }

    @Override
    public void drawTextured(MeshHandle handle, float[] model, TextureHandle textureHandle, float opacity) {
        VulkanMesh mesh = toVulkanMesh(handle);
        requireLayout(mesh, VertexLayout.TEXTURED);
        VulkanTexture texture = toVulkanTexture(textureHandle);
        if (mesh.vertexCount() == 0) {
            return;
        }
        bindPipeline(VulkanScenePipeline.Kind.TEXTURED);
        boundDescriptorSet.write(0, texture.descriptorSet());
        commands.cmdBindDescriptorSets(commandBuffer, VkPipelineBindPoint.GRAPHICS, pipelines.pipelineLayout(),
                0, 1, boundDescriptorSet, 0, null);
        pushConstants(model, new float[]{1, 1, 1, opacity}, NONE, NONE, NONE);
        bindVertexBuffer(mesh);
        commands.cmdDraw(commandBuffer, mesh.vertexCount(), 1, 0, 0);
    }

    @Override
    public void drawTriangles(MeshHandle handle, float[] model, float[] rgba, boolean lit) {
        VulkanMesh mesh = toVulkanMesh(handle);
        requireLayout(mesh, VertexLayout.MESH);
        if (mesh.vertexCount() == 0) {
            return;
        }
        bindPipeline(VulkanScenePipeline.Kind.TRIANGLES);
        // The mesh shaders read the normal matrix and the lit flag where the line shaders read
        // the completed colour, the parameters and the flags; see mesh-push.glsl.
        float[] normal = Mat4.normalMatrix(model == null ? Mat4.identity() : model);
        pushConstants(model, rgba,
                new float[]{normal[0], normal[1], normal[2], lit ? 1 : 0},
                new float[]{normal[3], normal[4], normal[5], 0},
                new float[]{normal[6], normal[7], normal[8], 0});
        bindVertexBuffer(mesh);
        commands.cmdDraw(commandBuffer, mesh.vertexCount(), 1, 0, 0);
    }

    @Override
    public void setDepthTest(boolean enabled) {
        if (depthTest != enabled) {
            depthTest = enabled;
            boundPipeline = null;
        }
    }

    @Override
    public void beginSubViewport(int x, int y, int width, int height, float[] viewProjection) {
        requireFrame();
        if (inSubViewport) {
            throw new IllegalStateException("Sub viewports cannot be nested");
        }
        inSubViewport = true;
        this.viewProjection = viewProjection;
        setViewportRect(x, y, width, height);
        depthClearRect.at(0).rect(rect -> rect
                .offset(offset -> offset.x(x).y(y))
                .extent(extent -> extent.width(width).height(height)));
        commands.cmdClearAttachments(commandBuffer, 1, depthClear, 1, depthClearRect);
    }

    @Override
    public void endSubViewport() {
        requireFrame();
        if (!inSubViewport) {
            return;
        }
        inSubViewport = false;
        viewProjection = cameraViewProjection;
        setViewportRect(0, 0, viewport.width(), viewport.height());
    }

    private void setViewportRect(int x, int y, int width, int height) {
        subViewport.at(0).x(x).y(y).width(width).height(height);
        subScissor.at(0)
                .offset(offset -> offset.x(x).y(y))
                .extent(extent -> extent.width(width).height(height));
        commands.cmdSetViewport(commandBuffer, 0, 1, subViewport);
        commands.cmdSetScissor(commandBuffer, 0, 1, subScissor);
    }

    private void drawLineMesh(MeshHandle handle, float[] model, float[] rgba, float widthPx,
                              int completedCommand, float[] completedRgba) {
        VulkanMesh mesh = toVulkanMesh(handle);
        requireLayout(mesh, VertexLayout.LINE);
        if (mesh.vertexCount() < 2) {
            return;
        }
        float physicalWidth = (float) viewport.toPhysical(widthPx);
        boolean wide = physicalWidth > THIN_LINE_PIXELS;
        bindPipeline(wide ? VulkanScenePipeline.Kind.WIDE_LINES : VulkanScenePipeline.Kind.LINES);
        float[] params = {completedCommand, physicalWidth, viewport.width(), viewport.height()};
        float[] flags = {0, rgba == null ? 1 : 0, 0, 0};
        pushConstants(model, rgba == null ? BLACK : rgba, completedRgba, params, flags);
        bindVertexBuffer(mesh);
        if (wide) {
            commands.cmdDraw(commandBuffer, 6, mesh.vertexCount() / 2, 0, 0);
        } else {
            commands.cmdDraw(commandBuffer, mesh.vertexCount(), 1, 0, 0);
        }
    }

    private void bindPipeline(VulkanScenePipeline.Kind kind) {
        requireFrame();
        VkPipeline pipeline = pipelines.pipeline(kind, depthTest);
        if (pipeline != boundPipeline) {
            commands.cmdBindPipeline(commandBuffer, VkPipelineBindPoint.GRAPHICS, pipeline);
            boundPipeline = pipeline;
        }
    }

    private void pushConstants(float[] model, float[] color, float[] completedColor, float[] params, float[] flags) {
        float[] modelViewProjection = model == null ? viewProjection : Mat4.multiply(viewProjection, model);
        MemorySegment.copy(modelViewProjection, 0, pushConstants, ValueLayout.JAVA_FLOAT, 0, Mat4.ELEMENTS);
        writeVec4(COLOR_OFFSET, color);
        writeVec4(COMPLETED_COLOR_OFFSET, completedColor);
        writeVec4(PARAMS_OFFSET, params);
        writeVec4(FLAGS_OFFSET, flags);
        commands.cmdPushConstants(commandBuffer, pipelines.pipelineLayout(),
                VkShaderStageFlags.VERTEX | VkShaderStageFlags.FRAGMENT, 0,
                VulkanScenePipeline.PUSH_CONSTANT_BYTES, pushConstants);
    }

    private void writeVec4(int floatOffset, float[] values) {
        for (int i = 0; i < 4; i++) {
            pushConstants.setAtIndex(ValueLayout.JAVA_FLOAT, floatOffset + i, i < values.length ? values[i] : 0);
        }
    }

    private void bindVertexBuffer(VulkanMesh mesh) {
        boundVertexBuffer.write(0, mesh.buffer().buffer());
        commands.cmdBindVertexBuffers(commandBuffer, 0, 1, boundVertexBuffer, boundVertexBufferOffset);
    }

    private void requireFrame() {
        if (commandBuffer == null) {
            throw new IllegalStateException("Drawing is only possible while a frame is being rendered");
        }
    }

    private static void requireLayout(VulkanMesh mesh, VertexLayout layout) {
        if (mesh.layout() != layout) {
            throw new IllegalArgumentException("Expected a " + layout + " mesh but got " + mesh.layout());
        }
    }

    private static VulkanTexture toVulkanTexture(TextureHandle handle) {
        if (handle instanceof VulkanTexture texture) {
            return texture;
        }
        throw new IllegalArgumentException("The texture was not uploaded through this renderer");
    }

    private static VulkanMesh toVulkanMesh(MeshHandle handle) {
        if (handle instanceof VulkanMesh mesh) {
            return mesh;
        }
        throw new IllegalArgumentException("The mesh was not uploaded through this renderer");
    }

    @Override
    public void close() {
        flushReleases();
        meshes.forEach(mesh -> vulkanDevice.destroyBuffer(mesh.buffer()));
        meshes.clear();
        textures.forEach(this::destroyTexture);
        textures.clear();
        commands.destroyDescriptorPool(vulkanDevice.device(), descriptorPool, null);
        commands.destroySampler(vulkanDevice.device(), sampler, null);
        arena.close();
    }

    private record VulkanMesh(VulkanDevice.AllocatedBuffer buffer, int vertexCount, VertexLayout layout)
            implements MeshHandle {
    }

    private record VulkanTexture(VulkanDevice.AllocatedImage image, VkDescriptorSet descriptorSet,
                                 int width, int height) implements TextureHandle {
    }
}
