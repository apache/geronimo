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
package org.apache.geronimo.naming.deployment;

import java.util.Set;

import org.apache.geronimo.gbean.GBeanData;

/**
 * @version $Revision$ $Date$
 */
public class GBeanResourceEnvironmentBuilder implements ResourceEnvironmentBuilder {

    private final GBeanData gbean;

    public GBeanResourceEnvironmentBuilder(GBeanData gbean) {
        this.gbean = gbean;
    }

    public Set getUnshareableResources() {
        return (Set) gbean.getAttribute("unshareableResources");
    }

    public void setUnshareableResources(Set unshareableResources) {
        gbean.setAttribute("unshareableResources", unshareableResources);
    }

    public Set getApplicationManagedSecurityResources() {
        return (Set) gbean.getAttribute("applicationManagedSecurityResources");
    }

    public void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources) {
        gbean.setAttribute("applicationManagedSecurityResources", applicationManagedSecurityResources);
    }
}
