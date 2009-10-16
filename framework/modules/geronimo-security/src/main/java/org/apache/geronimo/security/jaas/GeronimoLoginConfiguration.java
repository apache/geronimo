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

package org.apache.geronimo.security.jaas;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.SecurityNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A JAAS configuration mechanism (associating JAAS configuration names with
 * specific LoginModule configurations).  This is a drop-in replacement for the
 * normal file-reading JAAS configuration mechanism.  Instead of getting
 * its configuration from its file, it gets its configuration from other
 * GBeans running in Geronimo.
 *
 * @version $Rev$ $Date$
 */
@GBean
public class GeronimoLoginConfiguration extends Configuration implements GBeanLifecycle, ReferenceCollectionListener {
    private static final Logger log = LoggerFactory.getLogger(GeronimoLoginConfiguration.class);
    private final Map<String, AppConfigurationEntry[]> entries = new ConcurrentHashMap<String, AppConfigurationEntry[]>();
    private Configuration oldConfiguration;
    private final Collection<ConfigurationEntryFactory> configurations;
    private final boolean useAllConfigurations;

    public GeronimoLoginConfiguration(@ParamReference(name="Configurations", namingType = SecurityNames.SECURITY_REALM)Collection<ConfigurationEntryFactory> configurations,
                                      @ParamAttribute(name="useAllConfigurations") boolean useAllConfigurations) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityServiceImpl.CONFIGURE);

        if (configurations instanceof ReferenceCollection) {
            ReferenceCollection ref = (ReferenceCollection) configurations;
            ref.addReferenceCollectionListener(this);
        }

        this.configurations = configurations;
        this.useAllConfigurations = useAllConfigurations;

        for (ConfigurationEntryFactory configuration : configurations) {
            addConfiguration(configuration);
        }
    }

    public Collection<ConfigurationEntryFactory> getConfigurations() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityServiceImpl.CONFIGURE);

        return configurations;
    }

    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        return entries.get(name);
    }

    public void refresh() {
    }

    public void memberAdded(ReferenceCollectionEvent event) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityServiceImpl.CONFIGURE);

        ConfigurationEntryFactory factory = (ConfigurationEntryFactory) event.getMember();
        addConfiguration(factory);
    }

    public void memberRemoved(ReferenceCollectionEvent event) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityServiceImpl.CONFIGURE);

        ConfigurationEntryFactory factory = (ConfigurationEntryFactory) event.getMember();

        entries.remove(factory.getConfigurationName());
        log.debug("Removed Application Configuration Entry " + factory.getConfigurationName());
    }

    private void addConfiguration(ConfigurationEntryFactory factory) {
        if (useAllConfigurations || factory.isGlobal()) {
            if (entries.containsKey(factory.getConfigurationName())) {
                throw new java.lang.IllegalArgumentException("ConfigurationEntry named: " + factory.getConfigurationName() + " already registered");
            }
            AppConfigurationEntry[] ace = factory.getAppConfigurationEntries();
            entries.put(factory.getConfigurationName(), ace);
            log.debug("Added Application Configuration Entry " + factory.getConfigurationName());
        }
    }

    public void doStart() throws Exception {
        try {
            oldConfiguration = Configuration.getConfiguration();
        } catch (SecurityException e) {
            oldConfiguration = null;
        }
        Configuration.setConfiguration(this);
        log.debug("Installed Geronimo login configuration");
    }

    public void doStop() throws Exception {
        Configuration.setConfiguration(oldConfiguration);

        for (String s : entries.keySet()) {
            log.debug("Removed Application Configuration Entry " + s);
        }
        entries.clear();

        log.debug("Uninstalled Geronimo login configuration");
    }

    public void doFail() {
        Configuration.setConfiguration(oldConfiguration);
        log.debug("Uninstalled Geronimo login configuration");
    }

}
