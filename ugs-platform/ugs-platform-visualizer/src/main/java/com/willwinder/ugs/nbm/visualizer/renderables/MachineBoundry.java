package com.willwinder.ugs.nbm.visualizer.renderables;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;

import javax.vecmath.Point3d;

import static com.jogamp.opengl.GL.GL_LINES;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_X;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Y;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Z;

public class MachineBoundry extends Renderable {
    private final BackendAPI backendAPI;
    private double softLimitX = 0;
    private double softLimitY = 0;
    private double softLimitZ = 0;
    private float[] machineBoundryBottomColor;
    private float[] machineBoundryLineColor;
    private float[] yAxisColor;
    private float[] xAxisColor;
    private float[] zAxisColor;
    private boolean softLimitsEnabled;

    /**
     * Construct with a priority number. Objects should be rendered from highest
     * to lowest priority.
     */
    public MachineBoundry() {
        super(Integer.MIN_VALUE, "Machine boundries");
        reloadPreferences(new VisualizerOptions());
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this::onUGSEvent);
    }

    private void onUGSEvent(UGSEvent event) {
        try {
            softLimitsEnabled = backendAPI.isConnected() && backendAPI.getController().getFirmwareSettings().isSoftLimitsEnabled();
            if (backendAPI.isConnected() && backendAPI.isIdle() && backendAPI.getController().getCapabilities().hasSoftLimits() && softLimitsEnabled) {
                softLimitX = backendAPI.getController().getFirmwareSettings().getSoftLimitX();
                softLimitY = backendAPI.getController().getFirmwareSettings().getSoftLimitY();
                softLimitZ = backendAPI.getController().getFirmwareSettings().getSoftLimitZ();
            }
        } catch (FirmwareSettingsException e) {
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

    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
        machineBoundryBottomColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VisualizerOptions.VISUALIZER_OPTION_BOUNDRY_PLANE).value);
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
            drawBoundryLines(gl, bottomLeft, topRight);
            drawAxisLines(gl, bottomLeft, topRight);
            drawBottomPlane(gl, bottomLeft, topRight);
        gl.glPopMatrix();
    }

    private void drawBottomPlane(GL2 gl, Point3d bottomLeft, Point3d topRight) {
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

    private double drawBoundryLines(GL2 gl, Point3d bottomLeft, Point3d topRight) {
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
        return offset;
    }
}
