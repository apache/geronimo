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

import java.io.InvalidClassException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.rmi.Remote;
import java.util.Iterator;
import java.util.List;
import java.math.BigInteger;

import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.SerializerFactory;
import javax.xml.rpc.encoding.DeserializerFactory;
import javax.xml.rpc.handler.HandlerChain;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastConstructor;

import org.apache.axis.client.Service;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.handlers.HandlerInfoChainFactory;
import org.apache.axis.AxisEngine;
import org.apache.axis.Constants;
import org.apache.axis.constants.Use;
import org.apache.axis.encoding.TypeMappingRegistry;
import org.apache.axis.encoding.TypeMapping;
import org.apache.axis.encoding.ser.SimpleSerializerFactory;
import org.apache.axis.encoding.ser.SimpleDeserializerFactory;
import org.apache.axis.encoding.ser.BaseSerializerFactory;
import org.apache.axis.encoding.ser.BaseDeserializerFactory;

/**
 * @version $Rev:  $ $Date:  $
 */
public class SEIFactoryImpl implements SEIFactory, Serializable {
    private static final Class[] SERVICE_ENDPOINT_CONSTRUCTOR_TYPES = new Class[]{GenericServiceEndpoint.class};

    private final QName serviceName;
    private final QName portQName;
    private final Class serviceEndpointClass;
    private final OperationInfo[] operationInfos;
    private transient final FastConstructor constructor;
    private final Object serviceImpl;
    private final List typeInfo;
    private final URL location;
    private final List handlerInfos;
    private final String credentialsName;
    private transient HandlerInfoChainFactory handlerInfoChainFactory;
    private transient OperationInfo[] sortedOperationInfos;
    private boolean initialized = false;

    public SEIFactoryImpl(QName serviceName, String portName, Class serviceEndpointClass, OperationInfo[] operationInfos, Object serviceImpl, List typeInfo, URL location, List handlerInfos, ClassLoader classLoader, String credentialsName) throws ClassNotFoundException {
        this.serviceName = serviceName;
        this.portQName = new QName("", portName);
        this.serviceEndpointClass = serviceEndpointClass;
        this.operationInfos = operationInfos;
        Class[] constructorTypes = new java.lang.Class[0];
        constructorTypes = classLoader == null ? SERVICE_ENDPOINT_CONSTRUCTOR_TYPES : new Class[]{classLoader.loadClass(GenericServiceEndpoint.class.getName())};
        this.constructor = FastClass.create(serviceEndpointClass).getConstructor(constructorTypes);
        this.serviceImpl = serviceImpl;
        this.typeInfo = typeInfo;
        this.location = location;
        this.handlerInfos = handlerInfos;
        this.credentialsName = credentialsName;
        this.handlerInfoChainFactory = new HandlerInfoChainFactory(handlerInfos);
        sortedOperationInfos = new OperationInfo[FastClass.create(serviceEndpointClass).getMaxIndex() + 1];
    }

    void initialize() {
        String encodingStyle = "";
        for (int i = 0; i < operationInfos.length; i++) {
            OperationInfo operationInfo = operationInfos[i];
            Signature signature = operationInfo.getSignature();
            MethodProxy methodProxy = MethodProxy.find(serviceEndpointClass, signature);
            if (methodProxy == null) {
                throw new RuntimeException("No method proxy for operationInfo " + signature);
            }
            int index = methodProxy.getSuperIndex();
            sortedOperationInfos[index] = operationInfo;
            if (operationInfo.getOperationDesc().getUse() == Use.ENCODED) {
                encodingStyle = org.apache.axis.Constants.URI_SOAP11_ENC;
            }
        }
        //register our type descriptors
        Service service = ((ServiceImpl) serviceImpl).getService();
        AxisEngine axisEngine = service.getEngine();
        TypeMappingRegistry typeMappingRegistry = axisEngine.getTypeMappingRegistry();
        TypeMapping typeMapping = typeMappingRegistry.getOrMakeTypeMapping(encodingStyle);
        typeMapping.register(BigInteger.class,
                Constants.XSD_UNSIGNEDLONG,
                new SimpleSerializerFactory(BigInteger.class, Constants.XSD_UNSIGNEDLONG),
                new SimpleDeserializerFactory(BigInteger.class, Constants.XSD_UNSIGNEDLONG));
        typeMapping.register(URI.class,
                Constants.XSD_ANYURI,
                new SimpleSerializerFactory(URI.class, Constants.XSD_ANYURI),
                new SimpleDeserializerFactory(URI.class, Constants.XSD_ANYURI));

        for (Iterator iter = typeInfo.iterator(); iter.hasNext();) {
            TypeInfo info = (TypeInfo) iter.next();
            TypeDesc.registerTypeDescForClass(info.getClazz(), info.buildTypeDesc());

            SerializerFactory sf =
                    BaseSerializerFactory.createFactory(info.getSerFactoryClass(), info.getClazz(), info.getqName());
            DeserializerFactory df =
                    BaseDeserializerFactory.createFactory(info.getDeserFactoryClass(), info.getClazz(), info.getqName());
            typeMapping.register(info.getClazz(), info.getqName(), sf, df);
        }
    }

    public Remote createServiceEndpoint() throws ServiceException {
        //TODO figure out why this can't be called in readResolve!
        synchronized (this) {
            if (!initialized) {
                initialize();
                initialized = true;
            }
        }
        Service service = ((ServiceImpl) serviceImpl).getService();
        GenericServiceEndpoint serviceEndpoint = new GenericServiceEndpoint(portQName, service, location);
        Callback callback = new ServiceEndpointMethodInterceptor(serviceEndpoint, sortedOperationInfos, credentialsName);
        Callback[] callbacks = new Callback[]{SerializableNoOp.INSTANCE, callback};
        Enhancer.registerCallbacks(serviceEndpointClass, callbacks);
        try {
            return (Remote) constructor.newInstance(new Object[]{serviceEndpoint});
        } catch (InvocationTargetException e) {
            e.getTargetException().printStackTrace();
            throw new ServiceException("Could not construct service instance", e.getTargetException());
        }
    }

    public HandlerChain createHandlerChain() {
        HandlerChain handlerChain = handlerInfoChainFactory.createHandlerChain();
        return handlerChain;
    }

    private Object readResolve() throws ObjectStreamException {
        try {
            SEIFactoryImpl seiFactory =  new SEIFactoryImpl(serviceName, portQName.getLocalPart(), serviceEndpointClass, operationInfos, serviceImpl, typeInfo, location, handlerInfos, null, credentialsName);
//            seiFactory.initialize();
            return seiFactory;
        } catch (ClassNotFoundException e) {
            throw new InvalidClassException(GenericServiceEndpoint.class.getName(), "this is impossible");
        }
    }

    public OperationInfo[] getOperationInfos() {
        return operationInfos;
    }

    public QName getPortQName() {
        return portQName;
    }

    public QName getServiceName() {
        return serviceName;
    }

    public URL getWSDLDocumentLocation() {
        try {
            return new URL(location.toExternalForm() + "?wsdl");
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
