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
package org.apache.geronimo.security.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;

import org.apache.geronimo.deployment.model.ejb.AssemblyDescriptor;
import org.apache.geronimo.deployment.model.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.ejb.Entity;
import org.apache.geronimo.deployment.model.ejb.ExcludeList;
import org.apache.geronimo.deployment.model.ejb.Method;
import org.apache.geronimo.deployment.model.ejb.MethodPermission;
import org.apache.geronimo.deployment.model.ejb.RpcBean;
import org.apache.geronimo.deployment.model.ejb.Session;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.web.WebApp;
import org.apache.geronimo.deployment.model.j2ee.SecurityRole;
import org.apache.geronimo.deployment.model.j2ee.SecurityRoleRef;
import org.apache.geronimo.deployment.model.web.SecurityConstraint;
import org.apache.geronimo.deployment.model.web.WebResourceCollection;
import org.apache.geronimo.security.GeronimoSecurityException;


/**
 * A collection of utility functions that assist with the configuration of
 * <code>PolicyConfiguration</code>s.
 *
 * @version $Revision: 1.5 $ $Date: 2004/01/04 22:52:28 $
 * @see javax.security.jacc.PolicyConfiguration
 *  @see "JSR 115" Java Authorization Contract for Containers
 */
public class ConfigurationUtil {

    /**
     * A simple helper method to register PolicyContextHandlers
     * @param handler an object that implements the <code>PolicyContextHandler</code>
     * interface. The value of this parameter must not be null.
     * @param replace this boolean value defines the behavior of this method
     * if, when it is called, a <code>PolicyContextHandler</code> has already
     * been registered to handle the same key. In that case, and if the value
     * of this argument is true, the existing handler is replaced with the
     * argument handler. If the value of this parameter is false the existing
     * registration is preserved and an exception is thrown.
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
     * @param webApp the deployment descriptor from which to obtain the
     * security constraints that are to be translated.
     * @throws org.apache.geronimo.security.GeronimoSecurityException if there is any violation of the semantics of
     * the security descriptor or the state of the module configuration.
     * @see javax.security.jacc.PolicyConfiguration
     * @see "Java Authorization Contract for Containers", section 3.1.3
     */
    public static void configure(PolicyConfiguration configuration, WebApp webApp) throws GeronimoSecurityException {

        HashSet securityRoles = new HashSet();
        SecurityRole[] securityRolesArray = webApp.getSecurityRole();
        for (int i = 0; i < securityRolesArray.length; i++) {
            securityRoles.add(securityRolesArray[i].getRoleName());
        }

        HashMap uncheckedPatterns = new HashMap();
        HashMap excludedPatterns = new HashMap();
        HashMap rolesPatterns = new HashMap();
        HashSet allSet = new HashSet();
        HashMap allMap = new HashMap();

        SecurityConstraint[] s = webApp.getSecurityConstraint();
        for (int i = 0; i < s.length; i++) {

            HashMap currentPatterns;
            if (s[i].getAuthConstraint() == null) {
                currentPatterns = uncheckedPatterns;
            } else if (s[i].getAuthConstraint().getRoleName().length == 0) {
                currentPatterns = excludedPatterns;
            } else {
                currentPatterns = rolesPatterns;
            }

            String transport = "";
            if (s[i].getUserDataConstraint() != null) {
                transport = s[i].getUserDataConstraint().getTransportGuarantee();
            }

            WebResourceCollection[] collection = s[i].getWebResourceCollection();
            for (int j = 0; j < collection.length; j++) {
                String[] methods = collection[j].getHttpMethod();
                String[] patterns = collection[j].getUrlPattern();
                for (int k = 0; k < patterns.length; k++) {
                    URLPattern pattern = (URLPattern) currentPatterns.get(patterns[k]);
                    if (pattern == null) {
                        pattern = new URLPattern(patterns[k]);
                        currentPatterns.put(patterns[k], pattern);
                    }

                    URLPattern allPattern = (URLPattern) allMap.get(patterns[k]);
                    if (allPattern == null) {
                        allPattern = new URLPattern(patterns[k]);
                        allSet.add(allPattern);
                        allMap.put(patterns[k], allPattern);
                    }

                    for (int l = 0; l < methods.length; l++) {
                        pattern.addMethod(methods[l]);
                        allPattern.addMethod(methods[l]);
                    }

                    if (methods.length == 0) {
                        pattern.addMethod("");
                        allPattern.addMethod("");
                    }

                    if (currentPatterns == rolesPatterns) {
                        String[] roles = s[i].getAuthConstraint().getRoleName();
                        for (int l = 0; l < roles.length; l++) {
                            if (roles[l].equals("*")) {
                                pattern.addAllRoles(securityRoles);
                            } else {
                                pattern.addRole(roles[l]);
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

    public static void configure(PolicyConfiguration configuration, EjbJar ejbJar) throws GeronimoSecurityException {

        EnterpriseBeans enterpriseBeans = ejbJar.getEnterpriseBeans();
        Entity[] entityBeans = enterpriseBeans.getEntity();
        Session[] sessionBeans = enterpriseBeans.getSession();

        AssemblyDescriptor assemblyDescriptor = ejbJar.getAssemblyDescriptor();
        MethodPermission[] methodPermissions = assemblyDescriptor.getMethodPermission();
        ExcludeList excludeList = assemblyDescriptor.getExcludeList();

        /**
         * Section 3.1.5.1
         */
        for (int i = 0; i < methodPermissions.length; i++) {
            MethodPermission methodPermission = methodPermissions[i];
            Method[] methods = methodPermission.getMethod();

            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                EJBMethodPermission permission = new EJBMethodPermission(method.getEjbName(),
                        method.getMethodName(),
                        method.getMethodIntf(),
                        method.getMethodParam());

                try {
                    if (methodPermission.isUnchecked()) {
                        configuration.addToUncheckedPolicy(permission);
                    } else {
                        String[] roleNames = methodPermission.getRoleName();

                        for (int k = 0; k < roleNames.length; k++) {
                            configuration.addToRole(roleNames[k], permission);
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
            Method[] methods = excludeList.getMethod();
            try {
                for (int i = 0; i < methods.length; i++) {
                    EJBMethodPermission permission = new EJBMethodPermission(methods[i].getEjbName(),
                            methods[i].getMethodName(),
                            methods[i].getMethodIntf(),
                            methods[i].getMethodParam());
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
            translateSecurityRoleRefs(configuration, entityBeans[i]);
        }

        for (int i = 0; i < sessionBeans.length; i++) {
            translateSecurityRoleRefs(configuration, sessionBeans[i]);
        }
    }

    private static void translateSecurityRoleRefs(PolicyConfiguration configuration, RpcBean bean) throws GeronimoSecurityException {

        try {
            SecurityRoleRef[] roleRefs = bean.getSecurityRoleRef();

            for (int i = 0; i < roleRefs.length; i++) {
                String roleName = roleRefs[i].getRoleName();
                String roleLink = roleRefs[i].getRoleLink();

                configuration.addToRole(roleLink, new EJBRoleRefPermission(bean.getEJBName(), roleName));
            }
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException(e);
        }
    }
}
