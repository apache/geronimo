/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.ejbserver;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import org.apache.geronimo.console.util.KernelManagementHelper;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.openejb.EjbContainer;
import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.openejb.Container;
import org.apache.openejb.ContainerType;
import org.apache.openejb.DeploymentInfo;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.Duration;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RemoteProxy
public class EjbHelper {

    private static final Logger log = LoggerFactory.getLogger(EjbHelper.class);
    
    private static final String POOLSIZE = "PoolSize";
    private static final String BULKPASSIVATE = "BulkPassivate";
    private static final String CAPACITY = "Capacity";
    private static final String TIMEOUT = "TimeOut";
    private static final String ACCESSTIMEOUT = "AccessTimeout";
    private static final String PASSIVATOR = "Passivator";
    private static final String STRICTPOOLING = "StrictPooling";
    private static final String INSTANCELIMIT = "InstanceLimit";
    private static final String CMPENGINEFACTORY = "CmpEngineFactory";
    private static final String TXRECOVERY = "TxRecovery";
    private static final String RESOURCEADAPTER  = "ResourceAdapter";
    private static final String MESSAGELISTENERINTERFACE = "MessageListenerInterface";
    private static final String ACTIVATIONSPECCLASS = "ActivationSpecClass";
    private static final String TRUE = "true";
    private static final String FALSE = "false";
    private static final String BUNDLE_NAME = "openejb-portlet";
    private static final String BEANCLASSNAME_KEY="portlet.openejb.view.beanclassname";
    private static final String BLI_KEY="portlet.openejb.view.businesslocalinterfaces";
    private static final String BRI_KEY="portlet.openejb.view.businessremoteinterfaces";
    private static final String DEPLOYMENTID_KEY="portlet.openejb.view.deploymentid";
    private static final String EJBNAME_KEY="portlet.openejb.view.ejbname";
    private static final String EJBHOMEI_KEY="portlet.openejb.view.ejbhomeinterface";
    private static final String JNDINAMES_KEY="portlet.openejb.view.jndinames";
    private static final String LHI_KEY="portlet.openejb.view.localhomeinterface";
    private static final String LI_KEY="portlet.openejb.view.localinterface";
    private static final String RI_KEY="portlet.openejb.view.remoteinterface";
    private static final String PKC_KEY="portlet.openejb.view.primarykeyclass";
    private static final String PKF_KEY="portlet.openejb.view.primarykeyfield";
    private static final String SEI_KEY="portlet.openejb.view.sepinterface";
    private static final String CC_KEY="portlet.openejb.view.containerclass";
    private static final String CI_KEY="portlet.openejb.view.containerid";
    private static final String CD_KEY="portlet.openejb.view.containerdescription";
    private static final String DN_KEY="portlet.openejb.view.displayname";
    
    
    private ContainerSystem containerSystem;
    private OpenEjbConfiguration configuration;
    private Kernel kernel;    
    private ResourceBundle resourceBundle;
    private KernelManagementHelper helper = null;
    public EjbHelper() {
        initContainerSystem();
    }

    private void initContainerSystem() {
        kernel = KernelRegistry.getSingleKernel();
        helper = new KernelManagementHelper(kernel);
        containerSystem = SystemInstance.get().getComponent(
                ContainerSystem.class);
        configuration = SystemInstance.get().getComponent(
                OpenEjbConfiguration.class);
        resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
    }

    @RemoteMethod
    public Tree getEjbInformation() {

        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        ClassLoader newcl = null;
        Container container = null;
        ContainerSystemInfo systemInfo = configuration.containerSystem;
        Map<ContainerType, TreeEntry> containerMap = new TreeMap<ContainerType, TreeEntry>();
        List<ContainerInfo> containerInfos = systemInfo.containers;
        ContainerInfo containerInfo = null;
        TreeEntry containerTypesEntry, containersEntry, deploymentsEntry = null;
        Tree tree = new Tree("name", "name");
        List<TreeEntry> entries = new ArrayList<TreeEntry>();
        for (int i = 0; i < containerInfos.size(); i++) {

            containerInfo = containerInfos.get(i);
            container = containerSystem.getContainer(containerInfo.id);
            List<TreeEntry> containers = null;
            if (containerMap.get(container.getContainerType()) != null) {
                containerTypesEntry = containerMap.get(container
                        .getContainerType());
                containers = containerTypesEntry.getChildren();
            } else {
                containerTypesEntry = new TreeEntry();
                containerTypesEntry.setName(resolveContainerTypes(container
                        .getContainerType()));
                containerTypesEntry.setValue(containerTypesEntry.getName());
                containers = new ArrayList<TreeEntry>();
                containerTypesEntry.setChildren(containers);
                containerMap.put(container.getContainerType(),
                        containerTypesEntry);
                entries.add(containerTypesEntry);
            }
            containersEntry = new TreeEntry();
            containersEntry.setName(containerInfo.id);
            containersEntry.setValue(containerInfo.id);

            DeploymentInfo[] deployments = container.deployments();
            containersEntry.setChildren(getDeployments(deployments));
            containers.add(containersEntry);
        }
        tree.setItems(entries);
        return tree;
    }

    public List getDeployments(DeploymentInfo[] deploymentInfos) {
        List<TreeEntry> deployments = new ArrayList<TreeEntry>();
        TreeEntry deploymentsEntry = null;
        for (DeploymentInfo deployment : deploymentInfos) {
            deploymentsEntry = new TreeEntry();
            deploymentsEntry.setName(deployment.getEjbName());
            deploymentsEntry.setValue(deployment.getContainer()
                    .getContainerID()
                    + "#^~" + deployment.getDeploymentID());
            deploymentsEntry.setChildren(new ArrayList<TreeEntry>());
            deployments.add(deploymentsEntry);
        }
        return deployments;
    }

    @RemoteMethod
    public List<EjbInformation> getContainerInfo(String containerId) {
        Container container = containerSystem.getContainer(containerId);
        if (container == null)
            return null;
        ContainerSystemInfo systemInfo = configuration.containerSystem;
        List<ContainerInfo> containerInfos = systemInfo.containers;
        List<EjbInformation> infos = new ArrayList<EjbInformation>();
        EjbInformation information = null;
        GBeanData data = null;
        if (container.getContainerType() == ContainerType.MESSAGE_DRIVEN) {
            data = getOpenEJBPropertiesGBean(containerId, OpenEjbSystem.class);
        } else {
            data = getOpenEJBPropertiesGBean(containerId, EjbContainer.class);
        }
        Properties props = null;
        if (data != null) {
            props = (Properties) data.getAttribute("properties");
        }
        for (ContainerInfo containerInfo : containerInfos) {
            if (containerInfo.id.equals(containerId)) {
                information = new EjbInformation();
                information.setName(resourceBundle.getString(CC_KEY));
                information.setId("ContainerClass");
                information.setValue(containerInfo.className);
                infos.add(information);
                information = new EjbInformation();
                information.setName(resourceBundle.getString(CI_KEY));
                information.setId("ContainerId");
                information.setValue(containerInfo.id);
                infos.add(information);
                information = new EjbInformation();
                information.setName(resourceBundle.getString(CD_KEY));
                information.setId("ContainerDesc");
                information.setValue(containerInfo.description);
                infos.add(information);
                information = new EjbInformation();
                information.setName(resourceBundle.getString(DN_KEY));
                information.setId("DisplayName");
                information.setValue(containerInfo.displayName);
                infos.add(information);
                List<String> editableProperties = new ArrayList<String>();
                editableProperties.add(POOLSIZE);
                editableProperties.add(BULKPASSIVATE);
                editableProperties.add(TIMEOUT);
                editableProperties.add(ACCESSTIMEOUT);
                editableProperties.add(CAPACITY);
                editableProperties.add(STRICTPOOLING);
                editableProperties.add(INSTANCELIMIT);                

                for (Map.Entry entry : containerInfo.properties.entrySet()) {
                    information = new EjbInformation();
                    information.setName(entry.getKey().toString());
                    information.setId(entry.getKey().toString());
                    if (entry.getKey().toString().equals(RESOURCEADAPTER)) {
                        information.setValue(entry.getValue().getClass()
                                .getName());
                    } else {
                        information.setValue(entry.getValue().toString());
                    }
                    if (editableProperties.contains(entry.getKey().toString())) {
                        String key = information.getName();
                        String value = null;
                        if (props != null && props.containsKey(key)) {
                            value = (String) props.get(key);
                        } else if (props != null
                                && props.containsKey(containerId + "." + key)) {
                            value = (String) props.get(containerId + "." + key);
                        } else {
                            value = information.getValue();
                        }
                        if (!value.equals(information.getValue())) {
                            information.setDirty(TRUE);
                        }
                        information.setValue(value);
                        information.setEditable(TRUE);
                    } else {
                        information.setEditable(FALSE);
                    }
                    infos.add(information);
                }

            }
        }
        return infos;
    }

    @RemoteMethod
    public Status setContainerProperty(String containerId, String propertyKey,
            String propertyValue) {
        propertyKey = propertyKey.trim();
        propertyValue = propertyValue.trim();
        if (containerId.indexOf("%20") != -1) {
            containerId = containerId.replaceAll("%20", "\\ ");
        }
        List<String> numericProperties = new ArrayList<String>();
        numericProperties.add(POOLSIZE);
        numericProperties.add(BULKPASSIVATE);
        numericProperties.add(TIMEOUT);
        numericProperties.add(INSTANCELIMIT); 
        numericProperties.add(CAPACITY);
        numericProperties.add(ACCESSTIMEOUT);
        
        if (numericProperties.contains(propertyKey)) {
            try {
                Integer.parseInt(propertyValue);
            } catch (NumberFormatException nfe) {
                return new Status(MessageFormat.format(resourceBundle.getString("portlet.openejb.view.message1"),propertyKey));
            }
        } else if (STRICTPOOLING.equals(propertyKey)) {
            if (!propertyValue.equalsIgnoreCase(TRUE)
                    && !propertyValue.equalsIgnoreCase(FALSE)) {
                return new Status(MessageFormat.format(resourceBundle.getString("portlet.openejb.view.message2"),propertyKey));
            }        
        } else {
            try {
                EjbHelper.class.getClassLoader().loadClass(propertyValue);
            } catch (ClassNotFoundException e) {
                return new Status(MessageFormat.format(resourceBundle.getString("portlet.openejb.view.message3"),propertyKey));
            }
        }

        Properties props = null;
        Container container = containerSystem.getContainer(containerId);
        if (container.getContainerType() == ContainerType.MESSAGE_DRIVEN) {
            OpenEjbSystem openEjbSystem = null;
            AbstractNameQuery absQuery = new AbstractNameQuery(
                    OpenEjbSystem.class.getName());
            Set systemGBeans = kernel.listGBeans(absQuery);
            for (Object obj : systemGBeans) {
            	AbstractName absName = (AbstractName) obj;
                openEjbSystem = kernel.getProxyManager()
                        .createProxy(absName, OpenEjbSystem.class);
                props = openEjbSystem.getProperties();
                if (props == null) {
                    props = new Properties();
                }
                props.put(containerId + "." + propertyKey, propertyValue);
                openEjbSystem.setProperties(props);
                getGBeanDataFromConfiguration(absName).setAttribute("properties", props);

            }
        } else {
            AbstractNameQuery absQuery = new AbstractNameQuery(
                    EjbContainer.class.getName());
            Set containerGBeans = kernel.listGBeans(absQuery);
            for (Object obj : containerGBeans) {
                try {
                    String id = (String) kernel.getAttribute(
                            (AbstractName) obj, "id");
                    if (containerId.equals(id)) {
                        AbstractName absName = (AbstractName) obj;
                        GBeanData gData1  = kernel.getGBeanData(absName);
                        ManageableAttributeStore attributeStore = kernel.getGBean(ManageableAttributeStore.class);                        
                        GBeanData gData  = getGBeanDataFromConfiguration(absName);
                        for(String attributeName : gData.getAttributeNames()){
                            if(attributeName.equalsIgnoreCase(propertyKey)){
                                // Hack to make changed values reflect on configuration restart.
                                gData.setAttribute(attributeName, propertyValue);
                                Properties gbeanProps = (Properties)gData1.getAttribute("properties");
                                gbeanProps.setProperty(propertyKey, propertyValue);
                                GAttributeInfo gAttributeInfo = gData.getGBeanInfo().getAttribute(attributeName);
                                attributeStore.setValue(absName.getArtifact(), absName, gAttributeInfo, propertyValue, Thread.currentThread().getContextClassLoader());
                            }
                        }
                    }
                } catch (GBeanNotFoundException e) {
                    return new Status(
                            resourceBundle.getString("portlet.openejb.view.errorMessage1"));
                } catch (NoSuchAttributeException e) {
                    return new Status(
                            resourceBundle.getString("portlet.openejb.view.errorMessage1"));
                } catch (Exception e) {
                    return new Status(
                            resourceBundle.getString("portlet.openejb.view.errorMessage1"));
                }
            }
        }
        return new Status(resourceBundle.getString("portlet.openejb.view.message4"));
    }
    
    private GBeanData getGBeanDataFromConfiguration(AbstractName absName){
        Configuration configuration = ConfigurationUtil.getConfigurationManager(kernel).getConfiguration(absName.getArtifact());
        GBeanData gData  = configuration.getGBeans().get(absName);
        return gData;    	
    }
    @RemoteMethod
    public List<EjbInformation> getDeploymentInfo(String containerId,
            String deploymentId) {
        Container container = containerSystem.getContainer(containerId);
        DeploymentInfo deploymentInfo = container
                .getDeploymentInfo(deploymentId);
        List<EjbInformation> informations = new ArrayList<EjbInformation>();
        EjbInformation information = new EjbInformation();
        information.setName(resourceBundle.getString(BEANCLASSNAME_KEY));        
        information.setValue(deploymentInfo.getBeanClass().getName());
        informations.add(information);

        if (deploymentInfo.getBusinessLocalInterface() != null) {
            information = new EjbInformation();
            information.setName(resourceBundle.getString(BLI_KEY));
            information.setValue(appendMultipleInterfaces(deploymentInfo
                    .getBusinessLocalInterfaces()));
            informations.add(information);
        }
        if (deploymentInfo.getBusinessRemoteInterface() != null) {
            information = new EjbInformation();
            information.setName(resourceBundle.getString(BRI_KEY));
            information.setValue(appendMultipleInterfaces(deploymentInfo
                    .getBusinessRemoteInterfaces()));
            informations.add(information);
        }
        information = new EjbInformation();
        information.setName(resourceBundle.getString(DEPLOYMENTID_KEY));
        information.setValue(deploymentId);
        informations.add(information);
        ;
        information = new EjbInformation();
        information.setName(resourceBundle.getString(EJBNAME_KEY));
        information.setValue(deploymentInfo.getEjbName());
        informations.add(information);
        if (deploymentInfo.getHomeInterface() != null) {
            information = new EjbInformation();
            information.setValue(deploymentInfo.getHomeInterface().getName());
            information.setName(resourceBundle.getString(EJBHOMEI_KEY));
            informations.add(information);
        }
        if (!container.getContainerType().equals(ContainerType.MESSAGE_DRIVEN)) {
            information = new EjbInformation();
            Class cls = null;
            try {
                cls = Class
                        .forName("org.apache.openejb.assembler.classic.JndiBuilder$Bindings");
                Method method = cls.getMethod("getBindings");
                List<String> jndiNames = (List) method.invoke(deploymentInfo
                        .get(cls));
                StringBuffer names = new StringBuffer();
                for (String jndiName : jndiNames) {
                    if (jndiName.startsWith("openejb/ejb/")) {
                        jndiName = jndiName.replaceFirst("openejb/ejb/", "");
                        names.append(jndiName).append(",");
                    }
                }
                information.setValue(names.substring(0, names.length() - 1));
            } catch (Exception e) {
                log.error(resourceBundle.getString("portlet.openejb.view.errorMessage2"), e);
            }
            information.setName(resourceBundle.getString(JNDINAMES_KEY));
            informations.add(information);
        }
        if (deploymentInfo.getLocalHomeInterface() != null) {
            information = new EjbInformation();
            information.setName(resourceBundle.getString(LHI_KEY));
            information.setValue(deploymentInfo.getLocalHomeInterface()
                    .getName());
            informations.add(information);
        }

        if (deploymentInfo.getLocalInterface() != null) {
            information = new EjbInformation();
            information.setName(resourceBundle.getString(LI_KEY));
            information.setValue(deploymentInfo.getLocalInterface().getName());
            informations.add(information);
        }

        if (deploymentInfo.getRemoteInterface() != null) {
            information = new EjbInformation();
            information.setName(resourceBundle.getString(RI_KEY));
            information.setValue(deploymentInfo.getRemoteInterface().getName());
            informations.add(information);
        }

        if (deploymentInfo.getPrimaryKeyClass() != null) {
            information = new EjbInformation();
            information.setName(resourceBundle.getString(PKC_KEY));
            information.setValue(deploymentInfo.getPrimaryKeyClass().getName());
            informations.add(information);
        }

        if (deploymentInfo.getPrimaryKeyField() != null) {
            information = new EjbInformation();
            information.setName(resourceBundle.getString(PKF_KEY));
            information.setValue(deploymentInfo.getPrimaryKeyField());
            informations.add(information);
        }

        if (deploymentInfo.getServiceEndpointInterface() != null) {
            information = new EjbInformation();
            information.setName(resourceBundle.getString(SEI_KEY));
            information.setValue(deploymentInfo.getServiceEndpointInterface()
                    .getName());
            informations.add(information);
        }

        return informations;
    }

    private String appendMultipleInterfaces(List<Class> multipleValues) {
        StringBuffer multipleValuedStringBuffer = new StringBuffer();
        for (Class singleValue : multipleValues) {
            multipleValuedStringBuffer.append(singleValue.getName())
                    .append(",");
        }
        if (multipleValuedStringBuffer.length() == 0) {
            return null;
        }
        return multipleValuedStringBuffer.substring(0,
                multipleValuedStringBuffer.length() - 1);
    }

    private String resolveContainerTypes(ContainerType cType) {
        switch (cType) {
        case STATELESS:
            return "Stateless Containers";
        case STATEFUL:
            return "Stateful Containers";
        case CMP_ENTITY:
            return "CMP Entity Containers";
        case BMP_ENTITY:
            return "BMP Entity Containers";
        case MESSAGE_DRIVEN:
            return "Message Driven Containers";
        default:
            return "None";
        }
    }

    private GBeanData getOpenEJBPropertiesGBean(String containerId, Class query) {
        GBeanData data = null;
        AbstractNameQuery absQuery = new AbstractNameQuery(query.getName());
        Set containerGBeans = kernel.listGBeans(absQuery);
        for (Object obj : containerGBeans) {
            try {
                data = kernel.getGBeanData((AbstractName) obj);
            } catch (GBeanNotFoundException e) {
                log.error(MessageFormat.format(resourceBundle.getString("portlet.openejb.view.errorMessage3")
                        , (AbstractName) obj), e);
            } catch (InternalKernelException e) {
                log.error(MessageFormat.format(resourceBundle.getString("portlet.openejb.view.errorMessage4")
                        , (AbstractName) obj), e);
            } catch (IllegalStateException e) {
                log.error(MessageFormat.format(resourceBundle.getString("portlet.openejb.view.errorMessage5")
                        , (AbstractName) obj), e);
            }
            if (containerId.equals(EjbContainer.getId(data.getAbstractName()))
                    || query == OpenEjbSystem.class) {
                break;
            }
            data = null;
        }
        return data;
    }

}
