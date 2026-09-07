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
package com.willwinder.universalgcodesender.fx.component.designer.render;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityListener;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import com.willwinder.ugs.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.gui.TabShapes;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerEventType;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.ControllerListener;
import com.willwinder.ugs.designer.logic.SettingsListener;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.LineMeshBuilder;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Mat4;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Scene;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneLayer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.TextureHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Draws the design of the active {@code .ugsd} workspace: every closed shape filled with the
 * shape background colour, which may be translucent, outlines for all, dashed outlines for
 * shapes that are not cut, and the markers for tabs, surfacing direction and center drilling. Meshes are built in each entity's own
 * coordinates and placed with its transform, so moving, resizing or rotating an entity does not
 * rebuild anything.
 *
 * <p>Design changes also mark the workspace dirty, which is what hides the stale toolpath until
 * the design is saved and its G-code regenerated.
 */
public final class DesignRenderable implements Renderable {
    private static final double FILL_Z = 0.2;
    private static final double OUTLINE_Z = 0.21;
    /**
     * The dash length on screen; the world length follows the zoom in steps of two.
     */
    private static final double DASH_PIXELS = 2;
    private static final float OUTLINE_WIDTH_PX = 1.5f;
    private static final float SELECTION_WIDTH_PX = 2.5f;
    private static final float TAB_WIDTH_PX = 2;
    private static final int MAX_TEXTURE_SIZE = 2048;
    private static final Color HINT_COLOR = Color.rgb(100, 100, 100);
    private static final float[] HINT_RGBA = toRgba(HINT_COLOR);
    private static final float[] SELECTION_RGBA = toRgba(Color.rgb(122, 161, 228));
    private static final float[] TAB_RGBA = toRgba(Colors.ORANGE);
    private static final Set<EventType> DESIGN_CHANGE_EVENTS = EnumSet.of(
            EventType.MOVED, EventType.RESIZED, EventType.ROTATED, EventType.PATH_CHANGED,
            EventType.CHILD_ADDED, EventType.CHILDREN_ADDED, EventType.CHILD_REMOVED,
            EventType.CHILDREN_REMOVED, EventType.SETTINGS_CHANGED);

    /**
     * The meshes of one entity, in its own coordinates. Reused for as long as the relative shape
     * instance and the styling it was built for stay the same. The dashed outline is the
     * exception: it is built in world coordinates with a dash length chosen for the zoom, so it
     * also depends on the transform and dash length it was built with.
     */
    private record EntityMeshes(Shape relativeShape, CutType cutType, AffineTransform transform, double dashLength,
                                MeshHandle fill, MeshHandle outline, MeshHandle dashedOutline, MeshHandle decoration,
                                MeshHandle image, TextureHandle texture, BufferedImage textureSource) {
        boolean isCurrentFor(Shape relativeShape, CutType cutType, AffineTransform transform, double dashLength,
                             BufferedImage preview) {
            return this.relativeShape == relativeShape && this.cutType == cutType && textureSource == preview
                    && (dashedOutline == null || (this.transform.equals(transform) && this.dashLength == dashLength));
        }

        void release(RenderContext context) {
            all().forEach(context::release);
            if (texture != null) {
                context.release(texture);
            }
        }

        List<MeshHandle> all() {
            List<MeshHandle> handles = new ArrayList<>();
            for (MeshHandle handle : new MeshHandle[]{fill, outline, dashedOutline, decoration, image}) {
                if (handle != null) {
                    handles.add(handle);
                }
            }
            return handles;
        }
    }

    private final Map<Entity, EntityMeshes> cache = new IdentityHashMap<>();
    private final EntityListener designListener = event -> {
        if (DESIGN_CHANGE_EVENTS.contains(event.getType())) {
            WorkspaceManager.getInstance().markActiveWorkspaceDirty(true);
            Platform.runLater(this::invalidate);
        }
    };
    private final ControllerListener controllerListener = event -> {
        if (event == ControllerEventType.NEW_DRAWING) {
            Platform.runLater(this::invalidate);
        }
    };
    private final SelectionListener selectionListener = event -> Platform.runLater(this::requestRender);
    private final SettingsListener settingsListener = () -> Platform.runLater(this::invalidate);
    private final ChangeListener<Object> redrawListener = (observable, oldValue, newValue) -> requestRender();
    private final WorkspaceManager.WorkspaceListener workspaceListener = new WorkspaceManager.WorkspaceListener() {
        @Override
        public void onWorkspaceOpened(WorkspaceContext workspace) {
            Platform.runLater(() -> bind(workspace));
        }

        @Override
        public void onWorkspaceClosed() {
            Platform.runLater(() -> bind(null));
        }

        @Override
        public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
        }
    };

    private Scene scene;
    private Controller controller;
    private List<Entity> entities = List.of();
    private MeshHandle tabs;
    private boolean stale = true;
    private double dashLength = Double.NaN;

    @Override
    public SceneLayer layer() {
        return SceneLayer.DESIGN_FILL;
    }

    @Override
    public boolean isVisible() {
        return controller != null && VisualizerSettings.getInstance().showDesignProperty().get();
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        VisualizerSettings settings = VisualizerSettings.getInstance();
        settings.showDesignProperty().addListener(redrawListener);
        settings.colorDesignShapeOutlineProperty().addListener(redrawListener);
        settings.colorDesignShapeBackgroundProperty().addListener(redrawListener);
        WorkspaceManager.getInstance().addListener(workspaceListener);
        bind(WorkspaceManager.getInstance().getActiveWorkspace().orElse(null));
    }

    @Override
    public void onDetached(Scene scene) {
        WorkspaceManager.getInstance().removeListener(workspaceListener);
        VisualizerSettings settings = VisualizerSettings.getInstance();
        settings.showDesignProperty().removeListener(redrawListener);
        settings.colorDesignShapeOutlineProperty().removeListener(redrawListener);
        settings.colorDesignShapeBackgroundProperty().removeListener(redrawListener);
        bind(null);
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        double zoomDashLength = dashLengthFor(context.camera());
        if (zoomDashLength != dashLength) {
            dashLength = zoomDashLength;
            stale = true;
        }
        if (stale) {
            refresh(context);
        }
        VisualizerSettings settings = VisualizerSettings.getInstance();
        float[] fillRgba = toRgba(Color.web(settings.colorDesignShapeBackgroundProperty().get()));
        float[] outlineRgba = toRgba(Color.web(settings.colorDesignShapeOutlineProperty().get()));

        for (Entity entity : entities) {
            EntityMeshes meshes = cache.get(entity);
            if (meshes == null) {
                continue;
            }
            AffineTransform transform = entity.getTransform();
            float[] fillModel = toModel(transform, FILL_Z);
            float[] outlineModel = toModel(transform, OUTLINE_Z);
            if (meshes.fill() != null) {
                context.drawTriangles(meshes.fill(), fillModel, fillRgba, false);
            }
            if (meshes.image() != null) {
                context.drawTextured(meshes.image(), fillModel, meshes.texture(), 1);
            }
            if (meshes.dashedOutline() != null) {
                context.drawLines(meshes.dashedOutline(), Mat4.translation(0, 0, OUTLINE_Z), HINT_RGBA, OUTLINE_WIDTH_PX);
            } else {
                context.drawLines(meshes.outline(), outlineModel, outlineRgba, OUTLINE_WIDTH_PX);
            }
            if (meshes.decoration() != null) {
                context.drawLines(meshes.decoration(), outlineModel, outlineRgba, OUTLINE_WIDTH_PX);
            }
            if (controller.getSelectionManager().isSelected(entity)) {
                context.drawLines(meshes.outline(), outlineModel, SELECTION_RGBA, SELECTION_WIDTH_PX);
            }
        }
        if (tabs != null) {
            context.drawLines(tabs, Mat4.translation(0, 0, OUTLINE_Z), TAB_RGBA, TAB_WIDTH_PX);
        }
    }

    /**
     * The dash length in world units that comes closest to {@link #DASH_PIXELS} on screen,
     * rounded to a power of two so the dashes are only rebuilt when the zoom roughly doubles.
     */
    private static double dashLengthFor(Camera camera) {
        double exact = DASH_PIXELS * camera.worldUnitsPerPixel();
        return Math.pow(2, Math.round(Math.log(exact) / Math.log(2)));
    }

    /**
     * Re-reads the drawing, keeping the meshes of entities whose geometry and styling are
     * unchanged and rebuilding the rest.
     */
    private void refresh(RenderContext context) {
        stale = false;
        Map<Entity, EntityMeshes> previous = new IdentityHashMap<>(cache);
        cache.clear();
        entities = controller == null ? List.of() : new ArrayList<>(controller.getModel().getEntities());
        LineMeshBuilder tabBuilder = new LineMeshBuilder();
        for (Entity entity : entities) {
            if (entity instanceof Cuttable cuttable && cuttable.isHidden()) {
                continue;
            }
            Shape relativeShape = entity.getRelativeShape();
            CutType cutType = entity instanceof Cuttable cuttable ? cuttable.getCutType() : CutType.NONE;
            BufferedImage preview = previewOf(entity);
            EntityMeshes existing = previous.remove(entity);
            if (existing != null && existing.isCurrentFor(relativeShape, cutType, entity.getTransform(), dashLength, preview)) {
                cache.put(entity, existing);
            } else {
                if (existing != null) {
                    existing.release(context);
                }
                cache.put(entity, build(context, entity, relativeShape, cutType, preview));
            }
            if (entity instanceof Cuttable cuttable) {
                appendTabs(tabBuilder, cuttable);
            }
        }
        previous.values().forEach(meshes -> meshes.release(context));
        if (tabs != null) {
            context.release(tabs);
            tabs = null;
        }
        if (!tabBuilder.isEmpty()) {
            tabs = context.upload(tabBuilder.build(), VertexLayout.LINE);
        }
    }

    /**
     * The image a raster shows: its ink mask, or the grey depth map when it is depth mapping.
     * Rasters cache these, so the same instance comes back until the raster changes.
     */
    private static BufferedImage previewOf(Entity entity) {
        return entity instanceof Raster raster ? raster.getPreviewImage() : null;
    }

    private EntityMeshes build(RenderContext context, Entity entity, Shape relativeShape, CutType cutType, BufferedImage preview) {
        MeshHandle outline = context.upload(DesignTessellator.outline(relativeShape, Color.BLACK), VertexLayout.LINE);
        MeshHandle image = null;
        TextureHandle texture = null;
        if (preview != null) {
            BufferedImage fitted = fitTexture(preview);
            texture = context.uploadTexture(fitted.getWidth(), fitted.getHeight(),
                    fitted.getRGB(0, 0, fitted.getWidth(), fitted.getHeight(), null, 0, fitted.getWidth()));
            image = context.upload(DesignTessellator.texturedQuad(relativeShape.getBounds2D()), VertexLayout.TEXTURED);
        }
        MeshHandle fill = null;
        MeshHandle dashed = null;
        MeshHandle decoration = null;
        if (cutType == CutType.NONE) {
            dashed = context.upload(DesignTessellator.dashedOutline(entity.getShape(), HINT_COLOR, dashLength), VertexLayout.LINE);
        }
        if (cutType == CutType.CENTER_DRILL) {
            decoration = context.upload(crosshair(relativeShape.getBounds2D()), VertexLayout.LINE);
        } else if (cutType == CutType.SURFACE && entity instanceof Cuttable cuttable) {
            decoration = context.upload(surfacingArrows(relativeShape.getBounds2D(), cuttable), VertexLayout.LINE);
        }

        if (cutType == CutType.SURFACE || cutType == CutType.LASER_FILL || cutType == CutType.PLOTTER_FILL || cutType == CutType.HEIGHT_MAP || cutType == CutType.POCKET) {
            fill = context.upload(DesignTessellator.fill(relativeShape), VertexLayout.MESH);
        }

        return new EntityMeshes(relativeShape, cutType, new AffineTransform(entity.getTransform()), dashLength,
                fill, outline, dashed, decoration, image, texture, preview);
    }

    /**
     * Scales the image down to at most {@link #MAX_TEXTURE_SIZE} on its longer side; the source
     * of a raster can be far larger than it will ever be shown.
     */
    private static BufferedImage fitTexture(BufferedImage image) {
        int longest = Math.max(image.getWidth(), image.getHeight());
        if (longest <= MAX_TEXTURE_SIZE) {
            return image;
        }
        double scale = (double) MAX_TEXTURE_SIZE / longest;
        int width = Math.max(1, (int) Math.round(image.getWidth() * scale));
        int height = Math.max(1, (int) Math.round(image.getHeight() * scale));
        BufferedImage fitted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = fitted.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(image, 0, 0, width, height, null);
        } finally {
            graphics.dispose();
        }
        return fitted;
    }

    /**
     * Marks the stretches that are left uncut so it is visible where the shape stays attached
     * to the stock. Tab shapes are already in workspace coordinates.
     */
    private void appendTabs(LineMeshBuilder builder, Cuttable cuttable) {
        Shape tabShape = TabShapes.create(cuttable, controller.getSettings());
        if (tabShape == null || tabShape.getPathIterator(null).isDone()) {
            return;
        }
        float[] segments = DesignTessellator.outline(tabShape, Colors.ORANGE);
        int floats = VertexLayout.LINE.floatsPerVertex();
        for (int i = 0; i + 2 * floats <= segments.length; i += 2 * floats) {
            builder.add(segments[i], segments[i + 1], segments[i + 2],
                    segments[i + floats], segments[i + floats + 1], segments[i + floats + 2], Colors.ORANGE);
        }
    }

    private static float[] crosshair(Rectangle2D bounds) {
        double centerX = bounds.getCenterX();
        double centerY = bounds.getCenterY();
        return new LineMeshBuilder(2)
                .add(bounds.getMinX() + 1, centerY, 0, bounds.getMaxX() - 1, centerY, 0, Color.BLACK)
                .add(centerX, bounds.getMinY() + 1, 0, centerX, bounds.getMaxY() - 1, 0, Color.BLACK)
                .build();
    }

    private static float[] surfacingArrows(Rectangle2D bounds, Cuttable cuttable) {
        LineMeshBuilder builder = new LineMeshBuilder(6);
        double angle = cuttable.getToolPathDirection() == ToolPathDirection.VERTICAL ? 90 : 0;
        double length = Math.min(bounds.getWidth(), bounds.getHeight());
        appendArrow(builder, bounds.getCenterX(), bounds.getCenterY(), angle, length);
        if (cuttable.getDirection() == Direction.BOTH) {
            appendArrow(builder, bounds.getCenterX(), bounds.getCenterY(), angle + 180, length);
        }
        return builder.build();
    }

    private static void appendArrow(LineMeshBuilder builder, double centerX, double centerY, double angleDegrees, double length) {
        double angle = Math.toRadians(angleDegrees);
        double half = length / 2;
        double headSize = length * 0.2;
        double tipX = centerX + Math.cos(angle) * half;
        double tipY = centerY + Math.sin(angle) * half;
        double tailX = centerX - Math.cos(angle) * half;
        double tailY = centerY - Math.sin(angle) * half;
        builder.add(tailX, tailY, 0, tipX, tipY, 0, Color.BLACK);
        for (double side : new double[]{150, -150}) {
            double headAngle = angle + Math.toRadians(side);
            builder.add(tipX, tipY, 0, tipX + Math.cos(headAngle) * headSize, tipY + Math.sin(headAngle) * headSize, 0, Color.BLACK);
        }
    }

    private void bind(WorkspaceContext workspace) {
        unbind();
        if (workspace instanceof UgsdWorkspaceContext) {
            controller = ControllerFactory.getController();
            controller.getModel().getRootEntity().addListener(designListener);
            controller.getSelectionManager().addSelectionListener(selectionListener);
            controller.getSettings().addListener(settingsListener);
            controller.addListener(controllerListener);
        }
        invalidate();
    }

    private void unbind() {
        if (controller != null) {
            controller.getModel().getRootEntity().removeListener(designListener);
            controller.getSelectionManager().removeSelectionListener(selectionListener);
            controller.getSettings().removeListener(settingsListener);
            controller.removeListener(controllerListener);
            controller = null;
        }
        releaseAll();
    }

    private void releaseAll() {
        if (scene != null) {
            cache.values().forEach(meshes -> meshes.release(scene.context()));
            if (tabs != null) {
                scene.context().release(tabs);
            }
        }
        cache.clear();
        tabs = null;
        entities = List.of();
    }

    private void invalidate() {
        stale = true;
        requestRender();
    }

    private void requestRender() {
        if (scene != null) {
            scene.requestRender();
        }
    }

    private static float[] toModel(AffineTransform transform, double z) {
        return Mat4.affine2D(transform.getScaleX(), transform.getShearY(), transform.getShearX(),
                transform.getScaleY(), transform.getTranslateX(), transform.getTranslateY(), z);
    }

    private static float[] toRgba(Color color) {
        return new float[]{(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity()};
    }
}
