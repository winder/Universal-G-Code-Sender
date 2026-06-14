/*
    Copyright 2024 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.core.lifecycle;

import org.apache.commons.io.FileUtils;
import org.netbeans.spi.sendopts.Env;
import org.netbeans.spi.sendopts.Option;
import org.netbeans.spi.sendopts.OptionProcessor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.Places;
import org.openide.util.lookup.ServiceProvider;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@ServiceProvider(service = OptionProcessor.class)
public class StartupOptionProcessor extends OptionProcessor {
    private static final Logger logger = Logger.getLogger(StartupOptionProcessor.class.getName());

    private final Option openOption = Option.additionalArguments('o', "open");
    private final Option defaultOpenOption = Option.defaultArguments();
    private final Option clearCacheOption = Option.withoutArgument('c', "clearcache");
    private final Option fullscreenOption = Option.withoutArgument(Option.NO_SHORT_NAME, "fullscreen");

    /**
     * Register interest in the "open" option.
     */
    @Override
    public Set<Option> getOptions() {
        HashSet<Option> set = new HashSet<>();
        set.add(openOption);
        set.add(defaultOpenOption);
        set.add(clearCacheOption);
        set.add(fullscreenOption);
        return set;
    }

    /**
     * CLI Handler.
     */
    @Override
    protected void process(Env env, Map<Option, String[]> optionsMap) {
        pruneStaleVersionCaches();

        if (optionsMap.containsKey(clearCacheOption)) {
            clearCache();
        }

        if (optionsMap.containsKey(fullscreenOption)) {
            FullScreenOptionProcessor.setUseFullScreen(true);
        }

        Optional<String> fileToOpen = getFileToOpen(optionsMap, openOption);
        if (fileToOpen.isEmpty()) {
            fileToOpen = getFileToOpen(optionsMap, defaultOpenOption);
        }

        if (fileToOpen.isPresent()) {
            String inputFile = fileToOpen.get();
            logger.info("File to open: " + inputFile);
            try {
                DataObject.find(FileUtil.toFileObject(new File(inputFile)))
                        .getLookup()
                        .lookup(OpenCookie.class)
                        .open();
            } catch (DataObjectNotFoundException e) {
                logger.log(Level.SEVERE, "Could not open file " + inputFile, e);
            }
        }
    }

    /**
     * Deletes the contents of the current version's cache directory. Note that
     * on Windows the cache files are typically already memory-mapped by the
     * module system at this point, so the deletion may only take full effect on
     * the next startup. For reliably avoiding a corrupt cache after an upgrade,
     * the cache directory is scoped per version (see ugsplatform.conf) and stale
     * directories are pruned by {@link #pruneStaleVersionCaches()}.
     */
    private void clearCache() {
        try {
            Arrays.stream(Objects.requireNonNull(Places.getCacheDirectory().listFiles(f -> f.getName().endsWith(".dat"))))
                    .forEach(FileUtils::deleteQuietly);

            FileUtils.deleteQuietly(new File(Places.getCacheDirectory().getAbsolutePath() + File.separator + ".lastUsedVersion"));
            FileUtils.deleteQuietly(new File(Places.getCacheDirectory().getAbsolutePath() + File.separator + "localeVariants"));
            FileUtils.deleteDirectory(new File(Places.getCacheDirectory().getAbsolutePath() + File.separator + "lastModified"));
        } catch (IOException e) {
            logger.log(Level.WARNING, "An error occurred while clearing cache files", e);
        }
    }

    /**
     * Removes cache directories belonging to other (typically older) versions.
     * The active cache directory is named after the running version and lives
     * under a shared parent (e.g. {@code .../var/cache/<version>}); every sibling
     * directory therefore belongs to a different version that is not running and
     * can be deleted without hitting file locks.
     */
    private void pruneStaleVersionCaches() {
        try {
            File currentCacheDir = Places.getCacheDirectory();
            File parent = currentCacheDir.getParentFile();
            if (parent == null) {
                return;
            }

            File[] siblings = parent.listFiles(File::isDirectory);
            if (siblings == null) {
                return;
            }

            Arrays.stream(siblings)
                    .filter(dir -> !dir.equals(currentCacheDir))
                    .forEach(dir -> {
                        logger.info("Removing stale cache directory " + dir.getAbsolutePath());
                        FileUtils.deleteQuietly(dir);
                    });
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "An error occurred while pruning stale cache directories", e);
        }
    }

    private Optional<String> getFileToOpen(Map<Option, String[]> optionsMap, Option option) {
        String[] files = optionsMap.getOrDefault(option, new String[0]);

        if (files.length > 0) {
            return Optional.of(files[0]);
        }
        return Optional.empty();
    }
}
