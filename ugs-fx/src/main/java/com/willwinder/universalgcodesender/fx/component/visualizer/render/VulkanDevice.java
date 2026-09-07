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

import club.doki7.ffm.library.ISharedLibrary;
import club.doki7.ffm.ptr.BytePtr;
import club.doki7.ffm.ptr.FloatPtr;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.ffm.ptr.PointerPtr;
import club.doki7.vulkan.Version;
import club.doki7.vulkan.bitmask.VkCommandPoolCreateFlags;
import club.doki7.vulkan.bitmask.VkInstanceCreateFlags;
import club.doki7.vulkan.bitmask.VkMemoryPropertyFlags;
import club.doki7.vulkan.bitmask.VkQueueFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.command.VkDeviceCommands;
import club.doki7.vulkan.command.VkEntryCommands;
import club.doki7.vulkan.command.VkInstanceCommands;
import club.doki7.vulkan.command.VkStaticCommands;
import club.doki7.vulkan.command.VulkanLoader;
import club.doki7.vulkan.datatype.VkApplicationInfo;
import club.doki7.vulkan.datatype.VkBufferCreateInfo;
import club.doki7.vulkan.datatype.VkCommandPoolCreateInfo;
import club.doki7.vulkan.datatype.VkDeviceCreateInfo;
import club.doki7.vulkan.datatype.VkDeviceQueueCreateInfo;
import club.doki7.vulkan.datatype.VkExtensionProperties;
import club.doki7.vulkan.datatype.VkImageCreateInfo;
import club.doki7.vulkan.datatype.VkImageViewCreateInfo;
import club.doki7.vulkan.datatype.VkInstanceCreateInfo;
import club.doki7.vulkan.datatype.VkMemoryAllocateInfo;
import club.doki7.vulkan.datatype.VkMemoryRequirements;
import club.doki7.vulkan.datatype.VkPhysicalDeviceLimits;
import club.doki7.vulkan.datatype.VkPhysicalDeviceMemoryProperties;
import club.doki7.vulkan.datatype.VkPhysicalDeviceProperties;
import club.doki7.vulkan.datatype.VkQueueFamilyProperties;
import club.doki7.vulkan.enumtype.VkImageLayout;
import club.doki7.vulkan.enumtype.VkImageTiling;
import club.doki7.vulkan.enumtype.VkImageType;
import club.doki7.vulkan.enumtype.VkImageViewType;
import club.doki7.vulkan.enumtype.VkPhysicalDeviceType;
import club.doki7.vulkan.enumtype.VkSharingMode;
import club.doki7.vulkan.handle.VkBuffer;
import club.doki7.vulkan.handle.VkCommandPool;
import club.doki7.vulkan.handle.VkDevice;
import club.doki7.vulkan.handle.VkDeviceMemory;
import club.doki7.vulkan.handle.VkImage;
import club.doki7.vulkan.handle.VkImageView;
import club.doki7.vulkan.handle.VkInstance;
import club.doki7.vulkan.handle.VkPhysicalDevice;
import club.doki7.vulkan.handle.VkQueue;

import java.lang.foreign.Arena;
import java.util.HashSet;
import java.util.Set;

/**
 * Owns the Vulkan instance, the logical device and the command pool the visualizer renders
 * with, and hands out the allocation helpers the rest of the renderer needs.
 *
 * <p>No surface or swapchain is created: the visualizer renders into its own images and copies
 * the result back to the CPU, so this works the same whether or not a window system is
 * available.
 */
final class VulkanDevice implements AutoCloseable {
    private static final String PORTABILITY_ENUMERATION_EXTENSION = "VK_KHR_portability_enumeration";
    private static final String PORTABILITY_SUBSET_EXTENSION = "VK_KHR_portability_subset";

    private final Arena arena = Arena.ofShared();
    private final ISharedLibrary vulkanLibrary;
    private final VkInstanceCommands instanceCommands;
    private final VkDeviceCommands deviceCommands;
    private final VkInstance instance;
    private final VkPhysicalDevice physicalDevice;
    private final VkDevice device;
    private final VkQueue queue;
    private final VkCommandPool commandPool;
    private final VkPhysicalDeviceMemoryProperties memoryProperties;
    private final String deviceName;
    private final int sampleCount;

    VulkanDevice() {
        vulkanLibrary = VulkanLibrary.load();
        VkStaticCommands staticCommands = VulkanLoader.loadStaticCommands(vulkanLibrary);
        VkEntryCommands entryCommands = VulkanLoader.loadEntryCommands(staticCommands);

        instance = createInstance(entryCommands);
        instanceCommands = VulkanLoader.loadInstanceCommands(instance, staticCommands);
        physicalDevice = selectPhysicalDevice();

        VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.allocate(arena);
        instanceCommands.getPhysicalDeviceProperties(physicalDevice, properties);
        deviceName = properties.deviceName().readString();
        sampleCount = selectSampleCount(properties.limits());

        memoryProperties = VkPhysicalDeviceMemoryProperties.allocate(arena);
        instanceCommands.getPhysicalDeviceMemoryProperties(physicalDevice, memoryProperties);

        int queueFamilyIndex = findGraphicsQueueFamily();
        device = createDevice(queueFamilyIndex);
        deviceCommands = VulkanLoader.loadDeviceCommands(device, staticCommands);
        queue = getDeviceQueue(queueFamilyIndex);
        commandPool = createCommandPool(queueFamilyIndex);
    }

    VkDevice device() {
        return device;
    }

    VkQueue queue() {
        return queue;
    }

    VkCommandPool commandPool() {
        return commandPool;
    }

    VkDeviceCommands commands() {
        return deviceCommands;
    }

    String deviceName() {
        return deviceName;
    }

    int sampleCount() {
        return sampleCount;
    }

    private VkInstance createInstance(VkEntryCommands entryCommands) {
        try (Arena temporary = Arena.ofConfined()) {
            VkApplicationInfo applicationInfo = VkApplicationInfo.allocate(temporary)
                    .pApplicationName(BytePtr.allocateString(temporary, "Universal G-code Sender"))
                    .applicationVersion(1)
                    .pEngineName(BytePtr.allocateString(temporary, "ugs-fx"))
                    .engineVersion(1)
                    .apiVersion(Version.VK_API_VERSION_1_0.encode());

            VkInstanceCreateInfo createInfo = VkInstanceCreateInfo.allocate(temporary)
                    .pApplicationInfo(applicationInfo);

            // MoltenVK is a portability driver rather than a conformant Vulkan implementation.
            // Both the Khronos loader and MoltenVK itself leave such a driver out of
            // vkEnumeratePhysicalDevices unless the instance asks for it, which would leave
            // macOS reporting no Vulkan capable device at all.
            if (extensionNames(entryCommands).contains(PORTABILITY_ENUMERATION_EXTENSION)) {
                createInfo.flags(VkInstanceCreateFlags.ENUMERATE_PORTABILITY_KHR)
                        .enabledExtensionCount(1)
                        .ppEnabledExtensionNames(PointerPtr.allocateStrings(temporary, PORTABILITY_ENUMERATION_EXTENSION));
            }

            VkInstance.Ptr handle = VkInstance.Ptr.allocate(temporary);
            Vk.check(entryCommands.createInstance(createInfo, null, handle), "vkCreateInstance");
            return handle.read();
        }
    }

    /**
     * Prefers a discrete GPU, since an offscreen render plus readback is exactly the workload
     * where the dedicated card is worth the transfer.
     */
    private VkPhysicalDevice selectPhysicalDevice() {
        try (Arena temporary = Arena.ofConfined()) {
            IntPtr count = IntPtr.allocate(temporary);
            Vk.check(instanceCommands.enumeratePhysicalDevices(instance, count, null),
                    "vkEnumeratePhysicalDevices");
            if (count.read() == 0) {
                throw new VulkanException("No Vulkan capable device was found");
            }

            VkPhysicalDevice.Ptr devices = VkPhysicalDevice.Ptr.allocate(temporary, count.read());
            Vk.check(instanceCommands.enumeratePhysicalDevices(instance, count, devices),
                    "vkEnumeratePhysicalDevices");

            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.allocate(temporary);
            for (int i = 0; i < count.read(); i++) {
                instanceCommands.getPhysicalDeviceProperties(devices.read(i), properties);
                if (properties.deviceType() == VkPhysicalDeviceType.DISCRETE_GPU) {
                    return devices.read(i);
                }
            }
            return devices.read(0);
        }
    }

    private Set<String> extensionNames(VkEntryCommands entryCommands) {
        try (Arena temporary = Arena.ofConfined()) {
            IntPtr count = IntPtr.allocate(temporary);
            Vk.check(entryCommands.enumerateInstanceExtensionProperties(null, count, null),
                    "vkEnumerateInstanceExtensionProperties");
            if (count.read() == 0) {
                return Set.of();
            }

            VkExtensionProperties.Ptr properties = VkExtensionProperties.allocate(temporary, count.read());
            Vk.check(entryCommands.enumerateInstanceExtensionProperties(null, count, properties),
                    "vkEnumerateInstanceExtensionProperties");
            return toNames(properties, count.read());
        }
    }

    private Set<String> extensionNames(VkPhysicalDevice candidate) {
        try (Arena temporary = Arena.ofConfined()) {
            IntPtr count = IntPtr.allocate(temporary);
            Vk.check(instanceCommands.enumerateDeviceExtensionProperties(candidate, null, count, null),
                    "vkEnumerateDeviceExtensionProperties");
            if (count.read() == 0) {
                return Set.of();
            }

            VkExtensionProperties.Ptr properties = VkExtensionProperties.allocate(temporary, count.read());
            Vk.check(instanceCommands.enumerateDeviceExtensionProperties(candidate, null, count, properties),
                    "vkEnumerateDeviceExtensionProperties");
            return toNames(properties, count.read());
        }
    }

    private static Set<String> toNames(VkExtensionProperties.Ptr properties, int count) {
        Set<String> names = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            names.add(properties.at(i).extensionName().readString());
        }
        return names;
    }

    private int selectSampleCount(VkPhysicalDeviceLimits limits) {
        int supported = limits.framebufferColorSampleCounts() & limits.framebufferDepthSampleCounts();
        for (int candidate : new int[]{VkSampleCountFlags._4, VkSampleCountFlags._2}) {
            if ((supported & candidate) != 0) {
                return candidate;
            }
        }
        return VkSampleCountFlags._1;
    }

    private int findGraphicsQueueFamily() {
        try (Arena temporary = Arena.ofConfined()) {
            IntPtr count = IntPtr.allocate(temporary);
            instanceCommands.getPhysicalDeviceQueueFamilyProperties(physicalDevice, count, null);

            VkQueueFamilyProperties.Ptr families = VkQueueFamilyProperties.allocate(temporary, count.read());
            instanceCommands.getPhysicalDeviceQueueFamilyProperties(physicalDevice, count, families);

            for (int i = 0; i < count.read(); i++) {
                if ((families.at(i).queueFlags() & VkQueueFlags.GRAPHICS) != 0) {
                    return i;
                }
            }
        }
        throw new VulkanException("No Vulkan queue family supports graphics operations");
    }

    private VkDevice createDevice(int queueFamilyIndex) {
        try (Arena temporary = Arena.ofConfined()) {
            VkDeviceQueueCreateInfo.Ptr queueCreateInfo = VkDeviceQueueCreateInfo.allocate(temporary, 1);
            queueCreateInfo.at(0)
                    .queueFamilyIndex(queueFamilyIndex)
                    .queueCount(1)
                    .pQueuePriorities(FloatPtr.allocateV(temporary, 1.0f));

            VkDeviceCreateInfo createInfo = VkDeviceCreateInfo.allocate(temporary)
                    .queueCreateInfoCount(1)
                    .pQueueCreateInfos(queueCreateInfo);

            // A device advertising the portability subset requires it to be enabled, so this is
            // not optional on MoltenVK even though nothing here uses the extension directly.
            if (extensionNames(physicalDevice).contains(PORTABILITY_SUBSET_EXTENSION)) {
                createInfo.enabledExtensionCount(1)
                        .ppEnabledExtensionNames(PointerPtr.allocateStrings(temporary, PORTABILITY_SUBSET_EXTENSION));
            }

            VkDevice.Ptr handle = VkDevice.Ptr.allocate(temporary);
            Vk.check(instanceCommands.createDevice(physicalDevice, createInfo, null, handle),
                    "vkCreateDevice");
            return handle.read();
        }
    }

    private VkQueue getDeviceQueue(int queueFamilyIndex) {
        try (Arena temporary = Arena.ofConfined()) {
            VkQueue.Ptr handle = VkQueue.Ptr.allocate(temporary);
            deviceCommands.getDeviceQueue(device, queueFamilyIndex, 0, handle);
            return handle.read();
        }
    }

    private VkCommandPool createCommandPool(int queueFamilyIndex) {
        try (Arena temporary = Arena.ofConfined()) {
            VkCommandPoolCreateInfo createInfo = VkCommandPoolCreateInfo.allocate(temporary)
                    .queueFamilyIndex(queueFamilyIndex)
                    .flags(VkCommandPoolCreateFlags.RESET_COMMAND_BUFFER);

            VkCommandPool.Ptr handle = VkCommandPool.Ptr.allocate(temporary);
            Vk.check(deviceCommands.createCommandPool(device, createInfo, null, handle),
                    "vkCreateCommandPool");
            return handle.read();
        }
    }

    AllocatedBuffer createBuffer(long size, int usage, int requiredMemoryProperties) {
        return createBuffer(size, usage, requiredMemoryProperties, requiredMemoryProperties);
    }

    /**
     * Allocates a buffer, using {@code preferredMemoryProperties} when a memory type offers
     * them and falling back to {@code requiredMemoryProperties} otherwise. Readback buffers use
     * this to ask for host cached memory, which the CPU can read an order of magnitude faster
     * than the uncached host visible memory a driver may otherwise hand out.
     */
    AllocatedBuffer createBuffer(long size, int usage, int preferredMemoryProperties, int requiredMemoryProperties) {
        try (Arena temporary = Arena.ofConfined()) {
            VkBufferCreateInfo createInfo = VkBufferCreateInfo.allocate(temporary)
                    .size(size)
                    .usage(usage)
                    .sharingMode(VkSharingMode.EXCLUSIVE);

            VkBuffer.Ptr handle = VkBuffer.Ptr.allocate(temporary);
            Vk.check(deviceCommands.createBuffer(device, createInfo, null, handle), "vkCreateBuffer");
            VkBuffer buffer = handle.read();

            VkMemoryRequirements requirements = VkMemoryRequirements.allocate(temporary);
            deviceCommands.getBufferMemoryRequirements(device, buffer, requirements);
            VkDeviceMemory memory = allocateMemory(requirements, preferredMemoryProperties, requiredMemoryProperties);

            Vk.check(deviceCommands.bindBufferMemory(device, buffer, memory, 0), "vkBindBufferMemory");
            return new AllocatedBuffer(buffer, memory, requirements.size());
        }
    }

    void destroyBuffer(AllocatedBuffer buffer) {
        deviceCommands.destroyBuffer(device, buffer.buffer(), null);
        deviceCommands.freeMemory(device, buffer.memory(), null);
    }

    AllocatedImage createImage(int width, int height, int format, int samples, int usage, int aspectMask) {
        try (Arena temporary = Arena.ofConfined()) {
            VkImageCreateInfo createInfo = VkImageCreateInfo.allocate(temporary)
                    .imageType(VkImageType._2D)
                    .format(format)
                    .extent(extent -> extent.width(width).height(height).depth(1))
                    .mipLevels(1)
                    .arrayLayers(1)
                    .samples(samples)
                    .tiling(VkImageTiling.OPTIMAL)
                    .usage(usage)
                    .sharingMode(VkSharingMode.EXCLUSIVE)
                    .initialLayout(VkImageLayout.UNDEFINED);

            VkImage.Ptr imageHandle = VkImage.Ptr.allocate(temporary);
            Vk.check(deviceCommands.createImage(device, createInfo, null, imageHandle), "vkCreateImage");
            VkImage image = imageHandle.read();

            VkMemoryRequirements requirements = VkMemoryRequirements.allocate(temporary);
            deviceCommands.getImageMemoryRequirements(device, image, requirements);
            VkDeviceMemory memory = allocateMemory(requirements, VkMemoryPropertyFlags.DEVICE_LOCAL);
            Vk.check(deviceCommands.bindImageMemory(device, image, memory, 0), "vkBindImageMemory");

            VkImageViewCreateInfo viewCreateInfo = VkImageViewCreateInfo.allocate(temporary)
                    .image(image)
                    .viewType(VkImageViewType._2D)
                    .format(format)
                    .subresourceRange(range -> range
                            .aspectMask(aspectMask)
                            .baseMipLevel(0)
                            .levelCount(1)
                            .baseArrayLayer(0)
                            .layerCount(1));

            VkImageView.Ptr viewHandle = VkImageView.Ptr.allocate(temporary);
            Vk.check(deviceCommands.createImageView(device, viewCreateInfo, null, viewHandle),
                    "vkCreateImageView");
            return new AllocatedImage(image, memory, viewHandle.read());
        }
    }

    void destroyImage(AllocatedImage image) {
        deviceCommands.destroyImageView(device, image.view(), null);
        deviceCommands.destroyImage(device, image.image(), null);
        deviceCommands.freeMemory(device, image.memory(), null);
    }

    private VkDeviceMemory allocateMemory(VkMemoryRequirements requirements, int requiredProperties) {
        return allocateMemory(requirements, requiredProperties, requiredProperties);
    }

    private VkDeviceMemory allocateMemory(VkMemoryRequirements requirements, int preferredProperties,
                                          int requiredProperties) {
        try (Arena temporary = Arena.ofConfined()) {
            VkMemoryAllocateInfo allocateInfo = VkMemoryAllocateInfo.allocate(temporary)
                    .allocationSize(requirements.size())
                    .memoryTypeIndex(selectMemoryType(requirements.memoryTypeBits(),
                            preferredProperties, requiredProperties));

            VkDeviceMemory.Ptr handle = VkDeviceMemory.Ptr.allocate(temporary);
            Vk.check(deviceCommands.allocateMemory(device, allocateInfo, null, handle), "vkAllocateMemory");
            return handle.read();
        }
    }

    private int selectMemoryType(int supportedTypeBits, int preferredProperties, int requiredProperties) {
        int preferred = findMemoryType(supportedTypeBits, preferredProperties);
        if (preferred >= 0) {
            return preferred;
        }

        int required = findMemoryType(supportedTypeBits, requiredProperties);
        if (required >= 0) {
            return required;
        }
        throw new VulkanException("No memory type matches the requested properties");
    }

    private int findMemoryType(int supportedTypeBits, int properties) {
        for (int i = 0; i < memoryProperties.memoryTypeCount(); i++) {
            boolean supported = (supportedTypeBits & (1 << i)) != 0;
            boolean hasProperties =
                    (memoryProperties.memoryTypesAt(i).propertyFlags() & properties) == properties;
            if (supported && hasProperties) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void close() {
        deviceCommands.deviceWaitIdle(device);
        deviceCommands.destroyCommandPool(device, commandPool, null);
        deviceCommands.destroyDevice(device, null);
        instanceCommands.destroyInstance(instance, null);
        arena.close();
        vulkanLibrary.close();
    }

    record AllocatedBuffer(VkBuffer buffer, VkDeviceMemory memory, long size) {
    }

    record AllocatedImage(VkImage image, VkDeviceMemory memory, VkImageView view) {
    }
}
