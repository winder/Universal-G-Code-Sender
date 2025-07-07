package com.willwinder.universalgcodesender.fx.helper;

import javafx.scene.paint.Color;

public class Colors {
    public static final Color BLACKISH = Color.rgb(53, 58, 64);
    public static final Color ORANGE = Color.rgb(255, 151, 11);
    public static final Color GREEN = Color.rgb(0, 201, 0);
    public static final Color RED = Color.rgb(240, 0, 0);

    public static final Color DARK_BLUE_GREY = Color.rgb(44, 65, 70);
    public static final Color MED_BLUE_GREY = Color.rgb(47, 93, 103);
    public static final Color BLUE = Color.rgb(79, 158, 176);

    /**
     * Returns the color as a hex code string
     *
     * @param color the color
     * @return the hex string with three parts if no alpha channel is used or four parts.
     */
    public static String toWeb(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        int a = (int) Math.round(color.getOpacity() * 255);


        if (a != 255) {
            return String.format("#%02X%02X%02X%02X", r, g, b, a);
        } else {
            return String.format("#%02X%02X%02X", r, g, b);
        }
    }

    /**
     * Blends multiple colors with each other
     *
     * @param colors one or more colors
     * @return a blended color
     */
    public static Color blend(Color... colors) {
        if (colors == null || colors.length == 0) {
            throw new IllegalArgumentException("Color list must not be empty");
        }

        double r = 0, g = 0, b = 0, a = 0;

        for (Color color : colors) {
            r += color.getRed();
            g += color.getGreen();
            b += color.getBlue();
            a += color.getOpacity();
        }

        int count = colors.length;
        return new Color(r / count, g / count, b / count, a / count);
    }


    /**
     * Interpolate between two colors given a percentage
     *
     * @param startColor  the start color to interpolate from
     * @param targetColor the target color to interpolate to
     * @param percent     the percentage of interpolation
     * @return the color interpolated with the given percent.
     */
    public static Color interpolate(Color startColor, Color targetColor, double percent) {
        double red = Math.min(1, startColor.getRed() + (percent * (targetColor.getRed() - startColor.getRed())));
        double green = Math.min(1, startColor.getGreen() + (percent * (targetColor.getGreen() - startColor.getGreen())));
        double blue = Math.min(1, startColor.getBlue() + (percent * (targetColor.getBlue() - startColor.getBlue())));
        double alpha = Math.min(1, startColor.getOpacity() + (percent * (targetColor.getOpacity() - startColor.getOpacity())));
        return new Color(red, green, blue, alpha);
    }

}
