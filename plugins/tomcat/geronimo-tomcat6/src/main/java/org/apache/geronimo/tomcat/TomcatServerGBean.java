/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.tomcat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.system.configuration.PluginAttributeStore;
import org.apache.geronimo.system.jmx.MBeanServerReference;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.tomcat.model.ServerType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.tomcat.util.modeler.Registry;
import org.xml.sax.SAXException;

/**
 * @version $Rev$ $Date$
 */

@GBean
public class TomcatServerGBean implements GBeanLifecycle {
    public static final XMLInputFactory XMLINPUT_FACTORY = XMLInputFactory.newInstance();
    public static final JAXBContext SERVER_CONTEXT;
    private static final String DEFAULT_CATALINA_HOME = "var/catalina";
    static {
        try {
            SERVER_CONTEXT = JAXBContext.newInstance(ServerType.class);
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create jaxb contexts for plugin types", e);
        }
    }

    //server.xml as a string
    private final String serverConfig;
    private final ClassLoader classLoader;
    private final ServerInfo serverInfo;
    private final Server server;

    public TomcatServerGBean(@ParamAttribute(name = "serverConfig") String serverConfig,
                             @ParamAttribute(name = "serverConfigLocation") String serverConfigLocation,
                             @ParamAttribute(name = "catalinaHome") String catalinaHome,
                             @ParamReference(name = "ServerInfo") ServerInfo serverInfo,
                             @ParamReference(name = "AttributeManager", namingType = "AttributeStore") PluginAttributeStore attributeStore,
                             @ParamReference(name = "MBeanServerReference") MBeanServerReference mbeanServerReference,
                             @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                             @ParamSpecial(type = SpecialAttributeType.kernel) Kernel kernel) throws Exception {
        this.serverConfig = serverConfig;
        this.serverInfo = serverInfo;
        this.classLoader = classLoader;

        if(mbeanServerReference != null) {
            Registry.setServer(mbeanServerReference.getMBeanServer());
        }
        
        if (catalinaHome == null){
            catalinaHome = DEFAULT_CATALINA_HOME;
        }
        System.setProperty("catalina.home", serverInfo.resolveServerPath(catalinaHome));
        System.setProperty("catalina.base", serverInfo.resolveServerPath(catalinaHome));

        if (serverConfig == null) {
            File loc = serverInfo.resolveServer(serverConfigLocation);
            Reader in = new FileReader(loc);
            StringBuilder b = new StringBuilder();
            char[] buf = new char[1024];
            int i;
            while ((i = in.read(buf)) > 0) {
                b.append(buf, 0, i);
            }
            serverConfig = b.toString();
        }

        if (attributeStore != null) {
            serverConfig = attributeStore.substitute(serverConfig);
        }
        Reader in = new StringReader(serverConfig);

        try {
            ServerType serverType = loadServerType(in);            
            server = serverType.build(classLoader, kernel);
        } finally {
            in.close();
        }
    }

    static ServerType loadServerType(Reader in) throws ParserConfigurationException, IOException, SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = SERVER_CONTEXT.createUnmarshaller();
        XMLStreamReader xmlStream = XMLINPUT_FACTORY.createXMLStreamReader(in);
        JAXBElement<ServerType> element = unmarshaller.unmarshal(xmlStream, ServerType.class);
        ServerType serverType = element.getValue();
        return serverType;
    }

    public void doStart() throws Exception {
        ((Lifecycle)server).start();
    }

    public void doStop() throws Exception {
        ((Lifecycle)server).stop();
    }

    public void doFail() {
        try {
            doStop();
        } catch (Exception e) {
            //TODO log??
        }
    }

    public Service getService(String serviceName) {
        Service service;
        if (serviceName == null) {
            Service[] services = server.findServices();
            if (services == null || services.length == 0) throw new IllegalStateException("No services in server");

            if (services.length > 1) throw new IllegalStateException("More than one service in server.  Provide name of desired server" + Arrays.asList(services));
            service = services[0];

        } else {
            service = server.findService(serviceName);
        }
        return service;
    }

    public Server getServer() {
        return server;
    }
}
