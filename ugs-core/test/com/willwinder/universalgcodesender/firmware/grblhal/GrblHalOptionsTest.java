package com.willwinder.universalgcodesender.firmware.grblhal;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class GrblHalOptionsTest {

    @Test
    public void isEnabled_shouldReturnTrueForReportedOptions() {
        GrblHalOptions options = new GrblHalOptions("[NEWOPT:ENUMS,RT+,HOME,TC,SED]");

        boolean result = options.isEnabled(GrblHalOption.TOOL_CHANGE);

        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void isEnabled_shouldReturnFalseForOptionsNotReported() {
        GrblHalOptions options = new GrblHalOptions("[NEWOPT:ENUMS,RT+]");

        boolean result = options.isEnabled(GrblHalOption.SD_CARD);

        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void isEnabled_shouldReturnFalseWhenNoOptionsWereReported() {
        GrblHalOptions options = new GrblHalOptions();

        boolean result = options.isEnabled(GrblHalOption.SETTINGS_ENUMERATION);

        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void getOptions_shouldKeepUnknownOptionCodes() {
        GrblHalOptions options = new GrblHalOptions("[NEWOPT:ENUMS, SOMETHING_NEW ,TC]");

        java.util.Set<String> result = options.getOptions();

        Assertions.assertThat(result).containsExactly("ENUMS", "SOMETHING_NEW", "TC");
    }

    @Test
    public void getOptions_shouldReturnEmptySetForMalformedResponse() {
        GrblHalOptions options = new GrblHalOptions("ok");

        java.util.Set<String> result = options.getOptions();

        Assertions.assertThat(result).isEmpty();
    }
}
