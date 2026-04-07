/*
 * Created on 28.11.2005
 *
 */
package org.kabeja.xml;

import java.io.OutputStream;


/**
 * @author simon
 *
 */
public class ConsoleSerializer extends SAXPrettyOutputter {
    /* (non-Javadoc)
     * @see org.kabeja.xml.SAXSerializer#setOutput(java.io.OutputStream)
     */
    public void setOutput(OutputStream out) {
        //switch output to console
        super.setOutput(System.out);
    }
}
