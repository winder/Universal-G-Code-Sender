package com.willwinder.ugs.nbm.visualizer.renderables;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;

import javax.vecmath.Point3d;

import static com.jogamp.opengl.GL.GL_LINES;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_X;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_XY_GRID;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_XY_PLANE;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Y;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Z;

public class Machine extends Renderable {
    private final BackendAPI backendAPI;
    private double softLimitX = 0;
    private double softLimitY = 0;
    private double softLimitZ = 0;
    private float[] gridPlaneColor;
    private float[] gridLineColor;
    private float[] yAxisColor;
    private float[] xAxisColor;
    private float[] zAxisColor;

    /**
     * Construct with a priority number. Objects should be rendered from highest
     * to lowest priority.
     */
    public Machine() {
        super(Integer.MIN_VALUE, "Machine boundries");
        reloadPreferences(new VisualizerOptions());
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this::onUGSEvent);
    }

    private void onUGSEvent(UGSEvent event) {
        if (backendAPI.isConnected() && backendAPI.getController().getFirmwareSettings() != null) {
            try {
                softLimitX = backendAPI.getController().getFirmwareSettings().getSoftLimitX();
                softLimitY = backendAPI.getController().getFirmwareSettings().getSoftLimitY();
                softLimitZ = backendAPI.getController().getFirmwareSettings().getSoftLimitZ();
            } catch (FirmwareSettingsException e) {
                e.printStackTrace();
            }
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
        gridPlaneColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_XY_PLANE).value);
        gridLineColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_XY_GRID).value);
        yAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_X).value);
        xAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_Y).value);
        zAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_Z).value);
    }

    private double getBestStepSize(double maxSide) {
        return maxSide / 20;
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d machineCoord, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor, Point3d mouseWorldCoordinates, Point3d rotation) {
        double maxSide = VisualizerUtils.findMaxSide(focusMin, focusMax);
        if (maxSide == 0) {
            maxSide = 1;
        }

        double xOffset = workCoord.x - machineCoord.x;
        double yOffset = workCoord.y - machineCoord.y;
        double zOffset = workCoord.z - machineCoord.z;
        double stepSize = getBestStepSize(maxSide);

        Point3d bottomLeft = new Point3d(-softLimitX + xOffset, -softLimitY + yOffset, -softLimitZ + zOffset);
        Point3d topRight = new Point3d(xOffset, yOffset, zOffset);

        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();


        double offset = 0.001;

        gl.glLineWidth(1.5f);
        grid(stepSize, bottomLeft, topRight, gl, offset);


        gl.glColor4fv(gridPlaneColor, 0);
        //System.out.println(bottomLeft + " x " + topRight + " work: " + workCoord + " machine: " + machineCoord);
        // floor - cover entire model and a little extra.
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3d(bottomLeft.x, bottomLeft.y, bottomLeft.getZ());
        gl.glVertex3d(bottomLeft.x, topRight.y, bottomLeft.getZ());
        gl.glVertex3d(topRight.x, topRight.y, bottomLeft.getZ());
        gl.glVertex3d(topRight.x, bottomLeft.y, bottomLeft.getZ());
        gl.glEnd();
        gl.glPopMatrix();
    }

    private void grid(double stepSize, Point3d bottomLeft, Point3d topRight, GL2 gl, double offset) {
        // grid
        gl.glBegin(GL_LINES);
        for (double x = bottomLeft.x; x <= topRight.x; x += stepSize) {
            for (double y = bottomLeft.y; y <= topRight.y; y += stepSize) {
                if (x == 0) continue;
                gl.glColor4fv(gridLineColor, 0);

                gl.glVertex3d(x, bottomLeft.y, bottomLeft.getZ() + offset);
                gl.glVertex3d(x, topRight.y, bottomLeft.getZ() + offset);

                gl.glVertex3d(x, bottomLeft.y, bottomLeft.getZ() - offset);
                gl.glVertex3d(x, topRight.y, bottomLeft.getZ() - offset);

                if (y == 0) continue;
                gl.glColor4fv(gridLineColor, 0);
                gl.glVertex3d(bottomLeft.x, y, bottomLeft.getZ() + offset);
                gl.glVertex3d(topRight.x, y, bottomLeft.getZ() + offset);

                gl.glVertex3d(bottomLeft.x, y, bottomLeft.getZ() - offset);
                gl.glVertex3d(topRight.x, y, bottomLeft.getZ() - offset);
            }
        }
        gl.glEnd();

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
}
