package com.willwinder.universalgcodesender.utils;

import java.awt.*;
import java.io.InputStream;

public class FontUtils {

    public static final String FONT_PATH = "/resources/fonts/";

    public static Font createFont(InputStream is, String fontName) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            is.close();
            return font;
        } catch (Exception exc) {
            exc.printStackTrace();
            System.err.println(fontName + " not loaded.  Using serif font.");
            return new Font("sans-serif", Font.PLAIN, 24);
        }
    }

    public static Font getLcdFont() {
        String fontName = "LCD.ttf";
        InputStream is = FontUtils.class.getResourceAsStream(FONT_PATH + fontName);
        return FontUtils.createFont(is, fontName);
    }

    public static Font getSansFont() {
        // https://www.fontsquirrel.com
        String fontName = "OpenSans-Regular.ttf";
        InputStream is = FontUtils.class.getResourceAsStream(FONT_PATH + fontName);
        return FontUtils.createFont(is, fontName);
    }

    public static Font getSansBoldFont() {
        // https://www.fontsquirrel.com
        String fontName = "OpenSans-CondBold.ttf";
        InputStream is = FontUtils.class.getResourceAsStream(FONT_PATH + fontName);
        return FontUtils.createFont(is, fontName);
    }
}
