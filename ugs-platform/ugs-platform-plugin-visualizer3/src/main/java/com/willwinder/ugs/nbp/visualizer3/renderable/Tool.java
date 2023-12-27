package com.willwinder.ugs.nbp.visualizer3.renderable;

import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_TOOL_COLOR;

import com.jme3.asset.AssetManager;
import com.jme3.scene.Node;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbp.visualizer3.ConstantScreenSizeControl;
import com.willwinder.ugs.nbp.visualizer3.Convert;

/** A widget that visualizes the tool. */
public class Tool extends Node implements IPreferencesListener {
    Cursor cursor;

    public Tool(AssetManager assetManager) {
        super("Tool");

        cursor = new Cursor(assetManager);

        Node tool = new Node(name);
        tool.attachChild(cursor);
        tool.addControl(new ConstantScreenSizeControl(80.0f));
        attachChild(tool);
    }

    public void reloadPreferences(VisualizerOptions vo) {
        cursor.setColor(Convert.ToColorRGBA(vo.getOptionForKey(VISUALIZER_OPTION_TOOL_COLOR).value));
    }
}
