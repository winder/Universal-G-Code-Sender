package com.willwinder.ugs.platform.surfacescanner.ui;

import com.willwinder.ugs.platform.surfacescanner.MeshLevelManager;
import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.ugs.platform.surfacescanner.renderable.AutoLevelPreview;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class AutoLevelerPanelTest {
    @Test
    public void coordinateEditorsRemainVisibleAfterResize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            AutoLevelSettings settings = new AutoLevelSettings();
            settings.setMinX(-12);
            AutoLevelerPanel panel;
            try (MockedStatic<LookupService> lookup = mockStatic(LookupService.class)) {
                BackendAPI backend = mock(BackendAPI.class);
                when(backend.getSettings()).thenReturn(new Settings());
                lookup.when(() -> LookupService.lookup(BackendAPI.class)).thenReturn(backend);
                panel = new AutoLevelerPanel(mock(SurfaceScanner.class), mock(MeshLevelManager.class),
                        mock(AutoLevelPreview.class), settings, UnitUtils.Units.MM);
            }
            JPanel coordinates = (JPanel) panel.getComponent(0);
            JSpinner[] spinners = Arrays.stream(coordinates.getComponents()).filter(JSpinner.class::isInstance)
                    .toArray(JSpinner[]::new);
            Object[] values = Arrays.stream(spinners).map(JSpinner::getValue).toArray();
            for (JSpinner spinner : spinners) {
                Dimension size = new Dimension(120, spinner.getEditor().getPreferredSize().height);
                spinner.getEditor().setMinimumSize(size);
                spinner.getEditor().setPreferredSize(size);
            }

            for (int width : new int[]{580, 1000, 580}) {
                panel.setSize(width, 400);
                panel.doLayout();
                coordinates.doLayout();
                Rectangle previous = null;
                for (int i = 0; i < spinners.length; i++) {
                    JSpinner spinner = spinners[i];
                    assertEquals(values[i], spinner.getValue());
                    spinner.doLayout();
                    Component editor = spinner.getEditor();
                    Rectangle bounds = SwingUtilities.convertRectangle(spinner, editor.getBounds(), panel);
                    assertTrue(bounds.width >= 120);
                    assertTrue(panel.getVisibleRect().contains(bounds));
                    Component heading = coordinates.getComponent(i % 2);
                    assertTrue(heading.getX() >= spinner.getX());
                    assertTrue(heading.getX() + heading.getWidth() <= spinner.getX() + spinner.getWidth());
                    if (i % 2 == 1) {
                        assertTrue(previous.getMaxX() <= bounds.x);
                    }
                    previous = bounds;
                }
            }
        });
    }
}
