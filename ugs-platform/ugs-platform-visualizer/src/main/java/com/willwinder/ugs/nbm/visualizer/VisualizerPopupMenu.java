/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.nbm.visualizer;

import com.willwinder.ugs.nbm.visualizer.shared.IRenderableRegistrationService;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.openide.util.Lookup;

/**
 *
 * @author wwinder
 */
public class VisualizerPopupMenu extends JPopupMenu {
    private static final Logger logger = Logger.getLogger(VisualizerPopupMenu.class.getName());
    private final JogToHereAction jogToHereAction;
    private final JMenuItem jogToHere = new JMenuItem();
    private final DecimalFormat decimalFormatter =
            new DecimalFormat("#.#####", Localization.dfs);

    public VisualizerPopupMenu(BackendAPI backend) {
        jogToHereAction = new JogToHereAction(backend);

        jogToHere.setText(String.format(Localization.getString("platform.visualizer.jogToHere"), 0, 0));

        jogToHere.setAction(jogToHereAction);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        IRenderableRegistrationService renderableService =
                Lookup.getDefault().lookup(IRenderableRegistrationService.class);
        Collection<Renderable> renderables = renderableService.getRenderables();

        this.removeAll();

        for (Renderable r : renderables) {
            JRenderableCheckBox box = new JRenderableCheckBox(r);
            add(box);
        }

        add(jogToHere);

        super.show(invoker, x, y);
    }

    public void setJogLocation(double x, double y) {

        String strX = decimalFormatter.format(x);
        String strY = decimalFormatter.format(y);

        jogToHereAction.setJogLocation(strX, strY);
        String jogToHereString = Localization.getString("platform.visualizer.popup.jogToHere");
        jogToHereString = jogToHereString.replaceAll("%f", "%s");
        jogToHere.setText(String.format(jogToHereString, strX, strY));
    }

    private static class JogToHereAction extends AbstractAction {
        private String x = "0";
        private String y = "0";
        private final BackendAPI backend;

        JogToHereAction(BackendAPI backend) {
            this.backend = backend;
        }

        public void setJogLocation(String x, String y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                backend.sendGcodeCommand("G0 X" + x + " Y" + y);
            } catch (Exception ex) {
                //logger.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
            }
        }
    }

    private class JRenderableCheckBox extends JCheckBox implements ItemListener {
        private Renderable r;

        public JRenderableCheckBox(Renderable r) {
            super(r.getTitle(), r.isEnabled());
            this.r = r;

            this.addItemListener(this);
        }

        @Override
        public void itemStateChanged(ItemEvent ie) {
            if (ie.getStateChange() == ItemEvent.SELECTED) {
                r.setEnabled(true);
            } else if (ie.getStateChange() == ItemEvent.DESELECTED) {
                r.setEnabled(false);
            }
        }
    }
}
