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

package org.apache.geronimo.security.util;

import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.PrimaryRealmPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EnterpriseBeansType;
import org.apache.geronimo.xbeans.j2ee.EntityBeanType;
import org.apache.geronimo.xbeans.j2ee.ExcludeListType;
import org.apache.geronimo.xbeans.j2ee.HttpMethodType;
import org.apache.geronimo.xbeans.j2ee.JavaTypeType;
import org.apache.geronimo.xbeans.j2ee.MethodPermissionType;
import org.apache.geronimo.xbeans.j2ee.MethodType;
import org.apache.geronimo.xbeans.j2ee.RoleNameType;
import org.apache.geronimo.xbeans.j2ee.SecurityConstraintType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleRefType;
import org.apache.geronimo.xbeans.j2ee.SecurityRoleType;
import org.apache.geronimo.xbeans.j2ee.SessionBeanType;
import org.apache.geronimo.xbeans.j2ee.UrlPatternType;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.j2ee.WebResourceCollectionType;


/**
 * A collection of utility functions that assist with the configuration of
 * <code>PolicyConfiguration</code>s.
 *
 * @version $Revision: 1.5 $ $Date: 2004/06/27 18:14:14 $
 * @see javax.security.jacc.PolicyConfiguration
 * @see "JSR 115" Java Authorization Contract for Containers
 */
public class ConfigurationUtil {

    /**
     * Create a RealmPrincipal from a deployment description.
     * @param principal the deployment description of the principal to be created.
     * @param realmName the security realm that the principal belongs go
     * @return a RealmPrincipal from a deployment description
     */
    public static RealmPrincipal generateRealmPrincipal(final Principal principal, final String realmName) {
        try {
            return (RealmPrincipal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    java.security.Principal p = null;
                    Class clazz = Class.forName(principal.getClassName());
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (java.security.Principal) constructor.newInstance(new Object[]{principal.getPrincipalName()});

                    return new RealmPrincipal(realmName, p);
                }
            });
        } catch (PrivilegedActionException e) {
            return null;
        }
    }

    /**
     * Create a RealmPrincipal from a deployment description.
     * @param principal the deployment description of the principal to be created.
     * @param realmName the security realm that the principal belongs go
     * @return a RealmPrincipal from a deployment description
     */
    public static PrimaryRealmPrincipal generatePrimaryRealmPrincipal(final Principal principal, final String realmName) {
        try {
            return (PrimaryRealmPrincipal) AccessController.doPrivileged(new PrivilegedExceptionAction() {
                public Object run() throws Exception {
                    java.security.Principal p = null;
                    Class clazz = Class.forName(principal.getClassName());
                    Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                    p = (java.security.Principal) constructor.newInstance(new Object[]{principal.getPrincipalName()});

                    return new PrimaryRealmPrincipal(realmName, p);
                }
            });
        } catch (PrivilegedActionException e) {
            return null;
        }
    }

    /**
     * A simple helper method to register PolicyContextHandlers
     *
     * @param handler an object that implements the <code>PolicyContextHandler</code>
     *                interface. The value of this parameter must not be null.
     * @param replace this boolean value defines the behavior of this method
     *                if, when it is called, a <code>PolicyContextHandler</code> has already
     *                been registered to handle the same key. In that case, and if the value
     *                of this argument is true, the existing handler is replaced with the
     *                argument handler. If the value of this parameter is false the existing
     *                registration is preserved and an exception is thrown.
     */
    public static void registerPolicyContextHandler(PolicyContextHandler handler, boolean replace) throws PolicyContextException {
        String[] keys = handler.getKeys();

        for (int i = 0; i < keys.length; i++) {
            PolicyContext.registerHandler(keys[i], handler, replace);
        }
    }

    /**
     * Translate the web deployment descriptors into equivalent security
     * permissions.  These permissions are placed into the appropriate
     * <code>PolicyConfiguration</code> object as defined in the JAAC spec.
     *
     * @param webApp the deployment descriptor from which to obtain the
     *               security constraints that are to be translated.
     * @throws org.apache.geronimo.security.GeronimoSecurityException
     *          if there is any violation of the semantics of
     *          the security descriptor or the state of the module configuration.
     * @see javax.security.jacc.PolicyConfiguration
     * @see "Java Authorization Contract for Containers", section 3.1.3
     */
    public static void configure(PolicyConfiguration configuration, WebAppType webApp) throws GeronimoSecurityException {

        HashSet securityRoles = new HashSet();
        SecurityRoleType[] securityRolesArray = webApp.getSecurityRoleArray();
        for (int i = 0; i < securityRolesArray.length; i++) {
            securityRoles.add(securityRolesArray[i].getRoleName());
        }

        HashMap uncheckedPatterns = new HashMap();
        HashMap excludedPatterns = new HashMap();
        HashMap rolesPatterns = new HashMap();
        HashSet allSet = new HashSet();
        HashMap allMap = new HashMap();

        SecurityConstraintType[] s = webApp.getSecurityConstraintArray();
        for (int i = 0; i < s.length; i++) {

            HashMap currentPatterns;
            if (s[i].getAuthConstraint() == null) {
                currentPatterns = uncheckedPatterns;
            } else if (s[i].getAuthConstraint().getRoleNameArray().length == 0) {
                currentPatterns = excludedPatterns;
            } else {
                currentPatterns = rolesPatterns;
            }

            String transport = "";
            if (s[i].getUserDataConstraint() != null) {
                transport = s[i].getUserDataConstraint().getTransportGuarantee().getStringValue();
            }

            WebResourceCollectionType[] collection = s[i].getWebResourceCollectionArray();
            for (int j = 0; j < collection.length; j++) {
                HttpMethodType[] methods = collection[j].getHttpMethodArray();
                UrlPatternType[] patterns = collection[j].getUrlPatternArray();
                for (int k = 0; k < patterns.length; k++) {
                    URLPattern pattern = (URLPattern) currentPatterns.get(patterns[k]);
                    if (pattern == null) {
                        pattern = new URLPattern(patterns[k].getStringValue());
                        currentPatterns.put(patterns[k].getStringValue(), pattern);
                    }

                    URLPattern allPattern = (URLPattern) allMap.get(patterns[k].getStringValue());
                    if (allPattern == null) {
                        allPattern = new URLPattern(patterns[k].getStringValue());
                        allSet.add(allPattern);
                        allMap.put(patterns[k].getStringValue(), allPattern);
                    }

                    for (int l = 0; l < methods.length; l++) {
                        pattern.addMethod(methods[l].getStringValue());
                        allPattern.addMethod(methods[l].getStringValue());
                    }

                    if (methods.length == 0) {
                        pattern.addMethod("");
                        allPattern.addMethod("");
                    }

                    if (currentPatterns == rolesPatterns) {
                        RoleNameType[] roles = s[i].getAuthConstraint().getRoleNameArray();
                        for (int l = 0; l < roles.length; l++) {
                            if (roles[l].getStringValue().equals("*")) {
                                pattern.addAllRoles(securityRoles);
                            } else {
                                pattern.addRole(roles[l].getStringValue());
                            }
                        }
                    }

                    pattern.setTransport(transport);
                }
            }
        }

        try {
            /**
             *
             */
            Iterator iter = excludedPatterns.keySet().iterator();
            while (iter.hasNext()) {
                URLPattern pattern = (URLPattern) excludedPatterns.get(iter.next());
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethods();

                configuration.addToExcludedPolicy(new WebResourcePermission(name, actions));
                configuration.addToExcludedPolicy(new WebUserDataPermission(name, actions));
            }

            /**
             *
             */
            iter = rolesPatterns.keySet().iterator();
            while (iter.hasNext()) {
                URLPattern pattern = (URLPattern) rolesPatterns.get(iter.next());
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethods();
                WebResourcePermission permission = new WebResourcePermission(name, actions);

                Iterator names = pattern.getRoles().iterator();
                while (names.hasNext()) {
                    configuration.addToRole((String) names.next(), permission);
                }
            }

            /**
             *
             */
            iter = uncheckedPatterns.keySet().iterator();
            while (iter.hasNext()) {
                URLPattern pattern = (URLPattern) uncheckedPatterns.get(iter.next());
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethods();

                configuration.addToUncheckedPolicy(new WebResourcePermission(name, actions));
            }

            /**
             *
             */
            iter = rolesPatterns.keySet().iterator();
            while (iter.hasNext()) {
                URLPattern pattern = (URLPattern) rolesPatterns.get(iter.next());
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethodsWithTransport();

                configuration.addToUncheckedPolicy(new WebUserDataPermission(name, actions));
            }

            iter = uncheckedPatterns.keySet().iterator();
            while (iter.hasNext()) {
                URLPattern pattern = (URLPattern) uncheckedPatterns.get(iter.next());
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethodsWithTransport();

                configuration.addToUncheckedPolicy(new WebUserDataPermission(name, actions));
            }

            /**
             * A <code>WebResourcePermission</code> and a <code>WebUserDataPermission</code> must be instantiated for
             * each <tt>url-pattern</tt> in the deployment descriptor and the default pattern "/", that is not combined
             * by the <tt>web-resource-collection</tt> elements of the deployment descriptor with ever HTTP method
             * value.  The permission objects must be contructed using the qualified pattern as their name and with
             * actions defined by the subset of the HTTP methods that do not occur in combination with the pattern.
             * The resulting permissions that must be added to the unchecked policy statements by calling the
             * <code>addToUncheckedPolcy</code> method on the <code>PolicyConfiguration</code> object.
             */
            iter = allSet.iterator();
            while (iter.hasNext()) {
                URLPattern pattern = (URLPattern) iter.next();
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getComplementedMethods();

                if (actions.length() == 0) {
                    continue;
                }

                configuration.addToUncheckedPolicy(new WebResourcePermission(name, actions));
                configuration.addToUncheckedPolicy(new WebUserDataPermission(name, actions));
            }

            URLPattern pattern = new URLPattern("/");
            if (!allSet.contains(pattern)) {
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getComplementedMethods();

                configuration.addToUncheckedPolicy(new WebResourcePermission(name, actions));
                configuration.addToUncheckedPolicy(new WebUserDataPermission(name, actions));
            }
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException(e);
        }
    }

    public static void configure(PolicyConfiguration configuration, EjbJarType ejbJar) throws GeronimoSecurityException {

        EnterpriseBeansType enterpriseBeans = ejbJar.getEnterpriseBeans();
        EntityBeanType[] entityBeans = enterpriseBeans.getEntityArray();
        SessionBeanType[] sessionBeans = enterpriseBeans.getSessionArray();

        AssemblyDescriptorType assemblyDescriptor = ejbJar.getAssemblyDescriptor();
        MethodPermissionType[] methodPermissions = assemblyDescriptor.getMethodPermissionArray();
        ExcludeListType excludeList = assemblyDescriptor.getExcludeList();

        /**
         * Section 3.1.5.1
         */
        for (int i = 0; i < methodPermissions.length; i++) {
            MethodPermissionType methodPermission = methodPermissions[i];
            MethodType[] methods = methodPermission.getMethodArray();

            for (int j = 0; j < methods.length; j++) {
                MethodType method = methods[j];
                EJBMethodPermission permission = new EJBMethodPermission(method.getEjbName().getStringValue(),
                                                                         method.getMethodName().getStringValue(),
                                                                         method.getMethodIntf().getStringValue(),
                                                                         toStringArray(method.getMethodParams().getMethodParamArray()));

                try {
                    if (methodPermission.getUnchecked() != null) {
                        configuration.addToUncheckedPolicy(permission);
                    } else {
                        RoleNameType[] roleNames = methodPermission.getRoleNameArray();

                        for (int k = 0; k < roleNames.length; k++) {
                            configuration.addToRole(roleNames[k].getStringValue(), permission);
                        }
                    }
                } catch (PolicyContextException e) {
                    throw new GeronimoSecurityException(e);
                }
            }
        }

        /**
         * Section 3.1.5.2
         */
        if (excludeList != null) {
            MethodType[] methods = excludeList.getMethodArray();
            try {
                for (int i = 0; i < methods.length; i++) {
                    EJBMethodPermission permission = new EJBMethodPermission(methods[i].getEjbName().getStringValue(),
                                                                             methods[i].getMethodName().getStringValue(),
                                                                             methods[i].getMethodIntf().getStringValue(),
                                                                             toStringArray(methods[i].getMethodParams().getMethodParamArray()));
                    configuration.addToExcludedPolicy(permission);
                }
            } catch (PolicyContextException e) {
                throw new GeronimoSecurityException(e);
            }
        }

        /**
         * Section 3.1.5.3
         */
        for (int i = 0; i < entityBeans.length; i++) {
            translateSecurityRoleRefs(configuration, entityBeans[i].getSecurityRoleRefArray(), entityBeans[i].getEjbName().getStringValue());
        }

        for (int i = 0; i < sessionBeans.length; i++) {
            translateSecurityRoleRefs(configuration, sessionBeans[i].getSecurityRoleRefArray(), sessionBeans[i].getEjbName().getStringValue());
        }
    }

    private static String[] toStringArray(JavaTypeType[] methodParamArray) {
        String[] result = new String[methodParamArray.length];
        for (int i = 0; i < methodParamArray.length; i++) {
            result[i] = methodParamArray[i].getStringValue();
        }
        return result;
    }


    private static void translateSecurityRoleRefs(PolicyConfiguration configuration, SecurityRoleRefType[] roleRefs, String ejbName) throws GeronimoSecurityException {

        try {

            for (int i = 0; i < roleRefs.length; i++) {
                String roleName = roleRefs[i].getRoleName().getStringValue();
                String roleLink = roleRefs[i].getRoleLink().getStringValue();

                configuration.addToRole(roleLink, new EJBRoleRefPermission(ejbName, roleName));
            }
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException(e);
        }
    }
}
