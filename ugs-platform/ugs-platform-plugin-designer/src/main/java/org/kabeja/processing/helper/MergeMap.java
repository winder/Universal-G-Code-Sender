/*
   Copyright 2008 Simon Mieth

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
package org.kabeja.processing.helper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;


public class MergeMap implements Map {
    private Map base;
    private Map override;

    public MergeMap(Map base, Map override) {
        this.base = base;
        this.override = override;
    }

    public void clear() {
    }

    public boolean containsKey(Object key) {
        if (this.override.containsKey(key)) {
            return true;
        } else {
            return this.base.containsKey(key);
        }
    }

    public boolean containsValue(Object value) {
        if (this.override.containsValue(value)) {
            return true;
        } else {
            return this.base.containsValue(value);
        }
    }

    public Set entrySet() {
        return null;
    }

    public Object get(Object key) {
        Object obj = this.override.get(key);

        if (obj == null) {
            obj = this.base.get(key);
        }

        return obj;
    }

    public boolean isEmpty() {
        if (this.override.isEmpty()) {
            return true;
        } else {
            return this.base.isEmpty();
        }
    }

    public Set keySet() {
        return null;
    }

    public Object put(Object arg0, Object arg1) {
        return null;
    }

    public void putAll(Map arg0) {
    }

    public Object remove(Object key) {
        return null;
    }

    public int size() {
        return this.base.size();
    }

    public Collection values() {
        return null;
    }
}
