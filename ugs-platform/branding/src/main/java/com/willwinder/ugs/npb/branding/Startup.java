package com.willwinder.ugs.npb.branding;

import com.willwinder.universalgcodesender.utils.Version;
import org.openide.modules.OnStart;

@OnStart
public class Startup implements Runnable {
    @Override
    public void run() {
        // Sets the version string in the about dialog
        System.setProperty("netbeans.buildnumber", Version.getVersionString());
    }
}
