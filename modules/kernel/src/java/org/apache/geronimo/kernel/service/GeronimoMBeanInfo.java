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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;

import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Callbacks;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.SimpleCallbacks;
import net.sf.cglib.reflect.FastClass;

/**
 * Describes a GeronimoMBean.  This extension allows the properties to be mutable during setup,
 * and once the MBean is deployed an imutable copy of will be made.  This class also adds support for multi target
 * POJOs under the MBean.
 *
 * @version $Revision: 1.13 $ $Date: 2003/12/30 08:25:32 $
 */
public final class GeronimoMBeanInfo extends MBeanInfo {

    public static final String ALWAYS = "always";
    public static final String NEVER = "never";
    /**
     * The key for the default target
     */
    final static String DEFAULT_TARGET_NAME = "default";

    /**
     * The key for the geronimo mbean
     */
    final static String GERONIMO_MBEAN_TARGET_NAME = "___Geronimo___MBean___";

    private static final MBeanConstructorInfo[] NO_CONSTRUCTORS = new MBeanConstructorInfo[0];

    private boolean autostart = false;
    //private
    final boolean immutable;
    private final int hashCode = System.identityHashCode(this);
    private String name;
    private String description;
    private final Map targetClasses = new HashMap();
    private final Set attributes = new HashSet();
    private final Set operations = new HashSet();
    private final Set notifications = new HashSet();
    private final Set endpoints = new HashSet();
    final Map targets = new HashMap();
    final Map targetFastClasses = new HashMap();

    public GeronimoMBeanInfo() {
        // first aregument must be non-nul until MX4J snapshot is updated
        super("Ignore", null, null, null, null, null);
        immutable = false;
    }

    GeronimoMBeanInfo(GeronimoMBeanInfo source) throws Exception {
        super("Ignore", null, null, null, null, null);
        immutable = true;
        autostart = source.autostart;

        //
        // Required
        //
        if (source.targetClasses.get(DEFAULT_TARGET_NAME) == null) {
            throw new IllegalStateException("No default target specified");
        }
        // we can just put all because everything in the targetClasses map is immutable
        targetClasses.putAll(source.targetClasses);

        //
        // Optional
        //
        name = source.name;
        description = source.description;

        //
        // Derived
        //
        targets.putAll(source.targets);
        String className = null;
        try {
            for (Iterator i = targetClasses.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                Object key = entry.getKey();
                className = (String) entry.getValue();

                // Insert Magic Here
                Class clazz;
                if (targets.containsKey(key)) {
                    clazz = targets.get(key).getClass();
                } else {
                    clazz = ParserUtil.loadClass(className);

                    if (Modifier.isFinal(clazz.getModifiers())) {
                        throw new IllegalArgumentException("Target class cannot be final: " + className);
                    }

                    GeronimoMBeanTarget target = createTarget(clazz);
                    targets.put(key, target);
                }
                FastClass fastClass = FastClass.create(clazz);
                targetFastClasses.put(key, fastClass);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Target class could not be loaded: className=" + className);
        }

        //
        // Contained classes
        //
        for (Iterator iterator = source.attributes.iterator(); iterator.hasNext();) {
            GeronimoAttributeInfo attributeInfo = (GeronimoAttributeInfo) iterator.next();
            attributes.add(new GeronimoAttributeInfo(attributeInfo, this));
        }

        for (Iterator iterator = source.operations.iterator(); iterator.hasNext();) {
            GeronimoOperationInfo operationInfo = (GeronimoOperationInfo) iterator.next();
            operations.add(new GeronimoOperationInfo(operationInfo, this));
        }

        for (Iterator iterator = source.notifications.iterator(); iterator.hasNext();) {
            GeronimoNotificationInfo notificationInfo = (GeronimoNotificationInfo) iterator.next();
            notifications.add(new GeronimoNotificationInfo(notificationInfo, this));
        }


        for (Iterator iterator = source.endpoints.iterator(); iterator.hasNext();) {
            GeronimoMBeanEndpoint endpoint = (GeronimoMBeanEndpoint) iterator.next();
            endpoints.add(new GeronimoMBeanEndpoint(endpoint, this));
        }
    }

    public String getClassName() {
        return getTargetClass();
    }

    public String getTargetClass() {
        return (String) targetClasses.get(DEFAULT_TARGET_NAME);
    }

    public void setTargetClass(Class clazz) {
        setTargetClass(clazz.getName());
    }

    public void setTargetClass(String className) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        targetClasses.put(DEFAULT_TARGET_NAME, className);
    }

    public String getTargetClass(String targetName) {
        return (String) targetClasses.get(targetName);
    }

    public void setTargetClass(String targetName, String className) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        if (targetName != null && targetName.length() > 0) {
            targetClasses.put(targetName, className);
        } else {
            targetClasses.put(DEFAULT_TARGET_NAME, className);
        }
    }

    public Object getTarget() {
        return targets.get(DEFAULT_TARGET_NAME);
    }

    Object getTarget(String name) {
        return targets.get(name);
    }

    public void setTarget(String name, Object target) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        targets.put(name, target);
    }

    public void setTarget(Object target) {
        setTarget(DEFAULT_TARGET_NAME, target);
    }

    FastClass getTargetFastClass() {
        return (FastClass) targetFastClasses.get(DEFAULT_TARGET_NAME);
    }

    FastClass getTargetFastClass(String name) {
        if (GERONIMO_MBEAN_TARGET_NAME.equals(name)) {
            return GeronimoMBean.fastClass;
        }
        return (FastClass) targetFastClasses.get(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.description = description;
    }

    public MBeanConstructorInfo[] getConstructors() {
        // This MBean does not have constructors
        return NO_CONSTRUCTORS;
    }

    public Set getAttributeSet() {
        return Collections.unmodifiableSet(attributes);
    }

    public MBeanAttributeInfo[] getAttributes() {
        return (MBeanAttributeInfo[]) attributes.toArray(new MBeanAttributeInfo[attributes.size()]);
    }

    public void addAttributeInfo(GeronimoAttributeInfo attributeInfo) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        attributes.add(attributeInfo);
    }

    public Set getOperationsSet() {
        return Collections.unmodifiableSet(operations);
    }

    public MBeanOperationInfo[] getOperations() {
        return (MBeanOperationInfo[]) operations.toArray(new MBeanOperationInfo[operations.size()]);
    }

    public void addOperationInfo(GeronimoOperationInfo operationInfo) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        operations.add(operationInfo);
    }

    public void addOperationsDeclaredIn(Class clazz) {
        addOperationsDeclaredIn(clazz, DEFAULT_TARGET_NAME);
    }

    public void addOperationFor(Method method) {
        addOperationFor(method, DEFAULT_TARGET_NAME);
    }

    public void addOperationsDeclaredIn(Class clazz, String targetName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            addOperationFor(methods[i], targetName);
        }
    }

    public void addOperationFor(Method method, String targetName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        ArrayList l = new ArrayList();
        Class[] classes = method.getParameterTypes();
        for (int j = 0; j < classes.length; j++) {
            Class class1 = classes[j];
            l.add(new GeronimoParameterInfo("arg" + (j + 1), class1, ""));
        }
        GeronimoParameterInfo params[] = new GeronimoParameterInfo[l.size()];
        l.toArray(params);
        addOperationInfo(new GeronimoOperationInfo(method.getName(), params, MBeanOperationInfo.ACTION, "", targetName));
    }

    public Set getNotificationsSet() {
        return Collections.unmodifiableSet(notifications);
    }

    public MBeanNotificationInfo[] getNotifications() {
        return (MBeanNotificationInfo[]) notifications.toArray(new MBeanNotificationInfo[notifications.size()]);
    }

    public void addNotificationInfo(GeronimoNotificationInfo notificationInfo) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        notifications.add(notificationInfo);
    }

    public Set getEndpointsSet() {
        return Collections.unmodifiableSet(endpoints);
    }

    public GeronimoMBeanEndpoint[] getEndpoints() {
        return (GeronimoMBeanEndpoint[]) endpoints.toArray(new GeronimoMBeanEndpoint[endpoints.size()]);
    }

    public void addEndpoint(GeronimoMBeanEndpoint endpoint) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        endpoints.add(endpoint);
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(boolean autostart) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.autostart = autostart;
    }


    private GeronimoMBeanTarget createTarget(Class superClass) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(superClass);
        enhancer.setInterfaces(new Class[]{GeronimoMBeanTarget.class});
        enhancer.setCallbackFilter(new TargetCallbackFilter(superClass));
        enhancer.setCallbacks(new SimpleCallbacks());
        Factory factory = enhancer.create();
        return (GeronimoMBeanTarget) factory.newInstance(NO_OP_METHOD_INTERCEPTOR);
    }

    private static final class TargetCallbackFilter implements CallbackFilter {
        private final Class superClass;

        public TargetCallbackFilter(Class superClass) {
            this.superClass = superClass;
        }

        public int accept(Method method) {
            String name = method.getName();
            Class[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 0 &&
                    method.getReturnType() == Void.TYPE &&
                    method.getExceptionTypes().length == 0 &&
                    ("doStart".equals(name) || "doStop".equals(name) || "doFail".equals(name))) {

                try {
                    superClass.getMethod(name, null);
                    return Callbacks.NO_OP;
                } catch (Exception e) {
                    return Callbacks.INTERCEPT;
                }
            }

            if (parameterTypes.length == 0 &&
                    method.getReturnType() == Boolean.TYPE &&
                    method.getExceptionTypes().length == 0 &&
                    ("canStart".equals(name) || "canStop".equals(name))) {

                try {
                    superClass.getMethod(name, null);
                    return Callbacks.NO_OP;
                } catch (Exception e) {
                    return Callbacks.INTERCEPT;
                }
            }

            if (parameterTypes.length == 1 && parameterTypes[0] == GeronimoMBeanContext.class &&
                    "setMBeanContext".equals(name) &&
                    method.getReturnType() == Void.TYPE &&
                    method.getExceptionTypes().length == 0) {

                try {
                    superClass.getMethod("setContext", parameterTypes);
                    return Callbacks.NO_OP;
                } catch (Exception e) {
                    return Callbacks.INTERCEPT;
                }
            }
            return Callbacks.NO_OP;
        }
    }

    private static final MethodInterceptor NO_OP_METHOD_INTERCEPTOR = new MethodInterceptor() {
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            if (Modifier.isAbstract(method.getModifiers())) {
                if (method.getReturnType() == Boolean.TYPE) {
                    return Boolean.TRUE;
                } else {
                    return null;
                }
            }
            return proxy.invokeSuper(obj, args);
        }
    };


    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object object) {
        return (this == object);
    }

    public String toString() {
        StringBuffer result = new StringBuffer("[GeronimoMBeanInfo: id=").append(super.toString()).append(" name=").append(name).append(" description=").append(description).append(" immutable=").append(immutable);
        for (Iterator iterator = attributes.iterator(); iterator.hasNext();) {
            GeronimoAttributeInfo geronimoAttributeInfo = (GeronimoAttributeInfo) iterator.next();
            result.append("\n    attribute: ").append(geronimoAttributeInfo);
        }
        for (Iterator iterator = operations.iterator(); iterator.hasNext();) {
            GeronimoOperationInfo geronimoOperationInfo = (GeronimoOperationInfo) iterator.next();
            result.append("\n    operation: ").append(geronimoOperationInfo);
        }
        for (Iterator iterator = endpoints.iterator(); iterator.hasNext();) {
            GeronimoMBeanEndpoint geronimoMBeanEndpoint = (GeronimoMBeanEndpoint) iterator.next();
            result.append("\n    endpoint: ").append(geronimoMBeanEndpoint);
        }
        result.append("]");
        return result.toString();
    }
}