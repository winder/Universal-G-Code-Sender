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

package com.willwinder.ugp.welcome;

import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import java.awt.Color;
import java.awt.Font;

/**
 * @author S. Aubrecht
 */
public class Constants {
    public static final int FONT_SIZE = 11;
    public static final String FONT_NAME = "Arial";
    public static final Font TAB_FONT = new Font(FONT_NAME, Font.BOLD, FONT_SIZE + 2); //NOI18N
    public static final Font GET_STARTED_FONT = new Font(FONT_NAME, Font.PLAIN, FONT_SIZE + 4); //NOI18N
    public static final Font CONTENT_HEADER_FONT = new Font(FONT_NAME, Font.BOLD, FONT_SIZE + 12); //NOI18N

    public static final Color COLOR_HEADER_BACKGROUND = ThemeColors.DARK_GREY;

    public static final Color COLOR_TEXT = ThemeColors.DARK_GREY;
    public static final Color COLOR_TEXT_DARK_LAF = ThemeColors.LIGHT_GREY;

    public static final Color COLOR_CONTENT_BACKGROUND = Color.WHITE;
    public static final Color COLOR_CONTENT_BACKGROUND_DARK_LAF = ThemeColors.VERY_DARK_GREY;


    public static final String IMAGE_TAB_SELECTED = "com/willwinder/ugp/welcome/resources/tab_selected.png"; // NOI18N
    public static final String IMAGE_TAB_ROLLOVER = "com/willwinder/ugp/welcome/resources/tab_rollover.png"; // NOI18N
    public static final String IMAGE_LOGO = "com/willwinder/ugp/welcome/resources/ugs_logo.png"; // NOI18N

    public static final int START_PAGE_MIN_WIDTH = 500;
}
