#version 450
#extension GL_GOOGLE_include_directive : require
#include "mesh-push.glsl"

// Triangles from a MESH layout buffer: position and normal per vertex. The normal is taken to
// world space with the normal matrix so rotated parts are lit correctly.

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;

layout(location = 0) out vec3 fragNormal;

void main() {
    gl_Position = push.modelViewProjection * vec4(inPosition, 1.0);
    mat3 normalMatrix = mat3(push.normalColumn0.xyz, push.normalColumn1.xyz, push.normalColumn2.xyz);
    fragNormal = normalMatrix * inNormal;
}
