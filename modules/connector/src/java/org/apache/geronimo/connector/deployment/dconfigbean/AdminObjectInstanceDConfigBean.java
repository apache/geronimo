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

import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;

import org.apache.geronimo.deployment.plugin.DConfigBeanSupport;
import org.apache.geronimo.xbeans.geronimo.GerAdminobjectInstanceType;
import org.apache.geronimo.xbeans.geronimo.GerConfigPropertySettingType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/11 08:02:20 $
 *
 * */
public class AdminObjectInstanceDConfigBean extends DConfigBeanSupport{
    private final static SchemaTypeLoader SCHEMA_TYPE_LOADER = XmlBeans.getContextTypeLoader();

    private final static String[] ADMIN_OBJECT_INSTANCE_XPATHS = {"config-property"};
    private Map configPropertiesMap = new HashMap();

    public AdminObjectInstanceDConfigBean() {
        super(null, null, SCHEMA_TYPE_LOADER);
    }

    public AdminObjectInstanceDConfigBean(DDBean ddBean, final GerAdminobjectInstanceType adminobjectInstance) {
        super(ddBean, adminobjectInstance, SCHEMA_TYPE_LOADER);
        initialize(ddBean, adminobjectInstance);
    }

    void setParent(DDBean ddBean, final GerAdminobjectInstanceType adminobjectInstance) {
        super.setParent(ddBean, adminobjectInstance);
        initialize(ddBean, adminobjectInstance);
    }

    private void initialize(DDBean ddBean, final GerAdminobjectInstanceType adminobjectInstance) {
        ConfigPropertiesHelper.initializeConfigSettings(ddBean, new ConfigPropertiesHelper.ConfigPropertiesSource() {
            public GerConfigPropertySettingType[] getConfigPropertySettingArray() {
                return adminobjectInstance.getConfigPropertySettingArray();
            }

            public GerConfigPropertySettingType addNewConfigPropertySetting() {
                return adminobjectInstance.addNewConfigPropertySetting();
            }

        }, configPropertiesMap);
    }


    GerAdminobjectInstanceType getAdminobjectInstance() {
        return (GerAdminobjectInstanceType)getXmlObject();
    }

    public String getAdminObjectName() {
        return getAdminobjectInstance().getAdminobjectName();
    }

    public void setAdminObjectName(String adminObjectName) {
        getAdminobjectInstance().setAdminobjectName(adminObjectName);
    }

    public DConfigBean getDConfigBean(DDBean bean) throws ConfigurationException {
        String xpath = bean.getXpath();
        if (xpath.equals(ADMIN_OBJECT_INSTANCE_XPATHS[0])) {
            String configPropertyName = bean.getText("config-property-name")[0];
            ConfigPropertySettingDConfigBean configPropertySetting = (ConfigPropertySettingDConfigBean) configPropertiesMap.get(configPropertyName);
            assert configPropertySetting != null;
            return configPropertySetting;
        }
        return null;
    }


    public String[] getXpaths() {
        return ADMIN_OBJECT_INSTANCE_XPATHS;
    }


}
