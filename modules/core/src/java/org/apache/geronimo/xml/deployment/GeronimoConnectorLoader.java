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
package org.apache.geronimo.xml.deployment;

import org.apache.geronimo.deployment.model.connector.AdminObject;
import org.apache.geronimo.deployment.model.connector.ConfigProperty;
import org.apache.geronimo.deployment.model.connector.ConnectionDefinition;
import org.apache.geronimo.deployment.model.connector.Connector;
import org.apache.geronimo.deployment.model.connector.ConnectorDocument;
import org.apache.geronimo.deployment.model.connector.InboundResourceAdapter;
import org.apache.geronimo.deployment.model.connector.MessageAdapter;
import org.apache.geronimo.deployment.model.connector.MessageListener;
import org.apache.geronimo.deployment.model.connector.OutboundResourceAdapter;
import org.apache.geronimo.deployment.model.connector.ResourceAdapter;
import org.apache.geronimo.deployment.model.geronimo.connector.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Knows how to load a set of POJOs from a DOM representing a ra.xml
 * deployment descriptor.
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/21 22:21:26 $
 */
public class GeronimoConnectorLoader {

    private GeronimoConnectorLoader() {
    }

    public static GeronimoConnectorDocument load(Document doc, ConnectorDocument connectorDocument) {
        Element root = doc.getDocumentElement();
        if (!"connector".equals(root.getTagName())) {
            throw new IllegalArgumentException("Document is not a ra instance");
        }

        Connector connector = connectorDocument.getConnector();
        GeronimoConnector geronimoConnector = new GeronimoConnector(connector);
        geronimoConnector.setResourceAdapter(loadResourceAdapter(root, connector));
        GeronimoConnectorDocument result = new GeronimoConnectorDocument();
        result.setConnector(geronimoConnector);
        return result;
    }


    private static GeronimoResourceAdapter loadResourceAdapter(Element econ, Connector connector) {
        Element era = LoaderUtil.getChild(econ, "resourceadapter");
        if (null == era) {
            throw new IllegalArgumentException("No resourceadapter element");
        }
        ResourceAdapter resourceAdapter = connector.getResourceAdapter();
        GeronimoResourceAdapter ra = new GeronimoResourceAdapter(resourceAdapter);
        ra.setName(LoaderUtil.getChildContent(era, "name"));
        ConfigProperty[] configProperty = ra.getConfigProperty();
        loadConfigSettings(era, configProperty);
        ra.setBootstrapContext(LoaderUtil.getChildContent(era, "bootstrapcontext-name"));
        ra.setOutboundResourceAdapter(loadOutboundResourceadapter(era, resourceAdapter.getOutboundResourceAdapter()));
        ra.setInboundResourceAdapter(loadInboundResourceadapter(era, resourceAdapter.getInboundResourceAdapter()));
        ra.setAdminObject(loadAdminobject(era, resourceAdapter.getAdminObject()));

        return ra;
    }

    private static void loadConfigSettings(Element era, ConfigProperty[] configProperty) {
        Element[] roots = LoaderUtil.getChildren(era, "config-property-setting");
        for (int i = 0; i < roots.length; i++)
            outer:
        {
                Element root = roots[i];
                String name = root.getAttribute("name");
                for (int j = 0; j < configProperty.length; j++) {
                    if (configProperty[j].getConfigPropertyName().equals(name)) {
                        configProperty[j].setConfigPropertyValue(LoaderUtil.getContent(root));
                        break outer;
                    }
                }
                throw new IllegalArgumentException("No such property as " + name);
            }
    }

    private static ConfigProperty[] loadConfigPropertySettings(Element era) {
        Element[] roots = LoaderUtil.getChildren(era, "config-property-setting");
        ConfigProperty[] configProperties = new ConfigProperty[roots.length];
        for (int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            configProperties[i] = new ConfigProperty();
            configProperties[i].setConfigPropertyName(root.getAttribute("name"));
            configProperties[i].setConfigPropertyType(root.getAttribute("type"));
            configProperties[i].setConfigPropertyValue(LoaderUtil.getContent(root));
        }
        return configProperties;
    }

    private static GeronimoOutboundResourceAdapter loadOutboundResourceadapter(Element era, OutboundResourceAdapter outboundResourceAdapter) {
        if (outboundResourceAdapter == null) {
            return null;
        }
        Element root = LoaderUtil.getChild(era, "outbound-resourceadapter");
        GeronimoOutboundResourceAdapter ora = new GeronimoOutboundResourceAdapter(outboundResourceAdapter);
        ora.setConnectionDefinition(loadConnectionDefinition(root, outboundResourceAdapter.getConnectionDefinition()));
        return ora;
    }

    private static GeronimoConnectionDefinition[] loadConnectionDefinition(Element ecd, ConnectionDefinition[] connectionDefinition) {
        Element[] roots = LoaderUtil.getChildren(ecd, "connection-definition");
        GeronimoConnectionDefinition[] conDefinition = new GeronimoConnectionDefinition[roots.length];
        for (int i = 0; i < roots.length; i++)
            loaded:
        {
                Element root = roots[i];
                String connectionFactoryInterface = LoaderUtil.getChildContent(root, "connectionfactory-interface");
                for (int j = 0; j < connectionDefinition.length; j++) {
                    if (connectionFactoryInterface.equals(connectionDefinition[j].getConnectionFactoryInterface())) {
                        conDefinition[i] = new GeronimoConnectionDefinition(connectionDefinition[j]);
                        ConfigProperty[] configProperty = conDefinition[i].getConfigProperty();
                        loadConfigSettings(root, configProperty);
                        conDefinition[i].setName(LoaderUtil.getChildContent(root, "name"));
                        conDefinition[i].setGlobalJndiName(LoaderUtil.getChildContent(root, "global-jndi-name"));
                        GeronimoConnectionManagerFactory connectionManagerFactory = new GeronimoConnectionManagerFactory();
                        Element ecmf = LoaderUtil.getChild(root, "connectionmanager-factory");
                        connectionManagerFactory.setConnectionManagerFactoryClass(LoaderUtil.getChildContent(ecmf, "connectionmanagerfactory-class"));
                        connectionManagerFactory.setRealmBridge(LoaderUtil.getChildContent(ecmf, "realm-bridge"));
                        connectionManagerFactory.setConfigProperty(loadConfigPropertySettings(ecmf));
                        conDefinition[i].setGeronimoConnectionManagerFactory(connectionManagerFactory);
                        break loaded;
                    }
                }
                throw new IllegalArgumentException("No such connectionfactory-interface as " + connectionFactoryInterface);
            }
        return conDefinition;
    }


    private static GeronimoInboundResourceAdapter loadInboundResourceadapter(Element era, InboundResourceAdapter inboundResourceAdapter) {
        if (inboundResourceAdapter == null) {
            return null;
        }
        Element root = LoaderUtil.getChild(era, "inbound-resourceadapter");
        GeronimoInboundResourceAdapter ira = new GeronimoInboundResourceAdapter();
        ira.setMessageAdapter(loadMessageAdapter(root, inboundResourceAdapter.getMessageAdapter()));
        return ira;
    }

    private static GeronimoMessageAdapter loadMessageAdapter(Element eira, MessageAdapter messageAdapter) {
        GeronimoMessageAdapter ma = null;
        Element root = LoaderUtil.getChild(eira, "messageadapter");
        if (null != root && null != messageAdapter) {
            ma = new GeronimoMessageAdapter();
            ma.setMessageListener(loadMessagelistener(root, messageAdapter.getMessageListener()));
        }
        return ma;
    }

    private static GeronimoMessageListener[] loadMessagelistener(Element ema, MessageListener[] messageListenerType) {
        Element[] roots = LoaderUtil.getChildren(ema, "messagelistener");
        GeronimoMessageListener[] messageListener = new GeronimoMessageListener[roots.length];
        for (int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            String messageListenerTypeName = LoaderUtil.getChildContent(root, "messagelistener-type");
            for (int j = 0; j < messageListenerType.length; j++) {
                if (messageListenerType[j].getMessageListenerType().equals(messageListenerTypeName)) {
                    messageListener[i] = new GeronimoMessageListener(messageListenerType[j]);
                    messageListener[i].setMessageEndpointFactoryName(LoaderUtil.getChildContent(root, "message-endpoint-factory"));
                    break;
                }
            }
        }
        return messageListener;
    }


    private static AdminObject[] loadAdminobject(Element era, AdminObject[] adminObjectType) {
        Element[] roots = LoaderUtil.getChildren(era, "adminobject");
        AdminObject[] adminObject = new AdminObject[roots.length];
        for (int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            adminObject[i] = new AdminObject();
            adminObject[i].setAdminObjectInterface(LoaderUtil.getChildContent(root, "adminobject-interface"));
            adminObject[i].setAdminObjectClass(LoaderUtil.getChildContent(root, "adminobject-class"));
            for (int j = 0; j < adminObjectType.length; j++) {
                if (adminObjectType[j].getAdminObjectInterface().equals(adminObject[i].getAdminObjectInterface())
                        && adminObjectType[j].getAdminObjectClass().equals(adminObject[i].getAdminObjectClass())) {
                    adminObject[i].setConfigProperty(
                            GeronimoResourceAdapter.copyConfigProperties(adminObjectType[j].getConfigProperty()));
                    loadConfigSettings(root, adminObject[i].getConfigProperty());
                    break;
                }
            }
        }
        return adminObject;
    }

}
