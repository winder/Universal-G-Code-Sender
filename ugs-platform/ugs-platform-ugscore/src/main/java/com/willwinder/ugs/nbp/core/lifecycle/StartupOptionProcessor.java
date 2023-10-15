package com.willwinder.ugs.nbp.core.lifecycle;

import org.apache.commons.io.FileUtils;
import org.netbeans.api.sendopts.CommandException;
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

    /**
     * Register interest in the "open" option.
     */
    @Override
    public Set<Option> getOptions() {
        HashSet<Option> set = new HashSet<>();
        set.add(openOption);
        set.add(defaultOpenOption);
        set.add(clearCacheOption);
        return set;
    }

    /**
     * CLI Handler.
     */
    @Override
    protected void process(Env env, Map<Option, String[]> optionsMap) throws CommandException {
        if (optionsMap.containsKey(clearCacheOption)) {
            clearCache();
        }

        Optional<String> fileToOpen = getFileToOpen(optionsMap, openOption);
        if (!fileToOpen.isPresent()) {
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

    private Optional<String> getFileToOpen(Map<Option, String[]> optionsMap, Option option) {
        String[] files = optionsMap.getOrDefault(option, new String[0]);

        if (files.length > 0) {
            return Optional.of(files[0]);
        }
        return Optional.empty();
    }
}
