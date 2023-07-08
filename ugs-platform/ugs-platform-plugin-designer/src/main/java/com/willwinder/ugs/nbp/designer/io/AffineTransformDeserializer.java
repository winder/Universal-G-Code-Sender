/*
    Copyright 2023 Will Winder
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
package com.willwinder.ugs.nbp.designer.io;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.awt.geom.AffineTransform;
import java.lang.reflect.Type;

/**
 * @author Joacim Breiler
 */
public class AffineTransformDeserializer implements JsonDeserializer<AffineTransform> {
    @Override
    public AffineTransform deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject affineTransform = jsonElement.getAsJsonObject();
        double m00 = affineTransform.get("m00").getAsDouble();
        double m10 = affineTransform.get("m10").getAsDouble();
        double m01 = affineTransform.get("m01").getAsDouble();
        double m11 = affineTransform.get("m11").getAsDouble();
        double m02 = affineTransform.has("m02") ? affineTransform.get("m02").getAsDouble() : 0;
        double m12 = affineTransform.has("m12") ? affineTransform.get("m12").getAsDouble() : 0;
        return new AffineTransform(m00, m10, m01, m11, m02, m12);
    }
}