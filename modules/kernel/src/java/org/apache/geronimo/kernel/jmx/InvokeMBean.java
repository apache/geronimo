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

package org.apache.geronimo.kernel.jmx;

import java.lang.reflect.Method;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeErrorException;
import javax.management.Attribute;

/**
 *
 *
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:01 $
 */
public class InvokeMBean {
    private final String name;
    private final String[] argumentTypes;
    private final Class[] declaredExceptions;
    private final boolean isAttribute;
    private final boolean isGetter;
    private final int expectedArguments;

    protected InvokeMBean(String name, String[] argumentTypes, Class[] declaredExceptions, boolean attribute, boolean getter, int expectedArguments) {
        this.name = name;
        this.argumentTypes = argumentTypes;
        this.declaredExceptions = declaredExceptions;
        isAttribute = attribute;
        isGetter = getter;
        this.expectedArguments = expectedArguments;
    }

    public InvokeMBean(Method method, boolean isAttribute, boolean isGetter) {
        this.isAttribute = isAttribute;
        this.isGetter = isGetter;
        this.expectedArguments = method.getParameterTypes().length;

        if (isAttribute) {
            if (isGetter && (method.getReturnType() == Void.TYPE || method.getParameterTypes().length > 0)) {
                throw new IllegalArgumentException("Getter attribute must take no parameters and return a value");
            }
            if (!isGetter && (method.getReturnType() != Void.TYPE || method.getParameterTypes().length != 1)) {
                throw new IllegalArgumentException("Getter attribute must take one parameter and not return a value");
            }
        }

        if(isAttribute) {
            if(method.getName().startsWith("is")) {
                name = method.getName().substring(2);
            } else {
                name = method.getName().substring(3);
            }
        } else {
            name = method.getName();
        }

        // conver the parameters to a MBeanServer friendly string array
        Class[] parameters = method.getParameterTypes();
        argumentTypes = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            argumentTypes[i] = parameters[i].getName();
        }

        declaredExceptions = method.getExceptionTypes();
    }

    public Object invoke(MBeanServer server, ObjectName objectName, Object[] arguments) throws Throwable {
        if (arguments.length != expectedArguments) {
            throw new IllegalArgumentException("Wrong number of arguments:" +
                    " expected " + expectedArguments + " but got " + arguments.length);
        }
        try {
            if (isAttribute) {
                if (isGetter) {
                    return server.getAttribute(objectName, name);
                } else {
                    server.setAttribute(objectName, new Attribute(name, arguments[0]));
                    return null;
                }
            } else {
                return server.invoke(objectName, name, arguments, argumentTypes);
            }
        } catch (Throwable t) {
            Throwable throwable = t;
            while (true) {
                for (int i = 0; i < declaredExceptions.length; i++) {
                    Class declaredException = declaredExceptions[i];
                    if (declaredException.isInstance(throwable)) {
                        throw throwable;
                    }
                }

                // Unwrap the exceptions we understand
                if (throwable instanceof MBeanException) {
                    throwable = (((MBeanException) throwable).getTargetException());
                } else if (throwable instanceof ReflectionException) {
                    throwable = (((ReflectionException) throwable).getTargetException());
                } else if (throwable instanceof RuntimeOperationsException) {
                    throwable = (((RuntimeOperationsException) throwable).getTargetException());
                } else if (throwable instanceof RuntimeMBeanException) {
                    throwable = (((RuntimeMBeanException) throwable).getTargetException());
                } else if (throwable instanceof RuntimeErrorException) {
                    throwable = (((RuntimeErrorException) throwable).getTargetError());
                } else {
                    // don't know how to unwrap this, just throw it
                    throw throwable;
                }
            }
        }
    }
}
