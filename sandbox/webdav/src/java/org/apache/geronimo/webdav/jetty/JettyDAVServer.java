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

package org.apache.geronimo.webdav.jetty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.webdav.DAVRepository;
import org.apache.geronimo.webdav.DAVServer;

import org.mortbay.http.HttpListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletHttpContext;

/**
 * DAVServer using under the cover a light Jetty servlet container.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/24 15:39:17 $
 */
public class JettyDAVServer implements DAVServer, GBean {
    private static final Log log = LogFactory.getLog(JettyDAVServer.class);

    /**
     * Context callback.
     */
    private GBeanContext context;
    
    /**
     * Jetty Server doing the actual work.
     */
    private final Server server;

    /**
     * DAVRepository to ServletHolder map.
     */
    private final Map repToServletHolder;

    /**
     * Connector to HttpListener map.
     */
    private final Map conToListener;

    /**
     * Repositories served by this server.
     */
    private ReferenceCollection repositories;
    private final ReferenceCollectionListener repositoryListener = new ReferenceCollectionListener() {
        public void memberAdded(ReferenceCollectionEvent event) {
            addRepository((DAVRepository) event.getMember());
		}
        
		public void memberRemoved(ReferenceCollectionEvent event) {
            removeRepository((DAVRepository) event.getMember());
		}
    };

    /**
     * Connectors injecting requests to this server.
     */
    private ReferenceCollection connectors;
    private final ReferenceCollectionListener connectorListener = new ReferenceCollectionListener() {
        public void memberAdded(ReferenceCollectionEvent event) {
            addConnector((JettyConnector) event.getMember());
        }

        public void memberRemoved(ReferenceCollectionEvent event) {
            removeConnector((JettyConnector) event.getMember());
        }
    };

    public JettyDAVServer() throws Exception {
        server = new Server();
        repToServletHolder = new HashMap();
        conToListener = new HashMap();
    }

    public void setConnectors(Collection aCollOfConnectors) {
        if (null == aCollOfConnectors) {
            connectors.removeReferenceCollectionListener(connectorListener);
            for (Iterator iter = connectors.iterator(); iter.hasNext();) {
                removeConnector((JettyConnector) iter.next());
            }
        }
        connectors = (ReferenceCollection) aCollOfConnectors;
        if (null != connectors) {
            connectors.addReferenceCollectionListener(connectorListener);
            for (Iterator iter = connectors.iterator(); iter.hasNext();) {
                addConnector((JettyConnector) iter.next());
            }
        }
    }

    public void addConnector(JettyConnector aConnector) {
        // The Connector MUST be running at this stage, otherwise a null
        // listener is returned. This is enforced by the Reference mechanism,
        // which publishes only running References.
        if (null == aConnector.getListener()) {
            throw new IllegalStateException("No defined listener.");
        }
        server.addListener(aConnector.getListener());
        synchronized (conToListener) {
            conToListener.put(aConnector, aConnector.getListener());
        }

    }

    public void removeConnector(JettyConnector aConnector) {
        // At this stage, the connector could be failed. In this later case
        // the underlying listener is undefined. Hence the conToListener Map.
        HttpListener httpListener;
        synchronized (conToListener) {
            httpListener = (HttpListener) conToListener.remove(aConnector);
        }
        if (null == httpListener) {
            throw new IllegalStateException("Connector not registered.");
        }
        server.removeListener(httpListener);
    }

    public void setRepositories(Collection aCollOfRepositories) {
        if (null == aCollOfRepositories) {
            repositories.removeReferenceCollectionListener(repositoryListener);
            for (Iterator iter = repositories.iterator(); iter.hasNext();) {
                removeRepository((DAVRepository) iter.next());
            }
        }
        repositories = (ReferenceCollection) aCollOfRepositories;
        if (null != repositories) {
            repositories.addReferenceCollectionListener(repositoryListener);
            for (Iterator iter = repositories.iterator(); iter.hasNext();) {
                addRepository((DAVRepository) iter.next());
            }
        }
    }

    /**
     * Adds a DAVRepository to this server.
     *
     * @param aRepository DAVRepository to be served by this server.
     */
    public void addRepository(DAVRepository aRepository) {
        // Gets the context associated to this repository.
        ServletHttpContext servletContext =
            (ServletHttpContext) server.getContext(aRepository.getHost(),
                aRepository.getContext());
        
        // Defines the servlet context attributes.
        Map attributes = aRepository.getServletContextAttr();
        for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
            Map.Entry attribute = (Map.Entry) iter.next();
            servletContext.setAttribute((String) attribute.getKey(),
                attribute.getValue());
        }

        ServletHolder holder = null;
        try {
            // Defines the WebDAV servlet.
            holder = servletContext.addServlet("DAVRepository", "/*", aRepository.getHandlingServlet().getName());

            // Defines the servlet init parameters.
            attributes = aRepository.getServletInitParam();
            for (Iterator iter = attributes.entrySet().iterator(); iter.hasNext();) {
                Map.Entry attribute = (Map.Entry) iter.next();
                holder.setInitParameter((String) attribute.getKey(), (String) attribute.getValue());
            }
            servletContext.start();
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }
        synchronized (repToServletHolder) {
            repToServletHolder.put(aRepository, holder);
        }
    }

    public void removeRepository(DAVRepository aRepository) {
        ServletHolder holder;
        synchronized (repToServletHolder) {
            holder = (ServletHolder) repToServletHolder.remove(aRepository);
            if (null == holder) {
                throw new IllegalArgumentException(aRepository +
                        " is not contained by " + this);
            }
        }
        holder.getHttpHandler().getHttpContext().removeHandler(holder.getHttpHandler());

        // Undefined the servlet context attributes.
        Map attributes = aRepository.getServletContextAttr();
        for (Iterator iter = attributes.keySet().iterator(); iter.hasNext();) {
            String attribute = (String) iter.next();
            holder.getHttpHandler().getHttpContext().removeAttribute(attribute);
        }
    }

    public Collection getRepositories() {
        return repositories;
    }

    public Collection getConnectors() {
        return connectors;
    }

    public void setGBeanContext(GBeanContext aContext) {
        context = aContext;        
    }

    public void doStart() throws WaitingException, Exception {
        log.info("Starting Jetty DAV Server");
        try {
            server.start();
        } catch (Exception e) {
            log.error("Can not start Jetty DAV Server", e);
            throw e;
        }
    }

    public void doStop() throws WaitingException {
        log.info("Stopping Jetty DAV Server");
        try {
            server.stop();
        } catch (Exception e) {
            log.error("Can not start Jetty DAV server", e);
        }
    }

    public void doFail() {
        log.info("Failing Jetty DAV Server");
        try {
            if (server.isStarted()) {
                server.stop();
            }
        } catch (Exception e) {
            log.error("Can not start Jetty DAV server", e);
        }
    }

    private final static GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("DAV Server - Jetty", JettyDAVServer.class.getName());
        infoFactory.addReference(new GReferenceInfo("Connectors", JettyConnector.class.getName()));
        infoFactory.addReference(new GReferenceInfo("Repositories", DAVRepository.class.getName()));
        infoFactory.addAttribute(new GAttributeInfo("Connectors", false, Boolean.TRUE, Boolean.TRUE, null, null));
        infoFactory.addAttribute(new GAttributeInfo("Repositories", false, Boolean.TRUE, Boolean.TRUE, null, null));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
