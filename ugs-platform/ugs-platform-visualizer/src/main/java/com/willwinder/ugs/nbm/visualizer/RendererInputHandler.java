/**
 * Process all the listeners and call methods in the renderer.
 */
/*
    Copyright 2016-2017 Will Winder

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

import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.google.common.eventbus.Subscribe;
import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbm.visualizer.renderables.GcodeModel;
import com.willwinder.ugs.nbm.visualizer.renderables.Highlight;
import com.willwinder.ugs.nbm.visualizer.renderables.Selection;
import com.willwinder.ugs.nbm.visualizer.renderables.SizeDisplay;
import com.willwinder.ugs.nbp.lib.eventbus.HighlightEvent;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Settings.FileStats;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import javax.swing.SwingUtilities;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class RendererInputHandler implements
        WindowListener, MouseWheelListener, MouseMotionListener,
        MouseListener, KeyListener, PreferenceChangeListener,
        ControllerListener, UGSEventListener {
    final private GcodeRenderer gcodeRenderer;
    final private FPSAnimator animator;
    private final GcodeModel gcodeModel;
    private final Highlight highlight;
    private final SizeDisplay sizeDisplay;
    private final Selection selection;
    private final VisualizerPopupMenu visualizerPopupMenu;
    private Settings settings;

    private static final int HIGH_FPS = 15;
    private static final int LOW_FPS = 4;

    public RendererInputHandler(GcodeRenderer gr, FPSAnimator a,
            VisualizerPopupMenu popup, Settings s) {
        gcodeRenderer = gr;
        animator = a;
        animator.start();
        visualizerPopupMenu = popup;
        settings = s;

        gcodeModel = new GcodeModel(Localization.getString("platform.visualizer.renderable.gcode-model"));
        highlight = new Highlight(gcodeModel, Localization.getString("platform.visualizer.renderable.highlight"));
        sizeDisplay = new SizeDisplay(Localization.getString("platform.visualizer.renderable.gcode-model-size"));
        selection = new Selection(Localization.getString("platform.visualizer.renderable.selection"));

        gr.registerRenderable(gcodeModel);
        gr.registerRenderable(highlight);
        gr.registerRenderable(sizeDisplay);
        gr.registerRenderable(selection);
    }

    
    private void setFPS(int fps) {
        animator.stop();
        animator.setFPS(fps);
        animator.start();
    }


    @Subscribe
    public void highlightEventListener(HighlightEvent he) {
        highlight.setHighlightedLines(he.getLines());
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        gcodeRenderer.reloadPreferences();
    }
 
    public void setGcodeFile(String file) {
        gcodeModel.setGcodeFile(file);
        gcodeRenderer.setObjectSize(gcodeModel.getMin(), gcodeModel.getMax());

        updateBounds(gcodeModel.getMin(), gcodeModel.getMax());
    }

    /**
     * Pass new bounds (after interpolating arcs) in case of weird arcs.
     */
    private void updateBounds(Point3d min, Point3d max) {
        // Update bounds.
        FileStats fs = settings.getFileStats();
        fs.minCoordinate = new Position(min.x, min.y, min.z, Units.MM);
        fs.maxCoordinate = new Position(max.x, max.y, max.z, Units.MM);
        settings.setFileStats(fs);
    }

    /**
     * UGS Event Listener
     */
    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse.isFileChangeEvent()) {
            animator.pause();

            switch (cse.getFileState()) {
                case FILE_LOADED:
                case FILE_LOADING:
                    setGcodeFile(cse.getFile());
                    break;
            }

            animator.resume();
        }
    }


    
    /**
     * Mouse Motion Listener
     */

    @Override
    public void mouseDragged(java.awt.event.MouseEvent e) {
        // Don't rotate if we're making a selection.
        if (selecting){
            gcodeRenderer.mouseMoved(new Point(e.getX(), e.getY()));
            selection.setEnd(gcodeRenderer.getMouseWorldLocation());
            return;
        }


        if (SwingUtilities.isLeftMouseButton(e)) {
            int x = e.getX();
            int y = e.getY();
            
            int panMouseButton = InputEvent.BUTTON2_MASK; // TODO: Make configurable

            if (e.isShiftDown() || e.getModifiers() == panMouseButton) {
                gcodeRenderer.mousePan(new Point(x,y));
            } else {
                gcodeRenderer.mouseRotate(new Point(x,y));
            }
        }
    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent e) {
        gcodeRenderer.mouseMoved(new Point(e.getX(), e.getY()));
    }

    /**
     * Mouse Wheel Listener
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        gcodeRenderer.zoom(e.getWheelRotation());
    }

    
    /**
     * Window Listener
     */

    @Override
    public void windowDeactivated(java.awt.event.WindowEvent e) {
        // Run this on another thread than the AWT event queue to
        // make sure the call to Animator.stop() completes before
        // exiting
        new Thread(animator::stop).start();
    }

    @Override
    public void windowOpened(java.awt.event.WindowEvent e) {
        new Thread(animator::start).start();
    }

    @Override
    public void windowClosing(java.awt.event.WindowEvent e) {
    }

    @Override
    public void windowClosed(java.awt.event.WindowEvent e) {
    }

    @Override
    public void windowIconified(java.awt.event.WindowEvent e) {
    }

    @Override
    public void windowDeiconified(java.awt.event.WindowEvent e) {
    }

    @Override
    public void windowActivated(java.awt.event.WindowEvent e) {
    }

    /**
     * Mouse Listener
     */

    @Override
    public void mouseClicked(MouseEvent e) {
        // Show popup
        if (SwingUtilities.isRightMouseButton(e) || e.isControlDown()) {
            Point3d coords = gcodeRenderer.getMouseWorldLocation();
            this.visualizerPopupMenu.setJogLocation(coords.x, coords.y);
            this.visualizerPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private boolean selecting = false;
    private Point3d selectionStart = null;
    private Point3d selectionEnd = null;

    /**
     * Mouse pressed is called on mouse-down.
     * Mouse released and mouse clicked are called on mouse-up.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        setFPS(HIGH_FPS);
        // Zoom
        if (e.getButton() == MouseEvent.BUTTON1 && e.isMetaDown()) {
            selecting = true;
            selectionStart = gcodeRenderer.getMouseWorldLocation();
            selection.setStart(selectionStart);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        setFPS(LOW_FPS);

        // Finish selecting.
        if (selecting) {
            selecting = false;
            selectionEnd = gcodeRenderer.getMouseWorldLocation();
            gcodeRenderer.zoomToRegion(selectionStart, selectionEnd, 1.0);
            selection.clear();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Key Listener
     */

     /**
     * KeyListener method.
     */
    @Override
    public void keyTyped(KeyEvent ke) {
    }

    /**
     * KeyListener method.
     */
    @Override
    public void keyPressed(KeyEvent ke) {
        setFPS(HIGH_FPS);

        int DELTA_SIZE = 1;
            
        switch(ke.getKeyCode()) {
            case KeyEvent.VK_UP:
                gcodeRenderer.pan(0, DELTA_SIZE);
                //this.eye.y+=DELTA_SIZE;
                break;
            case KeyEvent.VK_DOWN:
                gcodeRenderer.pan(0, -DELTA_SIZE);
                break;
            case KeyEvent.VK_LEFT:
                gcodeRenderer.pan(-DELTA_SIZE, 0);
                break;
            case KeyEvent.VK_RIGHT:
                gcodeRenderer.pan(DELTA_SIZE, 0);
                break;
            case KeyEvent.VK_MINUS:
                if (ke.isControlDown())
                    gcodeRenderer.zoom(-1);
                break;
            case KeyEvent.VK_0:
            case KeyEvent.VK_ESCAPE:
                gcodeRenderer.resetView();
                break;
        }
        
        switch(ke.getKeyChar()) {
            case '+':
                if (ke.isControlDown())
                    gcodeRenderer.zoom(1);
                break;
        }
    }
    
    /**
     * KeyListener method.
     */
    @Override
    public void keyReleased(KeyEvent ke) {
        setFPS(LOW_FPS);
    }

    /**
     * Controller listener methods
     */
    @Override
    public void statusStringListener(ControllerStatus status) {
        sizeDisplay.setUnits(status.getMachineCoord().getUnits());
        gcodeRenderer.setMachineCoordinate(status.getMachineCoord());
        gcodeRenderer.setWorkCoordinate(status.getWorkCoord());
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
    }

    @Override
    public void commandSent(GcodeCommand command) {
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        gcodeModel.setCurrentCommandNumber(command.getCommandNumber());
        // TODO: When to redraw??
    }

    @Override
    public void commandComment(String comment) {
    }

    @Override
    public void probeCoordinates(Position p) {
    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
    }

    @Override
    public void postProcessData(int numRows) {
    }
}
