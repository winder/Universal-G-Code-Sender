package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.actions.ActionRegistry;
import com.willwinder.universalgcodesender.fx.actions.MacroAction;
import com.willwinder.universalgcodesender.model.BackendAPI;

public class MacroActionService {
    public static void registerMacros() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.getSettings().getMacros().forEach(macro -> ActionRegistry.getInstance().registerAction(new MacroAction(macro)));
    }
}
