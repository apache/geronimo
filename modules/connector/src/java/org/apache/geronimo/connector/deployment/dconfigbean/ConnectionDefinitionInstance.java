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

import java.math.BigInteger;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathListener;

import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 * @version $Revision 1.0$  $Date: 2004/02/21 01:10:50 $
 */
public class ConnectionDefinitionInstance extends XmlBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();
    private ConfigPropertySettings[] configs = new ConfigPropertySettings[0];
    private ConnectionDefinitionDConfigBean parent;
    private XpathListener configListener;

    public ConnectionDefinitionInstance() {
        super(null, SCHEMA_TYPE_LOADER);
    }

    void initialize(GerConnectiondefinitionInstanceType xmlObject, ConnectionDefinitionDConfigBean parent) {
        setXmlObject(xmlObject);
        this.parent = parent;
        DDBean parentDDBean = parent.getDDBean();
        configListener = ConfigPropertiesHelper.initialize(parentDDBean, new ConfigPropertiesHelper.ConfigPropertiesSource() {
            public GerConfigPropertySettingType[] getConfigPropertySettingArray() {
                return getConnectiondefinitionInstance().getConfigPropertySettingArray();
            }

            public GerConfigPropertySettingType addNewConfigPropertySetting() {
                return getConnectiondefinitionInstance().addNewConfigPropertySetting();
            }

            public void removeConfigPropertySetting(int j) {
                getConnectiondefinitionInstance().removeConfigPropertySetting(j);
            }

            public ConfigPropertySettings[] getConfigPropertySettings() {
                return configs;
            }

            public void setConfigPropertySettings(ConfigPropertySettings[] configs) {
                setConfigProperty(configs);
            }

        }, "config-property", "config-property-name");
    }


    boolean hasParent() {
        return parent != null;
    }

    void dispose() {
        if (configs != null) {
            for (int i = 0; i < configs.length; i++) {
                configs[i].dispose();
            }
        }
        if (parent != null) {
            parent.getDDBean().removeXpathListener("config-property", configListener);
        }
        configs = null;
        configListener = null;
        parent = null;
    }

// JavaBean properties for this object (with a couple helper methods)

    GerConnectiondefinitionInstanceType getConnectiondefinitionInstance() {
        return (GerConnectiondefinitionInstanceType) getXmlObject();
    }

    GerConnectionmanagerType getConnectionManager() {
        return getConnectiondefinitionInstance().getConnectionmanager();
    }

    public ConfigPropertySettings[] getConfigProperty() {
        return configs;
    }

    private void setConfigProperty(ConfigPropertySettings[] configs) { // can only be changed by adding a new DDBean
        ConfigPropertySettings[] old = getConfigProperty();
        this.configs = configs;
        pcs.firePropertyChange("configProperty", old, configs);
    }

    public String getName() {
        return getConnectiondefinitionInstance().getName();
    }

    public void setName(String name) {
        String old = getName();
        getConnectiondefinitionInstance().setName(name);
        pcs.firePropertyChange("name", old, name);
    }

    public String getGlobalJNDIName() {
        return getConnectiondefinitionInstance().getGlobalJndiName();
    }

    public void setGlobalJNDIName(String globalJNDIName) {
        String old = getGlobalJNDIName();
        getConnectiondefinitionInstance().setGlobalJndiName(globalJNDIName);
        pcs.firePropertyChange("globalJNDIName", old, globalJNDIName);
    }

    public boolean isUseConnectionRequestInfo() {
        return getConnectionManager().getUseConnectionRequestInfo();
    }

    public void setUseConnectionRequestInfo(boolean useConnectionRequestInfo) {
        boolean old = isUseConnectionRequestInfo();
        getConnectionManager().setUseConnectionRequestInfo(useConnectionRequestInfo);
        pcs.firePropertyChange("useConnectionRequestInfo", old, useConnectionRequestInfo);
    }

    public boolean isUseSubject() {
        return getConnectionManager().getUseSubject();
    }

    public void setUseSubject(boolean useSubject) {
        boolean old = isUseSubject();
        getConnectionManager().setUseSubject(useSubject);
        pcs.firePropertyChange("useSubject", old, useSubject);
    }

    public boolean isUseTransactionCaching() {
        return getConnectionManager().getUseTransactionCaching();
    }

    public void setUseTransactionCaching(boolean useTransactionCaching) {
        boolean old = isUseTransactionCaching();
        getConnectionManager().setUseTransactionCaching(useTransactionCaching);
        pcs.firePropertyChange("useTransactionCaching", old, useTransactionCaching);
    }

    public boolean isUseLocalTransactions() {
        return getConnectionManager().getUseLocalTransactions();
    }

    public void setUseLocalTransactions(boolean useLocalTransactions) {
        boolean old = isUseLocalTransactions();
        getConnectionManager().setUseLocalTransactions(useLocalTransactions);
        pcs.firePropertyChange("useLocalTransactions", old, useLocalTransactions);
    }

    public boolean isUseTransactions() {
        return getConnectionManager().getUseTransactions();
    }

    public void setUseTransactions(boolean useTransactions) {
        boolean old = isUseTransactions();
        getConnectionManager().setUseTransactions(useTransactions);
        pcs.firePropertyChange("useTransactions", old, useTransactions);
    }

    public int getMaxSize() {
        BigInteger test = getConnectionManager().getMaxSize();
        return test == null ? 0 : test.intValue();
    }

    public void setMaxSize(int maxSize) {
        int old = getMaxSize();
        getConnectionManager().setMaxSize(BigInteger.valueOf(maxSize));
        pcs.firePropertyChange("maxSize", old, maxSize);
    }

    public int getBlockingTimeout() {
        BigInteger test = getConnectionManager().getBlockingTimeout();
        return test == null ? 0 : test.intValue();
    }

    public void setBlockingTimeout(int blockingTimeout) {
        int old = getBlockingTimeout();
        getConnectionManager().setBlockingTimeout(BigInteger.valueOf(blockingTimeout));
        pcs.firePropertyChange("blockingTimeout", old, blockingTimeout);
    }

    public String getRealmBridgeName() {
        return getConnectionManager().getRealmBridge();
    }

    public void setRealmBridgeName(String realmBridgeName) {
        String old = getRealmBridgeName();
        getConnectionManager().setRealmBridge(realmBridgeName);
        pcs.firePropertyChange("realmBridgeName", old, realmBridgeName);
    }

}
