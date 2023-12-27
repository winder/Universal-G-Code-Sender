package com.willwinder.ugs.nbp.visualizer3;

import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;

// Control to force something to have a constant size in screen pixels.
public class ConstantScreenSizeControl extends AbstractControl {

    // Desired height in screen pixels of 1 unit in the inner space.
    private float targetHeight;

    public ConstantScreenSizeControl(float targetHeight) {
        this.targetHeight = targetHeight;
    }

    @Override
    protected void controlUpdate(float tpf) {
    }

    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
        Camera cam = vp.getCamera();

        // TODO this is a bit wrong, I think
        spatial.setLocalScale(
                spatial.getWorldTranslation().add(cam.getLocation()).length() * targetHeight / cam.getHeight());

        fixRefreshFlags();
    }

    private void fixRefreshFlags() {
        // force transforms to update below this node
        spatial.updateGeometricState();

        // force world bound to update
        Spatial rootNode = spatial;
        while (rootNode.getParent() != null) {
            rootNode = rootNode.getParent();
        }
        rootNode.getWorldBound();
    }
}
