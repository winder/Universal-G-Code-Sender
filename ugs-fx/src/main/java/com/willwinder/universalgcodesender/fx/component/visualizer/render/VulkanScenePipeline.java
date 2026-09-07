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

import club.doki7.ffm.ptr.BytePtr;
import club.doki7.ffm.ptr.IntPtr;
import club.doki7.vulkan.VkConstants;
import club.doki7.vulkan.bitmask.VkAccessFlags;
import club.doki7.vulkan.bitmask.VkColorComponentFlags;
import club.doki7.vulkan.bitmask.VkCullModeFlags;
import club.doki7.vulkan.bitmask.VkPipelineStageFlags;
import club.doki7.vulkan.bitmask.VkSampleCountFlags;
import club.doki7.vulkan.bitmask.VkShaderStageFlags;
import club.doki7.vulkan.datatype.VkAttachmentDescription;
import club.doki7.vulkan.datatype.VkAttachmentReference;
import club.doki7.vulkan.datatype.VkDescriptorSetLayoutBinding;
import club.doki7.vulkan.datatype.VkDescriptorSetLayoutCreateInfo;
import club.doki7.vulkan.datatype.VkGraphicsPipelineCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineColorBlendAttachmentState;
import club.doki7.vulkan.datatype.VkPipelineColorBlendStateCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineDepthStencilStateCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineDynamicStateCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineInputAssemblyStateCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineLayoutCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineMultisampleStateCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineRasterizationStateCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineShaderStageCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineVertexInputStateCreateInfo;
import club.doki7.vulkan.datatype.VkPipelineViewportStateCreateInfo;
import club.doki7.vulkan.datatype.VkPushConstantRange;
import club.doki7.vulkan.datatype.VkRenderPassCreateInfo;
import club.doki7.vulkan.datatype.VkShaderModuleCreateInfo;
import club.doki7.vulkan.datatype.VkSubpassDependency;
import club.doki7.vulkan.datatype.VkSubpassDescription;
import club.doki7.vulkan.datatype.VkVertexInputAttributeDescription;
import club.doki7.vulkan.datatype.VkVertexInputBindingDescription;
import club.doki7.vulkan.enumtype.VkAttachmentLoadOp;
import club.doki7.vulkan.enumtype.VkAttachmentStoreOp;
import club.doki7.vulkan.enumtype.VkBlendFactor;
import club.doki7.vulkan.enumtype.VkBlendOp;
import club.doki7.vulkan.enumtype.VkCompareOp;
import club.doki7.vulkan.enumtype.VkDescriptorType;
import club.doki7.vulkan.enumtype.VkDynamicState;
import club.doki7.vulkan.enumtype.VkFormat;
import club.doki7.vulkan.enumtype.VkFrontFace;
import club.doki7.vulkan.enumtype.VkImageLayout;
import club.doki7.vulkan.enumtype.VkPipelineBindPoint;
import club.doki7.vulkan.enumtype.VkPolygonMode;
import club.doki7.vulkan.enumtype.VkPrimitiveTopology;
import club.doki7.vulkan.enumtype.VkVertexInputRate;
import club.doki7.vulkan.handle.VkDescriptorSetLayout;
import club.doki7.vulkan.handle.VkPipeline;
import club.doki7.vulkan.handle.VkPipelineLayout;
import club.doki7.vulkan.handle.VkRenderPass;
import club.doki7.vulkan.handle.VkShaderModule;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.Arena;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The render pass and the graphics pipelines. There is one pipeline per {@link Kind}, each in a
 * depth tested and an untested variant, all sharing one pipeline layout with a single push
 * constant block (see {@code push.glsl}).
 */
final class VulkanScenePipeline implements AutoCloseable {
    static final int PUSH_CONSTANT_BYTES = 128;
    private static final String SHADER_PATH = "/shaders/vulkan/";

    /**
     * What is being drawn, which decides the shaders, the topology and how the vertex buffer is
     * read.
     */
    enum Kind {
        /** One pixel lines, two {@link VertexLayout#LINE} vertices per segment. */
        LINES,
        /** Screen space quads, one {@link VertexLayout#LINE} segment per instance. */
        WIDE_LINES,
        /** {@link VertexLayout#MESH} triangles. */
        TRIANGLES,
        /** {@link VertexLayout#TEXTURED} triangles sampling the bound texture. */
        TEXTURED
    }

    private final VulkanDevice vulkanDevice;
    private final VkRenderPass renderPass;
    private final VkDescriptorSetLayout textureSetLayout;
    private final VkPipelineLayout pipelineLayout;
    private final Map<Kind, VkPipeline> depthTestedPipelines = new EnumMap<>(Kind.class);
    private final Map<Kind, VkPipeline> untestedPipelines = new EnumMap<>(Kind.class);

    VulkanScenePipeline(VulkanDevice vulkanDevice) {
        this.vulkanDevice = vulkanDevice;
        this.renderPass = createRenderPass();
        this.textureSetLayout = createTextureSetLayout();
        this.pipelineLayout = createPipelineLayout();

        VkShaderModule lineVertex = createShaderModule("line.vert.spv");
        VkShaderModule wideLineVertex = createShaderModule("wideline.vert.spv");
        VkShaderModule colorFragment = createShaderModule("color.frag.spv");
        VkShaderModule meshVertex = createShaderModule("mesh.vert.spv");
        VkShaderModule meshFragment = createShaderModule("mesh.frag.spv");
        VkShaderModule textureVertex = createShaderModule("texture.vert.spv");
        VkShaderModule textureFragment = createShaderModule("texture.frag.spv");
        List<VkShaderModule> modules = List.of(lineVertex, wideLineVertex, colorFragment, meshVertex, meshFragment,
                textureVertex, textureFragment);
        try (Arena temporary = Arena.ofConfined()) {
            Map<Kind, VkPipelineShaderStageCreateInfo.Ptr> stages = new EnumMap<>(Kind.class);
            stages.put(Kind.LINES, stages(temporary, lineVertex, colorFragment));
            stages.put(Kind.WIDE_LINES, stages(temporary, wideLineVertex, colorFragment));
            stages.put(Kind.TRIANGLES, stages(temporary, meshVertex, meshFragment));
            stages.put(Kind.TEXTURED, stages(temporary, textureVertex, textureFragment));
            createPipelines(temporary, stages);
        } finally {
            modules.forEach(module -> vulkanDevice.commands().destroyShaderModule(vulkanDevice.device(), module, null));
        }
    }

    VkRenderPass renderPass() {
        return renderPass;
    }

    VkPipelineLayout pipelineLayout() {
        return pipelineLayout;
    }

    /** The layout of the single combined image sampler the textured pipeline reads at set 0. */
    VkDescriptorSetLayout textureSetLayout() {
        return textureSetLayout;
    }

    VkPipeline pipeline(Kind kind, boolean depthTested) {
        return (depthTested ? depthTestedPipelines : untestedPipelines).get(kind);
    }

    private boolean isMultisampled() {
        return vulkanDevice.sampleCount() != VkSampleCountFlags._1;
    }

    /**
     * A single subpass that clears, draws and — when multisampling is active — resolves into a
     * single sampled image. Whichever image ends up holding the frame is left in
     * {@code TRANSFER_SRC_OPTIMAL} so it can be copied to the readback buffer straight away.
     */
    private VkRenderPass createRenderPass() {
        try (Arena temporary = Arena.ofConfined()) {
            boolean multisampled = isMultisampled();
            int attachmentCount = multisampled ? 3 : 2;
            VkAttachmentDescription.Ptr attachments = VkAttachmentDescription.allocate(temporary, attachmentCount);
            attachments.at(0)
                    .format(VulkanRenderTarget.COLOR_FORMAT)
                    .samples(vulkanDevice.sampleCount())
                    .loadOp(VkAttachmentLoadOp.CLEAR)
                    .storeOp(multisampled ? VkAttachmentStoreOp.DONT_CARE : VkAttachmentStoreOp.STORE)
                    .stencilLoadOp(VkAttachmentLoadOp.DONT_CARE)
                    .stencilStoreOp(VkAttachmentStoreOp.DONT_CARE)
                    .initialLayout(VkImageLayout.UNDEFINED)
                    .finalLayout(multisampled
                            ? VkImageLayout.COLOR_ATTACHMENT_OPTIMAL
                            : VkImageLayout.TRANSFER_SRC_OPTIMAL);
            attachments.at(1)
                    .format(VulkanRenderTarget.DEPTH_FORMAT)
                    .samples(vulkanDevice.sampleCount())
                    .loadOp(VkAttachmentLoadOp.CLEAR)
                    .storeOp(VkAttachmentStoreOp.DONT_CARE)
                    .stencilLoadOp(VkAttachmentLoadOp.DONT_CARE)
                    .stencilStoreOp(VkAttachmentStoreOp.DONT_CARE)
                    .initialLayout(VkImageLayout.UNDEFINED)
                    .finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
            if (multisampled) {
                attachments.at(2)
                        .format(VulkanRenderTarget.COLOR_FORMAT)
                        .samples(VkSampleCountFlags._1)
                        .loadOp(VkAttachmentLoadOp.DONT_CARE)
                        .storeOp(VkAttachmentStoreOp.STORE)
                        .stencilLoadOp(VkAttachmentLoadOp.DONT_CARE)
                        .stencilStoreOp(VkAttachmentStoreOp.DONT_CARE)
                        .initialLayout(VkImageLayout.UNDEFINED)
                        .finalLayout(VkImageLayout.TRANSFER_SRC_OPTIMAL);
            }

            VkAttachmentReference colorReference = VkAttachmentReference.allocate(temporary)
                    .attachment(0)
                    .layout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
            VkAttachmentReference depthReference = VkAttachmentReference.allocate(temporary)
                    .attachment(1)
                    .layout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Ptr subpass = VkSubpassDescription.allocate(temporary, 1);
            subpass.at(0)
                    .pipelineBindPoint(VkPipelineBindPoint.GRAPHICS)
                    .colorAttachmentCount(1)
                    .pColorAttachments(colorReference)
                    .pDepthStencilAttachment(depthReference);
            if (multisampled) {
                subpass.at(0).pResolveAttachments(VkAttachmentReference.allocate(temporary)
                        .attachment(2)
                        .layout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL));
            }

            // Make the finished frame visible to the copy that follows the render pass.
            VkSubpassDependency.Ptr dependency = VkSubpassDependency.allocate(temporary, 1);
            dependency.at(0)
                    .srcSubpass(0)
                    .dstSubpass(VkConstants.SUBPASS_EXTERNAL)
                    .srcStageMask(VkPipelineStageFlags.COLOR_ATTACHMENT_OUTPUT)
                    .srcAccessMask(VkAccessFlags.COLOR_ATTACHMENT_WRITE)
                    .dstStageMask(VkPipelineStageFlags.TRANSFER)
                    .dstAccessMask(VkAccessFlags.TRANSFER_READ);

            VkRenderPassCreateInfo createInfo = VkRenderPassCreateInfo.allocate(temporary)
                    .attachmentCount(attachmentCount)
                    .pAttachments(attachments)
                    .subpassCount(1)
                    .pSubpasses(subpass)
                    .dependencyCount(1)
                    .pDependencies(dependency);
            VkRenderPass.Ptr handle = VkRenderPass.Ptr.allocate(temporary);
            Vk.check(vulkanDevice.commands().createRenderPass(vulkanDevice.device(), createInfo, null, handle),
                    "vkCreateRenderPass");
            return handle.read();
        }
    }

    private VkDescriptorSetLayout createTextureSetLayout() {
        try (Arena temporary = Arena.ofConfined()) {
            VkDescriptorSetLayoutBinding.Ptr bindings = VkDescriptorSetLayoutBinding.allocate(temporary, 1);
            bindings.at(0)
                    .binding(0)
                    .descriptorType(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
                    .descriptorCount(1)
                    .stageFlags(VkShaderStageFlags.FRAGMENT);
            VkDescriptorSetLayoutCreateInfo createInfo = VkDescriptorSetLayoutCreateInfo.allocate(temporary)
                    .bindingCount(1)
                    .pBindings(bindings);
            VkDescriptorSetLayout.Ptr handle = VkDescriptorSetLayout.Ptr.allocate(temporary);
            Vk.check(vulkanDevice.commands().createDescriptorSetLayout(vulkanDevice.device(), createInfo, null, handle),
                    "vkCreateDescriptorSetLayout");
            return handle.read();
        }
    }

    /**
     * One layout for every pipeline: the push constants plus the texture set, which only the
     * textured pipeline reads. Sharing it keeps the bound descriptor set valid across pipeline
     * switches.
     */
    private VkPipelineLayout createPipelineLayout() {
        try (Arena temporary = Arena.ofConfined()) {
            VkPushConstantRange.Ptr pushConstants = VkPushConstantRange.allocate(temporary, 1);
            pushConstants.at(0)
                    .stageFlags(VkShaderStageFlags.VERTEX | VkShaderStageFlags.FRAGMENT)
                    .offset(0)
                    .size(PUSH_CONSTANT_BYTES);
            VkPipelineLayoutCreateInfo createInfo = VkPipelineLayoutCreateInfo.allocate(temporary)
                    .setLayoutCount(1)
                    .pushConstantRangeCount(1)
                    .pPushConstantRanges(pushConstants);
            createInfo.pSetLayoutsRaw(VkDescriptorSetLayout.Ptr.allocateV(temporary, textureSetLayout).segment());
            VkPipelineLayout.Ptr handle = VkPipelineLayout.Ptr.allocate(temporary);
            Vk.check(vulkanDevice.commands().createPipelineLayout(vulkanDevice.device(), createInfo, null, handle),
                    "vkCreatePipelineLayout");
            return handle.read();
        }
    }

    private static VkPipelineShaderStageCreateInfo.Ptr stages(Arena arena, VkShaderModule vertex, VkShaderModule fragment) {
        BytePtr entryPoint = BytePtr.allocateString(arena, "main");
        VkPipelineShaderStageCreateInfo.Ptr stages = VkPipelineShaderStageCreateInfo.allocate(arena, 2);
        stages.at(0).stage(VkShaderStageFlags.VERTEX).module(vertex).pName(entryPoint);
        stages.at(1).stage(VkShaderStageFlags.FRAGMENT).module(fragment).pName(entryPoint);
        return stages;
    }

    /**
     * Creates every kind in both depth variants with one call, in the order of {@link Kind}
     * with the depth tested variant first.
     */
    private void createPipelines(Arena arena, Map<Kind, VkPipelineShaderStageCreateInfo.Ptr> stages) {
        Map<Kind, VkPipelineVertexInputStateCreateInfo> vertexInputs = new EnumMap<>(Kind.class);
        vertexInputs.put(Kind.LINES, lineVertexInput(arena));
        vertexInputs.put(Kind.WIDE_LINES, wideLineVertexInput(arena));
        vertexInputs.put(Kind.TRIANGLES, meshVertexInput(arena));
        vertexInputs.put(Kind.TEXTURED, texturedVertexInput(arena));

        Map<Kind, Integer> topologies = new EnumMap<>(Kind.class);
        topologies.put(Kind.LINES, VkPrimitiveTopology.LINE_LIST);
        topologies.put(Kind.WIDE_LINES, VkPrimitiveTopology.TRIANGLE_LIST);
        topologies.put(Kind.TRIANGLES, VkPrimitiveTopology.TRIANGLE_LIST);
        topologies.put(Kind.TEXTURED, VkPrimitiveTopology.TRIANGLE_LIST);

        VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.allocate(arena)
                .viewportCount(1)
                .scissorCount(1);
        VkPipelineRasterizationStateCreateInfo rasterization = VkPipelineRasterizationStateCreateInfo.allocate(arena)
                .polygonMode(VkPolygonMode.FILL)
                .cullMode(VkCullModeFlags.NONE)
                .frontFace(VkFrontFace.COUNTER_CLOCKWISE)
                .lineWidth(1.0f);
        VkPipelineMultisampleStateCreateInfo multisample = VkPipelineMultisampleStateCreateInfo.allocate(arena)
                .rasterizationSamples(vulkanDevice.sampleCount())
                .minSampleShading(1.0f);
        // Equal depths pass so that coplanar translucent fills, such as overlapping design
        // shapes, all blend in draw order instead of only the first one showing.
        VkPipelineDepthStencilStateCreateInfo depthTested = VkPipelineDepthStencilStateCreateInfo.allocate(arena)
                .depthTestEnable(VkConstants.TRUE)
                .depthWriteEnable(VkConstants.TRUE)
                .depthCompareOp(VkCompareOp.LESS_OR_EQUAL);
        VkPipelineDepthStencilStateCreateInfo untested = VkPipelineDepthStencilStateCreateInfo.allocate(arena)
                .depthTestEnable(VkConstants.FALSE)
                .depthWriteEnable(VkConstants.FALSE)
                .depthCompareOp(VkCompareOp.ALWAYS);

        VkPipelineColorBlendAttachmentState.Ptr blendAttachment =
                VkPipelineColorBlendAttachmentState.allocate(arena, 1);
        blendAttachment.at(0)
                .blendEnable(VkConstants.TRUE)
                .srcColorBlendFactor(VkBlendFactor.SRC_ALPHA)
                .dstColorBlendFactor(VkBlendFactor.ONE_MINUS_SRC_ALPHA)
                .colorBlendOp(VkBlendOp.ADD)
                .srcAlphaBlendFactor(VkBlendFactor.ONE)
                .dstAlphaBlendFactor(VkBlendFactor.ONE_MINUS_SRC_ALPHA)
                .alphaBlendOp(VkBlendOp.ADD)
                .colorWriteMask(VkColorComponentFlags.R | VkColorComponentFlags.G
                        | VkColorComponentFlags.B | VkColorComponentFlags.A);
        VkPipelineColorBlendStateCreateInfo colorBlend = VkPipelineColorBlendStateCreateInfo.allocate(arena)
                .attachmentCount(1)
                .pAttachments(blendAttachment);
        VkPipelineDynamicStateCreateInfo dynamicState = VkPipelineDynamicStateCreateInfo.allocate(arena)
                .dynamicStateCount(2)
                .pDynamicStates(IntPtr.allocateV(arena, VkDynamicState.VIEWPORT, VkDynamicState.SCISSOR));

        List<Kind> kinds = new ArrayList<>();
        List<Boolean> depthVariants = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            for (boolean depth : new boolean[]{true, false}) {
                kinds.add(kind);
                depthVariants.add(depth);
            }
        }

        int count = kinds.size();
        VkGraphicsPipelineCreateInfo.Ptr createInfos = VkGraphicsPipelineCreateInfo.allocate(arena, count);
        for (int i = 0; i < count; i++) {
            Kind kind = kinds.get(i);
            VkPipelineInputAssemblyStateCreateInfo inputAssembly =
                    VkPipelineInputAssemblyStateCreateInfo.allocate(arena).topology(topologies.get(kind));
            createInfos.at(i)
                    .stageCount(2)
                    .pStages(stages.get(kind))
                    .pVertexInputState(vertexInputs.get(kind))
                    .pInputAssemblyState(inputAssembly)
                    .pViewportState(viewportState)
                    .pRasterizationState(rasterization)
                    .pMultisampleState(multisample)
                    .pDepthStencilState(depthVariants.get(i) ? depthTested : untested)
                    .pColorBlendState(colorBlend)
                    .pDynamicState(dynamicState)
                    .layout(pipelineLayout)
                    .renderPass(renderPass)
                    .subpass(0);
        }

        VkPipeline.Ptr handles = VkPipeline.Ptr.allocate(arena, count);
        Vk.check(vulkanDevice.commands().createGraphicsPipelines(vulkanDevice.device(), null,
                count, createInfos, null, handles), "vkCreateGraphicsPipelines");
        for (int i = 0; i < count; i++) {
            (depthVariants.get(i) ? depthTestedPipelines : untestedPipelines).put(kinds.get(i), handles.read(i));
        }
    }

    /** Position, colour and command number per vertex, as {@code LineMeshBuilder} writes them. */
    private static VkPipelineVertexInputStateCreateInfo lineVertexInput(Arena arena) {
        VkVertexInputBindingDescription.Ptr binding = VkVertexInputBindingDescription.allocate(arena, 1);
        binding.at(0)
                .binding(0)
                .stride(VertexLayout.LINE.bytesPerVertex())
                .inputRate(VkVertexInputRate.VERTEX);
        VkVertexInputAttributeDescription.Ptr attributes = VkVertexInputAttributeDescription.allocate(arena, 3);
        attributes.at(0).location(0).binding(0).format(VkFormat.R32G32B32_SFLOAT).offset(0);
        attributes.at(1).location(1).binding(0).format(VkFormat.R32G32B32A32_SFLOAT).offset(3 * Float.BYTES);
        attributes.at(2).location(2).binding(0).format(VkFormat.R32_SFLOAT).offset(7 * Float.BYTES);
        return vertexInput(arena, binding, attributes);
    }

    /**
     * The same buffer as {@link #lineVertexInput} read one segment per instance, so the
     * stride covers both vertices and the end position sits where the second vertex begins.
     */
    private static VkPipelineVertexInputStateCreateInfo wideLineVertexInput(Arena arena) {
        int vertexBytes = VertexLayout.LINE.bytesPerVertex();
        VkVertexInputBindingDescription.Ptr binding = VkVertexInputBindingDescription.allocate(arena, 1);
        binding.at(0)
                .binding(0)
                .stride(2 * vertexBytes)
                .inputRate(VkVertexInputRate.INSTANCE);
        VkVertexInputAttributeDescription.Ptr attributes = VkVertexInputAttributeDescription.allocate(arena, 4);
        attributes.at(0).location(0).binding(0).format(VkFormat.R32G32B32_SFLOAT).offset(0);
        attributes.at(1).location(1).binding(0).format(VkFormat.R32G32B32A32_SFLOAT).offset(3 * Float.BYTES);
        attributes.at(2).location(2).binding(0).format(VkFormat.R32_SFLOAT).offset(7 * Float.BYTES);
        attributes.at(3).location(3).binding(0).format(VkFormat.R32G32B32_SFLOAT).offset(vertexBytes);
        return vertexInput(arena, binding, attributes);
    }

    /** Position and normal per vertex, as {@code SceneMeshes} and {@code MeshConverter} write them. */
    private static VkPipelineVertexInputStateCreateInfo meshVertexInput(Arena arena) {
        VkVertexInputBindingDescription.Ptr binding = VkVertexInputBindingDescription.allocate(arena, 1);
        binding.at(0)
                .binding(0)
                .stride(VertexLayout.MESH.bytesPerVertex())
                .inputRate(VkVertexInputRate.VERTEX);
        VkVertexInputAttributeDescription.Ptr attributes = VkVertexInputAttributeDescription.allocate(arena, 2);
        attributes.at(0).location(0).binding(0).format(VkFormat.R32G32B32_SFLOAT).offset(0);
        attributes.at(1).location(1).binding(0).format(VkFormat.R32G32B32_SFLOAT).offset(3 * Float.BYTES);
        return vertexInput(arena, binding, attributes);
    }

    /** Position and texture coordinate per vertex. */
    private static VkPipelineVertexInputStateCreateInfo texturedVertexInput(Arena arena) {
        VkVertexInputBindingDescription.Ptr binding = VkVertexInputBindingDescription.allocate(arena, 1);
        binding.at(0)
                .binding(0)
                .stride(VertexLayout.TEXTURED.bytesPerVertex())
                .inputRate(VkVertexInputRate.VERTEX);
        VkVertexInputAttributeDescription.Ptr attributes = VkVertexInputAttributeDescription.allocate(arena, 2);
        attributes.at(0).location(0).binding(0).format(VkFormat.R32G32B32_SFLOAT).offset(0);
        attributes.at(1).location(1).binding(0).format(VkFormat.R32G32_SFLOAT).offset(3 * Float.BYTES);
        return vertexInput(arena, binding, attributes);
    }

    private static VkPipelineVertexInputStateCreateInfo vertexInput(Arena arena,
                                                                    VkVertexInputBindingDescription.Ptr binding,
                                                                    VkVertexInputAttributeDescription.Ptr attributes) {
        return VkPipelineVertexInputStateCreateInfo.allocate(arena)
                .vertexBindingDescriptionCount(1)
                .pVertexBindingDescriptions(binding)
                .vertexAttributeDescriptionCount((int) attributes.size())
                .pVertexAttributeDescriptions(attributes);
    }

    private VkShaderModule createShaderModule(String name) {
        byte[] code = readShader(name);
        try (Arena temporary = Arena.ofConfined()) {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.allocate(temporary)
                    .codeSize(code.length)
                    .pCode(IntPtr.allocate(temporary, code));
            VkShaderModule.Ptr handle = VkShaderModule.Ptr.allocate(temporary);
            Vk.check(vulkanDevice.commands().createShaderModule(vulkanDevice.device(), createInfo, null, handle),
                    "vkCreateShaderModule");
            return handle.read();
        }
    }

    private byte[] readShader(String name) {
        String resource = SHADER_PATH + name;
        try (InputStream stream = Objects.requireNonNull(
                VulkanScenePipeline.class.getResourceAsStream(resource), resource)) {
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new VulkanException("Could not read the shader " + resource, e);
        }
    }

    @Override
    public void close() {
        for (Map<Kind, VkPipeline> pipelines : List.of(untestedPipelines, depthTestedPipelines)) {
            pipelines.values().forEach(pipeline ->
                    vulkanDevice.commands().destroyPipeline(vulkanDevice.device(), pipeline, null));
            pipelines.clear();
        }
        vulkanDevice.commands().destroyPipelineLayout(vulkanDevice.device(), pipelineLayout, null);
        vulkanDevice.commands().destroyDescriptorSetLayout(vulkanDevice.device(), textureSetLayout, null);
        vulkanDevice.commands().destroyRenderPass(vulkanDevice.device(), renderPass, null);
    }
}
