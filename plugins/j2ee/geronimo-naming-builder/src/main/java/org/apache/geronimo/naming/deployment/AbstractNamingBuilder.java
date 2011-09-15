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

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.annotation.Holder;
import org.apache.geronimo.j2ee.annotation.Injection;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.jndi.JndiKey;
import org.apache.geronimo.j2ee.jndi.JndiScope;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.xbeans.geronimo.naming.GerAbstractNamingEntryDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerAbstractNamingEntryType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractNamingBuilder implements NamingBuilder {
    private final Logger log = LoggerFactory.getLogger(AbstractNamingBuilder.class);

    protected static final QName BASE_NAMING_QNAME = GerAbstractNamingEntryType.type.getDocumentElementName();
    protected static final String J2EE_NAMESPACE = "http://java.sun.com/xml/ns/j2ee";
    protected static final String JEE_NAMESPACE = "http://java.sun.com/xml/ns/javaee";
    protected static final NamespaceElementConverter J2EE_CONVERTER = new NamespaceElementConverter(J2EE_NAMESPACE);
    protected static final NamespaceElementConverter JEE_CONVERTER = new NamespaceElementConverter(JEE_NAMESPACE);
    protected static final NamespaceElementConverter NAMING_CONVERTER = new NamespaceElementConverter(GerAbstractNamingEntryDocument.type.getDocumentElementName().getNamespaceURI());

    private final Environment defaultEnvironment;

    protected AbstractNamingBuilder() {
        defaultEnvironment = null;
    }

    protected AbstractNamingBuilder(Environment defaultEnvironment) {
        this.defaultEnvironment = defaultEnvironment;
    }

    public Environment getEnvironment() {
        return this.defaultEnvironment;
    }

    public void buildEnvironment(JndiConsumer specDD, XmlObject plan, Environment environment) throws DeploymentException {
        // TODO Currently this method is called before the xml is metadata complete, so will not contain all refs
        // Just always call mergeEnvironment until this is fixed
        //
        // if (willMergeEnvironment(specDD, plan)) {
        EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        // }
    }

    protected boolean willMergeEnvironment(JndiConsumer specDD, XmlObject plan) throws DeploymentException {
        return false;
    }

    protected boolean matchesDefaultEnvironment(Environment environment) {
        for (Iterator iterator = defaultEnvironment.getDependencies().iterator(); iterator.hasNext();) {
            Dependency defaultDependency = (Dependency) iterator.next();
            boolean matches = false;
            for (Iterator iterator1 = environment.getDependencies().iterator(); iterator1.hasNext();) {
                Dependency actualDependency = (Dependency) iterator1.next();
                if (matches(defaultDependency, actualDependency)) {
                    matches = true;
                    break;
                }
            }
            if (!matches) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(Dependency defaultDependency, Dependency actualDependency) {
        if (defaultDependency.getArtifact().matches(actualDependency.getArtifact())
                || actualDependency.getArtifact().matches(defaultDependency.getArtifact())) {
            return defaultDependency.getImportType() == actualDependency.getImportType()
                    || actualDependency.getImportType() == ImportType.ALL;
        }
        return false;
    }

    public void initContext(JndiConsumer specDD, XmlObject plan, Module module) throws DeploymentException {
    }

    public int getPriority() {
        return NORMAL_PRIORITY;
    }

    /**
     * This accepts keys like jndi entry names in the spec dds, that is either starting with
     * java:comp, java:module, java:app, or java:global, or in the java:comp/env space.
     * For example, java:comp/env/foo and foo are equivalent keys.
     *
     * @param key jndi name, either including java:<scope> or after java:comp/env.
     * @param value value to bind
     * @param contexts set of scopes to bind into.
     * @param injectionTargets
     * @param sharedContext
     */
    protected void put(String key, Object value, ReferenceType type, Map<JndiKey, Map<String, Object>> contexts, Set<InjectionTarget> injectionTargets, Map<EARContext.Key, Object> sharedContext) {
        key = normalize(key);
        JndiKey jndiKey = keyFor(key);
        Map<String, Object> scope = contexts.get(jndiKey);
        if (scope == null) {
            scope = new HashMap<String, Object>();
            contexts.put(jndiKey, scope);
        }
        if (log.isDebugEnabled()) {
            log.debug("binding at name " + key + " in scope " + jndiKey + " value " + value);
        }
        scope.put(key, value);
        addInjections(key, type, injectionTargets, NamingBuilder.INJECTION_KEY.get(sharedContext));
    }

    protected Object lookupJndiContextMap(Module module, String key) {
        key = normalize(key);
        JndiKey jndiKey = keyFor(key);
        Map<String, Object> scope = module.getJndiScope(jndiKey);
        if (scope == null) return null;
        return scope.get(key);
    }

    protected String normalize(String name) {
        if (name.startsWith("java:")) {
            return name.substring("java:".length());
        }
        throw new IllegalArgumentException("All jndi names should start with java: not " + name);
    }

    protected JndiKey keyFor(String name) {
        int pos = name.indexOf("/");
        if (pos == -1) {
            throw new IllegalArgumentException("no possible key in " + name);
        }
        String type = name.substring(0, pos);
        return JndiScope.valueOf(type);
    }

    protected boolean isSharableJndiNamespace(String name) {
        name = normalize(name);
        return name.startsWith("app/") || name.startsWith("module/") || name.startsWith("global/");
    }

    protected static String getJndiName(String name) {
        if (name.indexOf(':') == -1) {
            return "java:comp/env/" + name.trim();
        } else {
            return name.trim();
        }
    }

    protected AbstractName getGBeanName(Map<EARContext.Key, Object> sharedContext) {
        return GBEAN_NAME_KEY.get(sharedContext);
    }

    protected static QNameSet buildQNameSet(String[] eeNamespaces, String localPart) {
        Set qnames = new HashSet(eeNamespaces.length);
        for (int i = 0; i < eeNamespaces.length; i++) {
            String namespace = eeNamespaces[i];
            qnames.add(new QName(namespace, localPart));
        }
        //xmlbeans 2.0 has a bug so forArray doesn't work.  Don't know if it's fixed in later xmlbeans versions
        //return QNameSet.forArray(qnames);
        return QNameSet.forSets(null, Collections.EMPTY_SET, Collections.EMPTY_SET, qnames);
    }

    /**
     * @param xmlObjects
     * @param converter
     * @param type
     * @return
     * @throws DeploymentException
     * @deprecated
     */
    protected static XmlObject[] convert(XmlObject[] xmlObjects, NamespaceElementConverter converter, SchemaType type) throws DeploymentException {
        //bizarre ArrayStoreException if xmlObjects is loaded by the wrong classloader
        XmlObject[] converted = new XmlObject[xmlObjects.length];
        for (int i = 0; i < xmlObjects.length; i++) {
            XmlObject xmlObject = xmlObjects[i].copy();
            if (xmlObject.schemaType() != type) {
                converter.convertElement(xmlObject);
                converted[i] = xmlObject.changeType(type);
            } else {
                converted[i] = xmlObject;
            }
            try {
                XmlBeansUtil.validateDD(converted[i]);
            } catch (XmlException e) {
                throw new DeploymentException("Could not validate xmlObject of type " + type, e);
            }
        }
        return converted;

    }

    protected static <T extends XmlObject> List<T> convert(XmlObject[] xmlObjects, NamespaceElementConverter converter, Class<T> c, SchemaType type) throws DeploymentException {
        //there's probably a better way to say T extends XmlObject and get the type from that
        List<T> result = new ArrayList<T>(xmlObjects.length);
        for (XmlObject xmlObject : xmlObjects) {
            xmlObject = convert(xmlObject, converter, type);
            result.add((T) xmlObject);
        }
        return result;
    }

    protected static XmlObject convert(XmlObject xmlObject, NamespaceElementConverter converter, SchemaType type) throws DeploymentException {
        Map ns = new HashMap();
        XmlCursor cursor = xmlObject.newCursor();
        try {
            cursor.getAllNamespaces(ns);
        } finally {
            cursor.dispose();
        }
        xmlObject = xmlObject.copy();
        cursor = xmlObject.newCursor();
        cursor.toNextToken();
        try {
            for (Object o : ns.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                cursor.insertNamespace((String) entry.getKey(), (String) entry.getValue());
            }
        } finally {
            cursor.dispose();
        }

        if (xmlObject.schemaType() != type) {
            converter.convertElement(xmlObject);
            xmlObject = xmlObject.changeType(type);
        }
        try {
            XmlBeansUtil.validateDD(xmlObject);
        } catch (XmlException e) {
            throw new DeploymentException("Could not validate xmlObject of type " + type, e);
        }
        return xmlObject;
    }

    protected static String getStringValue(String s) {
        return s == null ? null : s.trim();
    }


    public static AbstractNameQuery buildAbstractNameQuery(GerPatternType pattern, String type, String moduleType, Set interfaceTypes) {
        return ENCConfigBuilder.buildAbstractNameQueryFromPattern(pattern, null, type, moduleType, interfaceTypes);
    }

    public static AbstractNameQuery buildAbstractNameQuery(Artifact configId, String module, String name, String type, String moduleType) {
        return ENCConfigBuilder.buildAbstractNameQuery(configId, module, normalizeJndiName(name), type, moduleType);
    }

    private static String normalizeJndiName(String name) {
        if (name.startsWith("java:")) {
            return name.substring(name.indexOf("/env/") + 5);
        } else {
            return name.trim();
        }
    }

    public static Class assureInterface(String interfaceName, String superInterfaceName, String interfaceType, Bundle bundle) throws DeploymentException {
        if (interfaceName == null || interfaceName.equals("")) {
            throw new DeploymentException("interface name cannot be blank");
        }
        Class clazz;
        try {
            clazz = bundle.loadClass(interfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(interfaceType + " interface class not found: " + interfaceName, e);
        }
        if (!clazz.isInterface()) {
            throw new DeploymentException(interfaceType + " interface is not an interface: " + interfaceName);
        }
        Class superInterface;
        try {
            superInterface = bundle.loadClass(superInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Class " + superInterfaceName + " could not be loaded", e);
        }
        if (!superInterface.isAssignableFrom(clazz)) {
            throw new DeploymentException(interfaceType + " interface does not extend " + superInterfaceName + ": " + interfaceName);
        }
        return clazz;
    }

    protected void addInjections(String jndiName, ReferenceType type, Set<InjectionTarget> injectionTargets, Holder holder) {
        for (InjectionTarget injectionTarget : injectionTargets) {
            String targetName = injectionTarget.getInjectionTargetName().trim();
            String targetClassName = injectionTarget.getInjectionTargetClass().trim();
            holder.addInjection(targetClassName, new Injection(targetClassName, targetName, jndiName, type));
        }
    }

    protected static Artifact[] getConfigId(Configuration localConfiguration, Configuration earConfiguration) {
        if (localConfiguration == earConfiguration) {
            return new Artifact[] {earConfiguration.getId()};
        }
        return new Artifact[] {earConfiguration.getId(),localConfiguration.getId()};
    }


    public QName getBaseQName() {
        return BASE_NAMING_QNAME;
    }

    protected String inferAndCheckType(Module module, Bundle bundle, Set<InjectionTarget> injectionTargets, String name, String typeName) throws DeploymentException {
        Class<?> type = null;
        if (typeName != null) {
            try {
                type = bundle.loadClass(typeName);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load type class for env entry named: " + name, e);
            }
        }
        for (InjectionTarget injectionTarget : injectionTargets) {
            String className = getStringValue(injectionTarget.getInjectionTargetClass());
            try {
                Class<?> clazz = bundle.loadClass(className);
                String fieldName = getStringValue(injectionTarget.getInjectionTargetName());
                Class<?> fieldType = getField(clazz, fieldName);
                type = fieldType;
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load injection target class for env entry named: " + name + " in class: " + className, e);
            } catch (NoSuchFieldException e) {
                throw new DeploymentException("could not access field for env entry named: " + name + " in class: " + className + "of type: " + type, e);
            }
        }
        if (type == null) {
            throw new DeploymentException("No way to determine type of env-entry " + name + " in component " + module.toString());
        }
        return type.getName();
    }

    private static Class<?> chooseType(String name, Class<?> originalType, Class<?> alternativeType) throws DeploymentException{
        alternativeType = deprimitivize(alternativeType);
        originalType = deprimitivize(originalType);
        if (originalType == null) {
            return alternativeType;
        } else if (!alternativeType.equals(originalType)) {
            if (alternativeType.isAssignableFrom(originalType)) {
                //originalType is subclass
                return originalType;
            } else if (originalType.isAssignableFrom(alternativeType)) {
                //alternativeType is subclass
                return alternativeType;
            } else {
                throw new DeploymentException("Mismatched types in named: " + name + " type: " + originalType );
            }
        }

        return originalType;
    }

    //duplicated in ResourceAnnotationHelper
    public static Class<?> deprimitivize(Class<?> fieldType) {
        return fieldType = fieldType.isPrimitive() ? primitives.get(fieldType): fieldType;
    }

    private static final Map<Class<?>, Class<?>> primitives = new HashMap<Class<?>, Class<?>>();

    static {
        primitives.put(boolean.class, Boolean.class);
        primitives.put(byte.class, Byte.class);
        primitives.put(char.class, Character.class);
        primitives.put(double.class, Double.class);
        primitives.put(float.class, Float.class);
        primitives.put(int.class, Integer.class);
        primitives.put(long.class, Long.class);
        primitives.put(short.class, Short.class);
    }

    private Class<?> getField(Class<?> clazz, String fieldName) throws NoSuchFieldException, DeploymentException {

        Class<?> type = null;

        do {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                Resource resource = field.getAnnotation(Resource.class);
                if (resource != null && resource.type()!=Object.class) {
                    type = chooseType(fieldName, field.getType(), resource.type());
                } else {
                    type = field.getType();
                }

            } catch (NoSuchFieldException e) {
                //look at superclass
            }
            for (Method method: clazz.getDeclaredMethods()) {
                if (method.getReturnType() == void.class && method.getParameterTypes().length == 1) {
                    String methodName = method.getName();
                    if (methodName.startsWith("set")) {
                        String setName = Introspector.decapitalize(methodName.substring(3));
                        if (fieldName.equals(setName)) {
                            Resource resource = method.getAnnotation(Resource.class);
                            if (resource != null && resource.type()!=Object.class) {
                                type = chooseType(fieldName, method.getParameterTypes()[0], resource.type());
                            } else {
                                type = method.getParameterTypes()[0];
                            }
                        }
                    }
                }
            }

            if (type != null) {
                return deprimitivize(type);
            }

            clazz = clazz.getSuperclass();
        } while (clazz != null);



        throw new NoSuchFieldException(fieldName);
    }
}


