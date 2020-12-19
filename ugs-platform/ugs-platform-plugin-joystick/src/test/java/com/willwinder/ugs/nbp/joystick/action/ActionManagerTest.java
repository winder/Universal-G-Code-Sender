package com.willwinder.ugs.nbp.joystick.action;

import com.willwinder.ugs.nbp.joystick.Settings;
import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.hamcrest.HamcrestArgumentMatcher;

import javax.swing.*;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActionManagerTest {

    private ActionManager actionManager;

    @Mock
    private ActionRegistrationService actionRegistrationService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        actionManager = new ActionManager(actionRegistrationService);
    }

    @Test
    public void getActionByIdShouldReturnEmptyOptionalIfNotFound() {
        Optional<ActionReference> actionReferenceOptional = Optional.empty();
        when(actionRegistrationService.getActionById(anyString())).thenReturn(actionReferenceOptional);

        Optional<ActionReference> actionReference = actionManager.getActionById("blapp");
        assertFalse(actionReference.isPresent());
    }

    @Test
    public void getActionByIdShouldReturnActionIfFound() {
        ActionReference actionReference = new ActionReference();
        Optional<ActionReference> actionReferenceOptional = Optional.of(actionReference);
        when(actionRegistrationService.getActionById(anyString())).thenReturn(actionReferenceOptional);

        Optional<ActionReference> result = actionManager.getActionById("blapp");
        assertTrue(result.isPresent());
    }

    @Test
    public void registerActionShouldBeAbleToRetrieve() {
        Optional<ActionReference> actionReferenceOptional = Optional.empty();
        when(actionRegistrationService.getActionById(anyString())).thenReturn(actionReferenceOptional);

        Action action = mock(Action.class);
        actionManager.registerAction("blapp", "blopp", action);

        Optional<ActionReference> storedAction = actionManager.getActionById("blapp");
        assertTrue(storedAction.isPresent());

        Set<String> categories = actionManager.getCategories();
        assertTrue(categories.contains("blopp"));
    }

    @Test
    public void getMappedActionShouldReturnAction() {
        // Given
        ActionReference actionReference = new ActionReference();
        Optional<ActionReference> actionReferenceOptional = Optional.of(actionReference);
        when(actionRegistrationService.getActionById(eq("blapp"))).thenReturn(actionReferenceOptional);

        Settings.setActionMapping(JoystickControl.Y, "blapp");

        // When
        Optional<ActionReference> mappedAction = actionManager.getMappedAction(JoystickControl.Y);

        // Then
        assertTrue(mappedAction.isPresent());
    }

    @Test
    public void getMappedActionShouldReturnAnalogAction() {
        // Given
        Action action = mock(Action.class);
        ActionReference actionReference = new ActionReference();
        actionReference.setAction(action);
        Optional<ActionReference> actionReferenceOptional = Optional.of(actionReference);
        when(actionRegistrationService.getActionById(eq("blapp"))).thenReturn(actionReferenceOptional);

        Settings.setActionMapping(JoystickControl.LEFT_X, "blapp");

        // When
        Optional<ActionReference> mappedAction = actionManager.getMappedAction(JoystickControl.LEFT_X);

        // Then
        assertTrue(mappedAction.isPresent());
        assertTrue(mappedAction.get().getAction() instanceof DigitalToAnalogActionAdapter);
    }
}