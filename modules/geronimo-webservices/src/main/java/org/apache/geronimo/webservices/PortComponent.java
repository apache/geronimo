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
package org.apache.geronimo.webservices;

import java.util.ArrayList;
import java.util.HashMap;

public class PortComponent {
    private String portComponentName;
    private String wsdlPort;
    private String serviceEndpointInterface;
    private ServiceImplBean serviceImplBean;

    /**
     * List of Handler objects
     *
     * @see org.apache.geronimo.webservices.Handler
     */
    private ArrayList handlerList = new ArrayList();
    /**
     * Map of Handler objects indexed by handlerName
     *
     * @see org.apache.geronimo.webservices.Handler#getHandlerName
     */
    private HashMap handlerMap = new HashMap();

    public void addHandler(Handler handler) throws IndexOutOfBoundsException {
        handlerList.add(handler);
        handlerMap.put(handler.getHandlerName(), handler);
    }

    public void addHandler(int index, Handler handler) throws IndexOutOfBoundsException {
        handlerList.add(index, handler);
        handlerMap.put(handler.getHandlerName(), handler);
    }

    public boolean removeHandler(Handler handler) {
        handlerMap.remove(handler.getHandlerName());
        return handlerList.remove(handler);
    }

    public Handler getHandler(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > handlerList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (Handler) handlerList.get(index);
    }

    public Handler[] getHandler() {
        int size = handlerList.size();
        Handler[] mArray = new Handler[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (Handler) handlerList.get(index);
        }
        return mArray;
    }

    public Handler getHandler(String handlerName) {
        return (Handler) handlerMap.get(handlerName);
    }

    public void setHandler(int index, Handler handler) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > handlerList.size())) {
            throw new IndexOutOfBoundsException();
        }
        Handler removed = (Handler) handlerList.set(index, handler);
        handlerMap.remove(removed.getHandlerName());
        handlerMap.put(handler.getHandlerName(), handler);
    }

    public void setHandler(Handler[] handlerArray) {
        handlerList.clear();
        for (int i = 0; i < handlerArray.length; i++) {
            Handler handler = handlerArray[i];
            handlerList.add(handler);
            handlerMap.put(handler.getHandlerName(), handler);
        }
    }

    public void clearHandler() {
        handlerList.clear();
        handlerMap.clear();
    }

    public String getPortComponentName() {
        return portComponentName;
    }

    public void setPortComponentName(String portComponentName) {
        this.portComponentName = portComponentName;
    }

    public String getWsdlPort() {
        return wsdlPort;
    }

    public void setWsdlPort(String wsdlPort) {
        this.wsdlPort = wsdlPort;
    }

    public String getServiceEndpointInterface() {
        return serviceEndpointInterface;
    }

    public void setServiceEndpointInterface(String serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public ServiceImplBean getServiceImplBean() {
        return serviceImplBean;
    }

    public void setServiceImplBean(ServiceImplBean serviceImplBean) {
        this.serviceImplBean = serviceImplBean;
    }
}
