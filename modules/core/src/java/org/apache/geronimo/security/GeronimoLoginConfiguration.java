/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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
 *        Apache Software Foundation (http:www.apache.org/)."
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
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security;

import org.apache.geronimo.kernel.jmx.MBeanProxyFactory;
import org.apache.geronimo.kernel.jmx.JMXUtil;

import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.AuthPermission;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;


/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/18 05:17:17 $
 */
public class GeronimoLoginConfiguration extends Configuration {
    private static MBeanServer mBeanServer;
    SecurityServiceMBean securityServiceMBean;

    public AppConfigurationEntry[] getAppConfigurationEntry(String realm) {
        if (mBeanServer == null) throw new java.lang.IllegalStateException("MBean Server not set");

        SecurityServiceMBean ss = (SecurityServiceMBean) MBeanProxyFactory.getProxy(SecurityServiceMBean.class,
                                                                                    mBeanServer,
                                                                                    JMXUtil.getObjectName("geronimo.security:type=SecurityService"));

        ArrayList list = new ArrayList();
        Iterator iter = ss.getRealms().iterator();
        while (iter.hasNext()) {
            ObjectInstance instance = (ObjectInstance) iter.next();

            SecurityRealm sr = (SecurityRealm) MBeanProxyFactory.getProxy(SecurityRealm.class, mBeanServer, instance.getObjectName());
            if (realm.equals(sr.getRealmName())) {
                AppConfigurationEntry[] ace = sr.getAppConfigurationEntry();

                for (int i = 0; i < ace.length; i++) {
                    AppConfigurationEntry entry = ace[i];
                    HashMap options = new HashMap();

                    options.putAll(entry.getOptions());
                    options.put(LoginModuleWrapper.REALM, realm);
                    options.put(LoginModuleWrapper.MODULE, entry.getLoginModuleName());

                    AppConfigurationEntry wrapper = new AppConfigurationEntry("org.apache.geronimo.security.LoginModuleWrapper",
                                                                              entry.getControlFlag(),
                                                                              options);
                    list.add(wrapper);
                }
                break;
            }
        }
        return (AppConfigurationEntry[]) list.toArray(new AppConfigurationEntry[0]);
    }

    public void refresh() {
    }

    /**
     * This sets the MBean server that the GeronimoLoginConfiguration is to use
     * when generating the AppConfigurationEntries.<p>
     *
     * todo This strikes me as kinda kludgy
     * @param server
     */
    public static void setMBeanServer(MBeanServer server) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new AuthPermission("setLoginConfiguration"));

        mBeanServer = server;
    }
}
