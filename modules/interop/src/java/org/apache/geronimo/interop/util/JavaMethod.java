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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.apache.geronimo.interop.SystemException;


/**
 * * Utility methods for obtaining method signatures and calling static
 * * methods on dynamically loaded classes.
 */
public class JavaMethod {
    private static HashMap _methodMap = new HashMap();

    public static Method[] add(Method m, Method[] a) {
        Method[] b = new Method[a.length + 1];
        System.arraycopy(a, 0, b, 0, a.length);
        b[a.length] = m;
        return b;
    }

    /**
     * * Return the short signature of a method.
     * * A short signature is "method-name(parameter-type, ...)".
     */
    public static String getShortSignature(String methodName, Class[] parameterTypes) {
        StringBuffer sb = new StringBuffer();
        sb.append(methodName);
        sb.append('(');
        int n = parameterTypes.length;
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(JavaType.getName(parameterTypes[i]));
        }
        sb.append(')');
        return sb.toString();
    }

    public static String getShortSignature(Method m) {
        return getShortSignature(m.getName(), m.getParameterTypes());
    }

    /**
     * * Return the long signature of a method.
     * * A long signature is "return-type class-name.method-name(parameter-type, ...)".
     */
    public static String getLongSignature(Class returnType, String className, String methodName, Class[] parameterTypes) {
        return JavaType.getName(returnType) + " " + className + "." + getShortSignature(methodName, parameterTypes);
    }

    public static String getLongSignature(Method m) {
        return getLongSignature(m.getReturnType(), m.getDeclaringClass().getName(), m.getName(), m.getParameterTypes());
    }

    public static String getLongSignature(Class c, Method m) {
        return getLongSignature(m.getReturnType(), c.getName(), m.getName(), m.getParameterTypes());
    }

    public static Method getMethod(String methodSignature) {
        Method method = (Method) _methodMap.get(methodSignature);
        if (method == null) {
            synchronized (_methodMap) {
                method = (Method) _methodMap.get(methodSignature);
                if (method == null) {
                    int parenPos = methodSignature.indexOf('(');
                    if (parenPos == -1) {
                        throw new IllegalArgumentException("methodSignature = " + methodSignature);
                    }
                    String fullMethodName = methodSignature.substring(0, parenPos);
                    String className = JavaClass.getNamePrefix(fullMethodName);
                    String methodName = JavaClass.getNameSuffix(fullMethodName);
                    String parameters = methodSignature.substring(parenPos);
                    String shortSig = methodName + parameters;
                    Class theClass = ThreadContext.loadClass(className);
                    Method[] methods = theClass.getMethods();
                    int n = methods.length;
                    for (int i = 0; i < n; i++) {
                        method = methods[i];
                        if (shortSig.equals(JavaMethod.getShortSignature(method))) {
                            _methodMap.put(methodSignature, method);
                            break;
                        }
                    }
                }
            }
        }
        if (method == null) {
            throw new IllegalArgumentException("method = " + methodSignature + " (not found)");
        }
        return method;
    }

    public static Method getInstanceMethod(String methodSignature) {
        Method method = getMethod(methodSignature);
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("method = " + methodSignature + " (static)");
        }
        return method;
    }

    public static Method getStaticMethod(String methodSignature) {
        Method method = getMethod(methodSignature);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("method = " + methodSignature + " (not static)");
        }
        return method;
    }

    public static Object invokeStatic(String methodSignature, Object p1) {
        return invokeStatic(methodSignature, new Object[]
        {
            p1
        });
    }

    public static Object invokeStatic(String methodSignature, Object p1, Object p2) {
        return invokeStatic(methodSignature, new Object[]
        {
            p1, p2
        });
    }

    public static Object invokeStatic(String methodSignature, Object p1, Object p2, Object p3) {
        return invokeStatic(methodSignature, new Object[]
        {
            p1, p2, p3
        });
    }

    public static Object invokeStatic(String methodSignature, Object[] args) {
        try {
            return getStaticMethod(methodSignature).invoke(null, args);
        } catch (InvocationTargetException ite) {
            throw new SystemException(ite.getTargetException());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }
}
