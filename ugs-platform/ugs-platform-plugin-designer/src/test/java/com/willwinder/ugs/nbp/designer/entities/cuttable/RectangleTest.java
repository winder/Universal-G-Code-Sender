package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.model.Size;
import org.junit.Test;

import static org.junit.Assert.*;

public class RectangleTest {

    @Test
    public void setSize() {
        Rectangle rectangle = new Rectangle(1, 1);
        rectangle.setSize(new Size(30, 30));
        assertEquals(30, rectangle.getSize().getWidth(), 0.1);
        assertEquals(30, rectangle.getSize().getHeight(), 0.1);
    }

}