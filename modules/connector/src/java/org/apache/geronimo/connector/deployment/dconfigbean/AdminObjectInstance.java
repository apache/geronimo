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

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathListener;

import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/22 19:11:52 $
 *
 * */
public class AdminObjectInstance extends XmlBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();
    private AdminObjectDConfigBean parent;
    private ConfigPropertySettings[] configs;
    private XpathListener configListener;

    public AdminObjectInstance() {
        super(null);
    }

    void initialize(GerAdminobjectInstanceType xmlObject, AdminObjectDConfigBean parent) {
        setXmlObject(xmlObject);
        this.parent = parent;
        DDBean parentDDBean = parent.getDDBean();
        configListener = ConfigPropertiesHelper.initialize(parentDDBean, new ConfigPropertiesHelper.ConfigPropertiesSource() {
            public GerConfigPropertySettingType[] getConfigPropertySettingArray() {
                return getAdminobjectInstance().getConfigPropertySettingArray();
            }

            public GerConfigPropertySettingType addNewConfigPropertySetting() {
                return getAdminobjectInstance().addNewConfigPropertySetting();
            }

            public void removeConfigPropertySetting(int j) {
                getAdminobjectInstance().removeConfigPropertySetting(j);
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
    GerAdminobjectInstanceType getAdminobjectInstance() {
        return (GerAdminobjectInstanceType) getXmlObject();
    }

    public ConfigPropertySettings[] getConfigProperty() {
        return configs;
    }

    private void setConfigProperty(ConfigPropertySettings[] configs) { // can only be changed by adding a new DDBean
        ConfigPropertySettings[] old = getConfigProperty();
        this.configs = configs;
        pcs.firePropertyChange("configProperty", old, configs);
    }

    public String getAdminObjectName() {
        return getAdminobjectInstance().getAdminobjectName();
    }

    public void setAdminObjectName(String adminObjectName) {
        getAdminobjectInstance().setAdminobjectName(adminObjectName);
    }

}
