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
import org.apache.commons.lang3.StringUtils;

import java.awt.Font;
import java.awt.geom.Rectangle2D;


/** A simple class which uses the TextRenderer to provide an FPS
    counter overlaid on top of the scene. */

public class Overlay {
  // Placement constants
  public static final int UPPER_LEFT  = 1;
  public static final int UPPER_RIGHT = 2;
  public static final int LOWER_LEFT  = 3;
  public static final int LOWER_RIGHT = 4;

  private int textLocation = LOWER_RIGHT;
  private GLDrawable drawable;
  private TextRenderer renderer;

  /** Creates a new Overlay with the given font size. An OpenGL
      context must be current at the time the constructor is called.

      @param drawable the drawable to render the text to
      @param textSize the point size of the font to use
      @throws GLException if an OpenGL context is not current when the constructor is called
  */
  public Overlay(GLDrawable drawable, int textSize) throws GLException {
    this(drawable, new Font(Font.SANS_SERIF, Font.BOLD, textSize));
  }

  /** Creates a new Overlay with the given font. An OpenGL context
      must be current at the time the constructor is called.

      @param drawable the drawable to render the text to
      @param font the font to use
      @throws GLException if an OpenGL context is not current when the constructor is called
  */
  public Overlay(GLDrawable drawable, Font font) throws GLException {
    this(drawable, font, true, true);
  }

  /** Creates a new Overlay with the given font and rendering
      attributes. An OpenGL context must be current at the time the
      constructor is called.

      @param drawable the drawable to render the text to
      @param font the font to use
      @param antialiased whether to use antialiased fonts
      @param useFractionalMetrics whether to use fractional font
      @throws GLException if an OpenGL context is not current when the constructor is called
  */
  public Overlay(GLDrawable drawable,
                    Font font,
                    boolean antialiased,
                    boolean useFractionalMetrics) throws GLException {
      this(drawable, new TextRenderer(font, antialiased, useFractionalMetrics));
  }

    /** Creates a new Overlay with the given font and rendering
     attributes. An OpenGL context must be current at the time the
     constructor is called.

     @param drawable the drawable to render the text to
     @param renderer for rendering text
     @throws GLException if an OpenGL context is not current when the constructor is called
     */
    public Overlay(GLDrawable drawable,
                   TextRenderer renderer) throws GLException {
        this.drawable = drawable;
        this.renderer = renderer;
    }

  /** Gets the relative location where the text of this Overlay
      will be drawn: one of UPPER_LEFT, UPPER_RIGHT, LOWER_LEFT, or
      LOWER_RIGHT. Defaults to LOWER_RIGHT. */
  public int getTextLocation() {
    return textLocation;
  }

  /** Sets the relative location where the text of this Overlay
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

  /** Updates the Overlay. It is assumed this method will be called only
      once per frame.
  */
  public void draw(String text) {
    if (StringUtils.isNotBlank(text)) {
      text = text.trim();

      renderer.beginRendering(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
      
      Rectangle2D bounds = renderer.getBounds(text);
      int width = (int) bounds.getWidth();
      int height = (int) bounds.getHeight();
      int offset = (int) (height * 0.5f);
      
      // Figure out the location at which to draw the text
      int x = 0;
      int y = 0;
      switch (textLocation) {
        case UPPER_LEFT:
          x = offset;
          y = drawable.getSurfaceHeight() - height - offset;
          break;

        case UPPER_RIGHT:
          x = drawable.getSurfaceWidth() - width - offset;
          y = drawable.getSurfaceHeight() - height - offset;
          break;

        case LOWER_LEFT:
          x = offset;
          y = offset;
          break;

        case LOWER_RIGHT:
          x = drawable.getSurfaceWidth() - width - offset;
          y = offset;
          break;

        default:
          break;
      }
      
      renderer.draw(text, x, y);
      renderer.endRendering();
    }
  }
}
