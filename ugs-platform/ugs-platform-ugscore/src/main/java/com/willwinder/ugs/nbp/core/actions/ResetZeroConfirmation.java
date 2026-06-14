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

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Shared helper that asks the user to confirm before resetting the work
 * coordinate zero. The confirmation can be permanently dismissed through a
 * "Don't ask again" checkbox which is persisted to the application settings.
 * <p>
 * The maintainer requested this behavior in
 * <a href="https://github.com/winder/Universal-G-Code-Sender/issues/3040">issue #3040</a>:
 * a confirmation box with an option to hide future questions, defaulting to ask.
 */
public final class ResetZeroConfirmation {

    private ResetZeroConfirmation() {
    }

    /**
     * Returns {@code true} if the reset-zero action should proceed.
     * <p>
     * When the user has opted out of the confirmation
     * ({@link Settings#isConfirmResetZero()} is {@code false}) this returns
     * {@code true} immediately without showing a dialog. Otherwise it shows a
     * yes/no confirmation dialog with a "Don't ask again" checkbox; checking the
     * box persists the opt-out to the settings.
     *
     * @param backend the active backend, used to read and persist settings
     * @return {@code true} if the user confirmed (or confirmation is disabled),
     * {@code false} if the user cancelled
     */
    public static boolean confirmResetZero(BackendAPI backend) {
        Settings settings = backend.getSettings();
        if (settings == null || !settings.isConfirmResetZero()) {
            return true;
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Localization.getString("platform.actions.resetZero.confirm.message")));
        JCheckBox dontAsk = new JCheckBox(Localization.getString("platform.actions.resetZero.confirm.dontAsk"));
        panel.add(dontAsk);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                Localization.getString("platform.actions.resetZero.confirm.title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (dontAsk.isSelected()) {
            settings.setConfirmResetZero(false);
            SettingsFactory.saveSettings(settings);
        }

        return result == JOptionPane.YES_OPTION;
    }
}
