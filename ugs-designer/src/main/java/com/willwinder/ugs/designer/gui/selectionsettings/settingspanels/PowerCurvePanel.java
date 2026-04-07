package com.willwinder.ugs.designer.gui.selectionsettings.settingspanels;

import com.willwinder.ugs.designer.gui.CurveCanvas;
import com.willwinder.universalgcodesender.i18n.Localization;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * @author Albert Giro github.com/giro-dev
 * Raster settings wrapper around {@link CurveCanvas} that adds a reset button.
 * Forwards the {@link CurveCanvas#PROPERTY_CURVE_CHANGED} event so existing listeners keep working.
 */
public class PowerCurvePanel extends JPanel {

    public static final String PROPERTY_CURVE_CHANGED = CurveCanvas.PROPERTY_CURVE_CHANGED;

    private final CurveCanvas canvas;

    public PowerCurvePanel() {
        setLayout(new BorderLayout(0, 4));

        canvas = new CurveCanvas(
                Localization.getString("platform.plugin.designer.curve.x-axis"),
                Localization.getString("platform.plugin.designer.curve.y-axis"));

        canvas.setToolTipText("<html>" +
                Localization.getString("platform.plugin.designer.curve.hint") +
                "</html>");

        // Show tooltip after 2 s of no movement, dismiss immediately on mouse move
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        ttm.setInitialDelay(2000);
        ttm.setDismissDelay(Integer.MAX_VALUE);
        ttm.setReshowDelay(2000);
        ttm.registerComponent(canvas);

        JButton resetBtn = new JButton(Localization.getString("platform.plugin.designer.curve.reset"));
        resetBtn.addActionListener(e -> canvas.resetToLinear());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        btnRow.setOpaque(false);
        btnRow.add(resetBtn);

        add(canvas, BorderLayout.CENTER);
        add(btnRow, BorderLayout.SOUTH);

        canvas.addPropertyChangeListener(CurveCanvas.PROPERTY_CURVE_CHANGED,
                e -> firePropertyChange(PROPERTY_CURVE_CHANGED, e.getOldValue(), e.getNewValue()));
    }

    public void setControlPoints(int[][] points) {
        canvas.setControlPoints(points);
    }

}
