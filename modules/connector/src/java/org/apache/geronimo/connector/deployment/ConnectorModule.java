/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.connector.deployment;

import java.beans.PropertyEditor;
import java.net.URI;
import java.util.Collections;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.geronimo.common.propertyeditor.PropertyEditors;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.deployment.ConfigurationCallback;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.deployment.DeploymentModule;
import org.apache.geronimo.deployment.model.connector.ConfigProperty;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoConnectionDefinition;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoConnectionManagerFactory;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoConnectorDocument;
import org.apache.geronimo.deployment.model.geronimo.connector.GeronimoResourceAdapter;
import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/25 21:07:03 $
 *
 * */
public class ConnectorModule implements DeploymentModule {

    public final static String BASE_RESOURCE_ADAPTER_NAME = "geronimo.management:J2eeType=ResourceAdapter,name=";
    private final static String BASE_CONNECTION_MANAGER_FACTORY_NAME = "geronimo.management:J2eeType=ConnectionManager,name=";
    private static final String BASE_MANAGED_CONNECTION_FACTORY_NAME = "geronimo.management:J2eeType=ManagedConnectionFactory,name=";
    private static final String BASE_REALM_BRIDGE_NAME = "geronimo.security:service=RealmBridge,name=";
    private static final String BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME = "geronimo.security:service=Realm,type=PasswordCredential,name=";

    private URI moduleID;
    private GeronimoConnectorDocument geronimoConnectorDocument;
    private ConnectorDeployer connectorDeployer;

    public ConnectorModule(URI moduleID, GeronimoConnectorDocument geronimoConnectorDocument, ConnectorDeployer connectorDeployer) {
        this.moduleID = moduleID;
        this.geronimoConnectorDocument = geronimoConnectorDocument;
        this.connectorDeployer = connectorDeployer;
    }

    public void init() throws DeploymentException {
    }

    public void generateClassPath(ConfigurationCallback callback) throws DeploymentException {
        //I have no idea
    }

    public void defineGBeans(ConfigurationCallback callback, ClassLoader cl) throws DeploymentException {
        GeronimoResourceAdapter geronimoResourceAdapter = geronimoConnectorDocument.getGeronimoConnector().getGeronimoResourceAdapter();
        //ResourceAdapter setup
        String resourceAdapterClassName = geronimoResourceAdapter.getResourceAdapterClass();
        ObjectName resourceAdapterObjectName = null;
        if (resourceAdapterClassName != null) {
            GBeanInfoFactory resourceAdapterInfoFactory = new GBeanInfoFactory(ResourceAdapterWrapper.class.getName(), ResourceAdapterWrapper.getGBeanInfo());
            GBeanMBean resourceAdapterGBean = setUpDynamicGBean(resourceAdapterInfoFactory, geronimoResourceAdapter.getConfigProperty());
            try {
                resourceAdapterGBean.setAttribute("ResourceAdapterClass", cl.loadClass(geronimoResourceAdapter.getResourceAdapterClass()));
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
            try {
                resourceAdapterObjectName = ObjectName.getInstance(BASE_RESOURCE_ADAPTER_NAME + moduleID);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Could not construct resource adapter object name", e);
            }
            callback.addGBean(resourceAdapterObjectName, resourceAdapterGBean);
        }
        //ManagedConnectionFactory setup
        for (int i = 0; i < geronimoResourceAdapter.getGeronimoOutboundResourceAdapter().getGeronimoConnectionDefinition().length; i++) {
            GeronimoConnectionDefinition geronimoConnectionDefinition = geronimoResourceAdapter.getGeronimoOutboundResourceAdapter().getGeronimoConnectionDefinition(i);
            assert geronimoConnectionDefinition != null: "Null GeronimoConnectionDefinition";
            //ConnectionManagerFactory
            GeronimoConnectionManagerFactory geronimoConnectionManagerFactory = geronimoConnectionDefinition.getGeronimoConnectionManagerFactory();
            String connectionManagerFactoryClassName = geronimoConnectionManagerFactory.getConnectionManagerFactoryClass();
            GBeanInfo connectionManagerFactoryGBeanInfo;
            try {
                connectionManagerFactoryGBeanInfo = GBeanInfo.getGBeanInfo(connectionManagerFactoryClassName, cl);
            } catch (InvalidConfigurationException e) {
                throw new DeploymentException("Unable to get GBeanInfo from class " + connectionManagerFactoryClassName, e);
            }

            GBeanMBean connectionManagerFactoryGBean;
            try {
                connectionManagerFactoryGBean = new GBeanMBean(connectionManagerFactoryGBeanInfo, cl);
            } catch (InvalidConfigurationException e) {
                throw new DeploymentException("Unable to create GMBean", e);
            }
            try {
                connectionManagerFactoryGBean.setReferencePatterns("Kernel", Collections.singleton(Kernel.KERNEL));
                connectionManagerFactoryGBean.setReferencePatterns("ConnectionTracker", Collections.singleton(connectorDeployer.getConnectionTrackerNamePattern()));
                if (geronimoConnectionManagerFactory.getRealmBridge() != null) {
                    connectionManagerFactoryGBean.setReferencePatterns("RealmBridge", Collections.singleton(ObjectName.getInstance(BASE_REALM_BRIDGE_NAME + geronimoConnectionManagerFactory.getRealmBridge())));
                }
                setDynamicAttributes(connectionManagerFactoryGBean, geronimoConnectionManagerFactory.getConfigProperty());
            } catch (DeploymentException e) {
                throw e;
            } catch (Exception e) {
                throw new DeploymentException("Problem setting up ConnectionManagerFactory", e);
            }
            ObjectName connectionManagerFactoryObjectName = null;
            try {
                connectionManagerFactoryObjectName = ObjectName.getInstance(BASE_CONNECTION_MANAGER_FACTORY_NAME + geronimoConnectionDefinition.getName());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Could not name ConnectionManagerFactory", e);
            }
            callback.addGBean(connectionManagerFactoryObjectName, connectionManagerFactoryGBean);
            //ManagedConnectionFactory

            GBeanInfoFactory managedConnectionFactoryInfoFactory = new GBeanInfoFactory(ManagedConnectionFactoryWrapper.class.getName(), ManagedConnectionFactoryWrapper.getGBeanInfo());
            GBeanMBean managedConnectionFactoryGBean = setUpDynamicGBean(managedConnectionFactoryInfoFactory, geronimoConnectionDefinition.getConfigProperty());
            try {
                managedConnectionFactoryGBean.setAttribute("ManagedConnectionFactoryClass", cl.loadClass(geronimoConnectionDefinition.getManagedConnectionFactoryClass()));
                managedConnectionFactoryGBean.setAttribute("ConnectionFactoryInterface", cl.loadClass(geronimoConnectionDefinition.getConnectionFactoryInterface()));
                managedConnectionFactoryGBean.setAttribute("ConnectionFactoryImplClass", cl.loadClass(geronimoConnectionDefinition.getConnectionFactoryImplClass()));
                managedConnectionFactoryGBean.setAttribute("ConnectionInterface", cl.loadClass(geronimoConnectionDefinition.getConnectionInterface()));
                managedConnectionFactoryGBean.setAttribute("ConnectionImplClass", cl.loadClass(geronimoConnectionDefinition.getConnectionImplClass()));
                managedConnectionFactoryGBean.setAttribute("GlobalJNDIName", geronimoConnectionDefinition.getGlobalJndiName());
                if (resourceAdapterClassName != null) {
                    managedConnectionFactoryGBean.setReferencePatterns("ResourceAdapterWrapper", Collections.singleton(resourceAdapterObjectName));
                }
                managedConnectionFactoryGBean.setReferencePatterns("ConnectionManagerFactory", Collections.singleton(connectionManagerFactoryObjectName));
                /*
                //TODO also set up the login module
                if (geronimoConnectionDefinition.getAuthentication().equals("BasicUserPassword")) {
                    managedConnectionFactoryGBean.setReferencePatterns("ManagedConnectionFactoryListener", Collections.singleton(ObjectName.getInstance(BASE_PASSWORD_CREDENTIAL_LOGIN_MODULE_NAME + geronimoConnectionDefinition.getName())));
                }
                */
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
            ObjectName managedConnectionFactoryObjectName = null;
            try {
                managedConnectionFactoryObjectName = ObjectName.getInstance(BASE_MANAGED_CONNECTION_FACTORY_NAME + geronimoConnectionDefinition.getName());
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Could not construct ManagedConnectionFactory object name", e);
            }
            callback.addGBean(managedConnectionFactoryObjectName, managedConnectionFactoryGBean);

        }

    }

    private GBeanMBean setUpDynamicGBean(GBeanInfoFactory infoFactory, ConfigProperty[] configProperties) throws DeploymentException {
        addDynamicAttributes(infoFactory, configProperties);
        GBeanInfo gbeanInfo = infoFactory.getBeanInfo();
        GBeanMBean gbean;
        try {
            gbean = new GBeanMBean(gbeanInfo);
        } catch (InvalidConfigurationException e) {
            throw new DeploymentException("Unable to create GMBean", e);
        }
        try {
            setDynamicAttributes(gbean, configProperties);
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return gbean;
    }

    //ManagedConnectionFactories are extremely restricted as to the attribute types.
    private void setDynamicAttributes(GBeanMBean gBean, ConfigProperty[] configProperties) throws DeploymentException, ReflectionException, MBeanException, InvalidAttributeValueException, AttributeNotFoundException {
        for (int i = 0; i < configProperties.length; i++) {
            ConfigProperty configProperty = configProperties[i];
            Object value;
            try {
                PropertyEditor editor = PropertyEditors.findEditor(configProperty.getConfigPropertyType());
                if (editor != null) {
                    editor.setAsText(configProperty.getConfigPropertyValue());
                    value = editor.getValue();
                } else {
                    throw new DeploymentException("No property editor for type: " + configProperty.getConfigPropertyType());
                }
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load attribute class: attribute: " + configProperty.getConfigPropertyName() + ", type: " + configProperty.getConfigPropertyType(), e);
            }

            gBean.setAttribute(configProperty.getConfigPropertyName(), value);
        }
    }

    void addDynamicAttributes(GBeanInfoFactory infoFactory, ConfigProperty[] configProperties) {
        for (int i = 0; i < configProperties.length; i++) {
            ConfigProperty configProperty = configProperties[i];
            infoFactory.addAttribute(new DynamicGAttributeInfo(configProperty.getConfigPropertyName(), true));
        }
    }

    public void complete() {
    }
}
