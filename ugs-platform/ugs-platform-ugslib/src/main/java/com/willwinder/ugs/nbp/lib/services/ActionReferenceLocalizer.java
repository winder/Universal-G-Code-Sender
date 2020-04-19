/**
 * The recommended way to localize UGS Actions when they aren't extending {@link javax.swing.AbstractAction}.
 *
 * Include a snippet like the following in your Action, ActionListener, etc:
 *    @OnStart
 *    public static class ActionLocalizer extends ActionReferenceLocalizer {
 *        public ActionLocalizer() {
 *            super(LocalizingService.CATEGORY_MACHINE, "RunFromHere", NAME);
 *        }
 *    }
 *
 * Note: Abstract actions may localize themselves directly, and more efficiently by putting 'putValue(Action.NAME, NAME);' in the constructor.
 */
/*
 * Copyright (C) 2020 Will Winder
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.lib.services;

import java.util.Optional;

public class ActionReferenceLocalizer extends Localizer {
    ActionReference ref;
    String newName;

    /**
     * @param category optional category field will narrow results.
     * @param name     Internal file object name like, only needs to be part of the name, like 'RunFromHere'.
     */
    public ActionReferenceLocalizer(String category, String name, String newName) {
        this.newName = newName;

        Optional<ActionReference> ref = ars.lookupAction(category, name);
        if (ref.isPresent()) {
            this.ref = ref.get();
        }
    }

    @Override
    public void run() {
        ars.overrideActionName(ref, newName);
    }
}
