/**
 * This was an attempt at data binding with NetBeans. There's got to be a better
 * way, but basically this is how it works...
 * 
 * The class returned by getPreferenceClass() is a singleton which is stored in
 * the CentralLookup, objects extending this class can either create their own,
 * or use a shared class and they will all be synchronized on the same property
 * file.
 * 
 * On construction the initComboBox function will be called to populate the box.
 * 
 */
/*
    Copywrite 2015-2016 Will Winder

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
package com.willwinder.ugs.nbp.core.connection;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.event.ActionEvent;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JComboBox;
import org.openide.util.NbPreferences;

/**
 *
 * @author wwinder
 */
public abstract class ComboWithPreferences extends JComboBox<String> implements UGSEventListener {

    /**
     * Not a great use of this pattern, but I don't like this pattern so hack in
     * a common enabled state based on connection.
     */
    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent ugse) {
        this.setEnabled(!backend.isConnected());
    }

    boolean initializing;
    Preferences pref;
    BackendAPI backend;

    abstract Class getPreferenceClass();
    abstract String getPreferenceName();
    abstract String getDefaultValue();
    abstract protected void initComboBox();
    
    public ComboWithPreferences() {
        initializing = true;
        
        pref = NbPreferences.forModule(getPreferenceClass());
        //initializePreference();

        this.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeEvent(evt);
            }
        });

        pref.addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                prefChange(evt);
            }
        });
        
        initComboBox();
        setCombo(getPreference());
        
        initializing = false;
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
    }
    
    final protected void setCombo(String value) {
        for (int i = 0; i < this.getItemCount(); i++) {
            if (this.getItemAt(i).equals(value)) {
                this.setSelectedIndex(i);
                return;
            }
        }
        
        if (this.getSelectedItem() == null || !this.getSelectedItem().equals(value)) {
            if (this.isEditable()) {
                this.getEditor().setItem(value);
            }
        }
    }
    
    private void initializePreference() {
        pref = NbPreferences.forModule(getPreferenceClass());

        Object item = this.getSelectedItem();
        if (item != null) {
            setPreference(this.getSelectedItem().toString());
        }
    }
    
    public void setInitializing(Boolean initializing) {
        this.initializing = initializing;
    }
    
    protected String getPreference() {
        return pref.get(getPreferenceName(), getDefaultValue());
    }
    
    protected void setPreference(String pref) {
        NbPreferences.forModule(this.getPreferenceClass()).put(this.getPreferenceName(), pref);
    }
    
    /**
     * When a preference changes call the set function on the preference to
     * notify others.
     */
    protected void changeEvent(ActionEvent evt) {
        if (initializing) return;
        Object item = this.getSelectedItem();
        if (item != null) {
            setPreference(item.toString());
        }
    }
    
    /**
     * Update combo if this was our preference.
     */
    protected void prefChange(PreferenceChangeEvent evt) {
        initializing = true;
        try {
            if (evt.getKey().equals(this.getPreferenceName()))
                setCombo(evt.getNewValue());
        } finally {
            initializing = false;
        }
    }
}
