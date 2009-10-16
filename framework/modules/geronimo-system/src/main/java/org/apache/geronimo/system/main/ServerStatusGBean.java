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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;


/**
 * @version $Rev:385659 $ $Date: 2007-03-07 14:40:07 +1100 (Wed, 07 Mar 2007) $
 */
public class ServerStatusGBean implements ServerStatus {
    public static final String SERVER_STATUS = "ServerStatus";
    private boolean serverStarted = false;

    public boolean isServerStarted() {
        return serverStarted;
    }

    public void setServerStarted(boolean flag) {
        serverStarted = flag;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SERVER_STATUS, ServerStatusGBean.class);
        infoFactory.addAttribute("serverStarted", Boolean.TYPE, true);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}

