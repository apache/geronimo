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
package org.apache.geronimo.jetty;

import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.UnavailableException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.mortbay.jetty.servlet.XMLConfiguration;
import org.mortbay.xml.XmlParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.jetty.JettyWebApplicationContext;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.jacc.RoleMappingConfiguration;
import org.apache.geronimo.security.util.URLPattern;


/**
 * @version $Revision: 1.1 $ $Date: 2004/05/30 19:09:57 $
 */
public class JettyXMLConfiguration extends XMLConfiguration {

    private static Log log = LogFactory.getLog(JettyXMLConfiguration.class);

    private HashSet securityRoles = new HashSet();
    private HashMap uncheckedPatterns = new HashMap();
    private HashMap excludedPatterns = new HashMap();
    private HashMap rolesPatterns = new HashMap();
    private HashSet allSet = new HashSet();
    private HashMap allMap = new HashMap();
    private HashSet allRoles = new HashSet();


    public JettyXMLConfiguration(JettyWebApplicationContext context) {
        super(context);
    }

    protected void initialize(XmlParser.Node config) throws ClassNotFoundException, UnavailableException {
        super.initialize(config);

        Iterator iter = allRoles.iterator();
        while (iter.hasNext()) {
            ((URLPattern) iter.next()).addAllRoles(securityRoles);
        }
    }

    /**
     * Translate the web deployment descriptors into equivalent security
     * permissions.  These permissions are placed into the appropriate
     * <code>PolicyConfiguration</code> object as defined in the JAAC spec.
     *
     * @param node the deployment descriptor from which to obtain the
     *             security constraints that are to be translated.
     * @throws org.apache.geronimo.security.GeronimoSecurityException
     *          if there is any violation of the semantics of
     *          the security descriptor or the state of the module configuration.
     * @see javax.security.jacc.PolicyConfiguration
     * @see "Java Authorization Contract for Containers", section 3.1.3
     */
    protected void initSecurityConstraint(XmlParser.Node node) {
        super.initSecurityConstraint(node);

        XmlParser.Node auths = node.get("auth-constraint");

        HashMap currentPatterns;
        if (auths == null) {
            currentPatterns = uncheckedPatterns;
        } else if (auths.size() == 0) {
            currentPatterns = excludedPatterns;
        } else {
            currentPatterns = rolesPatterns;
        }

        XmlParser.Node data = node.get("user-data-constraint");
        String transport = "";
        if (data != null) {
            transport = data.get("transport-guarantee").toString(false, true).toUpperCase();
        }

        Iterator resourceIiter = node.iterator("web-resource-collection");
        while (resourceIiter.hasNext()) {
            XmlParser.Node collection = (XmlParser.Node) resourceIiter.next();
            Iterator urlPattermIter = collection.iterator("url-pattern");
            while (urlPattermIter.hasNext()) {
                String url = ((XmlParser.Node) urlPattermIter.next()).toString(false, true);
                URLPattern pattern = (URLPattern) currentPatterns.get(url);
                if (pattern == null) {
                    pattern = new URLPattern(url);
                    currentPatterns.put(url, pattern);
                }

                URLPattern allPattern = (URLPattern) allMap.get(url);
                if (allPattern == null) {
                    allPattern = new URLPattern(url);
                    allSet.add(allPattern);
                    allMap.put(url, allPattern);
                }

                boolean noMethods = true;
                Iterator methodIter = collection.iterator("http-method");
                while (methodIter.hasNext()) {
                    String method = ((XmlParser.Node) urlPattermIter.next()).toString(false, true);
                    pattern.addMethod(method);
                    allPattern.addMethod(method);
                    noMethods = false;
                }

                if (noMethods) {
                    pattern.addMethod("");
                    allPattern.addMethod("");
                }

                if (currentPatterns == rolesPatterns) {
                    Iterator roleNameIter = auths.iterator("role-name");
                    while (roleNameIter.hasNext()) {
                        String role = ((XmlParser.Node) roleNameIter.next()).toString(false, true);
                        if (role.equals("*")) {
                            allRoles.add(pattern);
                        } else {
                            pattern.addRole(role);
                        }
                    }
                }

                pattern.setTransport(transport);
            }
        }
    }

    protected void initSecurityRole(XmlParser.Node node) {
        securityRoles.add(node.get("role-name").toString(false, true));
    }

    public void configure(PolicyConfiguration configuration, Security security) throws GeronimoSecurityException {

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

            RoleMappingConfiguration roleMapper = (RoleMappingConfiguration) configuration;
            Iterator rollMappings = security.getRollMappings().iterator();
            while (rollMappings.hasNext()) {
                Role role = (Role) rollMappings.next();

                if (!securityRoles.contains(role.getRoleName())) throw new GeronimoSecurityException("Role does not exist in this configuration");

                Iterator realms = role.getRealms().iterator();
                while (realms.hasNext()) {
                    Set principalSet = new HashSet();
                    Realm realm = (Realm) realms.next();

                    Iterator principals = realm.getPrincipals().iterator();
                    while (principals.hasNext()) {
                        Principal principal = (Principal) principals.next();

                        Class clazz = Class.forName(principal.getClassName());
                        Constructor constructor = clazz.getDeclaredConstructor(new Class[]{String.class});
                        java.security.Principal p = (java.security.Principal) constructor.newInstance(new Object[]{principal.getPrincipalName()});
                        principalSet.add(new RealmPrincipal(realm.getRealmName(), p));
                    }
                    roleMapper.addRoleMapping(role.getRoleName(), principalSet);
                }
            }
        } catch (ClassCastException cce) {
            throw new GeronimoSecurityException("Policy configuration object does not implement RoleMappingConfiguration", cce.getCause());
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException(e);
        } catch (IllegalAccessException e) {
            throw new GeronimoSecurityException(e);
        } catch (NoSuchMethodException e) {
            throw new GeronimoSecurityException(e);
        } catch (InvocationTargetException e) {
            throw new GeronimoSecurityException(e);
        } catch (InstantiationException e) {
            throw new GeronimoSecurityException(e);
        } catch (ClassNotFoundException e) {
            throw new GeronimoSecurityException(e);
        }
    }
}
