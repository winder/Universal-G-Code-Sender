#version 450
#extension GL_GOOGLE_include_directive : require
#include "push.glsl"

// Lines wider than one pixel. Vulkan's lineWidth needs the wideLines feature, which MoltenVK
// does not have, so each segment is expanded into a screen space quad here instead. The same
// LINE layout buffer as line.vert is read one segment per instance: the two vertices of a
// segment form one 64 byte instance, so no second upload is needed to change the width.

layout(location = 0) in vec3 inStart;
layout(location = 1) in vec4 inStartColor;
layout(location = 2) in float inStartCommand;
layout(location = 3) in vec3 inEnd;

layout(location = 0) out vec4 fragColor;

// x picks the end of the segment, y the side of the line. Two triangles per quad.
const vec2 CORNERS[6] = vec2[](
    vec2(0.0, -1.0), vec2(1.0, -1.0), vec2(1.0, 1.0),
    vec2(0.0, -1.0), vec2(1.0, 1.0), vec2(0.0, 1.0)
);

void main() {
    vec2 corner = CORNERS[gl_VertexIndex];
    vec4 clipStart = push.modelViewProjection * vec4(inStart, 1.0);
    vec4 clipEnd = push.modelViewProjection * vec4(inEnd, 1.0);
    vec2 viewport = push.params.zw;

    vec2 screenStart = clipStart.xy / clipStart.w * viewport * 0.5;
    vec2 screenEnd = clipEnd.xy / clipEnd.w * viewport * 0.5;
    vec2 along = screenEnd - screenStart;
    float length = length(along);
    vec2 direction = length > 1e-6 ? along / length : vec2(1.0, 0.0);
    vec2 normal = vec2(-direction.y, direction.x);

    vec2 offsetPixels = normal * push.params.y * 0.5 * corner.y;
    vec2 offsetClip = offsetPixels * 2.0 / viewport;
    vec4 clip = corner.x < 0.5 ? clipStart : clipEnd;
    gl_Position = clip + vec4(offsetClip * clip.w, 0.0, 0.0);

    vec4 base = push.flags.y > 0.5 ? inStartColor : push.color;
    bool completed = inStartCommand >= 0.0 && inStartCommand <= push.params.x;
    fragColor = completed ? push.completedColor : base;
}
