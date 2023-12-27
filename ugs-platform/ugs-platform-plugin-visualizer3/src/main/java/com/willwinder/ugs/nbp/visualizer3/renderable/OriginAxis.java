package com.willwinder.ugs.nbp.visualizer3.renderable;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.control.BillboardControl.Alignment;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Line;

/** A widget that draws the X, Y, and Z axis as colored arrows with labels. */
public class OriginAxis extends Node {

    public OriginAxis(AssetManager assetManager, BitmapFont font) {
        super("OriginAxis");

        // Draw the arrowheads and labels
        String[] labels = { "X", "Y", "Z" };
        Vector3f[] directions = { Vector3f.UNIT_X, Vector3f.UNIT_Y, Vector3f.UNIT_Z };
        ColorRGBA[] colors = { ColorRGBA.Red, ColorRGBA.Green, ColorRGBA.Blue };

        for (int i = 0; i < 3; i++) {
            // Draw the arrow
            {
                Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.setColor("Color", colors[i]);
                mat.getAdditionalRenderState().setLineWidth(2.0f);

                Geometry arrowLine = new Geometry(name + "_" + labels[i] + "_ArrowLine",
                        new Line(Vector3f.ZERO, directions[i]));
                arrowLine.setMaterial(mat);
                attachChild(arrowLine);

                Geometry arrowHead = new Geometry(name + "_" + labels[i] + "_ArrowHead",
                        new Cylinder(2, 16, Float.MIN_VALUE, 0.07f, 0.2f, true, false));
                arrowHead.setMaterial(mat);
                arrowHead.lookAt(directions[i], Vector3f.UNIT_Y);
                arrowHead.setLocalTranslation(directions[i]);
                attachChild(arrowHead);
            }

            // Draw the axis label
            {
                var label = new BitmapText(font);
                label.setColor(colors[i]);
                label.setSize(1.0f);
                label.setText(labels[i]);
                // Center the text about the origin so we can billboard/scale using that as the
                // pivot.
                label.setLocalTranslation(new Vector3f(-label.getLineWidth() / 2, label.getLineHeight() / 2, 0));
                label.setQueueBucket(Bucket.Transparent);

                Node node = new Node(name + "_" + labels[i] + "_label");
                node.attachChild(label);
                node.setLocalTranslation(directions[i].mult(1.4f));
                node.setLocalScale(0.25f);
                var billboardControl = new BillboardControl();
                billboardControl.setAlignment(Alignment.Screen);
                node.addControl(billboardControl);
                // TODO uniform size letters?
                // node.addControl(new ConstantScreenSizeControl(12.f));
                attachChild(node);
            }
        }
    }
}
