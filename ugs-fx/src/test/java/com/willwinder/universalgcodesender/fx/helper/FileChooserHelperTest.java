package com.willwinder.universalgcodesender.fx.helper;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class FileChooserHelperTest {

    private static final File USER_HOME = new File(System.getProperty("user.home"));

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void resolveInitialDirectory_shouldReturnDirectoryWhenItExists() throws IOException {
        File directory = temporaryFolder.newFolder();

        assertThat(FileChooserHelper.resolveInitialDirectory(directory.getAbsolutePath())).isEqualTo(directory);
    }

    @Test
    public void resolveInitialDirectory_shouldFallBackToUserHomeWhenDirectoryIsMissing() {
        File missing = new File(temporaryFolder.getRoot(), "missing");

        assertThat(FileChooserHelper.resolveInitialDirectory(missing.getAbsolutePath())).isEqualTo(USER_HOME);
    }

    @Test
    public void resolveInitialDirectory_shouldFallBackToUserHomeWhenPathIsAFile() throws IOException {
        File file = temporaryFolder.newFile();

        assertThat(FileChooserHelper.resolveInitialDirectory(file.getAbsolutePath())).isEqualTo(USER_HOME);
    }

    @Test
    public void resolveInitialDirectory_shouldFallBackToUserHomeWhenPathIsEmpty() {
        assertThat(FileChooserHelper.resolveInitialDirectory(null)).isEqualTo(USER_HOME);
        assertThat(FileChooserHelper.resolveInitialDirectory("")).isEqualTo(USER_HOME);
    }
}
