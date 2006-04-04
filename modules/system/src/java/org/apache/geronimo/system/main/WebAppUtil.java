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
            AbstractName mgrName = (AbstractName) it.next();
            AbstractName[] cntNames = (AbstractName[]) kernel.getAttribute(mgrName, "containers");
            for (int i = 0; i < cntNames.length; i++) {
                AbstractName cntName = cntNames[i];
                AbstractName[] cncNames = (AbstractName[]) kernel.invoke(mgrName, "getConnectorsForContainer", new Object[]{cntName}, new String[]{AbstractName.class.getName()});
                Map map = new HashMap();
                for (int j = 0; j < cncNames.length; j++) {
                    AbstractName cncName = cncNames[j];
                    String protocol = (String) kernel.getAttribute(cncName, "protocol");
                    String url = (String) kernel.getAttribute(cncName, "connectUrl");
                    map.put(protocol, url);
                }
                String urlPrefix = "";
                if((urlPrefix = (String) map.get("HTTP")) == null) {
                    if((urlPrefix = (String) map.get("HTTPS")) == null) {
                        urlPrefix = (String) map.get("AJP");
                    }
                }
                containers.put(cntName.getObjectName().getCanonicalName(), urlPrefix);
            }
        }
        return containers;
    }
}
