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
package org.apache.geronimo.security.deployment;

import java.util.Map;

import org.apache.geronimo.security.deploy.SubjectInfo;

/**
 * @version $Rev$ $Date$
 */
public class SecurityConfiguration {

    public static SecurityConfiguration DEFAULT_SECURITY_CONFIGURATION = new SecurityConfiguration(null, false, false);

    private final String defaultRole;
    private final boolean doAsCurrentCaller;
    private final boolean isUseContextHandler;

    public SecurityConfiguration(String defaultRole, boolean doAsCurrentCaller, boolean useContextHandler) {
        this.defaultRole = defaultRole;
        this.doAsCurrentCaller = doAsCurrentCaller;
        isUseContextHandler = useContextHandler;
    }

    public String getDefaultRole() {
        return defaultRole;
    }

    public boolean isDoAsCurrentCaller() {
        return doAsCurrentCaller;
    }

    public boolean isUseContextHandler() {
        return isUseContextHandler;
    }

    public boolean isDefault() {
        return this == DEFAULT_SECURITY_CONFIGURATION;
    }
}
