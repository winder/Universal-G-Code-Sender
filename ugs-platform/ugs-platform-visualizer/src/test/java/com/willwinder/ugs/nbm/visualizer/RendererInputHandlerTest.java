package com.willwinder.ugs.nbm.visualizer;

import com.jogamp.opengl.GLAnimatorControl;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.awt.Canvas;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
        RendererInputHandler target = new RendererInputHandler(renderer, mock(GLAnimatorControl.class), backend, 1, 60);

        target.mouseMoved(new MouseEvent(component, MouseEvent.MOUSE_MOVED, 0, 0,
                50, 25, 50, 25, 0, false, MouseEvent.NOBUTTON));

        verify(renderer).mouseMoved(new Point(63, 38));
    }

    @Test
    public void disposeUnregistersOwnedRenderables() {
        BackendAPI backend = mock(BackendAPI.class);
        when(backend.getSettings()).thenReturn(mock(Settings.class));
        GcodeRenderer renderer = mock(GcodeRenderer.class);
        ArgumentCaptor<Renderable> registered = ArgumentCaptor.forClass(Renderable.class);

        RendererInputHandler target = new RendererInputHandler(renderer, mock(GLAnimatorControl.class), backend, 1, 60);
        verify(renderer, times(3)).registerRenderable(registered.capture());

        target.dispose();

        ArgumentCaptor<Renderable> removed = ArgumentCaptor.forClass(Renderable.class);
        verify(renderer, times(3)).removeRenderable(removed.capture());
        List<Renderable> registeredRenderables = registered.getAllValues();
        List<Renderable> removedRenderables = removed.getAllValues();
        assertEquals(registeredRenderables, removedRenderables);
    }
}
