package com.willwinder.ugs.designer.io;

import com.willwinder.ugs.designer.model.Design;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Test;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DesignFileImporterTest {

    @Test
    public void isRasterFile_shouldDetectImageExtensionsIgnoringCase() {
        assertThat(DesignFileImporter.isRasterFile(new File("image.PNG"))).isTrue();
        assertThat(DesignFileImporter.isRasterFile(new File("image.jpeg"))).isTrue();
        assertThat(DesignFileImporter.isRasterFile(new File("image.bmp"))).isTrue();
        assertThat(DesignFileImporter.isRasterFile(new File("drawing.svg"))).isFalse();
    }

    @Test
    public void isSupportedFile_shouldAcceptAllListedFormats() {
        assertThat(DesignFileImporter.isSupportedFile(new File("drawing.svg"))).isTrue();
        assertThat(DesignFileImporter.isSupportedFile(new File("board-drl.gbr"))).isTrue();
        assertThat(DesignFileImporter.isSupportedFile(new File("part.STL"))).isTrue();
        assertThat(DesignFileImporter.isSupportedFile(new File("program.gcode"))).isFalse();
    }

    @Test
    public void allSupportedFormats_shouldContainEveryExtensionOfEveryFormat() {
        DesignFileImporter.FILE_FORMATS.forEach(format ->
                assertThat(DesignFileImporter.ALL_SUPPORTED_FORMATS.extensions()).containsAll(format.extensions()));
    }

    @Test
    public void readDesign_shouldReadSvgFile() {
        File file = new File(Objects.requireNonNull(DesignFileImporterTest.class.getResource("/20x20mm.svg")).getFile());

        Optional<Design> design = DesignFileImporter.readDesign(file, new Settings());

        assertThat(design).isPresent();
        assertThat(design.get().getEntities()).isNotEmpty();
    }

    @Test
    public void readDesign_shouldReturnEmptyForUnsupportedExtension() {
        Optional<Design> design = DesignFileImporter.readDesign(new File("program.gcode"), new Settings());

        assertThat(design).isEmpty();
    }
}
