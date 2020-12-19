package com.willwinder.ugs.nbp.lib.services;

import org.openide.util.Lookup;

public abstract class Localizer implements Runnable {
    protected static ActionRegistrationService ars =  Lookup.getDefault().lookup(ActionRegistrationService.class);
}
