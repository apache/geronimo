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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyConfiguration;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebRoleRefPermission;
import javax.security.jacc.WebUserDataPermission;
import javax.servlet.UnavailableException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.security.RealmPrincipal;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.deploy.Realm;
import org.apache.geronimo.security.deploy.Role;
import org.apache.geronimo.security.deploy.Security;
import org.apache.geronimo.security.jacc.RoleMappingConfiguration;
import org.apache.geronimo.security.util.ConfigurationUtil;
import org.apache.geronimo.security.util.URLPattern;
import org.mortbay.jetty.servlet.XMLConfiguration;
import org.mortbay.xml.XmlParser;


/**
 * JettyXMLConfiguration reads the web-app configuration and translates them
 * into corresponding JACC policy permissions.
 *
 * @version $Rev$ $Date$
 */
public class JettyXMLConfiguration extends XMLConfiguration {
    private static Log log = LogFactory.getLog(JettyXMLConfiguration.class);

    private final Set securityRoles = new HashSet();
    private final Map uncheckedPatterns = new HashMap();
    private final Map excludedPatterns = new HashMap();
    private final Map rolesPatterns = new HashMap();
    private final Set allSet = new HashSet();
    private final Map allMap = new HashMap();
    private final Set allRoles = new HashSet();
    private final Map roleRefs = new HashMap();
    private final Map servletRoles = new HashMap();


    protected void initialize(XmlParser.Node config) throws ClassNotFoundException, UnavailableException {
        super.initialize(config);

        Iterator iter = allRoles.iterator();
        while (iter.hasNext()) {
            ((URLPattern) iter.next()).addAllRoles(securityRoles);
        }
    }

    protected void initServlet(XmlParser.Node node)
            throws ClassNotFoundException, UnavailableException, IOException, MalformedURLException {

        super.initServlet(node);

        String name = node.getString("servlet-name", false, true);
        if (name == null) name = node.getString("servlet-class", false, true);

        Set roles = (Set) servletRoles.get(name);
        if (roles == null) {
            roles = new HashSet();
            servletRoles.put(name, roles);
        }

        Iterator sRefsIter = node.iterator("security-role-ref");
        while (sRefsIter.hasNext()) {
            XmlParser.Node securityRef = (XmlParser.Node) sRefsIter.next();
            String roleName = securityRef.getString("role-name", false, true);
            String roleLink = securityRef.getString("role-link", false, true);

            if (roleName != null && roleName.length() > 0 && roleLink != null && roleLink.length() > 0) {
                if (log.isDebugEnabled()) log.debug("link role " + roleName + " to " + roleLink + " for " + this);

                roles.add(roleName);

                Set refs = (Set) roleRefs.get(roleLink);
                if (refs == null) {
                    refs = new HashSet();
                    roleRefs.put(roleLink, refs);
                }
                refs.add(new WebRoleRefPermission(name, roleLink));
            } else {
                log.warn("Ignored invalid security-role-ref element: " + "servlet-name=" + name + ", " + securityRef);
            }
        }
    }

    /**
     * Translate the web deployment descriptors into equivalent security
     * permissions.  These permissions are placed into the appropriate
     * <code>PolicyConfiguration</code> object as defined in the JACC spec.
     *
     * @param node deployment descriptor from which to obtain the
     * security constraints that are to be translated.
     * @throws org.apache.geronimo.security.GeronimoSecurityException if there
     * is any violation of the semantics of the security descriptor or the state
     * of the module configuration.
     * @see javax.security.jacc.PolicyConfiguration
     * @see "Java Authorization Contract for Containers", section 3.1.3
     */
    protected void initSecurityConstraint(XmlParser.Node node) {
        super.initSecurityConstraint(node);

        XmlParser.Node auths = node.get("auth-constraint");

        Map currentPatterns;
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

        for (Iterator resourceIiter = node.iterator("web-resource-collection"); resourceIiter.hasNext();) {
            XmlParser.Node collection = (XmlParser.Node) resourceIiter.next();
            for (Iterator urlPattermIter = collection.iterator("url-pattern"); urlPattermIter.hasNext();) {
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
                for (Iterator methodIter = collection.iterator("http-method"); methodIter.hasNext();) {
                    String method = ((XmlParser.Node) methodIter.next()).toString(false, true);
                    pattern.addMethod(method);
                    allPattern.addMethod(method);
                    noMethods = false;
                }

                if (noMethods) {
                    pattern.addMethod("");
                    allPattern.addMethod("");
                }

                if (currentPatterns == rolesPatterns) {
                    for (Iterator roleNameIter = auths.iterator("role-name"); roleNameIter.hasNext();) {
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
        super.initSecurityRole(node);

        securityRoles.add(node.get("role-name").toString(false, true));
    }

    /**
     * This method dumps the intermediate security information into the JACC
     * PolicyConfiguration.
     *
     * @param configuration the JACC PolicyConfiguration
     * @param security the augmented security information from the geronimo-web.xml file
     */
    public void configure(PolicyConfiguration configuration, Security security) throws GeronimoSecurityException {

        try {
            Iterator iter = excludedPatterns.keySet().iterator();
            while (iter.hasNext()) {
                URLPattern pattern = (URLPattern) excludedPatterns.get(iter.next());
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethods();

                configuration.addToExcludedPolicy(new WebResourcePermission(name, actions));
                configuration.addToExcludedPolicy(new WebUserDataPermission(name, actions));
            }

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

            iter = uncheckedPatterns.keySet().iterator();
            while (iter.hasNext()) {
                URLPattern pattern = (URLPattern) uncheckedPatterns.get(iter.next());
                String name = pattern.getQualifiedPattern(allSet);
                String actions = pattern.getMethods();

                configuration.addToUncheckedPolicy(new WebResourcePermission(name, actions));
            }

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

            JettyWebAppJACCContext context = (JettyWebAppJACCContext) getWebApplicationContext();
            RoleMappingConfiguration roleMapper = (RoleMappingConfiguration) configuration;
            Iterator rollMappings = security.getRoleMappings().iterator();
            while (rollMappings.hasNext()) {
                Role role = (Role) rollMappings.next();
                String roleName = role.getRoleName();
                Set principalSet = new HashSet();

                if (!securityRoles.contains(roleName)) throw new GeronimoSecurityException("Role does not exist in this configuration");

                Subject roleDesignate = new Subject();

                Iterator realms = role.getRealms().iterator();
                while (realms.hasNext()) {
                    Realm realm = (Realm) realms.next();

                    Iterator principals = realm.getPrincipals().iterator();
                    while (principals.hasNext()) {
                        Principal principal = (Principal) principals.next();

                        RealmPrincipal realmPrincipal = ConfigurationUtil.generateRealmPrincipal(principal, realm.getRealmName());

                        if (realmPrincipal == null) throw new GeronimoSecurityException("Unable to create realm principal");

                        principalSet.add(realmPrincipal);
                        if (principal.isDesignatedRunAs()) roleDesignate.getPrincipals().add(realmPrincipal);
                    }
                }
                roleMapper.addRoleMapping(roleName, principalSet);

                if (roleDesignate.getPrincipals().size() > 0) context.setRoleDesignate(roleName, roleDesignate);
            }

            Iterator keys = roleRefs.keySet().iterator();
            while (keys.hasNext()) {
                String roleLink = (String) keys.next();
                iter = ((Set) roleRefs.get(roleLink)).iterator();

                while (iter.hasNext()) {
                    configuration.addToRole(roleLink, (WebRoleRefPermission) iter.next());
                }
            }

            keys = servletRoles.keySet().iterator();
            while (keys.hasNext()) {
                String servletName = (String) keys.next();
                Set roles = new HashSet(securityRoles);

                roles.removeAll((Set) servletRoles.get(servletName));

                iter = roles.iterator();
                while (iter.hasNext()) {
                    String roleName = (String) iter.next();
                    configuration.addToRole(roleName, new WebRoleRefPermission(servletName, roleName));
                }
            }

        } catch (ClassCastException cce) {
            throw new GeronimoSecurityException("Policy configuration object does not implement RoleMappingConfiguration", cce.getCause());
        } catch (PolicyContextException e) {
            throw new GeronimoSecurityException(e);
        }
    }
}
