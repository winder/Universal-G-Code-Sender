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
package org.kabeja.dxf.helpers;

import java.util.Stack;
import java.util.StringTokenizer;

import org.kabeja.dxf.DXFMText;
import org.kabeja.dxf.DXFText;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFTextParser {
    public static TextDocument parseDXFMText(DXFMText text) {
        // initialize
        TextDocument doc = new TextDocument();
        StringBuffer buf = new StringBuffer();
        StringBuffer value = new StringBuffer();
        StyledTextParagraph p = new StyledTextParagraph();
        p.setFontHeight(text.getHeight());
        p.setInsertPoint(text.getInsertPoint());

        switch (text.getAlignment()) {
        case DXFMText.ATTACHMENT_TOP_LEFT:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_TOP);

            break;

        case DXFMText.ATTACHMENT_TOP_CENTER:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_TOP);

            break;

        case DXFMText.ATTACHMENT_TOP_RIGHT:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_TOP);

            break;

        case DXFMText.ATTACHMENT_MIDDLE_LEFT:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_CENTER);

            break;

        case DXFMText.ATTACHMENT_MIDDLE_CENTER:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_CENTER);

            break;

        case DXFMText.ATTACHMENT_MIDDLE_RIGHT:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_CENTER);

            break;

        case DXFMText.ATTACHMENT_BOTTOM_LEFT:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_BASELINE);

            break;

        case DXFMText.ATTACHMENT_BOTTOM_CENTER:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_BASELINE);

            break;

        case DXFMText.ATTACHMENT_BOTTOM_RIGHT:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_BASELINE);

            break;

        default:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_BASELINE);

            break;
        }

        boolean formatting = false;
        boolean keyfollow = false;
        boolean complete = true;
        Stack paras = new Stack();
        int linecount = 0;
        String str = text.getText();
        char key = ' ';

        //parse the symbols
        str = parseSymbols(str);

        //parse the style
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            switch (c) {
            case '\\':

                if (formatting) {
                    if (!complete) {
                        parseStyledTextParagraphSettings(key, value.toString(),
                            p);
                        value.delete(0, value.length());
                        formatting = true;
                        keyfollow = true;
                    } else {
                        buf.append(c);
                        formatting = false;
                    }
                } else {
                    formatting = true;
                    keyfollow = true;
                    complete = false;
                }

                break;

            case '~':

                if (formatting) {
                    buf.append(c);
                    formatting = false;
                    keyfollow = false;
                }

                break;

            case ';':

                if (formatting) {
                    parseStyledTextParagraphSettings(key, value.toString(), p);
                    value.delete(0, value.length());
                    formatting = false;
                    complete = true;
                    keyfollow = false;
                } else {
                    buf.append(c);
                }

                break;

            case '}':

                if (formatting && keyfollow) {
                    buf.append(c);
                    formatting = false;
                    keyfollow = false;
                } else {
                    if (formatting) {
                        value.append(c);
                    } else {
                        // format change end
                        doc.addStyledParagraph(p);
                        p = createParagraphFromParent(p);

                        if (paras.size() > 0) {
                            p = (StyledTextParagraph) paras.pop();
                        }
                    }
                }

                break;

            case '{':

                if (formatting && keyfollow) {
                    buf.append(c);
                    formatting = false;
                    keyfollow = false;
                } else {
                    if (formatting) {
                        value.append(c);
                    } else {
                        // start format change
                        if (i != 0) {
                            paras.add(p);
                            p = new StyledTextParagraph();
                        }
                    }
                }

                break;

            case 'O':

                if (formatting && keyfollow) {
                    p.setText(buf.toString());
                    buf.delete(0, buf.length());
                    doc.addStyledParagraph(p);
                    p = createParagraphFromParent(p);
                    p.setOverline(true);
                    formatting = false;
                    keyfollow = false;
                } else {
                    if (formatting) {
                        value.append(c);
                    } else {
                        buf.append(c);
                    }
                }

                break;

            case 'o':

                if (formatting && keyfollow) {
                    p.setText(buf.toString());
                    buf.delete(0, buf.length());
                    doc.addStyledParagraph(p);
                    p = createParagraphFromParent(p);
                    p.setOverline(false);
                    formatting = false;
                    keyfollow = false;
                } else {
                    if (formatting) {
                        value.append(c);
                    } else {
                        buf.append(c);
                    }
                }

                break;

            case 'u':

                if (formatting && keyfollow) {
                    p.setText(buf.toString());
                    buf.delete(0, buf.length());
                    doc.addStyledParagraph(p);
                    p = createParagraphFromParent(p);
                    p.setUnderline(false);
                    formatting = false;
                    keyfollow = false;
                } else {
                    if (formatting) {
                        value.append(c);
                    } else {
                        buf.append(c);
                    }
                }

                break;

            case 'L':

                if (formatting && keyfollow) {
                    p.setText(buf.toString());
                    buf.delete(0, buf.length());
                    doc.addStyledParagraph(p);
                    p = createParagraphFromParent(p);
                    p.setUnderline(true);
                    formatting = false;
                    keyfollow = false;
                } else {
                    if (formatting) {
                        value.append(c);
                    } else {
                        buf.append(c);
                    }
                }

                break;

            case 'l':

                if (formatting && keyfollow) {
                    p.setText(buf.toString());
                    buf.delete(0, buf.length());
                    doc.addStyledParagraph(p);
                    p = createParagraphFromParent(p);
                    p.setUnderline(false);
                    formatting = false;
                    keyfollow = false;
                } else {
                    if (formatting) {
                        value.append(c);
                    } else {
                        buf.append(c);
                    }
                }

                break;

            case 'P':

                if (formatting && keyfollow) {
                    linecount++;
                    p.setText(buf.toString());
                    buf.delete(0, buf.length());

                    doc.addStyledParagraph(p);
                    p = createParagraphFromParent(p);

                    formatting = false;
                    keyfollow = false;
                    p.setLineIndex(linecount);
                    p.setNewline(true);
                } else {
                    if (formatting) {
                        value.append(c);
                    } else {
                        buf.append(c);
                    }
                }

                break;

            default:

                if (formatting) {
                    if (keyfollow) {
                        key = c;
                        keyfollow = false;
                    } else {
                        value.append(c);
                    }
                } else {
                    buf.append(c);
                }

                break;
            }
        }

        if (formatting) {
            parseStyledTextParagraphSettings(key, value.toString(), p);
        }

        if (buf.length() > 0) {
            p.setText(buf.toString());
            doc.addStyledParagraph(p);
        }

        if (doc.getParagraphCount() == 0) {
            doc.addStyledParagraph(p);
        }

        return doc;
    }

    protected static StyledTextParagraph createParagraphFromParent(
        StyledTextParagraph parent) {
        StyledTextParagraph p = new StyledTextParagraph();
        p.setValign(parent.getValign());
        p.setBold(parent.isBold());
        p.setFont(parent.getFont());
        p.setItalic(parent.isItalic());
        p.setUnderline(parent.isUnderline());
        p.setOverline(parent.isOverline());
        p.setWidth(parent.getWidth());
        p.setFontHeight(parent.getFontHeight());
        p.setInsertPoint(parent.getInsertPoint());

        return p;
    }

    public static TextDocument parseDXFText(DXFText text) {
        TextDocument doc = new TextDocument();

        //  boolean asciicontrol = false;
        StringBuffer buf = new StringBuffer();

        StyledTextParagraph p = new StyledTextParagraph();
        p.setFontHeight(text.getHeight());
        p.setInsertPoint(text.getAlignmentPoint());

        switch (text.getValign()) {
        case DXFText.VALIGN_BASELINE:

            if (text.getAlign() == DXFText.ALIGN_MIDDLE) {
                // described in the DXF specs
                p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_CENTER);
            } else {
                p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_BASELINE);
            }

            break;

        case DXFText.VALIGN_BOTTOM:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_BASELINE);

            break;

        case DXFText.VALIGN_CENTER:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_CENTER);

            break;

        case DXFText.VALIGN_TOP:
            p.setValign(StyledTextParagraph.VERTICAL_ALIGNMENT_TOP);

            break;
        }

        if ((text.getAlign() == 3) || (text.getAlign() == 5)) {
            double length = DXFUtils.distance(text.getInsertPoint(),
                    text.getAlignmentPoint());
            p.setWidth(length);
        }

        //parse the symbols
        String str = parseSymbols(text.getText());

        //parse the style
        int marker = 0;
        char c;

        // initialize
        boolean overline = false;
        boolean underline = false;

        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);

            if (c == '%') {
                marker++;
            } else {
                if (marker == 0) {
                    buf.append(c);
                } else if (marker == 2) {
                    switch (c) {
                    case 'o':
                        p.setText(buf.toString());
                        p.setUnderline(underline);
                        p.setOverline(overline);
                        doc.addStyledParagraph(p);
                        p = createParagraphFromParent(p);
                        buf.delete(0, buf.length());
                        overline = !overline;
                        p.setOverline(overline);

                        break;

                    case 'u':
                        p.setText(buf.toString());
                        p.setUnderline(underline);
                        p.setOverline(overline);
                        doc.addStyledParagraph(p);
                        p = createParagraphFromParent(p);

                        buf.delete(0, buf.length());
                        underline = !underline;

                        p.setUnderline(underline);

                        break;
                    }

                    marker = 0;
                }
            }
        }

        if ((marker == 1) || (marker == 3)) {
            buf.append('%');
        }

        // something left over?
        if (buf.length() > 0) {
            p.setText(buf.toString());
            doc.addStyledParagraph(p);
        }

        return doc;
    }

    public static void parseStyledTextParagraphSettings(char key, String value,
        StyledTextParagraph para) {
        switch (key) {
        case 'A':
            para.setValign(Integer.parseInt(value));

            break;

        case 'H':

            if (value.endsWith("x")) {
                para.setFontHeight(para.getFontHeight() * Double.parseDouble(
                        value.substring(0, value.length() - 1)));
            } else {
                para.setFontHeight(Double.parseDouble(value));
            }

            break;

        case 'Q':
            para.setObliquiAngle(Double.parseDouble(value));

            break;

        case 'W':
            para.setWidth(Double.parseDouble(value));

            break;

        case 'T':
            para.setCharacterspace(Double.parseDouble(value));

            break;

        case 'f':
            parseFontSettings(value.trim(), para);

            break;

        case 'F':
            para.setFontFile(value.trim());

            break;

        case 'S':

            // TODO handle
            break;
        }
    }

    public static void parseFontSettings(String value, StyledTextParagraph para) {
        StringTokenizer st = new StringTokenizer(value, "|");
        para.setFont(st.nextToken());

        while (st.hasMoreTokens()) {
            String option = st.nextToken();
            char code = option.charAt(0);
            int i = Integer.parseInt(option.substring(1));

            switch (code) {
            case 'b':
                para.setBold(i == 1);

                break;

            case 'i':
                para.setItalic(i == 1);

                break;

            case 'c':

                // ??? color?
                break;

            case 'p':

                // ???
                break;
            }
        }
    }

    public static String parseSymbols(String text) {
        boolean asciicontrol = false;
        StringBuffer buf = new StringBuffer();
        int marker = 0;
        char c;

        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);

            if (c == '%') {
                if (marker == 2) {
                    // a sequence of %%%%%065 means '%A'
                    buf.append('%');
                    marker = 0;
                } else {
                    marker++;
                }
            } else if (c == '^') {
                asciicontrol = true;
            } else if (asciicontrol) {
                // ASCII-control sign map
                if (Character.isWhitespace(c)) {
                    buf.append('^');
                } else {
                    // filtering acsii controls here
                }

                asciicontrol = false;
            } else if (c == '\\') {
                //test for unicode
                if ((text.length() > (i + 6)) && (text.charAt(i + 1) == 'U') &&
                        (text.charAt(i + 2) == '+')) {
                    String part = text.substring(i + 3, i + 7);
                    int unicode = Integer.parseInt(part, 16);
                    buf.append((char) unicode);
                    i += 6;
                } else {
                    buf.append('\\');
                }
            } else {
                if (marker == 0) {
                    buf.append(c);
                } else if (marker == 1) {
                    // the control % self
                    buf.append('%');
                    marker = 0;
                } else if (marker == 2) {
                    switch (c) {
                    case 'd':
                        buf.append('\u00B0');

                        break;

                    case 'c':
                        buf.append('\u2205');

                        break;

                    case 'p':
                        buf.append('\u00B1');

                        break;

                    default:

                        if (Character.isDigit(c) && ((i + 2) < text.length())) {
                            String code = "" + c + text.charAt(i + 1) +
                                text.charAt(i + 2);

                            try {
                                c = (char) Integer.parseInt(code);
                                buf.append(c);
                                i += 2;
                            } catch (NumberFormatException e) {
                                // TODO sometimes there is only one
                                // digit, so what should be the 
                                //replacement???
                                buf.append('?');
                                i++;
                            }
                        } else {
                            //a style control write back
                            buf.append("%%");
                            buf.append(c);
                        }
                    }

                    marker = 0;
                } else if (marker == 3) {
                    buf.append('%');
                    marker = 0;
                }
            }
        }

        if ((marker == 1) || (marker == 3)) {
            buf.append('%');
        }

        return buf.toString();
    }
}
