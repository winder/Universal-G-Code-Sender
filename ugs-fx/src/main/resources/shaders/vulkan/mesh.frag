#version 450
#extension GL_GOOGLE_include_directive : require
#include "mesh-push.glsl"

layout(location = 0) in vec3 fragNormal;

layout(location = 0) out vec4 outColor;

// A bright studio setup: sky and ground ambient depending on how much the face looks up, a key
// light from above and in front, and a softer fill from the opposite side. Both lights use the
// absolute cosine so faces are lit whichever way their triangles are wound. The machine parts
// use dark base colours, so the lights may push a face somewhat above its base colour, as the
// JavaFX lights did.
const vec3 KEY_DIRECTION = normalize(vec3(0.4, -0.6, 0.7));
const vec3 FILL_DIRECTION = normalize(vec3(-0.6, 0.5, 0.3));
const float GROUND_AMBIENT = 0.6;
const float SKY_AMBIENT = 0.9;
const float KEY_STRENGTH = 0.5;
const float FILL_STRENGTH = 0.2;
const float MAX_SHADE = 1.35;

void main() {
    float lit = push.normalColumn0.w;
    vec3 normal = normalize(fragNormal);
    float ambient = mix(GROUND_AMBIENT, SKY_AMBIENT, normal.z * 0.5 + 0.5);
    float key = KEY_STRENGTH * abs(dot(normal, KEY_DIRECTION));
    float fill = FILL_STRENGTH * abs(dot(normal, FILL_DIRECTION));
    float shade = mix(1.0, min(ambient + key + fill, MAX_SHADE), lit);
    outColor = vec4(push.color.rgb * shade, push.color.a);
}
