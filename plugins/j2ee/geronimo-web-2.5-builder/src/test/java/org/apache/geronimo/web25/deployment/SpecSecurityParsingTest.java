/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.web25.deployment;

import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

import javax.security.jacc.WebResourcePermission;

import junit.framework.TestCase;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.xbeans.javaee.WebAppDocument;
import org.apache.geronimo.xbeans.javaee.WebAppType;
import org.apache.xmlbeans.XmlOptions;

/**
 * @version $Rev$ $Date$
 */
public class SpecSecurityParsingTest extends TestCase {

    private ClassLoader classLoader = this.getClass().getClassLoader();
    private XmlOptions options = new XmlOptions();
    private TestWebModuleBuilder builder = new TestWebModuleBuilder();
    private Set<String> roleSet = new HashSet<String>();
    private Map<String, PermissionCollection> rolePermissionMap = new HashMap<String, PermissionCollection>();


    public void testParsing() throws Exception {
        roleSet.add("Admin");
        URL srcXml = classLoader.getResource("security/web1.xml");
        WebAppDocument webAppDoc = WebAppDocument.Factory.parse(srcXml, options);
        WebAppType webAppType = webAppDoc.getWebApp();
        ComponentPermissions permissions = builder.buildSpecSecurityConfig(webAppType, roleSet, rolePermissionMap);
        PermissionCollection unchecked = permissions.getUncheckedPermissions();
        assertTrue(unchecked.implies(new WebResourcePermission("/login.do", "!")));
        assertTrue(unchecked.implies(new WebResourcePermission("/foo", "!")));
        assertFalse(unchecked.implies(new WebResourcePermission("/foo.do", "!")));
        PermissionCollection adminPermissions = permissions.getRolePermissions().get("Admin");
        assertTrue(adminPermissions.implies(new WebResourcePermission("foo.do", "GET,POST")));
    }

    /**
     * make sure a resource permission with a role doesn't turn into an unchecked permission due to mistakes in
     * HTTPMethod "all" handling
     * @throws Exception
     */
    public void testAllMethodsConstraint() throws Exception {
        roleSet.add("Admin");
        URL srcXml = classLoader.getResource("security/web2.xml");
        WebAppDocument webAppDoc = WebAppDocument.Factory.parse(srcXml, options);
        WebAppType webAppType = webAppDoc.getWebApp();
        ComponentPermissions permissions = builder.buildSpecSecurityConfig(webAppType, roleSet, rolePermissionMap);
        PermissionCollection unchecked = permissions.getUncheckedPermissions();
        assertFalse(unchecked.implies(new WebResourcePermission("/Test", "!")));
        PermissionCollection adminPermissions = permissions.getRolePermissions().get("Admin");
        assertTrue(adminPermissions.implies(new WebResourcePermission("/Test", "GET,POST")));
    }

    public void testDifferentRoleDifferentHttpMethod() throws Exception {
        roleSet.add("userGet");
        roleSet.add("userPost");
        URL srcXml = classLoader.getResource("security/web3.xml");
        WebAppDocument webAppDoc = WebAppDocument.Factory.parse(srcXml, options);
        WebAppType webAppType = webAppDoc.getWebApp();
        ComponentPermissions permissions = builder.buildSpecSecurityConfig(webAppType, roleSet, rolePermissionMap);
        PermissionCollection unchecked = permissions.getUncheckedPermissions();
        Permission p = new WebResourcePermission("/app/*", "GET");
        assertTrue(implies(p, permissions, "userGet"));
        assertFalse(implies(p, permissions, "userPost"));
        p = new WebResourcePermission("/app/home", "POST");
        assertTrue(implies(p, permissions, "userPost"));
        assertFalse(implies(p, permissions, "userGet"));
    }

    private boolean implies(Permission p, ComponentPermissions permissions, String role) {
        PermissionCollection excluded = permissions.getExcludedPermissions();
        if (excluded.implies(p))
            return false;
        PermissionCollection unchecked = permissions.getUncheckedPermissions();
        if (unchecked.implies(p))
            return true;
        if (role == null)
            return false;
        PermissionCollection rolePermissions = permissions.getRolePermissions().get(role);
        return rolePermissions != null && rolePermissions.implies(p);
    }

    public static class TestWebModuleBuilder extends AbstractWebModuleBuilder {

        protected TestWebModuleBuilder() {
            super(null, null, null, null, null, Collections.EMPTY_SET, null);
        }

        protected Module createModule(Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, boolean standAlone, String contextRoot, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
            return null;
        }

        public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        }

        public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
        }

        public String getSchemaNamespace() {
            return null;
        }
    }
}
