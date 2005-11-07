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
package org.apache.geronimo.console.databasemanager.wizard;

import java.io.IOException;
import java.io.Serializable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.portlet.PortletRequest;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.enterprise.deploy.spi.exceptions.InvalidModuleException;
import javax.enterprise.deploy.model.exceptions.DDBeanCreateException;
import javax.enterprise.deploy.model.DDBeanRoot;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.deployment.tools.loader.ConnectorDeployable;
import org.apache.geronimo.connector.deployment.jsr88.Connector15DCBRoot;
import org.apache.geronimo.connector.deployment.jsr88.ConnectorDCB;
import org.apache.geronimo.connector.deployment.jsr88.Dependency;
import org.apache.geronimo.connector.deployment.jsr88.ResourceAdapter;
import org.apache.geronimo.connector.deployment.jsr88.ConnectionDefinition;
import org.apache.geronimo.connector.deployment.jsr88.ConnectionDefinitionInstance;
import org.apache.geronimo.connector.deployment.jsr88.ConfigPropertySetting;
import org.apache.geronimo.connector.deployment.jsr88.ConnectionManager;
import org.apache.geronimo.connector.deployment.jsr88.SinglePool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class DatabasePoolPortlet extends BasePortlet {
    /**
     * todo: EVIL!!!  Should be replaced with something, somehow!
     */
    private final static String TRANQL_RAR_NAME = "tranql/rars/tranql-connector-1.0.rar";

    private final static Log log = LogFactory.getLog(DatabasePoolPortlet.class);
    private static final String LIST_VIEW            = "/WEB-INF/view/dbwizard/list.jsp";
    private static final String EDIT_VIEW            = "/WEB-INF/view/dbwizard/edit.jsp";
    private static final String SELECT_RDBMS_VIEW    = "/WEB-INF/view/dbwizard/selectDatabase.jsp";
    private static final String BASIC_PARAMS_VIEW    = "/WEB-INF/view/dbwizard/basicParams.jsp";
    private static final String CONFIRM_URL_VIEW     = "/WEB-INF/view/dbwizard/confirmURL.jsp";
    private static final String TEST_CONNECTION_VIEW = "/WEB-INF/view/dbwizard/testConnection.jsp";
    private static final String LIST_MODE            = "list";
    private static final String EDIT_MODE            = "edit";
    private static final String SELECT_RDBMS_MODE    = "rdbms";
    private static final String BASIC_PARAMS_MODE    = "params";
    private static final String CONFIRM_URL_MODE     = "url";
    private static final String TEST_CONNECTION_MODE = "test";
    private static final String SAVE_MODE            = "save";
    private static final String MODE_KEY = "mode";

    private PortletRequestDispatcher listView;
    private PortletRequestDispatcher editView;
    private PortletRequestDispatcher selectRDBMSView;
    private PortletRequestDispatcher basicParamsView;
    private PortletRequestDispatcher confirmURLView;
    private PortletRequestDispatcher testConnectionView;

    public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
        listView = portletConfig.getPortletContext().getRequestDispatcher(LIST_VIEW);
        editView = portletConfig.getPortletContext().getRequestDispatcher(EDIT_VIEW);
        selectRDBMSView = portletConfig.getPortletContext().getRequestDispatcher(SELECT_RDBMS_VIEW);
        basicParamsView = portletConfig.getPortletContext().getRequestDispatcher(BASIC_PARAMS_VIEW);
        confirmURLView = portletConfig.getPortletContext().getRequestDispatcher(CONFIRM_URL_VIEW);
        testConnectionView = portletConfig.getPortletContext().getRequestDispatcher(TEST_CONNECTION_VIEW);
    }

    public void destroy() {
        listView = null;
        editView = null;
        selectRDBMSView = null;
        basicParamsView = null;
        confirmURLView = null;
        testConnectionView = null;
        super.destroy();
    }

    public void processAction(ActionRequest actionRequest,
            ActionResponse actionResponse) throws PortletException, IOException {
        String mode = actionRequest.getParameter(MODE_KEY);
        PoolData data = new PoolData();
        data.load(actionRequest);
        if(mode.equals("process-"+SELECT_RDBMS_MODE)) {
            if(data.getDbtype().equals("Other")) {
                actionResponse.setRenderParameter(MODE_KEY, EDIT_MODE);
            } else {
                DatabaseInfo info = null;
                info = getDatabaseInfo(data);
                if(info != null) {
                    data.driverClass = info.getDriverClass();
                    data.urlPrototype = info.getUrl();
                }
                actionResponse.setRenderParameter(MODE_KEY, BASIC_PARAMS_MODE);
            }
        } else if(mode.equals("process-"+BASIC_PARAMS_MODE)) {
            DatabaseInfo info = null;
            info = getDatabaseInfo(data);
            if(info != null) {
                data.url = populateURL(info.getUrl(), info.getUrlParameters(), data.getProperties());
            }
            if(attemptDriverLoad(actionRequest, data) != null) {
                actionResponse.setRenderParameter(MODE_KEY, CONFIRM_URL_MODE);
            } else {
                actionResponse.setRenderParameter("driverError", "Unable to load driver "+data.driverClass);
                actionResponse.setRenderParameter(MODE_KEY, BASIC_PARAMS_MODE);
            }
        } else if(mode.equals("process-"+CONFIRM_URL_MODE)) {
            String test = actionRequest.getParameter("test");
            if(test == null || test.equals("true")) {
                String result = null;
                String stack = null;
                try {
                    result = attemptConnect(actionRequest, data);
                } catch (Exception e) {
                    StringWriter writer = new StringWriter();
                    PrintWriter temp = new PrintWriter(writer);
                    e.printStackTrace(temp);
                    temp.flush();
                    temp.close();
                    stack = writer.getBuffer().toString();
                }
                if(result != null) actionResponse.setRenderParameter("connectResult", result);
                actionRequest.getPortletSession(true).setAttribute("connectError", stack);
                actionResponse.setRenderParameter(MODE_KEY, TEST_CONNECTION_MODE);
            } else {
                save(actionRequest, data);
            }
        } else if(mode.equals(SAVE_MODE)) {
            save(actionRequest, data);
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
            PoolData data = new PoolData();
            data.load(renderRequest);
            renderRequest.setAttribute("pool", data);
            if(mode == null || mode.equals("")) {
                mode = LIST_MODE;
            }
            if(mode.equals(LIST_MODE)) {
                renderList(renderRequest, renderResponse);
            } else if(mode.equals(EDIT_MODE)) {
                renderEdit(renderRequest, renderResponse);
            } else if(mode.equals(SELECT_RDBMS_MODE)) {
                renderSelectRDBMS(renderRequest, renderResponse);
            } else if(mode.equals(BASIC_PARAMS_MODE)) {
                renderBasicParams(renderRequest, renderResponse, data);
            } else if(mode.equals(CONFIRM_URL_MODE)) {
                renderConfirmURL(renderRequest, renderResponse);
            } else if(mode.equals(TEST_CONNECTION_MODE)) {
                renderTestConnection(renderRequest, renderResponse);
            }
        } catch (Throwable e) {
            log.error("Unable to render portlet", e);
        }
    }

    private void renderList(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        JCAManagedConnectionFactory[] databases = PortletManager.getOutboundFactories(renderRequest, "javax.sql.DataSource");
        ConnectionPool[] pools = new ConnectionPool[databases.length];
        for (int i = 0; i < databases.length; i++) {
            JCAManagedConnectionFactory db = databases[i];
            try {
                ObjectName name = ObjectName.getInstance(db.getObjectName());
                pools[i] = new ConnectionPool(name.getCanonicalName(), name.getKeyProperty(NameFactory.J2EE_NAME));
            } catch (MalformedObjectNameException e) {
                e.printStackTrace();
            }
        }
        renderRequest.setAttribute("pools", pools);
        listView.include(renderRequest, renderResponse);
    }

    private void renderEdit(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        editView.include(renderRequest, renderResponse);
    }

    private void renderSelectRDBMS(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        renderRequest.setAttribute("databases", DatabaseInfo.ALL_DATABASES);
        selectRDBMSView.include(renderRequest, renderResponse);
    }

    private void renderBasicParams(RenderRequest renderRequest, RenderResponse renderResponse, PoolData data) throws IOException, PortletException {
        // List the available JARs
        List list = new ArrayList();
        ListableRepository[] repos = PortletManager.getListableRepositories(renderRequest);
        for (int i = 0; i < repos.length; i++) {
            ListableRepository repo = repos[i];
            try {
                final URI[] uris = repo.listURIs();
                final String[] strings = new String[uris.length];
                for (int j = 0; j < strings.length; j++) {
                    strings[j] = uris[j].toString();
                }
                list.addAll(Arrays.asList(strings));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        Collections.sort(list);
        renderRequest.setAttribute("jars", list);
        // Make sure all properties available for the DB are listed
        DatabaseInfo info = getDatabaseInfo(data);
        if(info != null) {
            String[] params = info.getUrlParameters();
            for (int i = 0; i < params.length; i++) {
                String param = params[i];
                final String key = "property-"+param;
                if(!data.getProperties().containsKey(key)) {
                    data.getProperties().put(key, param.equals("port") && info.getDefaultPort() > 0 ? new Integer(info.getDefaultPort()) : null);
                }
            }
        }
        // Pass on errors
        renderRequest.setAttribute("driverError", renderRequest.getParameter("driverError"));

        basicParamsView.include(renderRequest, renderResponse);
    }

    private void renderConfirmURL(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        confirmURLView.include(renderRequest, renderResponse);
    }

    private void renderTestConnection(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        // Pass on results
        renderRequest.setAttribute("connectResult", renderRequest.getParameter("connectResult"));
        renderRequest.setAttribute("connectError", renderRequest.getPortletSession().getAttribute("connectError"));
        testConnectionView.include(renderRequest, renderResponse);
    }

    private static String attemptConnect(PortletRequest request, PoolData data) throws SQLException, IllegalAccessException, InstantiationException {
        Class driverClass = attemptDriverLoad(request, data);
        Driver driver = (Driver) driverClass.newInstance();
        if(driver.acceptsURL(data.url)) {
            Properties props = new Properties();
            props.put("user", data.user);
            props.put("password", data.password);
            Connection con = null;
            try {
                con = driver.connect(data.url, props);
                final DatabaseMetaData metaData = con.getMetaData();
                return metaData.getDatabaseProductName()+" "+metaData.getDatabaseProductVersion();
            } finally {
                if(con != null) try{con.close();}catch(SQLException e) {}
            }
        } else throw new SQLException("Driver "+data.getDriverClass()+" does not accept URL "+data.url);
    }

    private static void save(PortletRequest request, PoolData data) {
        URL url = getTranQLRAR(request);
        DeploymentManager mgr = PortletManager.getDeploymentManager(request);
        try {
            ConnectorDeployable deployable = new ConnectorDeployable(url);
            DeploymentConfiguration config = mgr.createConfiguration(deployable);
            final DDBeanRoot ddBeanRoot = deployable.getDDBeanRoot();
            Connector15DCBRoot root = (Connector15DCBRoot) config.getDConfigBeanRoot(ddBeanRoot);
            ConnectorDCB connector = (ConnectorDCB) root.getDConfigBean(ddBeanRoot.getChildBean(root.getXpaths()[0])[0]);
            connector.setConfigID("DatabasePool"+data.getName());
            connector.setParentID("org/apache/geronimo/Server");
            if(data.jar1 != null && !data.jar1.equals("")) {
                Dependency dep = new Dependency();
                connector.setDependency(new Dependency[]{dep});
                dep.setURI(data.jar1);
            }
            if(data.jar2 != null && !data.jar2.equals("")) {
                Dependency dep = new Dependency();
                Dependency[] old = connector.getDependency();
                Dependency[] longer = new Dependency[old.length+1];
                System.arraycopy(old, 0, longer, 0, old.length);
                longer[old.length] = dep;
                connector.setDependency(longer);
                dep.setURI(data.jar2);
            }
            if(data.jar3 != null && !data.jar3.equals("")) {
                Dependency dep = new Dependency();
                Dependency[] old = connector.getDependency();
                Dependency[] longer = new Dependency[old.length+1];
                System.arraycopy(old, 0, longer, 0, old.length);
                longer[old.length] = dep;
                connector.setDependency(longer);
                dep.setURI(data.jar3);
            }
            ResourceAdapter adapter = connector.getResourceAdapter()[0];
            ConnectionDefinition definition = new ConnectionDefinition();
            adapter.setConnectionDefinition(new ConnectionDefinition[]{definition});
            definition.setConnectionFactoryInterface("javax.sql.DataSource");
            ConnectionDefinitionInstance instance = new ConnectionDefinitionInstance();
            definition.setConnectionInstance(new ConnectionDefinitionInstance[]{instance});
            instance.setName(data.getName());
            ConfigPropertySetting[] settings = instance.getConfigPropertySetting();
            for (int i = 0; i < settings.length; i++) {
                ConfigPropertySetting setting = settings[i];
                if(setting.getName().equals("UserName")) {
                    setting.setValue(data.user);
                } else if(setting.getName().equals("Password")) {
                    setting.setValue(data.password);
                } else if(setting.getName().equals("ConnectionURL")) {
                    setting.setValue(data.url);
                } else if(setting.getName().equals("Driver")) {
                    setting.setValue(data.driverClass);
                }
            }
            ConnectionManager manager = instance.getConnectionManager();
            manager.setTransactionLocal(true);
            SinglePool pool = new SinglePool();
            manager.setPoolSingle(pool);
            pool.setMatchOne(true);
            if(data.minSize != null && !data.minSize.equals("")) {
                pool.setMinSize(new Integer(data.minSize));
            }
            if(data.maxSize != null && !data.maxSize.equals("")) {
                pool.setMaxSize(new Integer(data.maxSize));
            }
            if(data.blockingTimeout != null && !data.blockingTimeout.equals("")) {
                pool.setBlockingTimeoutMillis(new Integer(data.blockingTimeout));
            }
            if(data.idleTimeout != null && !data.idleTimeout.equals("")) {
                pool.setIdleTimeoutMinutes(new Integer(data.idleTimeout));
            }
            File tempFile = File.createTempFile("console-deployment",".xml");
            tempFile.deleteOnExit();
            log.debug("Writing database pool deployment plan to "+tempFile.getAbsolutePath());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
            config.save(out);
            out.flush();
            out.close();
            Target[] targets = mgr.getTargets();
            ProgressObject po = mgr.distribute(targets, new File(url.getPath()), tempFile);
            waitForProgress(po);
            if(po.getDeploymentStatus().isCompleted()) {
                TargetModuleID[] ids = po.getResultTargetModuleIDs();
                po = mgr.start(ids);
                waitForProgress(po);
                if(po.getDeploymentStatus().isCompleted()) {
                    System.out.println("Deployment completed successfully!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(mgr != null) mgr.release();
        }
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

    /**
     * todo: This is not ideal...  Should look up the RAR name somehow...
     * Maybe eventually should not assume the RAR is on the local filesystem...
     */
    private static URL getTranQLRAR(PortletRequest request) {
        try {
            URI uri = new URI(TRANQL_RAR_NAME);
            Repository[] repos = PortletManager.getRepositories(request);
            for (int i = 0; i < repos.length; i++) {
                Repository repo = repos[i];
                URL url = repo.getURL(uri);
                if(url != null && url.getProtocol().equals("file")) {
                    File file = new File(url.getPath());
                    if(file.exists() && file.canRead() && !file.isDirectory()) {
                        return url;
                    }
                }
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * WARNING: This method relies on having access to the same repository
     * URLs as the server uses.
     */
    private static Class attemptDriverLoad(PortletRequest request, PoolData data) {
        List list = new ArrayList();
        try {
            URI one = new URI(data.getJar1());
            URI two = new URI(data.getJar2());
            URI three = new URI(data.getJar3());

            ListableRepository[] repos = PortletManager.getListableRepositories(request);
            for (int i = 0; i < repos.length; i++) {
                ListableRepository repo = repos[i];
                if(one != null) {
                    URL url = repo.getURL(one);
                    if(url != null) {
                        list.add(url);
                        one = null;
                    }
                }
                if(two != null) {
                    URL url = repo.getURL(two);
                    if(url != null) {
                        list.add(url);
                        two = null;
                    }
                }
                if(three != null) {
                    URL url = repo.getURL(three);
                    if(url != null) {
                        list.add(url);
                        three = null;
                    }
                }
            }
            URLClassLoader loader = new URLClassLoader((URL[]) list.toArray(new URL[list.size()]), DatabasePoolPortlet.class.getClassLoader());
            try {
                return loader.loadClass(data.driverClass);
            } catch (ClassNotFoundException e) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String populateURL(String url, String[] keys, Map properties) {
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            String value = (String) properties.get("property-"+key);
            if(value == null) {
                int begin = url.indexOf("<"+key+">");
                int end = begin + key.length() + 2;
                for(int j=begin-1; j>=0; j--) {
                    char c = url.charAt(j);
                    if(c == ';' || c == ':') {
                        begin = j;
                        break;
                    } else if(c == '/') {
                        break;
                    }
                }
                url = url.substring(0, begin)+url.substring(end);
            } else {
                url = url.replaceAll("<"+key+">", value);
            }
        }
        return url;
    }

    private static DatabaseInfo getDatabaseInfo(PoolData data) {
        DatabaseInfo info = null;
        for (int i = 0; i < DatabaseInfo.ALL_DATABASES.length; i++) {
            DatabaseInfo next = DatabaseInfo.ALL_DATABASES[i];
            if(next.getName().equals(data.getDbtype())) {
                info = next;
                break;
            }
        }
        return info;
    }

    public static class PoolData implements Serializable {
        private String name;
        private String dbtype;
        private String user;
        private String password;
        private Map properties = new HashMap();
        private String driverClass;
        private String url;
        private String urlPrototype;
        private String jar1;
        private String jar2;
        private String jar3;
        private String minSize;
        private String maxSize;
        private String blockingTimeout;
        private String idleTimeout;

        public void load(PortletRequest request) {
            name = request.getParameter("name");
            driverClass = request.getParameter("driverClass");
            dbtype = request.getParameter("dbtype");
            user = request.getParameter("user");
            password = request.getParameter("password");
            url = request.getParameter("url");
            urlPrototype = request.getParameter("urlPrototype");
            jar1 = request.getParameter("jar1");
            jar2 = request.getParameter("jar2");
            jar3 = request.getParameter("jar3");
            minSize = request.getParameter("minSize");
            maxSize = request.getParameter("maxSize");
            blockingTimeout = request.getParameter("blockingTimeout");
            idleTimeout = request.getParameter("idleTimeout");
            Map map = request.getParameterMap();
            for (Iterator it = map.keySet().iterator(); it.hasNext();) {
                String key = (String) it.next();
                if(key.startsWith("property-")) {
                    properties.put(key, request.getParameter(key));
                }
            }
        }

        public void store(ActionResponse response) {
            if(name != null) response.setRenderParameter("name", name);
            if(dbtype != null) response.setRenderParameter("dbtype", dbtype);
            if(driverClass != null) response.setRenderParameter("driverClass", driverClass);
            if(user != null) response.setRenderParameter("user", user);
            if(password != null) response.setRenderParameter("password", password);
            if(url != null) response.setRenderParameter("urlPrototype", urlPrototype);
            if(urlPrototype != null) response.setRenderParameter("url", url);
            if(jar1 != null) response.setRenderParameter("jar1", jar1);
            if(jar2 != null) response.setRenderParameter("jar2", jar2);
            if(jar3 != null) response.setRenderParameter("jar3", jar3);
            if(minSize != null) response.setRenderParameter("minSize", minSize);
            if(maxSize != null) response.setRenderParameter("maxSize", maxSize);
            if(blockingTimeout != null) response.setRenderParameter("blockingTimeout", blockingTimeout);
            if(idleTimeout != null) response.setRenderParameter("idleTimeout", idleTimeout);
            for (Iterator it = properties.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                if(entry.getValue() != null) {
                    response.setRenderParameter((String)entry.getKey(), (String)entry.getValue());
                }
            }
        }

        public String getName() {
            return name;
        }

        public String getDbtype() {
            return dbtype;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public Map getProperties() {
            return properties;
        }

        public String getUrl() {
            return url;
        }

        public String getJar1() {
            return jar1;
        }

        public String getJar2() {
            return jar2;
        }

        public String getJar3() {
            return jar3;
        }

        public String getMinSize() {
            return minSize;
        }

        public String getMaxSize() {
            return maxSize;
        }

        public String getBlockingTimeout() {
            return blockingTimeout;
        }

        public String getIdleTimeout() {
            return idleTimeout;
        }

        public String getDriverClass() {
            return driverClass;
        }

        public String getUrlPrototype() {
            return urlPrototype;
        }

        public String toString() {
            String temp= "NAME="+name+"\n"+
                    "DB="+dbtype+"\n"+
                    "CLASS="+driverClass+"\n"+
                    "FORMAT="+urlPrototype+"\n"+
                    "URL="+url+"\n"+
                    "USER="+user+"\n"+
                    "PASS="+password+"\n"+
                    "JAR1="+jar1+"\n"+
                    "JAR2="+jar2+"\n"+
                    "JAR3="+jar3+"\n"+
                    "MIN="+minSize+"\n"+
                    "MAX="+maxSize+"\n"+
                    "BLOCK="+blockingTimeout+"\n"+
                    "IDLE="+idleTimeout;
            for (Iterator it = properties.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                temp+="\n"+entry.getKey()+"="+entry.getValue();
            }
            return temp;
        }
    }

    public static class ConnectionPool implements Serializable {
        private String objectName;
        private String name;

        public ConnectionPool(String objectName, String name) {
            this.objectName = objectName;
            this.name = name;
        }

        public String getObjectName() {
            return objectName;
        }

        public String getName() {
            return name;
        }
    }
}
