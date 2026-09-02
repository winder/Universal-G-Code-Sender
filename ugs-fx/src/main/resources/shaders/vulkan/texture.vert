#version 450
#extension GL_GOOGLE_include_directive : require
#include "push.glsl"

// Textured triangles from a TEXTURED layout buffer: position and texture coordinate per vertex.
layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inUv;

layout(location = 0) out vec2 fragUv;

void main() {
    gl_Position = push.modelViewProjection * vec4(inPosition, 1.0);
    fragUv = inUv;
}
