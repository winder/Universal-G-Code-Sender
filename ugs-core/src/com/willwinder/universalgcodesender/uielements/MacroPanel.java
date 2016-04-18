package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.MainWindow;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.layout.*;

import javax.swing.*;
import javax.swing.GroupLayout;
import java.awt.*;
import java.awt.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class MacroPanel extends JPanel implements UGSEventListener {

    private final BackendAPI backend;
    private final java.util.List<JButton> customGcodeButtons = new ArrayList<JButton>();
    private final java.util.List<JTextField> customGcodeTextFields = new ArrayList<JTextField>();
    private final java.util.List<JTextField> customGcodeNameFields = new ArrayList<JTextField>();
    private final java.util.List<JTextField> customGcodeDescriptionFields = new ArrayList<JTextField>();

    private final JLabel buttonLabel = new JLabel(Localization.getString("macroPanel.button"));
    private final JLabel nameLabel = new JLabel(Localization.getString("macroPanel.name"));
    private final JLabel gcodeLabel = new JLabel(Localization.getString("macroPanel.text"));
    private final JLabel decriptionLabel = new JLabel(Localization.getString("macroPanel.description"));

    @Deprecated
    public MacroPanel() {
        this(null);
    }

    public MacroPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }
    }

    @Deprecated
    public MacroPanel(Settings settings, BackendAPI backend) {
        this(backend);
    }

    @Override
    public void doLayout() {
        if (backend == null) {
            //I suppose this should be in a text field.
            System.err.println("settings is null!  Cannot init buttons!");
            return;
        }
        Integer lastMacroIndex = backend.getSettings().getLastMacroIndex()+1;

        for (int i = customGcodeButtons.size(); i <= lastMacroIndex; i++) {
            JButton button = createMacroButton(i);
            JTextField textField = createMacroTextField(i);
            JTextField nameField = createMacroNameField(i);
            JTextField descriptionField = createMacroDescriptionField(i);

            Macro macro = backend.getSettings().getMacro(i);
            if (macro != null) {
                textField.setText(macro.getGcode());
                if (macro.getName() != null) {
                    nameField.setText(macro.getName());
                }
                if (macro.getDescription() != null) {
                    descriptionField.setText(macro.getDescription());
                }
            }
        }

        MigLayout layout = new MigLayout("fill, wrap 4", "[fill, sg 1]r[fill]r[fill, grow 50]r[fill, grow 50]");
        setLayout(layout);
//        add(buttonLabel, "sg 1");
//        add(nameLabel, "w 75!");
//        add(gcodeLabel);
//        add(decriptionLabel);

        for (int i = 0; i < customGcodeButtons.size(); i++) {
            add(customGcodeButtons.get(i), "sg 1");
            add(customGcodeNameFields.get(i), "w 75!");
            add(customGcodeTextFields.get(i));
            add(customGcodeDescriptionFields.get(i));
        }

        super.doLayout();
    }

    private JTextField createMacroTextField(int index) {
        JTextField textField = new MacroTextField(index, backend.getSettings());

        customGcodeTextFields.add(textField);
        return textField;
    }

    private JTextField createMacroNameField(int index) {
        JTextField textField = new MacroNameField(index, backend.getSettings());

        customGcodeNameFields.add(textField);
        return textField;
    }

    private JTextField createMacroDescriptionField(int index) {
        JTextField textField = new MacroDescriptionField(index, backend.getSettings());

        customGcodeDescriptionFields.add(textField);
        return textField;
    }

    private JButton createMacroButton(int i) {
        JButton button = new JButton(i+"");

        button.setEnabled(false);
        this.setToolTipText(Localization.getString("macroPanel.button"));

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customGcodeButtonActionPerformed(i);
            }
        });
        customGcodeButtons.add(button);
        return button;
    }

    private void customGcodeButtonActionPerformed(int i) {
        //Poor coupling here.  We should probably pull the executeCustomGcode method out into the backend.
        if (backend == null) {
            System.err.println("MacroPanel not properly initialized.  Cannot execute custom gcode");
        } else {
            Macro macro = backend.getSettings().getMacro(i);
            executeCustomGcode(macro.getGcode(), backend);
        }
    }

    private void updateCustomGcodeControls(boolean enabled) {
        for (JButton button : customGcodeButtons) {
            button.setEnabled(enabled);
        }
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        updateCustomGcodeControls(backend.isIdle());
    }

    public static void executeCustomGcode(String str, BackendAPI backend)
    {
        if (str == null) {
            return;
        }

        str = str.replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", "");
        final String[] parts = str.split(";");
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String cmd : parts)
                    {
                        backend.sendGcodeCommand(cmd);
                    }
                } catch (Exception ex) {
                    GUIHelpers.displayErrorDialog(ex.getMessage());
                }
            }
        });
    }
}
