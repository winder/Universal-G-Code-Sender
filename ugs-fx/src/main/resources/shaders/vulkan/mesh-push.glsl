// Push constants of the mesh pipeline, 128 bytes like push.glsl but laid out for lit
// triangles: the space the line pipelines spend on completed colour and parameters carries
// the normal matrix here. VulkanRenderContext writes both layouts.
layout(push_constant) uniform Push {
    mat4 modelViewProjection;   // offset 0
    vec4 color;                 // offset 64: rgba
    vec4 normalColumn0;         // offset 80: xyz = first column of the normal matrix, w = lit
    vec4 normalColumn1;         // offset 96: xyz = second column
    vec4 normalColumn2;         // offset 112: xyz = third column
} push;
