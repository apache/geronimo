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
package org.apache.geronimo.mail;

import javax.mail.Authenticator;
import javax.mail.Session;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;


/**
 * GBean that provides access to JavaMail Sessions.
 *
 * @version $Rev: $ $Date: $
 */
public class MailGBean implements GBeanLifecycle {

    private final Log log = LogFactory.getLog(MailGBean.class);

    private boolean useDefault = false;
    private Properties properties = new Properties();
    private Authenticator authenticator;


    public boolean getUseDefault() {
        return useDefault;
    }

    public void setUseDefault(boolean useDefault) {
        this.useDefault = useDefault;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public Object $getResource() {
        if (useDefault) {
            if (authenticator == null) {
                return Session.getDefaultInstance(properties);
            } else {
                return Session.getDefaultInstance(properties, authenticator);
            }
        } else {
            if (authenticator == null) {
                return Session.getInstance(properties);
            } else {
                return Session.getInstance(properties, authenticator);
            }
        }
    }

    public void doStart() throws WaitingException, Exception {
        log.info("Started - will return "
                 + (useDefault ? "default" : "new")
                 + " instance "
                 + (authenticator == null ? "without" : "with")
                 + " authenticator");
    }

    public void doStop() throws WaitingException, Exception {
        log.info("Stopped");
    }

    public void doFail() {
        log.info("Failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(MailGBean.class);

        infoFactory.addAttribute("useDefault", Boolean.TYPE, true);
        infoFactory.addAttribute("properties", Properties.class, true);
        infoFactory.addReference("Authenticator", Authenticator.class);
        infoFactory.addOperation("$getResource");

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
