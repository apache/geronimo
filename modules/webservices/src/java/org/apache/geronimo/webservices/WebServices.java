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


/**
 * A dtd version of the J2EE webservices.xml file would look like this:
 * <p/>
 * webservices (webservice-description+)
 * webservice-description (webservice-description-name, wsdl-file, jaxrpc-mapping-file, port-component+)
 * port-component (port-component-name, wsdl-port, service-endpoint-interface, service-impl-bean, handler*)
 * service-impl-bean (ejb-link|servlet-link)
 * handler (handler-name, handler-class, init-param*, soap-header*, soap-role*)
 */
public class WebServices {
    /**
     * List of WebServiceDescription objects
     *
     * @see org.apache.geronimo.webservices.WebServiceDescription
     */
    private ArrayList webServiceDescriptionList = new ArrayList();
    /**
     * Map of WebServiceDescription objects indexed by webServiceDescriptionName
     *
     * @see org.apache.geronimo.webservices.WebServiceDescription#getWebServiceDescriptionName
     */
    private HashMap webServiceDescriptionMap = new HashMap();

    public void addWebServiceDescription(WebServiceDescription webServiceDescription) throws IndexOutOfBoundsException {
        webServiceDescriptionList.add(webServiceDescription);
        webServiceDescriptionMap.put(webServiceDescription.getWebServiceDescriptionName(), webServiceDescription);
    }

    public void addWebServiceDescription(int index, WebServiceDescription webServiceDescription) throws IndexOutOfBoundsException {
        webServiceDescriptionList.add(index, webServiceDescription);
        webServiceDescriptionMap.put(webServiceDescription.getWebServiceDescriptionName(), webServiceDescription);
    }

    public boolean removeWebServiceDescription(WebServiceDescription webServiceDescription) {
        webServiceDescriptionMap.remove(webServiceDescription.getWebServiceDescriptionName());
        return webServiceDescriptionList.remove(webServiceDescription);
    }

    public WebServiceDescription getWebServiceDescription(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > webServiceDescriptionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (WebServiceDescription) webServiceDescriptionList.get(index);
    }

    public WebServiceDescription[] getWebServiceDescription() {
        int size = webServiceDescriptionList.size();
        WebServiceDescription[] mArray = new WebServiceDescription[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (WebServiceDescription) webServiceDescriptionList.get(index);
        }
        return mArray;
    }

    public WebServiceDescription getWebServiceDescription(String webServiceDescriptionName) {
        return (WebServiceDescription) webServiceDescriptionMap.get(webServiceDescriptionName);
    }

    public void setWebServiceDescription(int index, WebServiceDescription webServiceDescription) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > webServiceDescriptionList.size())) {
            throw new IndexOutOfBoundsException();
        }
        WebServiceDescription removed = (WebServiceDescription) webServiceDescriptionList.set(index, webServiceDescription);
        webServiceDescriptionMap.remove(removed.getWebServiceDescriptionName());
        webServiceDescriptionMap.put(webServiceDescription.getWebServiceDescriptionName(), webServiceDescription);
    }

    public void setWebServiceDescription(WebServiceDescription[] webServiceDescriptionArray) {
        clearWebServiceDescription();
        for (int i = 0; i < webServiceDescriptionArray.length; i++) {
            WebServiceDescription webServiceDescription = webServiceDescriptionArray[i];
            webServiceDescriptionList.add(webServiceDescription);
            webServiceDescriptionMap.put(webServiceDescription.getWebServiceDescriptionName(), webServiceDescription);
        }
    }

    public void clearWebServiceDescription() {
        webServiceDescriptionList.clear();
        webServiceDescriptionMap.clear();
    }
}
