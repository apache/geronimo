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
import java.util.jar.JarInputStream;
import javax.management.AttributeNotFoundException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.resource.spi.security.PasswordCredential;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
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
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
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
 * @version $Revision: 1.11 $ $Date: 2004/06/05 01:40:09 $
 */
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
        //addGBeans called from here so it is included in tests.
        addGBeans(geronimoConnector, cl, context);

        ResourceadapterType resourceAdapter = ((ConnectorDocument) genericConnectorDocument).getConnector().getResourceadapter();
        GerResourceadapterType geronimoResourceAdapter = geronimoConnector.getResourceadapter();
        for (int i = 0; i < geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray().length; i++) {
            GerConnectionDefinitionType geronimoConnectionDefinition = geronimoResourceAdapter.getOutboundResourceadapter().getConnectionDefinitionArray(i);
            assert geronimoConnectionDefinition != null: "Null GeronimoConnectionDefinition";

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
                GBeanInfoFactory managedConnectionFactoryInfoFactory = new GBeanInfoFactory(ManagedConnectionFactoryWrapper.class, ManagedConnectionFactoryWrapper.getGBeanInfo());
                GBeanMBean managedConnectionFactoryGBean = setUpDynamicGBean(managedConnectionFactoryInfoFactory, resourceAdapter.getConfigPropertyArray(), connectionfactoryInstance.getConfigPropertySettingArray(), cl);
                try {
                    managedConnectionFactoryGBean.setAttribute("ManagedConnectionFactoryClass", cl.loadClass(resourceAdapter.getManagedconnectionfactoryClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionFactoryInterface", cl.loadClass(resourceAdapter.getConnectionfactoryInterface().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionFactoryImplClass", cl.loadClass(resourceAdapter.getConnectionfactoryImplClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionInterface", cl.loadClass(resourceAdapter.getConnectionInterface().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("ConnectionImplClass", cl.loadClass(resourceAdapter.getConnectionImplClass().getStringValue()));
                    managedConnectionFactoryGBean.setAttribute("GlobalJNDIName", connectionfactoryInstance.getGlobalJndiName());
                    managedConnectionFactoryGBean.setReferencePatterns("ConnectionManagerFactory", Collections.singleton(connectionManagerObjectName));
                    if (connectionfactoryInstance.getCredentialInterface() != null && PasswordCredential.class.getName().equals(connectionfactoryInstance.getCredentialInterface().getStringValue())) {
                        GBeanMBean realmGBean = new GBeanMBean(PasswordCredentialRealm.class.getName());
                        realmGBean.setAttribute("RealmName", BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + connectionfactoryInstance.getName());
                        context.addGBean(ObjectName.getInstance(BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + connectionfactoryInstance.getName()), realmGBean);
                        managedConnectionFactoryGBean.setReferencePatterns("ManagedConnectionFactoryListener", Collections.singleton(ObjectName.getInstance(BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + connectionfactoryInstance.getName())));
                    }
                    managedConnectionFactoryGBean.setAttribute("SelfName", managedConnectionFactoryObjectName);
                } catch (Exception e) {
                    throw new DeploymentException(e);
                }
                context.addGBean(managedConnectionFactoryObjectName, managedConnectionFactoryGBean);

            }
        }

    }


    private GBeanMBean setUpDynamicGBean(GBeanInfoFactory infoFactory, ConfigPropertyType[] configProperties, GerConfigPropertySettingType[] configPropertySettings, ClassLoader cl) throws DeploymentException {
        addDynamicAttributes(infoFactory, configProperties);
        GBeanInfo gbeanInfo = infoFactory.getBeanInfo();
        GBeanMBean gbean;
        try {
            gbean = new GBeanMBean(gbeanInfo, cl);
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
    private void setDynamicAttributes(GBeanMBean gBean, ConfigPropertyType[] configProperties, GerConfigPropertySettingType[] configPropertySettings) throws DeploymentException, ReflectionException, AttributeNotFoundException {
        for (int i = 0; i < configProperties.length; i++) {
            ConfigPropertyType configProperty = configProperties[i];
            Object value;
            try {
                PropertyEditor editor = PropertyEditors.findEditor(configProperty.getConfigPropertyType().getStringValue(), gBean.getClassLoader());
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

    private void addDynamicAttributes(GBeanInfoFactory infoFactory, ConfigPropertyType[] configProperties) {
        for (int i = 0; i < configProperties.length; i++) {
            ConfigPropertyType configProperty = configProperties[i];
            infoFactory.addAttribute(new DynamicGAttributeInfo(configProperty.getConfigPropertyName().getStringValue(), true));
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("Geronimo RAR 1.0 Configuration Builder", RAR_1_0ConfigBuilder.class, AbstractRARConfigBuilder.GBEAN_INFO);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return RAR_1_0ConfigBuilder.GBEAN_INFO;
    }
}
