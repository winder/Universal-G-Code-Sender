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
    // Without an explicit UID, Java auto-generates one from the class's
    // exact field layout - any future field addition (like color/icon just
    // now) silently changes it, breaking deserialization of any Macro
    // NetBeans has cached from a previous session (it serializes one into
    // each dynamically-registered per-macro action under the userdir's
    // config/Actions/Macro/). Freezing it here means adding fields in the
    // future won't do that again. Does not affect the actual macro list,
    // which is stored as plain JSON via Gson (see SettingsFactory) - this
    // only matters for that NetBeans action cache.
    @Serial
    private static final long serialVersionUID = 1L;

    private String uuid = UUID.randomUUID().toString();
    private String name;
    private String description;
    private String gcode;
    private MacroVersion version;
    // Dashboard-only styling, optional on every macro (including every one
    // that predates this or was created/edited in the native Settings UI,
    // which doesn't know about these). Kept as plain fields rather than
    // encoded into gcode as a comment specifically so they survive being
    // edited from either UI without any parsing - Settings is persisted via
    // plain Gson reflection (see SettingsFactory), so new fields round-trip
    // automatically with no extra (de)serialization code.
    private String color;
    private String icon;

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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "Macro{" +
                "uuid='" + uuid + '\'' +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", gcode='" + gcode + '\'' +
                ", version=" + version +
                ", color='" + color + '\'' +
                ", icon='" + icon + '\'' +
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
