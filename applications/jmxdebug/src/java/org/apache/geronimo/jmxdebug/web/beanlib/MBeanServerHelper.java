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

import org.apache.geronimo.jmxdebug.util.ObjectInstanceComparator;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import java.util.List;
import java.util.Collection;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Little helper bean to deal w/ the mbean server
 *
 * @version $Id: MBeanServerHelper.java,v 1.1 2004/02/18 15:33:09 geirm Exp $
 */
public class MBeanServerHelper {
    final private MBeanServer server;

    public MBeanServerHelper() {
        this.server = getMBeanServer();
    }

    /**
     * Returns the mbean server.  Hokey as we just take the first
     * one...
     *
     * @return
     */
    public static MBeanServer getMBeanServer() {
        List l = MBeanServerFactory.findMBeanServer(null);

        if (l.size() > 0) {
            return (MBeanServer) l.get(0);
        }

        return null;
    }

    /**
     *  Returns a Collection of InstanceObjects for all mbeans in the server
     *
     * @return Collection of InstanceObjects
     */
    public Collection getMBeans() {
        return getMBeans("*:*");
    }

    /**
     *   Returns a Collection of InstanceObjects filtered by the input
     *   filter
     *
     * @param filterString  filter to use.  Defaults to *:* if null
     * @return Collection of InstanceObjects that match the filter
     */
    public Collection getMBeans(String filterString) {

        if (server != null) {
            ObjectName objectName = null;
            try {
                objectName = new ObjectName((filterString == null ? "*:*" : filterString));
                Set s = server.queryMBeans(objectName, null);

                List list = new ArrayList();
                list.addAll(s);
                ObjectInstanceComparator comparator = new ObjectInstanceComparator();
                Collections.sort(list, comparator);

                return list;
            }
            catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("MBeanServerHelper : error : no mbean server");
        }

        return null;
    }
}
