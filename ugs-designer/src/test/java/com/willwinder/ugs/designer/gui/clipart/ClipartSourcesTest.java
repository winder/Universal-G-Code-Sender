package com.willwinder.ugs.designer.gui.clipart;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClipartSourcesTest {

    @Test
    public void getSources_shouldLoadEverySourceWithAttribution() {
        List<ClipartSource> sources = ClipartSources.getSources();

        assertThat(sources).isNotEmpty();
        assertThat(sources).allSatisfy(source -> {
            assertThat(source.getName()).isNotBlank();
            assertThat(source.getCredits()).isNotBlank();
            assertThat(source.getLicense()).isNotBlank();
            assertThat(source.getUrl()).startsWith("http");
        });
    }

    @Test
    public void getCliparts_shouldReturnClipartsSortedByName() {
        List<Clipart> cliparts = ClipartSources.getCliparts(Category.ALL);

        assertThat(cliparts).isNotEmpty();
        assertThat(cliparts).extracting(clipart -> clipart.getName().toLowerCase()).isSorted();
    }

    @Test
    public void getCliparts_shouldOnlyReturnClipartsInTheCategory() {
        List<Clipart> cliparts = ClipartSources.getCliparts(Category.ANIMALS);

        assertThat(cliparts).isNotEmpty();
        assertThat(cliparts).allSatisfy(clipart -> assertThat(clipart.getCategory()).isEqualTo(Category.ANIMALS));
    }
}
