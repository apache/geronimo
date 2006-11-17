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
package org.apache.geronimo.axis.client;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import javax.xml.rpc.ServiceException;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @version $Rev$ $Date$
 */
public class ServiceMethodInterceptor implements MethodInterceptor, Serializable {

    private final Map seiFactoryMap;

    public ServiceMethodInterceptor(Map seiFactoryMap) {
        this.seiFactoryMap = seiFactoryMap;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        if (objects.length == 0) {
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                String portName = methodName.substring(3);
                SEIFactory seiFactory = (SEIFactory) seiFactoryMap.get(portName);
                if (seiFactory != null) {
                    return seiFactory.createServiceEndpoint();
                }
            }
        }
        throw new ServiceException("Unrecognized method name or argument list: " + method.getName());
    }

}
