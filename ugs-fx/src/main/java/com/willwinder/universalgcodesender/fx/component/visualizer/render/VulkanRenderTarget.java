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

import club.doki7.ffm.ptr.PointerPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkBufferUsageFlags;
import club.doki7.vulkan.bitmask.VkImageAspectFlags;
import club.doki7.vulkan.bitmask.VkImageUsageFlags;
import club.doki7.vulkan.bitmask.VkMemoryPropertyFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.datatype.VkFramebufferCreateInfo;
import club.doki7.vulkan.enumtype.VkFormat;
import club.doki7.vulkan.handle.VkBuffer;
import club.doki7.vulkan.handle.VkFramebuffer;
import club.doki7.vulkan.handle.VkImage;
import club.doki7.vulkan.handle.VkImageView;
import club.doki7.vulkan.handle.VkRenderPass;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

/**
 * The images the scene is rendered into, plus the host visible buffer the finished frame is
 * copied back to. Sized to the component, so it is thrown away and rebuilt on every resize.
 *
 * <p>The colour format is chosen to match JavaFX's {@code BYTE_BGRA_PRE} pixel format, which
 * lets the mapped readback memory be handed straight to a {@code PixelBuffer} without a
 * conversion pass.
 */
final class VulkanRenderTarget implements AutoCloseable {
    static final int COLOR_FORMAT = VkFormat.B8G8R8A8_UNORM;
    static final int DEPTH_FORMAT = VkFormat.D32_SFLOAT;
    static final int BYTES_PER_PIXEL = 4;

    private final VulkanDevice vulkanDevice;
    private final int width;
    private final int height;
    private final boolean multisampled;
    private final VulkanDevice.AllocatedImage colorImage;
    private final VulkanDevice.AllocatedImage depthImage;
    private final VulkanDevice.AllocatedImage resolveImage;
    private final VkFramebuffer framebuffer;
    private final VulkanDevice.AllocatedBuffer readbackBuffer;
    private final ByteBuffer pixels;

    VulkanRenderTarget(VulkanDevice vulkanDevice, VkRenderPass renderPass, int width, int height) {
        this.vulkanDevice = vulkanDevice;
        this.width = width;
        this.height = height;
        this.multisampled = vulkanDevice.sampleCount() != VkSampleCountFlags._1;

        int colorUsage = VkImageUsageFlags.COLOR_ATTACHMENT
                | (multisampled ? 0 : VkImageUsageFlags.TRANSFER_SRC);
        colorImage = vulkanDevice.createImage(width, height, COLOR_FORMAT, vulkanDevice.sampleCount(),
                colorUsage, VkImageAspectFlags.COLOR);
        depthImage = vulkanDevice.createImage(width, height, DEPTH_FORMAT, vulkanDevice.sampleCount(),
                VkImageUsageFlags.DEPTH_STENCIL_ATTACHMENT, VkImageAspectFlags.DEPTH);
        resolveImage = multisampled
                ? vulkanDevice.createImage(width, height, COLOR_FORMAT, VkSampleCountFlags._1,
                VkImageUsageFlags.COLOR_ATTACHMENT | VkImageUsageFlags.TRANSFER_SRC,
                VkImageAspectFlags.COLOR)
                : null;

        framebuffer = createFramebuffer(renderPass);
        int requiredMemory = VkMemoryPropertyFlags.HOST_VISIBLE | VkMemoryPropertyFlags.HOST_COHERENT;
        readbackBuffer = vulkanDevice.createBuffer((long) width * height * BYTES_PER_PIXEL,
                VkBufferUsageFlags.TRANSFER_DST,
                requiredMemory | VkMemoryPropertyFlags.HOST_CACHED,
                requiredMemory);
        pixels = mapReadbackBuffer();
    }

    int width() {
        return width;
    }

    int height() {
        return height;
    }

    VkFramebuffer framebuffer() {
        return framebuffer;
    }

    /**
     * The single sampled image holding the finished frame, which is the resolve target when
     * multisampling is active and the colour attachment itself otherwise.
     */
    VkImage resolvedImage() {
        return multisampled ? resolveImage.image() : colorImage.image();
    }

    VkBuffer readbackBuffer() {
        return readbackBuffer.buffer();
    }

    /**
     * Direct buffer over the mapped readback memory, laid out as tightly packed BGRA rows.
     */
    ByteBuffer pixels() {
        return pixels;
    }

    private VkFramebuffer createFramebuffer(VkRenderPass renderPass) {
        try (Arena temporary = Arena.ofConfined()) {
            int attachmentCount = multisampled ? 3 : 2;
            VkImageView.Ptr attachments = VkImageView.Ptr.allocate(temporary, attachmentCount);
            attachments.write(0, colorImage.view());
            attachments.write(1, depthImage.view());
            if (multisampled) {
                attachments.write(2, resolveImage.view());
            }

            VkFramebufferCreateInfo createInfo = VkFramebufferCreateInfo.allocate(temporary)
                    .renderPass(renderPass)
                    .attachmentCount(attachmentCount)
                    .pAttachments(attachments)
                    .width(width)
                    .height(height)
                    .layers(1);

            VkFramebuffer.Ptr handle = VkFramebuffer.Ptr.allocate(temporary);
            Vk.check(vulkanDevice.commands().createFramebuffer(vulkanDevice.device(), createInfo, null, handle),
                    "vkCreateFramebuffer");
            return handle.read();
        }
    }

    private ByteBuffer mapReadbackBuffer() {
        try (Arena temporary = Arena.ofConfined()) {
            PointerPtr pointer = PointerPtr.allocate(temporary);
            Vk.check(vulkanDevice.commands().mapMemory(vulkanDevice.device(), readbackBuffer.memory(),
                    0, VkConstants.WHOLE_SIZE, 0, pointer), "vkMapMemory");

            MemorySegment mapped = pointer.read()
                    .reinterpret((long) width * height * BYTES_PER_PIXEL);
            return mapped.asByteBuffer();
        }
    }

    @Override
    public void close() {
        vulkanDevice.commands().unmapMemory(vulkanDevice.device(), readbackBuffer.memory());
        vulkanDevice.destroyBuffer(readbackBuffer);
        vulkanDevice.commands().destroyFramebuffer(vulkanDevice.device(), framebuffer, null);
        if (resolveImage != null) {
            vulkanDevice.destroyImage(resolveImage);
        }
        vulkanDevice.destroyImage(depthImage);
        vulkanDevice.destroyImage(colorImage);
    }
}
