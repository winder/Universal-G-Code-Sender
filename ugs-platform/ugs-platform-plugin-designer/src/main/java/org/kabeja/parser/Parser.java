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
package org.kabeja.parser;

import java.io.InputStream;

import org.kabeja.dxf.DXFDocument;


/**
 * This interface describes a Parser, which will parse a specific
 * format and create a DXFDocument from this data.
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>supportedExtension()</li>
 *   <li>parse(...)</li>
 *   <li>getDXFDocument()</li>
 * </ol>
 *
 * <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public interface Parser extends Handler {
	
	
	/**
	 * Parse the given file.
	 * @param file the file to parse
	 * @throws ParseException
	 */
	
    public abstract void parse(String file) throws ParseException;

    /**
     * Parse the given file with the specific encoding.
     * @param file
     * @param encoding
     * @throws ParseException
     */
    
    
    public abstract void parse(String file, String encoding)
        throws ParseException;

    
    
    /**
     * Parse the given inputstream
     * @param input
     * @param encoding
     * @throws ParseException
     */
    public abstract void parse(InputStream input, String encoding)
        throws ParseException;
    
    /**
     * Gets the parsed DXFDocument after parsing.
     * @return the parsed @see org.kabeja.dxf.DXFDocument after parsing. 
     */

    public abstract DXFDocument getDocument();
    
    /**
     * 
     * @param extension
     * @return
     */

    public abstract boolean supportedExtension(String extension);

    /**
     * Gets the name of the parser.
     * @return
     */
    public abstract String getName();
}
