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
package org.apache.geronimo.gbean.jmx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ReflectionException;

import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.InvalidConfigurationException;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/17 00:32:10 $
 */
public class GBeanMBeanOperation {
    private final GBeanMBean gMBean;
    private final String name;
    private final List parameterTypes;
    private final MBeanOperationInfo mbeanOperationInfo;
    private final MethodInvoker methodInvoker;

    public GBeanMBeanOperation(GBeanMBean gMBean, String name, List parameterTypes, Class returnType, MethodInvoker methodInvoker) {
        this.gMBean = gMBean;
        this.name = name;
        this.parameterTypes = Collections.unmodifiableList(new ArrayList(parameterTypes));
        this.methodInvoker = methodInvoker;

        MBeanParameterInfo[] signature = new MBeanParameterInfo[parameterTypes.size()];
        for (int i = 0; i < signature.length; i++) {
            signature[i] = new MBeanParameterInfo(
                    "arg" + i,
                    (String) parameterTypes.get(i),
                    null);
        }

        mbeanOperationInfo = new MBeanOperationInfo(
                name,
                null,
                signature,
                returnType.getName(),
                MBeanOperationInfo.UNKNOWN
        );

    }

    public GBeanMBeanOperation(GBeanMBean gMBean, GOperationInfo operationInfo) throws InvalidConfigurationException {
        this.gMBean = gMBean;
        this.name = operationInfo.getName();

        // get an array of the parameter classes
        this.parameterTypes = Collections.unmodifiableList(new ArrayList(operationInfo.getParameterList()));
        Class[] types = new Class[parameterTypes.size()];
        ClassLoader classLoader = gMBean.getClassLoader();
        for (int i = 0; i < types.length; i++) {
            String type = (String) parameterTypes.get(i);
            try {
                types[i] = loadClass((String)parameterTypes.get(i), classLoader);
            } catch (ClassNotFoundException e) {
                throw new InvalidConfigurationException("Could not load operation parameter class:" +
                        " name=" + operationInfo.getName() +
                        " class=" + type);
            }
        }

        // get a method invoker for the operation
        Class returnType;
        try {
            Method javaMethod = gMBean.getType().getMethod(operationInfo.getMethodName(), types);
            returnType = javaMethod.getReturnType();
            methodInvoker = new FastMethodInvoker(javaMethod);
        } catch (Exception e) {
            throw new InvalidConfigurationException("Target does not have specified method:" +
                    " name=" + operationInfo.getName() +
                    " methodName=" + operationInfo.getMethodName() +
                    " targetClass=" + gMBean.getType().getName());
        }

        MBeanParameterInfo[] signature = new MBeanParameterInfo[parameterTypes.size()];
        for (int i = 0; i < signature.length; i++) {
            signature[i] = new MBeanParameterInfo(
                    "arg" + i,
                    (String) parameterTypes.get(i),
                    null);
        }

        mbeanOperationInfo = new MBeanOperationInfo(
                operationInfo.getName(),
                null,
                signature,
                returnType.getName(),
                MBeanOperationInfo.UNKNOWN
        );
    }

    public String getName() {
        return name;
    }

    public List getParameterTypes() {
        return parameterTypes;
    }

    public MBeanOperationInfo getMbeanOperationInfo() {
        return mbeanOperationInfo;
    }

    public Object invoke(Object[] arguments) throws MBeanException, ReflectionException {
        if (gMBean.isOffline()) {
            throw new IllegalStateException("Operations can not be called while offline");
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(gMBean.getClassLoader());
            return methodInvoker.invoke(gMBean.getTarget(), arguments);
        } catch (Throwable throwable) {
            throw new ReflectionException(new InvocationTargetException(throwable));
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    /**
     * Load a class for the given name.
     *
     * <p>Handles loading primitive types as well as VM class and array syntax.
     *
     * @param className the name of the Class to be loaded
     * @param classLoader the class loader to load the Class object from
     * @return the Class object for the given name
     *
     * @throws ClassNotFoundException   if classloader could not locate the specified class
     */
    private static Class loadClass(final String className, final ClassLoader classLoader) throws ClassNotFoundException {
        if (className == null) {
            throw new IllegalArgumentException("Class name is null");
        }
        if (classLoader == null) {
            throw new IllegalArgumentException("Class loader is null");
        }

        // First just try to load
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException ignore) {
            // handle special cases below
        }

        Class type = null;

        // Check if it is a primitive type
        type = getPrimitiveType(className);
        if (type != null) return type;

        // Check if it is a vm primitive
        type = getVMPrimitiveType(className);
        if (type != null) return type;

        // Handle VM class syntax (Lclassname;)
        if (className.charAt(0) == 'L' && className.charAt(className.length() - 1) == ';') {
            return classLoader.loadClass(className.substring(1, className.length() - 1));
        }

        // Handle VM array syntax ([type)
        if (className.charAt(0) == '[') {
            int arrayDimension = className.lastIndexOf('[') + 1;
            String componentClassName = className.substring(arrayDimension, className.length());
            type = loadClass(componentClassName, classLoader);

            int dim[] = new int[arrayDimension];
            java.util.Arrays.fill(dim, 0);
            return Array.newInstance(type, dim).getClass();
        }

        // Handle user friendly type[] syntax
        if (className.endsWith("[]")) {
            // get the base component class name and the arrayDimensions
            int arrayDimension = 0;
            String componentClassName = className;
            while (componentClassName.endsWith("[]")) {
                componentClassName = componentClassName.substring(0, componentClassName.length() - 2);
                arrayDimension++;
            }

            // load the base type
            type = loadClass(componentClassName, classLoader);

            // return the array type
            int[] dim = new int[arrayDimension];
            java.util.Arrays.fill(dim, 0);
            return Array.newInstance(type, dim).getClass();
        }

        // Else we can not load (give up)
        throw new ClassNotFoundException(className);
    }

    /** Primitive type name -> class map. */
    private static final Map PRIMITIVES = new HashMap();

    /** Setup the primitives map. */
    static {
        PRIMITIVES.put("boolean", Boolean.TYPE);
        PRIMITIVES.put("byte", Byte.TYPE);
        PRIMITIVES.put("char", Character.TYPE);
        PRIMITIVES.put("short", Short.TYPE);
        PRIMITIVES.put("int", Integer.TYPE);
        PRIMITIVES.put("long", Long.TYPE);
        PRIMITIVES.put("float", Float.TYPE);
        PRIMITIVES.put("double", Double.TYPE);
        PRIMITIVES.put("void", Void.TYPE);
    }

    /**
     * Get the primitive type for the given primitive name.
     *
     * @param name    Primitive type name (boolean, byte, int, ...)
     * @return        Primitive type or null.
     */
    private static Class getPrimitiveType(final String name) {
        return (Class) PRIMITIVES.get(name);
    }

    /** VM primitive type name -> primitive type */
    private static final HashMap VM_PRIMITIVES = new HashMap();

    /** Setup the vm primitives map. */
    static {
        VM_PRIMITIVES.put("B", byte.class);
        VM_PRIMITIVES.put("C", char.class);
        VM_PRIMITIVES.put("D", double.class);
        VM_PRIMITIVES.put("F", float.class);
        VM_PRIMITIVES.put("I", int.class);
        VM_PRIMITIVES.put("J", long.class);
        VM_PRIMITIVES.put("S", short.class);
        VM_PRIMITIVES.put("Z", boolean.class);
        VM_PRIMITIVES.put("V", void.class);
    }

    /**
     * Get the primitive type for the given VM primitive name.
     *
     * <p>Mapping:
     * <pre>
     *   B - byte
     *   C - char
     *   D - double
     *   F - float
     *   I - int
     *   J - long
     *   S - short
     *   Z - boolean
     *   V - void
     * </pre>
     *
     * @param name    VM primitive type name (B, C, J, ...)
     * @return        Primitive type or null.
     */
    private static Class getVMPrimitiveType(final String name) {
        return (Class) VM_PRIMITIVES.get(name);
    }
}
