/**
 *
 * Copyright 2004 The Apache Software Foundation
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.gbean;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:48 $
 */
public class GConstructorInfo implements Serializable {
    private final List attributeNames;
    private final List types;
    private final Map attributeTypeMap;

    public GConstructorInfo(String[] attributeNames, Class[] types) {
        this(Arrays.asList(attributeNames), Arrays.asList(types));
    }

    public GConstructorInfo(List attributeNames, List types) {
        assert attributeNames.size() == types.size(): "name count: " + attributeNames.size() + " does not match type count: " + types.size();
        // todo check of null types and names
        // todo check that is a name is listed twice that is has the same type each time
        this.attributeNames = Collections.unmodifiableList(attributeNames);
        this.types = Collections.unmodifiableList(types);

        Map typeMap = new HashMap(this.attributeNames.size());
        for (Iterator nameIter = attributeNames.iterator(), typeIter = types.iterator(); nameIter.hasNext();) {
            typeMap.put(nameIter.next(), typeIter.next());
        }
        attributeTypeMap = Collections.unmodifiableMap(typeMap);
    }

    public List getAttributeNames() {
        return attributeNames;
    }

    public List getTypes() {
        return types;
    }

    public Map getAttributeTypeMap() {
        return attributeTypeMap;
    }

    public String toString() {
        return "[GConstructorInfo: attributeNames=" + attributeNames + " types=" + types + "]";
    }
}
