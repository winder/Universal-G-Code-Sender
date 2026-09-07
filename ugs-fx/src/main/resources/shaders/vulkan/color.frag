#version 450

// Passes the colour the line vertex shaders computed straight through.

layout(location = 0) in vec4 fragColor;

layout(location = 0) out vec4 outColor;

void main() {
    outColor = fragColor;
}
