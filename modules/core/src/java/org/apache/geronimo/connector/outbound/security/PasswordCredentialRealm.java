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

package org.apache.geronimo.connector.outbound.security;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.security.AbstractSecurityRealm;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.SecurityRealm;
import org.apache.regexp.RE;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/22 06:39:24 $
 *
 * */
public class PasswordCredentialRealm implements SecurityRealm, ManagedConnectionFactoryListener {

    private static final GBeanInfo GBEAN_INFO;

    private String realmName;

    ManagedConnectionFactory managedConnectionFactory;

    static final String REALM_INSTANCE = "org.apache.connector.outbound.security.PasswordCredentialRealm";


    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public String getRealmName() {
        return realmName;
    }

    public Set getGroupPrincipals() throws GeronimoSecurityException {
        return null;
    }

    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException {
        return null;
    }

    public Set getUserPrincipals() throws GeronimoSecurityException {
        return null;
    }

    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException {
        return null;
    }

    public void refresh() throws GeronimoSecurityException {
    }

    public AppConfigurationEntry[] getAppConfigurationEntry() {
        Map options = new HashMap();
        options.put(REALM_INSTANCE, this);
        AppConfigurationEntry appConfigurationEntry = new AppConfigurationEntry(PasswordCredentialLoginModule.class.getName(),
                AppConfigurationEntry.LoginModuleControlFlag.REQUISITE,
                options);
        return new AppConfigurationEntry[] {appConfigurationEntry};
    }

    public void setManagedConnectionFactory(ManagedConnectionFactory managedConnectionFactory) {
        this.managedConnectionFactory = managedConnectionFactory;
    }

    ManagedConnectionFactory getManagedConnectionFactory() {
        return managedConnectionFactory;
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(PasswordCredentialRealm.class.getName(), AbstractSecurityRealm.getGBeanInfo());
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
