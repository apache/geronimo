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
package org.apache.geronimo.system.configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.SwitchablePersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 *
 * @version $Rev: 486815 $ $Date: 2006-12-14 06:45:10 +1100 (Thu, 14 Dec 2006) $
 */

@GBean
public class SwitchableLocalAttributeManager extends LocalAttributeManager implements SwitchablePersistentConfigurationList {
    private boolean online;
    
    public SwitchableLocalAttributeManager(
            @ParamAttribute(name="configFile") String configFile,
            @ParamAttribute(name="substitutionsFile") String substitutionsFile,
            @ParamAttribute(name="configSubstitutionsPrefix") String configSubstitutionsPrefix,
            @ParamAttribute(name="readOnly") boolean readOnly,
            @ParamReference(name = "ServerInfo")ServerInfo serverInfo) {
        //TODO convert to delegation if this is actually needed.
//        super(configFile, substitutionsFile, configSubstitutionsPrefix, readOnly, serverInfo);
    }

    @Override
    public synchronized List<Artifact> restore() throws IOException {
        return Collections.emptyList();
    }

    public synchronized void setOnline(boolean online) {
        this.online = online;
    }

    public synchronized boolean isOnline() {
        return online;
    }

    @Override
    public synchronized void addConfiguration(Artifact configurationName) {
        if (online) {
            super.addConfiguration(configurationName);
        }
    }
    
    @Override
    public void startConfiguration(Artifact configurationName) {
        if (online) {
            super.startConfiguration(configurationName);
        }
    }
    
    @Override
    public void stopConfiguration(Artifact configName) {
        if (online) {
            super.stopConfiguration(configName);
        }
    }
    
    @Override
    public synchronized void removeConfiguration(Artifact configName) {
        if (online) {
            super.removeConfiguration(configName);
        }
    }
    
    @Override
    public void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration) {
        if (online) {
            super.migrateConfiguration(oldName, newName, configuration);
        }
    }
    
//    public static final GBeanInfo GBEAN_INFO;
//
//    static {
//        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SwitchableLocalAttributeManager.class);  //, LocalAttributeManager.GBEAN_INFO
//
//        infoFactory.addInterface(SwitchablePersistentConfigurationList.class);
//
//        GBEAN_INFO = infoFactory.getBeanInfo();
//    }
//
//    public static GBeanInfo getGBeanInfo() {
//        return GBEAN_INFO;
//    }
//
}
