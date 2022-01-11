/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.uielements.helpers;

import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.util.awt.TextRenderer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.awt.geom.Rectangle2D;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * @author Joacim Breiler
 */
public class OverlayTest {

    private Overlay target;

    @Mock
    private GLDrawable drawable;

    @Mock
    private TextRenderer renderer;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(renderer.getBounds(any()))
                .thenReturn(new Rectangle2D.Double(0, 0, 0, 0));

        // Initialize the test target
        target = new Overlay(drawable, renderer);
    }

    @Test
    public void drawingNulledTextShouldDoNothing() {
        target.draw(null);
        verifyNoInteractions(drawable);
        verifyNoInteractions(renderer);
    }

    @Test
    public void drawingEmptyTextShouldDoNothing() {
        target.draw("");
        verifyNoInteractions(drawable);
        verifyNoInteractions(renderer);
    }

    @Test
    public void drawingBlankSpaceTextShouldDoNothing() {
        target.draw(" ");
        verifyNoInteractions(drawable);
        verifyNoInteractions(renderer);
    }

    @Test
    public void drawingTextShouldBeDrawn() {
        target.draw(" FPS: 123");
        verify(renderer, atLeastOnce())
                .draw(any(), anyInt(), anyInt());
    }
}
