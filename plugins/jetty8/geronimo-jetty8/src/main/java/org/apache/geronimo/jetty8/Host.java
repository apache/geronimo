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
package org.apache.geronimo.jetty8;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;

/**
 * Host gbean for jetty8 containing an array of hosts and virtual hosts
 */
@GBean
public class Host {

    private final String[] hosts;
    private final String[] virtualHosts;

    public Host(@ParamAttribute(name="hosts")String[] hosts, 
                @ParamAttribute(name="virtualHosts")String[] virtualHosts) {
        this.hosts = hosts;
        this.virtualHosts = virtualHosts;
    }

    public String[] getHosts() {
        return hosts;
    }

    public String[] getVirtualHosts() {
        return virtualHosts;
    }

}
