/*
    Copyright 2026 Will Winder

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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResetZeroConfirmationTest {

    @Test
    public void confirmResetZeroShouldReturnTrueWithoutDialogWhenConfirmationDisabled() {
        Settings settings = new Settings();
        settings.setConfirmResetZero(false);

        BackendAPI backend = mock(BackendAPI.class);
        when(backend.getSettings()).thenReturn(settings);

        // Must not block on a Swing dialog; returns true immediately.
        assertTrue(ResetZeroConfirmation.confirmResetZero(backend));

        // The opt-out state is left untouched (no mutation when already disabled).
        assertFalse(settings.isConfirmResetZero());
    }

    @Test
    public void confirmResetZeroShouldReturnTrueWhenSettingsAreUnavailable() {
        BackendAPI backend = mock(BackendAPI.class);
        when(backend.getSettings()).thenReturn(null);

        assertTrue(ResetZeroConfirmation.confirmResetZero(backend));
    }

    @Test
    public void confirmResetZeroSettingShouldDefaultToTrue() {
        assertTrue(new Settings().isConfirmResetZero());
    }

    @Test
    public void settingConfirmResetZeroShouldUpdateTheValue() {
        Settings settings = new Settings();
        settings.setConfirmResetZero(false);
        assertFalse(settings.isConfirmResetZero());

        settings.setConfirmResetZero(true);
        assertTrue(settings.isConfirmResetZero());
    }
}
