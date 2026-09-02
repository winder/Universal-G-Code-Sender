package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DesignGcodeServiceTest {
    private Controller controller;
    private BackendAPI backend;
    private DesignGcodeService service;

    @Before
    public void setUp() {
        controller = ControllerFactory.getController();
        controller.newDrawing();
        backend = mock(BackendAPI.class);
        when(backend.isSendingFile()).thenReturn(false);
        service = new DesignGcodeService(controller, backend, "test", Runnable::run);
    }

    @After
    public void tearDown() {
        service.unbind();
    }

    @Test
    public void regenerateNow_shouldWriteAndLoadTheGcode() throws Exception {
        service.regenerateNow();

        verify(backend).setGcodeFile(any(File.class));
    }

    @Test
    public void regenerateAsync_shouldLoadWithoutWaitingForTheDebounce() throws Exception {
        service.bind();

        service.regenerateAsync();

        verify(backend, timeout(300)).setGcodeFile(any(File.class));
    }

    @Test
    public void designChanges_shouldRegenerateOnceAfterTheyStop() throws Exception {
        service.bind();

        controller.getModel().insertEntity(new Rectangle(0, 0, 10, 10));
        controller.getModel().insertEntity(new Rectangle(20, 0, 10, 10));
        controller.getModel().insertEntity(new Rectangle(40, 0, 10, 10));

        verify(backend, timeout(3000)).setGcodeFile(any(File.class));
        Thread.sleep(700);
        verify(backend, timeout(100).times(1)).setGcodeFile(any(File.class));
    }

    @Test
    public void designChanges_shouldReportBusyUntilTheGcodeIsLoaded() throws Exception {
        service.bind();

        controller.getModel().insertEntity(new Rectangle(0, 0, 10, 10));

        assertThat(service.busyProperty().get()).isTrue();
        verify(backend, timeout(3000)).setGcodeFile(any(File.class));
        for (int i = 0; i < 50 && service.busyProperty().get(); i++) {
            Thread.sleep(20);
        }
        assertThat(service.busyProperty().get()).isFalse();
    }

    @Test
    public void designChanges_shouldAbortARunningRegeneration() throws Exception {
        // A load that takes two seconds unless interrupted.
        doAnswer(invocation -> {
            Thread.sleep(2000);
            return null;
        }).when(backend).setGcodeFile(any(File.class));
        service.bind();
        service.regenerateAsync();
        Thread.sleep(300);

        controller.getModel().insertEntity(new Rectangle(0, 0, 10, 10));

        // Without the abort the second load would only start after the first one finishes.
        verify(backend, timeout(1500).times(2)).setGcodeFile(any(File.class));
    }

    @Test
    public void designChanges_shouldBeIgnoredWhenNotBound() throws Exception {
        controller.getModel().insertEntity(new Rectangle(0, 0, 10, 10));

        Thread.sleep(800);
        verify(backend, never()).setGcodeFile(any(File.class));
    }
}
