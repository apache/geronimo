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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;

import javax.portlet.PortletRequest;

import org.apache.geronimo.j2ee.deployment.ApplicationInfo;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.WebModule;

import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationDocument;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerApplicationType;
import org.apache.geronimo.xbeans.geronimo.j2ee.GerModuleType;
import org.apache.geronimo.xbeans.geronimo.web.GerWebAppDocument;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;

/**
 * 
 * @version $Rev$ $Date$
 */
public class EARConfigData {
    private GerApplicationType enterpriseApp = GerApplicationType.Factory.newInstance();

    private EnvironmentConfigData environmentConfig;

    private Hashtable<String, WARConfigData> webModules = new Hashtable<String, WARConfigData>();

    private Hashtable<String, EjbConfigData> ejbModules = new Hashtable<String, EjbConfigData>();

    private String deploymentPlan;

    public EnvironmentConfigData getEnvironmentConfig() {
        return environmentConfig;
    }

    public void parseEAR(ApplicationInfo applicationInfo) {
        environmentConfig = new EnvironmentConfigData(getEnterpriseApp().addNewEnvironment());
        environmentConfig.parseEnvironment(applicationInfo.getEnvironment());

        LinkedHashSet<Module<?,?>> modules = applicationInfo.getModules();
        for (Module<?,?> module : modules) {
            //Module module = (Module) module1;
            if (ConfigurationModuleType.WAR == module.getType()) {
                WARConfigData warConfig = new WARConfigData();
                warConfig.parseReferences(((WebModule) module).getSpecDD());
                warConfig.parseSecurity(((WebModule) module).getSpecDD());
                webModules.put(module.getName(), warConfig);
            } else if (ConfigurationModuleType.EJB == module.getType()) {
                EjbConfigData ejbConfig = new EjbConfigData();
                ejbModules.put(module.getName(), ejbConfig);
            } else {
                System.out.println("Module Type = " + module.getType());
            }
        }
    }

    public void readEnvironmentData(PortletRequest request) {
        environmentConfig.readEnvironmentData(request);
    }

    public void readReferencesData(PortletRequest request) {
        String moduleName = "";
        if (webModules.containsKey(moduleName)) {
            WARConfigData warConfig = webModules.get(moduleName);
            warConfig.readReferencesData(request);
        } else if (ejbModules.contains(moduleName)) {
            EjbConfigData ejbConfig = ejbModules.get(moduleName);
            ejbConfig.readReferencesData(request);
        } else {
            System.out.println("Invaild module name: " + moduleName + " !!");
        }
    }

    public void readSecurityData(PortletRequest request) {
        String moduleName = "";
        if (webModules.containsKey(moduleName)) {
            WARConfigData warConfig = webModules.get(moduleName);
            warConfig.readSecurityData(request);
        } else if (ejbModules.contains(moduleName)) {
            EjbConfigData ejbConfig = ejbModules.get(moduleName);
            ejbConfig.readSecurityData(request);
        } else {
            System.out.println("Invaild module name: " + moduleName + " !!");
        }
    }

    public HashSet<String> consolidateDependencies() {
        HashSet<String> deps = environmentConfig.getDependenciesSet();
        for (Enumeration<WARConfigData> e = webModules.elements(); e.hasMoreElements();) {
            WARConfigData warConfig = e.nextElement();
            deps.addAll(warConfig.consolidateDependencies());
        }
        for (Enumeration<EjbConfigData> e = ejbModules.elements(); e.hasMoreElements();) {
            EjbConfigData ejbConfig = e.nextElement();
            deps.addAll(ejbConfig.consolidateDependencies());
        }
        return deps;
    }

    public String createDeploymentPlan() throws IOException {
        environmentConfig.storeDependencies();
        for (int i = getEnterpriseApp().getModuleArray().length - 1; i >= 0; i--) {
            getEnterpriseApp().removeModule(i);
        }
        for (Enumeration<String> e = webModules.keys(); e.hasMoreElements();) {
            String moduleName = e.nextElement();
            GerModuleType newModule = getEnterpriseApp().addNewModule();
            newModule.addNewWeb().setStringValue(moduleName);

            WARConfigData warConfig = webModules.get(moduleName);

            warConfig.getJndiRefsConfig().storeResourceRefs(warConfig.getWebApp());
            if (warConfig.getSecurity() != null) {
                warConfig.getWebApp().setSecurity(warConfig.getSecurity());
            }
            
            GerWebAppDocument webAppDocument = GerWebAppDocument.Factory.newInstance();
            webAppDocument.setWebApp(warConfig.getWebApp());

            // See http://xmlbeans.apache.org/docs/2.0.0/guide/conHandlingAny.html
            XmlCursor xsAnyCursor = webAppDocument.newCursor();
            xsAnyCursor.toNextToken();
            XmlCursor rootCursor = newModule.newCursor();
            rootCursor.toEndToken();
            xsAnyCursor.moveXml(rootCursor);
            xsAnyCursor.dispose();
            rootCursor.dispose();
        }
        for (Enumeration<String> e = ejbModules.keys(); e.hasMoreElements();) {
            String moduleName = e.nextElement();
            GerModuleType newModule = getEnterpriseApp().addNewModule();
            newModule.addNewEjb().setStringValue(moduleName);
            EjbConfigData ejbConfig = ejbModules.get(moduleName);
        }

        GerApplicationDocument appDocument = GerApplicationDocument.Factory.newInstance();
        appDocument.setApplication(enterpriseApp);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(4);
        options.setUseDefaultNamespace();
        appDocument.save(outputStream, options);
        outputStream.close();
        deploymentPlan = new String(outputStream.toByteArray(), "US-ASCII");
        return deploymentPlan;
    }

    public GerApplicationType getEnterpriseApp() {
        return enterpriseApp;
    }

    public String getDeploymentPlan() {
        try {
            return createDeploymentPlan();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String setDeploymentPlan(String deploymentPlan) {
        this.deploymentPlan = deploymentPlan;
        try {
            GerApplicationDocument doc = GerApplicationDocument.Factory.parse(deploymentPlan);            
            this.enterpriseApp = doc.getApplication();
            this.environmentConfig = new EnvironmentConfigData(this.enterpriseApp.getEnvironment());
        } catch(XmlException e) {
            return e.getMessage();
        }
        return null;
    }

    public Hashtable<String, WARConfigData> getWebModules() {
        return webModules;
    }

    public Hashtable<String, EjbConfigData> getEjbModules() {
        return ejbModules;
    }
}
