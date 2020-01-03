/**
 * Abstract settings class with helper widgets, and change detection.
 */
/*
    Copyright 2016-2020 Will Winder

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
package com.willwinder.universalgcodesender.uielements.helpers;

import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public abstract class AbstractUGSSettings extends JPanel {
    final private Collection<Component> components = new ArrayList<>();
    final protected Settings settings;
    final private IChanged changer;

    public AbstractUGSSettings(Settings settings, IChanged changer) {
        this.settings = settings;
        this.changer = changer;
    }

    protected abstract void updateComponentsInternal(Settings s);
    public abstract void save();
    public abstract String getHelpMessage();
    public abstract void restoreDefaults() throws Exception;

    // not sure when we'd use this so defaulting to true.
    public boolean settingsValid() { return true; }

    protected void change() {
        if (changer != null) changer.changed();
    }

    private void initActions() {
        for (Component c : components) {
            Class clazz = c.getClass();
            if (clazz == Spinner.class) {
                ((Spinner)c).spinner.addChangeListener((ChangeEvent e) -> {
                    change();
                });
            }
            else if (clazz == Checkbox.class) {
                ((Checkbox)c).box.addActionListener((ActionEvent e) -> {
                    change();
                });
            }
            else if (clazz == Textfield.class) {
                ((Textfield)c).text.addPropertyChangeListener("value", (PropertyChangeEvent e) -> {
                    change();
                });
            }
            else if (clazz == JComboBox.class) {
                ((JComboBox)c).addActionListener((ActionEvent e) -> {
                    change();
                });
            }
            else if (clazz == JButton.class) {
                ((JButton)c).addActionListener((ActionEvent e) -> {
                    change();
                });
            }
        }
    }

    public Component addIgnoreChanges(Component comp) {
        return super.add(comp);
    }

    public void addIgnoreChanges(Component comp, Object constraints) {
        super.add(comp, constraints);
    }

    public Component add(Component comp) {
        Component ret = super.add(comp);
        components.add(comp);
        return ret;
    }

    public void add(Component comp, Object constraints) {
        super.add(comp, constraints);
        components.add(comp);
    }

    public Component add(JPanel panel, Component comp) {
        Component ret = panel.add(comp);
        components.add(comp);
        return ret;
    }

    protected void updateComponents() {
        updateComponents(settings);
    }

    public void updateComponents(Settings s) {
        components.clear();
        updateComponentsInternal(s);
        initActions();
    }
    /**
     * Helper object to simplify layout.
     */
    protected class Spinner extends JPanel {
        JLabel label;
        public JSpinner spinner;
        public Spinner(String text, SpinnerModel model) {
            label = new JLabel(text);
            spinner = new JSpinner(model);
            setLayout(new MigLayout("insets 0, wrap 2"));
            add(spinner, "w 70");
            add(label);
        }

        public void setValue(Object v) { spinner.setValue(v); }
        public Object getValue() { return spinner.getValue(); }
    }

    protected class Checkbox extends JPanel {
        public JCheckBox box;
        public Checkbox(String text) {
            box = new JCheckBox(text);
            setLayout(new MigLayout("insets 0"));
            add(box, "gapleft 50, w 100");
        }

        public void setSelected(Boolean s) {box.setSelected(s); }
        public boolean getValue() { return box.isSelected(); }
    }
    
    protected class Textfield extends JPanel {
        JLabel label;
        public JTextField text;
        public Textfield(String labelText) {
            this(labelText, false);
        }
        
        public Textfield(String labelText, boolean labelFirst) {
            label = new JLabel(labelText);
            text = new JTextField();
            setLayout(new MigLayout("insets 0, wrap 2"));
            if (labelFirst) {
                add(label, "w 70");
                add(text);                
            } else {
                add(text, "w 70");
                add(label);
            }
        }
        
        public void setValue(String t) { label.setText(t); }
        public String getValue() { return label.getText(); }
    }
}
