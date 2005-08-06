/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.j2ee.management.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.DependencyManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

/**
 * @version $Rev$ $Date$
 */
public class Util {
    private final static Log log = LogFactory.getLog(Util.class);

    public static String[] getObjectNames(Kernel kernel, J2eeContext context, String[] j2eeTypes) throws MalformedObjectNameException {
        List objectNames = new LinkedList();
        for (int i = 0; i < j2eeTypes.length; i++) {
            String j2eeType = j2eeTypes[i];
            ObjectName query = NameFactory.getComponentInModuleQuery(null, null, null, null, null, j2eeType,  context);
            objectNames.addAll(kernel.listGBeans(query));
        }
        String[] names = new String[objectNames.size()];
        Iterator iterator = objectNames.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            names[i] = iterator.next().toString();
        }
        return names;
    }

    public static String[] getObjectNames(Kernel kernel, Object parentName, String[] j2eeTypes) {
        List objectNames = new LinkedList();
        for (int i = 0; i < j2eeTypes.length; i++) {
            String j2eeType = j2eeTypes[i];
            String name = parentName + "j2eeType=" + j2eeType + ",*";
            try {
                objectNames.addAll(kernel.listGBeans(new ObjectName(name)));
            } catch (MalformedObjectNameException e) {
                throw new IllegalArgumentException("Malformed ObjectName: " + name);
            }
        }
        String[] names = new String[objectNames.size()];
        Iterator iterator = objectNames.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            names[i] = iterator.next().toString();
        }
        return names;
    }

    /**
     * Gets a Configuration that is the parent of the specified object.
     *
     * @param objectName the bean to find the Configuration for
     * @return the Configuration the bean is in, or null if it is not in a Configuration
     */
    public synchronized static ObjectName getConfiguration(Kernel kernel, ObjectName objectName) {
        DependencyManager mgr = kernel.getDependencyManager();
        Set parents = mgr.getParents(objectName);
        if(parents == null || parents.isEmpty()) {
            log.warn("No parents found for "+objectName);
            return null;
        }
        for (Iterator it = parents.iterator(); it.hasNext();) {
            ObjectName name = (ObjectName) it.next();
            try {
                GBeanInfo info = kernel.getGBeanInfo(name);
                if(info.getClassName().equals(Configuration.class.getName())) {
                    return name;
                }
            } catch (GBeanNotFoundException e) {} // should never happen
        }
        log.warn("No Configuration parent found");
        return null;
    }
}
