package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.designer.io.DesignFileImporter;
import javafx.stage.FileChooser;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DesignImportActionTest {

    @Test
    public void createExtensionFilters_shouldStartWithAllSupportedFormatsFollowedByEachFormat() {
        List<FileChooser.ExtensionFilter> filters = DesignImportAction.createExtensionFilters();

        assertThat(filters).hasSize(DesignFileImporter.FILE_FORMATS.size() + 1);
        assertThat(filters.getFirst().getDescription()).isEqualTo(DesignFileImporter.ALL_SUPPORTED_FORMATS.description());
        assertThat(filters.getFirst().getExtensions()).contains("*.svg", "*.dxf", "*.stl", "*.png");
    }

    @Test
    public void createExtensionFilters_shouldPrefixExtensionsWithWildcard() {
        List<FileChooser.ExtensionFilter> filters = DesignImportAction.createExtensionFilters();

        assertThat(filters).allSatisfy(filter ->
                assertThat(filter.getExtensions()).allMatch(extension -> extension.startsWith("*.")));
    }
}
