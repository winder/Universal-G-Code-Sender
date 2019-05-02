package com.willwinder.universalgcodesender.pendantui;

import com.willwinder.universalgcodesender.model.BackendAPI;

public class BackendAPIFactory {
    private static BackendAPIFactory instance;
    private BackendAPI backendAPI;

    public static BackendAPIFactory getInstance() {
        if (instance == null) {
            instance = new BackendAPIFactory();
        }
        return instance;
    }

    public void register(BackendAPI backendAPI) {
        this.backendAPI = backendAPI;
    }

    public BackendAPI getBackendAPI() {
        return backendAPI;
    }
}
