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


/**
 * This is a helper class, which convert to different output formats.
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public final class DXFValue {
    private String value;
    private int integerValue = Integer.MAX_VALUE;

    /**
     *
     */
    public DXFValue() {
        super();
    }

    public DXFValue(String value) {
        setValue(value);
    }

    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    private void setValue(String value) {
        this.value = value.trim();
    }

    public double getDoubleValue() {
        return Double.parseDouble(value);
    }

    public int getIntegerValue() {
        return Integer.parseInt(value);
    }

    /**
     * Convert the DXF value to boolean
     * 0 -> false
     * 1 -> true
     * @return
     */
    public boolean getBooleanValue() {
        //0 -> true
        //else -> false
        return (getIntegerValue() == 0) ? true : false;
    }

    public String toString() {
        return value;
    }

    public boolean isBitSet(int pos) {
        if (this.integerValue == Integer.MAX_VALUE) {
            this.integerValue = getIntegerValue();
        }

        return (this.integerValue & pos) == pos;
    }
}
