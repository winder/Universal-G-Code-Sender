#version 450
#extension GL_GOOGLE_include_directive : require
#include "push.glsl"

// Samples the bound texture and tints it with the push colour, whose alpha sets the opacity.
layout(set = 0, binding = 0) uniform sampler2D image;

layout(location = 0) in vec2 fragUv;
layout(location = 0) out vec4 outColor;

void main() {
    outColor = texture(image, fragUv) * push.color;
}
