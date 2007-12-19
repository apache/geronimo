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
package org.apache.geronimo.monitoring;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;

public class MBeanHelper {
    
    /**
     * Return all MBeans that provide stats
     */
    public static Set<String> getStatsProvidersMBeans(Set<String> allMBeans) {
        Kernel kernel = KernelRegistry.getSingleKernel();
        Set<String> result = new HashSet();

        try {
            for (Iterator it = allMBeans.iterator(); it.hasNext(); ) {
                try {
                    String mbeanName = (String) it.next();
                    Boolean statisticsProvider = (Boolean) kernel.getAttribute(new ObjectName(mbeanName), "statisticsProvider");
                    if (Boolean.TRUE.equals(statisticsProvider)) {
                        result.add(mbeanName);
                    }
                } catch (Exception e) {
                    // this will happen if there is not a matching attribute "statisticsProvider"
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
