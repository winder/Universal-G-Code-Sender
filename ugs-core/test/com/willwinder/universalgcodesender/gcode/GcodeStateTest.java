package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GcodeStateTest {
    @Test
    public void toGcodeTest() {
        GcodeState state = new GcodeState();

        assertThat(state.toGcode())
                .isEqualTo("G21G90G91.1G94G54G17F0.0S0.0");

        state.units = Code.G20;
        state.offset = Code.G58;
        state.plane = Plane.UV;
        state.feedMode = Code.G93;
        state.distanceMode = Code.G91;
        state.arcDistanceMode = Code.G90_1;

        String result2 = "G20G91G90.1G93G58G17.1F0.0S0.0";
        assertThat(state.toGcode())
                .isEqualTo(result2);

        // Motion mode isn't included because it can't be submitted as a gcode command.
        state.currentMotionMode = Code.G3;

        assertThat(state.toGcode())
                .isEqualTo(result2);

        state.coolant = Code.M7;
        state.spindle = Code.M3;

        assertThat(state.toGcode())
                .isEqualTo(result2 + "M3M7");
    }
}
