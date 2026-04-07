/*
   Copyright 2006 Simon Mieth

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
package org.kabeja.io;

import java.io.OutputStream;
import java.util.Map;

import org.kabeja.dxf.DXFDocument;


/**
 *
 * This interface describes a Generator, which will generate  output  the given stream.
 *<h3>Lifecycle</h3>
 *
 * <ol>
 * <li>setProperties</li>
 * <li>getSuffix()</li>
 * <li>getMimeType()</li>
 * <li>generate()</li>
 * </ol>
 *@author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 */
public interface StreamGenerator {
    public void setProperties(Map properties);

    public String getSuffix();

    public String getMimeType();

    /**
     * Output the generation result to the given stream.
     * @param doc the @see DXFDocument to  output
     * @param out
     */
    public void generate(DXFDocument doc, OutputStream out);
}
