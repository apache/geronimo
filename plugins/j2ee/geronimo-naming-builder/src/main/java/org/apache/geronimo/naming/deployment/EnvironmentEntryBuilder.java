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

package org.apache.geronimo.naming.deployment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.reference.KernelReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerEnvEntryDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEnvEntryType;
import org.apache.geronimo.xbeans.javaee6.DescriptionType;
import org.apache.geronimo.xbeans.javaee6.EnvEntryType;
import org.apache.geronimo.xbeans.javaee6.EnvEntryTypeValuesType;
import org.apache.geronimo.xbeans.javaee6.InjectionTargetType;
import org.apache.geronimo.xbeans.javaee6.JndiNameType;
import org.apache.geronimo.xbeans.javaee6.XsdStringType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class EnvironmentEntryBuilder extends AbstractNamingBuilder implements GBeanLifecycle {

    private static final Logger log = LoggerFactory.getLogger(EnvironmentEntryBuilder.class);
    private static final Map<String, String> NAMESPACE_UPDATES = new HashMap<String, String>();
    static {
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/naming", "http://geronimo.apache.org/xml/ns/naming-1.2");
        NAMESPACE_UPDATES.put("http://geronimo.apache.org/xml/ns/naming-1.1", "http://geronimo.apache.org/xml/ns/naming-1.2");
    }

    private static final QName GER_ENV_ENTRY_QNAME = GerEnvEntryDocument.type.getDocumentElementName();
    private static final QNameSet GER_ENV_ENTRY_QNAME_SET = QNameSet.singleton(GER_ENV_ENTRY_QNAME);
    private final QNameSet envEntryQNameSet;

    public EnvironmentEntryBuilder(String[] eeNamespaces) {
        envEntryQNameSet = buildQNameSet(eeNamespaces, "env-entry");
    }

    public void doStart() throws Exception {
        XmlBeansUtil.registerNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doStop() {
        XmlBeansUtil.unregisterNamespaceUpdates(NAMESPACE_UPDATES);
    }

    public void doFail() {
        doStop();
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {

        // Discover and process any @Resource annotations (if !metadata-complete)
        if ((module != null) && (module.getClassFinder() != null)) {

            // Process all the annotations for this naming builder type
            try {
                ResourceAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder(), EnvEntryRefProcessor.INSTANCE);
            }
            catch (Exception e) {
                log.warn("Unable to process @Resource annotations for module" + module.getName(), e);
            }
        }

        List<EnvEntryType> envEntriesUntyped = convert(specDD.selectChildren(envEntryQNameSet), JEE_CONVERTER, EnvEntryType.class, EnvEntryType.type);
        XmlObject[] gerEnvEntryUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_ENV_ENTRY_QNAME_SET);
        Map<String, String> envEntryMap = mapEnvEntries(gerEnvEntryUntyped);
        for (EnvEntryType envEntry: envEntriesUntyped) {
            String name = getStringValue(envEntry.getEnvEntryName());
            String type = getStringValue(envEntry.getEnvEntryType());
            String text = envEntryMap.remove(name);
            if (text == null) {
                text = getStringValue(envEntry.getEnvEntryValue());
            }
            try {
                Object value;
                if (text == null) {
                    if ("org.apache.geronimo.kernel.Kernel".equals(type)) {
                        value = new KernelReference();
                    } else {
                        value = null;
                    }
                } else if ("java.lang.String".equals(type)) {
                    value = text;
                } else if ("java.lang.Character".equals(type)) {
                    value = text.charAt(0);
                } else if ("java.lang.Boolean".equals(type)) {
                    value = Boolean.valueOf(text);
                } else if ("java.lang.Byte".equals(type)) {
                    value = Byte.valueOf(text);
                } else if ("java.lang.Short".equals(type)) {
                    value = Short.valueOf(text);
                } else if ("java.lang.Integer".equals(type)) {
                    value = Integer.valueOf(text);
                } else if ("java.lang.Long".equals(type)) {
                    value = Long.valueOf(text);
                } else if ("java.lang.Float".equals(type)) {
                    value = Float.valueOf(text);
                } else if ("java.lang.Double".equals(type)) {
                    value = Double.valueOf(text);
                } else {
                    throw new DeploymentException("unrecognized type: " + type);
                }
                // perform resource injection only if there is a value specified
                // see Java EE 5 spec, section EE.5.4.1.3
                if (value != null) {
                    addInjections(name, envEntry.getInjectionTargetArray(), componentContext);
                    put(name, value, getJndiContextMap(componentContext));
                }
            } catch (NumberFormatException e) {
                throw new DeploymentException("Invalid env-entry value for name: " + name, e);
            }
        }
        if (!envEntryMap.isEmpty()) {
            throw new DeploymentException("Unknown env-entry elements in geronimo plan: " + envEntryMap);
        }

    }

    private Map<String, String> mapEnvEntries(XmlObject[] refs) {
        Map<String, String> envEntryMap = new HashMap<String, String>();
        if (refs != null) {
            for (XmlObject ref1 : refs) {
                GerEnvEntryType ref = (GerEnvEntryType) ref1.copy().changeType(GerEnvEntryType.type);
                envEntryMap.put(ref.getEnvEntryName().trim(), ref.getEnvEntryValue().trim());
            }
        }
        return envEntryMap;
    }
    public QNameSet getSpecQNameSet() {
        return envEntryQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

    public static class EnvEntryRefProcessor extends ResourceAnnotationHelper.ResourceProcessor {

        public static final EnvEntryRefProcessor INSTANCE = new EnvEntryRefProcessor();

        private EnvEntryRefProcessor() {
        }

        public boolean processResource(AnnotatedApp annotatedApp, Resource annotation, Class cls, Method method, Field field) {
            String resourceName = getResourceName(annotation, method, field);
            String resourceType = getResourceType(annotation, method, field);
            if (resourceType.equals("java.lang.String") ||
                    resourceType.equals("java.lang.Character") ||
                    resourceType.equals("java.lang.Integer") ||
                    resourceType.equals("java.lang.Boolean") ||
                    resourceType.equals("java.lang.Double") ||
                    resourceType.equals("java.lang.Byte") ||
                    resourceType.equals("java.lang.Short") ||
                    resourceType.equals("java.lang.Long") ||
                    resourceType.equals("java.lang.Float")) {

                log.debug("addResource(): <env-entry> found");

                boolean exists = false;
                EnvEntryType[] envEntries = annotatedApp.getEnvEntryArray();
                for (EnvEntryType envEntry : envEntries) {
                    if (getStringValue(envEntry.getEnvEntryName()).equals(resourceName)) {
                        exists = true;
                        if (method != null || field != null) {
                            InjectionTargetType[] targets = envEntry.getInjectionTargetArray();
                            if (!hasTarget(method, field, targets)) {
                                configureInjectionTarget(envEntry.addNewInjectionTarget(), method, field);
                            }
                        }
                        break;
                    }
                }
                if (!exists) {
                    try {

                        log.debug("addResource(): Does not exist in DD: " + resourceName);

                        // Doesn't exist in deployment descriptor -- add new
                        EnvEntryType envEntry = annotatedApp.addNewEnvEntry();

                        //------------------------------------------------------------------------------
                        // <env-entry> required elements:
                        //------------------------------------------------------------------------------

                        // env-entry-name
                        JndiNameType envEntryName = envEntry.addNewEnvEntryName();
                        envEntryName.setStringValue(resourceName);

                        if (!resourceType.equals("")) {
                            // env-entry-type
                            EnvEntryTypeValuesType envEntryType = envEntry.addNewEnvEntryType();
                            envEntryType.setStringValue(resourceType);
                        }                         
                        if (method != null || field != null) {
                            // injectionTarget
                            InjectionTargetType injectionTarget = envEntry.addNewInjectionTarget();
                            configureInjectionTarget(injectionTarget, method, field);
                        }

                        // mappedName
                        String mappdedNameAnnotation = annotation.mappedName();
                        if (!mappdedNameAnnotation.equals("")) {
                            XsdStringType mappedName = envEntry.addNewMappedName();
                            mappedName.setStringValue(mappdedNameAnnotation);
                            envEntry.setMappedName(mappedName);
                        }

                        //------------------------------------------------------------------------------
                        // <env-entry> optional elements:
                        //------------------------------------------------------------------------------

                        // description
                        String descriptionAnnotation = annotation.description();
                        if (!descriptionAnnotation.equals("")) {
                            DescriptionType description = envEntry.addNewDescription();
                            description.setStringValue(descriptionAnnotation);
                        }

                    }
                    catch (Exception anyException) {
                        log.debug("ResourceAnnotationHelper: Exception caught while processing <env-entry>");
                    }
                }
            }
            return false;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EnvironmentEntryBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.setConstructor(new String[] {"eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
