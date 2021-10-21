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
package org.kabeja.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.kabeja.dxf.DXFDocument;


/**
 * @author <a href="mailto:simon.mieth@gmx.de">Simon Mieth</a>
 *
 */
public class PostProcessManager {
    private ArrayList processors = new ArrayList();

    public void addPostProcessor(PostProcessor pp) {
        processors.add(pp);
    }

    public void addPostProcessor(String classname) {
        try {
            PostProcessor pp = (PostProcessor) this.getClass().getClassLoader()
                                                   .loadClass(classname)
                                                   .newInstance();
            addPostProcessor(pp);
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void process(DXFDocument doc, Map context) throws ProcessorException {
        Iterator i = processors.iterator();

        while (i.hasNext()) {
            PostProcessor pp = (PostProcessor) i.next();
            pp.process(doc, context);
        }
    }
}
