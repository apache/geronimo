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

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.deployment.plugin.XmlBeanSupport;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.XpathEvent;
import java.math.BigInteger;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @version $Revision 1.0$  $Date: 2004/02/15 17:53:22 $
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
        DDBean[] beans = parent.getDDBean().getChildBean("config-property");
        configs = new ConfigPropertySettings[beans.length];
        Set xmlBeans = new HashSet(Arrays.asList(getConnectiondefinitionInstance().getConfigPropertySettingArray()));
        for (int i = 0; i < beans.length; i++) {
            DDBean bean = beans[i];
            String[] names = bean.getText("config-property-name");
            String name = names.length == 1 ? names[0] : "";
            GerConfigPropertySettingType target = null;
            for (Iterator it = xmlBeans.iterator(); it.hasNext();) {
                GerConfigPropertySettingType setting = (GerConfigPropertySettingType) it.next();
                if(setting.getName().equals(name)) {
                    target = setting;
                    xmlBeans.remove(target);
                    break;
                }
            }
            if(target == null) {
                target = getConnectiondefinitionInstance().addNewConfigPropertySetting();
            }
            configs[i] = new ConfigPropertySettings();
            configs[i].initialize(target, bean);
        }
        for (Iterator it = xmlBeans.iterator(); it.hasNext();) { // used to be in XmlBeans, no longer anything matching in J2EE DD
            GerConfigPropertySettingType target = (GerConfigPropertySettingType) it.next();
            for (int i = 0; i < getConnectiondefinitionInstance().getConfigPropertySettingArray().length; i++) {
                if(getConnectiondefinitionInstance().getConfigPropertySettingArray(i) == target) {
                    getConnectiondefinitionInstance().removeConfigPropertySetting(i);
                    break;
                }
            }
        }
        parent.getDDBean().addXpathListener("config-property", configListener = new XpathListener() {
            public void fireXpathEvent(XpathEvent xpe) {
                if(xpe.isAddEvent()) {
                    ConfigPropertySettings[] bigger = new ConfigPropertySettings[configs.length+1];
                    System.arraycopy(configs, 0, bigger, 0, configs.length);
                    bigger[configs.length] = new ConfigPropertySettings();
                    bigger[configs.length].initialize(getConnectiondefinitionInstance().addNewConfigPropertySetting(), xpe.getBean());
                    setConfigProperty(bigger);
                } else if(xpe.isRemoveEvent()) {
                    int index = -1;
                    for (int i = 0; i < configs.length; i++) {
                        if(configs[i].matches(xpe.getBean())) {
                            // remove the XMLBean
                            for (int j = 0; j < getConnectiondefinitionInstance().getConfigPropertySettingArray().length; j++) {
                                GerConfigPropertySettingType test = getConnectiondefinitionInstance().getConfigPropertySettingArray(j);
                                if(test == configs[i].getConfigPropertySetting()) {
                                    getConnectiondefinitionInstance().removeConfigPropertySetting(j);
                                    break;
                                }
                            }
                            // clean up the JavaBean
                            configs[i].dispose();
                            index = i;
                            break;
                        }
                    }
                    // remove the JavaBean from my list
                    if(index > -1) {
                        ConfigPropertySettings[] smaller = new ConfigPropertySettings[configs.length-1];
                        System.arraycopy(configs, 0, smaller, 0, index);
                        System.arraycopy(configs, index+1, smaller, index, smaller.length-index);
                        setConfigProperty(smaller);
                    }
                }
                // ignore change event (no contents, no attributes)
            }
        });
    }

    boolean hasParent() {
        return parent != null;
    }

    void dispose() {
        if(configs != null) {
            for (int i = 0; i < configs.length; i++) {
                configs[i].dispose();
            }
        }
        if(parent != null) {
            parent.getDDBean().removeXpathListener("config-property", configListener);
        }
        configs = null;
        configListener = null;
        parent = null;
    }

// JavaBean properties for this object (with a couple helper methods)

    GerConnectiondefinitionInstanceType getConnectiondefinitionInstance() {
        return (GerConnectiondefinitionInstanceType)getXmlObject();
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
