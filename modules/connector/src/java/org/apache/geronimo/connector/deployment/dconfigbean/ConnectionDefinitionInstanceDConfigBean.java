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

import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.geronimo.xbeans.geronimo.GerConnectiondefinitionInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConnectionmanagerType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/11 08:02:20 $
 *
 * */
public class ConnectionDefinitionInstanceDConfigBean extends DConfigBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();

    private final static String[] CONNECTION_DEFINITION_INSTANCE_XPATHS = {
        "config-property"};
    private Map configPropertiesMap = new HashMap();

    public ConnectionDefinitionInstanceDConfigBean() {
        super(null, null, SCHEMA_TYPE_LOADER);
    }

    public ConnectionDefinitionInstanceDConfigBean(DDBean ddBean, final GerConnectiondefinitionInstanceType connectiondefinitionInstance) {
        super(ddBean, connectiondefinitionInstance, SCHEMA_TYPE_LOADER);
        initialize(ddBean, connectiondefinitionInstance);
    }

    void setParent(DDBean ddBean, final GerConnectiondefinitionInstanceType connectiondefinitionInstance){
        super.setParent(ddBean, connectiondefinitionInstance);
        initialize(ddBean, connectiondefinitionInstance);
    }

    private void initialize(DDBean ddBean, final GerConnectiondefinitionInstanceType connectiondefinitionInstance) {
        ConfigPropertiesHelper.initializeConfigSettings(ddBean, new ConfigPropertiesHelper.ConfigPropertiesSource() {
            public GerConfigPropertySettingType[] getConfigPropertySettingArray() {
                return connectiondefinitionInstance.getConfigPropertySettingArray();
            }

            public GerConfigPropertySettingType addNewConfigPropertySetting() {
                return connectiondefinitionInstance.addNewConfigPropertySetting();
            }

        }, configPropertiesMap);
        if (connectiondefinitionInstance.getConnectionmanager() == null) {
            connectiondefinitionInstance.addNewConnectionmanager();
        }
    }


    GerConnectiondefinitionInstanceType getConnectiondefinitionInstance() {
        return (GerConnectiondefinitionInstanceType)getXmlObject();
    }

    GerConnectionmanagerType getConnectionManager() {
        return getConnectiondefinitionInstance().getConnectionmanager();
    }

    public String getName() {
        return getConnectiondefinitionInstance().getName();
    }

    public void setName(String name) {
        getConnectiondefinitionInstance().setName(name);
    }

    public String getGlobalJNDIName() {
        return getConnectiondefinitionInstance().getGlobalJndiName();
    }

    public void setGlobalJNDIName(String globalJNDIName) {
        getConnectiondefinitionInstance().setGlobalJndiName(globalJNDIName);
    }

    public boolean isUseConnectionRequestInfo() {
        return getConnectionManager().getUseConnectionRequestInfo();
    }

    public void setUseConnectionRequestInfo(boolean useConnectionRequestInfo) {
        getConnectionManager().setUseConnectionRequestInfo(useConnectionRequestInfo);
    }

    public boolean isUseSubject() {
        return getConnectionManager().getUseSubject();
    }

    public void setUseSubject(boolean useSubject) {
        getConnectionManager().setUseSubject(useSubject);
    }

    public boolean isUseTransactionCaching() {
        return getConnectionManager().getUseTransactionCaching();
    }

    public void setUseTransactionCaching(boolean useTransactionCaching) {
        getConnectionManager().setUseTransactionCaching(useTransactionCaching);
    }

    public boolean isUseLocalTransactions() {
        return getConnectionManager().getUseLocalTransactions();
    }

    public void setUseLocalTransactions(boolean useLocalTransactions) {
        getConnectionManager().setUseLocalTransactions(useLocalTransactions);
    }

    public boolean isUseTransactions() {
        return getConnectionManager().getUseTransactions();
    }

    public void setUseTransactions(boolean useTransactions) {
        getConnectionManager().setUseTransactions(useTransactions);
    }

    public int getMaxSize() {
        return getConnectionManager().getMaxSize().intValue();
    }

    public void setMaxSize(int maxSize) {
        getConnectionManager().setMaxSize(BigInteger.valueOf(maxSize));
    }

    public int getBlockingTimeout() {
        return getConnectionManager().getBlockingTimeout().intValue();
    }

    public void setBlockingTimeout(int blockingTimeout) {
        getConnectionManager().setBlockingTimeout(BigInteger.valueOf(blockingTimeout));
    }

    public String getRealmBridgeName() {
        return getConnectionManager().getRealmBridge();
    }

    public void setRealmBridgeName(String realmBridgeName) {
        getConnectionManager().setRealmBridge(realmBridgeName);
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        String xpath = bean.getXpath();
        if (xpath.equals(CONNECTION_DEFINITION_INSTANCE_XPATHS[0])) {
            String configPropertyName = bean.getText("config-property-name")[0];
            ConfigPropertySettingDConfigBean configPropertySetting = (ConfigPropertySettingDConfigBean) configPropertiesMap.get(configPropertyName);
            assert configPropertySetting != null;
            return configPropertySetting;
        }
        return null;
    }


    public String[] getXpaths() {
        return CONNECTION_DEFINITION_INSTANCE_XPATHS;
    }

}
