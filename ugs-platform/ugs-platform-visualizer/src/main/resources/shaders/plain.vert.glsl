#version 110

// Output color to the fragment shader
varying vec4 fragColor;

void main() {
    // Set the vertex position
    gl_Position = ftransform();

    // Pass the vertex color to the fragment shader
    fragColor = gl_Color;
}