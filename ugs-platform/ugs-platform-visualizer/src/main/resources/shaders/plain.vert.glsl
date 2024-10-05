#version 110

// Input vertex position and color
attribute vec3 inPosition;
attribute vec4 inColor;

// Output color to the fragment shader
varying vec4 fragColor;

void main() {
    // Set the vertex position
    gl_Position = gl_ModelViewProjectionMatrix * vec4(inPosition, 1.0);

    // Pass the vertex color to the fragment shader
    fragColor = inColor;
}