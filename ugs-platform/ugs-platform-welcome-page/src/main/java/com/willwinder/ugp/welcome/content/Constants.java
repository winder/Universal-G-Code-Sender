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

package com.willwinder.ugp.welcome.content;

import java.awt.Color;
import java.awt.Font;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 *
 * @author S. Aubrecht
 */
public interface Constants {

    static final String COLOR_SECTION_HEADER = "SectionHeaderColor"; //NOI18N
    static final String COLOR_BIG_BUTTON = "BigButtonColor"; //NOI18N
    static final String COLOR_BOTTOM_BAR = "BottomBarColor"; //NOI18N
    static final String COLOR_BORDER = "BorderColor"; //NOI18N
    //static final Color COLOR_TAB_BACKGROUND = new Color(2,26,114);
    static final Color COLOR_TAB_BACKGROUND = new Color(30, 86, 157);
    static final Color COLOR_TAB_CONTENT_BACKGROUND = Color.WHITE;
    static final Color COLOR_TAB_CONTENT_BACKGROUND2 = Color.LIGHT_GRAY;
    
    static final int FONT_SIZE = 11;
    static final String FONT_NAME = "Arial";
    static final Font BUTTON_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE+1 );
    static final Font RSS_DESCRIPTION_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE-1 );
    static final Font TAB_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE+1 ); //NOI18N
    static final Font SECTION_HEADER_FONT = new Font( FONT_NAME, Font.BOLD, FONT_SIZE+7 ); //NOI18N
    static final Font GET_STARTED_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE+1 ) ; //NOI18N
    static final Font CONTENT_HEADER_FONT = new Font( FONT_NAME, Font.BOLD, FONT_SIZE+13 ) ; //NOI18N

    static final Font FEATURE_FONT = new Font( FONT_NAME, Font.PLAIN, FONT_SIZE+7 ) ; //NOI18N
    static final Font FEATURE_HEADER_FONT = new Font( FONT_NAME, Font.BOLD, FONT_SIZE+7 ) ; //NOI18N

    static final String IMAGE_CONTENT_BANNER = "com/willwinder/ugp/welcome/resources/content_banner.png"; // NOI18N
    static final String IMAGE_TAB_SELECTED = "com/willwinder/ugp/welcome/resources/tab_selected.png"; // NOI18N
    static final String IMAGE_TAB_ROLLOVER = "com/willwinder/ugp/welcome/resources/tab_rollover.png"; // NOI18N
    static final String IMAGE_LOGO = "com/willwinder/ugp/welcome/resources/ugs_logo.gif"; // NOI18N

    //static final String BROKEN_IMAGE = "org/netbeans/modules/welcome/resources/broken_image.gif"; // NOI18N
    //static final String IMAGE_PICTURE_FRAME = "org/netbeans/modules/welcome/resources/picture_frame.png"; // NOI18N

    static final int TEXT_INSETS_LEFT = 10;
    static final int TEXT_INSETS_RIGHT = 10;

    static final Border HEADER_TEXT_BORDER = BorderFactory.createEmptyBorder( 1, TEXT_INSETS_LEFT, 1, TEXT_INSETS_RIGHT );
    
    static final int START_PAGE_MIN_WIDTH = 700;

    static final Logger USAGE_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.projects"); //NOI18N
}
