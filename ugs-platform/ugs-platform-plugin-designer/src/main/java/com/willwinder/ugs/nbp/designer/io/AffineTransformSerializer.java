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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.awt.geom.AffineTransform;
import java.lang.reflect.Type;

/**
 * @author Joacim Breiler
 */
public class AffineTransformSerializer implements JsonSerializer<AffineTransform> {
    @Override
    public JsonElement serialize(AffineTransform affineTransform, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        double[] matrix = new double[6];
        affineTransform.getMatrix(matrix);

        jsonObject.addProperty("m00", matrix[0]);
        jsonObject.addProperty("m10", matrix[1]);
        jsonObject.addProperty("m01", matrix[2]);
        jsonObject.addProperty("m11", matrix[3]);
        jsonObject.addProperty("m02", matrix[4]);
        jsonObject.addProperty("m12", matrix[5]);
        return jsonObject;
    }
}
