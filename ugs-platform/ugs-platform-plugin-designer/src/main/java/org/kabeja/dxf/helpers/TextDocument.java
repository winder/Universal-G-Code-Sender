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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class TextDocument {
    protected List paragraphs = new ArrayList();

    /**
     * Return the pure text content.
     *
     * @return the text content
     */
    public String getText() {
        Iterator i = this.paragraphs.iterator();
        StringBuffer buf = new StringBuffer();

        while (i.hasNext()) {
            StyledTextParagraph para = (StyledTextParagraph) i.next();
            buf.append(para.getText());

            if (para.isNewline()) {
                buf.append('\n');
            }
        }

        return buf.toString();
    }

    public void addStyledParagraph(StyledTextParagraph para) {
        this.paragraphs.add(para);
    }

    public Iterator getStyledParagraphIterator() {
        return this.paragraphs.iterator();
    }

    public int getParagraphCount() {
        return this.paragraphs.size();
    }

    public StyledTextParagraph getStyleTextParagraph(int i) {
        return (StyledTextParagraph) this.paragraphs.get(i);
    }

    public int getLineCount() {
        int count = 1;
        Iterator i = this.paragraphs.iterator();

        while (i.hasNext()) {
            StyledTextParagraph para = (StyledTextParagraph) i.next();

            if (para.isNewline()) {
                count++;
            }
        }

        return count;
    }

    public int getMaximumLineLength() {
        int count = 0;
        int max = 0;
        Iterator i = paragraphs.iterator();

        while (i.hasNext()) {
            StyledTextParagraph para = (StyledTextParagraph) i.next();

            if (!para.isNewline()) {
                count += para.getLength();
            } else {
                if (count > max) {
                    max = count;
                }

                count = para.getLength();
            }
        }

        return max;
    }
}
