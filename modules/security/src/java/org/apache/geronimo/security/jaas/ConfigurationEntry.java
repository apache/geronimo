/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security.jaas;

import javax.management.MBeanServer;
import javax.security.auth.login.AppConfigurationEntry;

import java.util.Collections;
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
 * @version $Revision: 1.1 $ $Date: 2004/02/17 00:05:39 $
 * @see org.apache.geronimo.security.jaas.GeronimoLoginConfiguration
 * @see javax.security.auth.login.Configuration
 */
public abstract class ConfigurationEntry implements GBean {

    private static final GBeanInfo GBEAN_INFO;

    protected GBeanMBeanContext context;
    protected String JAASId;
    protected AppConfigurationEntry.LoginModuleControlFlag controlFlag;
    protected Map options = Collections.EMPTY_MAP;

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

    public AppConfigurationEntry.LoginModuleControlFlag getControlFlag() {
        return controlFlag;
    }

    public void setControlFlag(AppConfigurationEntry.LoginModuleControlFlag controlFlag) {
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
