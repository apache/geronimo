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
package org.apache.geronimo.j2ee.j2eeobjectnames;

import javax.management.ObjectName;

/**
 * @version $Rev: $ $Date: $
 */
public class J2eeContextImpl implements J2eeContext {

    private final String domainName;
    private final String serverName;
    private final String applicationName;
    private final String moduleName;
    private final String j2eeName;
    private final String j2eeType;

    public J2eeContextImpl(String domainName, String serverName, String applicationName, String moduleName, String j2eeName, String j2eeType) {
        this.domainName = domainName;
        this.serverName = serverName;
        this.applicationName = applicationName;
        this.moduleName = moduleName;
        this.j2eeName = j2eeName;
        this.j2eeType = j2eeType;
    }

    public J2eeContextImpl(ObjectName source, String moduleType) {
        this.domainName = source.getDomain();
        this.serverName = source.getKeyProperty(NameFactory.J2EE_SERVER);
        this.applicationName = source.getKeyProperty(NameFactory.J2EE_APPLICATION);
        this.moduleName = source.getKeyProperty(moduleType);
        this.j2eeType = source.getKeyProperty(NameFactory.J2EE_TYPE);
        this.j2eeName = source.getKeyProperty(NameFactory.J2EE_NAME);
    }


    public String getJ2eeDomainName() {
        return domainName;
    }

    public String getJ2eeServerName() {
        return serverName;
    }

    public String getJ2eeApplicationName() {
        return applicationName;
    }

    public String getJ2eeModuleName() {
        return moduleName;
    }

    public String getJ2eeName() {
        return j2eeName;
    }

    public String getJ2eeType() {
        return j2eeType;
    }

    public String getJ2eeDomainName(String override) {
        return override == null? domainName: override;
    }

    public String getJ2eeServerName(String override) {
        return override == null? serverName: override;
    }

    public String getJ2eeApplicationName(String override) {
        return override == null? applicationName: override;
    }

    public String getJ2eeModuleName(String override) {
        return override == null? moduleName: override;
    }

    //most likely the last 2 don't make any sense.
    public String getJ2eeName(String override) {
        return override == null? j2eeName: override;
    }

    public String getJ2eeType(String override) {
        return override == null? j2eeType: override;
    }
}
