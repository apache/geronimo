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

package org.apache.geronimo.security.jacc;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import javax.security.jacc.PolicyConfiguration;

import noNamespace.PrincipalType;
import noNamespace.RealmType;
import noNamespace.RoleMappingsType;
import noNamespace.RoleType;
import noNamespace.SecurityType;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.xbeans.j2ee.WebAppType;


/**
 * This es an GBean wrapper class that performs much of the utility work
 * needed to perform the translation of the web deployment descriptors
 * into equivalent security permissions.  These permissions are placed into
 * the appropriate <code>PolicyConfiguration</code> object as defined in the
 * JAAC spec.
 * <p/>
 * <p>It is expected that deployment tools will configure modules through
 * these utility MBeans and not directly access the
 * <code>PolicyConfiguration</code> objects.
 *
 * @version $Rev$ $Date$
 * @see javax.security.jacc.PolicyConfiguration
 * @see "Java Authorization Contract for Containers", section 3.1.3
 */
public class WebModuleConfiguration extends AbstractModuleConfiguration {
    private WebAppType webApp;
    private SecurityType security;

    public WebModuleConfiguration(String contextId, WebAppType webApp, SecurityType security) {
        super(contextId);
        this.webApp = webApp;
        this.security = security;
    }

    /**
     * Translate the web deployment descriptors into equivalent security
     * permissions.  These permissions are placed into the appropriate
     * <code>PolicyConfiguration</code> object as defined in the JAAC spec.
     *
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
            RoleMappingsType roleMappings = security.getRoleMappings();
            if (roleMappings != null) {
                RoleType[] roles = roleMappings.getRoleArray();
                for (int i = 0; i < roles.length; i++) {
                    RoleType role = roles[i];
                    RealmType[] realms = role.getRealmArray();
                    for (int j = 0; j < realms.length; j++) {
                        RealmType realm = realms[j];
                        PrincipalType[] principals = realm.getPrincipalArray();
                        HashSet set = new HashSet();
                        for (int k = 0; k < principals.length; k++) {
                            PrincipalType principal = principals[k];
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


    private static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(WebModuleConfiguration.class, AbstractModuleConfiguration.GBEAN_INFO);
        //TODO make sure this attribute not backed by a getter or setter works.
        infoFactory.addAttribute("webApp", WebAppType.class, true);
        infoFactory.addAttribute("security", SecurityType.class, true);
        infoFactory.setConstructor(new String[]{"contextID", "webApp", "security"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
