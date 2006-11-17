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
package org.apache.geronimo.system.main;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;

/**
 * Utility functions for dealing with web applications
 *
 * @version $Rev$ $Date$
 */
public class WebAppUtil {
    /**
     * Generates a Map where the keys are web container object names (as Strings)
     * and the values are URLs (as Strings) to connect to a web app running in
     * the matching container (though the web app context needs to be added to
     * the end to be complete).
     *
     * NOTE: same as a method in geronimo-jsr88 CommandSupport, but neither
     *       module should obviously be dependent on the other and it's not
     *       clear that this belongs in geronimo-common
     */
    public static Map mapContainersToURLs(Kernel kernel) throws Exception {
        Map containers = new HashMap();
        Set set = kernel.listGBeans(new AbstractNameQuery("org.apache.geronimo.management.geronimo.WebManager"));
        for (Iterator it = set.iterator(); it.hasNext();) {
            AbstractName manager = (AbstractName) it.next();
            Map results = (Map)kernel.invoke(manager, "mapContainersToURLs");
            containers.putAll(results);
        }
        return containers;
    }
}
