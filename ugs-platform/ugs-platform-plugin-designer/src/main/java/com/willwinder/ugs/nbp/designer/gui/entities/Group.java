package com.willwinder.ugs.nbp.designer.gui.entities;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Group extends AbstractEntity {
    private List<Entity> children = new ArrayList<>();

    @Override
    public final void render(Graphics2D g) {
        children.forEach(node -> node.render(g));
    }

    @Override
    public void setSize(Dimension s) {

    }

    @Override
    public Shape getShape() {
        Area area = new Area();
        List<Entity> allChildren = getAllChildren();
        allChildren.stream()
                .filter(c -> c != this)
                .forEach(c -> area.add(new Area(c.getShape())));

        return area.getBounds();
    }


    @Override
    public Shape getRelativeShape() {
        return new Rectangle2D.Double();
    }

    public void addChild(Entity node) {
        if (!containsChild(node)) {
            children.add(node);
            node.setParent(this);
        }
    }

    public boolean containsChild(Entity node) {
        return children.contains(node);
    }

    public void removeChild(Entity shape) {
        children.remove(shape);
    }

    @Override
    public void destroy() {
        super.destroy();
        children.forEach(Entity::destroy);
    }

    public void removeAll(List<? extends Entity> shapes) {
        this.children.removeAll(shapes);
    }

    public void removeAll() {
        this.children.clear();
    }

    public List<Entity> getChildrenAt(Point2D p) {
        List<Entity> result = this.children
                .stream()
                .flatMap(s -> {
                    if (s instanceof Group) {
                        return ((Group) s).getChildrenAt(p).stream();
                    } else if (s.isWithin(p)) {
                        return Stream.of(s);
                    } else {
                        return Stream.empty();
                    }
                }).collect(Collectors.toList());

        return Collections.unmodifiableList(result);
    }

    /**
     * Get a list of all direct children of this group
     *
     * @return a list of children entites
     */
    public final List<Entity> getChildren() {
        return Collections.unmodifiableList(this.children);
    }


    public final List<Entity> getAllChildren() {
        List<Entity> result = this.children
                .stream()
                .flatMap(s -> {
                    if (s instanceof Group) {
                        return ((Group) s).getAllChildren().stream();
                    } else {
                        return Stream.of(s);
                    }
                }).collect(Collectors.toList());

        return Collections.unmodifiableList(result);
    }
}
