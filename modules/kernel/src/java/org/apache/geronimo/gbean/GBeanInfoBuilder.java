/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.gbean;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @version $Rev$ $Date$
 */
public class GBeanInfoBuilder {

    public static final String DEFAULT_J2EE_TYPE = "GBean"; //NameFactory.GERONIMO_SERVICE

    private static final Class[] NO_ARGS = {};

    private final String name;

    private final String j2eeType;

    private final Class gbeanType;

    private final Map attributes = new HashMap();

    private GConstructorInfo constructor = new GConstructorInfo();

    private final Map operations = new HashMap();

    private final Map references = new HashMap();

    private final Set interfaces = new HashSet();

    public GBeanInfoBuilder(Class gbeanType) {
        this(checkNotNull(gbeanType).getName(), gbeanType, null, null);
    }

    public GBeanInfoBuilder(Class gbeanType, String j2eeType) {
        this(checkNotNull(gbeanType).getName(), gbeanType, null, j2eeType);
    }

    public GBeanInfoBuilder(String name, Class gbeanType) {
        this(name, checkNotNull(gbeanType), null, null);
    }

    public GBeanInfoBuilder(String name, Class gbeanType, String j2eeType) {
        this(name, checkNotNull(gbeanType), null, j2eeType);
    }

    public GBeanInfoBuilder(Class gbeanType, GBeanInfo source) {
        this(checkNotNull(gbeanType).getName(), gbeanType, source);
    }

    public GBeanInfoBuilder(Class gbeanType, GBeanInfo source, String j2eeType) {
        this(checkNotNull(gbeanType).getName(), gbeanType, source, j2eeType);
    }

    //TODO this is not used, shall we remove it?
    public GBeanInfoBuilder(String name, ClassLoader classLoader) {
        this(checkNotNull(name), loadClass(classLoader, name), GBeanInfo.getGBeanInfo(name, classLoader));
    }

    public GBeanInfoBuilder(String name, Class gbeanType, GBeanInfo source) {
        this(name, gbeanType, source, null);
    }

    public GBeanInfoBuilder(String name, Class gbeanType, GBeanInfo source, String j2eeType) {
        checkNotNull(name);
        checkNotNull(gbeanType);
        this.name = name;
        this.gbeanType = gbeanType;
        if (source != null) {
            for (Iterator i = source.getAttributes().iterator(); i.hasNext();) {
                GAttributeInfo attributeInfo = (GAttributeInfo) i.next();
                attributes.put(attributeInfo.getName(), attributeInfo);
            }

            for (Iterator i = source.getOperations().iterator(); i.hasNext();) {
                GOperationInfo operationInfo = (GOperationInfo) i.next();
                operations.put(new GOperationSignature(operationInfo.getName(),
                        operationInfo.getParameterList()), operationInfo);
            }

            for (Iterator iterator = source.getReferences().iterator(); iterator.hasNext();) {
                GReferenceInfo referenceInfo = (GReferenceInfo) iterator.next();
                references.put(referenceInfo.getName(), new RefInfo(referenceInfo.getReferenceType(), referenceInfo.getNameTypeName()));
            }

            for (Iterator iterator = source.getInterfaces().iterator(); iterator.hasNext();) {
                String intf = (String) iterator.next();
                interfaces.add(intf);
            }

            //in case subclass constructor has same parameters as superclass.
            constructor = source.getConstructor();
        }
        if (j2eeType != null) {
            this.j2eeType = j2eeType;
        } else if (source != null) {
            this.j2eeType = source.getJ2eeType();
        } else {
            this.j2eeType = DEFAULT_J2EE_TYPE; //NameFactory.GERONIMO_SERVICE
        }
    }

    public void addInterface(Class intf) {
        addInterface(intf, new String[0]);
    }

    //do not use beaninfo Introspector to list the properties.  This method is primarily for interfaces,
    //and it does not process superinterfaces.  It seems to really only work well for classes.
    public void addInterface(Class intf, String[] persistentAttributes) {
        Set persistentNames = new HashSet(Arrays.asList(persistentAttributes));
        Method[] methods = intf.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (isGetter(method)) {
                String attributeName = getAttributeName(method);
                GAttributeInfo attribute = (GAttributeInfo) attributes.get(attributeName);
                String attributeType = method.getReturnType().getName();
                if (attribute == null) {
                    attributes.put(attributeName,
                            new GAttributeInfo(attributeName,
                                    attributeType,
                                    persistentNames.contains(attributeName),
                                    method.getName(),
                                    null));
                } else {
                    if (!attributeType.equals(attribute.getType())) {
                        throw new IllegalArgumentException("Getter and setter type do not match: " + attributeName);
                    }
                    attributes.put(attributeName,
                            new GAttributeInfo(attributeName,
                                    attributeType,
                                    attribute.isPersistent(),
                                    method.getName(),
                                    attribute.getSetterName()));
                }
            } else if (isSetter(method)) {
                String attributeName = getAttributeName(method);
                String attributeType = method.getParameterTypes()[0].getName();
                GAttributeInfo attribute = (GAttributeInfo) attributes.get(attributeName);
                if (attribute == null) {
                    attributes.put(attributeName,
                            new GAttributeInfo(attributeName,
                                    attributeType,
                                    persistentNames.contains(attributeName),
                                    null,
                                    method.getName()));
                } else {
                    if (!attributeType.equals(attribute.getType())) {
                        throw new IllegalArgumentException("Getter and setter type do not match: " + attributeName);
                    }
                    attributes.put(attributeName,
                            new GAttributeInfo(attributeName,
                                    attributeType,
                                    attribute.isPersistent(),
                                    attribute.getGetterName(),
                                    method.getName()));
                }
            } else {
                addOperation(new GOperationInfo(method.getName(), method.getParameterTypes()));
            }
        }
        if(intf.isInterface()) {
            addInterface(interfaces, intf);
        }
    }

    private static void addInterface(Set set, Class intf) {
        String name = intf.getName();
        if(set.contains(name)) {
            return;
        }
        set.add(name);
        Class cls[] = intf.getInterfaces();
        for (int i = 0; i < cls.length; i++) {
            addInterface(set, cls[i]);
        }
    }

    public void addAttribute(String name, Class type, boolean persistent) {
        addAttribute(name, type.getName(), persistent);
    }

    public void addAttribute(String name, String type, boolean persistent) {
        String getter = searchForGetter(name, type, gbeanType);
        String setter = searchForSetter(name, type, gbeanType);
        addAttribute(new GAttributeInfo(name, type, persistent, getter, setter));
    }

    public void addAttribute(GAttributeInfo info) {
        attributes.put(info.getName(), info);
    }

    public void setConstructor(GConstructorInfo constructor) {
        assert constructor != null;
        this.constructor = constructor;
    }

    public void setConstructor(String[] names) {
        constructor = new GConstructorInfo(names);
    }

    public void addOperation(GOperationInfo operationInfo) {
        operations.put(new GOperationSignature(operationInfo.getName(), operationInfo.getParameterList()), operationInfo);
    }

    public void addOperation(String name) {
        addOperation(new GOperationInfo(name, NO_ARGS));
    }

    public void addOperation(String name, Class[] paramTypes) {
        addOperation(new GOperationInfo(name, paramTypes));
    }

    public void addReference(GReferenceInfo info) {
        references.put(info.getName(), new RefInfo(info.getReferenceType(), info.getNameTypeName()));
    }

    /**
     * Add a reference to another GBean or collection of GBeans
     * @param name the name of the reference
     * @param type The proxy type of the GBean or objects in a ReferenceCollection
     * @param namingType the string expected as the type component of the name.  For jsr-77 names this is the j2eeType value
     */
    public void addReference(String name, Class type, String namingType) {
        references.put(name, new RefInfo(type.getName(), namingType));
    }

    public void addReference(String name, Class type) {
        references.put(name, new RefInfo(type.getName(), null));
    }

    public GBeanInfo getBeanInfo() {
        // get the types of the constructor args
        // this also verifies that we have a valid constructor
        Map constructorTypes = getConstructorTypes();

        // build the reference infos now that we know the constructor types
        Set referenceInfos = new HashSet();
        for (Iterator iterator = references.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String referenceName = (String) entry.getKey();
            RefInfo refInfo = (RefInfo) entry.getValue();
            String referenceType = refInfo.getJavaType();
            String namingType = refInfo.getNamingType();

            String proxyType = (String) constructorTypes.get(referenceName);
            String setterName = null;
            if (proxyType == null) {
                Method setter = searchForSetterMethod(referenceName, referenceType, gbeanType);
                if (setter == null) {
                    setter = searchForSetterMethod(referenceName, Collection.class.getName(), gbeanType);
                    if (setter == null) {
                        throw new InvalidConfigurationException("Reference must be a constructor argument or have a setter: name=" + referenceName);
                    }
                }
                proxyType = setter.getParameterTypes()[0].getName();

                setterName = setter.getName();
            }

            if (!proxyType.equals(Collection.class.getName()) && !proxyType.equals(referenceType)) {
                throw new InvalidConfigurationException("Reference proxy type must be Collection or " + referenceType + ": name=" + referenceName);
            }

            referenceInfos.add(new GReferenceInfo(referenceName, referenceType, proxyType, setterName, namingType));
        }


        return new GBeanInfo(name, gbeanType.getName(), j2eeType, attributes.values(), constructor, operations.values(), referenceInfos, interfaces);
    }

    private Map getConstructorTypes() throws InvalidConfigurationException {
        List arguments = constructor.getAttributeNames();
        String[] argumentTypes = new String[arguments.size()];
        boolean[] isReference = new boolean[arguments.size()];
        for (int i = 0; i < argumentTypes.length; i++) {
            String argumentName = (String) arguments.get(i);
            if (attributes.containsKey(argumentName)) {
                GAttributeInfo attribute = (GAttributeInfo) attributes.get(argumentName);
                argumentTypes[i] = attribute.getType();
                isReference[i] = false;
            } else if (references.containsKey(argumentName)) {
                argumentTypes[i] = ((RefInfo) references.get(argumentName)).getJavaType();
                isReference[i] = true;
            }
        }

        Constructor[] constructors = gbeanType.getConstructors();
        Set validConstructors = new HashSet();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            if (isValidConstructor(constructor, argumentTypes, isReference)) {
                validConstructors.add(constructor);
            }
        }

        if (validConstructors.isEmpty()) {
            throw new InvalidConfigurationException("Could not find a valid constructor for GBean: " + name);
        }
        if (validConstructors.size() > 1) {
            throw new InvalidConfigurationException("More then one valid constructors found for GBean: " + name);
        }

        Map constructorTypes = new HashMap();
        Constructor constructor = (Constructor) validConstructors.iterator().next();
        Class[] parameterTypes = constructor.getParameterTypes();
        Iterator argumentIterator = arguments.iterator();
        for (int i = 0; i < parameterTypes.length; i++) {
            String parameterType = parameterTypes[i].getName();
            String argumentName = (String) argumentIterator.next();
            constructorTypes.put(argumentName, parameterType);
        }
        return constructorTypes;
    }

    private static String searchForGetter(String name, String type, Class gbeanType) throws InvalidConfigurationException {
        Method getterMethod = null;

        // no explicit name give so we must search for a name
        String getterName = "get" + name;
        String isName = "is" + name;
        Method[] methods = gbeanType.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getParameterTypes().length == 0 && methods[i].getReturnType() != Void.TYPE
                    && (getterName.equalsIgnoreCase(methods[i].getName()) || isName.equalsIgnoreCase(methods[i].getName()))) {

                // found it
                getterMethod = methods[i];
                break;
            }
        }

        // if the return type of the getter doesn't match, throw an exception
        if (getterMethod != null && !type.equals(getterMethod.getReturnType().getName())) {
            throw new InvalidConfigurationException("Incorrect return type for getter method:" +
                    " name=" + name +
                    ", targetClass=" + gbeanType.getName() +
                    ", getter type=" + getterMethod.getReturnType() +
                    ", expected type=" + type);
        }

        if (getterMethod == null) {
            return null;
        }
        return getterMethod.getName();
    }

    private static String searchForSetter(String name, String type, Class gbeanType) throws InvalidConfigurationException {
        Method method = searchForSetterMethod(name, type, gbeanType);
        if (method == null) {
            return null;
        }
        return method.getName();
    }

    private static Method searchForSetterMethod(String name, String type, Class gbeanType) throws InvalidConfigurationException {
        // no explicit name give so we must search for a name
        String setterName = "set" + name;
        Method[] methods = gbeanType.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getParameterTypes().length == 1 &&
                    method.getParameterTypes()[0].getName().equals(type) &&
                    method.getReturnType() == Void.TYPE &&
                    setterName.equalsIgnoreCase(method.getName())) {

                return method;
            }
        }

        // a setter is not necessary for this attribute
        return null;
    }

    private static boolean isValidConstructor(Constructor constructor, String[] argumentTypes, boolean[] isReference) {
        Class[] parameterTypes = constructor.getParameterTypes();

        // same number of parameters?
        if (parameterTypes.length != argumentTypes.length) {
            return false;
        }

        // is each parameter the correct type?
        for (int i = 0; i < parameterTypes.length; i++) {
            String parameterType = parameterTypes[i].getName();
            if (isReference[i]) {
                // reference: does type match
                // OR is it a java.util.Collection
                // OR is it a java.util.Set?
                if (!parameterType.equals(argumentTypes[i]) &&
                        !parameterType.equals(Collection.class.getName()) &&
                        !parameterType.equals(Set.class.getName())) {
                    return false;
                }
            } else {
                // attribute: does type match?
                if (!parameterType.equals(argumentTypes[i])) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getAttributeName(Method method) {
        String name = method.getName();
        String attributeName = (name.startsWith("get") || name.startsWith("set")) ? name.substring(3) : name.substring(2);
        attributeName = Introspector.decapitalize(attributeName);
        return attributeName;
    }

    private boolean isSetter(Method method) {
        return method.getName().startsWith("set") && method.getParameterTypes().length == 1;
    }

    private static boolean isGetter(Method method) {
        String name = method.getName();
        return (name.startsWith("get") || name.startsWith("is")) && method.getParameterTypes().length == 0;
    }

    /**
     * Checks whether or not the input argument is null; otherwise it throws
     * {@link IllegalArgumentException}.
     *
     * @param clazz the input argument to validate
     * @throws IllegalArgumentException if input is null
     */
    private static Class checkNotNull(final Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("null argument supplied");
        }
        return clazz;
    }

    /**
     * Checks whether or not the input argument is null; otherwise it throws
     * {@link IllegalArgumentException}.
     *
     * @param string the input argument to validate
     * @throws IllegalArgumentException if input is null
     */
    private static String checkNotNull(final String string) {
        if (string == null) {
            throw new IllegalArgumentException("null argument supplied");
        }
        return string;
    }

    private static Class loadClass(ClassLoader classLoader, String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load class " + name, e);
        }
    }

    private static class RefInfo {
        private final String javaType;
        private final String namingType;

        public RefInfo(String javaType, String namingType) {
            this.javaType = javaType;
            this.namingType = namingType;
        }

        public String getJavaType() {
            return javaType;
        }

        public String getNamingType() {
            return namingType;
        }
    }
}
