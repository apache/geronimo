/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.XmlAttributeBuilder;
import org.apache.geronimo.deployment.service.XmlReferenceBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.ReferenceMap;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.deployment.model.loginconfig.AbstractLoginModuleType;
import org.apache.geronimo.security.deployment.model.loginconfig.LoginConfigType;
import org.apache.geronimo.security.deployment.model.loginconfig.LoginModuleRefType;
import org.apache.geronimo.security.deployment.model.loginconfig.LoginModuleType;
import org.apache.geronimo.security.deployment.model.loginconfig.ObjectFactory;
import org.apache.geronimo.security.deployment.model.loginconfig.OptionType;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleControlFlag;
import org.apache.geronimo.security.jaas.LoginModuleControlFlagEditor;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.osgi.framework.Bundle;


/**
 * @version $Rev$ $Date$
 */
public class LoginConfigBuilder {
    public static final String LOGIN_CONFIG_NAMESPACE = ObjectFactory._LoginConfig_QNAME.getNamespaceURI();
    private static final QName LOGIN_MODULE_QNAME = new QName(LOGIN_CONFIG_NAMESPACE, "login-module");
    private static final QName SERVER_SIDE_QNAME = new QName(null, "server-side");

    private final Naming naming;
    private final Map xmlAttributeBuilderMap;

    public LoginConfigBuilder(Kernel kernel, Collection xmlAttributeBuilderMap) {
        this(kernel.getNaming(), xmlAttributeBuilderMap);
    }

    public LoginConfigBuilder(Naming naming, Collection xmlAttributeBuilders) {
        this.naming = naming;
        if (xmlAttributeBuilders != null) {
            ReferenceMap.Key key = new ReferenceMap.Key() {

                public Object getKey(Object object) {
                    return ((XmlAttributeBuilder) object).getNamespace();
                }
            };
            xmlAttributeBuilderMap = new ReferenceMap(xmlAttributeBuilders, new HashMap(), key);
        } else {
            xmlAttributeBuilderMap = new HashMap();
        }
    }

    public String getNamespace() {
        return LOGIN_CONFIG_NAMESPACE;
    }

    public ReferencePatterns getReferences(Object xmlObject, DeploymentContext context, AbstractName parentName, Bundle bundle) throws DeploymentException {
        List<GBeanData> uses = new ArrayList<GBeanData>();
        LoginConfigType loginConfig = (LoginConfigType) xmlObject;
//        XmlCursor xmlCursor = loginConfig.newCursor();
//        xmlCursor.push();
        try {
            //munge xml
//            if (xmlCursor.toChild(LOGIN_MODULE_QNAME)) {
//                do {
//                    xmlCursor.removeAttribute(SERVER_SIDE_QNAME);
//                } while (xmlCursor.toNextSibling(LOGIN_MODULE_QNAME));
//            }
//            xmlCursor.pop();
            //validate
//            XmlOptions xmlOptions = new XmlOptions();
//            xmlOptions.setLoadLineNumbers();
//            Collection errors = new ArrayList();
//            xmlOptions.setErrorListener(errors);
//            if (!loginConfig.validate(xmlOptions)) {
//                throw new DeploymentException("Invalid login configuration:\n" + errors + "\nDescriptor: " + loginConfig.toString());
//            }
            //find the login modules
            Set<String> loginModuleNames = new HashSet<String>();
            for (AbstractLoginModuleType abstractLoginModule: loginConfig.getLoginModuleRefOrLoginModule()) {
                String controlFlag = abstractLoginModule.getControlFlag().toString();
                boolean wrapPrincipals = (abstractLoginModule.isWrapPrincipals() != null && abstractLoginModule.isWrapPrincipals());
                ReferencePatterns loginModuleReferencePatterns;
                String name;
                if (abstractLoginModule instanceof LoginModuleRefType) {
                    LoginModuleRefType loginModuleRef = (LoginModuleRefType) abstractLoginModule;
                    String patternType = loginModuleRef.getFilter();
                    //TODO osgi this is a filter
                    AbstractNameQuery loginModuleNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", patternType), LoginModuleGBean.class.getName());
                    loginModuleReferencePatterns = new ReferencePatterns(loginModuleNameQuery);
                    name = (String) loginModuleNameQuery.getName().get("name");
                    if (name == null) {
                        throw new DeploymentException("You must specify the name of the login module in the login module ref " + patternType);
                    }
//TODO configid reinstate this check for duplicate domain names
//                    try
//                    {
//                        String loginDomain = (String) context.getAttribute(loginModuleName, "loginDomainName");
//                        if (!loginModuleNames.add(loginDomain))
//                        {
//                            throw new DeploymentException("Security realm contains two login domains called '" + loginDomain + "'");
//                        }
//                    }
//                    catch (DeploymentException e)
//                    {
//                        throw e;
//                    }
//                    catch (Exception e)
//                    {
//                        throw new DeploymentException("Unable to create reference to login module " + name, e);
//                    }
                } else if (abstractLoginModule instanceof LoginModuleType) {
                    //create the LoginModuleGBean also
                    AbstractName loginModuleName;

                    LoginModuleType loginModule = (LoginModuleType) abstractLoginModule;
                    name = trim(loginModule.getLoginDomainName());
                    if (!loginModuleNames.add(name)) {
                        throw new DeploymentException("Security realm contains two login domains called '" + name + "'");
                    }
                    String className = trim(loginModule.getLoginModuleClass());
                    Map<String, Object> options = new HashMap<String, Object>();
                    for (OptionType gerOptionType : loginModule.getOption()) {
                        String key = gerOptionType.getName();
                        String value = trim(gerOptionType.getValue());
                        options.put(key, value);
                    }
//                    XmlAttributeType[] xmlOptionArray = loginModule.getXmlOptionArray();
//                    if (xmlOptionArray != null) {
//                        for (XmlAttributeType xmlOptionType : xmlOptionArray) {
//                            String key = xmlOptionType.getName().trim();
//                            XmlObject[] anys = xmlOptionType.selectChildren(XmlAttributeType.type.qnameSetForWildcardElements());
//                            if (anys.length != 1) {
//                                throw new DeploymentException("Unexpected count of xs:any elements in xml-attribute " + anys.length + " qnameset: " + XmlAttributeType.type.qnameSetForWildcardElements());
//                            }
//                            String namespace = xmlObject.getDomNode().getNamespaceURI();
//                            XmlAttributeBuilder builder = (XmlAttributeBuilder) xmlAttributeBuilderMap.get(namespace);
//                            if (builder == null) {
//                                throw new DeploymentException("No attribute builder deployed for namespace: " + namespace);
//                            }
//                            Object value = builder.getValue(anys[0], xmlOptionType, null, bundle);
//                            options.put(key, value);
//                        }
//                    }
                    loginModuleName = naming.createChildName(parentName, name, SecurityNames.LOGIN_MODULE);
                    loginModuleReferencePatterns = new ReferencePatterns(loginModuleName);
                    GBeanData loginModuleGBeanData = new GBeanData(loginModuleName, LoginModuleGBean.GBEAN_INFO);
                    loginModuleGBeanData.setAttribute("loginDomainName", name);
                    loginModuleGBeanData.setAttribute("loginModuleClass", className);
                    loginModuleGBeanData.setAttribute("options", options);
                    loginModuleGBeanData.setAttribute("wrapPrincipals", wrapPrincipals);

                    context.addGBean(loginModuleGBeanData);
                } else {
                    throw new DeploymentException("Unknown abstract login module type: " + abstractLoginModule.getClass());
                }
                AbstractName thisName;
                thisName = naming.createChildName(parentName, name, "LoginModuleUse");
                GBeanData loginModuleUseGBeanData = new GBeanData(thisName, JaasLoginModuleUse.GBEAN_INFO);
                loginModuleUseGBeanData.setAttribute("controlFlag", getControlFlag(controlFlag));
                loginModuleUseGBeanData.setReferencePatterns("LoginModule", loginModuleReferencePatterns);
                uses.add(loginModuleUseGBeanData);
            }
            for (int i = uses.size() - 1; i >= 0; i--) {
                GBeanData data = uses.get(i);
                if (i > 0) {
                    uses.get(i - 1).setReferencePattern("Next", data.getAbstractName());
                }
                context.addGBean(data);
            }
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException(e);
//        } finally {
//            xmlCursor.dispose();
        }
        return uses.size() == 0 ? null : new ReferencePatterns(uses.get(0).getAbstractName());
    }

    private LoginModuleControlFlag getControlFlag(String controlFlag) {
        LoginModuleControlFlagEditor editor = new LoginModuleControlFlagEditor();
        editor.setAsText(controlFlag);
        return (LoginModuleControlFlag) editor.getValue();
    }

    private String trim(String string) {
        return string == null ? null : string.trim();
    }

    public static final GBeanInfo GBEAN_INFO;


    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(LoginConfigBuilder.class, "XmlReferenceBuilder");
        infoBuilder.addAttribute("kernel", Kernel.class, false, false);
        infoBuilder.addReference("xmlAttributeBuilders", XmlAttributeBuilder.class, "XmlAttributeBuilder");
        infoBuilder.setConstructor(new String[]{"kernel", "xmlAttributeBuilders"});
        GBEAN_INFO = infoBuilder.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
