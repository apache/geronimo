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
package org.apache.geronimo.console.jmsmanager.wizard;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.status.ProgressObject;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.geronimo.connector.deployment.jsr88.AdminObjectDCB;
import org.apache.geronimo.connector.deployment.jsr88.AdminObjectInstance;
import org.apache.geronimo.connector.deployment.jsr88.ConnectionDefinition;
import org.apache.geronimo.connector.deployment.jsr88.ConnectionDefinitionInstance;
import org.apache.geronimo.connector.deployment.jsr88.Connector15DCBRoot;
import org.apache.geronimo.connector.deployment.jsr88.ConnectorDCB;
import org.apache.geronimo.connector.deployment.jsr88.ResourceAdapter;
import org.apache.geronimo.connector.deployment.jsr88.ResourceAdapterInstance;
import org.apache.geronimo.connector.deployment.jsr88.SinglePool;
import org.apache.geronimo.console.BasePortlet;
import org.apache.geronimo.console.MultiPageAbstractHandler;
import org.apache.geronimo.console.MultiPageModel;
import org.apache.geronimo.console.jmsmanager.ManagementHelper;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.deployment.service.jsr88.EnvironmentData;
import org.apache.geronimo.deployment.tools.loader.ConnectorDeployable;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.naming.deployment.jsr88.GBeanLocator;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for portlet helpers
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractHandler extends MultiPageAbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractHandler.class);
    // ********** This part specific to JMS portlets **********

    protected final static String LIST_MODE="list";
    protected final static String SELECT_PROVIDER_MODE="provider";
    protected final static String CONFIGURE_RA_MODE="ra";
    protected final static String ADD_FACTORY_MODE="factory";
    protected final static String SELECT_FACTORY_TYPE_MODE="factoryType";
    protected final static String ADD_DESTINATION_MODE="destination";
    protected final static String SELECT_DESTINATION_TYPE_MODE="destinationType";
    protected final static String REVIEW_MODE="review";
    protected final static String SHOW_PLAN_MODE="plan";
    protected final static String DEPLOY_MODE="deploy";

    protected final static String VIEW_MESSAGES="viewMessages";
    protected final static String MESSAGE_DETAILS="messageDetails";
    protected final static String SEND_MESSAGE="sendmessage";
    protected final static String ADMIN_OBJ_NAME = "adminObjName";
    protected final static String ADMIN_OBJ_TYPE = "adminObjType";
    protected final static String SUBMIT = "submit";
    protected final static String CORRELATION_ID = "correlationId";
    protected final static String PURGE = "purge";
    protected final static String IS_PERSISTENT = "isPersistent";
    protected final static String PRIORITY = "priority";
    protected final static String JMS_TYPE = "jmsType";
    protected final static String MESSAGE = "message";
    protected final static String PHYSICAL_NAME = "physicalName";
    protected final static String RA_ADAPTER_OBJ_NAME = "adapterObjectName";
    protected final static String MESSAGES = "messages";
    protected final static String MESSAGE_ID = "messageId";
    protected final static String MESSAGE_TXT="messageTxt";
    protected final static String BROKER_NAME = "brokerName";
    protected final static String RESOURCE_ADAPTER_MODULE_NAME = "resourceAdapterModuleName";

    protected final static String PROVIDER_PARAMETER="provider";
    protected final static String RAR_FILE_PARAMETER="rar";
    protected final static String DEPENDENCY_PARAMETER="dependency";
    protected final static String INSTANCE_NAME_PARAMETER="instanceName";
    protected final static String NAME_PARAMETER="name";
    protected final static String CURRENT_FACTORY_PARAMETER="currentFactoryID";
    protected final static String CURRENT_DEST_PARAMETER="currentDestinationID";
    protected final static String FACTORY_TYPE_PARAMETER="factoryType";
    protected final static String DEST_TYPE_PARAMETER="destinationType";
    protected final static String TRANSACTION_PARAMETER="transaction";
    protected final static String XA_TRANSACTION_PARAMETER="xaTransaction";
    protected final static String XA_THREAD_PARAMETER="xaThread";
    protected final static String MIN_SIZE_PARAMETER="poolMinSize";
    protected final static String MAX_SIZE_PARAMETER="poolMaxSize";
    protected final static String IDLE_TIME_PARAMETER="poolIdleTimeout";
    protected final static String BLOCK_TIME_PARAMETER="poolBlockingTimeout";

    public AbstractHandler(String mode, String viewName) {
        super(mode, viewName);
    }
    
    public AbstractHandler(String mode, String viewName, BasePortlet portlet) {
        super(mode, viewName, portlet);
    }

    public static class JMSResourceData implements MultiPageModel {
        private String rarURI;
        private String dependency;
        private String instanceName;
        private Properties instanceProps = new Properties();
        private String workManager;
        private int currentFactory = -1;
        private int currentDestination = -1;
        private int factoryType = -1;
        private int destinationType = -1;
        private List connectionFactories = new ArrayList();
        private List adminObjects = new ArrayList();
        // Used for editing an existing resource
        private String objectName;

        public JMSResourceData(PortletRequest request) {
            Map map = request.getParameterMap();
            rarURI = request.getParameter(RAR_FILE_PARAMETER);
            dependency = request.getParameter(DEPENDENCY_PARAMETER);
            instanceName = request.getParameter(INSTANCE_NAME_PARAMETER);
            factoryType = isEmpty(request.getParameter(FACTORY_TYPE_PARAMETER)) ? -1 : Integer.parseInt(request.getParameter(FACTORY_TYPE_PARAMETER));
            currentFactory = isEmpty(request.getParameter(CURRENT_FACTORY_PARAMETER)) ? -1 : Integer.parseInt(request.getParameter(CURRENT_FACTORY_PARAMETER));
            destinationType = isEmpty(request.getParameter(DEST_TYPE_PARAMETER)) ? -1 : Integer.parseInt(request.getParameter(DEST_TYPE_PARAMETER));
            currentDestination = isEmpty(request.getParameter(CURRENT_DEST_PARAMETER)) ? -1 : Integer.parseInt(request.getParameter(CURRENT_DEST_PARAMETER));
            for(int i=0; i<20; i++) {
                String key = "instance-config-" + i;
                if(map.containsKey(key)) {
                    instanceProps.setProperty(key, request.getParameter(key));
                }
            }
            workManager = "DefaultWorkManager"; //todo
            int index = 0;
            while(true) {
                String key = "factory."+(index++)+".";
                if(!map.containsKey(key+FACTORY_TYPE_PARAMETER)) {
                    break;
                }
                JMSConnectionFactoryData data = new JMSConnectionFactoryData();
                data.load(request, key);
                connectionFactories.add(data);
            }
            index = 0;
            while(true) {
                String key = "destination."+(index++)+".";
                if(!map.containsKey(key+DEST_TYPE_PARAMETER)) {
                    break;
                }
                JMSAdminObjectData data = new JMSAdminObjectData();
                data.load(request, key);
                adminObjects.add(data);
            }
            createIfNecessary();
        }

        public void createIfNecessary() {
            while(currentFactory >= connectionFactories.size()) {
                connectionFactories.add(new JMSConnectionFactoryData());
            }
            while(currentDestination >= adminObjects.size()) {
                adminObjects.add(new JMSAdminObjectData());
            }
        }

        public void save(ActionResponse response, PortletSession session) {
            if(!isEmpty(rarURI)) response.setRenderParameter(RAR_FILE_PARAMETER, rarURI);
            if(!isEmpty(dependency)) response.setRenderParameter(DEPENDENCY_PARAMETER, dependency);
            if(!isEmpty(instanceName)) response.setRenderParameter(INSTANCE_NAME_PARAMETER, instanceName);
            for (Iterator it = instanceProps.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                if(!isEmpty((String)entry.getValue())) {
                    response.setRenderParameter((String)entry.getKey(), (String)entry.getValue());
                }
            }
            if(!isEmpty(workManager)) response.setRenderParameter("workManager", workManager);
            response.setRenderParameter(FACTORY_TYPE_PARAMETER, Integer.toString(factoryType));
            response.setRenderParameter(DEST_TYPE_PARAMETER, Integer.toString(destinationType));
            response.setRenderParameter(CURRENT_DEST_PARAMETER, Integer.toString(currentDestination));
            response.setRenderParameter(CURRENT_FACTORY_PARAMETER, Integer.toString(currentFactory));
            for (int i = 0; i < connectionFactories.size(); i++) {
                JMSConnectionFactoryData data = (JMSConnectionFactoryData) connectionFactories.get(i);
                String key = "factory."+i+".";
                data.save(response, key);
            }
            for (int i = 0; i < adminObjects.size(); i++) {
                JMSAdminObjectData data = (JMSAdminObjectData) adminObjects.get(i);
                String key = "destination."+i+".";
                data.save(response, key);
            }
        }

        public int getFactoryType() {
            return factoryType;
        }

        public void setFactoryType(int factoryType) {
            this.factoryType = factoryType;
        }

        public int getDestinationType() {
            return destinationType;
        }

        public void setDestinationType(int destinationType) {
            this.destinationType = destinationType;
        }

        public int getCurrentFactoryID() {
            return currentFactory;
        }

        public void setCurrentFactoryID(int id) {
            currentFactory = id;
        }

        public int getCurrentDestinationID() {
            return currentDestination;
        }

        public void setCurrentDestinationID(int id) {
            currentDestination = id;
        }

        public String getRarURI() {
            return rarURI;
        }

        public void setRarURI(String rarURI) {
            this.rarURI = rarURI;
        }

        public String getDependency() {
            return dependency;
        }

        public void setDependency(String dependency) {
            this.dependency = dependency;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }

        public String getWorkManager() {
            return workManager;
        }

        public void setWorkManager(String workManager) {
            this.workManager = workManager;
        }

        public Properties getInstanceProps() {
            return instanceProps;
        }

        public List getConnectionFactories() {
            return connectionFactories;
        }

        public List getAdminObjects() {
            return adminObjects;
        }

        public JMSConnectionFactoryData getCurrentFactory() {
            return (JMSConnectionFactoryData) connectionFactories.get(currentFactory);
        }

        public JMSAdminObjectData getCurrentDestination() {
            return (JMSAdminObjectData) adminObjects.get(currentDestination);
        }

        public int getConnectionFactoryCount() {
            return connectionFactories.size();
        }

        public int getDestinationCount() {
            return adminObjects.size();
        }
    }

    public static class JMSConnectionFactoryData {
        private int factoryType;
        private String instanceName;
        private String transaction; //none, local, xa
        private boolean xaTransactionCaching;
        private boolean xaThreadCaching;
        private Integer poolMinSize;
        private Integer poolMaxSize;
        private Integer poolBlockingTimeout;
        private Integer poolIdleTimeout;
        private Properties instanceProps = new Properties();

        public void load(PortletRequest request, String prefix) {
            factoryType = isEmpty(request.getParameter(prefix+FACTORY_TYPE_PARAMETER)) ? -1 : Integer.parseInt(request.getParameter(prefix+FACTORY_TYPE_PARAMETER));
            instanceName = request.getParameter(prefix+INSTANCE_NAME_PARAMETER);
            transaction = request.getParameter(prefix+TRANSACTION_PARAMETER);
            xaThreadCaching = !isEmpty(request.getParameter(prefix+XA_THREAD_PARAMETER)) && request.getParameter(prefix+XA_THREAD_PARAMETER).equals("true");
            xaTransactionCaching = isEmpty(request.getParameter(prefix+XA_TRANSACTION_PARAMETER)) || request.getParameter(prefix+XA_TRANSACTION_PARAMETER).equals("true");
            poolMinSize = isEmpty(request.getParameter(prefix+MIN_SIZE_PARAMETER)) ? null : new Integer(request.getParameter(prefix+MIN_SIZE_PARAMETER));
            poolMaxSize = isEmpty(request.getParameter(prefix+MAX_SIZE_PARAMETER)) ? null : new Integer(request.getParameter(prefix+MAX_SIZE_PARAMETER));
            poolIdleTimeout = isEmpty(request.getParameter(prefix+IDLE_TIME_PARAMETER)) ? null : new Integer(request.getParameter(prefix+IDLE_TIME_PARAMETER));
            poolBlockingTimeout = isEmpty(request.getParameter(prefix+BLOCK_TIME_PARAMETER)) ? null : new Integer(request.getParameter(prefix+BLOCK_TIME_PARAMETER));
            Map map = request.getParameterMap();
            for(int i=0; i<20; i++) {
                String key = prefix+"instance-config-" + i;
                if(map.containsKey(key)) {
                    instanceProps.setProperty(key.substring(prefix.length()), request.getParameter(key));
                }
            }
        }

        public void save(ActionResponse response, String prefix) {
            if(factoryType > -1) response.setRenderParameter(prefix+FACTORY_TYPE_PARAMETER, Integer.toString(factoryType));
            if(!isEmpty(instanceName)) response.setRenderParameter(prefix+INSTANCE_NAME_PARAMETER, instanceName);
            if(!isEmpty(transaction)) response.setRenderParameter(prefix+TRANSACTION_PARAMETER, transaction);
            response.setRenderParameter(prefix+XA_THREAD_PARAMETER, Boolean.toString(xaThreadCaching));
            response.setRenderParameter(prefix+XA_TRANSACTION_PARAMETER, Boolean.toString(xaTransactionCaching));
            if(poolMinSize != null) response.setRenderParameter(prefix+MIN_SIZE_PARAMETER, poolMinSize.toString());
            if(poolMaxSize != null) response.setRenderParameter(prefix+MAX_SIZE_PARAMETER, poolMaxSize.toString());
            if(poolBlockingTimeout != null) response.setRenderParameter(prefix+BLOCK_TIME_PARAMETER, poolBlockingTimeout.toString());
            if(poolIdleTimeout != null) response.setRenderParameter(prefix+IDLE_TIME_PARAMETER, poolIdleTimeout.toString());
            for (Iterator it = instanceProps.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                if(!isEmpty((String)entry.getValue())) {
                    response.setRenderParameter(prefix+entry.getKey(), (String)entry.getValue());
                }
            }
        }

        public int getFactoryType() {
            return factoryType;
        }

        public void setFactoryType(int factoryType) {
            this.factoryType = factoryType;
        }

        public String getInstanceName() {
            return instanceName;
        }

        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }

        public String getTransaction() {
            return transaction;
        }

        public void setTransaction(String transaction) {
            this.transaction = transaction;
        }

        public boolean isXaTransactionCaching() {
            return xaTransactionCaching;
        }

        public void setXaTransactionCaching(boolean xaTransactionCaching) {
            this.xaTransactionCaching = xaTransactionCaching;
        }

        public boolean isXaThreadCaching() {
            return xaThreadCaching;
        }

        public void setXaThreadCaching(boolean xaThreadCaching) {
            this.xaThreadCaching = xaThreadCaching;
        }

        public Integer getPoolMinSize() {
            return poolMinSize;
        }

        public void setPoolMinSize(Integer poolMinSize) {
            this.poolMinSize = poolMinSize;
        }

        public Integer getPoolMaxSize() {
            return poolMaxSize;
        }

        public void setPoolMaxSize(Integer poolMaxSize) {
            this.poolMaxSize = poolMaxSize;
        }

        public Integer getPoolBlockingTimeout() {
            return poolBlockingTimeout;
        }

        public void setPoolBlockingTimeout(Integer poolBlockingTimeout) {
            this.poolBlockingTimeout = poolBlockingTimeout;
        }

        public Integer getPoolIdleTimeout() {
            return poolIdleTimeout;
        }

        public void setPoolIdleTimeout(Integer poolIdleTimeout) {
            this.poolIdleTimeout = poolIdleTimeout;
        }

        public Properties getInstanceProps() {
            return instanceProps;
        }
    }

    public static class JMSAdminObjectData {
        private int destinationType;
        private String name;
        private Properties instanceProps = new Properties();

        public void load(PortletRequest request, String prefix) {
            destinationType = isEmpty(request.getParameter(prefix+DEST_TYPE_PARAMETER)) ? -1 : Integer.parseInt(request.getParameter(prefix+DEST_TYPE_PARAMETER));
            name = request.getParameter(prefix+NAME_PARAMETER);
            Map map = request.getParameterMap();
            for(int i=0; i<20; i++) {
                String key = prefix+"instance-config-" + i;
                if(map.containsKey(key)) {
                    instanceProps.setProperty(key.substring(prefix.length()), request.getParameter(key));
                }
            }
        }

        public void save(ActionResponse response, String prefix) {
            if(destinationType > -1) response.setRenderParameter(prefix+DEST_TYPE_PARAMETER, Integer.toString(destinationType));
            if(!isEmpty(name)) response.setRenderParameter(prefix+NAME_PARAMETER, name);
            for (Iterator it = instanceProps.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                if(!isEmpty((String)entry.getValue())) {
                    response.setRenderParameter(prefix+entry.getKey(), (String)entry.getValue());
                }
            }
        }

        public int getDestinationType() {
            return destinationType;
        }

        public void setDestinationType(int destinationType) {
            this.destinationType = destinationType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Properties getInstanceProps() {
            return instanceProps;
        }
    }

    private static String getPropertyName(String propertyKey, JMSProviderData.ConfigPropertyData[] configs) {
        int pos = propertyKey.lastIndexOf('-');
        String num = propertyKey.substring(pos+1);
        return configs[Integer.parseInt(num)].getName();
    }

    protected String save(PortletRequest request, ActionResponse response, JMSResourceData data, boolean planOnly) throws IOException {
        JMSProviderData provider = JMSProviderData.getProviderData(data.rarURI, request);
        if(data.objectName == null || data.objectName.equals("")) { // we're creating a new pool
            //data.instanceName = data.instanceName.replaceAll("\\s", "");
            DeploymentManager mgr = ManagementHelper.getManagementHelper(request).getDeploymentManager();
            try {
                File rarFile = PortletManager.getRepositoryEntry(request, data.getRarURI());
                Bundle rarBundle = PortletManager.getRepositoryEntryBundle(request, data.getRarURI());
                ConnectorDeployable deployable = new ConnectorDeployable(rarBundle);
                DeploymentConfiguration config = mgr.createConfiguration(deployable);
                final DDBeanRoot ddBeanRoot = deployable.getDDBeanRoot();
                Connector15DCBRoot root = (Connector15DCBRoot) config.getDConfigBeanRoot(ddBeanRoot);
                ConnectorDCB connector = (ConnectorDCB) root.getDConfigBean(ddBeanRoot.getChildBean(root.getXpaths()[0])[0]);

                EnvironmentData environment = new EnvironmentData();
                connector.setEnvironment(environment);
                org.apache.geronimo.deployment.service.jsr88.Artifact configId = new org.apache.geronimo.deployment.service.jsr88.Artifact();
                environment.setConfigId(configId);
                configId.setGroupId("console.jms");
                configId.setArtifactId(data.instanceName);
                configId.setVersion("1.0");
                configId.setType("car");
                if(data.dependency != null && !data.dependency.trim().equals("")) {
                    Artifact artifact = Artifact.create(data.dependency.trim());
                    org.apache.geronimo.deployment.service.jsr88.Artifact dep = new org.apache.geronimo.deployment.service.jsr88.Artifact();
                    environment.setDependencies(new org.apache.geronimo.deployment.service.jsr88.Artifact[]{dep});
                    dep.setArtifactId(artifact.getArtifactId());
                    if(artifact.getGroupId() != null) {
                        dep.setGroupId(artifact.getGroupId());
                    }
                    if(artifact.getType() != null) {
                        dep.setType(artifact.getType());
                    }
                    if(artifact.getVersion() != null) {
                        dep.setVersion(artifact.getVersion().toString());
                    }
                }
                
                // Basic settings on RA plan and RA instance
                ResourceAdapter ra;
                if(connector.getResourceAdapter().length > 0) {
                    ra = connector.getResourceAdapter(0);
                } else {
                    ra = new ResourceAdapter();
                    connector.setResourceAdapter(new ResourceAdapter[]{ra});
                }
                ResourceAdapterInstance raInstance = new ResourceAdapterInstance();
                ra.setResourceAdapterInstance(raInstance);
                raInstance.setResourceAdapterName(data.instanceName);
                for (Iterator it = data.instanceProps.entrySet().iterator(); it.hasNext();) {
                    Map.Entry entry = (Map.Entry) it.next();
                    String name = getPropertyName((String)entry.getKey(), provider.getInstanceConfigProperties());
                    for(int i=0; i<raInstance.getConfigPropertySetting().length; i++) {
                        if(raInstance.getConfigPropertySetting(i).getName().equals(name)) {
                            raInstance.getConfigPropertySetting(i).setValue((String)entry.getValue());
                            break;
                        }
                    }
                }
                GBeanLocator workManager = new GBeanLocator();
                raInstance.setWorkManager(workManager);
                workManager.setGBeanLink(data.workManager); //todo
                // Connection Factories
                if(data.getConnectionFactoryCount() > 0) {
                    ConnectionDefinition[] defs = new ConnectionDefinition[data.getConnectionFactoryCount()];
                    for (int i = 0; i < defs.length; i++) {
                        defs[i] = new ConnectionDefinition();
                    }
                    ra.setConnectionDefinition(defs);
                    for (int i = 0; i < data.getConnectionFactories().size(); i++) {
                        JMSConnectionFactoryData factoryData = (JMSConnectionFactoryData) data.getConnectionFactories().get(i);
                        JMSProviderData.ConnectionDefinition providerData = provider.getConnectionDefinitions()[factoryData.getFactoryType()];
                        ConnectionDefinition def = defs[i];
                        def.setConnectionFactoryInterface(providerData.getConnectionFactoryInterface());
                        ConnectionDefinitionInstance instance = new ConnectionDefinitionInstance();
                        def.setConnectionInstance(new ConnectionDefinitionInstance[]{instance});
                        if(providerData.getConnectionFactoryInterface().equals("javax.jms.ConnectionFactory")) {
                            instance.setImplementedInterface(new String[]{"javax.jms.QueueConnectionFactory","javax.jms.TopicConnectionFactory"});
                        }
                        instance.setName(factoryData.getInstanceName());
                        SinglePool pool = new SinglePool();
                        instance.getConnectionManager().setPoolSingle(pool);
                        pool.setMatchOne(true);
                        pool.setMaxSize(factoryData.getPoolMaxSize());
                        pool.setMinSize(factoryData.getPoolMinSize());
                        pool.setBlockingTimeoutMillis(factoryData.getPoolBlockingTimeout());
                        pool.setIdleTimeoutMinutes(factoryData.getPoolIdleTimeout());
                        if(factoryData.getTransaction().equals("none")) {
                            instance.getConnectionManager().setTransactionNone(true);
                        } else if(factoryData.getTransaction().equals("local")) {
                            instance.getConnectionManager().setTransactionLocal(true);
                        } else if(factoryData.getTransaction().equals("xa")) {
                            instance.getConnectionManager().setTransactionXA(true);
                            instance.getConnectionManager().setTransactionXACachingThread(factoryData.isXaThreadCaching());
                            instance.getConnectionManager().setTransactionXACachingTransaction(factoryData.isXaTransactionCaching());
                        }
                        for (Iterator it = factoryData.instanceProps.entrySet().iterator(); it.hasNext();) {
                            Map.Entry entry = (Map.Entry) it.next();
                            String name = getPropertyName((String)entry.getKey(), providerData.getConfigProperties());
                            for(int j=0; j<instance.getConfigPropertySetting().length; j++) {
                                if(instance.getConfigPropertySetting(j).getName().equals(name)) {
                                    instance.getConfigPropertySetting(j).setValue((String)entry.getValue());
                                    break;
                                }
                            }
                        }
                    }
                }

                // Destinations
                DDBean[] ddBeans = connector.getDDBean().getChildBean(connector.getXpaths()[0]);
                AdminObjectDCB[] adminDCBs = new AdminObjectDCB[ddBeans.length];
                for (int i = 0; i < adminDCBs.length; i++) {
                    adminDCBs[i] = (AdminObjectDCB) connector.getDConfigBean(ddBeans[i]);
                }
                for (int i = 0; i < data.getAdminObjects().size(); i++) {
                    JMSAdminObjectData admin = (JMSAdminObjectData) data.getAdminObjects().get(i);
                    JMSProviderData.AdminObjectDefinition providerData = provider.getAdminObjectDefinitions()[admin.getDestinationType()];
                    for (int j = 0; j < adminDCBs.length; j++) {
                        AdminObjectDCB adminDCB = adminDCBs[j];
                        if(adminDCB.getAdminObjectInterface().equals(providerData.getAdminObjectInterface())) {
                            AdminObjectInstance[] before = adminDCB.getAdminObjectInstance();
                            AdminObjectInstance[] after = new AdminObjectInstance[before.length+1];
                            System.arraycopy(before, 0, after, 0, before.length);
                            AdminObjectInstance instance = new AdminObjectInstance();
                            after[before.length] = instance;
                            adminDCB.setAdminObjectInstance(after);
                            instance.setMessageDestinationName(admin.getName());
                            for (Iterator it = admin.instanceProps.entrySet().iterator(); it.hasNext();) {
                                Map.Entry entry = (Map.Entry) it.next();
                                String name = getPropertyName((String)entry.getKey(), providerData.getConfigProperties());
                                for(int k=0; k<instance.getConfigPropertySetting().length; k++) {
                                    if(instance.getConfigPropertySetting(k).getName().equals(name)) {
                                        instance.getConfigPropertySetting(k).setValue((String)entry.getValue());
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }
                }

                // Save
                if(planOnly) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    config.save(out);
                    out.close();
                    return new String(out.toByteArray(), "US-ASCII");
                } else {
                    File tempFile = File.createTempFile("console-deployment",".xml");
                    tempFile.deleteOnExit();
                    log.debug("Writing JMS Resource deployment plan to "+tempFile.getAbsolutePath());
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
                    config.save(out);
                    out.flush();
                    out.close();
                    Target[] targets = mgr.getTargets();
                    if (null == targets) {
                        throw new IllegalStateException("No target to distribute to");
                    }
                    targets = new Target[] {targets[0]};
                    
                    ProgressObject po = mgr.distribute(targets, rarFile, tempFile);
                    waitForProgress(po);
                    if(po.getDeploymentStatus().isCompleted()) {
                        TargetModuleID[] ids = po.getResultTargetModuleIDs();
                        po = mgr.start(ids);
                        waitForProgress(po);
                        if(po.getDeploymentStatus().isCompleted()) {
                            ids = po.getResultTargetModuleIDs();
                            portlet.addInfoMessage(request, portlet.getLocalizedString(request, "activemq.infoMsg02"));
                            log.info("Deployment completed successfully!");
                        }
                    } else if (po.getDeploymentStatus().isFailed()){
                        portlet.addErrorMessage(request, portlet.getLocalizedString(request, "activemq.errorMsg02"), po.getDeploymentStatus().getMessage());
                    }
                }
            } catch (Exception e) {
                log.error("Unable to save connection pool", e);
            } finally {
                if(mgr != null) mgr.release();
            }
        } else { // We're saving updates to an existing pool
            if(planOnly) {
                throw new UnsupportedOperationException("Can't update a plan for an existing deployment");
            }
            throw new UnsupportedOperationException("Can't edit existing configurations yet");
        }
        return null;
    }

    protected static void waitForProgress(ProgressObject po) {
        while(po.getDeploymentStatus().isRunning()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
}


