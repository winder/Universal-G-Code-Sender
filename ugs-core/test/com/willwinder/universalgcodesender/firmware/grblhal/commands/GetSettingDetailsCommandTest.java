package com.willwinder.universalgcodesender.firmware.grblhal.commands;

import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalDataType;
import com.willwinder.universalgcodesender.firmware.grblhal.GrblHalSettingDetail;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class GetSettingDetailsCommandTest {
    private GetSettingDetailsCommand command;

    @Before
    public void setUp() {
        command = new GetSettingDetailsCommand();
        command.appendResponse("[SETTING:0|35|Step pulse time|microseconds|6|#0.0|1.0||0|0]");
        command.appendResponse("[SETTING:13|1|Report in inches||0||||0|0]");
        command.appendResponse("[SETTING:14|2|Invert control inputs||1|N/A,Feed hold,Cycle start,N/A,N/A,N/A,EStop|||0|0]");
        command.appendResponse("[SETTING:43|11|Homing passes||5|##0|1|128|0|0]");
        command.appendResponse("[SETTING:70|15|Network Services||1|Telnet,Websocket,HTTP,N/A,N/A,mDNS,SSDP|||1|0]");
        command.appendResponse("[SETTING:302|15|IP Address||9||||1|0]");
        command.appendResponse("[SETTING:394|6|Spindle on delay|s|6|#0.0|0.5|60|0|1]");
        command.appendResponse("ok");
    }

    @Test
    public void getSettingDetails_shouldParseOneDetailPerSetting() {
        List<GrblHalSettingDetail> result = command.getSettingDetails();

        Assertions.assertThat(result)
                .extracting(GrblHalSettingDetail::key)
                .containsExactly("$0", "$13", "$14", "$43", "$70", "$302", "$394");
    }

    @Test
    public void getSettingDetails_shouldParseAllFields() {
        GrblHalSettingDetail result = command.getSettingDetails().get(0);

        Assertions.assertThat(result.key()).isEqualTo("$0");
        Assertions.assertThat(result.groupId()).isEqualTo("35");
        Assertions.assertThat(result.name()).isEqualTo("Step pulse time");
        Assertions.assertThat(result.units()).isEqualTo("microseconds");
        Assertions.assertThat(result.dataType()).isEqualTo(GrblHalDataType.DECIMAL);
        Assertions.assertThat(result.format()).isEqualTo("#0.0");
        Assertions.assertThat(result.min()).isEqualTo("1.0");
        Assertions.assertThat(result.max()).isEmpty();
        Assertions.assertThat(result.rebootRequired()).isFalse();
        Assertions.assertThat(result.allowNull()).isFalse();
    }

    @Test
    public void getSettingDetails_shouldKeepTheBitLabelsForBitfieldSettings() {
        GrblHalSettingDetail result = command.getSettingDetails().get(2);

        Assertions.assertThat(result.dataType()).isEqualTo(GrblHalDataType.BITFIELD);
        Assertions.assertThat(result.format()).isEqualTo("N/A,Feed hold,Cycle start,N/A,N/A,N/A,EStop");
        Assertions.assertThat(result.units()).isEmpty();
    }

    @Test
    public void getSettingDetails_shouldParseTheValueRange() {
        GrblHalSettingDetail result = command.getSettingDetails().get(3);

        Assertions.assertThat(result.dataType()).isEqualTo(GrblHalDataType.INTEGER);
        Assertions.assertThat(result.min()).isEqualTo("1");
        Assertions.assertThat(result.max()).isEqualTo("128");
    }

    @Test
    public void getSettingDetails_shouldParseTheRebootRequiredFlag() {
        GrblHalSettingDetail result = command.getSettingDetails().get(4);

        Assertions.assertThat(result.rebootRequired()).isTrue();
        Assertions.assertThat(result.allowNull()).isFalse();
    }

    @Test
    public void getSettingDetails_shouldParseTheAllowNullFlag() {
        GrblHalSettingDetail result = command.getSettingDetails().get(6);

        Assertions.assertThat(result.min()).isEqualTo("0.5");
        Assertions.assertThat(result.max()).isEqualTo("60");
        Assertions.assertThat(result.allowNull()).isTrue();
    }

    @Test
    public void getSettingDetails_shouldParseBooleanAndAddressDataTypes() {
        List<GrblHalSettingDetail> result = command.getSettingDetails();

        Assertions.assertThat(result.get(1).dataType()).isEqualTo(GrblHalDataType.BOOLEAN);
        Assertions.assertThat(result.get(5).dataType()).isEqualTo(GrblHalDataType.IPV4);
    }

    @Test
    public void getSettingDetails_shouldParseSettingsWithOmittedTrailingFields() {
        GetSettingDetailsCommand shortCommand = new GetSettingDetailsCommand();
        shortCommand.appendResponse("[SETTING:0|18|Step pulse time|microseconds|6|#0.0|2.0|]");
        shortCommand.appendResponse("ok");

        GrblHalSettingDetail result = shortCommand.getSettingDetails().get(0);

        Assertions.assertThat(result.min()).isEqualTo("2.0");
        Assertions.assertThat(result.max()).isEmpty();
        Assertions.assertThat(result.rebootRequired()).isFalse();
        Assertions.assertThat(result.allowNull()).isFalse();
    }

    @Test
    public void getSettingDetails_shouldUseUnknownForDataTypesNotSupportedByUgs() {
        GetSettingDetailsCommand unknownCommand = new GetSettingDetailsCommand();
        unknownCommand.appendResponse("[SETTING:900|1|Something new||42||||0|0]");
        unknownCommand.appendResponse("ok");

        GrblHalSettingDetail result = unknownCommand.getSettingDetails().get(0);

        Assertions.assertThat(result.dataType()).isEqualTo(GrblHalDataType.UNKNOWN);
    }

    @Test
    public void getSettingDetails_shouldReturnEmptyListWhenTheCommandWasNotSupported() {
        GetSettingDetailsCommand unsupportedCommand = new GetSettingDetailsCommand();
        unsupportedCommand.appendResponse("error:2");

        List<GrblHalSettingDetail> result = unsupportedCommand.getSettingDetails();

        Assertions.assertThat(result).isEmpty();
    }
}
