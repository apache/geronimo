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

package org.apache.geronimo.security.jaas;

import javax.security.auth.login.AppConfigurationEntry;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;


/**
 * A simple GBean that allows servers to dynamically create JAAS login
 * configuration entries on the fly.  When <code>GeronimoLoginConfiguration</code>
 * has been registered via <code>Configuration.setConfiguration()</code>,
 * all logins will pick up these configuration entries and not what could
 * possibly be stored in a login config file referenced by
 * <code>java.security.auth.login.config</code>.
 * <p/>
 * <p>More specifically, you can only use this method or Sun's JAAS config
 * file.
 *
 * @version $Rev$ $Date$
 * @see GeronimoLoginConfiguration
 * @see javax.security.auth.login.Configuration
 */
public class ConfigurationEntryRealmLocal extends ConfigurationEntry {
    private String realmName;

    public ConfigurationEntryRealmLocal(Kernel kernel) {
        super(kernel);
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public AppConfigurationEntry[] getAppConfigurationEntry() {
        try {
            return new AppConfigurationEntry[]{
                new AppConfigurationEntry("org.apache.geronimo.security.jaas.JaasLoginCoordinator",
                        getControlFlag().getFlag(),
                        getOptions())};
        } catch (Exception e) {
        }
        return null;
    }

    public void doStart() throws WaitingException, Exception {
        super.doStart();

        options.put("realm", realmName);
        options.put("kernel", kernel.getKernelName());
    }

    public void doStop() throws WaitingException, Exception {
        super.doStop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(ConfigurationEntryRealmLocal.class, ConfigurationEntry.GBEAN_INFO);
        infoFactory.addAttribute("realmName", String.class, true);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
