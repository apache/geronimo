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

import javax.management.MBeanServer;
import javax.security.auth.login.AppConfigurationEntry;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBeanContext;


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
 * @version $Revision: 1.3 $ $Date: 2004/05/30 01:27:35 $
 * @see org.apache.geronimo.security.jaas.GeronimoLoginConfiguration
 * @see javax.security.auth.login.Configuration
 */
public abstract class ConfigurationEntry implements GBean {

    private static final GBeanInfo GBEAN_INFO;

    protected GBeanMBeanContext context;
    protected String JAASId;
    protected LoginModuleControlFlag controlFlag;
    protected Map options = new HashMap();

    /**
     * Get the JAAS config id for this configuration entry.
     *
     * @return the JAAS config id for this configuration entry
     */
    public String getJAASId() {
        return JAASId;
    }

    /**
     * Set the JAAS config id for this configuration entry.
     *
     * @param JAASId the JAAS config id for this configuration entry
     */
    public void setJAASId(String JAASId) {
        this.JAASId = JAASId;
    }

    public LoginModuleControlFlag getControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(LoginModuleControlFlag controlFlag) {
        this.controlFlag = controlFlag;
    }

    public Map getOptions() {
        return options;
    }

    public void setOptions(Map options) {
        this.options = options;
    }

    public void setGBeanContext(GBeanContext context) {
        this.context = (GBeanMBeanContext) context;
    }

    public MBeanServer getMBeanServer() {
        return context.getServer();
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

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ConfigurationEntry.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("JAASId", true));
        infoFactory.addAttribute(new GAttributeInfo("ControlFlag", true));
        infoFactory.addAttribute(new GAttributeInfo("Options", true));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
