/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.willwinder.universalgcodesender.utils;

import java.awt.image.RGBImageFilter;
import java.util.Hashtable;

public class DisabledIconFilter extends RGBImageFilter {
    public static final RGBImageFilter INSTANCE_LIGHT = new DisabledIconFilter(false);
    public static final RGBImageFilter INSTANCE_DARK = new DisabledIconFilter(true);
    private final int baseGray;

    DisabledIconFilter(boolean dark) {
        canFilterIndexColorModel = true;
        baseGray = dark ? 0x444444 : 0x888888;
    }

    @Override
    public int filterRGB(int x, int y, int argb) {
        return
                // Keep the alpha channel unmodified.
                (argb & 0xff000000) +
                        // Reduce the color bandwidth by a quarter (>> 2), and mix with gray.
                        baseGray +
                        ((((argb >> 16) & 0xff) >> 2) << 16) +
                        ((((argb >> 8) & 0xff) >> 2) << 8) +
                        ((((argb) & 0xff) >> 2));
    }

    // override the superclass behaviour to not pollute
    // the heap with useless properties strings. Saves tens of KBs
    @Override
    public void setProperties(Hashtable<?, ?> props) {
        consumer.setProperties((Hashtable<?, ?>) props.clone());
    }
}