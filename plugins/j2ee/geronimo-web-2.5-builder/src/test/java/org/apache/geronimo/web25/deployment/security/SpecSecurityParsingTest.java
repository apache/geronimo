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


package org.apache.geronimo.web25.deployment.security;

import java.io.InputStream;
import java.net.URL;
import java.security.Permission;
import java.security.PermissionCollection;

import javax.security.jacc.WebResourcePermission;
import javax.security.jacc.WebUserDataPermission;

import junit.framework.TestCase;

import org.apache.geronimo.security.jacc.ComponentPermissions;
import org.apache.geronimo.web.security.SpecSecurityBuilder;
import org.apache.geronimo.web25.deployment.DefaultWebAppInfoFactory;
import org.apache.geronimo.web25.deployment.WebAppInfoBuilder;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.WebApp;

/**
 * @version $Rev$ $Date$
 */
public class SpecSecurityParsingTest extends TestCase {

    private ClassLoader classLoader = this.getClass().getClassLoader();

    public void testParsing() throws Exception {
        WebApp webApp = parse("security/web1.xml");
        WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, new DefaultWebAppInfoFactory());
        webAppInfoBuilder.build();
        SpecSecurityBuilder builder = new SpecSecurityBuilder(webAppInfoBuilder.getWebAppInfo());
        ComponentPermissions permissions = builder.buildSpecSecurityConfig();
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
        WebApp webApp = parse("security/web2.xml");
        WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, new DefaultWebAppInfoFactory());
        webAppInfoBuilder.build();
        SpecSecurityBuilder builder = new SpecSecurityBuilder(webAppInfoBuilder.getWebAppInfo());
        ComponentPermissions permissions = builder.buildSpecSecurityConfig();
        Permission p = new WebResourcePermission("/Test/Foo", "GET,POST");
        assertTrue(implies(p, permissions, "Admin"));
        assertFalse(implies(new WebResourcePermission("/Test", ""), permissions, null));
        assertFalse(implies(new WebResourcePermission("/Test", "!"), permissions, null));
    }

    public void testExcludedConstraint() throws Exception {
        WebApp webApp = parse("security/web3.xml");
        WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, new DefaultWebAppInfoFactory());
        webAppInfoBuilder.build();
        SpecSecurityBuilder builder = new SpecSecurityBuilder(webAppInfoBuilder.getWebAppInfo());
        ComponentPermissions permissions = builder.buildSpecSecurityConfig();
        Permission p = new WebResourcePermission("/Test/Foo", "GET,POST");
        assertTrue(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, null));
        p = new WebResourcePermission("/Test/Bar/Foo", "GET,POST");
        assertFalse(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, null));
        // check only GET method excluded here.
        p = new WebResourcePermission("/Test/Baz/Foo", "GET");
        assertFalse(implies(p, permissions, "Admin"));
        p = new WebResourcePermission("/Test/Baz/Foo", "POST");
        assertTrue(implies(p, permissions, "Admin"));
        // test excluding longer path than allowed
        p = new WebResourcePermission("/Foo/Baz", "GET");
        assertTrue(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, "Peon"));
        p = new WebResourcePermission("/Foo/Bar/Foo", "POST");
        assertTrue(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, "Peon"));
        p = new WebResourcePermission("/Foo/Bar/Foo", "GET");
        assertFalse(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, "Peon"));
    }
    public void testExcludedRemovesRoleConstraint() throws Exception {
        WebApp webApp = parse("security/web4.xml");
        WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, new DefaultWebAppInfoFactory());
        webAppInfoBuilder.build();
        SpecSecurityBuilder builder = new SpecSecurityBuilder(webAppInfoBuilder.getWebAppInfo());
        ComponentPermissions permissions = builder.buildSpecSecurityConfig();
        // test excluding longer path than allowed
        Permission p = new WebResourcePermission("/Foo/Baz", "GET");
        assertTrue(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, "Peon"));
        p = new WebResourcePermission("/Foo/Bar/Foo", "POST");
        assertTrue(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, "Peon"));
        p = new WebResourcePermission("/Foo/Bar/Foo", "GET");
        assertFalse(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, "Peon"));
        // test excluding longer path allows unchecked access to other http methods
        p = new WebResourcePermission("/Bar/Baz", "GET");
        assertTrue(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, "Peon"));
        p = new WebResourcePermission("/Bar/Bar/Bar", "POST");
        assertTrue(implies(p, permissions, "Admin"));
        //This one is false unless excluded constraint allows other https methods unchecked access
//        assertFalse(implies(p, permissions, "Peon"));
        assertTrue(implies(p, permissions, "Peon"));
        p = new WebResourcePermission("/Bar/Bar/Bar", "GET");
        assertFalse(implies(p, permissions, "Admin"));
        assertFalse(implies(p, permissions, "Peon"));
    }

    //overlapping excluded and role constraint, excluded constraint wins.
    public void testExcludedAndRoleConstraint() throws Exception {
        WebApp webApp = parse("security/web5.xml");
        WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, new DefaultWebAppInfoFactory());
        webAppInfoBuilder.build();
        SpecSecurityBuilder builder = new SpecSecurityBuilder(webAppInfoBuilder.getWebAppInfo());
        ComponentPermissions permissions = builder.buildSpecSecurityConfig();
        // test excluding longer path than allowed
        Permission p = new WebResourcePermission("/foo/Baz", "GET");
        assertFalse(implies(p, permissions, "user"));
        assertFalse(implies(p, permissions, null));
        p = new WebResourcePermission("/bar", "GET");
        assertTrue(implies(p, permissions, "user"));
        assertTrue(implies(p, permissions, null));
        p = new WebUserDataPermission("/bar", "GET");
        assertTrue(implies(p, permissions, "user"));
        assertTrue(implies(p, permissions, null));
    }

    public void testHTTPOmissionMethodsConstraint() throws Exception {
        WebApp webApp = parse("security/web6.xml");
        WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, new DefaultWebAppInfoFactory());
        webAppInfoBuilder.build();
        SpecSecurityBuilder builder = new SpecSecurityBuilder(webAppInfoBuilder.getWebAppInfo());
        ComponentPermissions permissions = builder.buildSpecSecurityConfig();
        Permission p = new WebResourcePermission("/app/*", "GET");
        assertFalse(implies(p, permissions, null));
        p = new WebResourcePermission("/app/home", "POST");
        assertTrue(implies(p, permissions, null));
    }

    public void testDifferentRoleDifferentHttpMethod() throws Exception {
        WebApp webApp = parse("security/web7.xml");
        WebAppInfoBuilder webAppInfoBuilder = new WebAppInfoBuilder(webApp, new DefaultWebAppInfoFactory());
        webAppInfoBuilder.build();
        SpecSecurityBuilder builder = new SpecSecurityBuilder(webAppInfoBuilder.getWebAppInfo());
        ComponentPermissions permissions = builder.buildSpecSecurityConfig();
        Permission p = new WebResourcePermission("/app/*", "GET");
        assertTrue(implies(p, permissions, "userGet"));
        assertFalse(implies(p, permissions, "userPost"));
        p = new WebResourcePermission("/app/home", "POST");
        assertTrue(implies(p, permissions, "userPost"));
        assertFalse(implies(p, permissions, "userGet"));
    }


    private boolean implies(Permission p, ComponentPermissions permissions, String role) {
        PermissionCollection excluded = permissions.getExcludedPermissions();
        if (excluded.implies(p)) return false;
        PermissionCollection unchecked = permissions.getUncheckedPermissions();
        if (unchecked.implies(p)) return true;
        if (role == null) return false;
        PermissionCollection rolePermissions = permissions.getRolePermissions().get(role);
        return rolePermissions != null && rolePermissions.implies(p);
    }

    private WebApp parse(String resource) throws Exception {
        URL specDDUrl = classLoader.getResource(resource);
        InputStream in = specDDUrl.openStream();
        try {
            return (WebApp) JaxbJavaee.unmarshalJavaee(WebApp.class, in);
        } finally {
            in.close();
        }

    }

}
