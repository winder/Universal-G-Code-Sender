package com.willwinder.ugs.nbp.visualizer3.renderable;

import java.nio.FloatBuffer;
import java.util.List;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.Mesh.Mode;
import com.jme3.scene.VertexBuffer.Format;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.VertexBuffer.Usage;
import com.jme3.util.BufferUtils;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.renderables.GcodeLineColorizer;
import com.willwinder.ugs.nbm.visualizer.renderables.GcodeModel;
import com.willwinder.ugs.nbp.visualizer3.Convert;
import com.willwinder.universalgcodesender.visualizer.LineSegment;

public class GcodeVisualizer extends Node implements IPreferencesListener {
    GcodeLineColorizer colorizer;
    Material mat;

    Mesh mesh;
    List<LineSegment> lineList;
    FloatBuffer fcb;

    private long currentCommandNumber = 0;

    public GcodeVisualizer(AssetManager assetManager) {
        super("GcodeVisualizer");

        colorizer = new GcodeLineColorizer();
        mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setBoolean("VertexColor", true);
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
        colorizer.reloadPreferences(vo);
        updateColorBuffer();
    }

    public void setCurrentCommandNumber(long currentCommandNumber) {
        this.currentCommandNumber = currentCommandNumber;
        updateColorBuffer();
    }

    public void setModel(GcodeModel model) {
        lineList = model.getLineList();

        int vertexCount = lineList.size() * 2;

        mesh = new Mesh();
        mesh.setMode(Mode.Lines);

        VertexBuffer pb = new VertexBuffer(Type.Position);
        FloatBuffer fpb = BufferUtils.createFloatBuffer(vertexCount * 3);
        pb.setupData(Usage.Static, 3, Format.Float, fpb);
        mesh.setBuffer(pb);
        for (LineSegment line : lineList) {
            fpb.put(Convert.ToVector3f(line.getStart()).toArray(null));
            fpb.put(Convert.ToVector3f(line.getEnd()).toArray(null));
        }

        VertexBuffer cb = new VertexBuffer(Type.Color);
        fcb = BufferUtils.createFloatBuffer(vertexCount * 4);
        cb.setupData(Usage.Dynamic, 4, Format.Float, fcb);
        mesh.setBuffer(cb);

        mesh.updateCounts();

        updateColorBuffer();

        Geometry geometry = new Geometry(name + "_mesh", mesh);
        geometry.setMaterial(mat);

        detachAllChildren();
        attachChild(geometry);
    }

    private void updateColorBuffer() {
        if (mesh == null) {
            return;
        }

        fcb.clear();
        for (LineSegment line : lineList) {
            var color = Convert.ToColorRGBA(colorizer.getColor(line, currentCommandNumber));
            float[] colorArray = color.getColorArray();
            // Start point
            fcb.put(colorArray);
            // End point
            fcb.put(colorArray);
        }
    }
}
