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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.ImageUtilities;

/**
 *
 * @author S. Aubrecht
 */
class ContentHeader extends JPanel implements Constants {

    private static final Image IMG_BANNER = ImageUtilities.loadImage(IMAGE_CONTENT_BANNER, true);
    private static final Color COL_BANNER_LEFT = new Color( 28, 82, 157 );
    private static final Color COL_BANNER_RIGHT = new Color( 41, 62, 109 );
    private final Color COL_GRADIENT_START = new Color( 249, 255, 249 );
    private final Color COL_GRADIENT_END = new Color( 237, 241, 244 );
    private final JLabel lblTitle = new JLabel();

    public ContentHeader( String title ) {
        setLayout( new BorderLayout() );
        lblTitle.setText( title );
        lblTitle.setFont( CONTENT_HEADER_FONT );
        lblTitle.setForeground( Color.white );
        add( lblTitle, BorderLayout.WEST );
        setBorder( BorderFactory.createEmptyBorder( 12+18, 34, 15, 34 ) );
    }

    @Override
    protected void paintComponent( Graphics g ) {
        Graphics2D g2d = ( Graphics2D ) g;
        int width = getWidth();
        int height = getHeight();

        g2d.setColor( new Color(162, 162, 162) );
        g2d.drawRect( 0, 0, width, 12 );
        g2d.setPaint( new GradientPaint( 0, 0, COL_GRADIENT_START, 0, 12, COL_GRADIENT_END ) );
        g2d.fillRect( 1, 0, width-2, 12 );

        int imgWidth = IMG_BANNER.getWidth( this );
        int imgX = (width - imgWidth)/2;
        g2d.drawImage( IMG_BANNER, imgX, 13, imgWidth, height-13, this );
        if( imgX > 0 ) {
            g2d.setPaint( COL_BANNER_LEFT );
            g2d.fillRect( 0, 13, imgX, height-13 );
            g2d.setPaint( COL_BANNER_RIGHT );
            g2d.fillRect( width-imgX-1, 13, imgX+1, height-13 );
        }
    }
}
