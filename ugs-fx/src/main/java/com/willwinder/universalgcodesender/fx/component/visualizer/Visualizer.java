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
package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.fx.actions.CenterCameraAction;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.Machine;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.Axes;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.Grid;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.Model;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.Ruler;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.Tool;
import com.willwinder.universalgcodesender.fx.model.WorkspaceBounds;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.animation.KeyFrame;
import javafx.event.ActionEvent;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.DirectionalLight;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SpotLight;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Visualizer extends Pane {
    private final PerspectiveCamera perspectiveCamera;
    private final ParallelCamera parallelCamera;
    private Camera camera;
    private final Group worldGroup;
    private double mouseOldX;
    private double mouseOldY;
    private DragHandler activeDragHandler;
    private double dragStartX;
    private double dragStartY;

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(180, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(180, Rotate.Z_AXIS);

    private final Rotate orientationCubeRotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate orientationCubeRotateY = new Rotate(180, Rotate.Y_AXIS);
    private final Rotate orientationCubeRotateZ = new Rotate(180, Rotate.Z_AXIS);

    private final Translate translate = new Translate(0, 0, 0);
    private final Translate cameraTranslate = new Translate(0, 0, -500); // initial zoom
    private final SubScene subScene;
    private final Group root3D;

    // The id of the workspace the view was last auto-centered on, so a load only recenters once.
    private volatile UUID lastCenteredWorkspaceId;

    public Visualizer() {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/visualizer.css")).toExternalForm());

        // Rotate group contains 3D objects
        Machine machine = new Machine();
        worldGroup = new Group(machine);
        worldGroup.getTransforms().addAll(translate, rotateX, rotateY, rotateZ);

        // Lighting
        DirectionalLight light = new DirectionalLight(Color.WHITE);
        light.setDirection(new Point3D(1, -1, -1));
        light.getScope().addAll(machine);

        Point3D lightDirection = new Point3D(0.5, 0.7, 0);
        SpotLight spotLight = new SpotLight(Color.DARKGREY);
        spotLight.setTranslateX(-400);
        spotLight.setTranslateY(-1000);
        spotLight.setTranslateZ(-200);
        spotLight.setDirection(lightDirection);
        spotLight.getScope().addAll(machine);

        // Root group applies panning
        AmbientLight ambient = new AmbientLight(Color.rgb(255, 255, 255));
        root3D = new Group(worldGroup, ambient, light, spotLight);

        // Make the rotation pivot follow the current pan position
        updateRotationPivotFromPan();

        subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.fillProperty().bind(VisualizerSettings.getInstance().colorBackgroundProperty().map(Color::web));

        perspectiveCamera = createPerspectiveCamera();
        parallelCamera = createParallelCamera();
        applyCameraMode(VisualizerSettings.getInstance().useParallelCameraProperty().get());

        VisualizerSettings.getInstance().useParallelCameraProperty().addListener(
                (obs, oldVal, newVal) -> applyCameraMode(newVal)
        );

        setMouseInteraction();

        // Orientation controls: cube in the top-left corner with the projection toggle beneath it.
        OrientationCube orientationCube = new OrientationCube(110);
        orientationCube.setOnFaceClicked(this::rotateTo);
        orientationCube.setRotations(orientationCubeRotateX, orientationCubeRotateY, orientationCubeRotateZ);
        orientationCube.layoutXProperty().set(5);
        orientationCube.layoutYProperty().set(5);

        OrientationToolbar orientationToolbar = new OrientationToolbar();
        orientationToolbar.layoutXProperty().bind(orientationCube.layoutXProperty()
                .add(orientationCube.sizeProperty().divide(2))
                .subtract(orientationToolbar.widthProperty().divide(2)));
        orientationToolbar.layoutYProperty().bind(orientationCube.layoutYProperty()
                .add(orientationCube.sizeProperty()).add(5));

        // Visualizer features in the top-right corner.
        VisualizerToolbar toolbar = new VisualizerToolbar();
        toolbar.layoutXProperty().bind(widthProperty().subtract(toolbar.widthProperty()).subtract(10));
        toolbar.layoutYProperty().set(9);

        getChildren().addAll(subScene, orientationCube, orientationToolbar, toolbar);

        // Add new models added through the visualizer service
        VisualizerService.getInstance().getModels().addListener((ListChangeListener<Model>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    worldGroup.getChildren().addAll(change.getAddedSubList());
                    spotLight.getScope().addAll(change.getAddedSubList().stream().filter(Model::useLighting).toList());
                }
                if (change.wasRemoved()) {
                    worldGroup.getChildren().removeAll(change.getRemoved());
                    spotLight.getScope().removeAll(change.getRemoved());
                }
            }
        });

        VisualizerService.getInstance().setCenterOnBoundsHandler(this::centerOnWorkspace);

        VisualizerService.getInstance().addModel(new Axes());
        VisualizerService.getInstance().addModel(new Grid());
        VisualizerService.getInstance().addModel(new WorkspaceScene());
        VisualizerService.getInstance().addModel(new Ruler());
        VisualizerService.getInstance().addModel(new Tool());

        // Fit the view to the content the first time a workspace is loaded.
        CenterCameraAction centerCameraAction = new CenterCameraAction();
        LookupService.lookup(BackendAPI.class).addUGSEventListener(event -> {
            if (event instanceof FileStateEvent fileStateEvent
                    && fileStateEvent.getFileState() == FileState.FILE_LOADED) {
                WorkspaceManager.getInstance().getActiveWorkspace().ifPresent(workspace -> {
                    if (!workspace.getId().equals(lastCenteredWorkspaceId)) {
                        lastCenteredWorkspaceId = workspace.getId();
                        centerCameraAction.handleAction(new ActionEvent());
                    }
                });
            }
        });
    }

    /**
     * Recenters and zooms the view so the given workspace bounds are framed.
     */
    public void centerOnWorkspace(WorkspaceBounds bounds) {
        double centerX = (bounds.minX() + bounds.maxX()) / 2.0;
        double centerY = (bounds.minY() + bounds.maxY()) / 2.0;
        double boundsWidth = Math.max(1e-3, bounds.maxX() - bounds.minX());
        double boundsHeight = Math.max(1e-3, bounds.maxY() - bounds.minY());

        // With translate applied outside the rotations and the pivot kept at (-tx, ty, 0), a
        // designer point (px, py) lands at screen (px + tx, 3*ty - py) in the top-down view.
        // Solving for the bounds center at the screen center (0, 0) gives:
        double targetTx = -centerX;
        double targetTy = centerY / 3.0;

        double viewportWidth = Math.max(1.0, subScene.getWidth());
        double viewportHeight = Math.max(1.0, subScene.getHeight());
        double aspect = viewportWidth / viewportHeight;
        double margin = 1.4;

        // Pin the rotation pivot to the final value now so the view never jumps when it settles.
        setRotationPivot(-targetTx, targetTy, 0);

        List<KeyValue> start = new ArrayList<>(List.of(
                new KeyValue(rotateX.angleProperty(), rotateX.getAngle()),
                new KeyValue(rotateY.angleProperty(), rotateY.getAngle()),
                new KeyValue(rotateZ.angleProperty(), rotateZ.getAngle()),
                new KeyValue(orientationCubeRotateX.angleProperty(), orientationCubeRotateX.getAngle()),
                new KeyValue(orientationCubeRotateY.angleProperty(), orientationCubeRotateY.getAngle()),
                new KeyValue(orientationCubeRotateZ.angleProperty(), orientationCubeRotateZ.getAngle()),
                new KeyValue(translate.xProperty(), translate.getX()),
                new KeyValue(translate.yProperty(), translate.getY())));

        List<KeyValue> end = new ArrayList<>(List.of(
                new KeyValue(rotateX.angleProperty(), 0),
                new KeyValue(rotateY.angleProperty(), 180),
                new KeyValue(rotateZ.angleProperty(), 180),
                new KeyValue(orientationCubeRotateX.angleProperty(), 0),
                new KeyValue(orientationCubeRotateY.angleProperty(), 180),
                new KeyValue(orientationCubeRotateZ.angleProperty(), 180),
                new KeyValue(translate.xProperty(), targetTx),
                new KeyValue(translate.yProperty(), targetTy)));

        if (camera instanceof PerspectiveCamera pc) {
            double neededWorldHeight = Math.max(boundsHeight, boundsWidth / aspect) * margin;
            double distance = neededWorldHeight / (2.0 * Math.tan(Math.toRadians(pc.getFieldOfView()) / 2.0));
            start.add(new KeyValue(cameraTranslate.zProperty(), cameraTranslate.getZ()));
            end.add(new KeyValue(cameraTranslate.zProperty(), -distance));
        } else {
            double fitScale = Math.min(viewportHeight / (boundsHeight * margin), viewportWidth / (boundsWidth * margin));
            start.add(new KeyValue(root3D.scaleXProperty(), root3D.getScaleX()));
            start.add(new KeyValue(root3D.scaleYProperty(), root3D.getScaleY()));
            end.add(new KeyValue(root3D.scaleXProperty(), fitScale));
            end.add(new KeyValue(root3D.scaleYProperty(), fitScale));
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, start.toArray(new KeyValue[0])),
                new KeyFrame(Duration.seconds(1), end.toArray(new KeyValue[0])));
        timeline.setOnFinished(e -> VisualizerService.getInstance().onZoomChange(getCurrentScale()));
        timeline.play();
    }

    private void setRotationPivot(double px, double py, double pz) {
        for (Rotate rotate : List.of(rotateX, rotateY, rotateZ)) {
            rotate.setPivotX(px);
            rotate.setPivotY(py);
            rotate.setPivotZ(pz);
        }
    }

    private void rotateTo(OrientationCubeFace face) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(rotateX.angleProperty(), rotateX.getAngle()),
                        new KeyValue(rotateY.angleProperty(), rotateY.getAngle()),
                        new KeyValue(rotateZ.angleProperty(), rotateZ.getAngle()),
                        new KeyValue(orientationCubeRotateX.angleProperty(), orientationCubeRotateX.getAngle()),
                        new KeyValue(orientationCubeRotateY.angleProperty(), orientationCubeRotateY.getAngle()),
                        new KeyValue(orientationCubeRotateZ.angleProperty(), orientationCubeRotateZ.getAngle())
                ),
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(rotateX.angleProperty(), face.getRotation().getX()),
                        new KeyValue(rotateY.angleProperty(), face.getRotation().getY()),
                        new KeyValue(rotateZ.angleProperty(), face.getRotation().getZ()),
                        new KeyValue(orientationCubeRotateX.angleProperty(), face.getRotation().getX()),
                        new KeyValue(orientationCubeRotateY.angleProperty(), face.getRotation().getY()),
                        new KeyValue(orientationCubeRotateZ.angleProperty(), face.getRotation().getZ())
                )
        );
        timeline.play();
    }

    private static MouseButton parseMouseButton(String value, MouseButton fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return MouseButton.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean isModifierDown(MouseEvent event, VisualizerSettings.ModifierKey modifier) {
        if (modifier == null || modifier == VisualizerSettings.ModifierKey.NONE) {
            return !event.isShiftDown() && !event.isControlDown()
                    && !event.isAltDown() && !event.isMetaDown();
        }

        return switch (modifier) {
            case SHIFT -> event.isShiftDown();
            case CTRL -> event.isControlDown();
            case ALT -> event.isAltDown();
            case META -> event.isMetaDown();
            default -> false;
        };
    }

    private static boolean isButtonDown(MouseEvent event, MouseButton button) {
        if (button == null) return false;
        return switch (button) {
            case PRIMARY -> event.isPrimaryButtonDown();
            case MIDDLE -> event.isMiddleButtonDown();
            case SECONDARY -> event.isSecondaryButtonDown();
            default -> false;
        };
    }

    private static MouseButton getPrimaryButton() {
        return parseMouseButton(
                VisualizerSettings.getInstance().primaryMouseButtonProperty().getValue(),
                MouseButton.PRIMARY
        );
    }

    private static VisualizerSettings.ModifierKey getPrimaryModifier() {
        return VisualizerSettings.ModifierKey.fromString(
                VisualizerSettings.getInstance().primaryModifierKeyProperty().getValue(),
                VisualizerSettings.ModifierKey.NONE
        );
    }

    /**
     * Whether a mouse press triggers the designer primary action (click/draw),
     * based on the configured primary button + modifier.
     */
    private static boolean isPrimaryActivation(MouseEvent event) {
        return event.getButton() == getPrimaryButton() && isModifierDown(event, getPrimaryModifier());
    }

    /**
     * Whether the designer primary button is still held during a drag,
     * based on the configured primary button + modifier.
     */
    private static boolean isPrimaryDragActive(MouseEvent event) {
        return isButtonDown(event, getPrimaryButton()) && isModifierDown(event, getPrimaryModifier());
    }

    private void setMouseInteraction() {
        // Handle mouse press event to store the initial position
        subScene.setOnMousePressed(event -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        // Global pick dispatcher: for a primary-button press, walk the picked node's
        // userData chain. A PickHandler means we hit a selectable entity — invoke it.
        // A DragHandler-only userData means we hit a control handle — leave it to the
        // worldGroup drag handler below. No handler found means an empty-space click;
        // fire a background click so listeners (e.g. the designer) can clear selection.
        subScene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isPrimaryActivation(event)) return;

            // When a designer drawing tool is active it takes priority over picking:
            // begin a draw gesture at the designer point under the cursor.
            Point2D designerPoint = toDesignerPoint(event.getX(), event.getY());
            if (designerPoint != null) {
                DragHandler drawHandler = VisualizerService.getInstance()
                        .beginDrawGesture(designerPoint.getX(), designerPoint.getY());
                if (drawHandler != null) {
                    activeDragHandler = drawHandler;
                    dragStartX = designerPoint.getX();
                    dragStartY = designerPoint.getY();
                    drawHandler.onDragStart(dragStartX, dragStartY);
                    event.consume();
                    return;
                }
            }

            Node hit = event.getPickResult().getIntersectedNode();
            while (hit != null) {
                Object userData = hit.getUserData();
                if (userData instanceof PickHandler handler) {
                    handler.onPicked(event.isShiftDown());
                    return;
                }
                if (userData instanceof DragHandler) {
                    return;
                }
                hit = hit.getParent();
            }

            VisualizerService.getInstance().fireBackgroundClick(event);
        });

        // Handle mouse dragged event to implement panning, rotating, and control dragging
        subScene.setOnMouseDragged((MouseEvent event) -> {
            if (activeDragHandler != null && isPrimaryDragActive(event)) {
                Point2D pt = toDesignerPoint(event.getX(), event.getY());
                if (pt != null) {
                    activeDragHandler.onDrag(dragStartX, dragStartY, pt.getX(), pt.getY());
                }
                mouseOldX = event.getSceneX();
                mouseOldY = event.getSceneY();
                return;
            }

            double dx = event.getSceneX() - mouseOldX;
            double dy = event.getSceneY() - mouseOldY;

            VisualizerSettings settings = VisualizerSettings.getInstance();

            MouseButton panButton = parseMouseButton(settings.panMouseButtonProperty().getValue(), MouseButton.SECONDARY);
            VisualizerSettings.ModifierKey panModifier = VisualizerSettings.ModifierKey.fromString(
                    settings.panModifierKeyProperty().getValue(),
                    VisualizerSettings.ModifierKey.NONE
            );

            MouseButton rotateButton = parseMouseButton(settings.rotateMouseButtonProperty().getValue(), MouseButton.SECONDARY);
            VisualizerSettings.ModifierKey rotateModifier = VisualizerSettings.ModifierKey.fromString(
                    settings.rotateModifierKeyProperty().getValue(),
                    VisualizerSettings.ModifierKey.NONE
            );

            boolean doPan = isButtonDown(event, panButton) && isModifierDown(event, panModifier);
            boolean doRotate = isButtonDown(event, rotateButton) && isModifierDown(event, rotateModifier);

            if (doPan) {
                // Pan (translate) the 3D scene
                double worldPerPixel = getWorldUnitsPerPixel();
                translate.setX(translate.getX() + (dx * worldPerPixel));
                translate.setY(translate.getY() + (dy * worldPerPixel));

                // Keep rotation centered on the screen center after panning
                updateRotationPivotFromPan();
            } else if (doRotate) {
                rotateX.setAngle(rotateX.getAngle() + dy * 0.5);
                rotateZ.setAngle(rotateZ.getAngle() + dx * 0.5);
                orientationCubeRotateX.setAngle(orientationCubeRotateX.getAngle() + dy * 0.5);
                orientationCubeRotateZ.setAngle(orientationCubeRotateZ.getAngle() + dx * 0.5);
            }

            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseReleased(event -> {
            DragHandler handler = activeDragHandler;
            activeDragHandler = null;
            if (handler != null) {
                Point2D pt = toDesignerPoint(event.getX(), event.getY());
                if (pt != null) {
                    handler.onDragEnd(dragStartX, dragStartY, pt.getX(), pt.getY());
                }
            }
        });

        // Detect drag start on control handles (DragHandler in userData) inside the SubScene.
        worldGroup.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isPrimaryActivation(event)) return;
            Node intersected = event.getPickResult().getIntersectedNode();
            Node current = intersected;
            while (current != null) {
                if (current.getUserData() instanceof DragHandler dh) {
                    Point3D localPt = event.getPickResult().getIntersectedPoint();
                    Point3D designerPt = worldGroup.sceneToLocal(intersected.localToScene(localPt));
                    activeDragHandler = dh;
                    dragStartX = designerPt.getX();
                    dragStartY = designerPt.getY();
                    dh.onDragStart(dragStartX, dragStartY);
                    event.consume();
                    return;
                }
                current = current.getParent();
            }
        });

        // Zoom with mouse scroll
        subScene.setOnScroll(event -> {
            boolean invert = VisualizerSettings.getInstance().invertZoomProperty().get();
            double currentScale = getCurrentScale();
            double delta = (invert ? -event.getDeltaY() : event.getDeltaY()) / currentScale;

            if (camera instanceof ParallelCamera) {
                double scale = root3D.getScaleY() + (delta / 200f);
                root3D.setScaleX(scale);
                root3D.setScaleY(scale);
            } else {
                cameraTranslate.setZ(cameraTranslate.getZ() + delta);
            }

            VisualizerService.getInstance().onZoomChange(getCurrentScale());
        });
    }

    /**
     * Converts a 1-pixel mouse movement into world units, based on the current projection.
     */
    private double getWorldUnitsPerPixel() {
        double viewportHeight = Math.max(1.0, subScene.getHeight());

        if (camera instanceof PerspectiveCamera pc) {
            // Camera looks down -Z, and we keep cameraTranslate Z negative (e.g. -500)
            double distance = Math.max(1e-6, -cameraTranslate.getZ());

            // JavaFX PerspectiveCamera uses a vertical field-of-view by default.
            double fovRad = Math.toRadians(pc.getFieldOfView());
            double visibleWorldHeight = 2.0 * distance * Math.tan(fovRad * 0.5);

            return visibleWorldHeight / viewportHeight;
        }

        double scale = Math.max(1e-6, root3D.getScaleY());
        return 1.0 / scale;
    }

    private double getCurrentScale() {
        double scale = root3D.getScaleX();
        if (camera instanceof PerspectiveCamera) {
            double baseDistance = 500.0;
            double currentDistance = Math.max(1e-6, -cameraTranslate.getZ());
            scale = baseDistance / currentDistance;
        } else {
            scale = scale / 8;
        }

        return scale;
    }

    private PerspectiveCamera createPerspectiveCamera() {
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(2);
        camera.setFarClip(6000);
        camera.getTransforms().add(cameraTranslate);
        return camera;
    }

    private ParallelCamera createParallelCamera() {
        ParallelCamera camera = new ParallelCamera();
        Translate cameraOffset = new Translate();
        cameraOffset.xProperty().bind(widthProperty().divide(-4));
        cameraOffset.yProperty().bind(heightProperty().divide(1.6));
        camera.getTransforms().add(cameraOffset);
        return camera;
    }

    private void applyCameraMode(boolean useParallel) {
        if (useParallel) {
            camera = parallelCamera;
            root3D.setScaleX(4);
            root3D.setScaleY(4);
        } else {
            camera = perspectiveCamera;
            root3D.setScaleX(1);
            root3D.setScaleY(1);
        }
        VisualizerService.getInstance().onZoomChange(getCurrentScale());
        subScene.setCamera(camera);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        // Ensure SubScene resizes with the parent node
        subScene.setWidth(getWidth());
        subScene.setHeight(getHeight());
    }

    /**
     * Converts a SubScene pixel position to designer (worldGroup local) coordinates
     * by intersecting the camera ray with the Z=0 plane in worldGroup local space.
     */
    private Point2D toDesignerPoint(double pixelX, double pixelY) {
        if (!(camera instanceof PerspectiveCamera pc)) return null;

        double W = subScene.getWidth();
        double H = subScene.getHeight();
        double camZ = cameraTranslate.getZ();

        double tanHalf = Math.tan(Math.toRadians(pc.getFieldOfView()) / 2.0);
        double ndcX = (pixelX - W / 2.0) / (H / 2.0);
        double ndcY = (pixelY - H / 2.0) / (H / 2.0);

        // Ray origin and a second point along the ray, both in root3D (parent of worldGroup) space
        Point3D originRoot = new Point3D(0, 0, camZ);
        Point3D endRoot = new Point3D(ndcX * tanHalf, ndcY * tanHalf, camZ + 1.0);

        // Transform to worldGroup local (designer) space using the inverse of worldGroup's transform
        Point3D localOrigin = worldGroup.parentToLocal(originRoot);
        Point3D localEnd = worldGroup.parentToLocal(endRoot);
        Point3D localDir = localEnd.subtract(localOrigin);

        if (Math.abs(localDir.getZ()) < 1e-9) return null;
        double t = -localOrigin.getZ() / localDir.getZ();
        return new Point2D(
                localOrigin.getX() + t * localDir.getX(),
                localOrigin.getY() + t * localDir.getY()
        );
    }

    private void updateRotationPivotFromPan() {
        double px = -translate.getX();
        double py = translate.getY();
        double pz = -translate.getZ();

        rotateX.setPivotX(px);
        rotateX.setPivotY(py);
        rotateX.setPivotZ(pz);

        rotateY.setPivotX(px);
        rotateY.setPivotY(py);
        rotateY.setPivotZ(pz);

        rotateZ.setPivotX(px);
        rotateZ.setPivotY(py);
        rotateZ.setPivotZ(pz);
    }
}
