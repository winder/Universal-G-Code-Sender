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
package org.kabeja.xml;

import java.io.OutputStream;

import org.kabeja.processing.Configurable;
import org.xml.sax.ContentHandler;


/**
 *This interface describes a Serializer, which will serialize the SAX-Events
 *to the given stream.
 *<h3>Lifecycle</h3>
 *
 * <ol>
 * <li>setProperties</li>
 * <li>getSuffix()</li>
 * <li>getMimeType()</li>
 * <li>setOutput()</li>
 * <li>startDocument and all other methods from org.xml.sax.ContentHandler </li>
 * </ol>
 *
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public interface SAXSerializer extends ContentHandler, Configurable {
    public String getSuffix();

    public String getMimeType();

    public void setOutput(OutputStream out);
}
