/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings;

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import com.willwinder.universalgcodesender.uielements.components.PercentSpinner;
import com.willwinder.universalgcodesender.uielements.components.UnitSpinner;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This event listener will listen to different types of components and dispatch
 * the value change in the same way using {@link FieldEventListener#onFieldUpdate(EntitySetting, Object)}.
 *
 * @author Joacim Breiler
 */
public class FieldEventDispatcher {
    private final Set<FieldEventListener> listeners = ConcurrentHashMap.newKeySet();
    private final EnumMap<EntitySetting, JComponent> componentsMap = new EnumMap<>(EntitySetting.class);
    private AtomicBoolean enabled = new AtomicBoolean(false);

    /**
     * Installs a listener to receive notification when the text of any
     * {@code JTextComponent} is changed. Internally, it installs a
     * {@link DocumentListener} on the text component's {@link Document},
     * and a {@link PropertyChangeListener} on the text component to detect
     * if the {@code Document} itself is replaced.
     * <p>
     * <a href="https://stackoverflow.com/questions/3953208/value-change-listener-to-jtextfield">Source</a>
     *
     * @param text           any text component, such as a {@link JTextField}
     *                       or {@link JTextArea}
     * @param changeListener a listener to receieve {@link ChangeEvent}s
     *                       when the text is changed; the source object for the events
     *                       will be the text component
     * @throws NullPointerException if either parameter is null
     */
    public static void addChangeListener(JTextComponent text, ChangeListener changeListener) {
        Objects.requireNonNull(text);
        Objects.requireNonNull(changeListener);
        DocumentListener dl = new DocumentListener() {
            private int lastChange = 0;
            private int lastNotifiedChange = 0;

            @Override
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lastChange++;
                SwingUtilities.invokeLater(() -> {
                    if (lastNotifiedChange != lastChange) {
                        lastNotifiedChange = lastChange;
                        changeListener.stateChanged(new ChangeEvent(text));
                    }
                });
            }
        };
        text.addPropertyChangeListener("document", (PropertyChangeEvent e) -> {
            Document d1 = (Document) e.getOldValue();
            Document d2 = (Document) e.getNewValue();
            if (d1 != null) d1.removeDocumentListener(dl);
            if (d2 != null) d2.addDocumentListener(dl);
            dl.changedUpdate(null);
        });
        Document d = text.getDocument();
        if (d != null) d.addDocumentListener(dl);
    }

    public void registerListener(EntitySetting entitySetting, TextFieldWithUnit component) {
        componentsMap.put(entitySetting, component);
        component.addPropertyChangeListener("value", this::valueUpdated);
    }

    public void registerListener(EntitySetting entitySetting, PercentSpinner component) {
        componentsMap.put(entitySetting, component);
        component.addChangeListener((ChangeEvent propertyChangeEvent) -> {
            Object source = propertyChangeEvent.getSource();
            componentsMap.entrySet()
                    .stream()
                    .filter(entrySet -> entrySet.getValue() == source)
                    .findFirst()
                    .ifPresent(entry -> updateValue(entry.getKey(), component.getDoubleValue() * 100d));
        });
    }

    public void registerListener(EntitySetting entitySetting, UnitSpinner component) {
        componentsMap.put(entitySetting, component);
        component.addChangeListener(this::spinnerValueUpdated);

        component.addPropertyChangeListener("value", this::valueUpdated);
    }

    private void spinnerValueUpdated(ChangeEvent changeEvent) {
        Object source = changeEvent.getSource();
        componentsMap.entrySet()
                .stream()
                .filter(entrySet -> entrySet.getValue() == source)
                .findFirst()
                .ifPresent(entry -> updateValue(entry.getKey(), ((JSpinner) entry.getValue()).getValue()));
    }

    public void registerListener(EntitySetting entitySetting, JTextComponent component) {
        componentsMap.put(entitySetting, component);
        addChangeListener(component, l -> updateValue(entitySetting, component.getText()));
    }

    public void registerListener(EntitySetting entitySetting, JComboBox<?> component) {
        componentsMap.put(entitySetting, component);
        component.addItemListener(l -> updateValue(entitySetting, l.getItem()));
    }
    
    public void registerListener(EntitySetting entitySetting, JCheckBox component) {
        componentsMap.put(entitySetting, component);
        component.addActionListener((e) -> {
            updateValue(entitySetting, component.isSelected());
        });
    }
    
    private void valueUpdated(PropertyChangeEvent propertyChangeEvent) {
        Object source = propertyChangeEvent.getSource();
        componentsMap.entrySet()
                .stream()
                .filter(entrySet -> entrySet.getValue() == source)
                .findFirst()
                .ifPresent(entry -> updateValue(entry.getKey(), propertyChangeEvent.getNewValue()));
    }


    public void updateValue(EntitySetting entitySetting, Object object) {
        if (this.enabled.get()) {
            listeners.forEach(l -> l.onFieldUpdate(entitySetting, object));
        }
    }

    public void addListener(FieldEventListener listener) {
        listeners.add(listener);
    }

    public void registerListener(EntitySetting entitySetting, JSlider slider) {
        componentsMap.put(entitySetting, slider);
        slider.addChangeListener(l -> updateValue(entitySetting, slider.getValue()));
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
}
