/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

import java.util.HashMap;

import org.apache.geronimo.interop.SystemException;


public abstract class ThreadContext {
    private static HashMap _primTypes;

    private static ThreadLocal _defaultRmiHost = new ThreadLocal();

    private static ThreadLocal _defaultRmiPort = new ThreadLocal();

    static {
        _primTypes = new HashMap();
        _primTypes.put("boolean", boolean.class);
        _primTypes.put("char", char.class);
        _primTypes.put("byte", byte.class);
        _primTypes.put("short", short.class);
        _primTypes.put("int", int.class);
        _primTypes.put("long", long.class);
        _primTypes.put("float", float.class);
        _primTypes.put("double", double.class);
        _primTypes.put("boolean[]", boolean[].class);
        _primTypes.put("char[]", char[].class);
        _primTypes.put("byte[]", byte[].class);
        _primTypes.put("short[]", short[].class);
        _primTypes.put("int[]", int[].class);
        _primTypes.put("long[]", long[].class);
        _primTypes.put("float[]", float[].class);
        _primTypes.put("double[]", double[].class);
    }

    public static String getDefaultRmiHost() {
        String host = (String) _defaultRmiHost.get();
        if (host == null) {
            host = "0";
        }
        return host;
    }

    public static int getDefaultRmiPort() {
        Integer port = (Integer) _defaultRmiPort.get();
        if (port == null) {
            port = IntegerCache.get(0);
        }
        return port.intValue();
    }

    public static Class loadClass(String className) {
        Class t = (Class) _primTypes.get(className);
        if (t != null) {
            return t;
        }
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                return Class.forName(className);
            } else {
                return loader.loadClass(className);
            }
        } catch (RuntimeException ex) {
            throw (RuntimeException) ex;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public static Class loadClass(String className, Class parentClass) {
        if (parentClass == null) {
            return loadClass(className);
        }
        Class t = (Class) _primTypes.get(className);
        if (t != null) {
            return t;
        }
        try {
            ClassLoader loader = parentClass.getClassLoader();
            if (loader == null) {
                return loadClass(className);
            } else {
                return loader.loadClass(className);
            }
        } catch (RuntimeException ex) {
            throw (RuntimeException) ex;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public static Class loadClassOrReturnNullIfNotFound(String className) {
        try {
            return loadClass(className);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public static Class loadClassOrReturnNullIfNotFound(String className, Class parentClass) {
        if (parentClass == null) {
            return loadClassOrReturnNullIfNotFound(className);
        }
        try {
            return loadClass(className, parentClass);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    public static void setDefaultRmiHost(String host) {
        _defaultRmiHost.set(host);
    }

    public static void setDefaultRmiPort(int port) {
        _defaultRmiPort.set(IntegerCache.get(port));
    }
}
