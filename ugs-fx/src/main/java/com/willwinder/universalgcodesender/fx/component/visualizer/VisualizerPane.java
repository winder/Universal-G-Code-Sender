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

import com.willwinder.universalgcodesender.fx.component.designer.DesignContextMenu;
import com.willwinder.universalgcodesender.fx.component.designer.TextEditOverlay;
import com.willwinder.universalgcodesender.fx.component.designer.editor.DesignEditor;
import com.willwinder.universalgcodesender.fx.component.designer.render.DesignRenderable;
import com.willwinder.universalgcodesender.fx.component.designer.render.HandlesRenderable;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.CameraNavigationHandler;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.InputRouter;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.OrientationCubeHandler;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.ScrollInput;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.Machine;
import com.willwinder.universalgcodesender.fx.component.visualizer.overlay.OverlayPainter;
import com.willwinder.universalgcodesender.fx.component.visualizer.render.VulkanFrameRenderer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Scene;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Viewport;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.AxesRenderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.GcodeToolpathRenderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.GridRenderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.OrientationCubeRenderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.RulerRenderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.SceneGraphRenderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.ToolMarkerRenderable;
import com.willwinder.universalgcodesender.fx.model.WorkspaceBounds;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The visualizer drawn with Vulkan. Vulkan cannot render into a JavaFX node, so frames are
 * rendered to an offscreen image, copied back to host memory and presented through a
 * {@link PixelBuffer}. A canvas above the image carries the 2D overlays, and the JavaFX
 * toolbars sit on top of both.
 *
 * <p>Frames are rendered on demand: the {@link Scene}, the {@link Camera} and a resize all ask
 * for one through {@link #requestRender()}, and the pulse renders at most one frame.
 */
public class VisualizerPane extends Pane {
    private static final Logger LOGGER = Logger.getLogger(VisualizerPane.class.getName());
    private static final double ORIENTATION_CUBE_SIZE = 130;
    private static final double MARGIN = 2;
    /** A right button that travels further than this before release is a drag, not a click. */
    private static final double CONTEXT_MENU_DRAG_THRESHOLD_PX = 3;
    /** A right button held longer than this is a pan or rotate, even if the mouse has not moved yet. */
    private static final long CONTEXT_MENU_HOLD_MILLIS = 300;

    private final ImageView imageView = new ImageView();
    private final Canvas overlayCanvas = new Canvas();
    private final List<OverlayPainter> overlayPainters = new CopyOnWriteArrayList<>();
    private final InputRouter inputRouter = new InputRouter(this::setCursor);
    private final Runnable renderRequestListener = this::requestRender;
    private final ChangeListener<String> backgroundColorListener =
            (observable, oldValue, newValue) -> applyBackgroundColor();
    private final ChangeListener<Boolean> projectionListener =
            (observable, oldValue, newValue) -> applyProjection(newValue);
    private final ListChangeListener<Renderable> serviceRenderablesListener = this::onServiceRenderablesChanged;
    private final UGSEventListener eventListener = this::onEvent;

    private VulkanFrameRenderer renderer;
    private Scene scene;
    private Camera camera;
    private CameraAnimator cameraAnimator;
    private DesignEditor designEditor;
    private TextEditOverlay textEditOverlay;
    private ContextMenu designContextMenu;
    private double secondaryPressX;
    private double secondaryPressY;
    private long secondaryPressNanos;
    private AnimationTimer renderLoop;
    private PixelBuffer<ByteBuffer> pixelBuffer;
    private String unavailableMessage;
    // The id of the workspace the view was last fitted to, so a load only fits the view once.
    private UUID lastFittedWorkspaceId;

    /**
     * The buffer handed to JavaFX. Deliberately not the Vulkan mapped memory, whose lifetime is
     * tied to the offscreen images and would be freed on resize while the render thread is
     * still reading it.
     */
    private ByteBuffer presentedPixels;
    private boolean sceneChanged = true;

    /**
     * Wall time of the last rendered frame. Not averaged, since on-demand rendering can leave a
     * running average reflecting nothing but the startup frames.
     */
    private double lastFrameMillis;

    public VisualizerPane() {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/visualizer.css")).toExternalForm());
        imageView.setPreserveRatio(false);
        overlayCanvas.setMouseTransparent(true);
        getChildren().addAll(imageView, overlayCanvas);
        setFocusTraversable(true);

        try {
            renderer = new VulkanFrameRenderer();
        } catch (RuntimeException | LinkageError e) {
            LOGGER.log(Level.SEVERE, "Could not initialize the Vulkan visualizer", e);
            unavailableMessage = "Vulkan is not available: " + e.getMessage();
            paintOverlays();
            return;
        }

        camera = new Camera();
        camera.addChangeListener(renderRequestListener);
        cameraAnimator = new CameraAnimator(camera);
        applyProjection(VisualizerSettings.getInstance().useParallelCameraProperty().get());
        VisualizerSettings.getInstance().useParallelCameraProperty().addListener(projectionListener);

        scene = new Scene(renderer.context());
        scene.addRenderListener(renderRequestListener);
        RulerRenderable ruler = new RulerRenderable();
        OrientationCubeRenderable orientationCube = new OrientationCubeRenderable(MARGIN, MARGIN, ORIENTATION_CUBE_SIZE);
        scene.add(new GridRenderable());
        scene.add(new AxesRenderable());
        scene.add(ruler);
        scene.add(new DesignRenderable());
        scene.add(new GcodeToolpathRenderable());
        scene.add(new ToolMarkerRenderable());
        scene.add(new SceneGraphRenderable(new Machine()));
        scene.add(orientationCube);
        VisualizerService.getInstance().getRenderables().forEach(scene::add);
        VisualizerService.getInstance().getRenderables().addListener(serviceRenderablesListener);

        textEditOverlay = new TextEditOverlay(camera);
        designContextMenu = DesignContextMenu.create();
        designEditor = new DesignEditor(camera, this::requestRender, textEditOverlay::edit,
                (screenX, screenY) -> designContextMenu.show(this, screenX, screenY));
        scene.add(new HandlesRenderable(designEditor));

        inputRouter.addHandler(new OrientationCubeHandler(orientationCube, cameraAnimator::rotateTo));
        inputRouter.addHandler(designEditor);
        inputRouter.addHandler(CameraNavigationHandler.fromSettings(camera));
        overlayPainters.add(ruler);
        overlayPainters.add(orientationCube);
        overlayPainters.add(designEditor);
        overlayPainters.add(this::paintFrameStats);
        getChildren().add(textEditOverlay);
        VisualizerService.getInstance().setCenterOnBoundsHandler(this::centerOnWorkspace);
        LookupService.lookup(BackendAPI.class).addUGSEventListener(eventListener);

        addToolbars();
        applyBackgroundColor();
        VisualizerSettings.getInstance().colorBackgroundProperty().addListener(backgroundColorListener);
        registerInputHandlers();
        startRenderLoop();
    }

    /**
     * Releases the Vulkan device and the offscreen images. The component cannot be used
     * afterwards.
     */
    public void dispose() {
        if (renderLoop != null) {
            renderLoop.stop();
            renderLoop = null;
        }
        if (cameraAnimator != null) {
            cameraAnimator.stop();
        }
        if (designEditor != null) {
            designEditor.dispose();
        }
        LookupService.lookup(BackendAPI.class).removeUGSEventListener(eventListener);
        VisualizerService.getInstance().getRenderables().removeListener(serviceRenderablesListener);
        VisualizerService.getInstance().setCenterOnBoundsHandler(null);
        VisualizerSettings.getInstance().colorBackgroundProperty().removeListener(backgroundColorListener);
        VisualizerSettings.getInstance().useParallelCameraProperty().removeListener(projectionListener);
        if (scene != null) {
            scene.removeRenderListener(renderRequestListener);
            scene.clear();
            scene = null;
        }
        if (camera != null) {
            camera.removeChangeListener(renderRequestListener);
            camera = null;
        }
        if (renderer != null) {
            renderer.close();
            renderer = null;
        }
        imageView.setImage(null);
        pixelBuffer = null;
        presentedPixels = null;
    }

    public Scene getScene3D() {
        return scene;
    }

    public Camera getCamera() {
        return camera;
    }

    public InputRouter getInputRouter() {
        return inputRouter;
    }

    public void addOverlayPainter(OverlayPainter painter) {
        overlayPainters.add(painter);
        requestRender();
    }

    public void removeOverlayPainter(OverlayPainter painter) {
        overlayPainters.remove(painter);
        requestRender();
    }

    /**
     * Asks for a frame to be rendered on the next pulse. Cheap to call repeatedly.
     */
    public void requestRender() {
        sceneChanged = true;
    }

    /**
     * Turns to look straight down and fits the given workspace extent, animated.
     */
    public void centerOnWorkspace(WorkspaceBounds bounds) {
        if (cameraAnimator == null) {
            return;
        }
        cameraAnimator.frame(new Bounds3(bounds.minX(), bounds.minY(), 0, bounds.maxX(), bounds.maxY(), 0));
    }

    private void onServiceRenderablesChanged(ListChangeListener.Change<? extends Renderable> change) {
        while (change.next()) {
            change.getAddedSubList().forEach(scene::add);
            change.getRemoved().forEach(scene::remove);
        }
    }

    /**
     * Fits the view to the content the first time a workspace is loaded.
     */
    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent fileStateEvent
                && fileStateEvent.getFileState() == FileState.FILE_LOADED) {
            Platform.runLater(this::fitNewWorkspace);
        }
    }

    private void fitNewWorkspace() {
        if (cameraAnimator == null) {
            return;
        }
        WorkspaceManager.getInstance().getActiveWorkspace().ifPresent(workspace -> {
            if (workspace.getId().equals(lastFittedWorkspaceId)) {
                return;
            }
            lastFittedWorkspaceId = workspace.getId();
            centerOnWorkspace(workspace.getBounds().orElse(new WorkspaceBounds(0, 0, 100, 100)));
        });
    }

    private void addToolbars() {
        OrientationToolbar orientationToolbar = new OrientationToolbar();
        orientationToolbar.layoutXProperty().bind(orientationToolbar.widthProperty().divide(-2)
                .add(MARGIN + ORIENTATION_CUBE_SIZE / 2));
        orientationToolbar.setLayoutY(MARGIN + ORIENTATION_CUBE_SIZE + MARGIN);

        VisualizerToolbar toolbar = new VisualizerToolbar();
        toolbar.layoutXProperty().bind(widthProperty().subtract(toolbar.widthProperty()).subtract(10));
        toolbar.setLayoutY(9);

        ToolButton toolButton = new ToolButton();
        toolButton.setLayoutX(20);
        toolButton.layoutYProperty().bind(heightProperty().subtract(toolButton.heightProperty()).subtract(20));

        GcodeRegenerationIndicator regenerationIndicator = new GcodeRegenerationIndicator();
        regenerationIndicator.layoutXProperty().bind(toolButton.layoutXProperty().add(toolButton.widthProperty()).add(8));
        regenerationIndicator.layoutYProperty().bind(toolButton.layoutYProperty()
                .add(toolButton.heightProperty().subtract(regenerationIndicator.heightProperty()).divide(2)));

        getChildren().addAll(orientationToolbar, toolbar, toolButton, regenerationIndicator);
    }

    private void applyProjection(boolean parallel) {
        camera.projectionProperty().set(parallel ? Camera.Projection.ORTHOGRAPHIC : Camera.Projection.PERSPECTIVE);
    }

    private void applyBackgroundColor() {
        Color background = Color.web(VisualizerSettings.getInstance().colorBackgroundProperty().get());
        renderer.setBackgroundColor(
                (float) background.getRed(),
                (float) background.getGreen(),
                (float) background.getBlue());
        requestRender();
    }

    private void startRenderLoop() {
        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    renderPulse();
                } catch (RuntimeException e) {
                    LOGGER.log(Level.SEVERE, "The Vulkan visualizer stopped rendering", e);
                    unavailableMessage = "Vulkan rendering failed: " + e.getMessage();
                    paintOverlays();
                    stop();
                }
            }
        };
        renderLoop.start();
    }

    /**
     * Renders a frame when the scene changed or the component was resized. A resize is detected
     * here rather than signalled from the layout pass, because rendering repaints the overlay
     * canvas and so may trigger a layout of its own.
     */
    private void renderPulse() {
        if (renderer == null) {
            return;
        }
        Viewport viewport = currentViewport();
        if (getWidth() <= 0 || getHeight() <= 0) {
            return;
        }
        boolean resized = pixelBuffer == null
                || pixelBuffer.getWidth() != viewport.width()
                || pixelBuffer.getHeight() != viewport.height();
        if (!sceneChanged && !resized) {
            return;
        }
        sceneChanged = false;
        renderFrame(viewport);
        paintOverlays();
    }

    private void renderFrame(Viewport viewport) {
        long startedAt = System.nanoTime();
        ByteBuffer renderedPixels = renderer.renderFrame(scene, camera, viewport);
        lastFrameMillis = (System.nanoTime() - startedAt) / 1e6;

        int width = viewport.width();
        int height = viewport.height();
        if (pixelBuffer == null || pixelBuffer.getWidth() != width || pixelBuffer.getHeight() != height) {
            presentedPixels = ByteBuffer.allocateDirect(renderedPixels.capacity());
            pixelBuffer = new PixelBuffer<>(width, height, presentedPixels, PixelFormat.getByteBgraPreInstance());
            imageView.setImage(new WritableImage(pixelBuffer));
        }

        // Copying inside the callback is the one point where JavaFX guarantees the render
        // thread is not reading the buffer. It also keeps what JavaFX holds on to a buffer we
        // own, so resizing the offscreen images can never pull mapped memory out from under
        // the render thread.
        pixelBuffer.updateBuffer(buffer -> {
            presentedPixels.put(0, renderedPixels, 0, renderedPixels.capacity());
            return null;
        });
    }

    private void paintOverlays() {
        GraphicsContext graphics = overlayCanvas.getGraphicsContext2D();
        double width = overlayCanvas.getWidth();
        double height = overlayCanvas.getHeight();
        graphics.clearRect(0, 0, width, height);
        if (unavailableMessage != null) {
            graphics.setFill(Color.web("#404040"));
            graphics.setTextAlign(TextAlignment.LEFT);
            graphics.setTextBaseline(VPos.BOTTOM);
            graphics.fillText(unavailableMessage, 10, height - 10);
            return;
        }
        for (OverlayPainter painter : overlayPainters) {
            graphics.save();
            painter.paint(graphics, camera, width, height);
            graphics.restore();
        }
    }

    private void paintFrameStats(GraphicsContext graphics, Camera camera, double width, double height) {
        graphics.setFill(Color.web("#404040"));
        graphics.setFont(Font.font(11));
        graphics.setTextAlign(TextAlignment.RIGHT);
        graphics.setTextBaseline(VPos.TOP);
        // Under the visualizer toolbar, clear of the drawer buttons along the right edge.
        graphics.fillText("%s — %dx MSAA — %.2f ms/frame"
                        .formatted(renderer.deviceName(), renderer.sampleCount(), lastFrameMillis),
                width - 12, 52);
    }

    /**
     * A right button released quickly and where it was pressed is a click asking for the context
     * menu. One that moved, or was held down, was a pan or rotate and must not open it.
     */
    private boolean isContextMenuClick(MouseEvent event) {
        double movedPx = Math.hypot(event.getX() - secondaryPressX, event.getY() - secondaryPressY);
        long heldMillis = (System.nanoTime() - secondaryPressNanos) / 1_000_000;
        return movedPx <= CONTEXT_MENU_DRAG_THRESHOLD_PX && heldMillis <= CONTEXT_MENU_HOLD_MILLIS;
    }

    private void hideContextMenu() {
        if (designContextMenu != null && designContextMenu.isShowing()) {
            designContextMenu.hide();
        }
    }

    private void registerInputHandlers() {
        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            requestFocus();
            hideContextMenu();
            if (event.getButton() == MouseButton.SECONDARY) {
                secondaryPressX = event.getX();
                secondaryPressY = event.getY();
                secondaryPressNanos = System.nanoTime();
            }
            if (camera != null) {
                inputRouter.pressed(PointerEvent.of(event, camera));
            }
        });
        addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            if (camera != null) {
                inputRouter.dragged(PointerEvent.of(event, camera));
            }
        });
        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (camera == null) {
                return;
            }
            inputRouter.released(PointerEvent.of(event, camera));
            if (event.getButton() == MouseButton.SECONDARY
                    && isContextMenuClick(event)
                    && inputRouter.contextMenu(PointerEvent.of(event, camera))) {
                event.consume();
            }
        });
        addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
            if (camera != null) {
                inputRouter.moved(PointerEvent.of(event, camera));
            }
        });
        addEventHandler(ScrollEvent.SCROLL, event -> {
            hideContextMenu();
            if (camera != null && inputRouter.scrolled(new ScrollInput(event.getX(), event.getY(),
                    event.getDeltaX(), event.getDeltaY(),
                    event.isShiftDown(), event.isControlDown(), event.isAltDown(), event.isMetaDown()))) {
                event.consume();
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (inputRouter.keyPressed(event)) {
                event.consume();
            }
        });
        addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            if (inputRouter.keyReleased(event)) {
                event.consume();
            }
        });
    }

    private Viewport currentViewport() {
        double scale = outputScale();
        return new Viewport((int) Math.round(getWidth() * scale), (int) Math.round(getHeight() * scale), scale);
    }

    /**
     * The scale between logical JavaFX units and physical pixels, so the offscreen image is
     * rendered at the display's real resolution instead of being upscaled.
     */
    private double outputScale() {
        javafx.scene.Scene fxScene = getScene();
        if (fxScene == null) {
            return 1;
        }
        Window window = fxScene.getWindow();
        return window == null ? 1 : window.getOutputScaleX();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        imageView.setFitWidth(getWidth());
        imageView.setFitHeight(getHeight());
        overlayCanvas.setWidth(getWidth());
        overlayCanvas.setHeight(getHeight());
        if (camera != null) {
            camera.setViewport(currentViewport());
        }
    }
}
