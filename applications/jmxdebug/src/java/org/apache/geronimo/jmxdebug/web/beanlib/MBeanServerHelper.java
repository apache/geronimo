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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.jmxdebug.util.ObjectNameComparator;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;

/**
 * Little helper bean to deal w/ the mbean server
 *
 * @version $Rev$ $Date$
 */
public class MBeanServerHelper {
    private final Kernel kernel;

    public MBeanServerHelper() {
        kernel = Kernel.getSingleKernel();
    }

    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Returns a Collection of InstanceObjects for all mbeans in the server
     *
     * @return Collection of InstanceObjects
     */
    public Collection getMBeans() {
        return getMBeans("*:*");
    }

    /**
     * Returns a Collection of InstanceObjects filtered by the input filter
     *
     * @param filterString filter to use.  Defaults to *:* if null
     * @return Collection of InstanceObjects that match the filter
     */
    public Collection getMBeans(String filterString) {
        if (filterString == null) {
            filterString = "*:*";
        }

        if (kernel != null) {
            ObjectName filter = null;
            try {
                filter = new ObjectName(filterString);
                Set names = kernel.listGBeans(filter);

                List sortedNames = new ArrayList(names);
                Collections.sort(sortedNames, ObjectNameComparator.INSTANCE);

                return sortedNames;
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("MBeanServerHelper : error : no mbean server");
        }

        return null;
    }

    public String getState(ObjectName name) {
        try {
            int state = ((Integer) kernel.getAttribute(name, "state")).intValue();
            return State.toString(state);
        } catch (Exception e) {
            return null;
        }
    }
}
