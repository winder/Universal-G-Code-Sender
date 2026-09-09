package com.willwinder.universalgcodesender.fx.component.toollibrary;

import org.junit.Test;

import java.util.Set;
import java.util.function.IntPredicate;

import static org.assertj.core.api.Assertions.assertThat;

public class ToolNumbersTest {
    private static final IntPredicate OCCUPIED = Set.of(2, 3, 5)::contains;

    @Test
    public void shouldSkipOccupiedNumbersGoingUp() {
        assertThat(ToolNumbers.nextFree(1, 1, OCCUPIED, 9999)).isEqualTo(4);
        assertThat(ToolNumbers.nextFree(4, 1, OCCUPIED, 9999)).isEqualTo(6);
        assertThat(ToolNumbers.nextFree(1, 2, OCCUPIED, 9999)).isEqualTo(6);
    }

    @Test
    public void shouldSkipOccupiedNumbersGoingDown() {
        assertThat(ToolNumbers.nextFree(6, -1, OCCUPIED, 9999)).isEqualTo(4);
        assertThat(ToolNumbers.nextFree(4, -1, OCCUPIED, 9999)).isEqualTo(1);
    }

    @Test
    public void unassignedShouldAlwaysBeReachable() {
        assertThat(ToolNumbers.nextFree(1, -1, number -> true, 9999)).isEqualTo(0);
        assertThat(ToolNumbers.nextFree(4, -1, Set.of(1, 2, 3)::contains, 9999)).isEqualTo(0);
    }

    @Test
    public void shouldStopAtTheEndsOfTheRange() {
        assertThat(ToolNumbers.nextFree(0, -1, OCCUPIED, 9999)).isEqualTo(0);
        assertThat(ToolNumbers.nextFree(9999, 1, OCCUPIED, 9999)).isEqualTo(9999);
        assertThat(ToolNumbers.nextFree(9998, 5, OCCUPIED, 9999)).isEqualTo(9999);
        assertThat(ToolNumbers.nextFree(7, 1, number -> number > 7, 9999)).isEqualTo(7);
    }
}
