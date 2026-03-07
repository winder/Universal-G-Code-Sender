package com.willwinder.universalgcodesender.fx.helper;

import javafx.scene.text.Font;

import java.io.InputStream;
import java.util.Objects;

public final class FontRegistry {
    private FontRegistry() {}

    // Adjust if your resource path differs:
    private static final String LCD_TTF_RESOURCE = "/resources/fonts/LCD.ttf";

    private static String lcdFamily;

    public static void registerFonts() {
        if (lcdFamily != null) return;

        try (InputStream is = FontRegistry.class.getResourceAsStream(LCD_TTF_RESOURCE)) {
            Objects.requireNonNull(is, "Font resource not found: " + LCD_TTF_RESOURCE);

            Font loaded = Font.loadFont(is, 14);
            Objects.requireNonNull(loaded, "JavaFX could not load font: " + LCD_TTF_RESOURCE);

            lcdFamily = loaded.getFamily();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to register LCD font from " + LCD_TTF_RESOURCE, e);
        }
    }

    /** The exact family name JavaFX registered (use this in -fx-font-family). */
    public static String lcdFamily() {
        if (lcdFamily == null) registerFonts();
        return lcdFamily;
    }
}