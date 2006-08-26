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

import java.io.InputStream;
import java.net.URL;

import org.apache.geronimo.common.DeploymentException;
import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.Marshaller;
import org.xml.sax.InputSource;

public class WebServicesFactory {

    private static WebServicesFactory webServicesFactory;

    private final Mapping mapping;
    private final Unmarshaller unmarshaller;

    private WebServicesFactory() {
        ClassLoader classLoader = WebServicesFactory.class.getClassLoader();
        URL mappingUrl = classLoader.getResource("org/apache/geronimo/webservices/webservices_1_1.xml");

        try {
            mapping = new Mapping(classLoader);
            mapping.loadMapping(mappingUrl);
            unmarshaller = new Unmarshaller(mapping);
        } catch (Exception e) {
            throw (IllegalStateException)new IllegalStateException("Unable to initialize xml unmarshaller").initCause(e);
        }
    }

    public static WebServicesFactory getInstance() {
        if (webServicesFactory == null){
            webServicesFactory = new WebServicesFactory();
        }
        return webServicesFactory;
    }

    public WebServices readXML(URL webservicesURL) throws DeploymentException {
        InputStream in = null;
        WebServices webservice = null;
        try {
            in = webservicesURL.openStream();
            webservice = (WebServices) unmarshaller.unmarshal(new InputSource(in));
        } catch (Exception e) {
            throw new DeploymentException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch(Exception ignored) {
                    // Don't care
                }
            }
        }
        return webservice;
    }

}
