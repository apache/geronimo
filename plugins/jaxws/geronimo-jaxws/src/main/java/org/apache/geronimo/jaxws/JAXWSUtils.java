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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingType;
import javax.xml.ws.WebServiceProvider;

import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXWSUtils {

    public static final String DEFAULT_CATALOG_WEB = "WEB-INF/jax-ws-catalog.xml";
    public static final String DEFAULT_CATALOG_EJB = "META-INF/jax-ws-catalog.xml";

    private static final Logger LOG = LoggerFactory.getLogger(JAXWSUtils.class);

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
            return new QName(getNamespace(seiClass, namespace), localName);
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

    public static boolean isWebServiceProvider(Class clazz) {
        return (clazz.isAnnotationPresent(WebServiceProvider.class) && isProperWebService(clazz));
    }

    private static boolean isProperWebService(Class clazz) {
        int modifiers = clazz.getModifiers();
        return (Modifier.isPublic(modifiers) &&
                !Modifier.isFinal(modifiers) &&
                !Modifier.isAbstract(modifiers));
    }

    public static String getServiceName(Class clazz) {
        return getServiceQName(clazz).getLocalPart();
    }

    private static String getServiceName(Class clazz, String name) {
        if (name == null || name.trim().length() == 0) {
            return clazz.getSimpleName() + "Service";
        } else {
            return name.trim();
        }
    }

    private static String getPortName(Class clazz, String name, String portName) {
        if (portName == null || portName.trim().length() == 0) {
            if (name == null || name.trim().length() == 0) {
                return clazz.getSimpleName() + "Port";
            } else {
                return name + "Port";
            }
        } else {
            return portName.trim();
        }
    }

    private static String getNamespace(Class clazz, String namespace) {
        if (namespace == null || namespace.trim().length() == 0) {
            Package pkg = clazz.getPackage();
            if (pkg == null) {
                return null;
            } else {
                return getNamespace(pkg.getName());
            }
        } else {
            return namespace.trim();
        }
    }

    private static String getNamespace(String packageName) {
        if (packageName == null || packageName.length() == 0) {
            return null;
        }
        StringTokenizer tokenizer = new StringTokenizer(packageName, ".");
        String[] tokens;
        if (tokenizer.countTokens() == 0) {
            tokens = new String[0];
        } else {
            tokens = new String[tokenizer.countTokens()];
            for (int i = tokenizer.countTokens() - 1; i >= 0; i--) {
                tokens[i] = tokenizer.nextToken();
            }
        }
        StringBuilder namespace = new StringBuilder("http://");
        String dot = "";
        for (int i = 0; i < tokens.length; i++) {
            if (i == 1) {
                dot = ".";
            }
            namespace.append(dot + tokens[i]);
        }
        namespace.append('/');
        return namespace.toString();
    }

    private static QName getServiceQName(Class clazz, String namespace, String name) {
        return new QName(getNamespace(clazz, namespace), getServiceName(clazz, name));
    }

    public static QName getServiceQName(Class clazz) {
        WebService webService =
            (WebService)clazz.getAnnotation(WebService.class);
        if (webService == null) {
            WebServiceProvider webServiceProvider =
                (WebServiceProvider)clazz.getAnnotation(WebServiceProvider.class);
            if (webServiceProvider == null) {
                throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
            }
            return getServiceQName(clazz, webServiceProvider.targetNamespace(), webServiceProvider.serviceName());
        } else {
            return getServiceQName(clazz, webService.targetNamespace(), webService.serviceName());
        }
    }

    private static QName getPortQName(Class clazz, String namespace, String name, String portName) {
        return new QName(getNamespace(clazz, namespace), getPortName(clazz, name, portName));
    }

    public static QName getPortQName(Class clazz) {
        WebService webService =
            (WebService)clazz.getAnnotation(WebService.class);
        if (webService == null) {
            WebServiceProvider webServiceProvider =
                (WebServiceProvider)clazz.getAnnotation(WebServiceProvider.class);
            if (webServiceProvider == null) {
                throw new IllegalArgumentException("The " + clazz.getName() + " is not annotated");
            }
            return getPortQName(clazz, webServiceProvider.targetNamespace(), null, webServiceProvider.portName());
        } else {
            return getPortQName(clazz, webService.targetNamespace(), webService.name(), webService.portName());
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
                    throw new RuntimeException("Unable to load SEI class: " + sei, e);
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

    private static String getWsdlLocation(Class clazz) {
        WebService webService = (WebService) clazz.getAnnotation(WebService.class);
        if (webService == null) {
            WebServiceProvider webServiceProvider =
                (WebServiceProvider)clazz.getAnnotation(WebServiceProvider.class);
            if (webServiceProvider == null) { //no WebService or WebServiceProvider annotation
                return "";
            } else {
                return webServiceProvider.wsdlLocation().trim();
            }
        } else {
            return webService.wsdlLocation().trim();
        }
    }

    private static String getServiceInterface(Class clazz) {
        WebService webService = (WebService) clazz.getAnnotation(WebService.class);
        if (webService == null) {
            //WebServiceProvider doesn't support endpointInterface property (JAX-WS 2.0 sec 7.7)
            return "";
        } else {
            if (webService.endpointInterface() == null || webService.endpointInterface().trim().equals("")) {
                return "";
            } else {
                return webService.endpointInterface().trim();
            }
        }
    }

    public static String getServiceWsdlLocation(Class clazz, Bundle bundle) {
        String wsdlLocation = getWsdlLocation(clazz);
        if (wsdlLocation != null && !wsdlLocation.equals("")) {
            return wsdlLocation;
        } else { //check if the interface contains the wsdlLocation value
            String serviceInterfaceClassName = getServiceInterface(clazz);
            if (serviceInterfaceClassName != null && !serviceInterfaceClassName.equals("")) {
                try {
                    Class serviceInterfaceClass = bundle.loadClass(serviceInterfaceClassName);
                    return getWsdlLocation(serviceInterfaceClass);
                } catch (Exception e) {
                    return "";
                }
            }
        }
        return "";
    }

    public static boolean containsWsdlLocation(Class clazz, Bundle bundle) {
        String wsdlLocSEIFromAnnotation = getServiceWsdlLocation(clazz, bundle);
        if (wsdlLocSEIFromAnnotation != null && !wsdlLocSEIFromAnnotation.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    public static String getBindingURIFromAnnot(Class clazz) {
        BindingType bindingType = (BindingType) clazz.getAnnotation(BindingType.class);
        if (bindingType == null) {
            return "";
        } else {
            return bindingType.value();
        }
    }

    public static URL getOASISCatalogURL(Bundle bundle, String catalogName) {
        if (catalogName == null) {
            return null;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Checking for {} catalog in classloader", catalogName);
        }
        URL catalogURL = bundle.getResource(catalogName);
        if (catalogURL == null ) {
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Checking for {} catalog in module directory", catalogName);
                }
                int jarSeparatorIndex = catalogName.indexOf("!/"); 
                if (jarSeparatorIndex != -1 && jarSeparatorIndex < (catalogName.length() - 2)) {
                    String jarFileName = catalogName.substring(0,jarSeparatorIndex);
                    URL jarFileURL = bundle.getEntry(jarFileName);
                    if(jarFileURL != null) {
                        return new URL("jar:" + jarFileURL + "!/" + catalogName.substring(jarSeparatorIndex +2));
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Not found the entry {} in the bundle {}", jarFileName, bundle.getLocation());
                    }
                } else {
                    URL tmpCatalogURL = BundleUtils.getEntry(bundle, catalogName);
                    if (tmpCatalogURL != null) {
                        tmpCatalogURL.openStream().close();
                        catalogURL = tmpCatalogURL;
                    }
                }
            } catch (FileNotFoundException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Catalog {} is not present in the module", catalogName);
                }
            } catch (IOException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.warn("Failed to open catalog file: " + catalogURL, e);
                }
            }
        }
        return catalogURL;
    }

    public static URL getWsdlURL(Bundle bundle, String wsdlFile) {
        if (wsdlFile == null) {
            return null;
        }
        URL wsdlURL = null;
        try {
            /**
             * Geronimo now installs the whole EAR as one bundle, and the nested WARs are extracted, while EJB are left as Jar files.
             * If the WSDL file is included in the EJB, it is required to get the Jar file entry firstly, then use jar protocol to load the WSDL
             * file.
             */
            int jarSeparatorIndex = wsdlFile.indexOf("!/");
            if (jarSeparatorIndex != -1 && jarSeparatorIndex < (wsdlFile.length() - 2)) {
                String jarFileName = wsdlFile.substring(0, jarSeparatorIndex);
                URL jarFileURL = bundle.getEntry(jarFileName);
                if (jarFileURL != null) {
                    wsdlURL = new URL("jar:" + jarFileURL + "!/" + wsdlFile.substring(jarSeparatorIndex + 2));
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Not found the entry {} in the bundle {}", jarFileName, bundle.getLocation());
                }
            } else {
                wsdlURL = BundleUtils.getEntry(bundle, wsdlFile);
            }
        } catch (MalformedURLException e) {
            LOG.warn("MalformedURLException when getting entry:" + wsdlFile + " from bundle " + bundle.getSymbolicName(), e);
            wsdlURL = null;
        }
        if (wsdlURL == null) {
            wsdlURL = bundle.getResource(wsdlFile);
            if (wsdlURL == null) {
                try {
                    wsdlURL = new URL(wsdlFile);
                } catch (MalformedURLException e) {
                }
            }
        }
        return wsdlURL;
    }
}
