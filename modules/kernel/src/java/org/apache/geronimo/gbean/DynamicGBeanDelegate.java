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
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:00 $
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
