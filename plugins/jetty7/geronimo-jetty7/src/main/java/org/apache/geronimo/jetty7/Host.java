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
package org.apache.geronimo.jetty7;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * Host gbean for jetty7 containing an array of hosts and virtual hosts
 */
public class Host {

    private final String[] hosts;
    private final String[] virtualHosts;

    public Host() {
        hosts = null;
        virtualHosts = null;
    }

    public Host(String[] hosts, String[] virtualHosts) {
        this.hosts = hosts;
        this.virtualHosts = virtualHosts;
    }

    public String[] getHosts() {
        return hosts;
    }

    public String[] getVirtualHosts() {
        return virtualHosts;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(Host.class, "Host");
        infoBuilder.addAttribute("hosts", String[].class, true);
        infoBuilder.addAttribute("virtualHosts", String[].class, true);
        infoBuilder.setConstructor(new String[] {"hosts", "virtualHosts"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
