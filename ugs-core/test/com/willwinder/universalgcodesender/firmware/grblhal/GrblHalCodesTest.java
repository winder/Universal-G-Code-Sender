package com.willwinder.universalgcodesender.firmware.grblhal;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class GrblHalCodesTest {
    private GrblHalCodes codes;

    @Before
    public void setUp() {
        codes = new GrblHalCodes();
        codes.updateErrorCodes(List.of(
                new GrblHalCode("1", "Expected command letter", "G-code words consist of a letter and a value. Letter was not found."),
                new GrblHalCode("2", "Bad number format", "")));
        codes.updateAlarmCodes(List.of(
                new GrblHalCode("1", "Hard limit", "Hard limit has been triggered. Machine position is likely lost.")));
    }

    @Test
    public void lookupCode_shouldDescribeAnErrorResponse() {
        String result = codes.lookupCode("error:1");

        Assertions.assertThat(result).isEqualTo("(error:1) G-code words consist of a letter and a value. Letter was not found.");
    }

    @Test
    public void lookupCode_shouldDescribeAnAlarmResponse() {
        String result = codes.lookupCode("ALARM:1");

        Assertions.assertThat(result).isEqualTo("(ALARM:1) Hard limit has been triggered. Machine position is likely lost.");
    }

    @Test
    public void lookupCode_shouldUseTheMessageWhenThereIsNoDescription() {
        String result = codes.lookupCode("error:2");

        Assertions.assertThat(result).isEqualTo("(error:2) Bad number format");
    }

    @Test
    public void lookupCode_shouldNotUseAnAlarmDescriptionForAnErrorCode() {
        String result = codes.lookupCode("error:1");

        Assertions.assertThat(result).doesNotContain("Hard limit");
    }

    @Test
    public void lookupCode_shouldReportCodesTheControllerDidNotEnumerate() {
        String result = codes.lookupCode("error:99");

        Assertions.assertThat(result).isEqualTo("(error:99) An unknown error has occurred");
    }

    @Test
    public void lookupCode_shouldReturnTheResponseUnchangedWhenNoCodesHaveBeenLoaded() {
        GrblHalCodes emptyCodes = new GrblHalCodes();

        String result = emptyCodes.lookupCode("error:1");

        Assertions.assertThat(result).isEqualTo("error:1");
    }

    @Test
    public void lookupCode_shouldReturnResponsesThatAreNotACodeUnchanged() {
        Assertions.assertThat(codes.lookupCode("ok")).isEqualTo("ok");
        Assertions.assertThat(codes.lookupCode("[VER:1.1f]")).isEqualTo("[VER:1.1f]");
        Assertions.assertThat(codes.lookupCode("error:oops")).isEqualTo("error:oops");
    }

    @Test
    public void updateErrorCodes_shouldReplaceThePreviouslyLoadedCodes() {
        codes.updateErrorCodes(List.of(new GrblHalCode("1", "Something else", "A different cause.")));

        String result = codes.lookupCode("error:1");

        Assertions.assertThat(result).isEqualTo("(error:1) A different cause.");
    }

    @Test
    public void getAlarmCode_shouldReturnTheReportedCode() {
        Assertions.assertThat(codes.getAlarmCode("1")).map(GrblHalCode::message).contains("Hard limit");
        Assertions.assertThat(codes.getAlarmCode("99")).isEmpty();
    }
}
