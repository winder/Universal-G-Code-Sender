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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * A side pane of the main window, holding whatever content the owner puts in it. It keeps its
 * width when the window is resized, and its title names it on the collapsed rail. Without content
 * the pane is empty, which its {@link CollapsibleSidePane} host takes as a cue to hide it altogether.
 */
public class SidePane extends VBox {

    private static final double MIN_WIDTH = 200;

    private final SidePaneAlignment sideAlignment;
    private final StringProperty title = new SimpleStringProperty();
    private final ObjectProperty<Node> content = new SimpleObjectProperty<>();

    public SidePane(SidePaneAlignment sideAlignment) {
        this.sideAlignment = sideAlignment;
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
     * The content filling the pane. Null leaves the pane empty.
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
            getChildren().clear();
        } else {
            VBox.setVgrow(content, Priority.ALWAYS);
            getChildren().setAll(content);
        }
    }
}
