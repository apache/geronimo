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

package org.apache.geronimo.connector.deployment;

import java.beans.PropertyEditor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarInputStream;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.resource.spi.security.PasswordCredential;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.AdminObjectWrapper;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.LocalTransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionLog;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PartitionedPool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.NoPool;
import org.apache.geronimo.connector.outbound.security.PasswordCredentialRealm;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.util.UnclosableInputStream;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerVersionType;
import org.apache.geronimo.xbeans.j2ee.AdminobjectType;
import org.apache.geronimo.xbeans.j2ee.ConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.ConnectionDefinitionType;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.geronimo.xbeans.j2ee.ConnectorType;
import org.apache.geronimo.xbeans.j2ee.ResourceadapterType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.11 $ $Date: 2004/05/06 03:58:22 $
 *
 * */
public class RAR_1_5ConfigBuilder extends AbstractRARConfigBuilder {


    public RAR_1_5ConfigBuilder(Kernel kernel, Repository repository, ObjectName connectionTrackerNamePattern) {
        super(kernel, repository, connectionTrackerNamePattern);
    }

    protected XmlObject getConnectorDocument(JarInputStream jarInputStream) throws XmlException, IOException, DeploymentException {
        ConnectorDocument connectorDocument = ConnectorDocument.Factory.parse(new UnclosableInputStream(jarInputStream));
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        Collection errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
        if (!connectorDocument.validate(xmlOptions)) {
            throw new DeploymentException("Invalid deployment descriptor: errors: " + errors);
        }
        return connectorDocument;
    }

    public boolean canConfigure(XmlObject plan) {
        return plan.schemaType() == TYPE && GerVersionType.X_1_5.equals(((GerConnectorDocument) plan).getConnector().getVersion());
    }

    void addConnectorGBeans(DeploymentContext context, XmlObject genericConnectorDocument, GerConnectorType geronimoConnector, ClassLoader cl) throws DeploymentException {
        //addGBeans called from here so it is included in tests.
        addGBeans(geronimoConnector, cl, context);

        ConnectorType connector = ((ConnectorDocument) genericConnectorDocument).getConnector();
        ResourceadapterType resourceadapter = connector.getResourceadapter();
        GerResourceadapterType geronimoResourceAdapter = geronimoConnector.getResourceadapter();
        //ResourceAdapter setup
        String resourceAdapterClassName = resourceadapter.getResourceadapterClass().getStringValue();
        if (resourceAdapterClassName == null) {
            throw new DeploymentException("No resource adapter class provided for J2ee Connector Architecture 1.5 adapter");
        }
        ObjectName resourceAdapterObjectName = null;
        GBeanInfoFactory resourceAdapterInfoFactory = new GBeanInfoFactory(ResourceAdapterWrapper.class.getName(), ResourceAdapterWrapper.getGBeanInfo());
        GBeanMBean resourceAdapterGBean = setUpDynamicGBean(resourceAdapterInfoFactory, resourceadapter.getConfigPropertyArray(), geronimoResourceAdapter.getResourceadapterInstance().getConfigPropertySettingArray());
        try {
            resourceAdapterGBean.setAttribute("ResourceAdapterClass", cl.loadClass(resourceAdapterClassName));
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        ObjectName bootstrapContextObjectName = null;
        try {
            bootstrapContextObjectName = ObjectName.getInstance(geronimoResourceAdapter.getResourceadapterInstance().getBootstrapcontextName().getStringValue());
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not create object name for bootstrap context", e);
        }
        resourceAdapterGBean.setReferencePatterns("BootstrapContext", Collections.singleton(bootstrapContextObjectName));
        try {
            resourceAdapterObjectName = ObjectName.getInstance(BASE_RESOURCE_ADAPTER_NAME + geronimoResourceAdapter.getResourceadapterInstance().getResourceadapterName() + ",configID=" + context.getConfigID());
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct resource adapter object name", e);
        }
        context.addGBean(resourceAdapterObjectName, resourceAdapterGBean);

        Map connectionDefinitions = new HashMap();
        for (int j = 0; j < resourceadapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; j++) {
            ConnectionDefinitionType connectionDefinition = resourceadapter.getOutboundResourceadapter().getConnectionDefinitionArray(j);
            connectionDefinitions.put(connectionDefinition.getConnectionfactoryInterface().getStringValue(), connectionDefinition);
        }
        //ManagedConnectionFactory setup
        for (int i = 0; i < geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; i++) {
            GerConnectionDefinitionType geronimoConnectionDefinition = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray(i);
            assert geronimoConnectionDefinition != null: "Null GeronimoConnectionDefinition";
            String connectionFactoryInterfaceName = geronimoConnectionDefinition.getConnectionfactoryInterface().getStringValue();
            ConnectionDefinitionType connectionDefinition = (ConnectionDefinitionType) connectionDefinitions.get(connectionFactoryInterfaceName);
            if (connectionDefinition == null) {
                throw new DeploymentException("No connection definition for ConnectionFactory class: " + connectionFactoryInterfaceName);
            }
            for (int j = 0; j < geronimoConnectionDefinition.getConnectiondefinitionInstanceArray().length; j++) {
                GerConnectiondefinitionInstanceType connectionfactoryInstance = geronimoConnectionDefinition.getConnectiondefinitionInstanceArray()[j];

                //ConnectionManager
                ObjectName connectionManagerObjectName = configureConnectionManager(connectionfactoryInstance, context);

                //ManagedConnectionFactory

                ObjectName managedConnectionFactoryObjectName = null;
                try {
                    managedConnectionFactoryObjectName = ObjectName.getInstance(JMXReferenceFactory.BASE_MANAGED_CONNECTION_FACTORY_NAME + connectionfactoryInstance.getName());
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct ManagedConnectionFactory object name", e);
                }
                GBeanInfoFactory managedConnectionFactoryInfoFactory = new GBeanInfoFactory(ManagedConnectionFactoryWrapper.class.getName(), ManagedConnectionFactoryWrapper.getGBeanInfo());
                GBeanMBean managedConnectionFactoryGBean = setUpDynamicGBean(managedConnectionFactoryInfoFactory, connectionDefinition.getConfigPropertyArray(), connectionfactoryInstance.getConfigPropertySettingArray());
                try {
                    managedConnectionFactoryGBean.setAttribute("ManagedConnectionFactoryClass", cl.loadClass(connectionDefinition.getManagedconnectionfactoryClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionFactoryInterface", cl.loadClass(connectionDefinition.getConnectionfactoryInterface().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionFactoryImplClass", cl.loadClass(connectionDefinition.getConnectionfactoryImplClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionInterface", cl.loadClass(connectionDefinition.getConnectionInterface().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionImplClass", cl.loadClass(connectionDefinition.getConnectionImplClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("GlobalJNDIName", connectionfactoryInstance.getGlobalJndiName());
                    if (resourceAdapterClassName != null) {
                        managedConnectionFactoryGBean.setReferencePatterns("ResourceAdapterWrapper", Collections.singleton(resourceAdapterObjectName));
                    }
                    managedConnectionFactoryGBean.setReferencePatterns("ConnectionManagerFactory", Collections.singleton(connectionManagerObjectName));
                    if (connectionfactoryInstance.getCredentialInterface() != null && PasswordCredential.class.getName().equals(connectionfactoryInstance.getCredentialInterface().getStringValue())) {
                        GBeanMBean realmGBean = new GBeanMBean(PasswordCredentialRealm.getGBeanInfo());
                        realmGBean.setAttribute("RealmName", BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + connectionfactoryInstance.getName());
                        context.addGBean(ObjectName.getInstance(BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + connectionfactoryInstance.getName()), realmGBean);
                        managedConnectionFactoryGBean.setReferencePatterns("ManagedConnectionFactoryListener", Collections.singleton(ObjectName.getInstance(BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + connectionfactoryInstance.getName())));
                    }
                    managedConnectionFactoryGBean.setReferencePatterns("Kernel", Collections.singleton(Kernel.KERNEL));
                    managedConnectionFactoryGBean.setAttribute("SelfName", managedConnectionFactoryObjectName);
                } catch (Exception e) {
                    throw new DeploymentException(e);
                }
                context.addGBean(managedConnectionFactoryObjectName, managedConnectionFactoryGBean);
            }

        }
        //admin objects
        Map adminObjectInterfaceMap = new HashMap();
        for (int i = 0; i < resourceadapter.getAdminobjectArray().length; i++) {
            AdminobjectType adminobject = resourceadapter.getAdminobjectArray()[i];
            adminObjectInterfaceMap.put(adminobject.getAdminobjectInterface().getStringValue(), adminobject);
        }
        for (int i = 0; i < geronimoResourceAdapter.getAdminobjectArray().length; i++) {
            GerAdminobjectType gerAdminObject = geronimoResourceAdapter.getAdminobjectArray()[i];
            AdminobjectType adminobject = (AdminobjectType) adminObjectInterfaceMap.get(gerAdminObject.getAdminobjectInterface().getStringValue());
            assert adminobject != null;
            for (int j = 0; j < gerAdminObject.getAdminobjectInstanceArray().length; j++) {
                GerAdminobjectInstanceType gerAdminobjectInstance = gerAdminObject.getAdminobjectInstanceArray()[j];
                GBeanInfoFactory adminObjectInfoFactory = new GBeanInfoFactory(AdminObjectWrapper.class.getName(), AdminObjectWrapper.getGBeanInfo());
                GBeanMBean adminObjectGBean = setUpDynamicGBean(adminObjectInfoFactory, adminobject.getConfigPropertyArray(), gerAdminobjectInstance.getConfigPropertySettingArray());
                try {
                    ObjectName adminObjectObjectName = ObjectName.getInstance(JMXReferenceFactory.BASE_ADMIN_OBJECT_NAME + gerAdminobjectInstance.getAdminobjectName());
                    adminObjectGBean.setAttribute("AdminObjectInterface", cl.loadClass(adminobject.getAdminobjectInterface().getStringValue()));
                    adminObjectGBean.setAttribute("AdminObjectClass", cl.loadClass(adminobject.getAdminobjectClass().getStringValue()));
                    adminObjectGBean.setReferencePatterns("Kernel", Collections.singleton(Kernel.KERNEL));
                    adminObjectGBean.setAttribute("SelfName", adminObjectObjectName);
                    context.addGBean(adminObjectObjectName, adminObjectGBean);
                } catch (Exception e) {
                    throw new DeploymentException("Could not construct AdminObject", e);
                }
            }
        }

    }


    private GBeanMBean setUpDynamicGBean(GBeanInfoFactory infoFactory, ConfigPropertyType[] configProperties, GerConfigPropertySettingType[] configPropertySettings) throws DeploymentException {
        addDynamicAttributes(infoFactory, configProperties);
        GBeanInfo gbeanInfo = infoFactory.getBeanInfo();
        GBeanMBean gbean;
        try {
            gbean = new GBeanMBean(gbeanInfo);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to create GMBean", e);
        }
        try {
            setDynamicAttributes(gbean, configProperties, configPropertySettings);
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return gbean;
    }

//ManagedConnectionFactories are extremely restricted as to the attribute types.
    private void setDynamicAttributes(GBeanMBean gBean, ConfigPropertyType[] configProperties, GerConfigPropertySettingType[] configPropertySettings) throws DeploymentException, ReflectionException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException {
        for (int i = 0; i < configProperties.length; i++) {
            ConfigPropertyType configProperty = configProperties[i];
            if (configProperty.getConfigPropertyValue() == null) {
                continue;
            }
            Object value;
            try {
                PropertyEditor editor = PropertyEditors.findEditor(configProperty.getConfigPropertyType().getStringValue());
                String valueString = null;
                if (editor != null) {
//look for explicit value setting
                    for (int j = 0; j < configPropertySettings.length; j++) {
                        GerConfigPropertySettingType configPropertySetting = configPropertySettings[j];
                        if (configPropertySetting.getName().equals(configProperty.getConfigPropertyName().getStringValue())) {
                            valueString = configPropertySetting.getStringValue();
                            break;
                        }
                    }
//look for default value
                    if (valueString == null) {
                        if (configProperty.getConfigPropertyValue() != null) {
                            valueString = configProperty.getConfigPropertyValue().getStringValue();
                        }
                    }
                    if (valueString != null) {
                        editor.setAsText(valueString);
                        value = editor.getValue();
                        gBean.setAttribute(configProperty.getConfigPropertyName().getStringValue(), value);
                    }
                } else {
                    throw new DeploymentException("No property editor for type: " + configProperty.getConfigPropertyType());
                }
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load attribute class: attribute: " + configProperty.getConfigPropertyName() + ", type: " + configProperty.getConfigPropertyType(), e);
            }

        }
    }

    void addDynamicAttributes(GBeanInfoFactory infoFactory, ConfigPropertyType[] configProperties) {
        for (int i = 0; i < configProperties.length; i++) {
            ConfigPropertyType configProperty = configProperties[i];
            infoFactory.addAttribute(new DynamicGAttributeInfo(configProperty.getConfigPropertyName().getStringValue(), true));
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Geronimo RAR 1.5 Configuration Builder", RAR_1_5ConfigBuilder.class.getName(), AbstractRARConfigBuilder.GBEAN_INFO);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return RAR_1_5ConfigBuilder.GBEAN_INFO;
    }
}

