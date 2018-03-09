package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.util.Hashtable;

public class Button extends RoundedPanel {
    private static final int COMMON_RADIUS = 7;

    public Button() {
        super(COMMON_RADIUS);
        setForeground(ThemeColors.LIGHT_BLUE);
        setBackground(ThemeColors.DARK_BLUE_GREY);
        setBackgroundDisabled(ThemeColors.VERY_DARK_GREY);
        setHoverBackground(ThemeColors.MED_BLUE_GREY);
        setPressedBackground(ThemeColors.LIGHT_BLUE_GREY);
        setForegroundDisabled(ThemeColors.DARK_BLUE_GREY);
        setLayout(new MigLayout("fill, inset 5 5 5 5"));
    }

    public Button(Component component) {
        this();
        add(component, "al center");
    }

    public Button(ImageIcon icon) {
        this();
        JLabel label = new JLabel(icon);
        label.setDisabledIcon(new ImageIcon(createDisabledImage(icon.getImage())));
        add(label, "grow");
    }

    private Image createDisabledImage(Image img) {
        ImageProducer prod = new FilteredImageSource(img.getSource(), new DisabledButtonFilter());
        return Toolkit.getDefaultToolkit().createImage(prod);
    }

    private class DisabledButtonFilter extends RGBImageFilter {

        DisabledButtonFilter() {
            canFilterIndexColorModel = true;
        }

        public int filterRGB(int x, int y, int rgb) {
            // Reduce the color bandwidth in quarter (>> 2) and Shift 0x44.
            return (rgb & 0xff000000) + 0x333333 + ((((rgb >> 16) & 0xff) >> 2) << 16) + ((((rgb >> 8) & 0xff) >> 2) << 8) + (((rgb) & 0xff) >> 2);
        }

        // override the superclass behaviour to not pollute
        // the heap with useless properties strings. Saves tens of KBs
        @Override
        public void setProperties(Hashtable props) {
            props = (Hashtable) props.clone();
            consumer.setProperties(props);
        }
    }
}
