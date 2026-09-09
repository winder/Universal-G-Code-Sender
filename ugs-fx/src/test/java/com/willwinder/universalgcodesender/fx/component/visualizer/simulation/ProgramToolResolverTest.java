package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.GcodeLines;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ProgramToolResolverTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ToolLibraryService library() throws IOException {
        ToolLibraryService library = new ToolLibraryService(folder.newFolder().toPath().resolve("tool-library.json"));
        library.getTools().forEach(t -> library.deleteTool(t.getId()));
        return library;
    }

    private static ToolDefinition tool(String name, int toolNumber, EndmillShape shape, double diameter) {
        ToolDefinition tool = new ToolDefinition();
        tool.setName(name);
        tool.setToolNumber(toolNumber);
        tool.setShape(shape);
        tool.setDiameter(diameter);
        return tool;
    }

    private static CutSegment cut(int toolNumber, int commandNumber) {
        return new CutSegment(0, 0, -1, 10, 0, -1, toolNumber, commandNumber);
    }

    @Test
    public void shouldFindToolCommentsWithAndWithoutToolChanges() {
        List<ProgramToolResolver.ToolComment> comments = ProgramToolResolver.findToolComments(List.of(
                "G21 ; millimeters",
                "; Tool: 6mm Upcut",
                "G0 X0",
                "M6 T2 ; Tool: 1/4\" Ball",
                "(Tool: 3mm V-bit 60°)",
                "; Tooling notes"));
        assertThat(comments).containsExactly(
                new ProgramToolResolver.ToolComment(1, "6mm Upcut"),
                new ProgramToolResolver.ToolComment(3, "1/4\" Ball"),
                new ProgramToolResolver.ToolComment(4, "3mm V-bit 60°"));
    }

    @Test
    public void toolCommentShouldDescribeSegmentsAfterItWhenNoSlotMatches() throws IOException {
        ToolLibraryService library = library();
        library.addTool(tool("Tiny", 1, EndmillShape.UPCUT, 1));
        LibraryToolResolver fallback = new LibraryToolResolver(library, "");
        ProgramToolResolver resolver = new ProgramToolResolver(List.of(
                new ProgramToolResolver.ToolComment(3, "6mm Upcut"),
                new ProgramToolResolver.ToolComment(20, "2mm Ball")), library, fallback);

        // Before any comment the library fallback applies
        assertThat(resolver.resolve(cut(0, 1)).diameter()).isCloseTo(1, within(1e-9));
        assertThat(resolver.resolve(cut(0, 3)).diameter()).isCloseTo(6, within(1e-9));
        assertThat(resolver.describe(cut(0, 3)).source()).isEqualTo(ResolvedTool.Source.PROGRAM_COMMENT);
        assertThat(resolver.describe(cut(0, 3)).label()).isEqualTo("6mm Upcut");
        assertThat(resolver.describe(cut(0, 1)).source()).isEqualTo(ResolvedTool.Source.LIBRARY_FIRST);
        assertThat(resolver.resolve(cut(0, 19)).diameter()).isCloseTo(6, within(1e-9));
        assertThat(resolver.resolve(cut(0, 20)).kind()).isEqualTo(ToolProfile.Kind.BALL);
        // A T word without a matching slot still uses the comment, and remembers the number asked for
        assertThat(resolver.resolve(cut(9, 25)).kind()).isEqualTo(ToolProfile.Kind.BALL);
        assertThat(resolver.describe(cut(9, 25)).requestedToolNumber()).isEqualTo(9);
        assertThat(resolver.describe(cut(9, 25)).isRequestedToolNumberUnassigned()).isTrue();
        assertThat(resolver.describe(cut(0, 25)).isRequestedToolNumberUnassigned()).isFalse();
    }

    @Test
    public void matchingSlotShouldBeatTheComment() throws IOException {
        ToolLibraryService library = library();
        library.addTool(tool("Big", 3, EndmillShape.UPCUT, 12));
        ProgramToolResolver resolver = new ProgramToolResolver(List.of(
                new ProgramToolResolver.ToolComment(1, "6mm Upcut")), library, new LibraryToolResolver(library, ""));
        assertThat(resolver.resolve(cut(3, 5)).diameter()).isCloseTo(12, within(1e-9));
        assertThat(resolver.resolve(cut(0, 5)).diameter()).isCloseTo(6, within(1e-9));
    }

    @Test
    public void commentNamingALibraryToolShouldUseThatTool() throws IOException {
        ToolLibraryService library = library();
        library.addTool(tool("Roughing bit", 0, EndmillShape.BALL, 8));
        ProgramToolResolver resolver = new ProgramToolResolver(List.of(
                new ProgramToolResolver.ToolComment(1, "roughing bit")), library, new LibraryToolResolver(library, ""));
        ResolvedTool resolved = resolver.describe(cut(0, 2));
        assertThat(resolved.profile().kind()).isEqualTo(ToolProfile.Kind.BALL);
        assertThat(resolved.profile().diameter()).isCloseTo(8, within(1e-9));
        assertThat(resolved.source()).isEqualTo(ResolvedTool.Source.PROGRAM_COMMENT_LIBRARY);
        assertThat(resolved.label()).isEqualTo("Roughing bit");
    }

    @Test
    public void commentNumberingShouldMatchTheParsedSegments() throws Exception {
        File program = folder.newFile("pocket.gcode");
        Files.writeString(program.toPath(), String.join("\n",
                "G21 ; millimeters",
                "G90",
                "; Tool: 6mm Upcut",
                "G0 X0 Y0 Z5",
                "G1 Z-1 F100",
                "G1 X10",
                "; Tool: 2mm Ball",
                "G1 Y10"));
        ToolLibraryService library = library();
        ProgramToolResolver resolver = ProgramToolResolver.forProgram(program.toPath(), library, new LibraryToolResolver(library, ""));
        List<LineSegment> segments = GcodeLines.parseSegments(program);
        List<CutSegment> cuts = CutSegment.fromLineSegments(segments);

        assertThat(cuts).extracting(CutSegment::commandNumber).containsExactly(4, 5, 7);
        assertThat(resolver.resolve(cuts.get(0)).diameter()).isCloseTo(6, within(1e-9));
        assertThat(resolver.resolve(cuts.get(1)).diameter()).isCloseTo(6, within(1e-9));
        assertThat(resolver.resolve(cuts.get(2)).kind()).isEqualTo(ToolProfile.Kind.BALL);
    }
}
