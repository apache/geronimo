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
package org.apache.geronimo.jaxws;

import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.namespace.QName;

public class JAXWSUtils {
    
    private static final Map<String, String> BINDING_MAP = 
        new HashMap<String, String>();
    
    static {
        BINDING_MAP.put("##SOAP11_HTTP", "http://schemas.xmlsoap.org/wsdl/soap/http");
        BINDING_MAP.put("##SOAP12_HTTP", "http://www.w3.org/2003/05/soap/bindings/HTTP/");
        BINDING_MAP.put("##SOAP11_HTTP_MTOM", "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true");
        BINDING_MAP.put("##SOAP12_HTTP_MTOM", "http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true");
        BINDING_MAP.put("##XML_HTTP", "http://www.w3.org/2004/08/wsdl/http");
    }
    
    private JAXWSUtils() {
    }

    public static QName getPortType(Class seiClass) {
        WebService webService = (WebService) seiClass.getAnnotation(WebService.class);        
        if (webService != null) {
            String localName = webService.name();
            if (localName == null || localName.length() == 0) {
                localName = seiClass.getName();
            }
            String namespace = webService.targetNamespace();
            return new QName(namespace, localName);
        }
        return null;
    }

    public static String getBindingURI(String token) {
        if (token == null) {
            // return the default
            return BINDING_MAP.get("##SOAP11_HTTP");
        } else if (token.startsWith("##")) {
            String uri = BINDING_MAP.get(token);
            if (uri == null) {
                throw new IllegalArgumentException("Unsupported binding token: " + token);
            }
            return uri;
        } else {
            return token;            
        }
    }
}
