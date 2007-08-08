/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class GConstructorInfo implements Serializable {
    private final List attributeNames;

    public GConstructorInfo() {
        this.attributeNames = Collections.EMPTY_LIST;
    }

    public GConstructorInfo(String[] attributeNames) {
        this(Arrays.asList(attributeNames));
    }

    public GConstructorInfo(List attributeNames) {
        this.attributeNames = Collections.unmodifiableList(attributeNames);
    }

    public List getAttributeNames() {
        return attributeNames;
    }

    public String toString() {
        return "[GConstructorInfo: attributeNames=" + attributeNames + "]";
    }
    
    public String toXML() {
    	String xml = "";
    	
    	xml += "<gConstructorInfo>";
    	
    	xml += "<attributes>";
    	
    	for (Iterator loop = attributeNames.iterator(); loop.hasNext(); ) {
    		xml += "<name>" + loop.next().toString() + "</name>";
    	}
    	
    	xml += "</attributes>";
    	
    	xml += "</gConstructorInfo>";
    	
    	return xml;
    }
}
