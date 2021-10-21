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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.kabeja.dxf.DXFDocument;
import org.kabeja.parser.dxf.DXFHandler;
import org.kabeja.parser.dxf.filter.DXFStreamFilter;
import org.kabeja.tools.CodePageParser;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 *
 */
public class DXFParser implements HandlerManager, Handler, Parser, DXFHandler {
    public final static String PARSER_NAME = "DXFParser";
    public final static String EXTENSION = "dxf";
    private final static String SECTION_START = "SECTION";
    private final static String SECTION_END = "ENDSEC";
    private final static String END_STREAM = "EOF";
    private final static int COMMAND_CODE = 0;
    public static final String DEFAULT_ENCODING = "";
    protected DXFDocument doc;
    protected Hashtable handlers = new Hashtable();
    protected DXFSectionHandler currentHandler;
    private String line;
    protected List streamFilters = new ArrayList();
    protected DXFHandler filter;

    // some parse flags
    private boolean key = false;
    private boolean sectionstarts = false;
    private int linecount;
    private boolean parse = false;

    public DXFParser() {
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.Parser#parse(java.lang.String)
     */
    public void parse(String file) throws ParseException {
        parse(file, DEFAULT_ENCODING);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.Parser#parse(java.lang.String, java.lang.String)
     */
    public void parse(String file, String encoding) throws ParseException {
        try {
            parse(new FileInputStream(file), encoding);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.Parser#parse(java.io.InputStream,
     *      java.lang.String)
     */
    public void parse(InputStream input, String encoding)
        throws ParseException {
        String currentKey = "";
        key = false;
        linecount = 0;
        parse = false;

        //initialize 
        doc = new DXFDocument();
        doc.setProperty(DXFDocument.PROPERTY_ENCODING, encoding);
        //the StreamFilters
        this.buildFilterChain();

        BufferedReader in = null;

        try {
            if ("".equals(encoding)) {
                BufferedInputStream buf = new BufferedInputStream(input);
                buf.mark(9000);

                try {
                    BufferedReader r = new BufferedReader(new InputStreamReader(
                                buf));
                    CodePageParser p = new CodePageParser();
                    encoding = p.parseEncoding(r);
                    buf.reset();

                    in = new BufferedReader(new InputStreamReader(buf, encoding));
                } catch (IOException e1) {
                    buf.reset();
                    in = new BufferedReader(new InputStreamReader(buf));
                }
            } else {
                in = new BufferedReader(new InputStreamReader(input, encoding));
            }

            key = true;
            sectionstarts = false;

            while ((line = in.readLine()) != null) {
                linecount++;

                if (key) {
                    currentKey = line;
                    key = false;
                } else {
                    int keyCode = Integer.parseInt(currentKey.trim());
                    //the filter chain
                    filter.parseGroup(keyCode, new DXFValue(line.trim()));
                    // parseGroup(currentKey, line);
                    key = true;
                }
            }

            in.close();

            in = null;

            // finish last parsing
            if (parse) {
                currentHandler.endSection();
            }
        } catch (FileNotFoundException e) {
            throw new ParseException(e.toString());
        } catch (IOException ioe) {
            throw new ParseException(ioe.toString());
        }
    }

    public void parseGroup(int keyCode, DXFValue value)
        throws ParseException {
        //System.out.println(""+keyCode);
        //System.out.println(" "+value.getValue());
        try {
            if (sectionstarts) {
                sectionstarts = false;

                if (handlers.containsKey(value.getValue())) {
                    currentHandler = (DXFSectionHandler) handlers.get(value.getValue());
                    parse = true;
                    currentHandler.setDXFDocument(doc);
                    currentHandler.startSection();
                } else {
                    parse = false;
                }

                return;
            }

            if ((keyCode == COMMAND_CODE) &&
                    SECTION_START.equals(value.getValue()) && !sectionstarts) {
                sectionstarts = true;
            }

            if ((keyCode == COMMAND_CODE) &&
                    SECTION_END.equals(value.getValue())) {
                if (parse) {
                    currentHandler.endSection();
                }

                parse = false;

                return;
            }

            if (parse) {
                currentHandler.parseGroup(keyCode, value);
            }

            return;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new ParseException("Line: " + linecount +
                " unsupported groupcode: " + key + " for value:" + value, e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.Parser#getDocument()
     */
    public DXFDocument getDocument() {
        return doc;
    }

    public void addDXFSectionHandler(DXFSectionHandler handler) {
        handler.setDXFDocument(doc);
        handlers.put(handler.getSectionKey(), handler);
    }

    public void addHandler(Handler handler) {
        addDXFSectionHandler((DXFSectionHandler) handler);
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.Handler#releaseDXFDocument()
     */

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.Parser#releaseDXFDocument()
     */
    public void releaseDXFDocument() {
        this.doc = null;

        Iterator i = handlers.values().iterator();

        while (i.hasNext()) {
            Handler handler = (Handler) i.next();
            handler.releaseDXFDocument();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see de.miethxml.kabeja.parser.Handler#setDXFDocument(de.miethxml.kabeja.dxf.DXFDocument)
     */

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.parser.Parser#setDXFDocument(org.kabeja.dxf.DXFDocument)
     */
    public void setDXFDocument(DXFDocument doc) {
        this.doc = doc;
    }

    public boolean supportedExtension(String extension) {
        return extension.toLowerCase().equals(EXTENSION);
    }

    public void addDXFStreamFilter(DXFStreamFilter filter) {
        this.streamFilters.add(filter);
    }

    public void removeDXFStreamFilter(DXFStreamFilter filter) {
        this.streamFilters.remove(filter);
    }

    protected void buildFilterChain() {
        // build the chain from end to start
        // the parser itself is the last element
        // in the chain
        DXFHandler handler = this;

        for (int i = this.streamFilters.size() - 1; i >= 0; i--) {
            DXFStreamFilter f = (DXFStreamFilter) this.streamFilters.get(i);
            f.setDXFHandler(handler);
            handler = f;
        }

        // the first is used filter and if no filter
        // the parser itself is the filter
        this.filter = handler;
    }

    public String getName() {
        return PARSER_NAME;
    }
}
