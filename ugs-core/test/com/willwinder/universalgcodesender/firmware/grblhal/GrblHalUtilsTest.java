package com.willwinder.universalgcodesender.firmware.grblhal;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOptions;
import com.willwinder.universalgcodesender.firmware.grbl.GrblCapabilitiesConstants;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class GrblHalUtilsTest {

    @Test
    public void isWelcomeMessage_shouldAcceptGrblHalWelcomeMessage() {
        boolean result = GrblHalUtils.isWelcomeMessage("GrblHAL 1.1f ['$' or '$HELP' for help]");

        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void isWelcomeMessage_shouldRejectFluidNcWelcomeMessage() {
        boolean result = GrblHalUtils.isWelcomeMessage("GrblHal 3.4 [FluidNC v3.4.2 (wifi) '$' for help]");

        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void isWelcomeMessage_shouldRejectGrblWelcomeMessage() {
        boolean result = GrblHalUtils.isWelcomeMessage("Grbl 1.1f ['$' for help]");

        Assertions.assertThat(result).isFalse();
    }

    @Test
    public void parseProbePosition_shouldReturnThePositionOfASuccessfulProbe() {
        Position result = GrblHalUtils.parseProbePosition("[PRB:1.000,2.000,-3.000:1]", Units.MM);

        Assertions.assertThat(result).isEqualTo(new Position(1, 2, -3, Units.MM));
    }

    @Test
    public void parseProbePosition_shouldReturnNullForAFailedProbe() {
        Position result = GrblHalUtils.parseProbePosition("[PRB:1.000,2.000,-3.000:0]", Units.MM);

        Assertions.assertThat(result).isNull();
    }

    @Test
    public void getSetCoordCommand_shouldGenerateAWorkPositionOffset() {
        String result = GrblHalUtils.getSetCoordCommand(PartialPosition.builder(Units.MM).setX(10.0).build());

        Assertions.assertThat(result).isEqualTo("G10 P0 L20 X10");
    }

    @Test
    public void getCapabilities_shouldAlwaysSupportTheGrblVersion1Protocol() {
        Capabilities result = GrblHalUtils.getCapabilities(new GrblBuildOptions());

        Assertions.assertThat(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT)).isTrue();
        Assertions.assertThat(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME)).isTrue();
        Assertions.assertThat(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)).isTrue();
        Assertions.assertThat(result.hasOverrides()).isTrue();
    }

    @Test
    public void getCapabilities_shouldAddVariableSpindleWhenEnabledInTheBuild() {
        Capabilities result = GrblHalUtils.getCapabilities(new GrblBuildOptions("[OPT:V,35,1024]"));

        Assertions.assertThat(result.hasCapability(CapabilitiesConstants.VARIABLE_SPINDLE)).isTrue();
    }

    @Test
    public void getCapabilities_shouldNotAddVariableSpindleWhenDisabledInTheBuild() {
        Capabilities result = GrblHalUtils.getCapabilities(new GrblBuildOptions("[OPT:M,35,1024]"));

        Assertions.assertThat(result.hasCapability(CapabilitiesConstants.VARIABLE_SPINDLE)).isFalse();
    }
}
