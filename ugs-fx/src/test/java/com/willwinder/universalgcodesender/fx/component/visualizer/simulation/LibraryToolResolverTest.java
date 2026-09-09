package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class LibraryToolResolverTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ToolLibraryService library() throws IOException {
        return new ToolLibraryService(folder.newFolder().toPath().resolve("tool-library.json"));
    }

    private static CutSegment cut(int toolNumber) {
        return new CutSegment(0, 0, 0, 1, 0, 0, toolNumber, 1);
    }

    private static ToolDefinition tool(String name, int toolNumber, EndmillShape shape, double diameter) {
        ToolDefinition tool = new ToolDefinition();
        tool.setName(name);
        tool.setToolNumber(toolNumber);
        tool.setShape(shape);
        tool.setDiameter(diameter);
        return tool;
    }

    @Test
    public void shouldResolveByToolNumber() throws IOException {
        ToolLibraryService library = library();
        library.getTools().forEach(t -> library.deleteTool(t.getId()));
        library.addTool(tool("Ball", 4, EndmillShape.BALL, 6));
        library.addTool(tool("Flat", 2, EndmillShape.UPCUT, 3));

        ResolvedTool resolved = new LibraryToolResolver(library, null).describe(cut(4));
        assertThat(resolved.profile().kind()).isEqualTo(ToolProfile.Kind.BALL);
        assertThat(resolved.profile().diameter()).isCloseTo(6, within(1e-9));
        assertThat(resolved.source()).isEqualTo(ResolvedTool.Source.PROGRAM_SLOT);
        assertThat(resolved.label()).isEqualTo("T4 · Ball");
        assertThat(resolved.requestedToolNumber()).isEqualTo(4);
        assertThat(resolved.isRequestedToolNumberUnassigned()).isFalse();
        assertThat(resolved.definition()).isPresent();
    }

    @Test
    public void shouldFallBackToTheDefaultToolThenTheLowestSlot() throws IOException {
        ToolLibraryService library = library();
        library.getTools().forEach(t -> library.deleteTool(t.getId()));
        ToolDefinition flat = tool("Flat", 2, EndmillShape.UPCUT, 3);
        ToolDefinition vBit = tool("V", 0, EndmillShape.V_BIT, 12);
        library.addTool(flat);
        library.addTool(vBit);

        assertThat(new LibraryToolResolver(library, vBit.getId()).resolve(cut(0)).kind()).isEqualTo(ToolProfile.Kind.V_BIT);
        assertThat(new LibraryToolResolver(library, vBit.getId()).describe(cut(0)).source()).isEqualTo(ResolvedTool.Source.DEFAULT_TOOL);
        assertThat(new LibraryToolResolver(library, vBit.getId()).describe(cut(0)).isRequestedToolNumberUnassigned()).isFalse();
        ResolvedTool forMissingSlot = new LibraryToolResolver(library, vBit.getId()).describe(cut(99));
        assertThat(forMissingSlot.requestedToolNumber()).isEqualTo(99);
        assertThat(forMissingSlot.isRequestedToolNumberUnassigned()).isTrue();
        assertThat(forMissingSlot.explanation()).contains("T99").contains("default tool");
        assertThat(new LibraryToolResolver(library, "").describe(cut(0)).source()).isEqualTo(ResolvedTool.Source.LIBRARY_FIRST);
        assertThat(new LibraryToolResolver(library, vBit.getId()).resolve(cut(99)).kind()).isEqualTo(ToolProfile.Kind.V_BIT);
        assertThat(new LibraryToolResolver(library, "").resolve(cut(0)).diameter()).isCloseTo(3, within(1e-9));
        assertThat(new LibraryToolResolver(library, "missing").resolve(cut(7)).diameter()).isCloseTo(3, within(1e-9));
    }

    @Test
    public void shouldUseTheBuiltInDefaultWhenTheLibraryIsEmpty() throws IOException {
        ToolLibraryService library = library();
        library.getTools().forEach(t -> library.deleteTool(t.getId()));
        ResolvedTool resolved = new LibraryToolResolver(library, "").describe(cut(1));
        assertThat(resolved.profile()).isSameAs(ToolProfile.DEFAULT);
        assertThat(resolved.source()).isEqualTo(ResolvedTool.Source.BUILT_IN);
    }
}
