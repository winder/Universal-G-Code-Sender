package com.willwinder.ugs.nbp.designer.io.ugsd;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.RuntimeTypeAdapterFactory;
import com.willwinder.ugs.nbp.designer.io.ugsd.common.UgsDesign;
import com.willwinder.ugs.nbp.designer.io.ugsd.v1.*;
import com.willwinder.ugs.nbp.designer.model.Design;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UgsDesignReader implements DesignReader {
    private static final Logger LOGGER = Logger.getLogger(UgsDesignReader.class.getSimpleName());
    @Override
    public Optional<Design> read(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            return read(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't load file " + file, e);
        }
    }

    @Override
    public Optional<Design> read(InputStream resourceAsStream) {
        try {
            String designFile = IOUtils.toString(resourceAsStream);
            Gson gson = new GsonBuilder().create();
            UgsDesign design = gson.fromJson(designFile, UgsDesign.class);

            switch(design.getVersion()) {
                case DesignV1.VERSION:
                    return parseV1(designFile);
                default:
                    throw new RuntimeException("Unknown version " + design.getVersion());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Couldn't load stream", e);
            throw new RuntimeException("Couldn't read from stream", e);
        }
    }

    @NotNull
    private Optional<Design> parseV1(String designFile) {
        RuntimeTypeAdapterFactory<EntityV1> entityAdapterFactory = RuntimeTypeAdapterFactory.of(EntityV1.class, "type");
        entityAdapterFactory.registerSubtype(EntityPathV1.class, EntityTypeV1.PATH.name());
        entityAdapterFactory.registerSubtype(EntityGroupV1.class, EntityTypeV1.GROUP.name());
        entityAdapterFactory.registerSubtype(EntityRectangleV1.class, EntityTypeV1.RECTANGLE.name());
        entityAdapterFactory.registerSubtype(EntityEllipseV1.class, EntityTypeV1.ELLIPSE.name());

        Gson gson = new GsonBuilder()
                .registerTypeAdapterFactory(entityAdapterFactory)
                .create();
        DesignV1 designV1 = gson.fromJson(designFile, DesignV1.class);
        return Optional.of(designV1.toInternal());
    }
}
