package com.willwinder.ugs.nbp.visualizer3;

import com.jme3.asset.AssetManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseAxisTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;

// TODO does JME3 also prefer an OrbitCameraState?
/**
 * A CAD-style orbiting camera using a right-handed Z-up coordinate system. This
 * camera constrains the orientation of the "up" axis so that it's easy to
 * return to the home position.
 */
public class OrbitCamera
        implements ActionListener, AnalogListener {

    public final static String INPUT_TOGGLE_ROTATE = "OrbitCameraToggleRotate";
    public final static String INPUT_TOGGLE_MOTION = "OrbitCameraToggleMotion";
    public final static String INPUT_MOUSE_DOWN = "OrbitCameraMouseDown";
    public final static String INPUT_MOUSE_UP = "OrbitCameraMouseUp";
    public final static String INPUT_MOUSE_LEFT = "OrbitCameraMouseLeft";
    public final static String INPUT_MOUSE_RIGHT = "OrbitCameraMouseRight";
    public final static String INPUT_ZOOM_IN = "OrbitCameraZoomIn";
    public final static String INPUT_ZOOM_OUT = "OrbitCameraZoomOut";
    public final static String INPUT_PAN_DOWN = "OrbitCameraPanDown";
    public final static String INPUT_PAN_UP = "OrbitCameraPanUp";
    public final static String INPUT_PAN_LEFT = "OrbitCameraPanLeft";
    public final static String INPUT_PAN_RIGHT = "OrbitCameraPanRight";

    Camera cam;
    private InputManager inputManager;

    /**
     * True if the mouse is currently controlling camera movement
     */
    boolean canMove;
    /**
     * True the current camera movement is a rotational move.
     */
    boolean moveIsRotation;

    // TODO only set a focal point while rotating? and set the focal point based on
    // the mouse pointer?
    /**
     * The point which the camera is focused on and will rotate around.
     */
    Vector3f focalPoint;
    /**
     * Angle formed by the Z axis and the projection of the line from the focal
     * point to the camera plane intersection point.
     */
    float theta;
    /**
     * Angle formed by the XY plane and the line from the focal point to the
     * camera plane intersection point.
     */
    float azimuth;
    /**
     * Distance from the camera to the focal point.
     */
    float distance;
    /**
     * An offset of the camera location on the camera plane. Without this
     * offset, the camera location with be determined by `focalPoint`, `theta`,
     * `azimuth`, and `distance`.
     */
    Vector2f cameraPlaneOffset;

    public OrbitCamera(AssetManager assetManager, Camera cam, InputManager inputManager) {
        this.cam = cam;
        this.registerWithInput(inputManager);

        this.setFocalPoint(Vector3f.ZERO);
    }

    // recalculate `azimuth`, `elevation`, `distance`, and `cameraPlaneOffset`,
    // given the new `focalPoint` and `cam`.
    public void setFocalPoint(Vector3f focalPoint) {
        this.focalPoint = focalPoint;

        boolean resetCameraOnNewFocalPoint = true;

        if (resetCameraOnNewFocalPoint) {
            this.cameraPlaneOffset = Vector2f.ZERO;

            // Orient above the focal point with the X mostly pointing to the right and Y
            // pointing towards the back.
            this.theta = 1.5f * FastMath.PI;
            this.azimuth = FastMath.QUARTER_PI;

            // TODO scale to fit model
            this.distance = 10.0f;

            updateCamera();
        } else {
            // Project the focal point onto the camera plane perpendicular to the camera
            // direction.
            Vector3f camLocation = cam.getLocation();
            Vector3f camDirection = cam.getDirection();
            Vector3f camToFocal = focalPoint.subtract(camLocation);
            float distanceFromCameraPlaneToFocal = camToFocal.dot(camDirection);
            Vector3f focalPointProjectedOnCameraPlane = focalPoint
                    .subtract(camDirection.mult(distanceFromCameraPlaneToFocal));

            Vector3f focalPointToCameraPlane = focalPointProjectedOnCameraPlane.subtract(focalPoint);

            this.theta = FastMath.atan2(focalPointToCameraPlane.getY(), focalPointToCameraPlane.getX());

            this.azimuth = FastMath.atan2(
                    focalPointToCameraPlane.getZ(),
                    new Vector2f(focalPointToCameraPlane.getX(), focalPointToCameraPlane.getY()).length());

            // We already calculated the distance.
            this.distance = distanceFromCameraPlaneToFocal;

            // The camera plane offset is calculated by transforming camera plane
            // intersection point into the camera's coordinate system.
            Vector3f cameraLocationToIntersection = focalPointProjectedOnCameraPlane.subtract(camLocation);
            this.cameraPlaneOffset = new Vector2f(
                    this.cam.getLeft().dot(cameraLocationToIntersection),
                    this.cam.getUp().dot(cameraLocationToIntersection));

            // TODO can changing the focal point require a camera move? can we represent any
            // Z-up view with our degrees of freedom? We need to allow negative distance to
            // support this?
            updateCamera();
        }
    }

    private void updateCamera() {
        var elevation = FastMath.sin(this.azimuth) * this.distance;
        var l = FastMath.cos(this.azimuth) * this.distance;
        Vector3f focalPointToCameraPlane = new Vector3f(
                (FastMath.cos(this.theta)) * l,
                (FastMath.sin(this.theta)) * l,
                elevation);
        var focalPointProjectedOnCameraPlane = this.focalPoint.add(focalPointToCameraPlane);

        this.cam.lookAtDirection(focalPointToCameraPlane.negate(), Vector3f.UNIT_Z);

        Vector3f movementOnCameraPlane = this.cam.getLeft().mult(this.cameraPlaneOffset.getX())
                .add(this.cam.getUp().mult(this.cameraPlaneOffset.getY()));

        this.cam.setLocation(focalPointProjectedOnCameraPlane.add(movementOnCameraPlane));

        // TODO handle ortho perspctive zoom, which is a widening of the frustrum
        // this.cam.setParallelProjection(true);
    }

    @Override
    public void onAnalog(String name, float value, float tpf) {
        if (canMove) {
            float dx = 0;
            if (name.equals(INPUT_MOUSE_LEFT)) {
                dx = -value;
            } else if (name.equals(INPUT_MOUSE_RIGHT)) {
                dx = value;
            }
            float dy = 0;
            if (name.equals(INPUT_MOUSE_UP)) {
                dy = value;
            } else if (name.equals(INPUT_MOUSE_DOWN)) {
                dy = -value;
            }

            if (moveIsRotation) {
                // constrained rotation that fixes the "up" axis

                // TODO rotate about the projected mouse point?
                this.theta -= dx * 5.0f;
                this.theta = this.theta % FastMath.TWO_PI;

                this.azimuth += dy * 5.0f;
                this.azimuth = FastMath.clamp(this.azimuth, -FastMath.HALF_PI + 0.001f, FastMath.HALF_PI - 0.001f);

                updateCamera();
            } else {
                // move on the plane perpendicular to the camera facing direction

                // Pick a magic number makes it feel like we're dragging the model directly.
                // This can probably be done with actually mapping a projected point on the
                // model to the screen. I'm not sure how to do this with AnalogListener and the
                // coordinate spaces it operates in.
                float panScale = 8495.f;
                panScale *= distance / 10.0f;
                Vector2f movement = new Vector2f(dx, dy)
                        .divide(this.cam.getHeight())
                        .mult(panScale);
                this.cameraPlaneOffset.set(this.cameraPlaneOffset.add(movement));
                updateCamera();
            }
        }

        // TODO improve zoom
        if (name.equals(INPUT_ZOOM_IN)) {
            this.distance -= value;
            // this.distance *= 1.0f / 1.1f;
            this.distance = FastMath.clamp(this.distance, 0.001f, 10000.0f);
            updateCamera();
        } else if (name.equals(INPUT_ZOOM_OUT)) {
            this.distance += value;
            // this.distance *= 1.1f;
            this.distance = FastMath.clamp(this.distance, 0.001f, 10000.0f);
            updateCamera();
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals(INPUT_TOGGLE_ROTATE)) {
            moveIsRotation = isPressed;
        }

        if (name.equals(INPUT_TOGGLE_MOTION)) {
            canMove = isPressed;
        }

        if (canMove && moveIsRotation) {
            inputManager.setCursorVisible(false);
        } else {
            inputManager.setCursorVisible(true);
        }

        if (isPressed) {
            float dx = 0;
            if (name.equals(INPUT_PAN_LEFT)) {
                dx = -1.0f;
            } else if (name.equals(INPUT_PAN_RIGHT)) {
                dx = 1.0f;
            }
            float dy = 0;
            if (name.equals(INPUT_PAN_UP)) {
                dy = 1.0f;
            } else if (name.equals(INPUT_PAN_DOWN)) {
                dy = -1.0f;
            }

            var cameraDirection = this.cam.getDirection();
            // TODO move camera based on cam direction on XY plane
        }

    }

    public final void registerWithInput(InputManager inputManager) {
        this.inputManager = inputManager;

        inputManager.addMapping(INPUT_TOGGLE_ROTATE,
                new KeyTrigger(KeyInput.KEY_LSHIFT));
        inputManager.addMapping(INPUT_TOGGLE_ROTATE,
                new KeyTrigger(KeyInput.KEY_RSHIFT));

        inputManager.addMapping(INPUT_TOGGLE_MOTION,
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        inputManager.addMapping(INPUT_MOUSE_LEFT,
                new MouseAxisTrigger(MouseInput.AXIS_X, true));
        inputManager.addMapping(INPUT_MOUSE_RIGHT,
                new MouseAxisTrigger(MouseInput.AXIS_X, false));

        inputManager.addMapping(INPUT_MOUSE_UP,
                new MouseAxisTrigger(MouseInput.AXIS_Y, true));
        inputManager.addMapping(INPUT_MOUSE_DOWN,
                new MouseAxisTrigger(MouseInput.AXIS_Y, false));

        inputManager.addMapping(INPUT_ZOOM_IN,
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
        inputManager.addMapping(INPUT_ZOOM_OUT,
                new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));

        inputManager.addMapping(INPUT_PAN_DOWN, new KeyTrigger(KeyInput.KEY_DOWN));
        inputManager.addMapping(INPUT_PAN_UP, new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping(INPUT_PAN_LEFT, new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping(INPUT_PAN_RIGHT, new KeyTrigger(KeyInput.KEY_RIGHT));

        String[] inputs = {
            INPUT_TOGGLE_ROTATE,
            INPUT_TOGGLE_MOTION,
            INPUT_MOUSE_LEFT,
            INPUT_MOUSE_RIGHT,
            INPUT_MOUSE_UP,
            INPUT_MOUSE_DOWN,
            INPUT_ZOOM_IN,
            INPUT_ZOOM_OUT,
            INPUT_PAN_DOWN,
            INPUT_PAN_UP,
            INPUT_PAN_LEFT,
            INPUT_PAN_RIGHT,};
        inputManager.addListener(this, inputs);
    }

    public void cleanupWithInput(InputManager mgr) {
        mgr.deleteMapping(INPUT_TOGGLE_ROTATE);
        mgr.deleteMapping(INPUT_TOGGLE_MOTION);
        mgr.deleteMapping(INPUT_MOUSE_LEFT);
        mgr.deleteMapping(INPUT_MOUSE_RIGHT);
        mgr.deleteMapping(INPUT_MOUSE_DOWN);
        mgr.deleteMapping(INPUT_MOUSE_UP);
        mgr.deleteMapping(INPUT_ZOOM_IN);
        mgr.deleteMapping(INPUT_ZOOM_OUT);
        mgr.deleteMapping(INPUT_PAN_DOWN);
        mgr.deleteMapping(INPUT_PAN_UP);
        mgr.deleteMapping(INPUT_PAN_LEFT);
        mgr.deleteMapping(INPUT_PAN_RIGHT);
        mgr.removeListener(this);
    }
}
