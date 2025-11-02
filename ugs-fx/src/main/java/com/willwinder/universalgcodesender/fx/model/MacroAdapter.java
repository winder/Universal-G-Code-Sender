/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.model;

import com.willwinder.universalgcodesender.types.Macro;
import javafx.beans.property.StringProperty;
import javafx.beans.property.adapter.JavaBeanStringProperty;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringProperty;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringPropertyBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * An adapter for a Macro that exposes its members as properties
 *
 * @author Joacim Breiler
 */
public class MacroAdapter {
    private final Macro macro;
    private final ReadOnlyJavaBeanStringProperty uuidProperty;
    private final JavaBeanStringProperty nameProperty;
    private final JavaBeanStringProperty descriptionProperty;
    private final JavaBeanStringProperty gcodeProperty;

    public MacroAdapter(Macro macro) {
        this.macro = macro;

        try {
            this.uuidProperty = ReadOnlyJavaBeanStringPropertyBuilder.create()
                    .bean(macro)
                    .name("uuid")
                    .build();

            this.nameProperty = JavaBeanStringPropertyBuilder.create()
                    .bean(macro)
                    .name("name")
                    .build();

            this.descriptionProperty = JavaBeanStringPropertyBuilder.create()
                    .bean(macro)
                    .name("description")
                    .build();

            this.gcodeProperty = JavaBeanStringPropertyBuilder.create()
                    .bean(macro)
                    .name("gcode")
                    .build();

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public StringProperty descriptionProperty() {
        return descriptionProperty;
    }

    public StringProperty gcodeProperty() {
        return gcodeProperty;
    }

    public Macro getMacro() {
        return macro;
    }

    public String getUuidProperty() {
        return uuidProperty.get();
    }

    public ReadOnlyJavaBeanStringProperty uuidPropertyProperty() {
        return uuidProperty;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MacroAdapter other)) return false;

        return new EqualsBuilder()
                .append(getUuid(), other.getUuid())
                .append(getName(), other.getName())
                .append(getDescription(), other.getDescription())
                .append(getGcode(), other.getGcode())
                .isEquals();
    }

    public String getName() {
        return nameProperty.get();
    }

    private String getDescription() {
        return descriptionProperty.get();
    }

    public String getGcode() {
        return gcodeProperty.get();
    }

    public String getUuid() {
        return uuidProperty.get();
    }

}
