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
package org.apache.geronimo.jmx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import javax.management.Attribute;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

/**
 *
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/11 17:59:12 $
 */
public class MBeanProxyFactory {
    public static Object getProxy(Class iface, MBeanServer server, ObjectName name) {
        assert (iface != null);
        assert (iface.isInterface());
        assert (server != null);

        ClassLoader cl = iface.getClassLoader();
        return Proxy.newProxyInstance(cl, new Class[]{iface}, new LocalHandler(server, name, iface));
    }

    protected abstract static class AbstractHandler implements InvocationHandler {
        protected final MBeanServer server;
        protected final ObjectName name;
        protected final Map operationMap;

        public AbstractHandler(MBeanServer server, ObjectName name, Class iface) {
            this.server = server;
            this.name = name;
            Method[] methods = iface.getMethods();
            operationMap = new HashMap(methods.length);
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                Class returnType = method.getReturnType();
                String methodName = method.getName();
                Class[] paramTypes = method.getParameterTypes();

                // skip non-operation methods
                if (methodName.startsWith("set") && returnType == Void.TYPE && paramTypes.length == 1) {
                    continue;
                } else if (methodName.startsWith("get") && paramTypes.length == 0) {
                    continue;
                } else if (methodName.startsWith("is") && returnType == Boolean.TYPE && paramTypes.length == 0) {
                    continue;
                }

                String[] paramTypeNames = new String[paramTypes.length];
                for (int j = 0; j < paramTypes.length; j++) {
                    paramTypeNames[j] = paramTypes[j].getName();
                }
                operationMap.put(method, paramTypeNames);
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                String methodName = method.getName();

                // quick check if this is an operation
                String[] params = (String[]) operationMap.get(method);
                if (params != null) {
                    return invokeOperation(methodName, args, params);
                }

                if (methodName.startsWith("set")) {
                    setAttribute(new Attribute(methodName.substring(3), args[0]));
                    return null;
                } else if (methodName.startsWith("get")) {
                    return getAttribute(methodName.substring(3));
                } else if (methodName.startsWith("is")) {
                    return getAttribute(methodName.substring(2));
                }
            } catch (Throwable t) {
                Class[] declaredEx = method.getExceptionTypes();
                Throwable tt = t;
                while (true) {
                    for (int i = 0; i < declaredEx.length; i++) {
                        Class aClass = declaredEx[i];
                        if (aClass.isInstance(tt)) {
                            throw tt;
                        }
                    }

                    if (tt instanceof MBeanException) {
                        tt = (((MBeanException) tt).getTargetException());
                    } else if (tt instanceof ReflectionException) {
                        tt = (((ReflectionException) tt).getTargetException());
                    } else if (tt instanceof RuntimeOperationsException) {
                        tt = (((RuntimeOperationsException) tt).getTargetException());
                    } else if (tt instanceof RuntimeMBeanException) {
                        tt = (((RuntimeMBeanException) tt).getTargetException());
                    } else if (tt instanceof RuntimeErrorException) {
                        tt = (((RuntimeErrorException) tt).getTargetError());
                    } else {
                        // don't know how to unwrap this, just throw it
                        throw tt;
                    }
                }
            }
            throw new AssertionError("Method did not match during invoke");
        }

        protected abstract void setAttribute(Attribute attribute) throws Exception;

        protected abstract Object getAttribute(String attribute) throws Exception;

        protected abstract Object invokeOperation(String method, Object[] args, String[] params) throws Exception;
    }

    protected static class LocalHandler extends AbstractHandler {
        public LocalHandler(MBeanServer server, ObjectName name, Class iface) {
            super(server, name, iface);
        }

        protected void setAttribute(Attribute attribute) throws Exception {
            server.setAttribute(name, attribute);
        }

        protected Object getAttribute(String attribute) throws Exception {
            return server.getAttribute(this.name, attribute);
        }

        protected Object invokeOperation(String method, Object[] args, String[] params) throws Exception {
            return server.invoke(name, method, args, params);
        }
    }
}
