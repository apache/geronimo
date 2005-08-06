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
package org.apache.geronimo.gbean;

import java.io.Serializable;

/**
 * Criteria for querying for a list of GBeans.  Currently all criteria are
 * "ORed" (a GBean meeting any of them matches).
 *
 * @version $Rev: 209177 $ $Date: 2005-07-04 21:42:14 -0400 (Mon, 04 Jul 2005) $
 */
public class GBeanQuery implements Serializable {
    private String[] gbeanNames;
    private String[] interfaces;

    /**
     * A query that will be populated later by getters and setters.
     */
    public GBeanQuery() {
    }

    /**
     * A query with a single GBean name, single interface, or both.  Either
     * argument may be null.
     *
     * @param gbeanName A GBean name criterion, or null if there is none
     * @param interfaceName An object name criterion, or null if there is none
     */
    public GBeanQuery(String gbeanName, String interfaceName) {
        gbeanNames = gbeanName == null ? null : new String[]{gbeanName};
        interfaces = interfaceName == null ? null : new String[]{interfaceName};
    }

    /**
     * A query with zero or more GBean names and zero or more interfaces.
     * Either argument may be null.
     *
     * @param gbeanNames GBean name criteria, or null if there are none
     * @param interfaceNames Interface name criteria, or null if there are none
     */
    public GBeanQuery(String[] gbeanNames, String[] interfaceNames) {
        this.gbeanNames = gbeanNames;
        interfaces = interfaceNames;
    }

    public String[] getGBeanNames() {
        return gbeanNames;
    }

    public void setGBeanNames(String[] gbeanNames) {
        this.gbeanNames = gbeanNames;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(String[] interfaces) {
        this.interfaces = interfaces;
    }

    /**
     * Checks whether any criteria are present.
     */
    public boolean isCriteria() {
        return (gbeanNames != null && gbeanNames.length > 0) || (interfaces != null && interfaces.length > 0);
    }
}
