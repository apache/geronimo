/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.geronimo.interop.properties.PropertyMap;


public class NamedValueList extends LinkedList {
    public NamedValueList() {
    }

    public NamedValueList(String namedValueList) {
        List csvList = ListUtil.getCommaSeparatedList(namedValueList);
        for (Iterator i = csvList.iterator(); i.hasNext();) {
            String item = (String) i.next();
            int eqPos = item.indexOf("=");
            if (eqPos == -1) {
                badList(namedValueList);
            }
            String name = item.substring(0, eqPos).trim();
            if (name.length() == 0) {
                badList(namedValueList);
            }
            String value = item.substring(eqPos + 1).trim();
            add(new NamedValue(name, value));
        }
    }

    // public methods

    public PropertyMap getProperties() {
        PropertyMap props = new PropertyMap();
        for (Iterator i = this.iterator(); i.hasNext();) {
            NamedValue nv = (NamedValue) i.next();
            props.put(nv.name, nv.value);
        }
        return props;
    }

    public String getValue(String name) {
        return getValue(name, null);
    }

    public String getValue(String name, String defaultValue) {
        return (String) getProperties().getProperty(name, defaultValue);
    }

    public String toString() {
        return ListUtil.formatCommaSeparatedList(this);
    }

    // protected methods

    protected void badList(String namedValueList) {
        throw new IllegalArgumentException("namedValueList = " + namedValueList);
    }
}
