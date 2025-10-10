package com.willwinder.ugs.nbp.designer.gui.selectionsettings.entitysettings;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.gui.FontCombo;
import net.miginfocom.swing.MigLayout;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Text entity settings panel decoupled from EntitySetting and dispatcher.
 * Fires PropertyChangeSupport events for properties: "text", "fontFamily".
 */
@ServiceProvider(service = EntitySettingsComponent.class)
public class TextSettingsPanel extends JPanel implements EntitySettingsComponent {
    public static final String PROP_TEXT = "text";
    public static final String PROP_FONT_FAMILY = "fontFamily";

    private static final String LABEL_CONSTRAINTS = "grow, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS_NO_WRAP = "grow, w 60:60:300, hmin 32, hmax 36";
    private static final String FIELD_CONSTRAINTS = FIELD_CONSTRAINTS_NO_WRAP + ", wrap";

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private JTextField textField; // initialize in constructor to avoid super() timing issues
    private FontCombo fontCombo;   // initialize in constructor to avoid super() timing issues

    private boolean updating = false;
    private String text = "";
    private String fontFamily = "";

    public TextSettingsPanel() {
        super(new MigLayout("insets 0, gap 10, fillx", "[sg label,right] 10 [grow]"));

        // Initialize components after super()
        textField = new JTextField();
        fontCombo = new FontCombo();

        // Text
        add(new JLabel("Text", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(textField, FIELD_CONSTRAINTS + ", spanx");

        // Font
        add(new JLabel("Font", SwingConstants.RIGHT), LABEL_CONSTRAINTS);
        add(fontCombo, FIELD_CONSTRAINTS + ", spanx");

        // Listeners -> fire property changes
        textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { onTextEdited(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { onTextEdited(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onTextEdited(); }
        });
        fontCombo.addActionListener(e -> {
            if (updating) return;
            setFontFamilyInternal(String.valueOf(fontCombo.getSelectedItem()));
        });
    }

    public String getTitle(){
        return "Text";
    }

    private void onTextEdited() {
        if (updating) return;
        setTextInternal(textField.getText());
    }

    // Public setters used by container to sync from selection
    public void setText(String newText) { setTextInternal(newText); }
    public void setFontFamily(String newFontFamily) { setFontFamilyInternal(newFontFamily); }

    private void setTextInternal(String newText) {
        String old = this.text;
        if (old != null && old.equals(newText)) return;
        this.text = newText != null ? newText : "";
        updating = true;
        try { textField.setText(this.text); } finally { updating = false; }
        pcs.firePropertyChange(PROP_TEXT, old, this.text);
    }

    private void setFontFamilyInternal(String newFontFamily) {
        String old = this.fontFamily;
        if (old != null && old.equals(newFontFamily)) return;
        this.fontFamily = newFontFamily != null ? newFontFamily : "";
        updating = true;
        try { fontCombo.setSelectedItem(this.fontFamily.isEmpty() ? null : this.fontFamily); } finally { updating = false; }
        pcs.firePropertyChange(PROP_FONT_FAMILY, old, this.fontFamily);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (textField != null) textField.setEnabled(enabled);
        if (fontCombo != null) fontCombo.setEnabled(enabled);
    }

    // EntitySettingsComponent implementation
    @Override
    public boolean isApplicable(Group selectionGroup) {
        return selectionGroup.getChildren().stream().allMatch(Text.class::isInstance);
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void setFromSelection(Group selectionGroup) {
        Text first = selectionGroup.getChildren().stream()
                .filter(Text.class::isInstance)
                .map(Text.class::cast)
                .findFirst()
                .orElse(null);
        if (first != null) {
            setText(first.getText());
            setFontFamily(first.getFontFamily());
        }
    }

    @Override
    public void applyChangeToSelection(String propertyName, Object newValue, Group selectionGroup) {
        String secureValue = newValue != null ? String.valueOf(newValue) : "";
        if (PROP_TEXT.equals(propertyName)) {
            selectionGroup.getChildren().stream()
                    .filter(Text.class::isInstance)
                    .map(Text.class::cast)
                    .forEach(t -> t.setText(secureValue));
        } else if (PROP_FONT_FAMILY.equals(propertyName)) {
            selectionGroup.getChildren().stream()
                    .filter(Text.class::isInstance)
                    .map(Text.class::cast)
                    .forEach(t -> t.setFontFamily(secureValue));
        }
    }

    @Override
    public void addChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removeChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
}
