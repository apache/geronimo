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
package org.apache.geronimo.console.securitymanager.realm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Arrays;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.deployment.xbeans.AttributeType;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.deployment.xbeans.DependencyType;
import org.apache.geronimo.deployment.xbeans.GbeanType;
import org.apache.geronimo.deployment.xbeans.ReferenceType;
import org.apache.geronimo.deployment.xbeans.XmlAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.proxy.GeronimoManagedBean;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.security.realm.SecurityRealm;
import org.apache.geronimo.security.realm.providers.FileAuditLoginModule;
import org.apache.geronimo.security.realm.providers.GeronimoPasswordCredentialLoginModule;
import org.apache.geronimo.security.realm.providers.RepeatedFailureLockoutLoginModule;
import org.apache.geronimo.security.jaas.JaasLoginModuleChain;
import org.apache.geronimo.security.jaas.LoginModuleSettings;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerControlFlagType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginConfigDocument;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginConfigType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginModuleType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerOptionType;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 * A portlet that lists, creates, and edits security realms.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class SecurityRealmPortlet extends BasePortlet {
    private final static Log log = LogFactory.getLog(SecurityRealmPortlet.class);
    private final static String[] SKIP_ENTRIES_WITH = new String[]{"geronimo", "tomcat", "tranql", "commons", "directory", "activemq"};
    private static final String LIST_VIEW          = "/WEB-INF/view/realmwizard/list.jsp";
    private static final String EDIT_VIEW          = "/WEB-INF/view/realmwizard/edit.jsp";
    private static final String SELECT_TYPE_VIEW   = "/WEB-INF/view/realmwizard/selectType.jsp";
    private static final String CONFIGURE_VIEW     = "/WEB-INF/view/realmwizard/configure.jsp";
    private static final String ADVANCED_VIEW      = "/WEB-INF/view/realmwizard/advanced.jsp";
    private static final String TEST_LOGIN_VIEW    = "/WEB-INF/view/realmwizard/testLogin.jsp";
    private static final String TEST_RESULTS_VIEW  = "/WEB-INF/view/realmwizard/testResults.jsp";
    private static final String SHOW_PLAN_VIEW     = "/WEB-INF/view/realmwizard/showPlan.jsp";
    private static final String USAGE_VIEW         = "/WEB-INF/view/realmwizard/usage.jsp";
    private static final String LIST_MODE          = "list";
    private static final String EDIT_MODE          = "edit";
    private static final String SELECT_TYPE_MODE   = "type";
    private static final String CONFIGURE_MODE     = "configure";
    private static final String ADVANCED_MODE      = "advanced";
    private static final String TEST_LOGIN_MODE    = "test";
    private static final String TEST_RESULTS_MODE  = "results";
    private static final String SHOW_PLAN_MODE     = "plan";
    private static final String EDIT_EXISTING_MODE = "editExisting";
    private static final String USAGE_MODE         = "usage";
    private static final String SAVE_MODE          = "save";
    private static final String MODE_KEY = "mode";

    private PortletRequestDispatcher listView;
    private PortletRequestDispatcher editView;
    private PortletRequestDispatcher selectTypeView;
    private PortletRequestDispatcher configureView;
    private PortletRequestDispatcher advancedView;
    private PortletRequestDispatcher testLoginView;
    private PortletRequestDispatcher testResultsView;
    private PortletRequestDispatcher planView;
    private PortletRequestDispatcher usageView;

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
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
        if(mode.equals(SELECT_TYPE_MODE)) {
            data.realmType="Properties File Realm";
            actionResponse.setRenderParameter(MODE_KEY, SELECT_TYPE_MODE);
        } else if(mode.equals("process-"+SELECT_TYPE_MODE)) {
            if(data.getName() != null && !data.getName().trim().equals("")) {
                // Config properties have to be set in render since they have values of null
                if(data.getRealmType().equals("Other")) {
                    actionResponse.setRenderParameter(MODE_KEY, EDIT_MODE);
                } else {
                    actionResponse.setRenderParameter(MODE_KEY, CONFIGURE_MODE);
                }
            } else {
                actionResponse.setRenderParameter(MODE_KEY, SELECT_TYPE_MODE);
            }
        } else if(mode.equals("process-"+CONFIGURE_MODE)) {
            final String error = actionTestLoginModuleLoad(actionRequest, data);
            if(error == null) {
                actionResponse.setRenderParameter(MODE_KEY, ADVANCED_MODE);
            } else {
                actionResponse.setRenderParameter("LoginModuleError", error);
                actionResponse.setRenderParameter(MODE_KEY, CONFIGURE_MODE);
            }
        } else if(mode.equals("process-"+ADVANCED_MODE)) {
            String test = actionRequest.getParameter("test");
            if(test == null || test.equals("true")) {
                actionResponse.setRenderParameter(MODE_KEY, TEST_LOGIN_MODE);
            } else {
                actionSaveRealm(actionRequest, data);
                actionResponse.setRenderParameter(MODE_KEY, LIST_MODE);
            }
        } else if(mode.equals("process-"+TEST_LOGIN_MODE)) {
            actionAttemptLogin(data, actionRequest, actionRequest.getPortletSession(true), actionRequest.getParameter("username"), actionRequest.getParameter("password"));
            actionResponse.setRenderParameter(MODE_KEY, TEST_RESULTS_MODE);
        } else if(mode.equals(SHOW_PLAN_MODE)) {
            XmlObject object = actionGeneratePlan(actionRequest, data);
            savePlanToSession(actionRequest.getPortletSession(true), object);
            actionResponse.setRenderParameter(MODE_KEY, SHOW_PLAN_MODE);
        } else if(mode.equals(EDIT_EXISTING_MODE)) {
            actionLoadExistingRealm(actionRequest, data);
            actionResponse.setRenderParameter(MODE_KEY, EDIT_MODE);
        } else if(mode.equals(CONFIGURE_MODE)) {
            if(data.getObjectName() != null || (data.getRealmType() != null && data.getRealmType().equals("Other"))) {
                actionResponse.setRenderParameter(MODE_KEY, EDIT_MODE);
            } else {
                actionResponse.setRenderParameter(MODE_KEY, CONFIGURE_MODE);
            }
        } else if(mode.equals(SAVE_MODE)) {
            actionSaveRealm(actionRequest, data);
            actionResponse.setRenderParameter(MODE_KEY, LIST_MODE);
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
            if(mode == null || mode.equals("")) {
                mode = LIST_MODE;
            }
            if(mode.equals(LIST_MODE)) {
                renderList(renderRequest, renderResponse);
            } else if(mode.equals(EDIT_MODE)) {
                renderEdit(renderRequest, renderResponse, data);
            } else if(mode.equals(SELECT_TYPE_MODE)) {
                renderSelectType(renderRequest, renderResponse);
            } else if(mode.equals(CONFIGURE_MODE)) {
                renderConfigure(renderRequest, renderResponse, data);
            } else if(mode.equals(ADVANCED_MODE)) {
                renderAdvanced(renderRequest, renderResponse, data);
            } else if(mode.equals(TEST_LOGIN_MODE)) {
                renderTestLoginForm(renderRequest, renderResponse);
            } else if(mode.equals(TEST_RESULTS_MODE)) {
                renderTestResults(renderRequest, renderResponse);
            } else if(mode.equals(SHOW_PLAN_MODE)) {
                renderPlan(renderRequest, renderResponse);
            } else if(mode.equals(USAGE_MODE)) {
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
            try {
                PortletManager.testLoginModule(request, module, options);
                return null;
            } catch (Exception e) {
                log.warn("Unable to initialize LoginModule", e);
                return "Unable to initialize LoginModule: "+e.getMessage();
            }
        } catch (Exception e) {
            log.warn("Unable to load LoginModule class", e);
            return "Unable to load LoginModule class: "+e.getMessage();
        }
    }

    private LoginModule loadModule(PortletRequest request, RealmData data, Map options) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassLoader loader = getClass().getClassLoader();
        if(data.jar != null && !data.jar.equals("")) {
            try {
                URI one = new URI(data.getJar());
                ListableRepository[] repos = PortletManager.getListableRepositories(request);
                for (int i = 0; i < repos.length; i++) {
                    ListableRepository repo = repos[i];
                    URL url = repo.getURL(one);
                    if(url != null) {
                        loader = new URLClassLoader(new URL[]{url}, loader);
                        break;
                    }
                }
            } catch (URISyntaxException e) {
                log.warn("Unable to construct JAR file reference", e);
            } catch (MalformedURLException e) {
                log.warn("Repository unable to look up JAR file", e);
            }
        }
        Class cls = loader.loadClass(getSelectedModule(data).getClassName());
        LoginModule module = (LoginModule) cls.newInstance();
        for (Iterator it = data.getOptions().keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            final Object value = data.getOptions().get(key);
            if(value != null && !value.equals("")) {
                options.put(key, value);
            }
        }
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
            session.setAttribute("TestLoginError", "Login Failed: "+(e.getMessage() == null ? "no message" : e.getMessage()));
        }
    }

    private XmlObject actionGeneratePlan(PortletRequest request, RealmData data) {
        normalize(data);
        ConfigurationDocument doc = ConfigurationDocument.Factory.newInstance();
        ConfigurationType root = doc.addNewConfiguration();
        root.setConfigId("SecurityRealm"+data.getName());
        root.setParentId("org/apache/geronimo/Server");
        // Dependencies
        if(data.getJar() != null) {
            DependencyType jar = root.addNewDependency();
            jar.setUri(data.getJar());
        }
        // Build the realm GBean
        GbeanType realm = root.addNewGbean();
        realm.setName(data.getName());
        realm.setClass1("org.apache.geronimo.security.realm.GenericSecurityRealm");
        AttributeType realmName = realm.addNewAttribute();
        realmName.setName("realmName");
        realmName.setStringValue(data.getName());
        ReferenceType serverInfo = realm.addNewReference();
        serverInfo.setName2("ServerInfo");
        serverInfo.setGbeanName(PortletManager.getCurrentServer(request).getServerInfo());
        XmlAttributeType config = realm.addNewXmlReference();
        // Construct the content to put in the XmlAttributeType
        GerLoginConfigDocument lcDoc = GerLoginConfigDocument.Factory.newInstance();
        GerLoginConfigType login = lcDoc.addNewLoginConfig();
        for (int i = 0; i < data.getModules().length; i++) {
            LoginModuleDetails details = data.getModules()[i];
            if(details.getLoginDomainName() == null || details.getLoginDomainName().equals("")) {
                continue;
            }
            GerLoginModuleType module = login.addNewLoginModule();
            module.setControlFlag(details.getControlFlag().equals("OPTIONAL") ? GerControlFlagType.OPTIONAL :
                    details.getControlFlag().equals("REQUIRED") ? GerControlFlagType.REQUIRED :
                    details.getControlFlag().equals("REQUISITE") ? GerControlFlagType.REQUISITE :
                    details.getControlFlag().equals("SUFFICIENT") ? GerControlFlagType.SUFFICIENT :
                    GerControlFlagType.OPTIONAL);
            module.setServerSide(details.isServerSide());
            module.setLoginDomainName(details.getLoginDomainName());
            module.setLoginModuleClass(details.getClassName());
            for (Iterator it = details.getOptions().entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                GerOptionType option = module.addNewOption();
                option.setName((String) entry.getKey());
                option.setStringValue((String) entry.getValue());
            }

            // bit of a hack -- to put the DataSource module in as a parent for SQL modules
            if(details.getClassName().indexOf("SQL") > -1) {
                String poolName = (String) details.getOptions().get("dataSourceName");
                String appName = (String) details.getOptions().get("dataSourceApplication");
                if(poolName != null) {
                    if(appName == null) appName = "null";
                    JCAManagedConnectionFactory[] factories = PortletManager.getOutboundFactoriesOfType(request, "javax.sql.DataSource");
                    for (int j = 0; j < factories.length; j++) {
                        JCAManagedConnectionFactory factory = factories[j];
                        try {
                            ObjectName objectName = ObjectName.getInstance(factory.getObjectName());
                            final String testName = objectName.getKeyProperty(NameFactory.J2EE_NAME);
                            final String testApp = objectName.getKeyProperty(NameFactory.J2EE_APPLICATION);
                            if(testName.equals(poolName) && testApp.equals(appName)) {
                                String moduleName = objectName.getKeyProperty(NameFactory.JCA_RESOURCE);
                                DependencyType imp = root.addNewImport();
                                imp.setUri(moduleName);
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

        return doc;
    }

    private void actionLoadExistingRealm(PortletRequest request, RealmData data) {
        SecurityRealm realm = (SecurityRealm) PortletManager.getManagedBean(request, data.getObjectName());
        data.name = realm.getRealmName();
        List list = new ArrayList();
        JaasLoginModuleChain node = (JaasLoginModuleChain) PortletManager.getManagedBean(request, realm.getLoginModuleChainName());
        while(node != null) {
            LoginModuleDetails details = new LoginModuleDetails();
            details.setControlFlag(node.getControlFlag());
            LoginModuleSettings module = (LoginModuleSettings) PortletManager.getManagedBean(request, node.getLoginModuleName());
            details.setLoginDomainName(module.getLoginDomainName());
            details.setClassName(module.getLoginModuleClass());
            details.setServerSide(module.isServerSide());
            details.setOptions(module.getOptions());
            list.add(details);
            final String next = node.getNextName();
            if(next == null) {
                break;
            }
            node = (JaasLoginModuleChain) PortletManager.getManagedBean(request, next);
        }
        data.modules = (LoginModuleDetails[]) list.toArray(new LoginModuleDetails[list.size()]);
    }

    private void actionSaveRealm(PortletRequest request, RealmData data) {
        normalize(data);
        if(data.objectName == null || data.objectName.equals("")) { // we're creating a new realm
            try {
                XmlObject plan = actionGeneratePlan(request, data);
                data.name = data.name.replaceAll("\\s", "");
                DeploymentManager mgr = PortletManager.getDeploymentManager(request);
                File tempFile = File.createTempFile("console-deployment",".xml");
                tempFile.deleteOnExit();
                log.debug("Writing security realm deployment plan to "+tempFile.getAbsolutePath());
                PrintWriter out = new PrintWriter(new FileWriter(tempFile));
                savePlanToStream(plan, out);
                out.flush();
                out.close();
                Target[] targets = mgr.getTargets();
                ProgressObject po = mgr.distribute(targets, null, tempFile);
                waitForProgress(po);
                if(po.getDeploymentStatus().isCompleted()) {
                    TargetModuleID[] ids = po.getResultTargetModuleIDs();
                    po = mgr.start(ids);
                    waitForProgress(po);
                    if(po.getDeploymentStatus().isCompleted()) {
                        System.out.println("Deployment completed successfully!");
                    }
                }
            } catch (IOException e) {
                log.error("Unable to save security realm", e);
            }
        } else {
            SecurityRealm realm = (SecurityRealm) PortletManager.getManagedBean(request, data.getObjectName());
            // index existing modules
            Map nodes = new HashMap();
            JaasLoginModuleChain node = (JaasLoginModuleChain) PortletManager.getManagedBean(request, realm.getLoginModuleChainName());
            while(node != null) {
                LoginModuleSettings module = (LoginModuleSettings) PortletManager.getManagedBean(request, node.getLoginModuleName());
                nodes.put(module.getLoginDomainName(), node);
                final String next = node.getNextName();
                if(next == null) {
                    break;
                }
                node = (JaasLoginModuleChain) PortletManager.getManagedBean(request, next);
            }
            // apply settings
            for (int i = 0; i < data.getModules().length; i++) {
                LoginModuleDetails details = data.getModules()[i];
                node = (JaasLoginModuleChain) nodes.get(details.getLoginDomainName());
                node.setControlFlag(details.getControlFlag());
                LoginModuleSettings module = (LoginModuleSettings) PortletManager.getManagedBean(request, node.getLoginModuleName());
                module.setOptions(details.getOptions());
                module.setServerSide(details.isServerSide());
                module.setLoginModuleClass(details.getClassName());
            }
        }
    }

    private void renderList(RenderRequest request, RenderResponse response) throws IOException, PortletException {
        SecurityRealm[] realms = PortletManager.getSecurityRealms(request);
        ExistingRealm[] results = new ExistingRealm[realms.length];
        for (int i = 0; i < results.length; i++) {
            final GeronimoManagedBean managedBean = (GeronimoManagedBean)realms[i];
            try {
                results[i] = new ExistingRealm(realms[i].getRealmName(), ObjectName.getInstance(managedBean.getObjectName()),
                        managedBean.getState());
            } catch (MalformedObjectNameException e) {
                log.error("Unable to retrieve ObjectName for security realm", e);
            }
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
        // Pass errors through
        if(request.getParameter("LoginModuleError") != null) {
            request.setAttribute("LoginModuleError", request.getParameter("LoginModuleError"));
        }
        // Clear out any cached modules
        data.modules = null;
        // Configure option list
        MasterLoginModuleInfo info = getSelectedModule(data);
        for (int i = 0; i < info.getOptions().length; i++) {
            MasterLoginModuleInfo.OptionInfo option = info.getOptions()[i];
            if(!data.getOptions().containsKey(option.getName())) {
                data.getOptions().put(option.getName(), null);
            }
        }
        data.reorderOptions(info.getOptions());
        request.setAttribute("optionMap", info.getOptionMap());
        if(info.getName().indexOf("SQL") > -1) {
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
        if(status == null) {
            Set principals = (Set) session.getAttribute("TestLoginPrincipals");
            status = "Login succeeded with "+(principals == null ? 0 : principals.size())+" principals";
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
            if(info.getName().equals(data.getRealmType())) {
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
                if(appName != null && !appName.equals("null")) {
                    display = display+" ("+appName+")";
                }
                pools.add(new DatabasePool(name, display, appName, factory.getObjectName()));
            }
            renderRequest.setAttribute("pools", pools);
        } catch (MalformedObjectNameException e) {
            log.error("Unable to parse ObjectName", e);
        }
    }

    private void loadDriverJARList(RenderRequest renderRequest) {
        // List the available JARs
        List list = new ArrayList();
        ListableRepository[] repos = PortletManager.getListableRepositories(renderRequest);
        for (int i = 0; i < repos.length; i++) {
            ListableRepository repo = repos[i];
            try {
                final URI[] uris = repo.listURIs();
                outer:
                for (int j = 0; j < uris.length; j++) {
                    String test = uris[j].toString();
                    for (int k = 0; k < SKIP_ENTRIES_WITH.length; k++) {
                        String skip = SKIP_ENTRIES_WITH[k];
                        if(test.indexOf(skip) > -1) {
                            continue outer;
                        }
                    }
                    list.add(test);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
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
        while(po.getDeploymentStatus().isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void normalize(RealmData data) {
        List list = new ArrayList();
        if(data.modules == null) {
            LoginModuleDetails module = new LoginModuleDetails();
            module.setClassName(getSelectedModule(data).getClassName());
            module.setControlFlag("REQUIRED");
            module.setLoginDomainName(data.getName());
            module.setServerSide(data.getRealmType().indexOf("erberos") < 0);
            Properties props = module.getOptions();
            for (Iterator it = data.getOptions().entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                props.setProperty((String)entry.getKey(), (String) entry.getValue());
            }
            list.add(module);
            if(data.isStorePassword()) {
                module = new LoginModuleDetails();
                module.setClassName(GeronimoPasswordCredentialLoginModule.class.getName());
                module.setControlFlag("OPTIONAL");
                module.setLoginDomainName(data.getName()+"-Password");
                module.setServerSide(true);
                list.add(module);
            }
            if(data.getAuditPath() != null) {
                module = new LoginModuleDetails();
                module.setClassName(FileAuditLoginModule.class.getName());
                module.setControlFlag("OPTIONAL");
                module.setLoginDomainName(data.getName()+"-Audit");
                module.setServerSide(true);
                props = module.getOptions();
                props.setProperty("file", data.getAuditPath());
                list.add(module);
            }
            if(data.isLockoutEnabled()) {
                module = new LoginModuleDetails();
                module.setClassName(RepeatedFailureLockoutLoginModule.class.getName());
                module.setControlFlag("REQUISITE");
                module.setLoginDomainName(data.getName()+"-Lockout");
                module.setServerSide(true);
                props = module.getOptions();
                props.setProperty("failureCount", data.getLockoutCount());
                props.setProperty("failurePeriodSecs", data.getLockoutWindow());
                props.setProperty("lockoutDurationSecs", data.getLockoutDuration());
                list.add(module);
            }
        } else {
            list.addAll(Arrays.asList(data.modules));
        }
        if(data.getObjectName() == null) {
            for(int i=list.size(); i<5; i++) {
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
        private String objectName; // used when editing existing realms
        private LoginModuleDetails[] modules;

        public void load(PortletRequest request) {
            name = request.getParameter("name");
            if(name != null && name.equals("")) name = null;
            realmType = request.getParameter("realmType");
            if(realmType != null && realmType.equals("")) realmType = null;
            jar = request.getParameter("jar");
            if(jar != null && jar.equals("")) jar = null;
            auditPath = request.getParameter("auditPath");
            if(auditPath != null && auditPath.equals("")) auditPath = null;
            lockoutCount = request.getParameter("lockoutCount");
            if(lockoutCount != null && lockoutCount.equals("")) lockoutCount = null;
            lockoutWindow = request.getParameter("lockoutWindow");
            if(lockoutWindow != null && lockoutWindow.equals("")) lockoutWindow = null;
            lockoutDuration = request.getParameter("lockoutDuration");
            if(lockoutDuration != null && lockoutDuration.equals("")) lockoutDuration = null;
            objectName = request.getParameter("objectName");
            if(objectName != null && objectName.equals("")) objectName = null;
            String test = request.getParameter("storePassword");
            storePassword = test != null && !test.equals("") && !test.equals("false");
            Map map = request.getParameterMap();
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                if(key.startsWith("option-")) {
                    if(key.equals("option-databasePoolObjectName")) { // special handling for a data source, where there's one select corresponding to two properties
                        String nameString = request.getParameter(key);
                        if(nameString != null && !nameString.equals("")) {
                            try {
                                ObjectName on = ObjectName.getInstance(nameString);
                                options.put("dataSourceName", on.getKeyProperty(NameFactory.J2EE_NAME));
                                options.put("dataSourceApplication", on.getKeyProperty(NameFactory.J2EE_APPLICATION));
                            } catch (MalformedObjectNameException e) {
                                log.error("Unable to parse ObjectName", e);
                            }
                        }
                    } else {
                        final String optionName = key.substring(7);
                        final String value = request.getParameter(key);
                        if(value != null && !value.equals("")) {
                            options.put(optionName, value);
                        }
                    }
                }
            }
            int count = 0;
            List list = new ArrayList();
            while(true) {
                int index = count;
                ++count;
                String name = request.getParameter("module-domain-"+index);
                if(name == null || name.equals("")) break;
                LoginModuleDetails details = new LoginModuleDetails();
                details.setLoginDomainName(name);
                String cls = request.getParameter("module-class-"+index);
                if(cls == null || cls.equals("")) continue;
                details.setClassName(cls);
                String flag = request.getParameter("module-control-"+index);
                if(flag == null || flag.equals("")) continue;
                details.setControlFlag(flag);
                String server = request.getParameter("module-server-"+index);
                if(server == null || server.equals("")) continue;
                details.setServerSide(new Boolean(server).booleanValue());
                String options = request.getParameter("module-options-"+index);
                if(options != null && !options.equals("")) {
                    BufferedReader in = new BufferedReader(new StringReader(options));
                    String line;
                    try {
                        while((line = in.readLine()) != null) {
                            if(line.startsWith("#") || line.equals("")) {
                                continue;
                            }
                            int pos = line.indexOf('=');
                            if(pos > -1) {
                                details.getOptions().setProperty(line.substring(0, pos), line.substring(pos+1));
                            }
                        }
                    } catch (IOException e) {
                        log.error("Unable to read properties '"+options+"'", e);
                    }
                }
                list.add(details);
            }
            if(list.size() > 0) {
                modules = (LoginModuleDetails[]) list.toArray(new LoginModuleDetails[list.size()]);
            }
        }

        public void reorderOptions(MasterLoginModuleInfo.OptionInfo[] info) {
            if(info == null || info.length == 0) {
                return; // Probably SQL or something that handles this manually
            }
            Map map = new LinkedHashMap();
            for(int i=0; i<info.length;i++) {
                if(options.containsKey(info[i].getName())) {
                    map.put(info[i].getName(), options.get(info[i].getName()));
                }
            }
            options = map;
        }

        public void store(ActionResponse response) {
            if(name != null) response.setRenderParameter("name", name);
            if(realmType != null) response.setRenderParameter("realmType", realmType);
            if(jar != null) response.setRenderParameter("jar", jar);
            if(auditPath != null) response.setRenderParameter("auditPath", auditPath);
            if(lockoutCount != null) response.setRenderParameter("lockoutCount", lockoutCount);
            if(lockoutWindow != null) response.setRenderParameter("lockoutWindow", lockoutWindow);
            if(lockoutDuration != null) response.setRenderParameter("lockoutDuration", lockoutDuration);
            if(objectName != null) response.setRenderParameter("objectName", objectName);
            if(storePassword) response.setRenderParameter("storePassword", "true");
            for (Iterator it = options.keySet().iterator(); it.hasNext();) {
                String name = (String) it.next();
                String value = (String) options.get(name);
                if(value != null) {
                    response.setRenderParameter("option-"+name, value);
                }
            }
            if(modules != null) {
                for (int i = 0; i < modules.length; i++) {
                    LoginModuleDetails module = modules[i];
                    if(module.getLoginDomainName() != null) response.setRenderParameter("module-domain-"+i, module.getLoginDomainName());
                    if(module.getClassName() != null) response.setRenderParameter("module-class-"+i, module.getClassName());
                    if(module.getControlFlag() != null) response.setRenderParameter("module-control-"+i, module.getControlFlag());
                    response.setRenderParameter("module-server-"+i, module.isServerSide() ? "true" : "false");
                    if(module.getOptions().size() > 0) response.setRenderParameter("module-options-"+i, module.getOptionString());
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

        public String getObjectName() {
            return objectName;
        }

        public boolean isTestable() {
            return getSelectedModule(this).isTestable();
        }

        public LoginModuleDetails[] getModules() {
            return modules;
        }
    }

    public static class LoginModuleDetails implements Serializable {
        private String loginDomainName;
        private String className;
        private String controlFlag;
        private boolean serverSide = true;
        private Properties options = new Properties();

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

        public String getControlFlag() {
            return controlFlag;
        }

        public void setControlFlag(String controlFlag) {
            this.controlFlag = controlFlag;
        }

        public boolean isServerSide() {
            return serverSide;
        }

        public void setServerSide(boolean serverSide) {
            this.serverSide = serverSide;
        }

        public Properties getOptions() {
            return options;
        }

        public void setOptions(Properties options) {
            this.options = options;
        }

        public String getOptionString() {
            StringBuffer buf = new StringBuffer();
            for (Iterator it = options.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                buf.append(key).append("=").append(options.getProperty(key)).append("\n");
            }
            return buf.toString();
        }
    }

    public static class ExistingRealm implements Serializable {
        private final String name;
        private final String objectName;
        private final String parentName;
        private final int state;

        public ExistingRealm(String name, ObjectName objectName, int state) {
            this.name = name;
            this.objectName = objectName.getCanonicalName();
            String parent = objectName.getKeyProperty(NameFactory.J2EE_APPLICATION);
            if(parent != null && parent.equals("null")) {
                parent = null;
            }
            parentName = parent;
            this.state = state;

        }

        public String getName() {
            return name;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getParentName() {
            return parentName;
        }

        public int getState() {
            return state;
        }

        public String getStateName() {
            return State.toString(state);
        }
    }

    public static class DatabasePool implements Serializable, Comparable {
        private final String name;
        private final String displayName;
        private final String applicationName;
        private final String objectName;

        public DatabasePool(String name, String displayName, String applicationName, String objectName) {
            this.name = name;
            this.displayName = displayName;
            this.applicationName = applicationName;
            this.objectName = objectName;
        }

        public String getName() {
            return name;
        }

        public String getApplicationName() {
            return applicationName;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int compareTo(Object o) {
            final DatabasePool pool = (DatabasePool)o;
            int names = name.compareTo(pool.name);
            if(applicationName == null) {
                if(pool.applicationName == null) {
                    return names;
                } else {
                    return -1;
                }
            } else {
                if(pool.applicationName == null) {
                    return 1;
                } else {
                    int test = applicationName.compareTo(pool.applicationName);
                    if(test != 0) {
                        return test;
                    } else {
                        return names;
                    }
                }
            }
        }
    }
}