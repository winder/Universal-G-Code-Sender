package com.willwinder.ugs.nbp.jog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.JogService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import java.awt.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Setup JOGL canvas, GcodeRenderer and RendererInputHandler.
 */
@TopComponent.Description(
        preferredID = "JogTopComponent"
)
@TopComponent.Registration(
        mode = "top_left",
        openAtStartup = true)
@ActionID(
        category = JogTopComponent.CATEGORY,
        id = JogTopComponent.ACTION_ID)
@ActionReference(
        path = JogTopComponent.WINOW_PATH)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:JogTopComponent>",
        preferredID = "JogTopComponent"
)
public final class JogTopComponent extends TopComponent implements JogPanelListener {

    public static final String WINOW_PATH = LocalizingService.MENU_WINDOW;
    public static final String CATEGORY = LocalizingService.CATEGORY_WINDOW;
    public static final String ACTION_ID = "com.willwinder.ugs.nbp.jog.JogTopComponent";
    private final BackendAPI backend;
    private final JogPanel jogPanel;
    private final JogService jogService;
    private JogPanelButtonEnum jogButtonPressed;

    public JogTopComponent() {
        jogService = CentralLookup.getDefault().lookup(JogService.class);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);

        setMinimumSize(new Dimension(200, 250));
        setPreferredSize(new Dimension(200, 250));
        setLayout(new BorderLayout());

        jogPanel = new JogPanel();
        jogPanel.addJogPanelListener(this);
        jogPanel.setEnabled(jogService.canJog());
        jogPanel.setJogFeedRate(Double.valueOf(backend.getSettings().getJogFeedRate()).intValue());
        add(jogPanel, BorderLayout.CENTER);


        ScheduledExecutorService executor =
                Executors.newSingleThreadScheduledExecutor();

        Runnable periodicTask = () -> {
            if (jogService.canJog() && jogButtonPressed != null) {
                try {
                    switch (jogButtonPressed) {
                        case BUTTON_XPOS:
                            jogService.adjustManualLocationXY(1, 0);
                            break;
                        case BUTTON_XNEG:
                            jogService.adjustManualLocationXY(-1, 0);
                            break;
                        case BUTTON_YPOS:
                            jogService.adjustManualLocationXY(0, 1);
                            break;
                        case BUTTON_YNEG:
                            jogService.adjustManualLocationXY(0, -1);
                            break;
                        case BUTTON_ZPOS:
                            jogService.adjustManualLocationZ(1);
                            break;
                        case BUTTON_ZNEG:
                            jogService.adjustManualLocationZ(-1);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        executor.scheduleAtFixedRate(periodicTask, 0, 500, TimeUnit.MILLISECONDS);
    }

    private void onEvent(UGSEvent event) {
        // Only update the panel if required
        boolean canJog = jogService.canJog();
        if (canJog != jogPanel.isEnabled()) {
            jogPanel.setEnabled(canJog);
        }

        if( event.isSettingChangeEvent() ) {
            jogPanel.setJogFeedRate(Double.valueOf(backend.getSettings().getJogFeedRate()).intValue());
        }
    }

    @Override
    public void onClick(JogPanelButtonEnum button) {

    }

    @Override
    public void onPressed(JogPanelButtonEnum button) {
        this.jogButtonPressed = button;
    }

    @Override
    public void onReleased(JogPanelButtonEnum button) {
        this.jogButtonPressed = null;
        if (backend.getController() != null && backend.isConnected()) {
            try {
                this.backend.getController().cancelSend();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
