package com.willwinder.ugs.nbp.visualizer3.renderable;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Cylinder;

/** A cone pointing at the origin that is about 1 unit tall. */
public class Cursor extends Node {
    private AssetManager assetManager;
    private Geometry toolBottom;
    private Geometry toolTop;

    public Cursor(AssetManager assetManager) {
        super("Cursor");

        // We could probably get away with fewer if we used a geometry generation
        // strategy that includes smooth normals. Here we're hacking cylinders into
        // cones.
        int radialSamples = 32;

        this.assetManager = assetManager;
        toolBottom = new Geometry(name + "_bottom",
                new Cylinder(2, radialSamples, 0.15f, Float.MIN_VALUE, 1.0f, true, false));
        toolBottom.setLocalTranslation(0, 0, 0.5f);

        toolTop = new Geometry(name + "_top",
                new Cylinder(2, radialSamples, Float.MIN_VALUE, 0.15f, 0.04f, true, false));
        toolTop.setLocalTranslation(0, 0, 1.02f);

        setColor(ColorRGBA.BlackNoAlpha);

        attachChild(toolBottom);
        attachChild(toolTop);
    }

    public void setColor(ColorRGBA color) {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/fakeLighting.j3md");
        mat.setColor("Color", color);
        toolBottom.setMaterial(mat);
        toolTop.setMaterial(mat);
    }
}
