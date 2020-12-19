package com.willwinder.universalgcodesender.firmware.smoothie;

import com.willwinder.universalgcodesender.firmware.DefaultFirmwareSettings;

public class SmoothieFirmwareSettings extends DefaultFirmwareSettings {
    @Override
    public boolean isHomingEnabled() {
        return true;
    }
}
