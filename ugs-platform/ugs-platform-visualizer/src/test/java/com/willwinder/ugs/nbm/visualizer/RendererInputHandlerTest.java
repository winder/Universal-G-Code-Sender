package com.willwinder.ugs.nbm.visualizer;

import com.jogamp.opengl.util.AnimatorBase;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;

import java.awt.Canvas;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RendererInputHandlerTest {

    @Test
    public void hoverUsesComponentPixelCoordinates() {
        // Non-uniform scale so the default-screen transform cannot satisfy the assertion.
        GraphicsConfiguration configuration = mock(GraphicsConfiguration.class);
        when(configuration.getDefaultTransform()).thenReturn(AffineTransform.getScaleInstance(1.25, 1.5));
        Canvas component = new Canvas() {
            @Override
            public GraphicsConfiguration getGraphicsConfiguration() {
                return configuration;
            }
        };
        BackendAPI backend = mock(BackendAPI.class);
        when(backend.getSettings()).thenReturn(mock(Settings.class));
        GcodeRenderer renderer = mock(GcodeRenderer.class);
        RendererInputHandler target = new RendererInputHandler(renderer, mock(AnimatorBase.class), backend, 1, 60);

        target.mouseMoved(new MouseEvent(component, MouseEvent.MOUSE_MOVED, 0, 0,
                50, 25, 50, 25, 0, false, MouseEvent.NOBUTTON));

        verify(renderer).mouseMoved(new Point(63, 38));
    }
}
