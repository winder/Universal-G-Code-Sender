#!/bin/bash
# Regenerates the SPIR-V modules loaded by VulkanScenePipeline.
#
# Requires glslang (Debian/Ubuntu: apt install glslang-tools, or the Vulkan SDK, or a release
# from https://github.com/KhronosGroup/glslang/releases). Older packages name the binary
# glslangValidator. Run from this directory and commit the .spv output.

set -e
cd "$(dirname "$0")"

if command -v glslang > /dev/null; then
    COMPILER=glslang
elif command -v glslangValidator > /dev/null; then
    COMPILER=glslangValidator
else
    echo "glslang or glslangValidator is required" >&2
    exit 1
fi

for shader in line.vert wideline.vert color.frag mesh.vert mesh.frag texture.vert texture.frag; do
    $COMPILER -V --target-env vulkan1.0 -I. -o "$shader.spv" "$shader"
    echo "compiled $shader -> $shader.spv"
done
