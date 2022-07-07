/*
    Copyright 2018-2022 Will Winder

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
package com.willwinder.ugp.welcome;

import com.willwinder.ugp.welcome.content.AbstractHtmlTab;
import com.willwinder.ugs.nbp.core.actions.OpenAction;

import java.awt.event.ActionEvent;

/**
 * @author wwinder
 */
public class GettingStartedTab extends AbstractHtmlTab {

    public GettingStartedTab() {
        super("Getting Started", GettingStartedTab.class.getResourceAsStream("/com/willwinder/ugp/welcome/resources/getstarted.html"));
    }

    public void openLink(String link) {
        switch (link) {
            case "open":
                new OpenAction().actionPerformed(new ActionEvent(this, 0, link));
                return;
            default:
                System.out.println("Unknown link action: " + link);
        }

    }
}

