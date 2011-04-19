/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.configcreator.configData;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;

import javax.portlet.PortletRequest;

import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppType;
import org.apache.xmlbeans.XmlOptions;

/**
 * 
 * @version $Rev$ $Date$
 */
public class WARConfigData {
    private GerWebAppType webApp = GerWebAppType.Factory.newInstance();

    private EnvironmentConfigData environmentConfig;

    private JndiRefsConfigData jndiRefsConfig;

    private MessageDestinationConfigData messageDestinationConfig;

    private SecurityConfigData securityConfig;

    private String uploadedWarUri;

    private String deploymentPlan;

    public void parseWeb(WebModule module) {
        environmentConfig = new EnvironmentConfigData(getWebApp().addNewEnvironment());
        environmentConfig.parseEnvironment(module.getEnvironment());
        getWebApp().setContextRoot(getWebApp().getEnvironment().getModuleId().getArtifactId());

        parseReferences(module.getSpecDD());
        parseSecurity(module.getSpecDD());
    }

    public void parseReferences(JndiConsumer annotatedApp) {
        jndiRefsConfig = new JndiRefsConfigData();
        jndiRefsConfig.parseWebDD(annotatedApp, webApp);
        messageDestinationConfig = new MessageDestinationConfigData();
        messageDestinationConfig.parseWebDD(annotatedApp, webApp);
    }

    public void parseSecurity(JndiConsumer annotatedApp) {
        securityConfig = new SecurityConfigData();
        securityConfig.parseWebDD(annotatedApp);
    }

    public void readEnvironmentData(PortletRequest request) {
        getWebApp().setContextRoot(request.getParameter("contextRoot"));
        environmentConfig.readEnvironmentData(request);
    }

    public void readReferencesData(PortletRequest request) {
        jndiRefsConfig.readReferencesData(request, getWebApp());
        messageDestinationConfig.readReferencesData(request, getWebApp());
    }

    public void readSecurityData(PortletRequest request) {
        getWebApp().setSecurityRealmName(request.getParameter("securityRealmName"));
        securityConfig.readSecurityData(request);
    }

    public HashSet<String> consolidateDependencies() {
        HashSet<String> deps = environmentConfig.getDependenciesSet();
        deps.addAll(jndiRefsConfig.getDependenciesSet());
        deps.addAll(messageDestinationConfig.getDependenciesSet());
        deps.addAll(securityConfig.getDependenciesSet());
        return deps;
    }

    public String createDeploymentPlan() throws IOException {
        environmentConfig.storeDependencies();
        jndiRefsConfig.storeResourceRefs(getWebApp());
        if (securityConfig.getSecurity() != null) {
            getWebApp().setSecurity(securityConfig.getSecurity());
        }

        GerWebAppDocument webAppDocument = GerWebAppDocument.Factory.newInstance();
        webAppDocument.setWebApp(getWebApp());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(4);
        options.setUseDefaultNamespace();
        webAppDocument.save(outputStream, options);
        outputStream.close();
        deploymentPlan = new String(outputStream.toByteArray(), "US-ASCII");
        return deploymentPlan;
    }

    public GerWebAppType getWebApp() {
        return webApp;
    }

    public EnvironmentConfigData getEnvironmentConfig() {
        return environmentConfig;
    }

    public JndiRefsConfigData getJndiRefsConfig() {
        return jndiRefsConfig;
    }

    public GerSecurityType getSecurity() {
        return securityConfig.getSecurity();
    }

    public boolean isReferenceNotResolved() {
        return jndiRefsConfig.isReferenceNotResolved() || messageDestinationConfig.isReferenceNotResolved();
    }

    public void setUploadedWarUri(String uploadedWarUri) {
        this.uploadedWarUri = uploadedWarUri;
    }

    public String getUploadedWarUri() {
        return uploadedWarUri;
    }

    public String getDeploymentPlan() {
        return deploymentPlan;
    }

    public void setDeploymentPlan(String deploymentPlan) {
        this.deploymentPlan = deploymentPlan;
    }

    public boolean needsResolveReferences() {
        if (getWebApp().getEjbRefArray().length > 0 || getWebApp().getEjbLocalRefArray().length > 0
                || getWebApp().getServiceRefArray().length > 0 || getWebApp().getResourceEnvRefArray().length > 0
                || getJndiRefsConfig().getJdbcPoolRefs().size() > 0
                || getJndiRefsConfig().getJavaMailSessionRefs().size() > 0
                || getJndiRefsConfig().getJmsConnectionFactoryRefs().size() > 0) {
            return true;
        }
        return false;
    }
}
