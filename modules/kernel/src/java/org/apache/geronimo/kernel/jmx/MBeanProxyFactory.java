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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.SimpleCallbacks;
import net.sf.cglib.reflect.FastClass;

/**
 * MBeanProxyFactory creates a dynamic proxy to an MBean by ObjectName.
 * The interface type and object existance are enforced during construction.
 *
 * @version $Revision: 1.2 $ $Date: 2003/11/07 17:32:11 $
 */
public final class MBeanProxyFactory {

    /**
     * Creates an MBean proxy using the specified interface to the objectName.
     *
     * @param iface the interface to implement for this proxy
     * @param server the MBeanServer in which the object is registered
     * @param objectName the objectName of the MBean to proxy
     * @return the new MBean proxy, which implemnts the specified interface
     */
    public static Object getProxy(Class iface, MBeanServer server, ObjectName objectName) {
        assert iface != null;
        assert iface.isInterface();
        assert server != null;

        // get the factory
        Factory factory = Enhancer.create(
                Object.class,
                new Class[]{iface},
                new InterfaceCallbackFilter(),
                new SimpleCallbacks());

        // build the method table
        FastClass fastClass = FastClass.create(iface);

        if (objectName.isPattern()) {
            Set names = server.queryNames(objectName, null);
            if (names.isEmpty()) {
                throw new IllegalArgumentException("No names mbeans registered that match object name pattern: " + objectName);
            }
            objectName = (ObjectName) names.iterator().next();
        }

        MBeanInfo info = null;
        try {
            info = server.getMBeanInfo(objectName);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not get MBeanInfo for target object: " + objectName);
        }

        MBeanAttributeInfo[] attributeInfos = info.getAttributes();
        Map attributes = new HashMap(attributeInfos.length);
        for (int i = 0; i < attributeInfos.length; i++) {
            MBeanAttributeInfo attributeInfo = attributeInfos[i];
            attributes.put(attributeInfo.getName(), attributeInfo);
        }

        MBeanOperationInfo[] operationInfos = info.getOperations();
        Map operations = new HashMap(operationInfos.length);
        for (int i = 0; i < operationInfos.length; i++) {
            MBeanOperationInfo operationInfo = operationInfos[i];
            operations.put(new MBeanOperationSignature(operationInfo), operationInfo);
        }

        InvokeMBean[] methodTable = new InvokeMBean[fastClass.getMaxIndex() + 1];
        Method[] methods = fastClass.getJavaClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            int index = fastClass.getIndex(method.getName(), method.getParameterTypes());
            if (operations.containsKey(new MBeanOperationSignature(method))) {
                methodTable[index] = new InvokeMBean(method, false, false);
            } else if (method.getName().startsWith("get") && attributes.containsKey(method.getName().substring(3))) {
                methodTable[index] = new InvokeMBean(method, true, true);
            } else if (method.getName().startsWith("is") && attributes.containsKey(method.getName().substring(2))) {
                methodTable[index] = new InvokeMBean(method, true, true);
            } else if (method.getName().startsWith("set") && attributes.containsKey(method.getName().substring(3))) {
                methodTable[index] = new InvokeMBean(method, true, false);
            }
        }
        return factory.newInstance(new MBeanProxyCallback(methodTable, server, objectName));
    }
}
