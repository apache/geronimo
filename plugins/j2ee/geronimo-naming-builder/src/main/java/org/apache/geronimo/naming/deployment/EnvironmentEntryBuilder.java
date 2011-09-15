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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.ResourceAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.reference.ClassReference;
import org.apache.geronimo.naming.reference.JndiReference;
import org.apache.geronimo.naming.reference.KernelReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerEnvEntryDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEnvEntryType;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.Text;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */

@GBean(j2eeType = NameFactory.MODULE_BUILDER)
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

    public EnvironmentEntryBuilder(@ParamAttribute(name = "eeNamespaces")String[] eeNamespaces) {
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

    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {

        // Discover and process any @Resource annotations (if !metadata-complete)
        if ((module != null) && (module.getClassFinder() != null)) {

            // Process all the annotations for this naming builder type
            try {
                ResourceAnnotationHelper.processAnnotations(specDD, module.getClassFinder(), EnvEntryRefProcessor.INSTANCE);
            }
            catch (Exception e) {
                log.warn("Unable to process @Resource annotations for module" + module.getName(), e);
            }
        }

        Bundle bundle = module.getEarContext().getDeploymentBundle();
        XmlObject[] gerEnvEntryUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_ENV_ENTRY_QNAME_SET);
        Map<String, String> envEntryMap = mapEnvEntries(gerEnvEntryUntyped);
        for (Map.Entry<String, EnvEntry> entry : specDD.getEnvEntryMap().entrySet()) {
            String name = entry.getKey();
            EnvEntry envEntry = entry.getValue();

            if (lookupJndiContextMap(module, name) != null) {
                // some other builder handled this entry already
                addInjections(normalize(name), ReferenceType.ENV_ENTRY, envEntry.getInjectionTarget(), NamingBuilder.INJECTION_KEY.get(sharedContext));
                continue;
            }

            String type = getStringValue(envEntry.getEnvEntryType());

            Object value = null;

            String strValueOverride = envEntryMap.remove(name);
            String strValue = null;
            if (strValueOverride == null) {
                strValue = envEntry.getEnvEntryValue();
                String lookupName = getStringValue(envEntry.getLookupName());
                if (strValue != null && lookupName != null) {
                    throw new DeploymentException("You must specify an environment entry value or lookup name but not both. Component: " + module.toString() + ", name: " + name + ", env-entry-value: " + strValue + ", lookup-name: " + lookupName + "");
                }
                if (lookupName != null) {
                    //TODO better circular reference checking
                    if (lookupName.equals(getJndiName(name))) {
                        throw new DeploymentException("env-entry lookup name refers to itself");
                    }
                    value = new JndiReference(lookupName);
                }
            } else {
                strValue = strValueOverride;
            }

            type = inferAndCheckType(module, bundle, envEntry.getInjectionTarget(), name, type);


            if (value == null) {
                if (strValue == null) {
                    if ("org.apache.geronimo.kernel.Kernel".equals(type)) {
                        value = new KernelReference();
                    }
                } else {
                    Class<?> typeClass;
                    try {
                        typeClass = bundle.loadClass(type);
                    } catch (ClassNotFoundException e) {
                        throw new DeploymentException("Could not env-entry type class " + type, e);
                    }
                    try {
                        if (String.class.equals(typeClass)) {
                            value = strValue;
                        } else if (Character.class.equals(typeClass)) {
                            if (strValue.length() == 1) {
                                value = strValue.charAt(0);
                            } else {
                                log.warn("invalid character value: {} for name {}", strValue, name);
                                value = ' ';
                            }
                        } else if (Boolean.class.equals(typeClass)) {
                            value = Boolean.valueOf(strValue);
                        } else if (Byte.class.equals(typeClass)) {
                            value = Byte.valueOf(strValue);
                        } else if (Short.class.equals(typeClass)) {
                            value = Short.valueOf(strValue);
                        } else if (Integer.class.equals(typeClass)) {
                            value = Integer.valueOf(strValue);
                        } else if (Long.class.equals(typeClass)) {
                            value = Long.valueOf(strValue);
                        } else if (Float.class.equals(typeClass)) {
                            value = Float.valueOf(strValue);
                        } else if (Double.class.equals(typeClass)) {
                            value = Double.valueOf(strValue);
                        } else if (Class.class.equals(typeClass)) {
                            value = new ClassReference(strValue);
                        } else if (typeClass.isEnum()) {
                            value = Enum.valueOf(typeClass.asSubclass(Enum.class), strValue);
                        } else {
                            throw new DeploymentException("Unrecognized env-entry type: " + type);
                        }
                    } catch (NumberFormatException e) {
                        throw new DeploymentException("Invalid env-entry value for name: " + name, e);
                    }
                }
            }

            // perform resource injection only if there is a value specified
            // see Java EE 5 spec, section EE.5.4.1.3
            if (value != null) {
                put(name, value, ReferenceType.ENV_ENTRY, module.getJndiContext(), envEntry.getInjectionTarget(), sharedContext);
            } else if(isSharableJndiNamespace(name)) {
                //Even the value is configured, while it is belong to those shareable namespace, it is still to be added to the injection list
                addInjections(normalize(name), ReferenceType.ENV_ENTRY, envEntry.getInjectionTarget(), NamingBuilder.INJECTION_KEY.get(sharedContext));
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
                envEntryMap.put(getJndiName(ref.getEnvEntryName().trim()), ref.getEnvEntryValue());
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

        private static final Set<String> knownEnvironmentEntries = new HashSet<String>(Arrays.asList(
                "boolean", "java.lang.Boolean",
                "char", "java.lang.Character",
                "byte", "java.lang.Byte",
                "short", "java.lang.Short",
                "int", "java.lang.Integer",
                "long", "java.lang.Long",
                "float", "java.lang.Float",
                "double", "java.lang.Double",
                "java.lang.String",
                "java.lang.Class"
        ));

        private EnvEntryRefProcessor() {
        }

        public boolean processResource(JndiConsumer annotatedApp, Resource annotation, Class cls, Method method, Field field) {
            String resourceName = getResourceName(annotation, method, field);
            Class resourceType = getResourceTypeClass(annotation, method, field);
            if (knownEnvironmentEntries.contains(resourceType.getName()) || resourceType.isEnum()) {
                log.debug("addResource(): <env-entry> found");

                EnvEntry envEntry = annotatedApp.getEnvEntryMap().get(getJndiName(resourceName));

                if (envEntry == null) {
                    try {

                        log.debug("addResource(): Does not exist in DD: " + resourceName);

                        // Doesn't exist in deployment descriptor -- add new
                        envEntry = new EnvEntry();

                        //------------------------------------------------------------------------------
                        // <env-entry> required elements:
                        //------------------------------------------------------------------------------

                        // env-entry-name
                        envEntry.setEnvEntryName(resourceName);

                        if (!resourceType.equals(Object.class)) {
                            // env-entry-type
                            envEntry.setEnvEntryType(deprimitivize(resourceType).getCanonicalName());
                        }

                        //------------------------------------------------------------------------------
                        // <env-entry> optional elements:
                        //------------------------------------------------------------------------------

                        // mappedName
                        String mappdedNameAnnotation = annotation.mappedName();
                        if (!mappdedNameAnnotation.equals("")) {
                            envEntry.setMappedName(mappdedNameAnnotation);
                        }

                        // description
                        String descriptionAnnotation = annotation.description();
                        if (!descriptionAnnotation.equals("")) {
                            envEntry.setDescriptions(new Text[] {new Text(null, descriptionAnnotation)});
                        }

                        // lookup
                        String lookup = annotation.lookup();
                        if (!lookup.equals("")) {
                            envEntry.setLookupName(lookup);
                        }
                        annotatedApp.getEnvEntry().add(envEntry);
                    }
                    catch (Exception anyException) {
                        log.debug("ResourceAnnotationHelper: Exception caught while processing <env-entry>");
                    }
                }

                if (method != null || field != null) {
                    Set<InjectionTarget> targets = envEntry.getInjectionTarget();
                    if (!hasTarget(method, field, targets)) {
                        envEntry.getInjectionTarget().add(configureInjectionTarget(method, field));
                    }
                }

                return true;
            }

            return false;
        }
    }

}
