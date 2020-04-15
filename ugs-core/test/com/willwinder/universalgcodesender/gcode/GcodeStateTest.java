package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GcodeStateTest {
    @Test
    public void machineStateCode() {
        GcodeState state = new GcodeState();

        assertThat(state.machineStateCode())
                .isEqualTo("G21G90G91.1G94G54G17");

        state.units = Code.G20;
        state.offset = Code.G58;
        state.plane = Plane.UV;
        state.feedMode = Code.G93;
        state.distanceMode = Code.G91;
        state.arcDistanceMode = Code.G90_1;

        String result2 = "G20G91G90.1G93G58G17.1";
        assertThat(state.machineStateCode())
                .isEqualTo(result2);

        // Motion mode isn't included because it can't be submitted as a gcode command.
        state.currentMotionMode = Code.G3;

        assertThat(state.machineStateCode())
                .isEqualTo(result2);


        // Not part of machine state
        state.coolant = Code.M7;
        state.spindle = Code.M3;
        state.speed = 25.0;

        assertThat(state.machineStateCode())
                .isEqualTo(result2);
    }

    @Test
    public void accessoriesStateCode() {
        GcodeState state = new GcodeState();

        assertThat(state.toAccessoriesCode())
                .isEqualTo("S0.0F0.0");

        state.coolant = Code.M7;
        state.spindle = Code.M3;

        assertThat(state.toAccessoriesCode())
                .isEqualTo("M3S0.0M7F0.0");
    }
}
