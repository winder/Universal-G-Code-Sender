package com.willwinder.universalgcodesender.uielements.panels;

import org.junit.Ignore;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.assertEquals;

@Ignore("Can not be run with a headless Java therefore disabled")
public class ButtonGridPanelTest {
    @Test
    public void shouldChooseOneColumnWhenBelowMinWidth() throws InterruptedException {
        ButtonGridPanel buttonGridPanel = new ButtonGridPanel();
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());

        buttonGridPanel.setSize(ButtonGridPanel.SizeEnum.ONE_COLUMN_SIZE.getMinWidth(), 32);
        buttonGridPanel.revalidate();
        Thread.sleep(50);

        GridLayout layout = (GridLayout) buttonGridPanel.getLayout();
        assertEquals(1, layout.getColumns());
    }

    @Test
    public void shouldChooseTwoColumnsWhenMinWidth() {
        ButtonGridPanel buttonGridPanel = new ButtonGridPanel();
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());

        buttonGridPanel.setSize(ButtonGridPanel.SizeEnum.TWO_COLUMN_SIZE.getMinWidth() + 1, 32);
        buttonGridPanel.revalidate();

        GridLayout layout = (GridLayout) buttonGridPanel.getLayout();
        assertEquals(2, layout.getColumns());
    }

    @Test
    public void shouldChooseTwoColumnsWhenNotEnoughElementsForCurrentSize() {
        ButtonGridPanel buttonGridPanel = new ButtonGridPanel();
        buttonGridPanel.add(new Label());
        buttonGridPanel.add(new Label());

        buttonGridPanel.setSize(ButtonGridPanel.SizeEnum.THREE_COLUMN_SIZE.getMinWidth(), 32);
        buttonGridPanel.revalidate();

        GridLayout layout = (GridLayout) buttonGridPanel.getLayout();
        assertEquals(2, layout.getColumns());
    }
}
