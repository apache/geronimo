/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.security.deployment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.XmlReferenceBuilder;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerAbstractLoginModuleType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginConfigType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginModuleRefType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginModuleType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerOptionType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class LoginConfigBuilder implements XmlReferenceBuilder {
    public static final String LOGIN_CONFIG_NAMESPACE = "http://geronimo.apache.org/xml/ns/loginconfig-1.0";

    public String getNamespace() {
        return LOGIN_CONFIG_NAMESPACE;
    }

    public Set getReferences(XmlObject xmlObject, DeploymentContext context, J2eeContext j2eeContext, ClassLoader classLoader) throws DeploymentException {
        GerLoginConfigType loginConfig = (GerLoginConfigType) xmlObject.copy().changeType(GerLoginConfigType.type);
        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setLoadLineNumbers();
        Collection errors = new ArrayList();
        xmlOptions.setErrorListener(errors);
        if (!loginConfig.validate(xmlOptions)) {
            throw new DeploymentException("Invalid login configuration:\n" + errors + "\nDescriptor: " + loginConfig.toString());
        }
        XmlCursor xmlCursor = loginConfig.newCursor();
        List uses = new ArrayList();
        Set loginModuleNames = new HashSet();
        try {
            boolean atStart = true;
            while ((atStart && xmlCursor.toFirstChild()) || (!atStart && xmlCursor.toNextSibling())) {
                atStart = false;
                XmlObject child = xmlCursor.getObject();
                GerAbstractLoginModuleType abstractLoginModule = (GerAbstractLoginModuleType) child;
                String controlFlag = abstractLoginModule.getControlFlag().toString();
                ObjectName loginModuleName;
                String name;
                if (abstractLoginModule instanceof GerLoginModuleRefType) {
                    GerLoginModuleRefType loginModuleRef = (GerLoginModuleRefType) abstractLoginModule;
                    String domain = trim(loginModuleRef.getDomain());
                    String server = trim(loginModuleRef.getServer());
                    String application = trim(loginModuleRef.getApplication());
                    String module = trim(loginModuleRef.getModule());
                    String type = trim(loginModuleRef.getType());
                    if (type == null) {
                        type = NameFactory.LOGIN_MODULE;
                    }
                    name = trim(loginModuleRef.getName());
                    try {
                        loginModuleName = NameFactory.getComponentName(domain, server, application, module, name, type, j2eeContext);
                    } catch (MalformedObjectNameException e) {
                        throw new DeploymentException("cannot construct login module name from parts,", e);
                    }
                    try {
                        String loginDomain = (String) context.getAttribute(loginModuleName, "loginDomainName");
                        if(!loginModuleNames.add(loginDomain)) {
                            throw new DeploymentException("Security realm contains two login domains called '"+loginDomain+"'");
                        }
                    } catch(DeploymentException e) {
                        throw e;
                    } catch(Exception e) {
                        throw new DeploymentException("Unable to create reference to login module "+name, e);
                    }
                } else if (abstractLoginModule instanceof GerLoginModuleType) {
                    //create the LoginModuleGBean also
                    name = null;
                    loginModuleName = null;

                    GerLoginModuleType loginModule = (GerLoginModuleType) abstractLoginModule;
                    name = trim(loginModule.getLoginDomainName());
                    if(!loginModuleNames.add(name)) {
                        throw new DeploymentException("Security realm contains two login domains called '"+name+"'");
                    }
                    String className = trim(loginModule.getLoginModuleClass());
                    boolean serverSide = loginModule.getServerSide();
                    Properties options = new Properties();
                    GerOptionType[] optionArray = loginModule.getOptionArray();
                    for (int j = 0; j < optionArray.length; j++) {
                        GerOptionType gerOptionType = optionArray[j];
                        String key = gerOptionType.getName();
                        String value = trim(gerOptionType.getStringValue());
                        options.setProperty(key, value);
                    }
                    try {
                        loginModuleName = NameFactory.getComponentName(null, null, null, null, name, NameFactory.LOGIN_MODULE, j2eeContext);
                    } catch (MalformedObjectNameException e) {
                        throw new DeploymentException("cannot construct login module use name from parts,", e);
                    }
                    GBeanData loginModuleGBeanData = new GBeanData(loginModuleName, LoginModuleGBean.GBEAN_INFO);
                    loginModuleGBeanData.setAttribute("loginDomainName", name);
                    loginModuleGBeanData.setAttribute("loginModuleClass", className);
                    loginModuleGBeanData.setAttribute("options", options);
                    loginModuleGBeanData.setAttribute("serverSide", new Boolean(serverSide));

                    context.addGBean(loginModuleGBeanData);
                } else {
                    throw new DeploymentException("Unknown abstract login module type: " + abstractLoginModule.getClass());
                }
                ObjectName thisName;
                try {
                    thisName = NameFactory.getComponentName(null, null, null, null, name, "LoginModuleUse", j2eeContext);
                } catch (MalformedObjectNameException e) {
                    throw new DeploymentException("cannot construct login module use name from parts,", e);
                }
                GBeanData loginModuleUseGBeanData = new GBeanData(thisName, JaasLoginModuleUse.GBEAN_INFO);
                loginModuleUseGBeanData.setAttribute("controlFlag", controlFlag);
                loginModuleUseGBeanData.setReferencePattern("LoginModule", loginModuleName);
                uses.add(loginModuleUseGBeanData);
            }
            for(int i=uses.size()-1; i>=0; i--) {
                GBeanData data = (GBeanData) uses.get(i);
                if(i > 0) {
                    ((GBeanData)uses.get(i-1)).setReferencePattern("Next", data.getName());
                }
                context.addGBean(data);
            }
        } finally {
            xmlCursor.dispose();
        }
        return uses.size() == 0 ? Collections.EMPTY_SET : Collections.singleton(((GBeanData)uses.get(0)).getName());
    }

    private String trim(String string) {
        return string == null ? null : string.trim();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(LoginConfigBuilder.class, "XmlReferenceBuilder");
        infoBuilder.addInterface(XmlReferenceBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
