package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ProgramToolsTest {

    @Test
    public void shouldListDistinctToolsInOrderOfFirstUse() {
        ToolResolver resolver = cut -> switch (cut.toolNumber()) {
            case 2 -> ResolvedTool.of(ToolProfile.flat(6), ResolvedTool.Source.PROGRAM_SLOT, "T2 · 6mm Upcut");
            case 5 -> ResolvedTool.of(ToolProfile.ball(3), ResolvedTool.Source.PROGRAM_SLOT, "T5 · 3mm Ball");
            default -> ResolvedTool.of(ToolProfile.DEFAULT, ResolvedTool.Source.LIBRARY_FIRST, "1/8\" Upcut");
        };
        List<ProgramTools.ProgramTool> tools = ProgramTools.collect(List.of(
                new CutSegment(0, 0, -1, 1, 0, -1, 2, 10),
                new CutSegment(1, 0, -1, 2, 0, -1, 2, 11),
                new CutSegment(2, 0, -1, 3, 0, -1, 0, 12),
                new CutSegment(3, 0, -1, 4, 0, -1, 5, 13),
                new CutSegment(4, 0, -1, 5, 0, -1, 2, 14)), resolver);

        assertThat(tools).extracting(tool -> tool.tool().label()).containsExactly("T2 · 6mm Upcut", "1/8\" Upcut", "T5 · 3mm Ball");
        assertThat(tools).extracting(ProgramTools.ProgramTool::firstCommand).containsExactly(10, 12, 13);
        assertThat(tools).extracting(ProgramTools.ProgramTool::cuts).containsExactly(3, 1, 1);
        assertThat(tools.get(1).tool().source().isFallback()).isTrue();
        assertThat(tools.get(0).tool().source().isFallback()).isFalse();
    }

    @Test
    public void fallbackShouldBeListedOncePerRequestedToolNumber() {
        ResolvedTool fallback = ResolvedTool.of(ToolProfile.DEFAULT, ResolvedTool.Source.LIBRARY_FIRST, "1/8\" Upcut");
        ToolResolver resolver = cut -> fallback.withRequestedToolNumber(cut.toolNumber());
        List<ProgramTools.ProgramTool> tools = ProgramTools.collect(List.of(
                new CutSegment(0, 0, -1, 1, 0, -1, 5, 1),
                new CutSegment(1, 0, -1, 2, 0, -1, 7, 2),
                new CutSegment(2, 0, -1, 3, 0, -1, 5, 3)), resolver);

        assertThat(tools).extracting(tool -> tool.tool().requestedToolNumber()).containsExactly(5, 7);
        assertThat(tools).extracting(ProgramTools.ProgramTool::cuts).containsExactly(2, 1);
        assertThat(tools.getFirst().tool().explanation()).contains("T5");
    }

    @Test
    public void shouldBeEmptyWithoutCuts() {
        assertThat(ProgramTools.collect(List.of(), ToolResolver.fixed(ToolProfile.DEFAULT))).isEmpty();
    }
}
