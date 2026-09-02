/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Shape3D;
import javafx.scene.transform.Transform;

import java.util.ArrayList;
import java.util.List;

/**
 * Walks a JavaFX scene graph and turns it into a flat list of draws for the Vulkan renderer.
 *
 * <p>The tree is never attached to a {@link javafx.scene.Scene}. JavaFX still composes the
 * transform chain and propagates property bindings on a detached tree, so the existing machine
 * models keep working unchanged: their nested groups, their {@code Rotate} and {@code Translate}
 * transforms and their bindings to the machine position all behave exactly as they do when
 * JavaFX renders them. Only the drawing is taken over here.
 *
 * <p>Reading the scene graph is not thread safe, so this has to run on the JavaFX application
 * thread, which is where the render pulse already happens.
 */
public final class SceneGraphWalker {
    private static final Color DEFAULT_COLOR = Color.web("#c0c0c0");

    private SceneGraphWalker() {
    }

    /**
     * A single shape to draw. The {@code key} is the shape it came from, so the renderer can
     * cache the uploaded vertex buffer and only ask for {@code vertices} on a cache miss.
     */
    public record Draw(Object key, Shape3D shape, float[] modelMatrix, float[] color) {
    }

    public static List<Draw> walk(Node root) {
        List<Draw> draws = new ArrayList<>();
        collect(root, draws);
        return draws;
    }

    private static void collect(Node node, List<Draw> draws) {
        if (!node.isVisible()) {
            return;
        }

        if (node instanceof Shape3D shape) {
            if (MeshConverter.isSupported(shape)) {
                draws.add(new Draw(shape, shape, toMatrix(shape.getLocalToSceneTransform()), color(shape)));
            }
            return;
        }

        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> collect(child, draws));
        }
    }

    private static float[] color(Shape3D shape) {
        Color diffuse = shape.getMaterial() instanceof PhongMaterial material && material.getDiffuseColor() != null
                ? material.getDiffuseColor()
                : DEFAULT_COLOR;

        return new float[]{
                (float) diffuse.getRed(),
                (float) diffuse.getGreen(),
                (float) diffuse.getBlue(),
                (float) diffuse.getOpacity()
        };
    }

    /**
     * The JavaFX affine transform as a column major matrix, which is the layout the shader's
     * {@code mat4} expects.
     */
    private static float[] toMatrix(Transform transform) {
        float[] matrix = new float[Mat4.ELEMENTS];
        matrix[0] = (float) transform.getMxx();
        matrix[1] = (float) transform.getMyx();
        matrix[2] = (float) transform.getMzx();
        matrix[4] = (float) transform.getMxy();
        matrix[5] = (float) transform.getMyy();
        matrix[6] = (float) transform.getMzy();
        matrix[8] = (float) transform.getMxz();
        matrix[9] = (float) transform.getMyz();
        matrix[10] = (float) transform.getMzz();
        matrix[12] = (float) transform.getTx();
        matrix[13] = (float) transform.getTy();
        matrix[14] = (float) transform.getTz();
        matrix[15] = 1;
        return matrix;
    }
}
