/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 * Sun gratefully acknowledges that this software was originally authored
 * and developed by Kenneth Bradley Russell and Christopher John Kline.
 */

package com.willwinder.universalgcodesender.uielements.helpers;

import com.jogamp.opengl.GLDrawable;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;



/** A simple class which uses the TextRenderer to provide an FPS
    counter overlaid on top of the scene. */

public class FPSCounter {
  // Placement constants
  public static final int UPPER_LEFT  = 1;
  public static final int UPPER_RIGHT = 2;
  public static final int LOWER_LEFT  = 3;
  public static final int LOWER_RIGHT = 4;

  private int textLocation = LOWER_RIGHT;
  private GLDrawable drawable;
  private TextRenderer renderer;
  final static private DecimalFormat format = new DecimalFormat("####.00");
  private int frameCount;
  private long startTime;
  private String fpsText;
  private int fpsMagnitude;
  private int fpsWidth;
  private int fpsHeight;
  private int fpsOffset;
  
  /** Creates a new FPSCounter with the given font size. An OpenGL
      context must be current at the time the constructor is called.

      @param drawable the drawable to render the text to
      @param textSize the point size of the font to use
      @throws GLException if an OpenGL context is not current when the constructor is called
  */
  public FPSCounter(GLDrawable drawable, int textSize) throws GLException {
    this(drawable, new Font("SansSerif", Font.BOLD, textSize));
  }

  /** Creates a new FPSCounter with the given font. An OpenGL context
      must be current at the time the constructor is called.

      @param drawable the drawable to render the text to
      @param font the font to use
      @throws GLException if an OpenGL context is not current when the constructor is called
  */
  public FPSCounter(GLDrawable drawable, Font font) throws GLException {
    this(drawable, font, true, true);
  }

  /** Creates a new FPSCounter with the given font and rendering
      attributes. An OpenGL context must be current at the time the
      constructor is called.

      @param drawable the drawable to render the text to
      @param font the font to use
      @param antialiased whether to use antialiased fonts
      @param useFractionalMetrics whether to use fractional font
      @throws GLException if an OpenGL context is not current when the constructor is called
  */
  public FPSCounter(GLDrawable drawable,
                    Font font,
                    boolean antialiased,
                    boolean useFractionalMetrics) throws GLException {
    this.drawable = drawable;
    renderer = new TextRenderer(font, antialiased, useFractionalMetrics);
  }

  /** Gets the relative location where the text of this FPSCounter
      will be drawn: one of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or
      LOWER_RIGHT. Defaults to LOWER_RIGHT. */
  public int getTextLocation() {
    return textLocation;
  }

  /** Sets the relative location where the text of this FPSCounter
      will be drawn: one of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or
      LOWER_RIGHT. Defaults to LOWER_RIGHT. */
  public void setTextLocation(int textLocation) {
    if (textLocation < UPPER_LEFT || textLocation > LOWER_RIGHT) {
      throw new IllegalArgumentException("textLocation");
    }
    this.textLocation = textLocation;
  }

  /** Changes the current color of this TextRenderer to the supplied
      one, where each component ranges from 0.0f - 1.0f. The alpha
      component, if used, does not need to be premultiplied into the
      color channels as described in the documentation for {@link
      Texture Texture}, although premultiplied colors are used
      internally. The default color is opaque white.

      @param r the red component of the new color
      @param g the green component of the new color
      @param b the blue component of the new color
      @param a the alpha component of the new color, 0.0f =
        completely transparent, 1.0f = completely opaque
      @throws GLException If an OpenGL context is not current when this method is called
  */
  public void setColor(float r, float g, float b, float a) throws GLException {
    renderer.setColor(r, g, b, a);
  }

  /** Updates the FPSCounter's internal timer and counter and draws
      the computed FPS. It is assumed this method will be called only
      once per frame.
  */
  public void draw() {
    if (startTime == 0) {
      startTime = System.currentTimeMillis();
    }

    if (++frameCount >= 100) {
      long endTime = System.currentTimeMillis();
      float fps = 100.0f / (float) (endTime - startTime) * 1000;
      recomputeFPSSize(fps);
      frameCount = 0;
      startTime = System.currentTimeMillis();

      fpsText = "FPS: " + format.format(fps);
    }

    if (fpsText != null) {
      renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
      // Figure out the location at which to draw the text
      int x = 0;
      int y = 0;
      switch (textLocation) {
        case UPPER_LEFT:
          x = fpsOffset;
          y = drawable.getSurfaceHeight() - fpsHeight - fpsOffset;
          break;

        case UPPER_RIGHT:
          x = drawable.getSurfaceWidth() - fpsWidth - fpsOffset;
          y = drawable.getSurfaceHeight() - fpsHeight - fpsOffset;
          break;

        case LOWER_LEFT:
          x = fpsOffset;
          y = fpsOffset;
          break;

        case LOWER_RIGHT:
          x = drawable.getSurfaceWidth() - fpsWidth - fpsOffset;
          y = fpsOffset;
          break;
        default:
          break;
      }

      renderer.draw(fpsText, x, y);
      renderer.endRendering();
    }
  }

  private void recomputeFPSSize(float fps) {
    String fpsText;
    int magnitude;
    if (fps >= 10000) {
      fpsText = "10000.00";
      magnitude = 5;
    } else if (fps >= 1000) {
      fpsText = "1000.00";
      magnitude = 4;
    } else if (fps >= 100) {
      fpsText = "100.00";
      magnitude = 3;
    } else if (fps >= 10) {
      fpsText = "10.00";
      magnitude = 2;
    } else {
      fpsText = "9.00";
      magnitude = 1;
    }

    if (magnitude > this.fpsMagnitude) {
      Rectangle2D bounds = renderer.getBounds("FPS: " + fpsText);
      fpsWidth = (int) bounds.getWidth();
      fpsHeight = (int) bounds.getHeight();
      fpsOffset = (int) (fpsHeight * 0.5f);
      this.fpsMagnitude = magnitude;
    }
  }
}
