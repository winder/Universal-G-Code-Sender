package com.willwinder.universalgcodesender.firmware.grblhal.commands;

import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalCode;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

public class GetAlarmCodesCommandTest {

    @Test
    public void getCodes_shouldParseTheEnumeratedAlarmCodes() {
        GetAlarmCodesCommand command = new GetAlarmCodesCommand();
        command.appendResponse("[ALARMCODE:1|Hard limit|Hard limit has been triggered. Machine position is likely lost due to sudden halt. Re-homing is highly recommended.]");
        command.appendResponse("[ALARMCODE:11|Homing fail|Homing cycle failed. Limit switch not found within search distances.]");
        command.appendResponse("ok");

        List<GrblHalCode> result = command.getCodes();

        Assertions.assertThat(result).extracting(GrblHalCode::code).containsExactly("1", "11");
        Assertions.assertThat(result.get(0).message()).isEqualTo("Hard limit");
        Assertions.assertThat(result.get(1).description())
                .isEqualTo("Homing cycle failed. Limit switch not found within search distances.");
    }

    @Test
    public void getCodes_shouldReturnEmptyListWhenTheCommandWasNotSupported() {
        GetAlarmCodesCommand command = new GetAlarmCodesCommand();
        command.appendResponse("error:2");

        List<GrblHalCode> result = command.getCodes();

        Assertions.assertThat(result).isEmpty();
    }
}
