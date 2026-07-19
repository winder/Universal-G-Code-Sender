package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.universalgcodesender.fx.component.visualizer.DepthLayers;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.MeshView;

import java.awt.Shape;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class EntityShapesNode extends Group {
    private final Group fillsGroup = new Group();
    private final Group bordersGroup = new Group();
    private Runnable onEntityMoved = () -> {};

    /**
     * Caches each entity's extruded fill mesh so a drag (move/resize/rotate) does not rebuild it.
     * Those operations only mutate the entity's transform — its {@link Entity#getRelativeShape()
     * relative shape} is unchanged — so the cached mesh is reused and merely repositioned with a
     * JavaFX transform. The cache is keyed on the relative-shape instance; when it changes (a real
     * geometry edit) the mesh is rebuilt. Building the fill runs JTS validation plus CSG
     * extrusion, which is far too expensive to redo on every mouse-move tick.
     */
    private final Map<Entity, CachedFill> fillCache = new IdentityHashMap<>();

    private record CachedFill(Shape relativeShape, MeshView mesh) {
    }

    public EntityShapesNode() {
        fillsGroup.setTranslateZ(DepthLayers.DESIGN_Z_OFFSET);
        bordersGroup.setTranslateZ(DepthLayers.DESIGN_OUTLINE_Z_OFFSET);
        getChildren().addAll(fillsGroup, bordersGroup);
    }

    public void setOnEntityMoved(Runnable onEntityMoved) {
        this.onEntityMoved = onEntityMoved != null ? onEntityMoved : () -> {};
    }

    public void refreshFromController() {
        Controller controller = ControllerFactory.getController();
        Runnable onMoved = () -> {
            refreshFromController();
            onEntityMoved.run();
        };

        List<MeshView> fills = new ArrayList<>();
        List<MeshView> borders = new ArrayList<>();
        Map<Entity, CachedFill> nextCache = new IdentityHashMap<>();

        for (Entity entity : controller.getDrawing().getEntities()) {
            EntityClickHandler handler = new EntityClickHandler(controller, entity, onMoved);

            MeshView fill = resolveFill(entity, nextCache);
            if (fill != null) {
                fill.getTransforms().setAll(EntityShapeFactory.toFxTransform(entity.getTransform()));
                fill.setUserData(handler);
                fills.add(fill);
            }

            MeshView border = EntityShapeFactory.createBorder(entity.getShape(), Color.DODGERBLUE);
            if (border != null) {
                border.setUserData(handler);
                borders.add(border);
            }
        }

        fillCache.clear();
        fillCache.putAll(nextCache);
        fillsGroup.getChildren().setAll(fills);
        bordersGroup.getChildren().setAll(borders);
    }

    /**
     * Returns the entity's fill mesh, reusing the cached one when the relative geometry is
     * unchanged and rebuilding it otherwise. Entities whose relative shape is regenerated on every
     * call (e.g. groups) naturally miss the cache and rebuild, matching the previous behaviour.
     */
    private MeshView resolveFill(Entity entity, Map<Entity, CachedFill> nextCache) {
        Shape relativeShape = entity.getRelativeShape();
        CachedFill cached = fillCache.get(entity);
        MeshView fill = (cached != null && cached.relativeShape() == relativeShape)
                ? cached.mesh()
                : EntityShapeFactory.createFill(relativeShape, Color.WHITE);
        nextCache.put(entity, new CachedFill(relativeShape, fill));
        return fill;
    }

    public void onZoomChange(double zoomFactor) {
    }
}
