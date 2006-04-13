/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.util;

import java.util.Map;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;

public class SecurityRealmBean {

    private ObjectName realmObjectName, configEntryObjName;

    private String configID, realmName, applicationConfigName;

    private boolean running;

    private Map attributes;

    public SecurityRealmBean(ObjectName realmObjectName,
            ObjectName configEntryObjName, String configID,
            String applicationConfigName, String realmName, boolean started,
            Map attributes) {
        this.realmObjectName = realmObjectName;
        this.configEntryObjName = configEntryObjName;
        this.configID = configID;
        this.applicationConfigName = applicationConfigName;
        this.realmName = realmName;
        this.running = started;
        this.attributes = attributes;
    }

    public SecurityRealmBean(String realmObjectName, String configEntryObjName,
            String configID, String applicationConfigName, String realmName,
            boolean started, Map attributes) {
        this(JMXUtil.getObjectName(realmObjectName), JMXUtil
                .getObjectName(configEntryObjName), configID,
                applicationConfigName, realmName, started, attributes);
    }

    /**
     * @return Returns the configEntryObjName.
     */
    public ObjectName getConfigEntryObjName() {
        return configEntryObjName;
    }

    /**
     * @return Returns the applicationConfigName.
     */
    public String getApplicationConfigName() {
        return applicationConfigName;
    }

    /**
     * @return Returns the attributes.
     */
    public Map getAttributes() {
        return attributes;
    }

    /**
     * @return Returns the configID.
     */
    public String getConfigID() {
        return configID;
    }

    /**
     * @return Returns the realmName.
     */
    public String getRealmName() {
        return realmName;
    }

    /**
     * @return Returns the realmObjectName.
     */
    public ObjectName getRealmObjectName() {
        return realmObjectName;
    }

    /**
     * @return Returns the running.
     */
    public boolean isRunning() {
        return running;
    }

}
