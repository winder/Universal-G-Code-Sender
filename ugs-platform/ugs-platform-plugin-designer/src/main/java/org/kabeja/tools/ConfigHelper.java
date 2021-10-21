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
package org.kabeja.tools;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class ConfigHelper {
    public static final String JAVA_14_SAX_DRIVER = "org.apache.crimson.parser.XMLReaderImpl";
    public static final String JAVA_15_SAX_DRIVER = "com.sun.org.apache.xerces.internal.parsers.SAXParser";

    public static String getSAXSDDriver() {
        // check for version 1.4 and above
        String ver = System.getProperty("java.version");
        String parser = null;

        try {
            parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader()
                                     .getClass().getName();

            XMLReader r = XMLReaderFactory.createXMLReader(parser);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        //        if (ver.startsWith("1.2") || ver.startsWith("1.3")) {
        //            parser = System.getProperty("org.xml.sax.driver");
        //        } else if (ver.startsWith("1.4")) {
        //            // jdk 1.4 uses crimson
        //            parser = JAVA_14_SAX_DRIVER;
        //        } else if (ver.startsWith("1.5")) {
        //            parser = JAVA_15_SAX_DRIVER;
        //        }
        return parser;
    }
}
