/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.web;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.EndpointCollection;
import org.apache.geronimo.gbean.EndpointCollectionEvent;
import org.apache.geronimo.gbean.EndpointCollectionListener;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpointListener;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.w3c.dom.Document;

/**
 * AbstractWebContainer
 *
 * Base class for web containers in Geronimo.  Integrations of existing web containers
 * such as Jetty, Tomcat et al need, in general, to subclass this class.
 *
 * The AbstractWebContainer provides the ability for concrete subclasses to
 * able to deploy web applications by registering as a DeploymentPlanner
 * with the Relationship service. This causes the DeploymentController to pass
 * the AbstractWebContainer the urls of deployments to be performed, which
 * the AbstractWebContainer accepts iff:
 * 1. the url is a packed jar whose name ends in .war and contains a WEB-INF/web.xml file
 * or
 * 2. the url is a directory which contains a WEB-INF/web.xml file
 *
 * @version $Revision: 1.29 $ $Date: 2004/01/17 17:02:38 $
 */
public abstract class AbstractWebContainer implements WebContainer {

    public final static String BASE_WEB_CONTAINER_NAME = "geronimo.web:type=WebContainer";
    public final static String BASE_WEB_APPLICATION_NAME = "geronimo.web:type=WebApplication";
    public final static String BASE_WEB_CONNECTOR_NAME = "geronimo.web:type=WebConnector";
    public final static String BASE_WEB_ACCESS_LOG_NAME = "geronimo.web:type=WebAccessLog";
    public final static String CONTAINER_CLAUSE = ",container=";

    private final static GBeanInfo GBEAN_INFO;


    private final static Log log = LogFactory.getLog(AbstractWebContainer.class);

    /**
     * Location of the default web.xml file
     */
    private URI defaultWebXmlURI = null;

    /**
     * Parsed default web.xml
     */
    private Document defaultWebXmlDoc = null;

    private EndpointCollection webApplications;
    private EndpointCollection webConnectors;
    private EndpointCollection webAccessLogs;
    private final EndpointCollectionListener webApplicationListener = new EndpointCollectionListener() {
        public void memberAdded(EndpointCollectionEvent event) {
            webApplicationAdded((WebApplication)event.getMember());
        }

        public void memberRemoved(EndpointCollectionEvent event) {
            webApplicationRemoval((WebApplication)event.getMember());
        }
    };

    private final EndpointCollectionListener webConnectorListener = new EndpointCollectionListener() {
        public void memberAdded(EndpointCollectionEvent event) {
            webConnectorAdded((WebConnector)event.getMember());
        }

        public void memberRemoved(EndpointCollectionEvent event) {
            webConnectorRemoval((WebConnector)event.getMember());
        }
    };

    private final EndpointCollectionListener webAccessLogListener = new EndpointCollectionListener() {
        public void memberAdded(EndpointCollectionEvent event) {
            webAccessLogAdded((WebAccessLog)event.getMember());
        }

        public void memberRemoved(EndpointCollectionEvent event) {
            webAccessLogRemoval((WebAccessLog)event.getMember());
        }
    };

    //deprecated, remove when GBean-only
    public AbstractWebContainer() {

    }

    public AbstractWebContainer(URI defaultWebXmlURI) {
        this.defaultWebXmlURI = defaultWebXmlURI;
    }

    /**
     * Get the URI of the web defaults.
     * @return the location of the default web.xml file for this container
     */
    public URI getDefaultWebXmlURI() {
        return defaultWebXmlURI;
    }

    /**
     * Set a uri of a web.xml containing defaults for this container.
     * @param uri the location of the default web.xml file
     */
    public void setDefaultWebXmlURI(URI uri) {
        log.debug("DefaultWebXmlURI=" + (uri == null ? "null" : uri.toString()));
        defaultWebXmlURI = uri;
    }

    /**
     * Get the parsed web defaults
     * @return the default web xml document
     */
    public Document getDefaultWebXmlDoc() {
        return defaultWebXmlDoc;
    }

    /**
     * Parse the web defaults descriptor
     * @throws Exception
     */
    protected void parseWebDefaults() throws Exception {
        if (defaultWebXmlURI == null) {
            return;
        }

        // TODO
    }

    public Collection getWebApplications() {
        return webApplications;
    }

    public void setWebApplications(Collection webApplications) {
        if (webApplications == null) {
            this.webApplications.removeEndpointCollectionListener(webApplicationListener);
            for (Iterator iterator = this.webApplications.iterator(); iterator.hasNext();) {
                webApplicationRemoval((WebApplication) iterator.next());
            }
        }
        this.webApplications = (EndpointCollection)webApplications;
        if (webApplications != null) {
            this.webApplications.addEndpointCollectionListener(webApplicationListener);
            for (Iterator iterator = this.webApplications.iterator(); iterator.hasNext();) {
                webApplicationAdded((WebApplication) iterator.next());
            }
        }
    }

    public Collection getWebConnectors() {
        return webConnectors;
    }

    public void setWebConnectors(Collection webConnectors) {
        if (webConnectors == null) {
            this.webConnectors.removeEndpointCollectionListener(webConnectorListener);
            for (Iterator iterator = this.webConnectors.iterator(); iterator.hasNext();) {
                webConnectorRemoval((WebConnector) iterator.next());
            }
        }
        this.webConnectors = (EndpointCollection)webConnectors;
        if (webConnectors != null) {
            this.webConnectors.addEndpointCollectionListener(webConnectorListener);
            for (Iterator iterator = this.webConnectors.iterator(); iterator.hasNext();) {
                webConnectorAdded((WebConnector) iterator.next());
            }
        }
    }

    public Collection getWebAccessLogs() {
        return webAccessLogs;
    }

    public void setWebAccessLogs(Collection webAccessLogs) {
        if (webAccessLogs == null) {
            this.webAccessLogs.removeEndpointCollectionListener(webAccessLogListener);
            for (Iterator iterator = this.webAccessLogs.iterator(); iterator.hasNext();) {
                webAccessLogRemoval((WebAccessLog) iterator.next());
            }
        }
        this.webAccessLogs = (EndpointCollection)webAccessLogs;
        if (webAccessLogs != null) {
            this.webAccessLogs.addEndpointCollectionListener(webAccessLogListener);
            for (Iterator iterator = this.webAccessLogs.iterator(); iterator.hasNext();) {
                webAccessLogAdded((WebAccessLog) iterator.next());
            }
        }
    }

    /**
     * Method called by addComponent after a WebConnector has been added.
     */
    protected void webConnectorAdded(WebConnector connector) {
    }

    /**
     * Method called by addComponment after a WebApplication has been added.
     */
    protected void webApplicationAdded(WebApplication app) {
    }

    protected void webAccessLogAdded(WebAccessLog log) {
    }

    /**
     * Method called by removeComponent before a WebConnector has been removed.
     */
    protected void webConnectorRemoval(WebConnector connector) {
    }

    /**
     * Method called by removeComponment before a WebApplication has been removed.
     */
    protected void webApplicationRemoval(WebApplication app) {
    }

    /**
     * Remove an access log service from the container
     */
    protected void webAccessLogRemoval(WebAccessLog log) {
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AbstractWebContainer.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("DefaultWebXmlURI", true));
        infoFactory.addAttribute(new GAttributeInfo("DefaultWebXmlDoc", true));
        infoFactory.setConstructor(new GConstructorInfo(Arrays.asList(new Object[] {"DefaultWebXmlURI", "DefaultWebXmlDoc"}),
                Arrays.asList(new Object[] {URI.class, Document.class})));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    /**
     * @deprecated
     * @param clazz
     * @param container
     * @param webApplicationClass
     * @param webConnectorClass
     * @param webAccessLogClass
     * @return
     * @throws Exception
     */
    public static GeronimoMBeanInfo getGeronimoMBeanInfo(Class clazz, String container, Class webApplicationClass, Class webConnectorClass, Class webAccessLogClass) throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(clazz);
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("DefaultWebXmlURI", true, true, "Location of web defaults"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("DefaultWebXmlDoc", true, false, "Parsed web defaults xml document"));
        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint(new GeronimoMBeanEndpointListener() {

            private AbstractWebContainer abstractWebContainer;

            public void setTarget(Object target) {
                abstractWebContainer = (AbstractWebContainer)target;
            }

            public void endpointAdded(Object endpoint) {
                abstractWebContainer.webApplicationAdded((WebApplication)endpoint);
            }

            public void endpointRemoved(Object endpoint) {
                abstractWebContainer.webApplicationRemoval((WebApplication)endpoint);
            }

        }, webApplicationClass, ObjectName.getInstance(BASE_WEB_APPLICATION_NAME + CONTAINER_CLAUSE + container + ",*")));

        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint(new GeronimoMBeanEndpointListener() {

            private AbstractWebContainer abstractWebContainer;

            public void setTarget(Object target) {
                abstractWebContainer = (AbstractWebContainer)target;
            }

            public void endpointAdded(Object endpoint) {
                abstractWebContainer.webConnectorAdded((WebConnector)endpoint);
            }

            public void endpointRemoved(Object endpoint) {
                abstractWebContainer.webConnectorRemoval((WebConnector)endpoint);
            }

        }, webConnectorClass, ObjectName.getInstance(BASE_WEB_CONNECTOR_NAME + CONTAINER_CLAUSE + container + ",*")));
        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint(new GeronimoMBeanEndpointListener() {

            private AbstractWebContainer abstractWebContainer;

            public void setTarget(Object target) {
                abstractWebContainer = (AbstractWebContainer)target;
            }

            public void endpointAdded(Object endpoint) {
                abstractWebContainer.webAccessLogAdded((WebAccessLog)endpoint);
            }

            public void endpointRemoved(Object endpoint) {
                abstractWebContainer.webAccessLogRemoval((WebAccessLog)endpoint);
            }

        }, webAccessLogClass, ObjectName.getInstance(BASE_WEB_ACCESS_LOG_NAME + CONTAINER_CLAUSE + container + ",*")));
        return mbeanInfo;
    }

}
