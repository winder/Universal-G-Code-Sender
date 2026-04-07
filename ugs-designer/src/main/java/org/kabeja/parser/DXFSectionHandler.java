/*
   Copyright 2005 Simon Mieth

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.kabeja.parser;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.parser.dxf.DXFHandler;


/**
 * This interface descripe a Section Handler, which should handle a SECTION
 * block.
 * <h3>Lifecycle</h3>
 * <ol>
 * <li>setDXFDocument</li>
 * <li>startSection</li>
 * <li>parseGroup (multiple)</li>
 * <li>endSection</li>
 * </lo>
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 *
 */
public interface DXFSectionHandler extends Handler, DXFHandler {
    public String getSectionKey();

    public void setDXFDocument(DXFDocument doc);

    public void startSection();

    public void endSection();
}
