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

package org.apache.geronimo.security.jaas;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.jaas.server.JaasLoginModuleConfiguration;


/**
 * A JAAS configuration mechanism (associating JAAS configuration names with
 * specific LoginModule configurations).  This is a drop-in replacement for the
 * normal file-reading JAAS configuration mechanism.  Instead of getting
 * its configuration from its file, it gets its configuration from other
 * GBeans running in Geronimo.
 *
 * @version $Rev$ $Date$
 */
public class GeronimoLoginConfiguration extends Configuration implements GBeanLifecycle, ReferenceCollectionListener {

    private final Log log = LogFactory.getLog(GeronimoLoginConfiguration.class);
    private static Map entries = new Hashtable();
    private Configuration oldConfiguration;
    private Collection configurations = Collections.EMPTY_SET;


    public Collection getConfigurations() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityServiceImpl.CONFIGURE);

        return configurations;
    }

    public void setConfigurations(Collection configurations) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(SecurityServiceImpl.CONFIGURE);

        ReferenceCollection ref = (ReferenceCollection) configurations;
        ref.addReferenceCollectionListener(this);

        this.configurations = configurations;

        for (Iterator iter = configurations.iterator(); iter.hasNext();) {
            addConfiguration((ConfigurationEntryFactory) iter.next());
        }
    }

    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        AppConfigurationEntry entry = (AppConfigurationEntry) entries.get(name);

        if (entry == null) return null;

        return new AppConfigurationEntry[]{entry};
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

    private final void addConfiguration(ConfigurationEntryFactory factory) {
        JaasLoginModuleConfiguration config = factory.generateConfiguration();
        if(config.getLoginDomainName() == null) {
            throw new IllegalArgumentException("A login module to be registered standalone must have a domain name!");
        }
        if (entries.containsKey(factory.getConfigurationName())) {
            throw new java.lang.IllegalArgumentException("ConfigurationEntry already registered");
        }
        AppConfigurationEntry ace = new AppConfigurationEntry(config.getLoginModuleClassName(), config.getFlag().getFlag(), config.getOptions());

        entries.put(factory.getConfigurationName(), ace);
        log.debug("Added Application Configuration Entry " + factory.getConfigurationName());
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

        for (Iterator iter = entries.keySet().iterator(); iter.hasNext();){
            log.debug("Removed Application Configuration Entry " + iter.next());
        }
        entries.clear();

        log.debug("Uninstalled Geronimo login configuration");
    }

    public void doFail() {
        Configuration.setConfiguration(oldConfiguration);
        log.debug("Uninstalled Geronimo login configuration");
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(GeronimoLoginConfiguration.class); //just a gbean
        infoFactory.addReference("Configurations", ConfigurationEntryFactory.class, null);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

}
