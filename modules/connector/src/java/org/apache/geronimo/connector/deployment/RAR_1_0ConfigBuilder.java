/**
 *
 * Copyright 2004 The Apache Software Foundation
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
import java.util.jar.JarInputStream;

import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.outbound.ConnectionManagerDeployment;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
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
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.geronimo.GerConnectorDocument;
import org.apache.geronimo.xbeans.geronimo.GerConnectorType;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerVersionType;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ConfigPropertyType;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ConnectorDocument;
import org.apache.geronimo.xbeans.j2ee.connector_1_0.ResourceadapterType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:09 $
 *
 * */
public class RAR_1_0ConfigBuilder extends AbstractRARConfigBuilder {

    public RAR_1_0ConfigBuilder(Kernel kernel, Repository repository, ObjectName connectionTrackerNamePattern) {
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
        return plan.schemaType() == TYPE && GerVersionType.X_1_0.equals(((GerConnectorDocument) plan).getConnector().getVersion());
    }

    void addConnectorGBeans(DeploymentContext context, XmlObject genericConnectorDocument, GerConnectorType geronimoConnector, ClassLoader cl) throws DeploymentException {
        ResourceadapterType resourceAdapter = ((ConnectorDocument) genericConnectorDocument).getConnector().getResourceadapter();
        GerResourceadapterType geronimoResourceAdapter = geronimoConnector.getResourceadapter();
        for (int i = 0; i < geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; i++) {
            GerConnectionDefinitionType geronimoConnectionDefinition = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray(i);
            assert geronimoConnectionDefinition != null: "Null GeronimoConnectionDefinition";
            //ConnectionManagerFactory
            for (int j = 0; j < geronimoConnectionDefinition.getConnectiondefinitionInstanceArray().length; j++) {
                GerConnectiondefinitionInstanceType gerConnectionfactoryInstance = geronimoConnectionDefinition.getConnectiondefinitionInstanceArray()[j];

                GerConnectionmanagerType connectionManagerFactory = gerConnectionfactoryInstance.getConnectionmanager();
                GBeanInfo connectionManagerFactoryGBeanInfo;
                try {
                    connectionManagerFactoryGBeanInfo = GBeanInfo.getGBeanInfo(ConnectionManagerDeployment.class.getName(), cl);
                } catch (InvalidConfigurationException e) {
                    throw new DeploymentException("Unable to get GBeanInfo from ConnectionManagerDeployment", e);
                }

                GBeanMBean connectionManagerFactoryGBean;
                try {
                    connectionManagerFactoryGBean = new GBeanMBean(connectionManagerFactoryGBeanInfo, cl);
                } catch (InvalidConfigurationException e) {
                    throw new DeploymentException("Unable to create GMBean", e);
                }
                try {
                    connectionManagerFactoryGBean.setAttribute("Name", gerConnectionfactoryInstance.getName());
                    connectionManagerFactoryGBean.setAttribute("BlockingTimeout", new Integer(connectionManagerFactory.getBlockingTimeout().intValue()));
                    connectionManagerFactoryGBean.setAttribute("MaxSize", new Integer(connectionManagerFactory.getMaxSize().intValue()));
                    connectionManagerFactoryGBean.setAttribute("UseTransactions", Boolean.valueOf(connectionManagerFactory.getUseTransactions()));
                    connectionManagerFactoryGBean.setAttribute("UseLocalTransactions", Boolean.valueOf(connectionManagerFactory.getUseLocalTransactions()));
                    connectionManagerFactoryGBean.setAttribute("UseTransactionCaching", Boolean.valueOf(connectionManagerFactory.getUseTransactionCaching()));
                    connectionManagerFactoryGBean.setAttribute("UseConnectionRequestInfo", Boolean.valueOf(connectionManagerFactory.getUseConnectionRequestInfo()));
                    connectionManagerFactoryGBean.setAttribute("UseSubject", Boolean.valueOf(connectionManagerFactory.getUseSubject()));
                    connectionManagerFactoryGBean.setReferencePatterns("Kernel", Collections.singleton(Kernel.KERNEL));
                    connectionManagerFactoryGBean.setReferencePatterns("ConnectionTracker", Collections.singleton(connectionTrackerNamePattern));
                    if (connectionManagerFactory.getRealmBridge() != null) {
                        connectionManagerFactoryGBean.setReferencePatterns("RealmBridge", Collections.singleton(ObjectName.getInstance(BASE_REALM_BRIDGE_NAME + connectionManagerFactory.getRealmBridge())));
                    }
                } catch (Exception e) {
                    throw new DeploymentException("Problem setting up ConnectionManagerFactory", e);
                }
                ObjectName connectionManagerFactoryObjectName = null;
                try {
                    connectionManagerFactoryObjectName = ObjectName.getInstance(BASE_CONNECTION_MANAGER_FACTORY_NAME + gerConnectionfactoryInstance.getName());
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not name ConnectionManagerFactory", e);
                }
                context.addGBean(connectionManagerFactoryObjectName, connectionManagerFactoryGBean);
                //ManagedConnectionFactory
                GBeanInfoFactory managedConnectionFactoryInfoFactory = new GBeanInfoFactory(ManagedConnectionFactoryWrapper.class.getName(), ManagedConnectionFactoryWrapper.getGBeanInfo());
                GBeanMBean managedConnectionFactoryGBean = setUpDynamicGBean(managedConnectionFactoryInfoFactory, resourceAdapter.getConfigPropertyArray(), gerConnectionfactoryInstance.getConfigPropertySettingArray());
                try {
                    managedConnectionFactoryGBean.setAttribute("ManagedConnectionFactoryClass", cl.loadClass(resourceAdapter.getManagedconnectionfactoryClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionFactoryInterface", cl.loadClass(resourceAdapter.getConnectionfactoryInterface().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionFactoryImplClass", cl.loadClass(resourceAdapter.getConnectionfactoryImplClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionInterface", cl.loadClass(resourceAdapter.getConnectionInterface().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionImplClass", cl.loadClass(resourceAdapter.getConnectionImplClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("GlobalJNDIName", gerConnectionfactoryInstance.getGlobalJndiName());
                    managedConnectionFactoryGBean.setReferencePatterns("ConnectionManagerFactory", Collections.singleton(connectionManagerFactoryObjectName));
                    //TODO also set up the login module
                    /*
                    if (geronimoConnectionDefinition.getAuthentication().equals("BasicUserPassword")) {
                        managedConnectionFactoryGBean.setReferencePatterns("ManagedConnectionFactoryListener", Collections.singleton(ObjectName.getInstance(BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + geronimoConnectionDefinition.getName())));
                    }
                    */
                } catch (Exception e) {
                    throw new DeploymentException(e);
                }
                ObjectName managedConnectionFactoryObjectName = null;
                try {
                    managedConnectionFactoryObjectName = ObjectName.getInstance(BASE_MANAGED_CONNECTION_FACTORY_NAME + gerConnectionfactoryInstance.getName());
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("Could not construct ManagedConnectionFactory object name", e);
                }
                context.addGBean(managedConnectionFactoryObjectName, managedConnectionFactoryGBean);

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
            if (configProperty.getConfigPropertyType() == null) {
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
                    throw new DeploymentException("No property editor for type: " + configProperty.getConfigPropertyType().getStringValue());
                }
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load attribute class: attribute: " + configProperty.getConfigPropertyName().getStringValue() + ", type: " + configProperty.getConfigPropertyType().getStringValue(), e);
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
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Geronimo RAR 1.0 Configuration Builder", RAR_1_0ConfigBuilder.class.getName(), AbstractRARConfigBuilder.GBEAN_INFO);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return RAR_1_0ConfigBuilder.GBEAN_INFO;
    }
}
