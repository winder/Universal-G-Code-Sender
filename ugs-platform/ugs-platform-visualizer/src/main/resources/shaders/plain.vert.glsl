#version 120

attribute vec3 position;
attribute vec4 color;

// Output color to the fragment shader
varying vec4 fragColor;

void main() {
    // Set the vertex position
    gl_Position = gl_ModelViewProjectionMatrix * vec4(position, 1.0);

    // Pass the vertex color to the fragment shader
     fragColor = color;
}