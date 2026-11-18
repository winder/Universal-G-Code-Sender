/*
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
package com.willwinder.ugs.platform.surfacescanner;

import com.willwinder.ugs.nbp.core.statusline.SeparatorPanel;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import org.openide.awt.Actions;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Status-bar entry that shows whether AutoLeveler correction is active.
 * Hidden until a valid surface exists. Click toggles Apply to Gcode;
 * double-click opens the AutoLeveler window.
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 4)
public class AutoLevelerStatusLineService implements StatusLineElementProvider {
    private static final int CLICK_DELAY_MS = 250;
    private static final Color COLOR_OFF = new Color(0x61, 0x61, 0x61);

    private final JLabel label;
    private final JComponent panel;

    public AutoLevelerStatusLineService() {
        label = new AutoLevelerStatusLine();
        panel = new SeparatorPanel(label);
        ((AutoLevelerStatusLine) label).setVisibilityTarget(panel);
        ((AutoLevelerStatusLine) label).updateLabel();
    }

    @Override
    public Component getStatusLineElement() {
        return panel;
    }

    private static final class AutoLevelerStatusLine extends JLabel implements UGSEventListener {
        private final BackendAPI backend;
        private final AutoLevelerSession session;
        private Timer clickTimer;
        private JComponent visibilityTarget;

        private AutoLevelerStatusLine() {
            backend = LookupService.lookup(BackendAPI.class);
            AutoLevelerSession lookupSession = Lookup.getDefault().lookup(AutoLevelerSession.class);
            session = lookupSession != null ? lookupSession : new AutoLevelerSession();
            session.initialize(backend);
            session.getScanner().addListener(this::updateLabel);

            backend.addUGSEventListener(this);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    if (e.getClickCount() >= 2) {
                        cancelPendingClick();
                        openAutoLeveler();
                    } else if (e.getClickCount() == 1) {
                        scheduleToggle();
                    }
                }
            });
        }

        private void setVisibilityTarget(JComponent visibilityTarget) {
            this.visibilityTarget = visibilityTarget;
        }

        private void scheduleToggle() {
            cancelPendingClick();
            clickTimer = new Timer(CLICK_DELAY_MS, e -> toggleApplyToGcode());
            clickTimer.setRepeats(false);
            clickTimer.start();
        }

        private void cancelPendingClick() {
            if (clickTimer != null) {
                clickTimer.stop();
                clickTimer = null;
            }
        }

        private void toggleApplyToGcode() {
            boolean enabled = backend.getSettings().getAutoLevelSettings().getApplyToGcode();
            backend.getSettings().getAutoLevelSettings().setApplyToGcode(!enabled);
            MeshLevelManager meshLevelManager = session.getMeshLevelManager();
            if (meshLevelManager != null) {
                meshLevelManager.update();
            }
            updateLabel();
        }

        private void openAutoLeveler() {
            TopComponent tc = WindowManager.getDefault().findTopComponent("AutoLevelerTopComponent");
            if (tc != null) {
                tc.open();
                tc.requestActive();
                return;
            }
            Action openAction = Actions.forID(
                    AutoLevelerTopComponent.AutoLevelerCategory,
                    AutoLevelerTopComponent.AutoLevelerActionId);
            if (openAction != null) {
                openAction.actionPerformed(null);
            }
        }

        private void updateLabel() {
            SwingUtilities.invokeLater(() -> {
                SurfaceScanner scanner = session.getScanner();
                boolean hasPoints = scanner != null && scanner.isValid();
                if (visibilityTarget != null) {
                    visibilityTarget.setVisible(hasPoints);
                }
                if (!hasPoints) {
                    setText("");
                    return;
                }

                boolean applyEnabled = backend.getSettings().getAutoLevelSettings().getApplyToGcode();
                setText(applyEnabled
                        ? Localization.getString("autoleveler.status.on")
                        : Localization.getString("autoleveler.status.off"));
                setForeground(applyEnabled ? defaultForeground() : COLOR_OFF);
                setToolTipText(Localization.getString("autoleveler.status.tooltip"));
            });
        }

        private static Color defaultForeground() {
            Color color = UIManager.getColor("Label.foreground");
            return color != null ? color : new JLabel().getForeground();
        }

        @Override
        public void UGSEvent(UGSEvent evt) {
            if (evt instanceof SettingChangedEvent) {
                updateLabel();
            }
        }
    }
}
