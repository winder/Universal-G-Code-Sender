package com.willwinder.ugs.nbm.visualizer.shared;

import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.opengl.DebugGL2;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.universalgcodesender.model.Position;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MouseProjectionUtilsTest {

    @Test
    public void projectsDrawablePixelCoordinatesWithoutScalingAgain() {
        GLAutoDrawable drawable = mock(GLAutoDrawable.class);
        // Mock a concrete GL2 implementation to avoid an oversized interface mock initializer.
        GL2 gl = mock(DebugGL2.class);
        when(drawable.getGL()).thenReturn(gl);
        when(gl.getGL2()).thenReturn(gl);
        NativeSurface surface = mock(NativeSurface.class);
        when(drawable.getNativeSurface()).thenReturn(surface);
        when(surface.convertToPixelUnits(any(int[].class))).thenAnswer(invocation -> {
            int[] point = invocation.getArgument(0);
            point[0] *= 2;
            point[1] *= 2;
            return point;
        });

        doAnswer(invocation -> {
            int[] viewport = invocation.getArgument(1);
            viewport[2] = 200;
            viewport[3] = 100;
            return null;
        }).when(gl).glGetIntegerv(eq(GL.GL_VIEWPORT), any(int[].class), eq(0));
        doAnswer(invocation -> {
            double[] matrix = invocation.getArgument(1);
            matrix[0] = matrix[5] = matrix[10] = matrix[15] = 1;
            return null;
        }).when(gl).glGetDoublev(anyInt(), any(double[].class), eq(0));

        Position position = MouseProjectionUtils.intersectPointWithXYPlane(drawable, 100, 49);

        assertEquals(0.0, position.getX(), 0.000001);
        assertEquals(0.0, position.getY(), 0.000001);
        assertEquals(0.0, position.getZ(), 0.000001);
    }
}
