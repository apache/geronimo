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
package org.apache.geronimo.kernel.jmx;

import java.beans.Introspector;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GOperationSignature;

/**
 * @version $Rev$ $Date$
 */
public class JMXInvocationHandler implements InvocationHandler {
    private final Class proxyType;
    private final ObjectName objectName;
    private final Map gbeanInvokers;

    public JMXInvocationHandler(Class type, MBeanServerConnection server, ObjectName objectName) {
        assert type != null;
        assert server != null;
        assert objectName != null;
        this.proxyType = type;
        this.objectName = objectName;
        gbeanInvokers = createGBeanInvokers(server, objectName);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        JMXInvoker jmxInvoker = (JMXInvoker) gbeanInvokers.get(method);
        if (jmxInvoker == null) {
            throw new UnsupportedOperationException("No implementation method: objectName=" + objectName + ", method=" + method);
        }

        return jmxInvoker.invoke(objectName, args);
    }

    public ObjectName getObjectName() {
        return objectName;
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

    private JMXInvoker createJMXGBeanInvoker(MBeanServerConnection server, Method method, Map operations, Map attributes) {
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
    private static final class HashCodeInvoke implements JMXInvoker {
        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return new Integer(objectName.hashCode());
        }
    }

    private static final class EqualsInvoke implements JMXInvoker {
        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            try {
                InvocationHandler handler = Proxy.getInvocationHandler(arguments[0]);
                if (handler instanceof JMXInvocationHandler) {
                    ObjectName otherObjectName = ((JMXInvocationHandler) handler).getObjectName();
                    return Boolean.valueOf(objectName.equals(otherObjectName));
                }
            } catch (IllegalArgumentException e) {
            }
            return Boolean.FALSE;
        }
    }

    private static final class ToStringInvoke implements JMXInvoker {
        private final String interfaceName;

        public ToStringInvoke(String interfaceName) {
            this.interfaceName = "[" + interfaceName + ": ";
        }

        public Object invoke(ObjectName objectName, Object[] arguments) throws Throwable {
            return interfaceName + objectName + "]";
        }
    }
}
