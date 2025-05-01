package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.pendantui.PendantURLBean;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Collection;
import java.util.logging.Logger;

public class PendantService {
    private static final Logger LOGGER = Logger.getLogger(PendantService.class.getName());
    private static PendantService instance = new PendantService();
    private final BackendAPI backend;
    private final BooleanProperty isStarted = new SimpleBooleanProperty(false);
    private PendantUI pendantUI;

    private PendantService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        autoStartPendant();
        backend.addUGSEventListener(this::onEvent);
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof SettingChangedEvent settingChangedEvent) {
            autoStartPendant();
        }
    }

    /**
     * Checks the auto start setting and starts the pendant.
     */
    private void autoStartPendant() {
        if (!isStarted.get() && backend.getSettings().isAutoStartPendant()) {
            ThreadHelper.invokeLater(this::start);
        }
    }


    public static PendantService getInstance() {
        if (instance == null) {
            instance = new PendantService();
        }
        return instance;
    }

    /**
     * Starts the pendant if not started. Returns a list of URL:s of the started pendant server.
     *
     * @return a list of URL:s to the pendant
     */
    public Collection<PendantURLBean> start() {
        Collection<PendantURLBean> results;
        if (pendantUI == null) {
            LOGGER.info("Starting pendant on port " + backend.getSettings().getPendantPort());
            pendantUI = new PendantUI(backend);
            results = pendantUI.start();
            isStarted.set(true);

            for (PendantURLBean result : results) {
                LOGGER.info("Pendant URL: " + result.getUrlString());
            }
        } else {
            results = pendantUI.getUrlList();
        }
        return results;
    }

    public void stop() {
        pendantUI.stop();
        isStarted.set(false);
    }

    public BooleanProperty isStartedProperty() {
        return isStarted;
    }
}
