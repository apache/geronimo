/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.gbean.jmx;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GOperationSignature;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/07/27 02:14:14 $
 */
public class VMMethodInterceptor implements ProxyMethodInterceptor, InvocationHandler {
    private final Class proxyType;
    private ObjectName objectName;
    private boolean stopped;
    private Map gbeanInvokers;

    public VMMethodInterceptor(Class proxyType) {
        this.proxyType = proxyType;
    }

    public void connect(MBeanServerConnection server, ObjectName objectName) {
        connect(server, objectName, false);
    }

    public void connect(MBeanServerConnection server, ObjectName objectName, boolean stopped) {
        assert server != null && objectName != null;
        this.objectName = objectName;
        this.stopped = stopped;
        gbeanInvokers = createGBeanInvokers(server, objectName);
    }

    public void disconnect() {
        stopped = true;
        objectName = null;
        gbeanInvokers = null;
    }

    public void start() {
        if (gbeanInvokers == null) {
            throw new IllegalStateException("Proxy is not connected");
        }
        this.stopped = false;
    }

    public void stop() {
        this.stopped = true;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        GBeanInvoker gbeanInvoker;

        synchronized (this) {
            if (stopped) {
                throw new IllegalStateException("Proxy is stopped");
            }
            gbeanInvoker = (GBeanInvoker) gbeanInvokers.get(method);
        }

        if (gbeanInvoker == null) {
            throw new NoSuchOperationError("No implementation method: objectName=" + objectName + ", method=" + method);
        }

        return gbeanInvoker.invoke(objectName, args);
    }

    private Map createGBeanInvokers(MBeanServerConnection server, ObjectName objectName) {
        Map invokers = createJMXGBeanInvokers(server, objectName, proxyType);

        // handle equals, hashCode and toString directly here
        try {
            invokers.put(Object.class.getMethod("equals", new Class[]{Object.class}), new EqualsInvoke());
            invokers.put(Object.class.getMethod("hashCode", null), new HashCodeInvoke());
            invokers.put(Object.class.getMethod("toString", null), new ToStringInvoke(proxyType.getName()));
        } catch (Exception e) {
            // this can not happen... all classes must implement equals, hashCode and toString
            throw new AssertionError(e);
        }

        return invokers;
    }

    private Map createJMXGBeanInvokers(MBeanServerConnection server, ObjectName objectName, Class proxyType) {
        MBeanInfo info;
        try {
            info = server.getMBeanInfo(objectName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get MBeanInfo for target object: " + objectName);
        }

        // build attributeName->attributeInfo map
        MBeanAttributeInfo[] attributeInfos = info.getAttributes();
        Map attributes = new HashMap(attributeInfos.length);
        for (int i = 0; i < attributeInfos.length; i++) {
            MBeanAttributeInfo attributeInfo = attributeInfos[i];
            attributes.put(attributeInfo.getName(), attributeInfo);
        }

        // build operationName->operationInfo map
        MBeanOperationInfo[] operationInfos = info.getOperations();
        Map operations = new HashMap(operationInfos.length);
        for (int i = 0; i < operationInfos.length; i++) {
            MBeanOperationInfo operationInfo = operationInfos[i];
            operations.put(new GOperationSignature(operationInfo), operationInfo);
        }

        // build the method lookup table
        Method[] methods = proxyType.getMethods();
        Map invokers = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            invokers.put(method, createJMXGBeanInvoker(server, method, operations, attributes));
        }

        return invokers;
    }

    private GBeanInvoker createJMXGBeanInvoker(MBeanServerConnection server, Method method, Map operations, Map attributes) {
        if (operations.containsKey(new GOperationSignature(method))) {
            return new JMXOperationInvoker(server, method);
        }

        String name = method.getName();
        if (name.startsWith("get")) {
            String attrName = method.getName().substring(3);
            if (attributes.containsKey(attrName)) {
                return new JMXGetAttributeInvoker(server, method, attrName);
            }
            attrName = Introspector.decapitalize(attrName);
            if (attributes.containsKey(attrName)) {
                return new JMXGetAttributeInvoker(server, method, attrName);
            }
        } else if (name.startsWith("is")) {
            String attrName = method.getName().substring(2);
            if (attributes.containsKey(attrName)) {
                return new JMXGetAttributeInvoker(server, method, attrName);
            }
            attrName = Introspector.decapitalize(attrName);
            if (attributes.containsKey(attrName)) {
                return new JMXGetAttributeInvoker(server, method, attrName);
            }
        } else if (name.startsWith("set")) {
            String attrName = method.getName().substring(3);
            if (attributes.containsKey(attrName)) {
                return new JMXSetAttributeInvoker(server, method, attrName);
            }
            attrName = Introspector.decapitalize(attrName);
            if (attributes.containsKey(attrName)) {
                return new JMXSetAttributeInvoker(server, method, attrName);
            }
        }
        return null;
    }
}
