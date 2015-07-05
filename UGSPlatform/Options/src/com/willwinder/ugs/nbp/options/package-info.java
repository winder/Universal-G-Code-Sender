/*
    Copywrite 2015 Will Winder

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
@OptionsPanelController.ContainerRegistration(
        id = "UGS", 
        categoryName = "#OptionsCategory_Name_UGS",
        iconBase = "com/willwinder/ugs/nbp/options/CNC.png", 
        keywords = "#OptionsCategory_Keywords_UGS", 
        keywordsCategory = "UGS")

@NbBundle.Messages(value = {
    "OptionsCategory_Name_UGS=UGS",
    "OptionsCategory_Keywords_UGS=UGS"})
package com.willwinder.ugs.nbp.options;

import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.NbBundle;
