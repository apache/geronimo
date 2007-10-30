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

    private final Map principalRoleMap;
    private final Map<String, SubjectInfo> roleDesignates;
    private final SubjectInfo defaultSubjectInfo;
    private final String defaultRole;
    private final boolean doAsCurrentCaller;
    private final boolean isUseContextHandler;

    public SecurityConfiguration(Map principalRoleMap, Map<String, SubjectInfo> roleDesignates, SubjectInfo defaultSubjectInfo, String defaultRole, boolean doAsCurrentCaller, boolean useContextHandler) {
        this.principalRoleMap = principalRoleMap;
        this.roleDesignates = roleDesignates;
        this.defaultSubjectInfo = defaultSubjectInfo;
        this.defaultRole = defaultRole;
        this.doAsCurrentCaller = doAsCurrentCaller;
        isUseContextHandler = useContextHandler;
    }

    public Map getPrincipalRoleMap() {
        return principalRoleMap;
    }

    public Map<String, SubjectInfo> getRoleDesignates() {
        return roleDesignates;
    }

    public SubjectInfo getDefaultSubjectInfo() {
        return defaultSubjectInfo;
    }

    public String getDefaultSubjectRealm() {
        return defaultSubjectInfo == null? null: defaultSubjectInfo.getRealm();
    }

    public String getDefaultSubjectId() {
        return defaultSubjectInfo == null? null: defaultSubjectInfo.getId();
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
}
