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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Wraps an <code>Object</code> in a <code>DynamicGBean</code> facade.
 *
 * @version $Rev$ $Date$
 */
public class DynamicGBeanDelegate implements DynamicGBean {
    
    private static final Map<Class, Class> TYPE_LOOKUP = new HashMap<Class, Class>();
    static {
        TYPE_LOOKUP.put(Byte.class, byte.class);
        TYPE_LOOKUP.put(Integer.class, int.class);
        TYPE_LOOKUP.put(Short.class, short.class);
        TYPE_LOOKUP.put(Long.class, long.class);
        TYPE_LOOKUP.put(Float.class, float.class);
        TYPE_LOOKUP.put(Double.class, double.class);
        TYPE_LOOKUP.put(Boolean.class, boolean.class);
        TYPE_LOOKUP.put(Character.class, char.class);
    }

    protected final Map<String, Operation> getters = new HashMap<String, Operation>();
    protected final Map<String, Map<Class, Operation>> setters = new HashMap<String, Map<Class, Operation>>();
    protected final Map<GOperationSignature, Operation> operations = new HashMap<GOperationSignature, Operation>();
    private Class targetClass;

    public void addAll(Object target) {
        this.targetClass = target.getClass();
        Method[] methods = targetClass.getMethods();
        for (Method method : methods) {
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
            addGetter(name.substring(3), target, method);
        } else if (name.startsWith("is")) {
            addGetter(name.substring(2), target, method);
        } else {
            throw new IllegalArgumentException("Method name must start with 'get' or 'is' " + method);
        }
    }

    public void addGetter(String name, Object target, Method method) {
        if (!(method.getParameterTypes().length == 0 && method.getReturnType() != Void.TYPE)) {
            throw new IllegalArgumentException("Method must take no parameters and return a value " + method);
        }
        getters.put(name, new Operation(target, method));
        // we want to be user-friendly so we put the attribute name in
        // the Map in both lower-case and upper-case
        getters.put(Introspector.decapitalize(name), new Operation(target, method));
    }

    public void addSetter(Object target, Method method) {
        if (!method.getName().startsWith("set")) {
            throw new IllegalArgumentException("Method name must start with 'set' " + method);
        }
        addSetter(method.getName().substring(3), target, method);
    }

    public void addSetter(String name, Object target, Method method) {
        if (!(method.getParameterTypes().length == 1 && method.getReturnType() == Void.TYPE)) {
            throw new IllegalArgumentException("Method must take one parameter and not return anything " + method);
        }
        Class type = method.getParameterTypes()[0];
        Operation operation = new Operation(target, method);
        addSetter(name, type, operation);        
        // we want to be user-friendly so we put the attribute name in
        // the Map in both lower-case and upper-case
        addSetter(Introspector.decapitalize(name), type, operation);    
    }

    private void addSetter(String name, Class type, Operation operation) {
        Map<Class, Operation> operations = setters.get(name);
        if (operations == null) {
            operations = new HashMap<Class, Operation>();
            setters.put(name, operations);
        }
        operations.put(type, operation);
    }
    
    public void addOperation(Object target, Method method) {
        Class[] parameters = method.getParameterTypes();
        String[] types = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            types[i] = parameters[i].getName();
        }
        GOperationSignature key = new GOperationSignature(method.getName(), types);
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
        Operation operation = getters.get(name);
        if (operation == null) {
            throw new IllegalArgumentException(targetClass.getName() + ": no getter for " + name);
        }
        return operation.invoke(null);
    }

    public void setAttribute(String name, Object value) throws Exception {
        Map<Class, Operation> operations = setters.get(name);
        if (operations == null) {
            throw new IllegalArgumentException(targetClass.getName() + ": no setters for " + name);
        }        
        Operation operation;
        if (operations.size() == 1) {
            operation = operations.values().iterator().next();
        } else if (value == null) {
            // TODO: use better algorithm?
            operation = operations.values().iterator().next();
        } else {
            Class valueType = value.getClass();
            // lookup using the specific type
            operation = operations.get(valueType);
            if (operation == null) {
                // if not found, check all setters if they accept the given type  
                operation = findOperation(operations, valueType);
                if (operation == null && TYPE_LOOKUP.containsKey(valueType)) {
                    // if not found, check all setters if they accept the primitive type
                    operation = findOperation(operations, TYPE_LOOKUP.get(valueType));
                }
                if (operation == null) {
                    throw new IllegalArgumentException(targetClass.getName() + ": no setter for " + name + " of type " + valueType);
                }
            }            
        }
        
        operation.invoke(new Object[]{value});
    }

    private Operation findOperation(Map<Class, Operation> operations, Class type) {
        for (Map.Entry<Class, Operation> entry : operations.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return entry.getValue();                
            }                    
        }
        return null;
    }
    
    public Object invoke(String name, Object[] arguments, String[] types) throws Exception {
        GOperationSignature signature = new GOperationSignature(name, types);
        Operation operation = operations.get(signature);
        if (operation == null) {
            throw new IllegalArgumentException(targetClass.getName() + ": no operation " + signature);
        }
        return operation.invoke(arguments);
    }

    /**
     * Gets all properties (with both a getter and setter), in the form of
     * propertyName
     * @return array of all property names
     */ 
    public String[] getProperties() {
        Set<String> one = getters.keySet();
        Set<String> two = setters.keySet();
        List<String> out = new ArrayList<String>();
        for (String name : one) {
            if (Character.isLowerCase(name.charAt(0)) && two.contains(name)) {
                out.add(name);
            }
        }
        return out.toArray(new String[out.size()]);
    }

    public Class getPropertyType(String name) {
        Operation oper = getters.get(name);
        return oper.method.getReturnType();
    }

    protected static class Operation {
        private final Object target;
        private final Method method;

        public Operation(Object target, Method method) {
            assert target != null;
            assert method != null;
            this.target = target;
            this.method = method;
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
