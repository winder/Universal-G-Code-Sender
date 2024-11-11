package com.willwinder.ugs.nbm.visualizer.shared;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shader.Shader;
import com.willwinder.ugs.nbm.visualizer.utils.RenderableUtils;
import com.willwinder.universalgcodesender.model.Position;

import java.util.ArrayList;
import java.util.List;

import static com.willwinder.ugs.nbm.visualizer.utils.RenderableUtils.*;

/**
 * A base class for rendering a vertex buffer object using a shader
 */
public abstract class VertexObjectRenderable extends Renderable {
    private double stepSize;
    private final List<Float> vertexList = new ArrayList<>();
    private final List<Float> normalList = new ArrayList<>();
    private final List<Float> colorList = new ArrayList<>();

    private Position objectMin = Position.ZERO;
    private Position objectMax = Position.ZERO;
    private Shader shader;

    private boolean reloadModel;
    private int vertexObjectId;
    private int vertexBufferId;
    private int colorBufferId;
    private int normalBufferId;


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
        return vertexList.size();
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
        super.reloadPreferences(vo);

        // Need to reload model the next time to load new colors and settings
        reloadModel = true;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        shader.init(drawable.getGL().getGL2());
    }

    private double padMinMaxPoint(double stepSize, double point, boolean negativeDirection) {
        if (stepSize < 0.01) return negativeDirection ? -1 : 1;

        int step = negativeDirection ? -1 : 1;
        long numberOfFullSteps = Math.round(point / stepSize);
        return (numberOfFullSteps + step) * stepSize;
    }

    @Override
    public final void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position objectMin, Position objectMax, double scaleFactor, Position mouseWorldCoordinates, Position rotation) {
        GL2 gl = (GL2) drawable.getGL();

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
        }

        // Use the shader program
        gl.glUseProgram(shader.getProgramId());

        // Bind the VAO containing the vertex data
        gl.glBindVertexArray(vertexObjectId);
        render(drawable);

        // Unbind the VAO
        gl.glBindVertexArray(0);
        gl.glUseProgram(0);
    }


    private void updateBuffers(GL2 gl) {
        gl.glDeleteBuffers(2, new int[]{vertexBufferId, colorBufferId, normalBufferId}, 0);
        vertexObjectId = bindVertexObject(gl);
        vertexBufferId = bindVertexBuffer(gl, vertexList, shader.getShaderVertexIndex());
        colorBufferId = bindColorBuffer(gl, colorList, shader.getShaderColorIndex());
        normalBufferId = bindNormalBuffer(gl, normalList, shader.getShaderNormalIndex());
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
