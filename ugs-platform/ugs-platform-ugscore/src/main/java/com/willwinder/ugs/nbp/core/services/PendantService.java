package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.pendantui.PendantURLBean;
import com.willwinder.universalgcodesender.utils.Settings;
import org.openide.util.lookup.ServiceProvider;

import java.util.Collection;

/**
 * A service that will start the pendant server if auto start is enabled
 */
@ServiceProvider(service = PendantService.class)
public class PendantService {

    private final BackendAPI backend;
    private PendantUI pendantUI;

    public PendantService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        autoStartPendant();
        System.err.println("Starting pendant service");
    }

    /**
     * Checks the auto start setting and starts the pendant.
     */
    private void autoStartPendant() {
        Settings settings = backend.getSettings();
        if (settings.isAutoStartPendant()) {
            startPendant();
        }
    }

    /**
     * Starts the pendant if not started. Returns a list of URL:s of the started pendant server.
     *
     * @return a list of URL:s to the pendant
     */
    public Collection<PendantURLBean> startPendant() {
        Collection<PendantURLBean> results;
        if (pendantUI == null) {
            pendantUI = new PendantUI(backend);
            backend.addControllerListener(pendantUI);
            results = pendantUI.start();

            for (PendantURLBean result : results) {
                backend.dispatchMessage(MessageType.INFO, "Pendant URL: " + result.getUrlString());
            }
        } else {
            results = pendantUI.getUrlList();
        }
        return results;
    }
}
