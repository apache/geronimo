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

import org.apache.geronimo.deployment.plugin.XmlBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.XpathListener;
import javax.enterprise.deploy.model.XpathEvent;

/**
 * @version $Revision 1.0$  $Date: 2004/02/22 19:11:52 $
 */
public class ConfigPropertySettings extends XmlBeanSupport {
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();
    private String type;
    private DDBean ddBean;
    private XpathListener typeListener;
    private XpathListener nameListener;

    public ConfigPropertySettings() {
        super(null);
    }

    void initialize(GerConfigPropertySettingType xmlObject, DDBean configPropertyBean) {
        setXmlObject(xmlObject);
        ddBean = configPropertyBean;
        DDBean[] child = configPropertyBean.getChildBean("config-property-type");
        if (child.length == 1) {
            setConfigPropertyType(child[0]);
        }
        child = configPropertyBean.getChildBean("config-property-name");
        if (child.length == 1) {
            setConfigPropertyName(child[0]);
        }
        configPropertyBean.addXpathListener("config-property-type", typeListener = new XpathListener() {
            public void fireXpathEvent(XpathEvent xpe) {
                if (xpe.isChangeEvent() || xpe.isAddEvent()) {
                    setConfigPropertyType(xpe.getBean());
                } else if (xpe.isRemoveEvent()) {
                    setConfigPropertyType((String) null);
                }
            }
        });
        configPropertyBean.addXpathListener("config-property-name", nameListener = new XpathListener() {
            public void fireXpathEvent(XpathEvent xpe) {
                if (xpe.isChangeEvent() || xpe.isAddEvent()) {
                    setConfigPropertyName(xpe.getBean());
                } else if (xpe.isRemoveEvent()) {
                    setConfigPropertyName((String) null);
                }
            }
        });
    }

    boolean matches(DDBean target) {
        return target.equals(ddBean);
    }

    void dispose() {
        if (ddBean != null) {
            ddBean.removeXpathListener("config-property-type", typeListener);
            ddBean.removeXpathListener("config-property-name", nameListener);
        }
        nameListener = null;
        typeListener = null;
        ddBean = null;
    }

    GerConfigPropertySettingType getConfigPropertySetting() {
        return (GerConfigPropertySettingType) getXmlObject();
    }

    public String getConfigPropertyName() {
        return getConfigPropertySetting().getName();
    }

    private void setConfigPropertyName(DDBean configPropertyBean) {
        if (configPropertyBean == null) {
            setConfigPropertyName((String) null);
        } else {
            setConfigPropertyName(configPropertyBean.getText());
        }
    }

    private void setConfigPropertyName(String name) {
        String old = getConfigPropertyName();
        getConfigPropertySetting().setName(name);
        pcs.firePropertyChange("configPropertyName", old, name);
    }

    public String getConfigPropertyType() {
        return type;
    }

    private void setConfigPropertyType(DDBean configPropertyBean) {
        if (configPropertyBean == null) {
            setConfigPropertyType((String) null);
        } else {
            setConfigPropertyType(configPropertyBean.getText());
        }
    }

    private void setConfigPropertyType(String type) {
        String old = getConfigPropertyType();
        this.type = type;
        pcs.firePropertyChange("configPropertyType", old, type);
    }

    public String getConfigPropertyValue() {
        return getConfigPropertySetting().getStringValue();
    }

    public void setConfigPropertyValue(String configPropertyValue) {
        String old = getConfigPropertyValue();
        getConfigPropertySetting().setStringValue(configPropertyValue);
        pcs.firePropertyChange("configPropertyValue", old, configPropertyValue);
    }
}
