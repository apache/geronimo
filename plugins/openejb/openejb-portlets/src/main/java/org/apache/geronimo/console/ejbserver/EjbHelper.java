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
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.geronimo.console.BaseRemoteProxy;
import org.apache.geronimo.console.message.CommonMessage;
import org.apache.geronimo.console.message.JSCommonMessage;
import org.apache.geronimo.console.util.KernelManagementHelper;
import org.apache.geronimo.console.util.Tree;
import org.apache.geronimo.console.util.TreeEntry;
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
import org.apache.openejb.BeanContext;
import org.apache.openejb.Container;
import org.apache.openejb.ContainerType;
import org.apache.openejb.assembler.classic.ContainerInfo;
import org.apache.openejb.assembler.classic.ContainerSystemInfo;
import org.apache.openejb.assembler.classic.OpenEjbConfiguration;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RemoteProxy
public class EjbHelper extends BaseRemoteProxy {

    private static final Logger log = LoggerFactory.getLogger(EjbHelper.class);

    private static final String POOLSIZE = "MaxSize";
    private static final String POOLMIN = "MinSize";
    private static final String BULKPASSIVATE = "BulkPassivate";
    private static final String CAPACITY = "Capacity";
    private static final String CLOSETIMEOUT = "CloseTimeout";
    private static final String ACCESSTIMEOUT = "AccessTimeout";
    private static final String IDLETIMEOUT = "IdleTimeout";    
    private static final String PASSIVATOR = "Passivator";
    private static final String TIMEOUT = "TimeOut";
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
                containerTypesEntry = containerMap.get(container.getContainerType());
                containers = containerTypesEntry.getChildren();
            } else {
                containerTypesEntry = new TreeEntry();
                containerTypesEntry.setName(resolveContainerTypes(container.getContainerType()));
                containerTypesEntry.setValues(new String[]{containerTypesEntry.getName()});
                containers = new ArrayList<TreeEntry>();
                containerTypesEntry.setChildren(containers);
                containerMap.put(container.getContainerType(),containerTypesEntry);
                entries.add(containerTypesEntry);
            }
            containersEntry = new TreeEntry();
            containersEntry.setName(containerInfo.id);
            containersEntry.setValues(new String[]{containerInfo.id});

            BeanContext[] deployments = container.getBeanContexts();
            containersEntry.setChildren(getDeployments(deployments));
            containers.add(containersEntry);
        }
        tree.setItems(entries);
        return tree;
    }

    public List<TreeEntry> getDeployments(BeanContext[] beanContexts) {
        List<TreeEntry> deployments = new ArrayList<TreeEntry>();
        TreeEntry deploymentsEntry = null;
        for (BeanContext deployment : beanContexts) {
            deploymentsEntry = new TreeEntry();
            deploymentsEntry.setName(deployment.getEjbName());
            deploymentsEntry.setValues(new String[]{
                    deployment.getContainer().getContainerID().toString(),
                    deployment.getDeploymentID().toString()});
            deploymentsEntry.setChildren(new ArrayList<TreeEntry>());
            deployments.add(deploymentsEntry);
        }
        return deployments;
    }

    @RemoteMethod
    public String getCurrentContainerProperty(String containerId, String propertyKey){
        ContainerSystemInfo systemInfo = configuration.containerSystem;
        List<ContainerInfo> containerInfos = systemInfo.containers;

        for (ContainerInfo containerInfo : containerInfos) {
        	containerId = replaceEscapes(containerId);
            if (containerInfo.id.equals(containerId)) {
            	return containerInfo.properties.getProperty(propertyKey);
            }
        }
        return null;
    }

    @RemoteMethod
    public List<EjbInformation> getContainerInfo(String containerId, HttpServletRequest request) {
    	containerId = replaceEscapes(containerId);
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
                information.setName(getLocalizedString(request, BUNDLE_NAME, CC_KEY));
                information.setId("ContainerClass");
                information.setValue(containerInfo.className);
                infos.add(information);
                information = new EjbInformation();
                information.setName(getLocalizedString(request, BUNDLE_NAME, CI_KEY));
                information.setId("ContainerId");
                information.setValue(containerInfo.id);
                infos.add(information);
                information = new EjbInformation();
                information.setName(getLocalizedString(request, BUNDLE_NAME, CD_KEY));
                information.setId("ContainerDesc");
                information.setValue(containerInfo.description);
                infos.add(information);
                information = new EjbInformation();
                information.setName(getLocalizedString(request, BUNDLE_NAME, DN_KEY));
                information.setId("DisplayName");
                information.setValue(containerInfo.displayName);
                infos.add(information);

                for (Map.Entry entry : containerInfo.properties.entrySet()) {
                    information = new EjbInformation();

                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();

                    information.setId(key);

                    String name = key;
                    if (ACCESSTIMEOUT.equals(key)) {
                        name = key + " (seconds)";
                    } else if (TIMEOUT.equals(key) || CLOSETIMEOUT.equals(key) || IDLETIMEOUT.equals(key)) {
                        name = key + " (minutes)";
                    }
                    information.setName(name);

                    String attributeName = key.substring(0,1).toLowerCase() + key.substring(1);
                    Object attributeValue = data.getAttribute(attributeName);
                    if (attributeValue != null) {
                        information.setEditable(TRUE);
                        information.setValue(attributeValue.toString());
                        if (props != null && !value.equals(props.get(key))) {
                            information.setDirty(TRUE);
                        }
                    } else {
                        information.setEditable(FALSE);
                        if (key.equals(RESOURCEADAPTER)) {
                            information.setValue(entry.getValue().getClass().getName());
                        } else {
                            information.setValue(value);
                        }
                    }
                    infos.add(information);
                }

            }
        }
        return infos;
    }

    @RemoteMethod
    public JSCommonMessage setContainerProperty(String containerId, String propertyKey,
            String propertyValue, HttpServletRequest request) {
        propertyKey = propertyKey.trim();
        propertyValue = propertyValue.trim();

        containerId = replaceEscapes(containerId);

        Container container = containerSystem.getContainer(containerId);

        String query = null;

        if (container.getContainerType() == ContainerType.MESSAGE_DRIVEN) {
            query = OpenEjbSystem.class.getName();
        } else {
            query = EjbContainer.class.getName();
        }

        AbstractNameQuery absQuery = new AbstractNameQuery(query);
        Set<AbstractName> gbeans = kernel.listGBeans(absQuery);
        for (AbstractName gbean : gbeans) {
            if (!gbean.getName().get("name").equals(containerId)) {
                continue;
            }
            String attributeName = propertyKey.substring(0,1).toLowerCase() + propertyKey.substring(1);
            Object attributeValue = null;

            if (ACCESSTIMEOUT.equals(propertyKey) || TIMEOUT.equals(propertyKey)) {
                try {
                    attributeValue = Long.parseLong(propertyValue);
                } catch (NumberFormatException nfe) {
                    return new JSCommonMessage(CommonMessage.Type.Error, 
                            getLocalizedString(request, BUNDLE_NAME, "portlet.openejb.view.numeric", propertyKey), null);
                }
            } else if (STRICTPOOLING.equals(propertyKey)) {
                if (!propertyValue.equalsIgnoreCase(TRUE)
                        && !propertyValue.equalsIgnoreCase(FALSE)) {
                    return new JSCommonMessage(CommonMessage.Type.Error, 
                            getLocalizedString(request, BUNDLE_NAME, "portlet.openejb.view.boolean", propertyKey), null);
                }
                attributeValue = Boolean.parseBoolean(propertyValue);
            } else {
                try {
                    attributeValue = Integer.parseInt(propertyValue);
                } catch (NumberFormatException nfe) {
                    return new JSCommonMessage(CommonMessage.Type.Error, 
                            getLocalizedString(request, BUNDLE_NAME, "portlet.openejb.view.numeric", propertyKey), null);
                }
            }

            try {
                kernel.setAttribute(gbean, attributeName, attributeValue);
            } catch (GBeanNotFoundException e) {
                return new JSCommonMessage(CommonMessage.Type.Error,
                        getLocalizedString(request, BUNDLE_NAME, "portlet.openejb.view.unchanged", propertyKey), null);
            } catch (NoSuchAttributeException e) {
                return new JSCommonMessage(CommonMessage.Type.Error,
                        getLocalizedString(request, BUNDLE_NAME, "portlet.openejb.view.unchanged", propertyKey), null);
            } catch (Exception e) {
                return new JSCommonMessage(CommonMessage.Type.Error,
                        getLocalizedString(request, BUNDLE_NAME, "portlet.openejb.view.unchanged", propertyKey), null);
            }
            
            return new JSCommonMessage(CommonMessage.Type.Warn, 
                    getLocalizedString(request, BUNDLE_NAME, "portlet.openejb.view.restart"), null);
        }

        return new JSCommonMessage(CommonMessage.Type.Error,
                getLocalizedString(request, BUNDLE_NAME, "portlet.openejb.view.unchanged", propertyKey), null);
    }

    private GBeanData getGBeanDataFromConfiguration(AbstractName absName){
        try {
            Configuration configuration = ConfigurationUtil.getConfigurationManager(kernel).getConfiguration(absName.getArtifact());
            GBeanData gData  = configuration.getGBeans().get(absName);
            return gData;
        } catch (GBeanNotFoundException e) {
            return null;
        }
    }
    @RemoteMethod
    public List<EjbInformation> getDeploymentInfo(String containerId,
            String deploymentId, HttpServletRequest request) {
        Container container = containerSystem.getContainer(containerId);
        BeanContext beanContext = container
                .getBeanContext(deploymentId);
        List<EjbInformation> informations = new ArrayList<EjbInformation>();
        EjbInformation information = new EjbInformation();
        information.setName(getLocalizedString(request, BUNDLE_NAME, BEANCLASSNAME_KEY));
        information.setValue(beanContext.getBeanClass().getName());
        informations.add(information);

        if (beanContext.getBusinessLocalInterface() != null) {
            information = new EjbInformation();
            information.setName(getLocalizedString(request, BUNDLE_NAME, BLI_KEY));
            information.setValue(appendMultipleInterfaces(beanContext
                    .getBusinessLocalInterfaces()));
            informations.add(information);
        }
        if (beanContext.getBusinessRemoteInterface() != null) {
            information = new EjbInformation();
            information.setName(getLocalizedString(request, BUNDLE_NAME, BRI_KEY));
            information.setValue(appendMultipleInterfaces(beanContext
                    .getBusinessRemoteInterfaces()));
            informations.add(information);
        }
        information = new EjbInformation();
        information.setName(getLocalizedString(request, BUNDLE_NAME, DEPLOYMENTID_KEY));
        information.setValue(deploymentId);
        informations.add(information);
        information = new EjbInformation();
        information.setName(getLocalizedString(request, BUNDLE_NAME, EJBNAME_KEY));
        information.setValue(beanContext.getEjbName());
        informations.add(information);
        if (beanContext.getHomeInterface() != null) {
            information = new EjbInformation();
            information.setValue(beanContext.getHomeInterface().getName());
            information.setName(getLocalizedString(request, BUNDLE_NAME, EJBHOMEI_KEY));
            informations.add(information);
        }
        if (!container.getContainerType().equals(ContainerType.MESSAGE_DRIVEN)) {
            information = new EjbInformation();
            Class cls = null;
            try {
                cls = Class.forName("org.apache.openejb.assembler.classic.JndiBuilder$Bindings");
                Method method = cls.getMethod("getBindings");
                List<String> jndiNames = (List) method.invoke(beanContext.get(cls));
                StringBuilder names = new StringBuilder();
                for (String jndiName : jndiNames) {
                    if (jndiName.startsWith("openejb/local/")) {
                        jndiName = jndiName.replaceFirst("openejb/local/", "");
                        names.append(jndiName).append(",");
                    }
                }
                information.setValue(names.substring(0, names.length() - 1));
            } catch (Exception e) {
                log.error("Exception when trying to get JNDI name", e);
            }
            information.setName(getLocalizedString(request, BUNDLE_NAME, JNDINAMES_KEY));
            informations.add(information);
        }
        if (beanContext.getLocalHomeInterface() != null) {
            information = new EjbInformation();
            information.setName(getLocalizedString(request, BUNDLE_NAME, LHI_KEY));
            information.setValue(beanContext.getLocalHomeInterface()
                    .getName());
            informations.add(information);
        }

        if (beanContext.getLocalInterface() != null) {
            information = new EjbInformation();
            information.setName(getLocalizedString(request, BUNDLE_NAME, LI_KEY));
            information.setValue(beanContext.getLocalInterface().getName());
            informations.add(information);
        }

        if (beanContext.getRemoteInterface() != null) {
            information = new EjbInformation();
            information.setName(getLocalizedString(request, BUNDLE_NAME, RI_KEY));
            information.setValue(beanContext.getRemoteInterface().getName());
            informations.add(information);
        }

        if (beanContext.getPrimaryKeyClass() != null) {
            information = new EjbInformation();
            information.setName(getLocalizedString(request, BUNDLE_NAME, PKC_KEY));
            information.setValue(beanContext.getPrimaryKeyClass().getName());
            informations.add(information);
        }

        if (beanContext.getPrimaryKeyField() != null) {
            information = new EjbInformation();
            information.setName(getLocalizedString(request, BUNDLE_NAME, PKF_KEY));
            information.setValue(beanContext.getPrimaryKeyField());
            informations.add(information);
        }

        if (beanContext.getServiceEndpointInterface() != null) {
            information = new EjbInformation();
            information.setName(getLocalizedString(request, BUNDLE_NAME, SEI_KEY));
            information.setValue(beanContext.getServiceEndpointInterface()
                    .getName());
            informations.add(information);
        }

        return informations;
    }

    private String appendMultipleInterfaces(List<Class> multipleValues) {
        StringBuilder multipleValuedStringBuilder = new StringBuilder();
        for (Class singleValue : multipleValues) {
            multipleValuedStringBuilder.append(singleValue.getName())
                    .append(",");
        }
        if (multipleValuedStringBuilder.length() == 0) {
            return null;
        }
        return multipleValuedStringBuilder.substring(0,
                multipleValuedStringBuilder.length() - 1);
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
        Set<AbstractName> containerGBeans = kernel.listGBeans(absQuery);
        for (AbstractName absName : containerGBeans) {
            try {
                data = kernel.getGBeanData(absName);
            } catch (GBeanNotFoundException e) {
                log.error(MessageFormat.format("GBeanNotFoundException for GBean Name: {0}"
                        , absName), e);
            } catch (InternalKernelException e) {
                log.error(MessageFormat.format("InternalKernelException for GBean Name: {0}"
                        , absName), e);
            } catch (IllegalStateException e) {
                log.error(MessageFormat.format("IllegalStateException for GBean Name: {0}"
                        , absName), e);
            }
            if (containerId.equals(EjbContainer.getId(data.getAbstractName()))
                    || query == OpenEjbSystem.class) {
                break;
            }
            data = null;
        }
        return data;
    }

    private String replaceEscapes(String escaped){
    	if (escaped.indexOf("%20") != -1) {
    	    return escaped.replaceAll("%20"," ");
    	}
    	return escaped;
    }

}
