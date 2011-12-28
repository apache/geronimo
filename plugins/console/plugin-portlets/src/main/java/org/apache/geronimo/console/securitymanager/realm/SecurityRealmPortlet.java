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
package org.apache.geronimo.console.securitymanager.realm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.security.auth.Subject;
import javax.security.auth.spi.LoginModule;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.car.ManagementHelper;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.deployment.xbeans.AbstractServiceType;
import org.apache.geronimo.deployment.xbeans.ArtifactType;
import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.DependenciesType;
import org.apache.geronimo.deployment.xbeans.EnvironmentType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.ModuleDocument;
import org.apache.geronimo.deployment.xbeans.ModuleType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.deployment.xbeans.XmlAttributeType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.security.jaas.ConfigurationEntryFactory;
import org.apache.geronimo.security.jaas.JaasLoginModuleChain;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleSettings;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.LoginModuleControlFlagEditor;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.security.realm.providers.FileAuditLoginModule;
import org.apache.geronimo.security.realm.providers.GeronimoPasswordCredentialLoginModule;
import org.apache.geronimo.security.realm.providers.NamedUsernamePasswordCredentialLoginModule;
import org.apache.geronimo.security.realm.providers.RepeatedFailureLockoutLoginModule;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerControlFlagType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginConfigDocument;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginConfigType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginModuleType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerOptionType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * A portlet that lists, creates, and edits security realms.
 *
 * @version $Rev$ $Date$
 */
public class SecurityRealmPortlet extends BasePortlet {
    private static final Logger log = LoggerFactory.getLogger(SecurityRealmPortlet.class);
    private final static String[] SKIP_ENTRIES_WITH = new String[]{"geronimo", "tomcat", "tranql", "commons", "directory", "activemq"};
    private static final String LIST_VIEW = "/WEB-INF/view/realmwizard/list.jsp";
    private static final String EDIT_VIEW = "/WEB-INF/view/realmwizard/edit.jsp";
    private static final String SELECT_TYPE_VIEW = "/WEB-INF/view/realmwizard/selectType.jsp";
    private static final String CONFIGURE_VIEW = "/WEB-INF/view/realmwizard/configure.jsp";
    private static final String ADVANCED_VIEW = "/WEB-INF/view/realmwizard/advanced.jsp";
    private static final String TEST_LOGIN_VIEW = "/WEB-INF/view/realmwizard/testLogin.jsp";
    private static final String TEST_RESULTS_VIEW = "/WEB-INF/view/realmwizard/testResults.jsp";
    private static final String SHOW_PLAN_VIEW = "/WEB-INF/view/realmwizard/showPlan.jsp";
    private static final String USAGE_VIEW = "/WEB-INF/view/realmwizard/usage.jsp";
    private static final String LIST_MODE = "list";
    private static final String EDIT_MODE = "edit";
    private static final String SELECT_TYPE_MODE = "type";
    private static final String CONFIGURE_MODE = "configure";
    private static final String ADVANCED_MODE = "advanced";
    private static final String TEST_LOGIN_MODE = "test";
    private static final String TEST_RESULTS_MODE = "results";
    private static final String SHOW_PLAN_MODE = "plan";
    private static final String EDIT_EXISTING_MODE = "editExisting";
    private static final String USAGE_MODE = "usage";
    private static final String SAVE_MODE = "save";
    private static final String MODE_KEY = "mode";
    private static final String CUSTOM_MODE = "custom";

    private static Kernel kernel;

    private PortletRequestDispatcher listView;
    private PortletRequestDispatcher editView;
    private PortletRequestDispatcher selectTypeView;
    private PortletRequestDispatcher configureView;
    private PortletRequestDispatcher advancedView;
    private PortletRequestDispatcher testLoginView;
    private PortletRequestDispatcher testResultsView;
    private PortletRequestDispatcher planView;
    private PortletRequestDispatcher usageView;
    private static final QName GBEAN_QNAME = new QName(ServiceDocument.type.getDocumentElementName().getNamespaceURI(), "gbean");

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        kernel = KernelRegistry.getSingleKernel();
        listView = portletConfig.getPortletContext().getRequestDispatcher(LIST_VIEW);
        editView = portletConfig.getPortletContext().getRequestDispatcher(EDIT_VIEW);
        selectTypeView = portletConfig.getPortletContext().getRequestDispatcher(SELECT_TYPE_VIEW);
        configureView = portletConfig.getPortletContext().getRequestDispatcher(CONFIGURE_VIEW);
        advancedView = portletConfig.getPortletContext().getRequestDispatcher(ADVANCED_VIEW);
        testLoginView = portletConfig.getPortletContext().getRequestDispatcher(TEST_LOGIN_VIEW);
        testResultsView = portletConfig.getPortletContext().getRequestDispatcher(TEST_RESULTS_VIEW);
        planView = portletConfig.getPortletContext().getRequestDispatcher(SHOW_PLAN_VIEW);
        usageView = portletConfig.getPortletContext().getRequestDispatcher(USAGE_VIEW);
    }

    public void destroy() {
        listView = null;
        editView = null;
        selectTypeView = null;
        configureView = null;
        advancedView = null;
        testLoginView = null;
        usageView = null;
        planView = null;
        super.destroy();
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter(MODE_KEY);
        RealmData data = new RealmData();
        data.load(actionRequest);
        if (mode.equals(SELECT_TYPE_MODE)) {
            data.realmType = "Properties File Realm";
            actionResponse.setRenderParameter(MODE_KEY, SELECT_TYPE_MODE);
        } else if (mode.equals("process-" + SELECT_TYPE_MODE)) {
            if (data.getName() != null && !data.getName().trim().equals("")) {
                // Check if realm with the same name already exists
                Artifact artifact = new Artifact("console.realm", getArtifactId(data.getName()), "1.0", "car");
                ConfigurationManager configurationManager = PortletManager.getConfigurationManager();
                if (configurationManager.isInstalled(artifact)) {
                    actionResponse.setRenderParameter(MODE_KEY, SELECT_TYPE_MODE);
                    String error = getLocalizedString(actionRequest, "plugin.errorMsg03");
                    addErrorMessage(actionRequest, error);
                } else {
                    // Config properties have to be set in render since they have values of null
                    if (data.getRealmType().equals("Other")) {
                        actionResponse.setRenderParameter(MODE_KEY, CUSTOM_MODE);
                    } else {
                        actionResponse.setRenderParameter(MODE_KEY, CONFIGURE_MODE);
                    }
                } 
            } else {
                actionResponse.setRenderParameter(MODE_KEY, SELECT_TYPE_MODE);
            }
        } else if (mode.equals("process-" + CONFIGURE_MODE)) {
            final String error = actionTestLoginModuleLoad(actionRequest, data);
            if (error == null) {
                actionResponse.setRenderParameter(MODE_KEY, ADVANCED_MODE);
            } else {
                actionResponse.setRenderParameter(MODE_KEY, CONFIGURE_MODE);
                addErrorMessage(actionRequest, error);
            }
        } else if (mode.equals("process-" + ADVANCED_MODE)) {
            String test = actionRequest.getParameter("test");
            if (test == null || test.equals("true")) {
                actionResponse.setRenderParameter(MODE_KEY, TEST_LOGIN_MODE);
            } else {
                final String error = actionSaveRealm(actionRequest, data);
                if (error == null) {
                    actionResponse.setRenderParameter(MODE_KEY, LIST_MODE);
                } else {
                    actionResponse.setRenderParameter("LoginModuleError", error);
                    actionResponse.setRenderParameter(MODE_KEY, CONFIGURE_MODE);
                }
            }
        } else if (mode.equals("process-" + TEST_LOGIN_MODE)) {
            actionAttemptLogin(data, actionRequest, actionRequest.getPortletSession(true), actionRequest.getParameter("username"), actionRequest.getParameter("password"));
            actionResponse.setRenderParameter(MODE_KEY, TEST_RESULTS_MODE);
        } else if (mode.equals(SHOW_PLAN_MODE)) {
            XmlObject object = actionGeneratePlan(actionRequest, data);
            savePlanToSession(actionRequest.getPortletSession(true), object);
            actionResponse.setRenderParameter(MODE_KEY, SHOW_PLAN_MODE);
        } else if (mode.equals(EDIT_EXISTING_MODE)) {
            actionLoadExistingRealm(actionRequest, data);
            actionResponse.setRenderParameter(MODE_KEY, EDIT_MODE);
        } else if (mode.equals(CONFIGURE_MODE)) {
            if (data.getAbstractName() != null) {
                actionResponse.setRenderParameter(MODE_KEY, EDIT_MODE);
            } else if((data.getRealmType() != null && data.getRealmType().equals("Other"))) {
                actionResponse.setRenderParameter(MODE_KEY, CUSTOM_MODE);
            } else {
                actionResponse.setRenderParameter(MODE_KEY, CONFIGURE_MODE);
            }
        } else if (mode.equals(SAVE_MODE)) {
            final String error = actionSaveRealm(actionRequest, data);
            if (error == null) {
                actionResponse.setRenderParameter(MODE_KEY, LIST_MODE);
            } else {
                actionResponse.setRenderParameter("LoginModuleError", error);
                actionResponse.setRenderParameter(MODE_KEY, CONFIGURE_MODE);
            }
        } else {
            actionResponse.setRenderParameter(MODE_KEY, mode);
        }
        data.store(actionResponse);
    }

    protected void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        if (WindowState.MINIMIZED.equals(renderRequest.getWindowState())) {
            return;
        }
        try {
            String mode = renderRequest.getParameter(MODE_KEY);
            RealmData data = new RealmData();
            data.load(renderRequest);
            renderRequest.setAttribute("realm", data);
            if (mode == null || mode.equals("")) {
                mode = LIST_MODE;
            }
            if (mode.equals(LIST_MODE)) {
                renderList(renderRequest, renderResponse);
            } else if (mode.equals(EDIT_MODE) || mode.equals(CUSTOM_MODE)) {
                renderRequest.setAttribute("mode", mode);
                if(mode.equals(CUSTOM_MODE)) loadDriverJARList(renderRequest);
                renderEdit(renderRequest, renderResponse, data);
            } else if (mode.equals(SELECT_TYPE_MODE)) {
                renderSelectType(renderRequest, renderResponse);
            } else if (mode.equals(CONFIGURE_MODE)) {
                renderConfigure(renderRequest, renderResponse, data);
            } else if (mode.equals(ADVANCED_MODE)) {
                renderAdvanced(renderRequest, renderResponse, data);
            } else if (mode.equals(TEST_LOGIN_MODE)) {
                renderTestLoginForm(renderRequest, renderResponse);
            } else if (mode.equals(TEST_RESULTS_MODE)) {
                renderTestResults(renderRequest, renderResponse);
            } else if (mode.equals(SHOW_PLAN_MODE)) {
                renderPlan(renderRequest, renderResponse);
            } else if (mode.equals(USAGE_MODE)) {
                renderUsage(renderRequest, renderResponse);
            }
        } catch (Throwable e) {
            log.error("Unable to render portlet", e);
        }
    }

    private String actionTestLoginModuleLoad(PortletRequest request, RealmData data) {
        Map options = new HashMap();
        try {
            LoginModule module = loadModule(request, data, options);
            log.warn("Testing with options " + options);
            try {
                PortletManager.testLoginModule(request, module, options);
                return null;
            } catch (Exception e) {
                log.warn("Unable to initialize LoginModule", e);
                return "Unable to initialize LoginModule: " + e.getMessage();
            }
        } catch (Exception e) {
            log.warn("Unable to load LoginModule class", e);
            return "Unable to load LoginModule class: " + e.getMessage();
        }
    }

    private LoginModule loadModule(PortletRequest request, RealmData data, Map options) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassLoader loader = getClass().getClassLoader();
        if (data.jar != null && !data.jar.equals("")) {
            try {
                Artifact one = Artifact.create(data.getJar());
                ListableRepository[] repos = PortletManager.getCurrentServer(request).getRepositories();
                for (int i = 0; i < repos.length; i++) {
                    ListableRepository repo = repos[i];
                    File file = repo.getLocation(one);
                    if (file != null) {
                        loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, loader);
                        break;
                    }
                }
            } catch (MalformedURLException e) {
                log.warn("Repository unable to look up JAR file", e);
            }
        }
        Class cls = loader.loadClass(getSelectedModule(data).getClassName());
        LoginModule module = (LoginModule) cls.newInstance();
        for (Iterator it = data.getOptions().keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            final Object value = data.getOptions().get(key);
            if (value != null && !value.equals("")) {
                options.put(key, value);
            }
        }
        options.put(JaasLoginModuleUse.CLASSLOADER_LM_OPTION, loader);
        return module;
    }

    private void actionAttemptLogin(RealmData data, PortletRequest request, PortletSession session, String username, String password) {
        session.removeAttribute("TestLoginPrincipals");
        session.removeAttribute("TestLoginError");
        Map options = new HashMap();
        try {
            LoginModule module = loadModule(request, data, options);
            Subject sub = PortletManager.testLoginModule(request, module, options, username, password);
            session.setAttribute("TestLoginPrincipals", sub.getPrincipals());
        } catch (Exception e) {
            log.warn("Test login failed", e);
            session.setAttribute("TestLoginError", "Login Failed: " + (e.getMessage() == null ? "no message" : e.getMessage()));
        }
    }

    private String getArtifactId(String name) {
        
        String artifactId = name;
        if(artifactId.indexOf('/') != -1) {
            // slash in artifact-id results in invalid configuration-id and leads to deployment errors.
            // Note: 0x002F = '/'
            artifactId = artifactId.replaceAll("/", "%2F");
        }
        
        return artifactId;
    }
    
    private XmlObject actionGeneratePlan(PortletRequest request, RealmData data) {
        normalize(data);
        ModuleDocument doc = ModuleDocument.Factory.newInstance();
        ModuleType root = doc.addNewModule();
        EnvironmentType environment = root.addNewEnvironment();
        ArtifactType configId = environment.addNewModuleId();
        configId.setGroupId("console.realm");
        String artifactId = getArtifactId(data.getName());
        
        configId.setArtifactId(artifactId);
        configId.setVersion("1.0");
        configId.setType("car");

        // Parent

        DependenciesType dependenciesType = environment.addNewDependencies();
        ArtifactType parent = dependenciesType.addNewDependency();
        parent.setGroupId("org.apache.geronimo.framework");
        parent.setArtifactId("j2ee-security");
        parent.setType("car");
        // Dependencies
        if (data.getJar() != null) {
            ArtifactType artifactType = dependenciesType.addNewDependency();
            Artifact artifact = Artifact.create(data.getJar());
            artifactType.setGroupId(artifact.getGroupId());
            artifactType.setArtifactId(artifact.getArtifactId());
            artifactType.setVersion(artifact.getVersion().toString());
            artifactType.setType(artifact.getType());
        }
        // Build the realm GBean
        GbeanType realm = GbeanType.Factory.newInstance();
        realm.setName(data.getName());
        realm.setClass1("org.apache.geronimo.security.realm.GenericSecurityRealm");
        AttributeType realmName = realm.addNewAttribute();
        realmName.setName("realmName");
        realmName.setStringValue(data.getName());
        AttributeType global = realm.addNewAttribute();
        global.setName("global");
        global.setStringValue(data.getGlobal());
        ReferenceType serverInfo = realm.addNewReference();
        serverInfo.setName2("ServerInfo");
        serverInfo.setName((String) PortletManager.getNameFor(request, PortletManager.getCurrentServer(request).getServerInfo()).getName().get("name"));
        XmlAttributeType config = realm.addNewXmlReference();
        // Construct the content to put in the XmlAttributeType
        GerLoginConfigDocument lcDoc = GerLoginConfigDocument.Factory.newInstance();
        GerLoginConfigType login = lcDoc.addNewLoginConfig();
        for (int i = 0; i < data.getModules().length; i++) {
            LoginModuleDetails details = data.getModules()[i];
            if (details.getLoginDomainName() == null || details.getLoginDomainName().equals("")) {
                continue;
            }
            GerLoginModuleType module = login.addNewLoginModule();
            module.setControlFlag(details.getControlFlag().equals(LoginModuleControlFlag.OPTIONAL) ? GerControlFlagType.OPTIONAL :
                    details.getControlFlag().equals(LoginModuleControlFlag.REQUIRED) ? GerControlFlagType.REQUIRED :
                            details.getControlFlag().equals(LoginModuleControlFlag.REQUISITE) ? GerControlFlagType.REQUISITE :
                                    details.getControlFlag().equals(LoginModuleControlFlag.SUFFICIENT) ? GerControlFlagType.SUFFICIENT :
                                            GerControlFlagType.OPTIONAL);
            module.setLoginDomainName(details.getLoginDomainName());
            module.setLoginModuleClass(details.getClassName());
            module.setWrapPrincipals(details.isWrapPrincipals());
            for (Iterator it = details.getOptions().entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                GerOptionType option = module.addNewOption();
                option.setName((String) entry.getKey());
                option.setStringValue((String) entry.getValue());
            }

            // bit of a hack -- to put the DataSource module in as a parent for SQL modules
            if (details.getClassName().indexOf("SQL") > -1) {
                String poolName = (String) details.getOptions().get("dataSourceName");
                String appName = (String) details.getOptions().get("dataSourceApplication");
                if (poolName != null) {
                    if (appName == null) appName = "null";
                    JCAManagedConnectionFactory[] factories = PortletManager.getOutboundFactoriesOfType(request, "javax.sql.DataSource");
                    for (int j = 0; j < factories.length; j++) {
                        JCAManagedConnectionFactory factory = factories[j];
                        try {
                            ObjectName objectName = ObjectName.getInstance(factory.getObjectName());
                            final String testName = objectName.getKeyProperty(NameFactory.J2EE_NAME);
                            final String testApp = objectName.getKeyProperty(NameFactory.J2EE_APPLICATION);
                            if (testName.equals(poolName) && testApp.equals(appName)) {
                                String moduleName = objectName.getKeyProperty(NameFactory.RESOURCE_ADAPTER_MODULE);

                                ArtifactType artifactType = dependenciesType.addNewDependency();
                                Artifact artifact = Artifact.create(moduleName);
                                artifactType.setGroupId(artifact.getGroupId());
                                artifactType.setArtifactId(artifact.getArtifactId());
                                artifactType.setVersion(artifact.getVersion().toString());
                                artifactType.setType(artifact.getType());
                                break;
                            }
                        } catch (MalformedObjectNameException e) {
                            log.error("Unable to parse ObjectName", e);
                        }
                    }
                }
            }
        }
        // Copy the content into the XmlAttributeType
        XmlCursor loginCursor = lcDoc.newCursor();
        loginCursor.toFirstContentToken();
        XmlCursor destination = config.newCursor();
        destination.toNextToken();
        loginCursor.moveXml(destination);
        loginCursor.dispose();
        destination.dispose();
        config.setName("LoginModuleConfiguration");
        root.setServiceArray(new AbstractServiceType[]{realm});


        //Above code inserts gbean using xsi:type=dep:GBeanType.  We also need to account for the substitution group
        //by changing the qname:
        XmlCursor gbeanCursor = root.newCursor();
        try {
            if (!gbeanCursor.toChild(ServiceDocument.type.getDocumentElementName())) {
                throw new RuntimeException("Could not find service element");
            }
            gbeanCursor.setName(GBEAN_QNAME);
        } finally {
            gbeanCursor.dispose();
        }

        return doc;
    }

    private void actionLoadExistingRealm(PortletRequest request, RealmData data) {
        SecurityRealm realm = (SecurityRealm) PortletManager.getManagedBean(request, new AbstractName(URI.create(data.getAbstractName())));
        data.name = realm.getRealmName();
        data.global = ((ConfigurationEntryFactory)realm).isGlobal();
        List list = new ArrayList();
        JaasLoginModuleChain node = realm.getLoginModuleChain();
        while (node != null) {
            LoginModuleDetails details = new LoginModuleDetails();
            details.setControlFlag(node.getControlFlag());
            LoginModuleSettings module = node.getLoginModule();
            details.setLoginDomainName(module.getLoginDomainName());
            details.setClassName(module.getLoginModuleClass());
            details.setWrapPrincipals(module.isWrapPrincipals());
            details.setOptions(module.getOptions());
            list.add(details);
            node = node.getNext();
            if (node == null) {
                break;
            }
        }
        data.modules = (LoginModuleDetails[]) list.toArray(new LoginModuleDetails[list.size()]);
    }

    private String actionSaveRealm(PortletRequest request, RealmData data) {
        normalize(data);
        if (data.getAbstractName() == null || data.getAbstractName().equals("")) { // we're creating a new realm
            try {
                XmlObject plan = actionGeneratePlan(request, data);
                data.name = data.name.replaceAll("\\s", "");
                DeploymentManager mgr = ManagementHelper.getManagementHelper(request).getDeploymentManager();
                File tempFile = File.createTempFile("console-deployment", ".xml");
                tempFile.deleteOnExit();
                log.debug("Writing security realm deployment plan to " + tempFile.getAbsolutePath());
                PrintWriter out = new PrintWriter(new FileWriter(tempFile));
                savePlanToStream(plan, out);
                out.flush();
                out.close();
                Target[] targets = mgr.getTargets();
                if (null == targets) {
                    throw new IllegalStateException("No target to distribute to");
                }
                targets = new Target[] {targets[0]};
                
                ProgressObject po = mgr.distribute(targets, null, tempFile);
                waitForProgress(po);
                if (po.getDeploymentStatus().isCompleted()) {
                    TargetModuleID[] ids = po.getResultTargetModuleIDs();
                    po = mgr.start(ids);
                    waitForProgress(po);
                    if (po.getDeploymentStatus().isCompleted()) {
                        log.info("Deployment completed successfully!");
                    }
                    else {
                        return "Unable to save security realm ";
                    }
                }
                else {
                    return "Unable to save security realm ";
                }
            } catch (IOException e) {
                log.error("Unable to save security realm", e);
                return "Unable to save security realm: " + e.getMessage();
            }
        } else {
            SecurityRealm realm = (SecurityRealm) PortletManager.getManagedBean(request, new AbstractName(URI.create(data.getAbstractName())));
            // index existing modules
            Map nodes = new HashMap();
            JaasLoginModuleChain node = realm.getLoginModuleChain();
            while (node != null) {
                LoginModuleSettings module = node.getLoginModule();
                nodes.put(module.getLoginDomainName(), node);
                node = node.getNext();
                if (node == null) {
                    break;
                }
            }
            // apply settings
            for (int i = 0; i < data.getModules().length; i++) {
                LoginModuleDetails details = data.getModules()[i];
                node = (JaasLoginModuleChain)PortletManager.getManagedBean(request, PortletManager.getNameFor(request, nodes.get(details.getLoginDomainName())));
                node.setControlFlag(details.getControlFlag());
                LoginModuleSettings module =(LoginModuleSettings)PortletManager.getManagedBean(request, PortletManager.getNameFor(request, node.getLoginModule()));
                module.setOptions(details.getOptions());
                module.setWrapPrincipals(details.isWrapPrincipals());
                module.setLoginModuleClass(details.getClassName());
            }
        }
        return null;
    }

    private void renderList(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        // Unfortunately there are two classes named SecurityRealm; one extends the other
        // The array type is management.geronimo.SecurityRealm (the superclass)
        // The array entry types are security.realm.SecurityRealm (the subclass)
        org.apache.geronimo.management.geronimo.SecurityRealm[] realms = PortletManager.getCurrentServer(request).getSecurityRealms();
        ExistingRealm[] results = new ExistingRealm[realms.length];

        // ConfigurationManager is used to determine if the SecurityRealm is deployed as a "SERVICE", i.e., "Server-wide"
        ConfigurationManager configMgr = null;
        if(results.length > 0) {
            // Needed only when there are any SecurityRealms
            configMgr = PortletManager.getConfigurationManager();
        }
        for (int i = 0; i < results.length; i++) {
            AbstractName abstractName = PortletManager.getNameFor(request, realms[i]);
            String parent;
            Configuration parentConfig = configMgr.getConfiguration(abstractName.getArtifact());
            ConfigurationModuleType parentType = parentConfig.getModuleType();
            if(ConfigurationModuleType.SERVICE.equals(parentType)) {
                parent = null; // Server-wide
            } else {
                parent = abstractName.getArtifact().toString();
            }
            results[i] = new ExistingRealm(realms[i].getRealmName(), abstractName, parent);
        }
        // Once done, release the ConfigurationManager
        if(configMgr != null) {
            ConfigurationUtil.releaseConfigurationManager(kernel, configMgr);
        }
        request.setAttribute("realms", results);
        listView.include(request, response);
    }

    private void renderEdit(RenderRequest request, RenderResponse response, RealmData data) throws IOException, PortletException {
        normalize(data);
        editView.include(request, response);
    }

    private void renderSelectType(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        request.setAttribute("moduleTypes", MasterLoginModuleInfo.getAllModules());
        selectTypeView.include(request, response);
    }

    private void renderConfigure(RenderRequest request, RenderResponse response, RealmData data) throws IOException, PortletException {
        // Clear out any cached modules
        data.modules = null;
        // Configure option list
        MasterLoginModuleInfo info = getSelectedModule(data);
        for (int i = 0; i < info.getOptions().length; i++) {
            MasterLoginModuleInfo.OptionInfo option = info.getOptions()[i];
            if (!data.getOptions().containsKey(option.getName())) {
                data.getOptions().put(option.getName(), null);
            }
        }
        data.reorderOptions(info.getOptions());
        request.setAttribute("optionMap", info.getOptionMap());
        if (info.getName().indexOf("SQL") > -1) {
            loadDriverJARList(request);
            loadDatabasePoolList(request);
        }
        configureView.include(request, response);
    }

    private void renderAdvanced(RenderRequest request, RenderResponse response, RealmData data) throws IOException, PortletException {
        // Clear out any cached modules
        data.modules = null;
        // Show the page
        advancedView.include(request, response);
    }

    private void renderTestLoginForm(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        testLoginView.include(request, response);
    }

    private void renderTestResults(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        PortletSession session = request.getPortletSession();
        String status = (String) session.getAttribute("TestLoginError");
        if (status == null) {
            Set principals = (Set) session.getAttribute("TestLoginPrincipals");
            status = "Login succeeded with " + (principals == null ? 0 : principals.size()) + " principals";
            request.setAttribute("principals", principals);
        }
        request.setAttribute("LoginResults", status);
        testResultsView.include(request, response);
    }

    private void renderPlan(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        String plan = (String) request.getPortletSession().getAttribute("SecurityRealmPlan");
        request.setAttribute("deploymentPlan", plan);
        planView.include(request, response);
    }

    private void renderUsage(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        usageView.include(request, response);
    }

    private static MasterLoginModuleInfo getSelectedModule(RealmData data) {
        MasterLoginModuleInfo[] all = MasterLoginModuleInfo.getAllModules();
        for (int i = 0; i < all.length; i++) {
            MasterLoginModuleInfo info = all[i];
            if (info.getName().equals(data.getRealmType())) {
                return info;
            }
        }
        return null;
    }

    private void loadDatabasePoolList(RenderRequest renderRequest) {
        JCAManagedConnectionFactory[] factories = PortletManager.getOutboundFactoriesOfType(renderRequest, "javax.sql.DataSource");
        List pools = new ArrayList();
        try {
            for (int i = 0; i < factories.length; i++) {
                JCAManagedConnectionFactory factory = factories[i];
                ObjectName objectName = ObjectName.getInstance(factory.getObjectName());
                final String name = objectName.getKeyProperty(NameFactory.J2EE_NAME);
                String display = name;
                final String appName = objectName.getKeyProperty(NameFactory.J2EE_APPLICATION);
                if (appName != null && !appName.equals("null")) {
                    display = display + " (" + appName + ")";
                }
                pools.add(new DatabasePool(name, display, appName, PortletManager.getNameFor(renderRequest, factory)));
            }
            renderRequest.setAttribute("pools", pools);
        } catch (MalformedObjectNameException e) {
            log.error("Unable to parse ObjectName", e);
        }
    }

    private void loadDriverJARList(RenderRequest renderRequest) {
        // List the available JARs
        List list = new ArrayList();
        ListableRepository[] repos = PortletManager.getCurrentServer(renderRequest).getRepositories();
        for (int i = 0; i < repos.length; i++) {
            ListableRepository repo = repos[i];

            SortedSet artifacts = repo.list();
            outer:
            for (Iterator iterator = artifacts.iterator(); iterator.hasNext();) {
                Artifact artifact = (Artifact) iterator.next();
                String test = artifact.toString();
                // todo should only test groupId and should check for long (org.apache.geronimo) and short form
                for (int k = 0; k < SKIP_ENTRIES_WITH.length; k++) {
                    String skip = SKIP_ENTRIES_WITH[k];
                    if (test.indexOf(skip) > -1) {
                        continue outer;
                    }
                }
                list.add(test);
            }
        }
        Collections.sort(list);
        renderRequest.setAttribute("jars", list);
    }

    private void savePlanToSession(PortletSession session, XmlObject object) {
        StringWriter out = new StringWriter();
        try {
            savePlanToStream(object, out);
            session.setAttribute("SecurityRealmPlan", out.getBuffer().toString());
        } catch (IOException e) {
            log.error("Unable to write deployment plan", e);
        }
    }

    private void savePlanToStream(XmlObject object, Writer out) throws IOException {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(4);
        options.setUseDefaultNamespace();
        object.save(out, options);
        out.close();
    }

    private static void waitForProgress(ProgressObject po) {
        while (po.getDeploymentStatus().isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public static void normalize(RealmData data) {
        List list = new ArrayList();
        if (data.modules == null) {
            LoginModuleDetails module = new LoginModuleDetails();
            module.setClassName(getSelectedModule(data).getClassName());
            module.setControlFlag(LoginModuleControlFlag.REQUIRED);
            module.setLoginDomainName(data.getName());
            Map<String, Object> props = module.getOptions();
            for (Iterator it = data.getOptions().entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                props.put((String) entry.getKey(), (String) entry.getValue());
            }
            list.add(module);
            if (data.isStorePassword()) {
                module = new LoginModuleDetails();
                module.setClassName(GeronimoPasswordCredentialLoginModule.class.getName());
                module.setControlFlag(LoginModuleControlFlag.OPTIONAL);
                module.setLoginDomainName(data.getName() + "-Password");
                list.add(module);
            }
            if (data.getAuditPath() != null) {
                module = new LoginModuleDetails();
                module.setClassName(FileAuditLoginModule.class.getName());
                module.setControlFlag(LoginModuleControlFlag.OPTIONAL);
                module.setLoginDomainName(data.getName() + "-Audit");
                props = module.getOptions();
                props.put("file", data.getAuditPath());
                list.add(module);
            }
            if (data.isLockoutEnabled()) {
                module = new LoginModuleDetails();
                module.setClassName(RepeatedFailureLockoutLoginModule.class.getName());
                module.setControlFlag(LoginModuleControlFlag.REQUISITE);
                module.setLoginDomainName(data.getName() + "-Lockout");
                props = module.getOptions();
                props.put("failureCount", data.getLockoutCount());
                props.put("failurePeriodSecs", data.getLockoutWindow());
                props.put("lockoutDurationSecs", data.getLockoutDuration());
                list.add(module);
            }
            if (data.getCredentialName() != null) {
                module = new LoginModuleDetails();
                module.setClassName(NamedUsernamePasswordCredentialLoginModule.class.getName());
                module.setControlFlag(LoginModuleControlFlag.OPTIONAL);
                module.setLoginDomainName(data.getName() + "-NamedUPC");
                props = module.getOptions();
                props.put(NamedUsernamePasswordCredentialLoginModule.CREDENTIAL_NAME, data.getCredentialName());
                list.add(module);
            }
        } else {
            list.addAll(Arrays.asList(data.modules));
        }
        if (data.getAbstractName() == null) {
            for (int i = list.size(); i < 5; i++) {
                LoginModuleDetails module = new LoginModuleDetails();
                list.add(module);
            }
        }
        data.modules = (LoginModuleDetails[]) list.toArray(new LoginModuleDetails[list.size()]);
    }

    public static class RealmData implements Serializable {
        private String name;
        private String realmType;
        private String jar;
        private Map options = new LinkedHashMap();
        private String auditPath;
        private String lockoutCount;
        private String lockoutWindow;
        private String lockoutDuration;
        private boolean storePassword;
        private String abstractName; // used when editing existing realms
        private LoginModuleDetails[] modules;
        private String credentialName;
        private boolean global;

        public void load(PortletRequest request) {
            name = request.getParameter("name");
            if (name != null && name.equals("")) name = null;
            realmType = request.getParameter("realmType");
            if (realmType != null && realmType.equals("")) realmType = null;
            jar = request.getParameter("jar");
            if (jar != null && jar.equals("")) jar = null;
            auditPath = request.getParameter("auditPath");
            if (auditPath != null && auditPath.equals("")) auditPath = null;
            lockoutCount = request.getParameter("lockoutCount");
            if (lockoutCount != null && lockoutCount.equals("")) lockoutCount = null;
            lockoutWindow = request.getParameter("lockoutWindow");
            if (lockoutWindow != null && lockoutWindow.equals("")) lockoutWindow = null;
            lockoutDuration = request.getParameter("lockoutDuration");
            if (lockoutDuration != null && lockoutDuration.equals("")) lockoutDuration = null;
            abstractName = request.getParameter("abstractName");
            if (abstractName != null && abstractName.equals("")) abstractName = null;
            String test = request.getParameter("storePassword");
            storePassword = test != null && !test.equals("") && !test.equals("false");
            credentialName = request.getParameter("credentialName");
            if (credentialName != null && credentialName.equals("")) credentialName = null;
            String globalStr = request.getParameter("global");
            global = "on".equals(globalStr) || "true".equals(globalStr);
            Map map = request.getParameterMap();
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                if (key.startsWith("option-")) {
                    if (key.equals("option-databasePoolAbstractName"))
                    { // special handling for a data source, where there's one select corresponding to two properties
                        String nameString = request.getParameter(key);
                        if (nameString != null && !nameString.equals("")) {
                            AbstractName an = new AbstractName(URI.create(nameString));
                            options.put("dataSourceName", an.getNameProperty(NameFactory.J2EE_NAME));
                            options.put("dataSourceApplication", an.getNameProperty(NameFactory.J2EE_APPLICATION));
                        }
                    } else {
                        final String optionName = key.substring(7);
                        final String value = request.getParameter(key);
                        if (value != null && !value.equals("")) {
                            options.put(optionName, value);
                        }
                    }
                }
            }
            int count = 0;
            List list = new ArrayList();
            while (true) {
                int index = count;
                ++count;
                String name = request.getParameter("module-domain-" + index);
                if (name == null || name.equals("")) break;
                LoginModuleDetails details = new LoginModuleDetails();
                details.setLoginDomainName(name);
                String cls = request.getParameter("module-class-" + index);
                if (cls == null || cls.equals("")) continue;
                details.setClassName(cls);
                String flag = request.getParameter("module-control-" + index);
                if (flag == null || flag.equals("")) continue;
                details.setControlFlag(toFlag(flag));
                String wrap = request.getParameter("module-wrap-" + index);
                if (wrap == null || wrap.equals("")) continue;
                details.setWrapPrincipals(Boolean.valueOf(wrap).booleanValue());
                String options = request.getParameter("module-options-" + index);
                if (options != null && !options.equals("")) {
                    BufferedReader in = new BufferedReader(new StringReader(options));
                    String line;
                    try {
                        while ((line = in.readLine()) != null) {
                            if (line.startsWith("#") || line.equals("")) {
                                continue;
                            }
                            int pos = line.indexOf('=');
                            if (pos > -1) {
                                details.getOptions().put(line.substring(0, pos), line.substring(pos + 1));
                            }
                        }
                    } catch (IOException e) {
                        log.error("Unable to read properties '" + options + "'", e);
                    }
                }
                list.add(details);
            }
            if (list.size() > 0) {
                modules = (LoginModuleDetails[]) list.toArray(new LoginModuleDetails[list.size()]);
            }
        }

        private LoginModuleControlFlag toFlag(String flag) {
            LoginModuleControlFlagEditor editor = new LoginModuleControlFlagEditor();
            editor.setAsText(flag);
            return (LoginModuleControlFlag) editor.getValue();
        }

        public void reorderOptions(MasterLoginModuleInfo.OptionInfo[] info) {
            if (info == null || info.length == 0) {
                return; // Probably SQL or something that handles this manually
            }
            Map map = new LinkedHashMap();
            for (int i = 0; i < info.length; i++) {
                if (options.containsKey(info[i].getName())) {
                    map.put(info[i].getName(), options.get(info[i].getName()));
                }
            }
            options = map;
        }

        public void store(ActionResponse response) {
            if (name != null) response.setRenderParameter("name", name);
            if (realmType != null) response.setRenderParameter("realmType", realmType);
            if (jar != null) response.setRenderParameter("jar", jar);
            if (auditPath != null) response.setRenderParameter("auditPath", auditPath);
            if (lockoutCount != null) response.setRenderParameter("lockoutCount", lockoutCount);
            if (lockoutWindow != null) response.setRenderParameter("lockoutWindow", lockoutWindow);
            if (lockoutDuration != null) response.setRenderParameter("lockoutDuration", lockoutDuration);
            if (abstractName != null) response.setRenderParameter("abstractName", abstractName);
            if (storePassword) response.setRenderParameter("storePassword", "true");
            if (credentialName != null) response.setRenderParameter("credentialName", credentialName);
            response.setRenderParameter("global", getGlobal());
            for (Object o : options.keySet()) {
                String name = (String) o;
                String value = (String) options.get(name);
                if (value != null) {
                    response.setRenderParameter("option-" + name, value);
                }
            }
            if (modules != null) {
                for (int i = 0; i < modules.length; i++) {
                    LoginModuleDetails module = modules[i];
                    if (module.getLoginDomainName() != null)
                        response.setRenderParameter("module-domain-" + i, module.getLoginDomainName());
                    if (module.getClassName() != null)
                        response.setRenderParameter("module-class-" + i, module.getClassName());
                    if (module.getControlFlag() != null)
                        response.setRenderParameter("module-control-" + i,module.getControlFlag().toString());
                    response.setRenderParameter("module-wrap-" + i, Boolean.toString(module.isWrapPrincipals()));
                    if (module.getOptions().size() > 0)
                        response.setRenderParameter("module-options-" + i, module.getOptionString());
                }
            }

        }

        public String getName() {
            return name;
        }

        public String getRealmType() {
            return realmType;
        }

        public Map getOptions() {
            return options;
        }

        public Set getOptionNames() {
            return options.keySet();
        }

        public String getJar() {
            return jar;
        }

        public String getAuditPath() {
            return auditPath;
        }

        public String getLockoutCount() {
            return lockoutCount;
        }

        public String getLockoutWindow() {
            return lockoutWindow;
        }

        public String getLockoutDuration() {
            return lockoutDuration;
        }

        public boolean isStorePassword() {
            return storePassword;
        }

        public boolean isLockoutEnabled() {
            return lockoutCount != null || lockoutWindow != null || lockoutDuration != null;
        }

        public String getCredentialName() {
            return credentialName;
        }

        public String getAbstractName() {
            return abstractName;
        }

        public boolean isTestable() {
            return getSelectedModule(this).isTestable();
        }

        public LoginModuleDetails[] getModules() {
            return modules;
        }

        public String getGlobal() {
            return Boolean.valueOf(global).toString();
        }
    }

    public static class LoginModuleDetails implements Serializable {
        private String loginDomainName;
        private String className;
        private LoginModuleControlFlag controlFlag;
        private boolean wrapPrincipals = false;
        private Map<String, Object> options = new HashMap<String, Object>();

        public String getLoginDomainName() {
            return loginDomainName;
        }

        public void setLoginDomainName(String loginDomainName) {
            this.loginDomainName = loginDomainName;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public LoginModuleControlFlag getControlFlag() {
            return controlFlag;
        }

        public void setControlFlag(LoginModuleControlFlag controlFlag) {
            this.controlFlag = controlFlag;
        }

        public Map<String, Object> getOptions() {
            return options;
        }

        public void setOptions(Map<String, Object> options) {
            this.options = options;
        }

        public boolean isWrapPrincipals() {
            return wrapPrincipals;
        }

        public void setWrapPrincipals(boolean wrapPrincipals) {
            this.wrapPrincipals = wrapPrincipals;
        }

        public String getOptionString() {
            StringBuilder buf = new StringBuilder();
            for (Iterator it = options.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                buf.append(key).append("=").append(options.get(key)).append("\n");
            }
            return buf.toString();
        }
    }

    public static class ExistingRealm implements Serializable {
        private final String name;
        private final String abstractName;
        private final String parentName;

        public ExistingRealm(String name, AbstractName abstractName, String parent) {
            this.name = name;
            this.abstractName = abstractName.toString();
            parentName = parent;
        }

        public String getName() {
            return name;
        }

        public String getAbstractName() {
            return abstractName;
        }

        public String getParentName() {
            return parentName;
        }

    }

    public static class DatabasePool implements Serializable, Comparable {
        private final String name;
        private final String displayName;
        private final String applicationName;
        private final String abstractName;

        public DatabasePool(String name, String displayName, String applicationName, AbstractName abstractName) {
            this.name = name;
            this.displayName = displayName;
            this.applicationName = applicationName;
            this.abstractName = abstractName.toString();
        }

        public String getName() {
            return name;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public String getAbstractName() {
            return abstractName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int compareTo(Object o) {
            final DatabasePool pool = (DatabasePool) o;
            int names = name.compareTo(pool.name);
            if (applicationName == null) {
                if (pool.applicationName == null) {
                    return names;
                } else {
                    return -1;
                }
            } else {
                if (pool.applicationName == null) {
                    return 1;
                } else {
                    int test = applicationName.compareTo(pool.applicationName);
                    if (test != 0) {
                        return test;
                    } else {
                        return names;
                    }
                }
            }
        }
    }
}
