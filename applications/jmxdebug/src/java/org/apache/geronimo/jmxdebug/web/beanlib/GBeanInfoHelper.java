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

package org.apache.geronimo.jmxdebug.web.beanlib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;

/**
 * Simple helper bean for dealing with MBeanInfo.  Helps dodge such
 * wacky APIs like  HashMap getKeyPropertyLIst() and wrap in
 * convenient ways for working in Velocity
 * 
 * @version $Rev$ $Date$
 */
public class GBeanInfoHelper {
    private final ObjectName objectName;
    private final GBeanData info;
    private final Kernel kernel;

    public GBeanInfoHelper(KernelHelper kernelHelper, String name) throws Exception {
        kernel = kernelHelper.getKernel();
        if (kernel != null) {
            objectName = new ObjectName(name);
            info = kernel.getGBeanData(objectName);
        } else {
            objectName = null;
            info = null;
        }

    }

    public String getCanonicalName() {
        return objectName.getCanonicalName();
    }

    public String getDomain() {
        return objectName.getDomain();
    }

    public String getState() {
        try {
            return State.toString(((Integer)kernel.getAttribute(objectName, "state")).intValue());
        } catch (Exception e) {
            return ("Could not get state: " + e.getMessage());
        }
    }

    /**
     * Returns the key properties and values a list of
     * maps, w/ 'key' and 'value' as entryies in each
     * map.  Makes easy in vel to do
     * #foreach($item in $list)
     * $item.key
     * $item.value
     * #end
     */
    public List getKeyProperties() {
        Hashtable h = objectName.getKeyPropertyList();

        Iterator it = h.keySet().iterator();

        List l = new ArrayList();

        while (it.hasNext()) {
            String key = (String) it.next();

            Map m = new HashMap();

            m.put("key", key);
            m.put("value", h.get(key));

            l.add(m);
        }

        return l;
    }

    public String getClassName() {
        return info.getGBeanInfo().getClassName();
    }

    public SortedMap getAttributes() {
        TreeMap attributes = new TreeMap(info.getAttributes());
        for (Iterator iterator = attributes.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getValue() == null) {
                entry.setValue("(null)");
            }
            if (entry.getValue() instanceof Object[]) {
                entry .setValue(Arrays.asList((Object[])entry.getValue()));
            }
        }

        return attributes;
    }

    public Set getOperationInfo() {
        return info.getGBeanInfo().getOperations();
    }
}
