package com.willwinder.ugs.nbm.visualizer.actions;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import org.junit.Before;
import org.junit.Test;

import java.awt.event.ActionEvent;

import static org.mockito.Mockito.*;

public class JogToHereActionTest {

    private BackendAPI backendAPI;
    private JogToHereAction jogToHereAction;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void jogToAction() {
        JogService jogServ = mock(JogService.class);
        jogToHereAction = spy(new JogToHereAction(jogServ, new Position(1, 2,3, UnitUtils.Units.MM)));

        jogToHereAction.actionPerformed(new ActionEvent("dummy", 1,"dummy"));

        // check if a 3D position results in a 2D (X/Y) move only
        verify(jogServ, times(1)).jogTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.MM));
    }
}