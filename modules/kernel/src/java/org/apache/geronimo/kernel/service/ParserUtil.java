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

package org.apache.geronimo.kernel.service;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.3 $ $Date: 2003/10/24 22:35:42 $
 */
public final class ParserUtil {
    private ParserUtil(){
    }


    private static Log log = LogFactory.getLog(ParserUtil.class);

    public static String parse(final String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input string is null");
        }

        if (log.isTraceEnabled()) {
            log.trace("Parsing input: " + input);
        }

        StringBuffer buff = new StringBuffer();

        int cur = 0;
        int prefixLoc = 0;
        int suffixLoc = 0;

        while (cur < input.length()) {
            prefixLoc = input.indexOf("${", cur);

            if (prefixLoc < 0) {
                break;
            }

            // check for a closing }
            suffixLoc = input.indexOf("}", prefixLoc);
            if (suffixLoc < 0) {
                throw new IllegalArgumentException("Input string is missing a closing '}': " + input);
            }

            // append the stuff before the property
            buff.append(input.substring(cur, prefixLoc));

            // append the property
            String propertyName = input.substring(prefixLoc + 2, suffixLoc);
            String property = System.getProperty(propertyName);
            if(property == null) {
                property = "";
            }
            buff.append(property);

            cur = suffixLoc + 1;
        }

        buff.append(input.substring(cur));

        if (log.isTraceEnabled()) {
            log.trace("Parsed result: " + buff);
        }

        return buff.toString();
    }


    private static final Class[] STRING_ARG = new Class[]{String.class};

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
    public static Class getPrimitiveType(final String name) {
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
    public static Class getVMPrimitiveType(final String name) {
        return (Class) VM_PRIMITIVES.get(name);
    }

    /** Map of primitive types to their wrapper classes */
    private static final Map PRIMITIVE_WRAPPERS = new HashMap();

    /** Setup the wrapper map. */
    static {
        PRIMITIVE_WRAPPERS.put(Boolean.TYPE, Boolean.class);
        PRIMITIVE_WRAPPERS.put(Byte.TYPE, Byte.class);
        PRIMITIVE_WRAPPERS.put(Character.TYPE, Character.class);
        PRIMITIVE_WRAPPERS.put(Double.TYPE, Double.class);
        PRIMITIVE_WRAPPERS.put(Float.TYPE, Float.class);
        PRIMITIVE_WRAPPERS.put(Integer.TYPE, Integer.class);
        PRIMITIVE_WRAPPERS.put(Long.TYPE, Long.class);
        PRIMITIVE_WRAPPERS.put(Short.TYPE, Short.class);
        PRIMITIVE_WRAPPERS.put(Void.TYPE, Void.class);
    }

    /////////////////////////////////////////////////////////////////////////
    //                            Class Loading                            //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Load a class for the given name using the context class loader.
     *
     * @see #loadClass(String,ClassLoader)
     *
     * @param className    The name of the Class to be loaded.
     * @return             The Class object for the given name.
     *
     * @throws ClassNotFoundException   Failed to load Class object.
     */
    public static Class loadClass(final String className) throws ClassNotFoundException {
        return loadClass(className, Thread.currentThread().getContextClassLoader());
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
    public static Class loadClass(final String className, final ClassLoader classLoader) throws ClassNotFoundException {
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

    public static Object getValue(ClassLoader cl, String typeName, String value, URI baseURI) throws ClassNotFoundException {
        Class type = null;
        type = loadClass(typeName, cl);
        return getValue(type, value, baseURI);
    }

    public static Object getValue(Class type, String value, URI baseURI) {
        value = parse(value);

        if (URI.class.equals(type)) {
            return baseURI.resolve(value);
        }
        if (URL.class.equals(type)) {
            try {
                return baseURI.resolve(value).toURL();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Value is not a valid URI: value=" + value);
            }
        }
        if (File.class.equals(type)) {
            return new File(baseURI.resolve(value));
        }

        // try a property editor
        PropertyEditor editor = findEditor(type);
        if (editor != null) {
            editor.setAsText(value);
            return editor.getValue();
        }

        // try a String constructor
        try {
            Constructor cons = type.getConstructor(STRING_ARG);
            return cons.newInstance(new Object[]{value});
        } catch (Exception e) {
            throw new IllegalArgumentException("Type does not have a registered property editor or a String constructor:" +
                    " type=" + type.getName());
        }
    }

    /**
     * Locate a value editor for a given target type.
     *
     * @param type   The class of the object to be edited.
     * @return       An editor for the given type or null if none was found.
     */
    public static PropertyEditor findEditor(final Class type)
    {
        if (type == null) {
            throw new IllegalArgumentException("Type is null");
        }

        PropertyEditor editor = PropertyEditorManager.findEditor(type);

        // Try to use adapter for array types
        if (editor == null && type.isArray()) {
            Class ctype = type.getComponentType();
            editor = findEditor(ctype);
            if (editor != null) {
                editor = new ArrayPropertyEditorAdapter(ctype, editor);
            }
        }

        return editor;
    }
}

