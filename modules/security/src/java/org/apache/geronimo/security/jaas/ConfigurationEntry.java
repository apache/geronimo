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

import java.util.Properties;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBeanContext;
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
 * @version $Revision: 1.10 $ $Date: 2004/06/05 07:53:22 $
 * @see org.apache.geronimo.security.jaas.GeronimoLoginConfiguration
 * @see javax.security.auth.login.Configuration
 */
public abstract class ConfigurationEntry implements GBeanLifecycle {
    protected final Kernel kernel;
    protected GBeanMBeanContext context;
    protected String applicationConfigName;
    protected LoginModuleControlFlag controlFlag;
    protected Properties options = new Properties();

    protected ConfigurationEntry(Kernel kernel) {
        this.kernel = kernel;
    }

    /**
     * Get the JAAS config id for this configuration entry.
     *
     * @return the JAAS config id for this configuration entry
     */
    public String getApplicationConfigName() {
        return applicationConfigName;
    }

    /**
     * Set the JAAS application configuration name for this configuration entry.
     *
     * @param applicationConfigName the JAAS config id for this configuration entry
     */
    public void setApplicationConfigName(String applicationConfigName) {
        this.applicationConfigName = applicationConfigName;
    }

    public LoginModuleControlFlag getControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(LoginModuleControlFlag controlFlag) {
        this.controlFlag = controlFlag;
    }

    public Properties getOptions() {
        return options;
    }

    public void setOptions(Properties options) {
        this.options = options == null ? new Properties() : options;
    }

    public abstract AppConfigurationEntry[] getAppConfigurationEntry();

    public void doStart() throws WaitingException, Exception {
        GeronimoLoginConfiguration.register(this);
    }

    public void doStop() throws WaitingException, Exception {
        GeronimoLoginConfiguration.unRegister(this);
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConfigurationEntry.class);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("ApplicationConfigName", String.class, true);
        infoFactory.addAttribute("ControlFlag", LoginModuleControlFlag.class, true);
        infoFactory.addAttribute("Options", Properties.class, true);
        infoFactory.setConstructor(new String[]{"kernel"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
