// Push constants shared by every pipeline, 128 bytes: the guaranteed minimum on any device.
// Kept in one file so the Java side (VulkanRenderContext) has a single layout to match.
layout(push_constant) uniform Push {
    mat4 modelViewProjection;   // offset 0
    vec4 color;                 // offset 64: rgba, used when flags.y == 0
    vec4 completedColor;        // offset 80: rgba for commands the controller has finished
    vec4 params;                // offset 96: x = last completed command, y = line width in pixels,
                                //            z = viewport width, w = viewport height
    vec4 flags;                 // offset 112: x = diffuse lighting, y = colour from the vertex
} push;
