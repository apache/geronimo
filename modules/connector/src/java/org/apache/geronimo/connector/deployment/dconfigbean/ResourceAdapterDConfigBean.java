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

package org.apache.geronimo.connector.deployment.dconfigbean;

import java.util.Map;
import java.util.HashMap;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerOutboundResourceadapterType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionDefinitionType;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/10 08:04:21 $
 *
 * */
public class ResourceAdapterDConfigBean extends DConfigBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();

    private final static String[] RESOURCE_ADAPTER_XPATHS = {
        "config-property",
        "outbound-resourceadapter/connection-definition",
        "adminobject"};
    private Map configPropertiesMap = new HashMap();
    private Map connectionDefinitionsMap = new HashMap();
    private Map adminObjectsMap = new HashMap();

    public ResourceAdapterDConfigBean(DDBean ddBean, final GerResourceadapterType resourceadapter) {
        super(ddBean, resourceadapter, SCHEMA_TYPE_LOADER);
        ConfigPropertiesHelper.initializeConfigSettings(ddBean, new ConfigPropertiesHelper.ConfigPropertiesSource() {
            public GerConfigPropertySettingType[] getConfigPropertySettingArray() {
                return resourceadapter.getConfigPropertySettingArray();
            }

            public GerConfigPropertySettingType addNewConfigPropertySetting() {
                return resourceadapter.addNewConfigPropertySetting();
            }

        }, configPropertiesMap);
        //initialize connection definitions
        GerOutboundResourceadapterType outboundResourceadapter = resourceadapter.getOutboundResourceadapter();
        if (outboundResourceadapter == null) {
            outboundResourceadapter = resourceadapter.addNewOutboundResourceadapter();
        }
        DDBean[] connectionDefinitionDDBeans = ddBean.getChildBean(RESOURCE_ADAPTER_XPATHS[1]);
        GerConnectionDefinitionType[] connectionDefinitions = outboundResourceadapter.getConnectionDefinitionArray();

        if (connectionDefinitions.length == 0) {
            //we are new
            for (int i = 0; i < connectionDefinitionDDBeans.length; i++) {
                DDBean connectionDefinitionDdBean = connectionDefinitionDDBeans[i];
                GerConnectionDefinitionType connectionDefinition = outboundResourceadapter.addNewConnectionDefinition();
                String connectionfactoryInterface = connectionDefinitionDdBean.getText("connectionfactory-interface")[0];
                ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean = new ConnectionDefinitionDConfigBean(connectionDefinitionDdBean, connectionDefinition);
                connectionDefinitionsMap.put(connectionfactoryInterface, connectionDefinitionDConfigBean);
            }
        } else {
            //we are read in from xml.  Check correct length
            assert connectionDefinitionDDBeans.length == connectionDefinitions.length;
            for (int i = 0; i < connectionDefinitionDDBeans.length; i++) {
                DDBean connectionDefinitionDdBean = connectionDefinitionDDBeans[i];
                GerConnectionDefinitionType connectionDefinition = connectionDefinitions[i];
                String connectionfactoryInterface = connectionDefinitionDdBean.getText("connectionfactory-interface")[0];
                assert connectionfactoryInterface.equals(connectionDefinition.getConnectionfactoryInterface().getStringValue());
                ConnectionDefinitionDConfigBean connectionDefinitionDConfigBean = new ConnectionDefinitionDConfigBean(connectionDefinitionDdBean, connectionDefinition);
                connectionDefinitionsMap.put(connectionfactoryInterface, connectionDefinitionDConfigBean);
            }
        }

        //admin objects
        DDBean[] adminObjecDdBeans = ddBean.getChildBean(RESOURCE_ADAPTER_XPATHS[2]);
        GerAdminobjectType[] adminobjectTypes = getResourceadapter().getAdminobjectArray();

        if (adminobjectTypes.length == 0) {
            //we are new
            for (int i = 0; i < adminObjecDdBeans.length; i++) {
                DDBean adminObjectDdBean = adminObjecDdBeans[i];
                GerAdminobjectType adminobjectType = getResourceadapter().addNewAdminobject();
                String adminObjectInterface = adminObjectDdBean.getText("adminobject-interface")[0];
                String adminObjectClass = adminObjectDdBean.getText("adminobject-class")[0];
                AdminObjectDConfigBean adminObjectDConfigBean = new AdminObjectDConfigBean(adminObjectDdBean, adminobjectType);
                adminObjectsMap.put(new Key(adminObjectInterface, adminObjectClass), adminObjectDConfigBean);
            }
        } else {
            //we are read in from xml.  Check correct length
            assert adminObjecDdBeans.length == adminobjectTypes.length;
            for (int i = 0; i < adminObjecDdBeans.length; i++) {
                DDBean adminObjectDdBean = adminObjecDdBeans[i];
                              GerAdminobjectType adminobjectType =  adminobjectTypes[i];
                String adminObjectInterface = adminObjectDdBean.getText("adminobject-interface")[0];
                assert(adminObjectInterface.equals(adminobjectType.getAdminobjectInterface().getStringValue()));
                String adminObjectClass = adminObjectDdBean.getText("adminobject-class")[0];
                assert(adminObjectClass.equals(adminobjectType.getAdminobjectClass().getStringValue()));
                AdminObjectDConfigBean adminObjectDConfigBean = new AdminObjectDConfigBean(adminObjectDdBean, adminobjectType);
                adminObjectsMap.put(new Key(adminObjectInterface, adminObjectClass), adminObjectDConfigBean);

            }
        }

    }

    GerResourceadapterType getResourceadapter() {
        return (GerResourceadapterType)getXmlObject();
    }

    public String getResourceAdapterName() {
        return getResourceadapter().getResourceadapterName();
    }

    public void setResourceAdapterName(String resourceAdapterName) {
        getResourceadapter().setResourceadapterName(resourceAdapterName);
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        String xpath = bean.getXpath();
        if (xpath.endsWith("/" + RESOURCE_ADAPTER_XPATHS[0])) {
            //resource adapter config property
            String configPropertyName = bean.getText("config-property-name")[0];
            ConfigPropertySettingDConfigBean configPropertySetting = (ConfigPropertySettingDConfigBean) configPropertiesMap.get(configPropertyName);
            assert configPropertySetting != null;
            return configPropertySetting;
        }
        if (xpath.endsWith("/" + RESOURCE_ADAPTER_XPATHS[1])) {
            //connection definition
            String connectionFactoryInterface = bean.getText("connectionfactory-interface")[0];
            ConnectionDefinitionDConfigBean connectionDefinition = (ConnectionDefinitionDConfigBean) connectionDefinitionsMap.get(connectionFactoryInterface);
            assert connectionDefinition != null;
            return connectionDefinition;
        }
        if (xpath.endsWith("/" + RESOURCE_ADAPTER_XPATHS[2])) {
            //admin objects
            String adminObjectInterface = bean.getText("adminobject-interface")[0];
            String adminObjectClass = bean.getText("adminobject-class")[0];
            AdminObjectDConfigBean adminObject = (AdminObjectDConfigBean) adminObjectsMap.get(new Key(adminObjectInterface, adminObjectClass));
            assert adminObject != null;
            return adminObject;
        }
        return null;
    }


    public String[] getXpaths() {
        return RESOURCE_ADAPTER_XPATHS;
    }


    //from doubleKeyedHashMap, currently in transaction module
    private final static class Key {
        private final Object part1;
        private final Object part2;

        public Key(Object part1, Object part2) {
            this.part1 = part1;
            this.part2 = part2;
        }

        public int hashCode() {
            return part1.hashCode() ^ part2.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                Key other = (Key) obj;
                return this.part1.equals(other.part1) && this.part2.equals(other.part2);
            } else {
                return false;
            }
        }
    }
}
