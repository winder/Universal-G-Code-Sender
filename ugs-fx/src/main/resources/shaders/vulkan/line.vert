#version 450
#extension GL_GOOGLE_include_directive : require
#include "push.glsl"

// One pixel lines from a LINE layout buffer: position, rgba colour and command number per vertex.

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec4 inColor;
layout(location = 2) in float inCommand;

layout(location = 0) out vec4 fragColor;

void main() {
    gl_Position = push.modelViewProjection * vec4(inPosition, 1.0);
    vec4 base = push.flags.y > 0.5 ? inColor : push.color;
    // Marking the run part of the program costs a single push constant rather than a rewrite
    // of the vertex buffer.
    bool completed = inCommand >= 0.0 && inCommand <= push.params.x;
    fragColor = completed ? push.completedColor : base;
}
