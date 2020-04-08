package com.willwinder.ugs.nbp.lib.services;

import javax.swing.*;

public class ActionReference {
    private String id;
    private Action action;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getName() {
        String name = (String) action.getValue(Action.NAME);
        if (name == null) {
            name = id;
        }
        return name;
    }
}
