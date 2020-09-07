/**
 * The recommended way to localize TopComponent menu items in UGS TopComponents.
 * 
 * Include a snippet like the following in your TopComponent:
  
  @OnStart
  public static class Localizer extends TopComponentLocalizer {
    public Localizer() {
      super("Window", "com.your.action.id", "Localized title");
    }
  }
  
 */
/*
    Copyright 2017-2020 Will Winder

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
package com.willwinder.ugs.nbp.lib.services;

/**
 *
 * @author wwinder
 */
public class TopComponentLocalizer extends Localizer {
  private final String category;
  private final String key;
  private final String name;

  /**
   * @param category Category used in the action registration.
   * @param key ActionId used for the action.
   * @param name String to use for the menu button.
   */
  public TopComponentLocalizer(String category, String key, String name) {
    this.category = category;
    this.key = key;
    this.name = name;
  }

  @Override
  public void run() {
    ars.overrideActionName(category, key, name);
  }
}
