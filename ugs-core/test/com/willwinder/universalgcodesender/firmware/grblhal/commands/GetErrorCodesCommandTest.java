package com.willwinder.universalgcodesender.firmware.grblhal.commands;

import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalCode;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

public class GetErrorCodesCommandTest {

    @Test
    public void getCodes_shouldParseTheEnumeratedErrorCodes() {
        GetErrorCodesCommand command = new GetErrorCodesCommand();
        command.appendResponse("[ERRORCODE:1|Expected command letter|G-code words consist of a letter and a value. Letter was not found.]");
        command.appendResponse("[ERRORCODE:2|Bad number format|Missing the expected G-code word value.]");
        command.appendResponse("ok");

        List<GrblHalCode> result = command.getCodes();

        Assertions.assertThat(result).extracting(GrblHalCode::code).containsExactly("1", "2");
        Assertions.assertThat(result.get(0).message()).isEqualTo("Expected command letter");
        Assertions.assertThat(result.get(0).description())
                .isEqualTo("G-code words consist of a letter and a value. Letter was not found.");
    }

    @Test
    public void getCodes_shouldParseCodesWithoutADescription() {
        GetErrorCodesCommand command = new GetErrorCodesCommand();
        command.appendResponse("[ERRORCODE:7|EEPROM read fail|]");
        command.appendResponse("ok");

        List<GrblHalCode> result = command.getCodes();

        Assertions.assertThat(result.get(0).message()).isEqualTo("EEPROM read fail");
        Assertions.assertThat(result.get(0).description()).isEmpty();
    }

    @Test
    public void getCodes_shouldKeepSeparatorsThatArePartOfTheDescription() {
        GetErrorCodesCommand command = new GetErrorCodesCommand();
        command.appendResponse("[ERRORCODE:3|Invalid statement|Use A|B|C instead.]");
        command.appendResponse("ok");

        List<GrblHalCode> result = command.getCodes();

        Assertions.assertThat(result.get(0).description()).isEqualTo("Use A|B|C instead.");
    }

    @Test
    public void getCodes_shouldIgnoreUnrelatedResponseLines() {
        GetErrorCodesCommand command = new GetErrorCodesCommand();
        command.appendResponse("[ALARMCODE:1|Hard limit|Should be ignored.]");
        command.appendResponse("[ERRORCODE:1|Expected command letter|A description.]");
        command.appendResponse("ok");

        List<GrblHalCode> result = command.getCodes();

        Assertions.assertThat(result).hasSize(1);
        Assertions.assertThat(result.get(0).message()).isEqualTo("Expected command letter");
    }

    @Test
    public void getCodes_shouldReturnEmptyListWhenTheCommandWasNotSupported() {
        GetErrorCodesCommand command = new GetErrorCodesCommand();
        command.appendResponse("error:2");

        List<GrblHalCode> result = command.getCodes();

        Assertions.assertThat(result).isEmpty();
    }
}
