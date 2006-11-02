/**
 *
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.service.SingleGBeanBuilder;
import org.apache.geronimo.deployment.service.XmlReferenceBuilder;
import org.apache.geronimo.deployment.xbeans.PatternType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerAbstractLoginModuleType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginConfigType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginModuleRefType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginModuleType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerOptionType;
import org.apache.geronimo.xbeans.geronimo.loginconfig.GerLoginConfigDocument;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;


/**
 * @version $Rev$ $Date$
 */
public class LoginConfigBuilder implements XmlReferenceBuilder {
    public static final String LOGIN_CONFIG_NAMESPACE = GerLoginConfigDocument.type.getDocumentElementName().getNamespaceURI();

    private final Naming naming;

    public LoginConfigBuilder(Kernel kernel) {
        this.naming = kernel.getNaming();
    }

    public LoginConfigBuilder(Naming naming) {
        this.naming = naming;
    }

    public String getNamespace() {
        return LOGIN_CONFIG_NAMESPACE;
    }

    public ReferencePatterns getReferences(XmlObject xmlObject, DeploymentContext context, AbstractName parentName, ClassLoader classLoader) throws DeploymentException {
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
                boolean wrapPrincipals = (abstractLoginModule.isSetWrapPrincipals() && abstractLoginModule.getWrapPrincipals());
                ReferencePatterns loginModuleReferencePatterns;
                String name;
                if (abstractLoginModule instanceof GerLoginModuleRefType) {
                    GerLoginModuleRefType loginModuleRef = (GerLoginModuleRefType) abstractLoginModule;
                    PatternType patternType = loginModuleRef.getPattern();
                    AbstractNameQuery loginModuleNameQuery = SingleGBeanBuilder.buildAbstractNameQuery(patternType, USE_REFERENCE_INFO);
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
                } else if (abstractLoginModule instanceof GerLoginModuleType) {
                    //create the LoginModuleGBean also
                    AbstractName loginModuleName;

                    GerLoginModuleType loginModule = (GerLoginModuleType) abstractLoginModule;
                    name = trim(loginModule.getLoginDomainName());
                    if (!loginModuleNames.add(name)) {
                        throw new DeploymentException("Security realm contains two login domains called '" + name + "'");
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
                    loginModuleName = naming.createChildName(parentName, name, NameFactory.LOGIN_MODULE);
                    loginModuleReferencePatterns = new ReferencePatterns(loginModuleName);
                    GBeanData loginModuleGBeanData = new GBeanData(loginModuleName, LoginModuleGBean.GBEAN_INFO);
                    loginModuleGBeanData.setAttribute("loginDomainName", name);
                    loginModuleGBeanData.setAttribute("loginModuleClass", className);
                    loginModuleGBeanData.setAttribute("options", options);
                    loginModuleGBeanData.setAttribute("serverSide", Boolean.valueOf(serverSide));
                    loginModuleGBeanData.setAttribute("wrapPrincipals", Boolean.valueOf(wrapPrincipals));

                    context.addGBean(loginModuleGBeanData);
                } else {
                    throw new DeploymentException("Unknown abstract login module type: " + abstractLoginModule.getClass());
                }
                AbstractName thisName;
                thisName = naming.createChildName(parentName, name, "LoginModuleUse");
                GBeanData loginModuleUseGBeanData = new GBeanData(thisName, JaasLoginModuleUse.GBEAN_INFO);
                loginModuleUseGBeanData.setAttribute("controlFlag", controlFlag);
                loginModuleUseGBeanData.setReferencePatterns("LoginModule", loginModuleReferencePatterns);
                uses.add(loginModuleUseGBeanData);
            }
            for (int i = uses.size() - 1; i >= 0; i--) {
                GBeanData data = (GBeanData) uses.get(i);
                if (i > 0) {
                    ((GBeanData) uses.get(i - 1)).setReferencePattern("Next", data.getAbstractName());
                }
                context.addGBean(data);
            }
        }
        catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException(e);
        } finally {
            xmlCursor.dispose();
        }
        return uses.size() == 0 ? null : new ReferencePatterns(((GBeanData) uses.get(0)).getAbstractName());
    }

    private String trim(String string) {
        return string == null ? null : string.trim();
    }

    public static final GBeanInfo GBEAN_INFO;

    private static final GReferenceInfo USE_REFERENCE_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(LoginConfigBuilder.class, "XmlReferenceBuilder");
        infoBuilder.addAttribute("kernel", Kernel.class, false, false);
        infoBuilder.setConstructor(new String[] {"kernel"});
        infoBuilder.addInterface(XmlReferenceBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();

        Set referenceInfos = JaasLoginModuleUse.GBEAN_INFO.getReferences();
        GReferenceInfo found = null;
        for (Iterator iterator = referenceInfos.iterator(); iterator.hasNext();) {
            GReferenceInfo testReferenceInfo = (GReferenceInfo) iterator.next();
            String testRefName = testReferenceInfo.getName();
            if (testRefName.equals("LoginModule")) {
                found = testReferenceInfo;
                break;
            }
        }
        if (found == null) {
            throw new RuntimeException("Someone changed the gbeaninfo on JaasLoginModuleUse");
        }
        USE_REFERENCE_INFO = found;

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
