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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeMBeanException;

/**
 * Simple helper bean for dealing with MBeanInfo.  Helps dodge such
 * wacky APIs like  HashMap getKeyPropertyLIst() and wrap in
 * convenient ways for working in Velocity
 * 
 * @version $Id: MBeanInfoHelper.java,v 1.2 2004/07/26 17:14:48 dain Exp $
 */
public class MBeanInfoHelper {
    private final ObjectName objectName;
    private final MBeanInfo info;
    private final MBeanServer server;

    public MBeanInfoHelper(MBeanServerHelper kernelHelper, String name) throws Exception {
        server = kernelHelper.getKernel().getMBeanServer();
        if (server != null) {
            objectName = new ObjectName(name);
            info = server.getMBeanInfo(objectName);
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

    public String getDescription() {
        return info.getDescription();
    }

    public String getClassName() {
        return info.getClassName();
    }

    public SortedMap getAttributes() {
        TreeMap attributes = new TreeMap();

        MBeanAttributeInfo[] arr = info.getAttributes();

        for (int i = 0; i < arr.length; i++) {
            MBeanAttributeInfo attribute = arr[i];
            String name = attribute.getName();

            if ((!attribute.isReadable() && !attribute.isWritable()) || name.startsWith("$")) {
                // hide attributes that are not readable or writable or start with a '$'
                continue;
            }

            Object value = null;
            if (attribute.isReadable()) {
                try {
                    value = server.getAttribute(objectName, name);
                } catch (MBeanException e) {
                    e.printStackTrace();
                } catch (AttributeNotFoundException e) {
                    e.printStackTrace();
                } catch (InstanceNotFoundException e) {
                    e.printStackTrace();
                } catch (ReflectionException e) {
                    e.printStackTrace();
                } catch (RuntimeMBeanException rme) {
                    rme.printStackTrace();
                }
            }

            try {
                AttributeData attributeData = new AttributeData(attribute, value, server.getClassLoaderFor(objectName));
                attributes.put(name, attributeData);
            } catch (Exception e) {
                // ignore; just hide weird attributes
            }
        }

        return attributes;
    }

    public MBeanOperationInfo[] getOperationInfo() {
        return info.getOperations();
    }
}
