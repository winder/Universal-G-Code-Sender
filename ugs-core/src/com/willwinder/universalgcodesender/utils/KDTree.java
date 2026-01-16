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
package com.willwinder.universalgcodesender.utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A implementation of a <a href="https://en.wikipedia.org/wiki/K-d_tree">K-dimensional tree</a>
 * for making it possible to quickly finding nearest point.
 *
 * @param <T> the type of value to store at the given coordinate
 * @author Joacim Breiler
 */
public class KDTree<T> {

    private Node<T> root;

    /**
     * Inserts a point with an associated value
     *
     * @param point the point to store the value at
     * @param value the value to store
     */
    public void insert(Point2D point, T value) {
        root = insert(root, point, value, true);
    }

    /**
     * Radius search (find all values within distance r of p)
     *
     * @param point  the center point to start searching from
     * @param radius the radius to search
     * @return a list of elements
     */
    public List<T> search(Point2D point, double radius) {
        List<T> result = new ArrayList<>();
        search(root, point, radius, result);
        return result;
    }

    /**
     * Get all values in the tree
     *
     * @return the list with all values
     */
    public List<T> getValues() {
        List<T> values = new ArrayList<>();
        collect(root, values);
        return values;
    }

    private Node<T> insert(Node<T> node, Point2D point, T value, boolean vertical) {
        if (node == null) {
            return new Node<>(point, value, vertical);
        }

        if (node.vertical) {
            if (point.getX() < node.point.getX()) {
                node.left = insert(node.left, point, value, !vertical);
            } else {
                node.right = insert(node.right, point, value, !vertical);
            }
        } else {
            if (point.getY() < node.point.getY()) {
                node.left = insert(node.left, point, value, !vertical);
            } else {
                node.right = insert(node.right, point, value, !vertical);
            }
        }

        return node;
    }

    private void search(
            Node<T> node,
            Point2D point,
            double radius,
            List<T> result
    ) {
        if (node == null) return;

        double r2 = radius * radius;
        double dx = node.point.getX() - point.getX();
        double dy = node.point.getY() - point.getY();
        double dist2 = dx * dx + dy * dy;

        if (dist2 <= r2) {
            result.add(node.value);
        }

        double delta = node.vertical
                ? dx
                : dy;

        if (delta > -radius) {
            search(node.left, point, radius, result);
        }
        if (delta < radius) {
            search(node.right, point, radius, result);
        }
    }

    private void collect(Node<T> node, List<T> out) {
        if (node == null) return;
        out.add(node.value);
        collect(node.left, out);
        collect(node.right, out);
    }

    private static final class Node<T> {
        final Point2D point;
        final T value;
        final boolean vertical;
        Node<T> left;
        Node<T> right;

        Node(Point2D point, T value, boolean vertical) {
            this.point = point;
            this.value = value;
            this.vertical = vertical;
        }
    }
}