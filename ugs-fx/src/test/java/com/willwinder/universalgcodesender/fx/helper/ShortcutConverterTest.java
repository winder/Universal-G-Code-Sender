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
package com.willwinder.universalgcodesender.fx.helper;

import org.apache.commons.lang3.SystemUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ShortcutConverterTest {

    @Test
    public void resolvePlatformShortcut_shouldResolveShortcutTokenToMetaOnMac() {
        String expectedModifier = SystemUtils.IS_OS_MAC ? "META" : "CTRL";

        String result = ShortcutConverter.resolvePlatformShortcut("SHORTCUT+S");

        assertThat(result).isEqualTo(expectedModifier + "+S");
    }

    @Test
    public void resolvePlatformShortcut_shouldKeepExplicitModifiersUntouched() {
        String result = ShortcutConverter.resolvePlatformShortcut("CTRL+SHIFT+Z");

        assertThat(result).isEqualTo("CTRL+SHIFT+Z");
    }

    @Test
    public void resolvePlatformShortcut_shouldUpperCaseParts() {
        String result = ShortcutConverter.resolvePlatformShortcut("shortcut+s");

        assertThat(result).isEqualTo(ShortcutConverter.getPlatformModifier() + "+S");
    }

    @Test
    public void resolvePlatformShortcut_shouldReturnInputWhenBlank() {
        String result = ShortcutConverter.resolvePlatformShortcut("");

        assertThat(result).isEmpty();
    }
}
