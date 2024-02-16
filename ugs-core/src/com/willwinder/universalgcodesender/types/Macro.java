package com.willwinder.universalgcodesender.types;

import com.google.common.base.Strings;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.util.Objects;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Phil on 9/6/2015.
 */
public class Macro implements Serializable {
    private String uuid = UUID.randomUUID().toString();
    private String name;
    private String description;
    private String gcode;
    private MacroVersion version;

    public Macro() {
    }

    public Macro(String uuid, String name, String description, String gcode) {
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.gcode = gcode;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getNameAndDescription(){
        if (!Strings.isNullOrEmpty(getName()) && !Strings.isNullOrEmpty(getDescription())){
            return String.format("%s: %s", getName(), getDescription());
        } else {
            if (!Strings.isNullOrEmpty(getName())){
                return this.getName();
            } else if (!Strings.isNullOrEmpty(getDescription())){
                return this.getDescription();
            } else {
                return "";
            }
        }
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGcode() {
        return gcode;
    }

    public void setGcode(String gcode) {
        this.gcode = gcode;
    }

    public MacroVersion getVersion() {
        return version;
    }

    public void setVersion(MacroVersion version) {
        this.version = Objects.requireNonNullElse(version, MacroVersion.V1);
    }

    @Override
    public String toString() {
        return "Macro{" +
                "uuid='" + uuid + '\'' +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", gcode='" + gcode + '\'' +
                ", version=" + version +
                '}';
    }

    @Serial
    public Object readResolve() {
        this.version = Objects.requireNonNullElse(this.version, MacroVersion.V2);
        return this;
    }

    @Serial
    public Object writeReplace() {
        this.version = Objects.requireNonNullElse(this.version, MacroVersion.V1);
        return this;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
}
