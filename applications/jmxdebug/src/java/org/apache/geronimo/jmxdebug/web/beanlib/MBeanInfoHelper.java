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

import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.RuntimeMBeanException;
import java.util.Map;
import java.util.List;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Simple helper bean for dealing with MBeanInfo.  Helps dodge such
 * wacky APIs like  HashMap getKeyPropertyLIst() and wrap in
 * convenient ways for working in Velocity
 * 
 * @version $Id: MBeanInfoHelper.java,v 1.1 2004/02/18 15:33:09 geirm Exp $
 */
public class MBeanInfoHelper {

    ObjectName oName;
    MBeanInfo info;

    public MBeanInfoHelper(String name) {
        MBeanServer server = MBeanServerHelper.getMBeanServer();

        if (server != null) {
            init(server, name);
        }
    }

    void init(MBeanServer server, String name) {
        try {
            oName = new ObjectName(name);
            info = server.getMBeanInfo(oName);
        }
        catch (MalformedObjectNameException e) {
            e.printStackTrace();
        }
        catch (ReflectionException e) {
            e.printStackTrace();
        }
        catch (InstanceNotFoundException e) {
            e.printStackTrace();
        }
        catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    public String getCanonicalName() {
        return oName.getCanonicalName();
    }

    public String getDomain() {
        return oName.getDomain();
    }

    /**
     * Returns the key properties and values a list of
     * maps, w/ 'key' and 'value' as entryies in each
     * map.  Makes easy in vel to do
     * #foreach($item in $list)
     * $item.key
     * $item.value
     * #end
     *
     * @return
     */
    public List getKeyProperties() {
        Hashtable h = oName.getKeyPropertyList();

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

    public List getAttributes() {
        
        List l = new ArrayList();
        MBeanAttributeInfo[] arr = info.getAttributes();
        MBeanServer server = MBeanServerHelper.getMBeanServer();

        for (int i = 0; i < arr.length; i++) {
            MBeanAttributeInfo foo = arr[i];
            Object value = null;
            try {
                value = server.getAttribute(oName, foo.getName());
            }
            catch (MBeanException e) {
                e.printStackTrace();
            }
            catch (AttributeNotFoundException e) {
                e.printStackTrace();
            }
            catch (InstanceNotFoundException e) {
                e.printStackTrace();
            }
            catch (ReflectionException e) {
                e.printStackTrace();
            }
            catch (RuntimeMBeanException rme) {
                rme.printStackTrace();
            }

            Map m = new HashMap();
            m.put("info", foo);
            m.put("value", value);
            l.add(m);
        }

        return l;
    }


    public MBeanOperationInfo[] getOperationInfo() {
        MBeanOperationInfo foo;

        return info.getOperations();
    }

}
