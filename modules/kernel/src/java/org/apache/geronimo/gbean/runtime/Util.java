/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.gbean.runtime;

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

import org.apache.geronimo.gbean.DynamicGAttributeInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.management.NotificationType;

/**
 * @version $Rev$ $Date$
 */
public final class Util {
    private Util() { }

    public static GBeanInfo perfect(GBeanInfo source, ClassLoader classLoader) {
        Class gbeanType;
        try {
            gbeanType = ClassLoading.loadClass(source.getClassName(), classLoader);
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load attribute class: " + source.getClassName());
        }

        Set attributes = new HashSet();
        for (Iterator iterator = source.getAttributes().iterator(); iterator.hasNext();) {
            GAttributeInfo attributeInfo = (GAttributeInfo) iterator.next();
            attributes.add(perfect(attributeInfo, gbeanType, classLoader));
        }

        Set operations = new HashSet();
        for (Iterator iterator = source.getOperations().iterator(); iterator.hasNext();) {
            GOperationInfo operationInfo = (GOperationInfo) iterator.next();
            operations.add(perfect(operationInfo));
        }

        Set constructorArgs = new HashSet(source.getConstructor().getAttributeNames());
        Set references = new HashSet();
        for (Iterator iterator = source.getReferences().iterator(); iterator.hasNext();) {
            GReferenceInfo referenceInfo = (GReferenceInfo) iterator.next();
            references.add(perfect(referenceInfo, gbeanType, constructorArgs.contains(referenceInfo.getName())));
        }

        return new GBeanInfo(source.getName(),
                source.getClassName(),
                attributes,
                source.getConstructor(),
                operations,
                references,
                new HashSet(Arrays.asList(NotificationType.TYPES)));
    }

    private static GAttributeInfo perfect(GAttributeInfo attributeInfo, Class gbeanType, ClassLoader classLoader) {
        String name = attributeInfo.getName();

        Class type;
        try {
            type = ClassLoading.loadClass(attributeInfo.getType(), classLoader);
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load attribute class: " + attributeInfo.getType());
        }
        boolean persistent = attributeInfo.isPersistent();

        // If attribute is persistent or not tagged as unreadable, search for a
        // getter method
        if (attributeInfo instanceof DynamicGAttributeInfo) {
            return new DynamicGAttributeInfo(name, attributeInfo.getType(), persistent, attributeInfo.isReadable().booleanValue(), attributeInfo.isWritable().booleanValue());
        }


        Method getterMethod = searchForGetter(gbeanType, attributeInfo, type);
        boolean readable;
        String getterName;
        if (getterMethod != null) {
            getterName = getterMethod.getName();
            // this attribute is readable as long as it was not explicitly
            // tagged as unreadable
            readable = attributeInfo.isReadable() != Boolean.FALSE;
        } else {
            getterName = null;
            readable = false;
        }

        // If attribute is persistent or not tagged as unwritable, search
        // for a setter method
        Method setterMethod = searchForSetter(gbeanType, attributeInfo, type);
        boolean writable;
        String setterName;
        if (setterMethod != null) {
            setterName = setterMethod.getName();

            // this attribute is writable as long as it was not explicitly
            // tagged as unwritable
            writable = attributeInfo.isWritable() != Boolean.FALSE;
        } else {
            setterName = null;
            writable = false;
        }

        return new GAttributeInfo(name, attributeInfo.getType(), persistent, Boolean.valueOf(readable), Boolean.valueOf(writable), getterName, setterName);
    }

    private static GOperationInfo perfect(GOperationInfo operationInfo) {
        return new GOperationInfo(operationInfo.getName(), operationInfo.getParameterList());
    }

    private static GReferenceInfo perfect(GReferenceInfo referenceInfo, Class gbeanType, boolean isConstructorArg) {
        String name = referenceInfo.getName();
        String setterName;
        if (isConstructorArg) {
            setterName = null;
        } else {
            Method setterMethod = searchForSetter(gbeanType, referenceInfo);
            setterName = setterMethod.getName();
        }

        return new GReferenceInfo(name, referenceInfo.getType(), setterName);
    }

    public static boolean isCollectionValuedReference(GBeanInstance gbeanMBean, GReferenceInfo referenceInfo, Class constructorType) {
        if (constructorType != null) {
            return Collection.class == constructorType;
        } else {
            Method setterMethod = searchForSetter(gbeanMBean.getType(), referenceInfo);
            return Collection.class == setterMethod.getParameterTypes()[0];
        }
    }

    /**
     * Search for a single valid constructor in the class.  A valid constructor is determined by the
     * attributes and references declared in the GBeanInfo.  For each, constructor gbean attribute
     * the parameter must have the exact same type.  For a constructor gbean reference parameter, the
     * parameter type must either match the reference proxy type, be java.util.Collection, or be
     * java.util.Set.
     *
     * @param beanInfo the metadata describing the constructor, attrbutes and references
     * @param type the target type in which we search for a constructor
     * @return the sole matching constructor
     * @throws org.apache.geronimo.gbean.InvalidConfigurationException if there are no valid constructors or more then one valid
     * constructors; multiple constructors can match in the case of a gbean reference parameter
     */
    public static Constructor searchForConstructor(GBeanInfo beanInfo, Class type) throws InvalidConfigurationException {
        Set attributes = beanInfo.getAttributes();
        Map attributeTypes = new HashMap(attributes.size());
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            GAttributeInfo attribute = (GAttributeInfo) iterator.next();
            attributeTypes.put(attribute.getName(), attribute.getType());
        }

        Set references = beanInfo.getReferences();
        Map referenceTypes = new HashMap(references.size());
        for (Iterator iterator = references.iterator(); iterator.hasNext();) {
            GReferenceInfo reference = (GReferenceInfo) iterator.next();
            referenceTypes.put(reference.getName(), reference.getType());
        }

        List arguments = beanInfo.getConstructor().getAttributeNames();
        String[] argumentTypes = new String[arguments.size()];
        boolean[] isReference = new boolean[arguments.size()];
        for (int i = 0; i < argumentTypes.length; i++) {
            String argumentName = (String) arguments.get(i);
            if (attributeTypes.containsKey(argumentName)) {
                argumentTypes[i] = (String) attributeTypes.get(argumentName);
                isReference[i] = false;
            } else if (referenceTypes.containsKey(argumentName)) {
                argumentTypes[i] = (String) referenceTypes.get(argumentName);
                isReference[i] = true;
            }
        }

        Constructor[] constructors = type.getConstructors();
        Set validConstructors = new HashSet();
        for (int i = 0; i < constructors.length; i++) {
            Constructor constructor = constructors[i];
            if (isValidConstructor(constructor, argumentTypes, isReference)) {
                validConstructors.add(constructor);
            }
        }

        if (validConstructors.isEmpty()) {
            throw new InvalidConfigurationException("Could not find a valid constructor for GBean: " + beanInfo.getName());
        }
        if (validConstructors.size() > 1) {
            throw new InvalidConfigurationException("More then one valid constructors found for GBean: " + beanInfo.getName());
        }
        return (Constructor) validConstructors.iterator().next();
    }

    /**
     * Is this a valid constructor for the GBean.  This is determined based on the argument types and
     * if an argument is a reference, as determined by the boolean array, the argument may also be
     * java.util.Collection or java.util.Set.
     *
     * @param constructor the class constructor
     * @param argumentTypes types of the attributes and references
     * @param isReference if the argument is a gbean reference
     * @return true if this is a valid constructor for gbean; false otherwise
     */
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

    public static Method searchForGetter(Class gbeanType, GAttributeInfo attributeInfo, Class type) throws InvalidConfigurationException {
        Method getterMethod = null;
        if (attributeInfo.getGetterName() == null) {
            // no explicit name give so we must search for a name
            String getterName = "get" + attributeInfo.getName();
            String isName = "is" + attributeInfo.getName();
            Method[] methods = gbeanType.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getParameterTypes().length == 0 && methods[i].getReturnType() != Void.TYPE
                        && (getterName.equalsIgnoreCase(methods[i].getName()) || isName.equalsIgnoreCase(methods[i].getName()))) {

                    // found it
                    getterMethod = methods[i];
                    break;
                }
            }
        } else {
            // we have an explicit name, so no searching is necessary
            try {
                getterMethod = gbeanType.getMethod(attributeInfo.getGetterName(), null);
                if (getterMethod.getReturnType() == Void.TYPE) {
                    throw new InvalidConfigurationException("Getter method return VOID:" +
                            " name=" + attributeInfo.getName() +
                            ", type=" + type.getName() +
                            ", targetClass=" + gbeanType.getName());
                }
            } catch (Exception e) {
                // we will throw the formatted exception below
            }
        }

        // if the return type of the getter doesn't match, throw an exception
        if (getterMethod != null && !type.equals(getterMethod.getReturnType())) {
            throw new InvalidConfigurationException("Incorrect return type for getter method:" +
                    " name=" + attributeInfo.getName() +
                    ", targetClass=" + gbeanType.getName() +
                    ", getter type=" + getterMethod.getReturnType() +
                    ", expected type=" + type.getName());
        }

        return getterMethod;
    }

    public static Method searchForSetter(Class gbeanType, GAttributeInfo attributeInfo, Class type) throws InvalidConfigurationException {
        if (attributeInfo.getSetterName() == null) {
            // no explicit name give so we must search for a name
            String setterName = "set" + attributeInfo.getName();
            Method[] methods = gbeanType.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getParameterTypes()[0].equals(type) &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equalsIgnoreCase(method.getName())) {

                    return method;
                }
            }
        } else {
            // we have an explicit name, so no searching is necessary
            try {
                Method method = gbeanType.getMethod(attributeInfo.getSetterName(), new Class[]{type});
                if (method.getReturnType() != Void.TYPE) {
                    throw new InvalidConfigurationException("Setter method must return VOID:" +
                            " name=" + attributeInfo.getName() +
                            ", type=" + type.getName() +
                            ", targetClass=" + gbeanType.getName());
                }
                return method;
            } catch (Exception e) {
                // we will throw the formatted exception below
            }
        }

        // a setter is not necessary for this attribute
        return null;
    }

    public static Method searchForSetter(Class gbeanType, GReferenceInfo referenceInfo) throws InvalidConfigurationException {
        if (referenceInfo.getSetterName() == null) {
            // no explicit name give so we must search for a name
            String setterName = "set" + referenceInfo.getName();
            Method[] methods = gbeanType.getMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equalsIgnoreCase(method.getName())) {

                    return method;
                }
            }
        } else {
            // even though we have an exact name we need to search the methods because
            // we don't know the parameter type
            Method[] methods = gbeanType.getMethods();
            String setterName = referenceInfo.getSetterName();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getParameterTypes().length == 1 &&
                        method.getReturnType() == Void.TYPE &&
                        setterName.equals(method.getName())) {

                    return method;
                }
            }
        }
        throw new InvalidConfigurationException("Target does not have specified method:" +
                " name=" + referenceInfo.getName() +
                " targetClass=" + gbeanType.getName());
    }
}
