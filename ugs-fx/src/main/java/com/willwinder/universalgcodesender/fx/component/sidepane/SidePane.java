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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A side pane of the main window: a slim header with the collapse button, above whatever content
 * the owner puts in it. The button sits on the edge facing the content, so it is at the right end
 * of a left-aligned pane and at the left end of a right-aligned one. The pane keeps its width when
 * the window is resized, and the title names it on the collapsed rail. Without content the pane
 * is empty, which its {@link CollapsibleSidePane} host takes as a cue to hide it altogether.
 */
public class SidePane extends VBox {

    private static final double MIN_WIDTH = 200;

    private final SidePaneAlignment sideAlignment;
    private final StringProperty title = new SimpleStringProperty();
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>();
    private final Node header;

    public SidePane(SidePaneAlignment sideAlignment, Class<? extends Action> toggleAction) {
        this.sideAlignment = sideAlignment;
        header = createHeader(toggleAction);
        getChildren().add(header);
        setMinWidth(MIN_WIDTH);
        SplitPane.setResizableWithParent(this, false);
        content.addListener((observable, oldContent, newContent) -> showContent(newContent));
    }

    /**
     * Named apart from {@link #getAlignment()}, which is the content alignment inherited from the box.
     */
    public SidePaneAlignment getSideAlignment() {
        return sideAlignment;
    }

    public StringProperty titleProperty() {
        return title;
    }

    /**
     * The content shown below the header, given the remaining height. Null leaves the pane empty.
     */
    public ObjectProperty<Node> contentProperty() {
        return content;
    }

    public Node getContent() {
        return content.get();
    }

    public void setContent(Node content) {
        this.content.set(content);
    }

    private void showContent(Node content) {
        if (content == null) {
            getChildren().setAll(header);
        } else {
            VBox.setVgrow(content, Priority.ALWAYS);
            getChildren().setAll(header, content);
        }
    }

    private Node createHeader(Class<? extends Action> toggleAction) {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(spacer);
        header.getStyleClass().add("side-pane-header");
        CollapsibleSidePane.createToggleButton(toggleAction).ifPresent(button ->
                header.getChildren().add(sideAlignment == SidePaneAlignment.LEFT ? 1 : 0, button));
        return header;
    }
}
