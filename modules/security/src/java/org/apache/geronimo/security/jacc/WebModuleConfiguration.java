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

package org.apache.geronimo.security.jacc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import javax.security.jacc.PolicyConfiguration;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.security.jacc.AbstractModuleConfiguration;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleMappingsType;
import org.apache.geronimo.xbeans.geronimo.security.GerRoleType;
import org.apache.geronimo.xbeans.geronimo.security.GerRealmType;
import org.apache.geronimo.xbeans.geronimo.security.GerPrincipalType;


/**
 * This es an GBean wrapper class that performs much of the utility work
 * needed to perform the translation of the web deployment descriptors
 * into equivalent security permissions.  These permissions are placed into
 * the appropriate <code>PolicyConfiguration</code> object as defined in the
 * JAAC spec.
 *
 * <p>It is expected that deployment tools will configure modules through
 * these utility MBeans and not directly access the
 * <code>PolicyConfiguration</code> objects.
 * @version $Revision: 1.2 $ $Date: 2004/02/12 08:14:05 $
 * @see javax.security.jacc.PolicyConfiguration
 * @see "Java Authorization Contract for Containers", section 3.1.3
 */
public class WebModuleConfiguration extends AbstractModuleConfiguration {

    private static final GBeanInfo GBEAN_INFO;

    private WebAppType webApp;
    private GerSecurityType security;

    public WebModuleConfiguration(String contextId, WebAppType webApp, GerSecurityType security) {
        super(contextId);
        this.webApp = webApp;
        this.security = security;
    }

    /**
     * Translate the web deployment descriptors into equivalent security
     * permissions.  These permissions are placed into the appropriate
     * <code>PolicyConfiguration</code> object as defined in the JAAC spec.
     * @throws org.apache.geronimo.security.GeronimoSecurityException if there is any violation of the semantics of
     * the security descriptor or the state of the module configuration.
     * @see javax.security.jacc.PolicyConfiguration
     * @see "Java Authorization Contract for Containers", section 3.1.3
     */
    public void doStart() {

        PolicyConfiguration configuration = getPolicyConfiguration();

        ConfigurationUtil.configure(configuration, webApp);
        setConfigured(true);

        //TODO not clear if schema allows/should allow security == null
        if (security != null) {
            GerRoleMappingsType roleMappings = security.getRoleMappings();
            if (roleMappings != null) {
                GerRoleType[] roles = roleMappings.getRoleArray();
                for (int i = 0; i < roles.length; i++) {
                    GerRoleType role = roles[i];
                    GerRealmType[] realms = role.getRealmArray();
                    for (int j = 0; j < realms.length; j++) {
                        GerRealmType realm = realms[j];
                        GerPrincipalType[] principals = realm.getPrincipalArray();
                        HashSet set = new HashSet();
                        for (int k = 0; k < principals.length; k++) {
                            GerPrincipalType principal = principals[k];
                            java.security.Principal p = null;
                            try {
                                Class clazz = Class.forName(principal.getClass1());
                                Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                                p = (java.security.Principal) constructor.newInstance(new Object[]{principal.getName()});
                                set.add(new RealmPrincipal(realm.getRealmName(), p));
                            } catch (InstantiationException e) {
                                throw new GeronimoSecurityException(e);
                            } catch (IllegalAccessException e) {
                                throw new GeronimoSecurityException(e);
                            } catch (ClassNotFoundException e) {
                                throw new GeronimoSecurityException(e);
                            } catch (NoSuchMethodException e) {
                                throw new GeronimoSecurityException(e);
                            } catch (InvocationTargetException e) {
                                throw new GeronimoSecurityException(e);
                            }
                        }
                        super.addRoleMapping(role.getRoleName(), set);
                    }
                }
            }
        }
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(WebModuleConfiguration.class.getName(), AbstractModuleConfiguration.getGBeanInfo());
        //TODO make sure this attribute not backed by a getter or setter works.
        infoFactory.addAttribute(new GAttributeInfo("WebApp", true));
        infoFactory.addAttribute(new GAttributeInfo("Security", true));
        infoFactory.setConstructor(new GConstructorInfo(
                new String[] {"ContextID", "WebApp", "Security"},
                new Class[] {String.class, WebAppType.class, GerSecurityType.class}));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
