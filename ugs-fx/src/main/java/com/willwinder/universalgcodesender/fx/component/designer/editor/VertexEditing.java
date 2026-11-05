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
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;

/**
 * What the active vertex tool has selected, for the actions and menus that work on the
 * selected node of a path. Updated and read on the JavaFX thread.
 */
public final class VertexEditing {
    private static final ReadOnlyBooleanWrapper NODE_SELECTED = new ReadOnlyBooleanWrapper(false);
    private static final ReadOnlyBooleanWrapper CAN_BREAK = new ReadOnlyBooleanWrapper(false);
    private static final ReadOnlyBooleanWrapper CAN_JOIN = new ReadOnlyBooleanWrapper(false);
    private static final ReadOnlyBooleanWrapper CAN_ADD_NODES = new ReadOnlyBooleanWrapper(false);
    private static final ReadOnlyBooleanWrapper CAN_MAKE_CURVE = new ReadOnlyBooleanWrapper(false);
    private static final ReadOnlyBooleanWrapper CAN_MAKE_LINE = new ReadOnlyBooleanWrapper(false);
    private static VertexTool activeTool;

    private VertexEditing() {
    }

    /**
     * True while the vertex tool is active and one of the nodes of the path is selected.
     */
    public static ReadOnlyBooleanProperty nodeSelectedProperty() {
        return NODE_SELECTED.getReadOnlyProperty();
    }

    /**
     * True when the path can be broken at the selected node, which is any node but the ends of
     * an open path.
     */
    public static ReadOnlyBooleanProperty canBreakProperty() {
        return CAN_BREAK.getReadOnlyProperty();
    }

    /**
     * True when exactly two nodes are selected and both are loose ends of open subpaths, which
     * can be joined into one node.
     */
    public static ReadOnlyBooleanProperty canJoinProperty() {
        return CAN_JOIN.getReadOnlyProperty();
    }

    /**
     * True when two of the selected nodes are next to each other, so a node can be added
     * between them.
     */
    public static ReadOnlyBooleanProperty canAddNodesProperty() {
        return CAN_ADD_NODES.getReadOnlyProperty();
    }

    /**
     * True when one of the selected nodes has a straight line on a side, which can be curved.
     */
    public static ReadOnlyBooleanProperty canMakeCurveProperty() {
        return CAN_MAKE_CURVE.getReadOnlyProperty();
    }

    /**
     * True when one of the selected nodes has a curve on a side, which can be straightened.
     */
    public static ReadOnlyBooleanProperty canMakeLineProperty() {
        return CAN_MAKE_LINE.getReadOnlyProperty();
    }

    /**
     * Adds a node halfway between each two selected nodes next to each other.
     *
     * @return true when nodes were added
     */
    public static boolean addNodes() {
        return activeTool != null && activeTool.addNodesBetweenSelected();
    }

    /**
     * Turns the selected nodes into smooth curve nodes.
     *
     * @return true when a node was changed
     */
    public static boolean makeCurve() {
        return activeTool != null && activeTool.makeSelectedNodesCurves();
    }

    /**
     * Turns the selected nodes into corner nodes with straight lines on both sides.
     *
     * @return true when a node was changed
     */
    public static boolean makeLine() {
        return activeTool != null && activeTool.makeSelectedNodesLines();
    }

    /**
     * Joins the two selected loose ends into one node.
     *
     * @return true when the nodes were joined
     */
    public static boolean joinPath() {
        return activeTool != null && activeTool.joinSelectedNodes();
    }

    /**
     * Breaks the path at the selected node.
     *
     * @return true when the path was broken
     */
    public static boolean breakAtSelectedNode() {
        return activeTool != null && activeTool.breakAtSelectedNode();
    }

    /**
     * What can be done with the selected nodes.
     */
    record Abilities(boolean canBreak, boolean canJoin, boolean canAddNodes, boolean canMakeCurve,
                     boolean canMakeLine) {
        static final Abilities NONE = new Abilities(false, false, false, false, false);
    }

    static void update(VertexTool tool, boolean nodeSelected, Abilities abilities) {
        activeTool = tool;
        NODE_SELECTED.set(nodeSelected);
        CAN_BREAK.set(nodeSelected && abilities.canBreak());
        CAN_JOIN.set(nodeSelected && abilities.canJoin());
        CAN_ADD_NODES.set(nodeSelected && abilities.canAddNodes());
        CAN_MAKE_CURVE.set(nodeSelected && abilities.canMakeCurve());
        CAN_MAKE_LINE.set(nodeSelected && abilities.canMakeLine());
    }

    static void clear(VertexTool tool) {
        if (activeTool == tool) {
            activeTool = null;
            NODE_SELECTED.set(false);
            CAN_BREAK.set(false);
            CAN_JOIN.set(false);
            CAN_ADD_NODES.set(false);
            CAN_MAKE_CURVE.set(false);
            CAN_MAKE_LINE.set(false);
        }
    }
}
