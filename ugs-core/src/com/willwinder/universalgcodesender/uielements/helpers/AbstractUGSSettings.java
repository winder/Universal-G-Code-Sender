/**
 * Abstract settings class with helper widgets, and change detection.
 */
package com.willwinder.universalgcodesender.uielements.helpers;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.utils.ControllerSettings.ProcessorConfig;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author wwinder
 */
public abstract class AbstractUGSSettings extends JPanel {
    final private Collection<Component> components = new ArrayList<>();
    final protected Settings settings;
    final protected IChanged changer;

    public AbstractUGSSettings(Settings settings, IChanged changer) {
        this.settings = settings;
        this.changer = changer;
    }

    protected abstract void updateComponentsInternal(Settings s);
    public abstract void save();
    public abstract String getHelpMessage();
    public abstract void restoreDefaults() throws Exception;

    private void change() {
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
            else if (clazz == JComboBox.class) {
                ((JComboBox)c).addActionListener((ActionEvent e) -> {
                    change();
                });
            }
        }
    }

    public Component addIgnoreChanges(Component comp) {
        return super.add(comp);
    }

    public Component add(Component comp) {
        Component ret = super.add(comp);
        components.add(comp);
        return ret;
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

    protected class ProcessorConfigCheckbox extends JPanel {
        final public JCheckBox box;
        final private ProcessorConfig pc;

        public ProcessorConfigCheckbox(ProcessorConfig pc) {
            this.pc = pc;
            box = new JCheckBox(Localization.getString(pc.name));
            box.setSelected(pc.enabled);
            box.addActionListener((ActionEvent e) -> {
                    pc.enabled = box.isSelected();
                });
            if (!pc.optional) {
                box.setEnabled(false);
            }
            setLayout(new MigLayout("insets 0"));
            add(box, "gapleft 50, w 100");
        }

        public void setSelected(Boolean s) {box.setSelected(s); }
        public boolean getValue() { return box.isSelected(); }
    }
}
