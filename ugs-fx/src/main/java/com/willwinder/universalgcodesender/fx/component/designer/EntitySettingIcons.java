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
package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.cuttable.PlungeType;
import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The icons that illustrate the choices in the {@link EntitySettingsPanel} combos.
 * <p>
 * The cut type icons are shared with the classic designer so the cut types look the same in both
 * editions. They are two-tone and already use the UGS palette, so they are rendered as-is. The
 * others are plain Phosphor icons that are tinted like the rest of the UI.
 */
final class EntitySettingIcons {
    static final int SIZE = 16;

    private static final Map<String, Optional<Image>> CACHE = new HashMap<>();

    private EntitySettingIcons() {
    }

    static Optional<ImageView> cutType(CutType cutType) {
        return load(cutTypeResource(cutType)).map(ImageView::new);
    }

    static Optional<ImageView> plungeType(PlungeType plungeType) {
        return tinted(plungeTypeResource(plungeType));
    }

    static Optional<ImageView> toolPathDirection(ToolPathDirection direction) {
        return tinted(toolPathDirectionResource(direction));
    }

    static Optional<ImageView> direction(Direction direction) {
        return tinted(directionResource(direction));
    }

    static String cutTypeResource(CutType cutType) {
        return switch (cutType) {
            case POCKET -> "img/cutpocket.svg";
            case VCARVE -> "img/cutvcarve.svg";
            case OUTSIDE_PATH -> "img/cutoutside.svg";
            case INSIDE_PATH -> "img/cutinside.svg";
            case ON_PATH, LASER_ON_PATH, PLOTTER_ON_PATH -> "img/cutonpath.svg";
            case CENTER_DRILL -> "img/centerdrill.svg";
            case SURFACE, LASER_FILL, LASER_RASTER, PLOTTER_FILL, HEIGHT_MAP -> "img/cutfill.svg";
            case NONE -> "img/cutnone.svg";
        };
    }

    static String plungeTypeResource(PlungeType plungeType) {
        return switch (plungeType) {
            case STRAIGHT -> "icons/plunge-straight.svg";
            case LINEAR_RAMP -> "icons/plunge-ramp.svg";
        };
    }

    static String toolPathDirectionResource(ToolPathDirection direction) {
        return switch (direction) {
            case HORIZONTAL -> "icons/arrows-horizontal.svg";
            case VERTICAL -> "icons/arrows-vertical.svg";
        };
    }

    static String directionResource(Direction direction) {
        return switch (direction) {
            case CLIMB -> "icons/direction-climb.svg";
            case CONVENTIONAL -> "icons/direction-conventional.svg";
            case BOTH -> "icons/direction-both.svg";
        };
    }

    private static Optional<ImageView> tinted(String resource) {
        return load(resource).map(image -> SvgLoader.tinted(image, Colors.BLACKISH));
    }

    private static Optional<Image> load(String resource) {
        return CACHE.computeIfAbsent(resource, r -> SvgLoader.loadIcon(r, SIZE));
    }
}
