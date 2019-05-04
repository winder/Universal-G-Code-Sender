package com.willwinder.ugs.nbm.visualizer.renderables;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;

import javax.vecmath.Point3d;

import static com.jogamp.opengl.GL.GL_LINES;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_X;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Y;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Z;

/**
 * Displays the machine boundries based on the soft limits
 */
public class MachineBoundries extends Renderable {
    private final BackendAPI backendAPI;
    private double softLimitX = 0;
    private double softLimitY = 0;
    private double softLimitZ = 0;
    private float[] machineBoundryBottomColor;
    private float[] machineBoundryLineColor;
    private float[] yAxisColor;
    private float[] xAxisColor;
    private float[] zAxisColor;
    private boolean softLimitsEnabled = false;

    /**
     * Construct with a priority number. Objects should be rendered from highest
     * to lowest priority.
     */
    public MachineBoundries(String title) {
        super(Integer.MIN_VALUE, title);
        reloadPreferences(new VisualizerOptions());
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(event -> onUGSEvent());
    }

    private void onUGSEvent() {
        try {
            // This will prevent us from accessing the firmware settings before the init
            // processes has finished and it will also prevent us from accessing the
            // controller after it has disconnected
            if (!backendAPI.isConnected() || !backendAPI.isIdle()) {
                return;
            }

            softLimitsEnabled = backendAPI.getController().getFirmwareSettings().isSoftLimitsEnabled();
            if (softLimitsEnabled) {
                softLimitX = backendAPI.getController().getFirmwareSettings().getSoftLimit(Axis.X);
                softLimitY = backendAPI.getController().getFirmwareSettings().getSoftLimit(Axis.Y);
                softLimitZ = backendAPI.getController().getFirmwareSettings().getSoftLimit(Axis.Z);
            }
        } catch (FirmwareSettingsException ignored) {
            // Never mind this.
        }

    }

    @Override
    public boolean rotate() {
        return true;
    }

    @Override
    public boolean center() {
        return true;
    }

    @Override
    public boolean enableLighting() {
        return false;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        // Not used
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
        machineBoundryBottomColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VisualizerOptions.VISUALIZER_OPTION_BOUNDRY_BASE).value);
        machineBoundryLineColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VisualizerOptions.VISUALIZER_OPTION_BOUNDRY_SIDES).value);
        yAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_X).value);
        xAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_Y).value);
        zAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_Z).value);
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d machineCoord, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor, Point3d mouseWorldCoordinates, Point3d rotation) {
        if (!softLimitsEnabled) {
            return;
        }

        double xOffset = workCoord.x - machineCoord.x;
        double yOffset = workCoord.y - machineCoord.y;
        double zOffset = workCoord.z - machineCoord.z;

        Point3d bottomLeft = new Point3d(-softLimitX + xOffset, -softLimitY + yOffset, -softLimitZ + zOffset);
        Point3d topRight = new Point3d(xOffset, yOffset, zOffset);

        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
            drawBase(gl, bottomLeft, topRight);
            drawSides(gl, bottomLeft, topRight);
            drawAxisLines(gl, bottomLeft, topRight);
        gl.glPopMatrix();
    }

    private void drawBase(GL2 gl, Point3d bottomLeft, Point3d topRight) {
        gl.glColor4fv(machineBoundryBottomColor, 0);
        gl.glBegin(GL2.GL_QUADS);
            gl.glVertex3d(bottomLeft.x, bottomLeft.y, bottomLeft.getZ());
            gl.glVertex3d(bottomLeft.x, topRight.y, bottomLeft.getZ());
            gl.glVertex3d(topRight.x, topRight.y, bottomLeft.getZ());
            gl.glVertex3d(topRight.x, bottomLeft.y, bottomLeft.getZ());
        gl.glEnd();
    }

    private void drawAxisLines(GL2 gl, Point3d bottomLeft, Point3d topRight) {
        double offset = 0.001;
        gl.glLineWidth(5f);
        gl.glBegin(GL_LINES);
            // X Axis Line
            gl.glColor4fv(yAxisColor, 0);
            gl.glVertex3d(0, bottomLeft.y, offset);
            gl.glVertex3d(0, topRight.y, offset);

            gl.glVertex3d(0, bottomLeft.y, offset);
            gl.glVertex3d(0, topRight.y, offset);

            // Y Axis Line
            gl.glColor4fv(xAxisColor, 0);
            gl.glVertex3d(bottomLeft.x, 0, offset);
            gl.glVertex3d(topRight.x, 0, offset);

            gl.glVertex3d(bottomLeft.x, 0, offset);
            gl.glVertex3d(topRight.x, 0, offset);

            // Z Axis Line
            gl.glColor4fv(zAxisColor, 0);
            gl.glVertex3d(0, 0, bottomLeft.z);
            gl.glVertex3d(0, 0, Math.max(topRight.z, -bottomLeft.z));
        gl.glEnd();
    }

    private void drawSides(GL2 gl, Point3d bottomLeft, Point3d topRight) {
        double offset = 0.001;
        gl.glLineWidth(3f);
        gl.glBegin(GL_LINES);
            gl.glColor4fv(machineBoundryLineColor, 0);
            gl.glVertex3d(bottomLeft.x, bottomLeft.y, bottomLeft.getZ() + offset);
            gl.glVertex3d(bottomLeft.x, topRight.y, bottomLeft.getZ() + offset);
            gl.glVertex3d(bottomLeft.x, topRight.y, bottomLeft.getZ() + offset);
            gl.glVertex3d(topRight.x, topRight.y, bottomLeft.getZ() + offset);
            gl.glVertex3d(topRight.x, topRight.y, bottomLeft.getZ() + offset);
            gl.glVertex3d(topRight.x, bottomLeft.y, bottomLeft.getZ() + offset);
            gl.glVertex3d(topRight.x, bottomLeft.y, bottomLeft.getZ() + offset);
            gl.glVertex3d(bottomLeft.x, bottomLeft.y, bottomLeft.getZ() + offset);

            gl.glVertex3d(bottomLeft.x, bottomLeft.y, topRight.getZ() + offset);
            gl.glVertex3d(bottomLeft.x, topRight.y, topRight.getZ() + offset);
            gl.glVertex3d(bottomLeft.x, topRight.y, topRight.getZ() + offset);
            gl.glVertex3d(topRight.x, topRight.y, topRight.getZ() + offset);
            gl.glVertex3d(topRight.x, topRight.y, topRight.getZ() + offset);
            gl.glVertex3d(topRight.x, bottomLeft.y, topRight.getZ() + offset);
            gl.glVertex3d(topRight.x, bottomLeft.y, topRight.getZ() + offset);
            gl.glVertex3d(bottomLeft.x, bottomLeft.y, topRight.getZ() + offset);

            gl.glVertex3d(bottomLeft.x, bottomLeft.y, bottomLeft.getZ());
            gl.glVertex3d(bottomLeft.x, bottomLeft.y, topRight.getZ());

            gl.glVertex3d(bottomLeft.x, topRight.y, bottomLeft.getZ());
            gl.glVertex3d(bottomLeft.x, topRight.y, topRight.getZ());

            gl.glVertex3d(topRight.x, bottomLeft.y, bottomLeft.getZ());
            gl.glVertex3d(topRight.x, bottomLeft.y, topRight.getZ());

            gl.glVertex3d(topRight.x, topRight.y, bottomLeft.getZ());
            gl.glVertex3d(topRight.x, topRight.y, topRight.getZ());
        gl.glEnd();
    }
}
