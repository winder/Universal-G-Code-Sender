package com.willwinder.universalgcodesender.fx.component.visualizer.input;

import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings.ModifierKey;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MouseMappingTest {

    @Test
    public void matchesPress_shouldRequireButtonAndNoModifiersForNone() {
        MouseMapping mapping = new MouseMapping(MouseButton.SECONDARY, ModifierKey.NONE);

        assertThat(mapping.matchesPress(press(MouseButton.SECONDARY, false))).isTrue();
        assertThat(mapping.matchesPress(press(MouseButton.SECONDARY, true))).isFalse();
        assertThat(mapping.matchesPress(press(MouseButton.PRIMARY, false))).isFalse();
    }

    @Test
    public void matchesPress_shouldRequireTheModifier() {
        MouseMapping mapping = new MouseMapping(MouseButton.SECONDARY, ModifierKey.SHIFT);

        assertThat(mapping.matchesPress(press(MouseButton.SECONDARY, true))).isTrue();
        assertThat(mapping.matchesPress(press(MouseButton.SECONDARY, false))).isFalse();
    }

    @Test
    public void matchesPressWithExtraModifiers_shouldAllowModifiersBeyondTheRequiredOne() {
        MouseMapping withoutModifier = new MouseMapping(MouseButton.PRIMARY, ModifierKey.NONE);
        MouseMapping withShift = new MouseMapping(MouseButton.PRIMARY, ModifierKey.SHIFT);

        assertThat(withoutModifier.matchesPressWithExtraModifiers(press(MouseButton.PRIMARY, true))).isTrue();
        assertThat(withoutModifier.matchesPressWithExtraModifiers(press(MouseButton.SECONDARY, false))).isFalse();
        assertThat(withShift.matchesPressWithExtraModifiers(press(MouseButton.PRIMARY, true))).isTrue();
        assertThat(withShift.matchesPressWithExtraModifiers(press(MouseButton.PRIMARY, false))).isFalse();
    }

    @Test
    public void matchesDrag_shouldLookAtHeldButtonsNotTheEventButton() {
        MouseMapping mapping = new MouseMapping(MouseButton.MIDDLE, ModifierKey.NONE);
        MouseEvent drag = new MouseEvent(MouseEvent.MOUSE_DRAGGED, 0, 0, 0, 0, MouseButton.NONE, 1,
                false, false, false, false, false, true, false, false, false, false, null);

        assertThat(mapping.matchesDrag(drag)).isTrue();
    }

    @Test
    public void parse_shouldFallBackOnUnknownValues() {
        MouseMapping mapping = MouseMapping.parse("bogus", "", MouseButton.SECONDARY, ModifierKey.SHIFT);

        assertThat(mapping).isEqualTo(new MouseMapping(MouseButton.SECONDARY, ModifierKey.SHIFT));
    }

    @Test
    public void parse_shouldReadSettingsValuesCaseInsensitively() {
        MouseMapping mapping = MouseMapping.parse("middle", "ctrl", MouseButton.SECONDARY, ModifierKey.NONE);

        assertThat(mapping).isEqualTo(new MouseMapping(MouseButton.MIDDLE, ModifierKey.CTRL));
    }

    static MouseEvent press(MouseButton button, boolean shift) {
        return new MouseEvent(MouseEvent.MOUSE_PRESSED, 0, 0, 0, 0, button, 1,
                shift, false, false, false,
                button == MouseButton.PRIMARY, button == MouseButton.MIDDLE, button == MouseButton.SECONDARY,
                false, false, false, null);
    }
}
