#version 120

// Input color from the vertex shader
varying vec4 fragColor;

void main() {
    // Set the fragment color to the input color
    gl_FragColor = fragColor;
}
