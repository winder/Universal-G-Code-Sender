package com.willwinder.ugs.nbp.visualizer3;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.post.SceneProcessor;
import com.jme3.profile.AppProfiler;
import com.jme3.renderer.Camera;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.texture.FrameBuffer;

/**
 * Makes the orientation cube camera match the orientation of the main camera.
 */
public class OrientationCubeCameraProcessor implements SceneProcessor {

    private Camera rootCamera;
    private Camera orientationCubeCam;
    private Node orientationCubeNode;

    public OrientationCubeCameraProcessor(Camera rootCamera, Camera orientationCubeCam, Node orientationCubeNode) {
        this.rootCamera = rootCamera;
        this.orientationCubeCam = orientationCubeCam;
        this.orientationCubeNode = orientationCubeNode;

        orientationCubeCam.setFov(30.0f); // TODO pick a better value?
    }

    @Override
    public void initialize(RenderManager rm, ViewPort vp) {
    }

    @Override
    public void reshape(ViewPort vp, int w, int h) {
        Vector2f orientationCubeViewPortSize = new Vector2f(150.0f, 150.0f);
        // Top-left alignment of uniform size
        orientationCubeCam
                .setViewPort(
                        0.0f,
                        orientationCubeViewPortSize.getX() / w,
                        1.0f - orientationCubeViewPortSize.getY() / h,
                        1.0f);
        orientationCubeCam.resize(w, h, true);
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public void preFrame(float tpf) {
        // These are called for us on the rootNode in `SimpleApplication`, but we need
        // to call it ourselves for our own node roots.
        orientationCubeNode.updateLogicalState(tpf);
        orientationCubeNode.updateGeometricState();

        // Make the orientation cube's rotation match the main camera
        orientationCubeCam.setLocation(Vector3f.ZERO.subtract(rootCamera.getDirection().mult(5.5f)));
        orientationCubeCam.lookAt(Vector3f.ZERO, Vector3f.UNIT_Z);
    }

    @Override
    public void postQueue(RenderQueue rq) {
    }

    @Override
    public void postFrame(FrameBuffer out) {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void setProfiler(AppProfiler profiler) {
    }
}
