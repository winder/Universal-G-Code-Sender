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
package com.willwinder.universalgcodesender.fx.component.sidepane;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.helper.SplitPaneDividers;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Hosts a {@link SidePane} as an outer item of a horizontal {@link SplitPane}, collapsible towards
 * its side of the window.
 * <p>
 * When collapsed the pane is removed from the split pane, so no divider or empty gap is left
 * behind, and a narrow {@linkplain #getRail() rail} takes its place outside the split pane. The
 * rail shows the pane title and a button that expands the pane again. The rail is only visible while
 * the pane is collapsed, so the owner can add it to its layout unconditionally. A pane without
 * {@linkplain SidePane#contentProperty() content} is hidden altogether, rail included, until it
 * has something to show again; its collapsed state is kept for when it returns.
 * <p>
 * The collapsed state is driven by a boolean property, typically a persisted setting, and toggled
 * through an {@link Action} so the same behaviour is reachable from the rail, the menu and a
 * shortcut. While the pane is shown the same action sits in a small {@linkplain #getEar() ear}
 * hanging from the top of the split pane, straddling the divider next to the pane, so the pane
 * needs no header row of its own. The owner places the ear in a layer over the split pane.
 * <p>
 * Showing or hiding the content makes JavaFX rebuild the dividers, which would hand the freed space
 * to every remaining item. Items marked {@link SplitPane#setResizableWithParent not resizable with
 * parent}, i.e. the other side panes, are instead kept at the pixel width they had, and the pane
 * itself comes back at the width it was collapsed from, so only the centre content grows or shrinks.
 */
public class CollapsibleSidePane {

    private static final int TOGGLE_ICON_SIZE = 16;

    private final SplitPane splitPane;
    private final SidePane pane;
    private final BooleanProperty collapsed;
    private final VBox rail;
    private final StackPane ear;
    private double expandedWidth;

    /**
     * @param splitPane    the horizontal split pane the pane lives in
     * @param pane         the pane to host; its alignment decides whether it is the first or last item
     * @param collapsed    the property holding the collapsed state, toggled by {@code toggleAction}
     * @param toggleAction the registered action that flips {@code collapsed}
     */
    public CollapsibleSidePane(SplitPane splitPane, SidePane pane, BooleanProperty collapsed, Class<? extends Action> toggleAction) {
        this.splitPane = splitPane;
        this.pane = pane;
        this.collapsed = collapsed;
        this.rail = createRail(toggleAction);
        this.ear = createEar(toggleAction);

        collapsed.addListener(observable -> update());
        pane.contentProperty().addListener(observable -> update());
        update();
    }

    /**
     * The narrow strip shown in place of the pane while collapsed. Place it on the same side of
     * the split pane as the pane; it is hidden and unmanaged whenever the pane is shown or empty.
     */
    public Node getRail() {
        return rail;
    }

    /**
     * The small tab with the collapse button. Add it to a pane laid over the split pane, in the
     * split pane's coordinate space; it positions itself on the divider and is only visible while
     * the pane is shown.
     */
    public Node getEar() {
        return ear;
    }

    /**
     * Creates a compact icon-only button for the given toggle action, styled to sit in a pane
     * header or toolbar. Empty if the action is not registered.
     */
    public static Optional<Node> createToggleButton(Class<? extends Action> toggleAction) {
        return ActionRegistry.getInstance()
                .getAction(toggleAction.getCanonicalName())
                .map(action -> {
                    ActionButton button = new ActionButton(action, TOGGLE_ICON_SIZE, false);
                    button.getStyleClass().add("side-pane-toggle");
                    return button;
                });
    }

    private VBox createRail(Class<? extends Action> toggleAction) {
        SidePaneAlignment alignment = pane.getSideAlignment();
        Label label = new Label();
        label.textProperty().bind(pane.titleProperty());
        label.getStyleClass().add("title");
        // Read top-to-bottom on the right edge and bottom-to-top on the left, like book spines.
        // Wrapping in a Group makes the layout use the rotated bounds instead of the original ones.
        label.setRotate(alignment == SidePaneAlignment.LEFT ? -90 : 90);

        VBox box = new VBox();
        box.getStyleClass().addAll("side-pane-rail", alignment == SidePaneAlignment.LEFT ? "left" : "right");
        createToggleButton(toggleAction).ifPresent(box.getChildren()::add);
        box.getChildren().add(new Group(label));
        return box;
    }

    private StackPane createEar(Class<? extends Action> toggleAction) {
        SidePaneAlignment alignment = pane.getSideAlignment();
        StackPane box = new StackPane();
        box.getStyleClass().addAll("side-pane-ear", alignment == SidePaneAlignment.LEFT ? "left" : "right");
        createToggleButton(toggleAction).ifPresent(box.getChildren()::add);

        // The ear sits on the centre content, flush against the divider that follows a left pane or
        // precedes a right one, so it reads as a tab growing out of that edge
        box.layoutXProperty().bind(Bindings.createDoubleBinding(() -> {
            double dividerWidth = dividerWidth();
            double x = alignment == SidePaneAlignment.LEFT
                    ? splitPane.getInsets().getLeft() + pane.getWidth() + dividerWidth - 1
                    : splitPane.getWidth() - splitPane.getInsets().getRight() - pane.getWidth() - dividerWidth - box.getWidth() + 1;
            return (double) Math.round(x);
        }, pane.widthProperty(), splitPane.widthProperty(), box.widthProperty()));
        return box;
    }

    private void update() {
        boolean hasContent = pane.getContent() != null;
        boolean showPane = hasContent && !collapsed.get();
        boolean paneShown = splitPane.getItems().contains(pane);
        if (showPane != paneShown) {
            Map<Node, Double> widths = captureSidePaneWidths();
            if (showPane) {
                boolean first = pane.getSideAlignment() == SidePaneAlignment.LEFT;
                splitPane.getItems().add(first ? 0 : splitPane.getItems().size(), pane);
                if (expandedWidth > 0) {
                    widths.put(pane, expandedWidth);
                }
            } else {
                expandedWidth = pane.getLayoutBounds().getWidth();
                splitPane.getItems().remove(pane);
            }
            // Deferred so it runs after the divider persistence has re-applied its stored fractions
            // to the recreated dividers, otherwise that restore would win over the kept widths.
            Platform.runLater(() -> restoreSidePaneWidths(widths));
        }

        boolean showRail = hasContent && collapsed.get();
        rail.setVisible(showRail);
        rail.setManaged(showRail);
        ear.setVisible(showPane);
        ear.setManaged(showPane);
    }

    private Map<Node, Double> captureSidePaneWidths() {
        Map<Node, Double> widths = new HashMap<>();
        for (Node item : splitPane.getItems()) {
            double width = item.getLayoutBounds().getWidth();
            if (item != pane && !SplitPane.isResizableWithParent(item) && width > 0) {
                widths.put(item, width);
            }
        }
        return widths;
    }

    private void restoreSidePaneWidths(Map<Node, Double> widths) {
        List<Node> items = splitPane.getItems();
        if (widths.isEmpty() || items.size() < 2) {
            return;
        }

        // The rail beside the split pane has just been shown or hidden, so lay the window out to get
        // the width the split pane will actually be divided at.
        SplitPaneDividers.layoutWindow(splitPane);
        Insets insets = splitPane.getInsets();
        double size = splitPane.getWidth() - insets.getLeft() - insets.getRight();
        if (size <= 0) {
            return;
        }

        // A divider position marks the centre of the divider, so half its width belongs to the item
        double halfDividerWidth = dividerWidth() / 2;
        Map<Integer, Double> positions = new HashMap<>();
        widths.forEach((item, width) -> {
            int index = items.indexOf(item);
            if (index == 0) {
                positions.put(0, (width + halfDividerWidth) / size);
            } else if (index == items.size() - 1) {
                positions.put(index - 1, 1 - (width + halfDividerWidth) / size);
            }
        });
        SplitPaneDividers.apply(splitPane, positions);
    }

    private double dividerWidth() {
        Node divider = splitPane.lookup(".split-pane-divider");
        return divider == null ? 0 : divider.prefWidth(-1);
    }
}
