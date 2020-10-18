package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.uielements.helpers.SteppedSizeManager;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.util.Arrays;

import static java.awt.event.ComponentEvent.COMPONENT_RESIZED;


public class ButtonGridPanel extends JPanel {

    public ButtonGridPanel() {
        this.setLayout(new GridLayout(0, 2));
        Dimension[] sizeList = Arrays.stream(SizeEnum.values())
                .map(sizeEnum -> new Dimension(sizeEnum.minWidth, 0))
                .toArray(Dimension[]::new);

        SteppedSizeManager steppedSizeManager = new SteppedSizeManager(this, sizeList);
        steppedSizeManager.addListener(this::onSizeChanged);
    }

    private void onSizeChanged(int sizeIndex) {
        SizeEnum sizeEnum = SizeEnum.valueFromIndex(sizeIndex - 1);
        int columns = Math.min(sizeEnum.getNumberOfColumns(), this.getComponentCount());
        ThreadHelper.invokeLater(() -> this.setLayout(new GridLayout(0, columns)));
    }

    public enum SizeEnum {
        ONE_COLUMN_SIZE(180, 1),
        TWO_COLUMN_SIZE(240, 2),
        THREE_COLUMN_SIZE(400, 3),
        FOUR_COLUMN_SIZE(500, 4),
        FIVE_COLUMN_SIZE(600, 5),
        SIX_COLUMN_SIZE(700, 6),
        SEVEN_COLUMN_SIZE(800, 7),
        EIGHT_COLUMN_SIZE(900, 8),
        NINE_COLUMN_SIZE(1000, 9);

        private final int minWidth;
        private final int numberOfColumns;

        SizeEnum(int minWidth, int numberOfColumns) {
            this.minWidth = minWidth;
            this.numberOfColumns = numberOfColumns;
        }

        public static SizeEnum valueFromIndex(int index) {
            if(index > values().length) {
                index = values().length;
            } else if(index < 0) {
                index = 0;
            }
            return values()[index];
        }

        public int getMinWidth() {
            return minWidth;
        }

        public int getNumberOfColumns() {
            return numberOfColumns;
        }
    }
}
