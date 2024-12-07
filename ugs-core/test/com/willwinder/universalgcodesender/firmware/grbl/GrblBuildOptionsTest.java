package com.willwinder.universalgcodesender.firmware.grbl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GrblBuildOptionsTest {

    @Test
    public void isEnabled_shouldWorkWithoutAnyOptions() {
        GrblBuildOptions options = new GrblBuildOptions();
        for (GrblBuildOption option : GrblBuildOption.values()) {
            assertFalse(options.isEnabled(option));
        }
    }

    @Test
    public void isEnabled_shouldWorkWithAnEmptyOptionString() {
        GrblBuildOptions options = new GrblBuildOptions("[OPT:]");
        for (GrblBuildOption option : GrblBuildOption.values()) {
            assertFalse(options.isEnabled(option));
        }
    }

    @Test
    public void isEnabled_shouldWorkWithJustOneOption() {
        GrblBuildOptions options = new GrblBuildOptions("[OPT:I]");
        for (GrblBuildOption option : GrblBuildOption.values()) {
            if (option == GrblBuildOption.BUILD_INFO_USER_STRING_DISABLED) {
                assertTrue(options.isEnabled(option));
            } else {
                assertFalse(options.isEnabled(option));
            }
        }
    }

    @Test
    public void isEnabled_shouldWorkWithUnknownOptions() {
        GrblBuildOptions options = new GrblBuildOptions("[OPT:-]");
        for (GrblBuildOption option : GrblBuildOption.values()) {
            assertFalse(options.isEnabled(option));
        }
    }

    @Test
    public void isEnabled_shouldWorkWithMoreOptions() {
        GrblBuildOptions options = new GrblBuildOptions("[OPT:IV]");
        for (GrblBuildOption option : GrblBuildOption.values()) {
            if (option == GrblBuildOption.BUILD_INFO_USER_STRING_DISABLED || option == GrblBuildOption.VARIABLE_SPINDLE_ENABLED) {
                assertTrue(options.isEnabled(option));
            } else {
                assertFalse(options.isEnabled(option));
            }
        }
    }

    @Test
    public void isEnabled_shouldWorkWithMoreOptionsAndPlannerValues() {
        GrblBuildOptions options = new GrblBuildOptions("[OPT:IV,15,128]");
        for (GrblBuildOption option : GrblBuildOption.values()) {
            if (option == GrblBuildOption.BUILD_INFO_USER_STRING_DISABLED || option == GrblBuildOption.VARIABLE_SPINDLE_ENABLED) {
                assertTrue(options.isEnabled(option));
            } else {
                assertFalse(options.isEnabled(option));
            }
        }
    }

    @Test
    public void isEnabled_shouldWorkWithMoreOptionsAndPlannerAndBufferValues() {
        GrblBuildOptions options = new GrblBuildOptions("[OPT:IV,15,128]");
        for (GrblBuildOption option : GrblBuildOption.values()) {
            if (option == GrblBuildOption.BUILD_INFO_USER_STRING_DISABLED || option == GrblBuildOption.VARIABLE_SPINDLE_ENABLED) {
                assertTrue(options.isEnabled(option));
            } else {
                assertFalse(options.isEnabled(option));
            }
        }
    }

    @Test
    public void isEnabled_shouldBeAbleToParseAllOptions() {
        String allOptions = Arrays.stream(GrblBuildOption.values()).map(GrblBuildOption::getCode).collect(Collectors.joining());
        GrblBuildOptions options = new GrblBuildOptions("[OPT:" + allOptions + ",15,128]");
        for (GrblBuildOption option : GrblBuildOption.values()) {
            assertTrue(options.isEnabled(option));
        }
    }
}