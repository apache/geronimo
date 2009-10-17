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


package org.apache.geronimo.security.deployment;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.osgi.MockBundleContext;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.mock.MockConfigurationManager;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class LoginConfigBuilderTest extends TestCase {
    //2.0 namespace, with server-side
    private static final String SAMPLE1 = "            <lc:login-config xmlns:lc=\"http://geronimo.apache.org/xml/ns/loginconfig-2.0\">\n" +
            "                <lc:login-module control-flag=\"REQUIRED\" server-side=\"true\" wrap-principals=\"true\">\n" +
            "                    <lc:login-domain-name>client-properties-realm</lc:login-domain-name>\n" +
            "                    <lc:login-module-class>org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule</lc:login-module-class>\n" +
            "                    <lc:option name=\"usersURI\">var/security/users.properties</lc:option>\n" +
            "                    <lc:option name=\"groupsURI\">var/security/groups.properties</lc:option>\n" +
            "                </lc:login-module>\n" +
            "                <lc:login-module control-flag=\"REQUIRED\" server-side=\"true\" wrap-principals=\"true\">\n" +
            "                    <lc:login-domain-name>default</lc:login-domain-name>\n" +
            "                    <lc:login-module-class>org.apache.geronimo.security.realm.providers.NamedUsernamePasswordCredentialLoginModule</lc:login-module-class>\n" +
            "                    <lc:option name=\"org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Name\">default</lc:option>\n" +
            "                </lc:login-module>\n" +
            "            </lc:login-config>";
    //1.2 namespace, with server-side
    private static final String SAMPLE2 = "            <lc:login-config xmlns:lc=\"http://geronimo.apache.org/xml/ns/loginconfig-1.2\">\n" +
            "                <lc:login-module control-flag=\"REQUIRED\" server-side=\"true\" wrap-principals=\"true\">\n" +
            "                    <lc:login-domain-name>client-properties-realm</lc:login-domain-name>\n" +
            "                    <lc:login-module-class>org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule</lc:login-module-class>\n" +
            "                    <lc:option name=\"usersURI\">var/security/users.properties</lc:option>\n" +
            "                    <lc:option name=\"groupsURI\">var/security/groups.properties</lc:option>\n" +
            "                </lc:login-module>\n" +
            "                <lc:login-module control-flag=\"REQUIRED\" server-side=\"true\" wrap-principals=\"true\">\n" +
            "                    <lc:login-domain-name>default</lc:login-domain-name>\n" +
            "                    <lc:login-module-class>org.apache.geronimo.security.realm.providers.NamedUsernamePasswordCredentialLoginModule</lc:login-module-class>\n" +
            "                    <lc:option name=\"org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Name\">default</lc:option>\n" +
            "                </lc:login-module>\n" +
            "            </lc:login-config>";
    //2.0 namespace, without server-side
    private static final String SAMPLE3 = "            <lc:login-config xmlns:lc=\"http://geronimo.apache.org/xml/ns/loginconfig-2.0\">\n" +
            "                <lc:login-module control-flag=\"REQUIRED\" wrap-principals=\"true\">\n" +
            "                    <lc:login-domain-name>client-properties-realm</lc:login-domain-name>\n" +
            "                    <lc:login-module-class>org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule</lc:login-module-class>\n" +
            "                    <lc:option name=\"usersURI\">var/security/users.properties</lc:option>\n" +
            "                    <lc:option name=\"groupsURI\">var/security/groups.properties</lc:option>\n" +
            "                </lc:login-module>\n" +
            "                <lc:login-module control-flag=\"REQUIRED\" wrap-principals=\"true\">\n" +
            "                    <lc:login-domain-name>default</lc:login-domain-name>\n" +
            "                    <lc:login-module-class>org.apache.geronimo.security.realm.providers.NamedUsernamePasswordCredentialLoginModule</lc:login-module-class>\n" +
            "                    <lc:option name=\"org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Name\">default</lc:option>\n" +
            "                </lc:login-module>\n" +
            "            </lc:login-config>";
    //2.0 namespace, without server-side, with login-module-ref
    private static final String SAMPLE4 = "            <lc:login-config xmlns:lc=\"http://geronimo.apache.org/xml/ns/loginconfig-2.0\">\n" +
            "                <lc:login-module-ref control-flag=\"REQUIRED\" wrap-principals=\"true\">\n" +
            "                    <lc:pattern><name xmlns='http://geronimo.apache.org/xml/ns/deployment-1.2'>client-properties-lm</name></lc:pattern>\n" +
            "                </lc:login-module-ref>\n" +
            "                <lc:login-module control-flag=\"REQUIRED\" wrap-principals=\"true\">\n" +
            "                    <lc:login-domain-name>default</lc:login-domain-name>\n" +
            "                    <lc:login-module-class>org.apache.geronimo.security.realm.providers.NamedUsernamePasswordCredentialLoginModule</lc:login-module-class>\n" +
            "                    <lc:option name=\"org.apache.geronimo.jaas.NamedUsernamePasswordCredential.Name\">default</lc:option>\n" +
            "                </lc:login-module>\n" +
            "            </lc:login-config>";

    public void test1() throws Exception {
        doTest(SAMPLE1);
    }
    public void test2() throws Exception {
        doTest(SAMPLE2);
    }
    public void test3() throws Exception {
        doTest(SAMPLE3);
    }
    public void test() throws Exception {
        doTest(SAMPLE4);
    }

    private void doTest(String text) throws XmlException, DeploymentException {
        GeronimoSecurityBuilderImpl secBuilder = new GeronimoSecurityBuilderImpl(null, null, null);
        secBuilder.doStart();
        LoginConfigBuilder builder = new LoginConfigBuilder(new Jsr77Naming(), null);
        XmlObject xmlObject = XmlBeansUtil.parse(text);
        XmlCursor cursor = xmlObject.newCursor();
        cursor.toFirstContentToken();
        xmlObject = cursor.getObject();
        HashMap<String, Artifact> locations = new HashMap<String, Artifact>();
        locations.put(null, Artifact.create("test/foo/1.0/car"));
        BundleContext bundleContext = new MockBundleContext(getClass().getClassLoader(), "", new HashMap<Artifact, ConfigurationData>(), locations);
        DeploymentContext context = new DeploymentContext(new File("."), null, new Environment(Artifact.create("test/foo/1.0/car")), null, ConfigurationModuleType.SERVICE, new Jsr77Naming(), new MockConfigurationManager(), Collections.<Repository>emptySet(), bundleContext);
        context.initializeConfiguration();
        AbstractName parentName = new AbstractName(URI.create("test/foo/1.0/car?name=parent,j2eeType=foo"));
        builder.getReferences(xmlObject, context, parentName, bundleContext.getBundle());
        secBuilder.doStop();
    }

}


