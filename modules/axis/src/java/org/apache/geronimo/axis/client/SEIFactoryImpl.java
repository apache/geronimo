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
package org.apache.geronimo.axis.client;

import java.lang.reflect.InvocationTargetException;
import java.rmi.Remote;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.io.InvalidClassException;
import java.util.List;
import java.net.URL;
import javax.xml.rpc.ServiceException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastConstructor;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.core.Signature;
import org.apache.axis.client.Service;

/**
 * @version $Rev:  $ $Date:  $
 */
public class SEIFactoryImpl implements SEIFactory, Serializable {
    private static final Class[] SERVICE_ENDPOINT_CONSTRUCTOR_TYPES = new Class[]{GenericServiceEndpoint.class};

    private final Class serviceEndpointClass;
    private final OperationInfo[] operationInfos;
    private transient final FastConstructor constructor;
    private final Object serviceImpl;
    private final List typeMappings;
    private final URL location;
    private transient OperationInfo[] sortedOperationInfos;

    public SEIFactoryImpl(Class serviceEndpointClass, OperationInfo[] operationInfos, Object serviceImpl, List typeMappings, URL location, ClassLoader classLoader) throws ClassNotFoundException {
        this.serviceEndpointClass = serviceEndpointClass;
        this.operationInfos = operationInfos;
        Class[] constructorTypes = new java.lang.Class[0];
            constructorTypes = classLoader == null? SERVICE_ENDPOINT_CONSTRUCTOR_TYPES: new Class[] {classLoader.loadClass(GenericServiceEndpoint.class.getName())};
        this.constructor = FastClass.create(serviceEndpointClass).getConstructor(constructorTypes);
        this.serviceImpl = serviceImpl;
        this.typeMappings = typeMappings;
        this.location = location;
        sortedOperationInfos = new OperationInfo[FastClass.create(serviceEndpointClass).getMaxIndex() + 1];
        for (int i = 0; i < operationInfos.length; i++) {
            OperationInfo operationInfo = operationInfos[i];
            Signature signature = operationInfo.getSignature();
            MethodProxy methodProxy = MethodProxy.find(serviceEndpointClass, signature);
            if (methodProxy == null) {
                throw new RuntimeException("No method proxy for operationInfo " + signature);
            }
            int index = methodProxy.getSuperIndex();
            sortedOperationInfos[index] = operationInfo;
        }
    }

    public Remote createServiceEndpoint() throws ServiceException {
        Service service = ((ServiceImpl)serviceImpl).getService();
        GenericServiceEndpoint serviceEndpoint = new GenericServiceEndpoint(service, typeMappings, location);
        Callback callback = new ServiceEndpointMethodInterceptor(serviceEndpoint, sortedOperationInfos);
        Callback[] callbacks = new Callback[]{SerializableNoOp.INSTANCE, callback};
        Enhancer.registerCallbacks(serviceEndpointClass, callbacks);
        try {
            return (Remote) constructor.newInstance(new Object[]{serviceEndpoint});
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            throw new ServiceException("Could not construct service instance", e.getTargetException());
        }
    }

    private Object readResolve() throws ObjectStreamException {
        try {
            return new SEIFactoryImpl(serviceEndpointClass, operationInfos, serviceImpl, typeMappings, location, null);
        } catch (ClassNotFoundException e) {
            throw new InvalidClassException(GenericServiceEndpoint.class.getName(), "this is impossible");
        }
    }
}
