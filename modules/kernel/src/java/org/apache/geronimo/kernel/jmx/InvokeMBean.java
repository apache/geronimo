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
 * @version $Revision: 1.2 $ $Date: 2003/11/07 18:06:02 $
 */
public final class InvokeMBean {
    private final String name;
    private final String[] argumentTypes;
    private final Class[] declaredExceptions;
    private final boolean isAttribute;
    private final boolean isGetter;
    private final int expectedArguments;

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
