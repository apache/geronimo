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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.rmi.Remote;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.encoding.TypeMappingRegistry;
import javax.xml.rpc.handler.HandlerRegistry;

import org.apache.axis.client.Service;


/**
 * @version $Rev$ $Date$
 */
public class ServiceImpl implements javax.xml.rpc.Service, Serializable {

    private transient Service delegate;
    private final Map portToImplementationMap;

    public ServiceImpl(Map portToImplementationMap) {
        this.portToImplementationMap = portToImplementationMap;
        this.delegate = new Service();
    }

    public Remote getPort(QName qName, Class portClass) throws ServiceException {
        if (qName != null) {
            String portName = qName.getLocalPart();
            Remote port = internalGetPort(portName);
            if (port != null) {
                return port;
            }
        }
        String fqcn = portClass.getName();
        String portName = fqcn.substring(fqcn.lastIndexOf('.'));
        Remote port = internalGetPort(portName);
        if (port != null) {
            return port;
        }
        return delegate.getPort(qName, portClass);
    }

    public Remote getPort(Class portClass) throws ServiceException {
        String fqcn = portClass.getName();
        String portName = fqcn.substring(fqcn.lastIndexOf('.'));
        Remote port = internalGetPort(portName);
        if (port != null) {
            return port;
        }
        return delegate.getPort(portClass);
    }

    public Call[] getCalls(QName qName) throws ServiceException {
        return delegate.getCalls(qName);
    }

    public Call createCall(QName qName) throws ServiceException {
        return delegate.createCall(qName);
    }

    public Call createCall(QName qName, QName qName1) throws ServiceException {
        return delegate.createCall(qName,  qName1);
    }

    public Call createCall(QName qName, String s) throws ServiceException {
        return delegate.createCall(qName, s);
    }

    public Call createCall() throws ServiceException {
        return delegate.createCall();
    }

    public QName getServiceName() {
        return delegate.getServiceName();
    }

    public Iterator getPorts() throws ServiceException {
        return portToImplementationMap.values().iterator();
    }

    public URL getWSDLDocumentLocation() {
        return delegate.getWSDLDocumentLocation();
    }

    public TypeMappingRegistry getTypeMappingRegistry() {
        return delegate.getTypeMappingRegistry();
    }

    public HandlerRegistry getHandlerRegistry() {
        return delegate.getHandlerRegistry();
    }

    Remote internalGetPort(String portName) throws ServiceException {
        if (portToImplementationMap.containsKey(portName)) {
            return (Remote) portToImplementationMap.get(portName);
        }
        return null;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        delegate = new Service();
    }

    Service getService() {
        return delegate;
    }
}
