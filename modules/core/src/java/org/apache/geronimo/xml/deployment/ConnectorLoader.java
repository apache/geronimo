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

import org.apache.geronimo.deployment.model.connector.ActivationSpec;
import org.apache.geronimo.deployment.model.connector.AdminObject;
import org.apache.geronimo.deployment.model.connector.AuthenticationMechanism;
import org.apache.geronimo.deployment.model.connector.ConfigProperty;
import org.apache.geronimo.deployment.model.connector.ConnectionDefinition;
import org.apache.geronimo.deployment.model.connector.Connector;
import org.apache.geronimo.deployment.model.connector.ConnectorDocument;
import org.apache.geronimo.deployment.model.connector.InboundResourceAdapter;
import org.apache.geronimo.deployment.model.connector.MessageAdapter;
import org.apache.geronimo.deployment.model.connector.MessageListener;
import org.apache.geronimo.deployment.model.connector.OutboundResourceAdapter;
import org.apache.geronimo.deployment.model.connector.RequiredConfigProperty;
import org.apache.geronimo.deployment.model.connector.ResourceAdapater;
import org.apache.geronimo.deployment.model.connector.SecurityPermission;
import org.apache.geronimo.deployment.model.connector.License;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Knows how to load a set of POJOs from a DOM representing a ra.xml
 * deployment descriptor.
 * 
 * @version $Revision: 1.1 $ $Date: 2003/09/29 02:01:08 $
 */
public class ConnectorLoader {
    public static ConnectorDocument load(Document doc) {
        Element root = doc.getDocumentElement();
        if (!"connector".equals(root.getTagName())) {
            throw new IllegalArgumentException("Document is not a ra instance");
        }
        Connector connector = new Connector();
        J2EELoader.loadDisplayable(root, connector);
        connector.setVendorName(LoaderUtil.getChildContent(root, "vendor-name"));
        connector.setEisType(LoaderUtil.getChildContent(root, "eis-type"));
        connector.setResourceAdapterVersion(LoaderUtil.getChildContent(root, "resourceadapter-version"));
        connector.setLicense(loadLicense(LoaderUtil.getChild(root, "license")));
        connector.setResourceAdapter(loadResourceadapater(root));
        connector.setVersion(LoaderUtil.getAttribute(root, "version"));
        ConnectorDocument result = new ConnectorDocument();
        result.setConnector(connector);
        return result;
    }

    private static License loadLicense(Element root) {
        License license = new License();
        J2EELoader.loadDescribable(root, license);
        license.setLicenseRequired(LoaderUtil.getChildContent(root, "license-required"));
        return license;
    }

    private static ResourceAdapater loadResourceadapater(Element econ) {
        ResourceAdapater ra = null;
        Element era = LoaderUtil.getChild(econ, "resourceadapter");
        if( null != era ) {
            ra = new ResourceAdapater();
            ra.setResourceAdapterClass(LoaderUtil.getChildContent(era, "resourceadapter-class"));
            ra.setConfigProperty(loadConfigProperty(era));
            ra.setOutboundResourceAdapter(loadOutboundResourceadapter(era));
            ra.setInboundResourceAdapter(loadInboundResourceadapter(era));
            ra.setAdminObject(loadAdminobject(era));
            ra.setSecurityPermission(loadSecurityPermission(era));
        }
        return ra;
    }
    
    private static ConfigProperty[] loadConfigProperty(Element era) {
        Element[] roots = LoaderUtil.getChildren(era, "config-property");
        ConfigProperty[] configProperties = new ConfigProperty[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            configProperties[i] = new ConfigProperty();
            J2EELoader.loadDescribable(root, configProperties[i]);
            configProperties[i].setConfigPropertyName(LoaderUtil.getChildContent(root, "config-property-name"));
            configProperties[i].setConfigPropertyType(LoaderUtil.getChildContent(root, "config-property-type"));
            configProperties[i].setConfigPropertyValue(LoaderUtil.getChildContent(root, "config-property-value"));
        }
        return configProperties;
    }
    
    private static OutboundResourceAdapter loadOutboundResourceadapter(Element era) {
        OutboundResourceAdapter ora = null;
        Element root = LoaderUtil.getChild(era, "outbound-resourceadapter");
        if ( null != root ) {
            ora = new OutboundResourceAdapter();
            ora.setConnectionDefinition(loadConnectionDefinition(root));
            ora.setTransactionSupport(LoaderUtil.getChildContent(root, "transaction-support"));
            ora.setAuthenticationMechanism(loadAuthenticationMechanism(root));
            ora.setReauthenticationSupport(LoaderUtil.getChildContent(root, "reauthentication-support"));
        }
        return ora;
    }

    private static ConnectionDefinition[] loadConnectionDefinition(Element ecd) {
        Element[] roots = LoaderUtil.getChildren(ecd, "connection-definition");
        ConnectionDefinition[] conDefinitions = new ConnectionDefinition[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            conDefinitions[i] = new ConnectionDefinition();
            conDefinitions[i].setManagedConnectionFactoryClass(LoaderUtil.getChildContent(root, "managedconnectionfactory-class"));
            conDefinitions[i].setConfigProperty(loadConfigProperty(root));
            conDefinitions[i].setConnectionFactoryInterface(LoaderUtil.getChildContent(root, "connectionfactory-interface"));
            conDefinitions[i].setConnectionFactoryImplClass(LoaderUtil.getChildContent(root, "connectionfactory-impl-class"));
            conDefinitions[i].setConnectionInterface(LoaderUtil.getChildContent(root, "connection-interface"));
            conDefinitions[i].setConnectionImplClass(LoaderUtil.getChildContent(root, "connection-impl-class"));
        }
        return conDefinitions;
    }

    private static AuthenticationMechanism[] loadAuthenticationMechanism(Element era) {
        Element[] roots = LoaderUtil.getChildren(era, "authentication-mechanism");
        AuthenticationMechanism[] authMech = new AuthenticationMechanism[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            authMech[i] = new AuthenticationMechanism();
            J2EELoader.loadDescribable(root, authMech[i]);
            authMech[i].setAuthenticationMechanismType(LoaderUtil.getChildContent(root, "authentication-mechanism-type"));
            authMech[i].setCredentialInterface(LoaderUtil.getChildContent(root, "credential-interface"));
        }
        return authMech;
    }

    private static InboundResourceAdapter loadInboundResourceadapter(Element era) {
        InboundResourceAdapter ira = null;
        Element root = LoaderUtil.getChild(era, "inbound-resourceadapter");
        if ( null != root ) {
            ira = new InboundResourceAdapter();
            ira.setMessageAdapter(loadMessageadapter(root));
        }
        return ira;    
    }
    
    private static MessageAdapter loadMessageadapter(Element eira) {
        MessageAdapter ma = null;
        Element root = LoaderUtil.getChild(eira, "messageadapter");
        if ( null != root ) {
            ma = new MessageAdapter();
            ma.setMessageListener(loadMessagelistener(root));
        }
        return ma;    
    }

    private static MessageListener[] loadMessagelistener(Element ema) {
        Element[] roots = LoaderUtil.getChildren(ema, "messagelistener");
        MessageListener[] messLst = new MessageListener[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            messLst[i] = new MessageListener();
            messLst[i].setMessageListenerType(LoaderUtil.getChildContent(root, "messagelistener-type"));
            messLst[i].setActivationSpec(loadActivationspec(root));
        }
        return messLst;
    }
    
    private static ActivationSpec loadActivationspec(Element eml) {
        ActivationSpec as = null;
        Element root = LoaderUtil.getChild(eml, "activationspec");
        if ( null != root ) {
            as = new ActivationSpec();
            as.setActivationSpecClass(LoaderUtil.getChildContent(root, "activationspec-class"));
            as.setRequiredConfigProperty(loadRequiredConfigProperty(root));
        }
        return as;
    }
    
    private static RequiredConfigProperty[] loadRequiredConfigProperty(Element eas) {
        Element[] roots = LoaderUtil.getChildren(eas, "required-config-property");
        RequiredConfigProperty[] reqConfProps = new RequiredConfigProperty[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            reqConfProps[i] = new RequiredConfigProperty();
            J2EELoader.loadDescribable(root, reqConfProps[i]);
            reqConfProps[i].setConfigPropertyName(LoaderUtil.getChildContent(root, "config-property-name"));
        }
        return reqConfProps;
    }
    
    private static AdminObject[] loadAdminobject(Element era) {
        Element[] roots = LoaderUtil.getChildren(era, "adminobject");
        AdminObject[] adminObjects = new AdminObject[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            adminObjects[i] = new AdminObject();
            adminObjects[i].setAdminObjectInterface(LoaderUtil.getChildContent(root, "adminobject-interface"));
            adminObjects[i].setAdminObjectClass(LoaderUtil.getChildContent(root, "adminobject-class"));
            adminObjects[i].setConfigProperty(loadConfigProperty(root));
        }
        return adminObjects;
    }
    
    private static SecurityPermission[] loadSecurityPermission(Element era) {
        Element[] roots = LoaderUtil.getChildren(era, "security-permission");
        SecurityPermission[] secPerms = new SecurityPermission[roots.length];
        for(int i = 0; i < roots.length; i++) {
            Element root = roots[i];
            secPerms[i] = new SecurityPermission();
            J2EELoader.loadDescribable(root, secPerms[i]);
            secPerms[i].setSecurityPermissionSpec(LoaderUtil.getChildContent(root, "security-permission-spec"));
        }
        return secPerms;
    }
}
