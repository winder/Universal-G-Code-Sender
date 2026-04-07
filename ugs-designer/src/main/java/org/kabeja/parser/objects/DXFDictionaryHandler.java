/*
   Copyright 2007 Simon Mieth

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
package org.kabeja.parser.objects;

import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.objects.DXFDictionary;
import org.kabeja.dxf.objects.DXFObject;
import org.kabeja.parser.DXFValue;


public class DXFDictionaryHandler extends AbstractDXFObjectHandler {
    public final int GROUPCODE_RECORD_NAME = 3;
    public final int GROUPCODE_RECORD_ID = 350;
    protected DXFDictionary dictionary;
    protected String objectName;
    protected boolean rootDictionaryParsed = false;

    public void endObject() {
    }

    public DXFObject getDXFObject() {
        return dictionary;
    }

    public String getObjectType() {
        return DXFConstants.OBJECT_TYPE_DICTIONARY;
    }

    public void parseGroup(int groupCode, DXFValue value) {
        switch (groupCode) {
        case GROUPCODE_RECORD_NAME:
            this.objectName = value.getValue();

            break;

        case GROUPCODE_RECORD_ID:
            this.dictionary.putDXFObjectRelation(this.objectName,
                value.getValue());

            break;

        default:
            super.parseCommonGroupCode(groupCode, value, this.dictionary);
        }
    }

    public void startObject() {
        if (this.rootDictionaryParsed) {
            this.dictionary = new DXFDictionary();
        } else {
            this.dictionary = this.doc.getRootDXFDictionary();
            this.rootDictionaryParsed = true;
        }
    }
}
