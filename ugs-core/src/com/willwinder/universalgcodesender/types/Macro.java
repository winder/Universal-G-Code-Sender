package com.willwinder.universalgcodesender.types;

import com.google.common.base.Strings;
import com.google.gson.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Created by Phil on 9/6/2015.
 */
public class Macro implements Serializable {
    private String uuid = UUID.randomUUID().toString();
    private String name;
    private String description;
    private String[] gcode = new String[0];

    public Macro() {
    }

    public Macro(String uuid, String name, String description, String[] gcode) {
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

    public String[] getGcode() {
        return gcode;
    }

    public String getGcodeString() {
        StringBuilder gcodeString = new StringBuilder();

        for (String gcode : gcode) {
            gcodeString.append(gcode).append("\n");
        }

        return gcodeString.toString();
    }

    public void setGcode(String[] gcode) {
        this.gcode = gcode;
    }

    @Override
    public String toString() {
        StringBuilder gcodeString = new StringBuilder();

        for (String gcode : gcode) {
            gcodeString.append(gcode).append("\n");
        }

        return "Macro{" +
                "uuid='" + uuid + '\'' +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", gcode='" + gcodeString + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    // Static inner class for custom serialization
    public static class MacroSerializer implements JsonSerializer<Macro> {
        @Override
        public JsonElement serialize(Macro src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("uuid", src.getUuid());
            jsonObject.addProperty("name", src.getName());
            jsonObject.addProperty("description", src.getDescription());
            jsonObject.addProperty("gcode", src.getGcodeString());
            return jsonObject;
        }
    }

    // Static inner class for custom deserialization
    public static class MacroDeserializer implements JsonDeserializer<Macro> {
        @Override
        public Macro deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            String uuid = jsonObject.get("uuid").getAsString();
            String name = jsonObject.get("name").getAsString();
            String description = jsonObject.has("description") ? jsonObject.get("description").getAsString() : null;
            String[] gcode;

            JsonElement gcodeElement = jsonObject.get("gcode");
            if (gcodeElement.isJsonArray()) {
                gcode = context.deserialize(gcodeElement, String[].class);
            } else {
                gcode = gcodeElement.getAsString().split("\n");
            }

            return new Macro(uuid, name, description, gcode);
        }
    }
}
