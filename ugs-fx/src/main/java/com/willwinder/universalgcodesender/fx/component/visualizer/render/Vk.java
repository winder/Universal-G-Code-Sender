/*
    Copyright 2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.fx.component.visualizer.render;

import club.doki7.vulkan.enumtype.VkResult;

final class Vk {
    private Vk() {
    }

    /**
     * Turns a non-successful VkResult into a {@link VulkanException} naming the failed call.
     */
    static void check(int result, String operation) {
        if (result != VkResult.SUCCESS) {
            throw new VulkanException(operation + " failed: " + VkResult.explain(result));
        }
    }
}
