/*
 * Simple dialog for configuring Sender settings.
 */
/*
    Copywrite 2013-2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.Frame;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public class UGSSettingsDialog extends JDialog {
    private static Logger logger = Logger.getLogger(UGSSettingsDialog.class.getName());

    private boolean saveChanges;
    private final Settings settings;
    private final AbstractUGSSettings settingsPanel;

    private final JButton restore = new JButton(Localization.getString("restore"));
    private final JButton closeWithSave = new JButton(Localization.getString("save.close"));
    private final JButton closeWithoutSave = new JButton(Localization.getString("close"));
    private final JButton helpButton = new JButton(Localization.getString("help"));
    
    /**
     * Creates new form GrblSettingsDialog
     */
    public UGSSettingsDialog(String settingsTitle, Settings settings, AbstractUGSSettings panel, Frame parent, boolean modal) {
        super(parent, modal);
        this.setTitle(settingsTitle);
        this.settings = settings;
        this.settingsPanel = panel;

        // Register callbacks
        restore.addActionListener(this::restoreDefaultSettings);
        closeWithSave.addActionListener(this::closeWithSaveActionPerformed);
        closeWithoutSave.addActionListener(this::closeWithoutSaveActionPerformed);
        helpButton.addActionListener(this::helpButtonActionPerformed);
        
        initComponents();
        setLocationRelativeTo(parent);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        saveChanges = false;
    }
    
    /**
     * Return status.
     */
    public boolean saveChanges() {
        return saveChanges;
    }

    private void initComponents() {
        JScrollPane scrollPane = new JScrollPane(settingsPanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        setLayout(new MigLayout("fillx"));
        add(scrollPane, "wrap, growx, span 4");
        add(helpButton);
        add(restore);
        add(closeWithoutSave);
        add(closeWithSave);

        pack();
    }

    private void closeWithoutSaveActionPerformed(java.awt.event.ActionEvent evt) {
        this.saveChanges = false;
        setVisible(false);
    }

    private void restoreDefaultSettings(java.awt.event.ActionEvent evt) {
        try {
            settingsPanel.restoreDefaults();
            //FirmwareUtils.restoreDefaults();
            //settingsPanel.updateComponents(new Settings());
        } catch (Exception e) {
            String message = "An error occurred while restoring defaults:"
                    + e.getLocalizedMessage();
            logger.log(Level.SEVERE, message, e);
            GUIHelpers.displayErrorDialog(message);
        }
    }

    private void closeWithSaveActionPerformed(java.awt.event.ActionEvent evt) {
        this.settingsPanel.save();
        this.saveChanges = true;
        setVisible(false);
    }

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String message = settingsPanel.getHelpMessage();
        
        JOptionPane.showMessageDialog(new JFrame(), 
                message, 
                Localization.getString("sender.help.dialog.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
