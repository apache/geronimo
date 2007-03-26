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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceProvider;

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
    
    public static boolean isWebService(Class clazz) {
        return ((clazz.isAnnotationPresent(WebService.class) || 
                 clazz.isAnnotationPresent(WebServiceProvider.class)) &&
                 isProperWebService(clazz));
    }
    
    private static boolean isProperWebService(Class clazz) {
        int modifiers = clazz.getModifiers();
        return (Modifier.isPublic(modifiers) &&
                !Modifier.isFinal(modifiers) &&
                !Modifier.isAbstract(modifiers));
    }
    
    public static String getServiceName(Class clazz) {
        WebService webService = 
            (WebService)clazz.getAnnotation(WebService.class);
        if (webService == null) {
            WebServiceProvider webServiceProvider = 
                (WebServiceProvider)clazz.getAnnotation(WebServiceProvider.class);
            if (webServiceProvider == null) {
                throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
            }
            return getServiceName(clazz, webServiceProvider.serviceName());
        } else {
            return getServiceName(clazz, webService.serviceName());
        }
    }
    
    private static String getServiceName(Class clazz, String name) {
        if (name == null || name.trim().length() == 0) {
            return clazz.getSimpleName() + "Service";
        } else {
            return name.trim();
        }       
    }
    
    public static String getName(Class clazz) {
        WebService webService = 
            (WebService)clazz.getAnnotation(WebService.class);
        if (webService == null) {
            WebServiceProvider webServiceProvider = 
                (WebServiceProvider)clazz.getAnnotation(WebServiceProvider.class);
            if (webServiceProvider == null) {
                throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
            } 
            return clazz.getSimpleName();         
        } else {
            String sei = webService.endpointInterface();
            if (sei == null || sei.trim().length() == 0) {
                return getName(clazz, webService.name());
            } else {
                try {
                    Class seiClass = clazz.getClassLoader().loadClass(sei.trim());
                    return getNameFromSEI(seiClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to load SEI class: " + sei);
                }
            }
        }
        
    }
        
    private static String getNameFromSEI(Class seiClass) {
        WebService webService = 
            (WebService)seiClass.getAnnotation(WebService.class);
        if (webService == null) {
            throw new IllegalArgumentException("The " + seiClass.getName() + " is not annotated");
        } 
        return getName(seiClass, webService.name());
    }
    
    private static String getName(Class clazz, String name) {
        if (name == null || name.trim().length() == 0) {
            return clazz.getSimpleName();
        } else {
            return name.trim();
        }  
    }
    
}
