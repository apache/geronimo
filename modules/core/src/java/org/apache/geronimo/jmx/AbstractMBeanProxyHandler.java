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
import java.util.Map;
import java.util.HashMap;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.Attribute;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;

/**
 * This class handles invocations for MBean proxies.  Normally only the getObjectName method is necessary.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/13 21:18:47 $
 */
abstract class AbstractMBeanProxyHandler implements InvocationHandler {
    protected final MBeanServer server;
    protected final Map operationMap;

    public AbstractMBeanProxyHandler(Class iface, MBeanServer server) {
        this.server = server;
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

    public abstract ObjectName getObjectName();

    protected void setAttribute(Attribute attribute) throws Exception {
        server.setAttribute(getObjectName(), attribute);
    }

    protected Object getAttribute(String attribute) throws Exception {
        return server.getAttribute(this.getObjectName(), attribute);
    }

    protected Object invokeOperation(String method, Object[] args, String[] params) throws Exception {
        return server.invoke(getObjectName(), method, args, params);
    }
}
