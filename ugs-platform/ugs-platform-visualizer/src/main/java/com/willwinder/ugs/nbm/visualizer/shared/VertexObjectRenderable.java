package com.willwinder.ugs.nbm.visualizer.shared;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.GLBuffers;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shader.Shader;
import com.willwinder.ugs.nbm.visualizer.utils.RenderableUtils;
import static com.willwinder.ugs.nbm.visualizer.utils.RenderableUtils.bindVertexBuffer;
import com.willwinder.universalgcodesender.model.Position;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A base class for rendering a vertex buffer object using a shader
 */
public abstract class VertexObjectRenderable extends Renderable {
    private double stepSize;
    private final IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX);
    private final List<Float> vertexList = new ArrayList<>();
    private final List<Float> normalList = new ArrayList<>();
    private final List<Float> colorList = new ArrayList<>();
    private final Shader shader;

    private Position objectMin = Position.ZERO;
    private Position objectMax = Position.ZERO;
    private boolean disabled = false;

    private interface Buffer {
        int VERTEX = 0;
        int NORMAL = 1;
        int COLOR = 2;
        int MAX = 3;
    }

    private boolean reloadModel;


    protected double getStepSize() {
        return stepSize;
    }

    protected VertexObjectRenderable(int priority, String title, String enabledOptionKey, Shader shader) {
        super(priority, title, enabledOptionKey);
        this.shader = shader;
        reloadPreferences(new VisualizerOptions());
    }

    protected void clear() {
        vertexList.clear();
        colorList.clear();
        normalList.clear();
    }

    @Override
    public boolean rotate() {
        return true;
    }

    @Override
    public boolean center() {
        return true;
    }

    protected void addNormal(double x, double y, double z) {
        RenderableUtils.addVertex(normalList, x, y, z);
    }

    protected void addVertex(double x, double y, double z) {
        RenderableUtils.addVertex(vertexList, x, y, z);
    }

    protected void addColor(float[] color) {
        RenderableUtils.addColor(colorList, color);
    }

    protected int getVertexCount() {
        return vertexList.size() / 3;
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
        super.reloadPreferences(vo);

        // Need to reload model the next time to load new colors and settings
        reloadModel = true;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        if (!gl.isFunctionAvailable("glGenBuffers") ||
                !gl.isFunctionAvailable("glBindBuffer") ||
                !gl.isFunctionAvailable("glBufferData") ||
                !gl.isFunctionAvailable("glDeleteBuffers")) {
            setEnabled(false);
            return;
        }

        gl.glGenBuffers(Buffer.MAX, bufferName);
        shader.init(gl);
        checkGLError(gl);
    }

    private double padMinMaxPoint(double stepSize, double point, boolean negativeDirection) {
        if (stepSize < 0.01) return negativeDirection ? -1 : 1;

        int step = negativeDirection ? -1 : 1;
        long numberOfFullSteps = Math.round(point / stepSize);
        return (numberOfFullSteps + step) * stepSize;
    }

    @Override
    public final void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position objectMin, Position objectMax, double scaleFactor, Position mouseWorldCoordinates, Position rotation) {
        GL2 gl = drawable.getGL().getGL2();

        if (!gl.isFunctionAvailable("glVertexAttribPointer") ||
                !gl.isFunctionAvailable("glBindBuffer") ||
                !gl.isFunctionAvailable("glUseProgram")) {
            setEnabled(false);
            return;
        }

        double newStepSize = RenderableUtils.getStepSize(scaleFactor);
        if (!this.objectMin.equals(objectMin) || !this.objectMax.equals(objectMax) || reloadModel || this.stepSize != newStepSize) {
            this.objectMin = new Position(objectMin);
            this.objectMax = new Position(objectMax);
            this.reloadModel = false;
            this.stepSize = newStepSize;

            Position bottomLeft = new Position(objectMin);
            Position topRight = new Position(objectMax);

            bottomLeft.x = padMinMaxPoint(newStepSize, bottomLeft.x, true);
            bottomLeft.y = padMinMaxPoint(newStepSize, bottomLeft.y, true);
            topRight.x = padMinMaxPoint(newStepSize, topRight.x, false);
            topRight.y = padMinMaxPoint(newStepSize, topRight.y, false);
            clear();
            reloadModel(gl, bottomLeft, topRight, scaleFactor);
            updateBuffers(gl);
            disabled = false;
        }

        if (disabled) {
            return;
        }

        try {
            // Use the shader program
            gl.glUseProgram(shader.getProgramId());
            checkGLError(gl);

            int positionAttribute = gl.glGetAttribLocation(shader.getProgramId(), "position");
            gl.glEnableVertexAttribArray(positionAttribute);
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl.glVertexAttribPointer(positionAttribute, 3, GL2.GL_FLOAT, false, 0, 0);
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
            checkGLError(gl);

            int colorAttribute = gl.glGetAttribLocation(shader.getProgramId(), "color");
            gl.glEnableVertexAttribArray(colorAttribute);
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, bufferName.get(Buffer.COLOR));
            gl.glVertexAttribPointer(colorAttribute, 4, GL2.GL_FLOAT, false, 0, 0);
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
            checkGLError(gl);

            render(drawable);

            // Disable the attribute after drawing
            gl.glDisableVertexAttribArray(positionAttribute);
            gl.glDisableVertexAttribArray(colorAttribute);
            checkGLError(gl);

            gl.glUseProgram(0);
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
            checkGLError(gl);
        } catch (Exception e) {
            // Temporarily disable the renderable
            disabled = true;
        } finally {
            gl.glUseProgram(0);
            gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        }
    }

    private void checkGLError(GL2 gl) {
        int error = gl.glGetError();
        if (error != GL2.GL_NO_ERROR) {
            // Try and clear all errors
            int errors = 0;
            while (gl.glGetError() != GL2.GL_NO_ERROR && errors < 10) {
                errors++;
            }
            throw new RuntimeException("GL error: " + error);
        }
    }

    private void updateBuffers(GL2 gl) {
        gl.glDeleteBuffers(3, new int[]{
                        bufferName.get(Buffer.VERTEX),
                        bufferName.get(Buffer.COLOR),
                        bufferName.get(Buffer.NORMAL)},
                0);


        bindVertexBuffer(gl, bufferName.get(Buffer.VERTEX), vertexList);
        bindVertexBuffer(gl, bufferName.get(Buffer.COLOR), colorList);
        bindVertexBuffer(gl, bufferName.get(Buffer.NORMAL), normalList);
    }

    /**
     * A method to be used for rendering the vertex buffer.
     *
     * @param drawable the current gl context
     */
    public abstract void render(GLAutoDrawable drawable);


    /**
     * Reload the model
     *
     * @param gl          the current gl context
     * @param bottomLeft  the loaded model bottom right corner
     * @param topRight    the loaded model top right corner
     * @param scaleFactor the scale factor being used
     */
    public abstract void reloadModel(GL2 gl, Position bottomLeft, Position topRight, double scaleFactor);
}
