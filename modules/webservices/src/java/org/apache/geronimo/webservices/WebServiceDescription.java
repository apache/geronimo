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

public class WebServiceDescription {
    private String webServiceDescriptionName;
    private String wsdlFile;
    private String jaxrpcMappingFile;

    /**
     * List of PortComponent objects
     *
     * @see org.apache.geronimo.webservices.PortComponent
     */
    private ArrayList portComponentList = new ArrayList();
    /**
     * Map of PortComponent objects indexed by portComponentName
     *
     * @see org.apache.geronimo.webservices.PortComponent#getPortComponentName
     */
    private HashMap portComponentMap = new HashMap();

    public String getWebServiceDescriptionName() {
        return webServiceDescriptionName;
    }

    public void setWebServiceDescriptionName(String webServiceDescriptionName) {
        this.webServiceDescriptionName = webServiceDescriptionName;
    }

    public String getWsdlFile() {
        return wsdlFile;
    }

    public void setWsdlFile(String wsdlFile) {
        this.wsdlFile = wsdlFile;
    }

    public String getJaxrpcMappingFile() {
        return jaxrpcMappingFile;
    }

    public void setJaxrpcMappingFile(String jaxrpcMappingFile) {
        this.jaxrpcMappingFile = jaxrpcMappingFile;
    }

    public void addPortComponent(PortComponent portComponent) throws IndexOutOfBoundsException {
        portComponentList.add(portComponent);
        portComponentMap.put(portComponent.getPortComponentName(), portComponent);
    }

    public void addPortComponent(int index, PortComponent portComponent) throws IndexOutOfBoundsException {
        portComponentList.add(index, portComponent);
        portComponentMap.put(portComponent.getPortComponentName(), portComponent);
    }

    public boolean removePortComponent(PortComponent portComponent) {
        portComponentMap.remove(portComponent.getPortComponentName());
        return portComponentList.remove(portComponent);
    }

    public PortComponent getPortComponent(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > portComponentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (PortComponent) portComponentList.get(index);
    }

    public PortComponent[] getPortComponent() {
        int size = portComponentList.size();
        PortComponent[] mArray = new PortComponent[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (PortComponent) portComponentList.get(index);
        }
        return mArray;
    }

    public PortComponent getPortComponent(String portComponentName) {
        return (PortComponent) portComponentMap.get(portComponentName);
    }

    public void setPortComponent(int index, PortComponent portComponent) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > portComponentList.size())) {
            throw new IndexOutOfBoundsException();
        }
        PortComponent removed = (PortComponent) portComponentList.set(index, portComponent);
        portComponentMap.remove(removed.getPortComponentName());
        portComponentMap.put(portComponent.getPortComponentName(), portComponent);
    }

    public void setPortComponent(PortComponent[] portComponentArray) {
        portComponentList.clear();
        for (int i = 0; i < portComponentArray.length; i++) {
            PortComponent portComponent = portComponentArray[i];
            portComponentList.add(portComponent);
            portComponentMap.put(portComponent.getPortComponentName(), portComponent);
        }
    }

    public void clearPortComponent() {
        portComponentList.clear();
        portComponentMap.clear();
    }

}
