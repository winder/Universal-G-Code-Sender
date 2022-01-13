/*
    Copyright 2016-2018 Will Winder

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

import com.jogamp.opengl.util.FPSAnimator;
import com.willwinder.ugs.nbm.visualizer.renderables.GcodeModel;
import com.willwinder.ugs.nbm.visualizer.renderables.Selection;
import com.willwinder.ugs.nbm.visualizer.renderables.SizeDisplay;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbm.visualizer.shared.RotationService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.*;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Settings.FileStats;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

/**
 * Process all the listeners and call methods in the renderer.
 *
 * @author wwinder
 */
public class RendererInputHandler implements
        WindowListener, MouseWheelListener, MouseMotionListener,
        MouseListener, KeyListener, PreferenceChangeListener, UGSEventListener {
    final private GcodeRenderer gcodeRenderer;
    final private FPSAnimator animator;
    private final BackendAPI backend;
    private final GcodeModel gcodeModel;
    private final SizeDisplay sizeDisplay;
    private final Selection selection;
    private Settings settings;

    private static final int HIGH_FPS = 15;
    private static final int LOW_FPS = 4;

    public RendererInputHandler(GcodeRenderer gr, FPSAnimator a, BackendAPI backend) {
        gcodeRenderer = gr;
        animator = a;
        this.backend = backend;
        animator.start();
        settings = backend.getSettings();

        RotationService rs = new RotationService();
        gcodeModel = new GcodeModel(Localization.getString("platform.visualizer.renderable.gcode-model"), rs);
        sizeDisplay = new SizeDisplay(Localization.getString("platform.visualizer.renderable.gcode-model-size"));
        selection = new Selection(Localization.getString("platform.visualizer.renderable.selection"));
        sizeDisplay.setUnits(settings.getPreferredUnits());

        gr.registerRenderable(gcodeModel);
        gr.registerRenderable(sizeDisplay);
        gr.registerRenderable(selection);
    }
    
    private void setFPS(int fps) {
        animator.stop();
        animator.setFPS(fps);
        animator.start();
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
    private void updateBounds(Position min, Position max) {
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
        if (cse instanceof FileStateEvent) {
            animator.pause();
            FileStateEvent fileStateEvent = (FileStateEvent) cse;
            switch (fileStateEvent.getFileState()) {
                case FILE_LOADED:
                    setGcodeFile(fileStateEvent.getFile());
                    break;
                case FILE_STREAM_COMPLETE:
                    gcodeModel.setCurrentCommandNumber(0);
                    break;
            }

            animator.resume();
        } else if (cse instanceof SettingChangedEvent) {
            sizeDisplay.setUnits(settings.getPreferredUnits());
        } else if (cse instanceof ControllerStatusEvent) {
            ControllerStatusEvent controllerStatusEvent = (ControllerStatusEvent) cse;
            gcodeRenderer.setMachineCoordinate(controllerStatusEvent.getStatus().getMachineCoord());
            gcodeRenderer.setWorkCoordinate(controllerStatusEvent.getStatus().getWorkCoord());
        } else if (cse instanceof CommandEvent) {
            CommandEvent commandEvent = (CommandEvent) cse;
            if (commandEvent.getCommandEventType() == CommandEventType.COMMAND_COMPLETE && !commandEvent.getCommand().isGenerated()) {
                gcodeModel.setCurrentCommandNumber(commandEvent.getCommand().getCommandNumber());
            }
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
            Position coords = gcodeRenderer.getMouseWorldLocation();

            // The position is always given in millimeters, convert to the preferred units
            Position position = new Position(coords.getX(), coords.getY(), coords.getZ(), Units.MM)
                    .getPositionIn(settings.getPreferredUnits());

            VisualizerPopupMenu visualizerPopupMenu = new VisualizerPopupMenu(backend, position);
            visualizerPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    private boolean selecting = false;
    private Position selectionStart = null;
    private Position selectionEnd = null;

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
}
