package com.willwinder.ugs.nbp.visualizer3;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Plane;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.willwinder.ugs.nbp.visualizer3.renderable.Cursor;

/**
 * Owns and updates the cursor at the XY plane representing the mouse.
 */
public class MouseCursorState extends AbstractAppState {

    private Application app;

    Vector3f lastCursorPosition = Vector3f.ZERO.clone();
    boolean cursorPositionValid;

    private Cursor cursor;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = app;

        cursor = new Cursor(app.getAssetManager());
        cursor.setColor(ColorRGBA.White);
        // cursor.addControl(new ConstantScreenSizeControl(60.0f));
    }

    @Override
    public void update(float tpf) {
        Vector2f click2d = app.getInputManager().getCursorPosition();
        Vector3f click3d = app.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
        Vector3f dir = app.getCamera().getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f)
                .subtractLocal(click3d)
                .normalizeLocal();
        Ray ray = new Ray(click3d, dir);

        cursorPositionValid = ray.intersectsWherePlane(new Plane(Vector3f.UNIT_Z, Vector3f.ZERO), lastCursorPosition);
    }

    @Override
    public void render(RenderManager rm) {
        if (cursorPositionValid && app.getInputManager().isCursorVisible()) {
            cursor.setLocalTranslation(lastCursorPosition);
            cursor.updateGeometricState();
            rm.renderScene(cursor, app.getViewPort());
        }
    }
}
