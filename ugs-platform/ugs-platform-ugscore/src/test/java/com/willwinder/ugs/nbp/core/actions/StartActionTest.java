package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Before;
import org.junit.Test;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;

import java.beans.PropertyChangeEvent;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openide.nodes.Node.PROP_COOKIE;

public class StartActionTest {

    @Before
    public void setUp() throws Exception {
        GUIBackend backend = new GUIBackend();
        Settings settings = new Settings();
        backend.applySettings(settings);
        LookupService.register(backend);
    }

    @Test
    public void isEnabledShouldReturnTrueWhenCanSend() {
        BackendAPI backendAPI = mock(BackendAPI.class);
        when(backendAPI.canSend()).thenReturn(true);

        StartAction startAction = new StartAction(backendAPI);
        assertTrue(startAction.isEnabled());
    }

    @Test
    public void isEnabledShouldReturnFalseOnUnsavedChanges() {
        BackendAPI backendAPI = mock(BackendAPI.class);
        when(backendAPI.canSend()).thenReturn(true);

        StartAction startAction = new StartAction(backendAPI);
        startAction.propertyChange(createUnsavedChangesEvent());

        assertTrue(startAction.isEnabled());
    }

    @Test
    public void isEnabledShouldReturnTrueForResumingOnUnsavedChanges() {
        BackendAPI backendAPI = mock(BackendAPI.class);
        when(backendAPI.canSend()).thenReturn(false);
        when(backendAPI.isPaused()).thenReturn(true);

        StartAction startAction = new StartAction(backendAPI);
        startAction.propertyChange(createUnsavedChangesEvent());

        assertTrue(startAction.isEnabled());
    }

    /**
     * Creates an event that simulates that there are unsaved changes to the gcode document
     *
     * @return an property changed event
     */
    private PropertyChangeEvent createUnsavedChangesEvent() {
        Node node = mock(Node.class);
        SaveCookie saveCookie = mock(SaveCookie.class);
        when(node.getCookie(SaveCookie.class)).thenReturn(saveCookie);
        return new PropertyChangeEvent(node, PROP_COOKIE, null, null);
    }
}
