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
package org.apache.geronimo.gbean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.kernel.jmx.MBeanOperationSignature;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;


/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/21 19:45:14 $
 */
public class DynamicGBeanDelegate implements DynamicGBean {
    protected final Map getters = new HashMap();
    protected final Map setters = new HashMap();
    protected final Map operations = new HashMap();

    public void addAll(Object target) {
        Class targetClass = target.getClass();
        Method[] methods = targetClass.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (isGetter(method)) {
                addGetter(target, method);
            } else if (isSetter(method)) {
                addSetter(target, method);
            } else {
                addOperation(target, method);
            }
        }
    }

    public void addGetter(Object target, Method method) {
        String name = method.getName();
        if (name.startsWith("get")) {
            addGetter(method.getName().substring(3), target, method);
        } else if (name.startsWith("is")) {
            addGetter(method.getName().substring(2), target, method);
        } else {
            throw new IllegalArgumentException("Method method name must start with 'get' or 'is' " + method);
        }
    }

    public void addGetter(String name, Object target, Method method) {
        if (!(method.getParameterTypes().length == 0 && method.getReturnType() != Void.TYPE)) {
            throw new IllegalArgumentException("Method must take no parameters and return a value " + method);
        }
        getters.put(name, new Operation(target, method));
    }

    public void addSetter(Object target, Method method) {
        if (!method.getName().startsWith("set")) {
            throw new IllegalArgumentException("Method method name must start with 'set' " + method);
        }
        addSetter(method.getName().substring(3), target, method);
    }

    public void addSetter(String name, Object target, Method method) {
        if (!(method.getParameterTypes().length == 1 && method.getReturnType() == Void.TYPE)) {
            throw new IllegalArgumentException("Method must take one parameter and not return anything " + method);
        }
        setters.put(name, new Operation(target, method));
    }

    public void addOperation(Object target, Method method) {
        Class[] parameters = method.getParameterTypes();
        String[] types = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getName();
        }
        MBeanOperationSignature key = new MBeanOperationSignature(method.getName(), types);
        operations.put(key, new Operation(target, method));
    }

    private boolean isGetter(Method method) {
        String name = method.getName();
        return (name.startsWith("get") || name.startsWith("is")) &&
                method.getParameterTypes().length == 0
                && method.getReturnType() != Void.TYPE;
    }

    private boolean isSetter(Method method) {
        return method.getName().startsWith("set") &&
                method.getParameterTypes().length == 1 &&
                method.getReturnType() == Void.TYPE;
    }

    public Object getAttribute(String name) throws Exception {
        Operation operation = (Operation) getters.get(name);
        if (operation == null) {
            throw new IllegalArgumentException("Unknown attribute " + name);
        }
        return operation.invoke(null);
    }

    public void setAttribute(String name, Object value) throws Exception {
        Operation operation = (Operation) setters.get(name);
        if (operation == null) {
            throw new IllegalArgumentException("Unknown attribute " + name);
        }
        operation.invoke(new Object[] {value});
    }

    public Object invoke(String name, Object[] arguments, String[] types) throws Exception {
        Operation operation = (Operation) operations.get(new MBeanOperationSignature(name, types));
        if (operation == null) {
            throw new IllegalArgumentException("Unknown attribute " + name);
        }
        return operation.invoke(arguments);
    }

    protected static class Operation {
        private final Object target;
        private final FastMethod method;

        public Operation(Object target, Method method) {
            this.target = target;
            this.method = FastClass.create(target.getClass()).getMethod(method);
        }

        public Object invoke(Object[] arguments) throws Exception {
            try {
                return method.invoke(target, arguments);
            } catch (InvocationTargetException e) {
                Throwable targetException = e.getTargetException();
                if (targetException instanceof Exception) {
                    throw (Exception) targetException;
                } else if (targetException instanceof Error) {
                    throw (Error) targetException;
                }
                throw e;
            }
        }
    }
}
