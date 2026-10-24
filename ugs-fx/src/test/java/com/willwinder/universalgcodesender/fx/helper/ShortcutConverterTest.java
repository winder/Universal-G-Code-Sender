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

    @Test
    public void hasCommandModifier_shouldBeTrueForControlMetaOrAlt() {
        assertThat(ShortcutConverter.hasCommandModifier("CTRL+Z")).isTrue();
        assertThat(ShortcutConverter.hasCommandModifier("META+SHIFT+Z")).isTrue();
        assertThat(ShortcutConverter.hasCommandModifier("alt+f4")).isTrue();
    }

    @Test
    public void hasCommandModifier_shouldBeFalseForPlainOrShiftedKeys() {
        assertThat(ShortcutConverter.hasCommandModifier("R")).isFalse();
        assertThat(ShortcutConverter.hasCommandModifier("SHIFT+R")).isFalse();
        assertThat(ShortcutConverter.hasCommandModifier("DELETE")).isFalse();
        assertThat(ShortcutConverter.hasCommandModifier("")).isFalse();
        assertThat(ShortcutConverter.hasCommandModifier(null)).isFalse();
    }

    @Test
    public void toDisplayString_shouldCapitalizeModifiersAndNameTheKey() {
        String result = ShortcutConverter.toDisplayString("CTRL+SHIFT+Z");

        assertThat(result).isEqualTo("Ctrl+Shift+Z");
    }

    @Test
    public void toDisplayString_shouldUseTheKeyCodeName() {
        String result = ShortcutConverter.toDisplayString("DELETE");

        assertThat(result).isEqualTo("Delete");
    }

    @Test
    public void toDisplayString_shouldShowMetaAsCmdOnMac() {
        String expected = SystemUtils.IS_OS_MAC ? "Cmd+S" : "Meta+S";

        String result = ShortcutConverter.toDisplayString("META+S");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void toDisplayString_shouldReturnEmptyWhenBlank() {
        String result = ShortcutConverter.toDisplayString("");

        assertThat(result).isEmpty();
    }
}
