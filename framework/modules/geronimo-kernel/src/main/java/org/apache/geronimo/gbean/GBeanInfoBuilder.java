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
package org.apache.geronimo.gbean;

import java.beans.Introspector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;

import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.Kernel;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @version $Rev$ $Date$
 */
public class GBeanInfoBuilder {
    public static GBeanInfoBuilder createStatic(Class gbeanType) {
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(gbeanType, gbeanType.getName(), gbeanType, null, null);
    }

    public static GBeanInfoBuilder createStatic(Class gbeanType, String j2eeType) {
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(gbeanType, gbeanType.getName(), gbeanType, null, j2eeType);
    }

    public static GBeanInfoBuilder createStatic(String name, Class gbeanType) {
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(gbeanType, name, gbeanType, null, null);
    }

    public static GBeanInfoBuilder createStatic(String name, Class gbeanType, String j2eeType) {
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(gbeanType, name, gbeanType, null, j2eeType);
    }

    public static GBeanInfoBuilder createStatic(Class gbeanType, GBeanInfo source) {
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(gbeanType, gbeanType.getName(), gbeanType, source, null);
    }

    public static GBeanInfoBuilder createStatic(Class gbeanType, GBeanInfo source, String j2eeType) {
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(gbeanType, gbeanType.getName(), gbeanType, source, j2eeType);
    }

    public static GBeanInfoBuilder createStatic(String name, Class gbeanType, GBeanInfo source) {
        if (name == null) throw new NullPointerException("name is null");
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(gbeanType, name, gbeanType, source, null);
    }

    //
    // These methods are used by classes that declare a GBeanInfo for another class
    //
    public static GBeanInfoBuilder createStatic(Class sourceClass, Class gbeanType) {
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(sourceClass, gbeanType.getName(), gbeanType, null, null);
    }

    public static GBeanInfoBuilder createStatic(Class sourceClass, Class gbeanType, String j2eeType) {
        if (sourceClass == null) throw new NullPointerException("sourceClass is null");
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(sourceClass, gbeanType.getName(), gbeanType, null, j2eeType);
    }

    public static GBeanInfoBuilder createStatic(Class sourceClass, Class gbeanType, GBeanInfo source, String j2eeType) {
        if (sourceClass == null) throw new NullPointerException("sourceClass is null");
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(sourceClass, gbeanType.getName(), gbeanType, source, j2eeType);
    }

    public static GBeanInfoBuilder createStatic(Class sourceClass, String name, Class gbeanType, String j2eeType) {
        if (sourceClass == null) throw new NullPointerException("sourceClass is null");
        if (name == null) throw new NullPointerException("name is null");
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return createStatic(sourceClass, name, gbeanType, null, j2eeType);
    }

    public static GBeanInfoBuilder createStatic(Class sourceClass, String name, Class gbeanType, GBeanInfo source, String j2eeType) {
        if (sourceClass == null) throw new NullPointerException("sourceClass is null");
        if (name == null) throw new NullPointerException("name is null");
        if (gbeanType == null) throw new NullPointerException("gbeanType is null");
        return new GBeanInfoBuilder(sourceClass.getName(), name, gbeanType, source, j2eeType);
    }

    public static final String DEFAULT_J2EE_TYPE = "GBean"; //NameFactory.GERONIMO_SERVICE

    private static final Class[] NO_ARGS = {};

    /**
     * The class from which the info can be retrieved using GBeanInfo.getGBeanInfo(className, classLoader)
     */
    private final String sourceClass;

    private final String name;

    private final String j2eeType;

    private final Class gbeanType;

    private final Map<String, GAttributeInfo> attributes = new HashMap<String, GAttributeInfo>();

    private GConstructorInfo constructor = new GConstructorInfo();

    private final Map<GOperationSignature, GOperationInfo> operations = new HashMap<GOperationSignature, GOperationInfo>();

    private final Map references = new HashMap();

    private final Set interfaces = new HashSet();

    private int priority = GBeanInfo.PRIORITY_NORMAL;

    private boolean osgiService;

    private Set<String> serviceInterfaces = new HashSet<String>();

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

    public GBeanInfoBuilder(String name, Class gbeanType, GBeanInfo source) {
        this(name, gbeanType, source, null);
    }

    public GBeanInfoBuilder(String name, Class gbeanType, GBeanInfo source, String j2eeType) {
        this(null, name, gbeanType, source, j2eeType);
    }

    private GBeanInfoBuilder(String sourceClass, String name, Class gbeanType, GBeanInfo source, String j2eeType) {
        checkNotNull(name);
        checkNotNull(gbeanType);
        this.name = name;
        this.gbeanType = gbeanType;
        this.sourceClass = sourceClass;

        if (source != null) {
            for (Iterator i = source.getAttributes().iterator(); i.hasNext();) {
                GAttributeInfo attributeInfo = (GAttributeInfo) i.next();
                attributes.put(attributeInfo.getName(), attributeInfo);
            }

            for (Iterator i = source.getOperations().iterator(); i.hasNext();) {
                GOperationInfo operationInfo = (GOperationInfo) i.next();
                operations.put(new GOperationSignature(operationInfo.getName(), operationInfo.getParameterList()), operationInfo);
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

            priority = source.getPriority();
            
            osgiService=source.isOsgiService();
            
            if (source.getServiceInterfaces() != null && source.getServiceInterfaces().length > 0) {
                
                serviceInterfaces.addAll(Arrays.asList(source.getServiceInterfaces()));
            }

        }
        if (j2eeType != null) {
            this.j2eeType = j2eeType;
        } else if (source != null) {
            this.j2eeType = source.getJ2eeType();
        } else {
            this.j2eeType = DEFAULT_J2EE_TYPE; //NameFactory.GERONIMO_SERVICE
        }

        // add all interfaces based on GBean type
        if (gbeanType.isArray()) {
            throw new IllegalArgumentException("GBean is an array type: gbeanType=" + gbeanType.getName());
        }
        Set<Class> allTypes = ClassLoading.getAllTypes(gbeanType);
        for (Iterator iterator = allTypes.iterator(); iterator.hasNext();) {
            Class type = (Class) iterator.next();
            addInterface(type);
        }
    }

    public void setPersistentAttributes(String[] persistentAttributes) {
        for (int i = 0; i < persistentAttributes.length; i++) {
            String attributeName = persistentAttributes[i];
            GAttributeInfo attribute = (GAttributeInfo) attributes.get(attributeName);
            if (attribute != null && !references.containsKey(attributeName)) {
                if (isMagicAttribute(attribute)) {
                    // magic attributes can't be persistent
                    continue;
                }
                attributes.put(attributeName,   
                        new GAttributeInfo(attributeName,
                                attribute.getType(), 
                                true,
                                attribute.isManageable(),
                                attribute.getEncryptedSetting(),
                                attribute.getGetterName(),
                                attribute.getSetterName()));
            } else {
                if (attributeName.equals("kernel")) {
                    addAttribute("kernel", Kernel.class, false);
                } else if (attributeName.equals("classLoader")) {
                    addAttribute("classLoader", ClassLoader.class, false);
                } else if (attributeName.equals("bundle")) {
                    addAttribute("bundle", Bundle.class, false);
                } else if (attributeName.equals("bundleContext")) {
                    addAttribute("bundleContext", BundleContext.class, false);
                } else if (attributeName.equals("abstractName")) {
                    addAttribute("abstractName", AbstractName.class, false);
                } else if (attributeName.equals("objectName")) {
                    addAttribute("objectName", String.class, false);
                }
            }
        }
    }

    public void setManageableAttributes(String[] manageableAttributes) {
        for (int i = 0; i < manageableAttributes.length; i++) {
            String attributeName = manageableAttributes[i];
            GAttributeInfo attribute = (GAttributeInfo) attributes.get(attributeName);
            if (attribute != null) {
                attributes.put(attributeName,
                        new GAttributeInfo(attributeName,
                                attribute.getType(),
                                attribute.isPersistent(),
                                true,
                                attribute.getEncryptedSetting(),
                                attribute.getGetterName(),
                                attribute.getSetterName()));
            }
        }
    }

    private boolean isMagicAttribute(GAttributeInfo attributeInfo) {
        String name = attributeInfo.getName();
        String type = attributeInfo.getType();
        return ("kernel".equals(name) && Kernel.class.getName().equals(type)) ||
                ("classLoader".equals(name) && ClassLoader.class.getName().equals(type)) ||
                ("bundle".equals(name) && Bundle.class.getName().equals(type)) ||
                ("bundleContext".equals(name) && BundleContext.class.getName().equals(type)) ||
                ("abstractName".equals(name) && AbstractName.class.getName().equals(type)) ||
                ("objectName".equals(name) && String.class.getName().equals(type));
    }

    public void addInterface(Class intf) {
        addInterface(intf, new String[0]);
    }

    //do not use beaninfo Introspector to list the properties.  This method is primarily for interfaces,
    //and it does not process superinterfaces.  It seems to really only work well for classes.
    public void addInterface(Class intf, String[] persistentAttributes) {
        addInterface(intf, persistentAttributes, new String[0]);
    }

    public void addInterface(Class intf, String[] persistentAttributes, String[] manageableAttributes) {
        Set persistentNames = new HashSet(Arrays.asList(persistentAttributes));
        Set manageableNames = new HashSet(Arrays.asList(manageableAttributes));
        Method[] methods = new Method[0];
        try {
            methods = intf.getMethods();
        } catch (NoClassDefFoundError e) {
            throw (NoClassDefFoundError)new NoClassDefFoundError("could not get methods from interface: " + intf.getName() + " due to: " + e.getMessage() + " loaded in classLoader: " + intf.getClassLoader()).initCause(e);
        }
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if ("java.lang.Object".equals(method.getDeclaringClass().getName())) continue;
            if (isGetter(method)) {
                String attributeName = getAttributeName(method);
                GAttributeInfo attribute = (GAttributeInfo) attributes.get(attributeName);
                String attributeType = method.getReturnType().getName();
                if (attribute == null) {
                    attributes.put(attributeName,
                            new GAttributeInfo(attributeName,
                                    attributeType,
                                    persistentNames.contains(attributeName),
                                    manageableNames.contains(attributeName),
                                    method.getName(),
                                    null));
                } else {
                    if (!attributeType.equals(attribute.getType())) {
                        throw new IllegalArgumentException("Getter and setter type do not match: " + attributeName + " for gbeanType: " + gbeanType.getName());
                    }
                    attributes.put(attributeName,
                            new GAttributeInfo(attributeName,
                                    attributeType,
                                    attribute.isPersistent() || persistentNames.contains(attributeName),
                                    attribute.isManageable() || manageableNames.contains(attributeName),
                                    attribute.getEncryptedSetting(),
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
                                    manageableNames.contains(attributeName),
                                    null,
                                    method.getName()));
                } else {
                    if (!attributeType.equals(attribute.getType())) {
                        throw new IllegalArgumentException("Getter and setter type do not match: " + attributeName + " for gbeanType: " + gbeanType.getName());
                    }
                    attributes.put(attributeName,
                            new GAttributeInfo(attributeName,
                                    attributeType,
                                    attribute.isPersistent() || persistentNames.contains(attributeName),
                                    attribute.isManageable() || manageableNames.contains(attributeName),
                                    attribute.getEncryptedSetting(),
                                    attribute.getGetterName(),
                                    method.getName()));
                }
            } else {
                addOperation(new GOperationInfo(method.getName(), method.getParameterTypes(), method.getReturnType().getName()));
            }
        }
        addInterface(interfaces, intf);
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
        addAttribute(name, type.getName(), persistent, true);
    }

    public void addAttribute(String name, String type, boolean persistent) {
        addAttribute(name, type, persistent, true);
    }

    public void addAttribute(String name, Class type, boolean persistent, boolean manageable) {
        addAttribute(name, type.getName(), persistent, manageable);
    }

    public void addAttribute(String name, String type, boolean persistent, boolean manageable) {
        String getter = searchForGetter(name, type, gbeanType);
        String setter = searchForSetter(name, type, gbeanType);
        addAttribute(new GAttributeInfo(name, type, persistent, manageable, getter, setter));
    }

    public void addAttribute(String name, Class type, boolean persistent, boolean manageable, boolean encrypted) {
        addAttribute(name, type.getName(), persistent, manageable, encrypted);
    }

    public void addAttribute(String name, String type, boolean persistent, boolean manageable, boolean encrypted) {
        String getter = searchForGetter(name, type, gbeanType);
        String setter = searchForSetter(name, type, gbeanType);
        addAttribute(new GAttributeInfo(name, type, persistent, manageable, encrypted, getter, setter));
    }

    public void addAttribute(GAttributeInfo info) {
        attributes.put(info.getName(), info);
    }

    public void setConstructor(GConstructorInfo constructor) {
        assert constructor != null;
        this.constructor = constructor;
        List names = constructor.getAttributeNames();
        setPersistentAttributes((String[]) names.toArray(new String[names.size()]));
    }

    public void setConstructor(String[] names) {
        constructor = new GConstructorInfo(names);
        setPersistentAttributes(names);
    }

    public void addOperation(GOperationInfo operationInfo) {
        operations.put(new GOperationSignature(operationInfo.getName(), operationInfo.getParameterList()), operationInfo);
    }
    
    /**
     * @deprecated
     */
    public void addOperation(String name) {
        // FIXME : This is needed because the getters/setters are not being added as operation
        // i.e. kerenl.invoke("getX") fails.
        addOperation(new GOperationInfo(name, NO_ARGS, ""));
    }

    /**
     * @deprecated
     */
    public void addOperation(String name, Class[] paramTypes) {
        //addOperation(new GOperationInfo(name, paramTypes, ""));
    }
   
    public void addOperation(String name, String returnType) {
        addOperation(new GOperationInfo(name, NO_ARGS, returnType));
    }

    // This is redundant because these operations are added automatically; it can be made private
    public void addOperation(String name, Class[] paramTypes, String returnType) {
        addOperation(new GOperationInfo(name, paramTypes, returnType));
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

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setOsgiService(boolean osgiService) {
        this.osgiService = osgiService;
    }

    public Set<String> getServiceInterfaces() {
        return serviceInterfaces;
    }

    public GBeanInfo getBeanInfo() {
        // get the types of the constructor args
        // this also verifies that we have a valid constructor
        Map constructorTypes = getConstructorTypes();

        // build the reference infos now that we know the constructor types
        Set referenceInfos = new HashSet();
        for (Object o : references.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
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
                        throw new InvalidConfigurationException("Reference must be a constructor argument or have a setter: name=" + referenceName + " for gbeanType: " + gbeanType);
                    }
                }
                proxyType = setter.getParameterTypes()[0].getName();

                setterName = setter.getName();
            }

            if (!proxyType.equals(Collection.class.getName()) && !proxyType.equals(referenceType)) {
                throw new InvalidConfigurationException("Reference proxy type must be Collection or " + referenceType + ": name=" + referenceName + " for gbeanType: " + gbeanType.getName());
            }

            referenceInfos.add(new GReferenceInfo(referenceName, referenceType, proxyType, setterName, namingType));
        }

        return new GBeanInfo(sourceClass, name, gbeanType.getName(), j2eeType, attributes.values(), constructor, operations.values(), referenceInfos, interfaces, priority, osgiService, serviceInterfaces.toArray(new String[serviceInterfaces.size()]));
    }

    private Map getConstructorTypes() throws InvalidConfigurationException {
        List arguments = constructor.getAttributeNames();
        String[] argumentTypes = new String[arguments.size()];
        boolean[] isReference = new boolean[arguments.size()];
        for (int i = 0; i < argumentTypes.length; i++) {
            String argumentName = (String) arguments.get(i);
            if (references.containsKey(argumentName)) {
                argumentTypes[i] = ((RefInfo) references.get(argumentName)).getJavaType();
                isReference[i] = true;
            } else if (attributes.containsKey(argumentName)) {
                GAttributeInfo attribute = (GAttributeInfo) attributes.get(argumentName);
                argumentTypes[i] = attribute.getType();
                isReference[i] = false;
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
            throw new InvalidConfigurationException("Could not find a valid constructor for GBean: " + gbeanType.getName());
        }
        if (validConstructors.size() > 1) {
            throw new InvalidConfigurationException("More then one valid constructors found for GBean: " + gbeanType.getName());
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
