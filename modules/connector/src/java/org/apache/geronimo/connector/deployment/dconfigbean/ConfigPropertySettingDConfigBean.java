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

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/09 23:13:27 $
 *
 * */
public class ConfigPropertySettingDConfigBean extends DConfigBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();
    private final static String[] CONFIG_PROPERTY_SETTING_XPATHS = {"config-property"};

    public ConfigPropertySettingDConfigBean(DDBean ddBean, GerConfigPropertySettingType configPropertySetting) {
        super(ddBean, configPropertySetting, SCHEMA_TYPE_LOADER);
        String name = ddBean.getText("config-property-name")[0];
        if (configPropertySetting.getName() == null) {
            configPropertySetting.setName(name);
            String value = ddBean.getText("config-property-value")[0];
            if (value != null) {
                configPropertySetting.setStringValue(value);
            }
        } else {
            assert name.equals(configPropertySetting.getName());
        }
    }

    GerConfigPropertySettingType getConfigPropertySetting() {
        return (GerConfigPropertySettingType)getXmlObject();
    }

    public String getConfigPropertyName() {
        return getConfigPropertySetting().getName();
    }

    //TODO this needs research about if it works.
    public String getConfigPropertyType() {
        return getDDBean().getText("config-property/config-property-type")[0];
    }

    public String getConfigPropertyValue() {
        return getConfigPropertySetting().getStringValue();
    }

    public void setConfigPropertyValue(String configPropertyValue) {
        getConfigPropertySetting().setStringValue(configPropertyValue);
    }
    public String[] getXpaths() {
        return CONFIG_PROPERTY_SETTING_XPATHS;
    }
}
