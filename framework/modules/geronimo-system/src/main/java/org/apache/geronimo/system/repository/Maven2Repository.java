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
package org.apache.geronimo.system.repository;

import java.io.File;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class Maven2Repository extends org.apache.geronimo.kernel.repository.Maven2Repository {

    private static final Logger log = LoggerFactory.getLogger(Maven2Repository.class);
    
    public Maven2Repository(URI root, ServerInfo serverInfo, boolean resolveToServer) {
        super(new ServerInfoRootResolver(serverInfo, resolveToServer).resolve(root));
        
        log.debug("Maven2Repository(root = {}, resolveToServer = {}) rootFile = {}", new Object[] { root, resolveToServer, rootFile });
    }

    public Maven2Repository(File rootFile) {
        super(rootFile);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(Maven2Repository.class, "Repository");
        infoFactory.addAttribute("root", URI.class, true);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addAttribute("resolveToServer", Boolean.TYPE, true);
        infoFactory.addInterface(Maven2Repository.class);
        infoFactory.setConstructor(new String[]{"root", "ServerInfo", "resolveToServer"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
