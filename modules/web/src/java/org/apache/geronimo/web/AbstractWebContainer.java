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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.Attribute;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.AbstractManagedContainer;
import org.apache.geronimo.core.service.Component;
import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.task.DestroyMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.RegisterMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StartMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StopMBeanInstance;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.web.deploy.RemoveWebApplication;
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
 * @jmx:mbean extends="org.apache.geronimo.web.WebContainer, org.apache.geronimo.kernel.management.StateManageable, javax.management.MBeanRegistration"
 * @version $Revision: 1.15 $ $Date: 2003/11/16 02:25:36 $
 */
public abstract class AbstractWebContainer
        extends AbstractManagedContainer
        implements WebContainer, AbstractWebContainerMBean, MBeanRegistration {
    private final static Log log =
            LogFactory.getLog(AbstractWebContainer.class);

    /**
     * Location of the default web.xml file
     */
    private URI defaultWebXmlURI = null;

    /**
     * Parsed default web.xml
     */
    private Document defaultWebXmlDoc = null;

    //this should move down to AbstractContainer
    private Map webAppMap = new HashMap();



    /* -------------------------------------------------------------------------------------- */
    /**Constructor
     */
    public AbstractWebContainer() {
    }

    /* -------------------------------------------------------------------------------------- */
    /**Monitor JMX notifications
     * When a web layer object such as a WebConnector, a WebAccessLog
     * or a WebApplication is registered in JMX, then set up the containment
     * relationship with it so that it becomes one of our components.
     * @param n a <code>Notification</code> value
     * @param o an <code>Object</code> value
     */
    public void handleNotification(Notification n, Object o) {
        ObjectName source = null;

        try {
            //Respond to registrations of WebConnectors, WebAccessLogs and WebApplications
            //Call  setContainer() on them which causes them to call addComponent() on us and
            //thus maintain the component/container hierarchy.
            if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(n.getType())) {
                MBeanServerNotification notification =
                        (MBeanServerNotification) n;
                source = notification.getMBeanName();
                if (server.isInstanceOf(source, WebConnector.class.getName())) {
                    log.debug("Received registration notification for webconnecter=" + source);
                    server.setAttribute(source, new Attribute("Container", (Container) this));
                } else if (server.isInstanceOf(source, WebAccessLog.class.getName())) {
                    log.debug("Received registration notification for weblog=" + source);
                    server.setAttribute(source, new Attribute("Container", (Container) this));
                } else if (server.isInstanceOf(source, WebApplication.class.getName())) {
                    log.debug("Received registration notification for webapplication=" + source);
                    server.setAttribute(source, new Attribute("Container", (Container) this));
                } else
                    log.debug("Ignoring registration of mbean=" + source);
            }
        } catch (InstanceNotFoundException e) {
            log.debug("Registration notification received for non-existant object: " + source);
        } catch (MBeanException e) {
            throw new IllegalStateException(e.toString());
        } catch (ReflectionException e) {
            throw new IllegalStateException(e.toString());
        } catch (Exception e) {
            throw new IllegalStateException(e.toString());
        }

        super.handleNotification(n, o);
    }



    /* -------------------------------------------------------------------------------------- */
    /**Something to be deployed.
     * We will deploy it if it looks and smells like a webapp. That is, it
     * is either a packed war with a ".war" suffix or a directory, and
     * in either case contains a WEB-INF/web.xml file.
     *
     * @param goal a <code>DeployURL</code> value
     * @param goals a <code>Set</code> value
     * @param plans a <code>Set</code> value
     * @return a <code>boolean</code> value
     * @exception DeploymentException if an error occurs
     *
     * @jmx.managed-operation
     */
    public boolean deploy(DeployURL goal, Set goals, Set plans)
            throws DeploymentException {

        if (getStateInstance() != State.RUNNING)
            throw new DeploymentException("WebContainer " + getObjectName() + " cannot deploy as it is not RUNNING");

        InputStream is;
        URL url = goal.getUrl();
        URI baseURI = URI.create(url.toString()).normalize();
        URLType type = goal.getType();
        URL webXmlURL = null;

        //check if this is a deployable webapp. This is either a directory or a war file that contains a WEB-INF directory
        if (type == URLType.PACKED_ARCHIVE) {
            InputStream stream = null;
            try {
                URL webInfURL =
                        new URL("jar:" + url.toExternalForm() + "!/WEB-INF/web.xml");
                stream = webInfURL.openStream();
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                throw new DeploymentException("Failed to open stream for URL: " + url, e);
            } finally {
                try {
                    if (stream != null)
                        stream.close();
                } catch (IOException iox) {
                    throw new DeploymentException("Failed to close stream for URL: " + url, iox);
                }
            }
        } else if (type == URLType.UNPACKED_ARCHIVE) {
            // check if there is a WEB-INF
            InputStream stream = null;
            try {
                URL webInfURL = new URL(url, "WEB-INF/web.xml");
                stream = webInfURL.openStream();
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                throw new DeploymentException("Failed to open stream for URL: " + url, e);
            } finally {
                try {
                    if (stream != null)
                        stream.close();
                } catch (IOException iox) {
                    throw new DeploymentException("Failed to close stream for URL: " + url, iox);
                }
            }
        } else if (type == URLType.RESOURCE) {
            //we will try and deploy a WEB-INF/web.xml by looking for the parent directory
            if (!url.getPath().endsWith("web.xml"))
                return false;

            try {
                File file = new File(url.toExternalForm());

                if (!file.exists())
                    throw new DeploymentException("No such file:" + url);

                File parent = file.getParentFile();
                if (!parent.getName().equals("WEB-INF"))
                    throw new DeploymentException("web.xml must be in WEB-INF directory");

                // find the parent dir of WEB-INF and try to deploy that
                parent = parent.getParentFile();
                url = parent.toURL();
            } catch (NullPointerException e) {
                throw new DeploymentException("No path to web.xml file", e);
            } catch (MalformedURLException e) {
                throw new DeploymentException("Bad url for possible webapp directory", e);
            }

        } else {
            //we can't deploy any other structure
            return false;
        }

        log.debug("Identified webapp to deploy");

        //check to see if there is already a deployment for the  webapp
        ObjectName deploymentName = null;
        try {
            deploymentName =
                    new ObjectName("geronimo.deployment:role=DeploymentUnit,url="
                    + ObjectName.quote(url.toString())
                    + ",type=WebApplication");
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }

        if (server.isRegistered(deploymentName))
            throw new DeploymentException("A web application deployment is already registered at:"
                    + deploymentName.toString());

        //Deploy the deployment unit itself. This registers an mbean for the deployment unit,
        //so we wind up with an mbean for the deployment of the webapp, as well as one for
        //the webapp itself.
        DeploymentPlan deploymentUnitPlan = new DeploymentPlan();
        WebDeployment deploymentInfo =
                new WebDeployment(deploymentName, null, url);
        deploymentUnitPlan.addTask(
                new RegisterMBeanInstance(server, deploymentName, deploymentInfo));
        MBeanMetadata deploymentUnitMetadata =
                new MBeanMetadata(deploymentName);
        deploymentUnitPlan.addTask(
                new StartMBeanInstance(server, deploymentUnitMetadata));
        plans.add(deploymentUnitPlan);


        //Create a webapp typed to the concrete type of the web container
        WebApplication webapp = createWebApplication(baseURI);
        ObjectName webappName;
        try {
            webappName = new ObjectName(webapp.getObjectName());
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }

        // Create a deployment plan for the webapp
        DeploymentPlan webappPlan = new DeploymentPlan();
        webappPlan.addTask(new RegisterMBeanInstance(server, webappName, webapp));

        //Set up a start dependency between the webapp and the deployment unit as a whole.
        //Thus, when the deployer starts the deployment, the webapp itself will be started. This
        //works better for the ServiceDeploymentPlanner, where one deployment unit can contain
        //many services to be deployed, but the Scanner is only ever going to give us individual
        //webapps
        MBeanMetadata webappMetadata = new MBeanMetadata(webappName);
        webappMetadata.setParentName(deploymentName);
        dependencyService.addStartDependency(webappName, deploymentName);

        // crack open the war and read the web.xml and geronimo-web.xml into the POJOs
        //TODO

        //Set up the ContextPath, which can come from:
        //  application.xml
        //  geronimo-web.xml
        //  name of the war (without .war extension) or webapp dir
        //
        //As we don't have any ear deployers (yet), so therefore no application.xml files,
        //nor any geronimo-web.xml files, then we will only support using the name of the
        //webapp.
        webapp.setContextPath(extractContextPath(baseURI));

        // Set up the parent classloader for the webapp
        webapp.setParentClassLoader(getClass().getClassLoader());

        //Set up the ENC etc
        //TODO

        //Add a task to start the webapp which will finish configuring it
        webappPlan.addTask(new StartMBeanInstance(server, webappMetadata));
        plans.add(webappPlan);

        goals.remove(goal);
        return true;
    }


    /* -------------------------------------------------------------------------------------- */
    /**Remove the deployment of a webapp.
     *
     * @param goal
     * @param goals
     * @param plans
     * @return
     * @throws DeploymentException
     *
     * @jmx.managed-operation
     */
    public boolean remove(UndeployURL goal, Set goals, Set plans)
            throws DeploymentException {
        //work out what the name of the deployment would be, assuming it is a webapp
        URL url = goal.getUrl();

        log.debug("WebContainer " + getObjectName() + " checking for removal of " + url);

        ObjectName deploymentName = null;
        try {
            deploymentName = new ObjectName("geronimo.deployment:role=DeploymentUnit,url="
                    + ObjectName.quote(url.toString())
                    + ",type=WebApplication");
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException(e);
        }

        //check to see if it is registered - if it isn't then either it isn't registered anymore, or
        //it wasn't a webapp, so in either case there is nothing
        //for us to do - let one of the other deployers try and handle it
        if (!server.isRegistered(deploymentName)) {
            log.debug("No deployment registered at: " + deploymentName);
            return false;
        }

        log.debug("Deployment is registered");

        //It is a webapp, and it is registered, so we should maybe undeploy it
        //so find out if it was in fact us that deployed it (could have been a different web container)
        Collection deploymentChildren = null;

        deploymentChildren = dependencyService.getStartChildren(deploymentName);

        List webapps = new ArrayList();

        log.debug("there are " + deploymentChildren.size() + " children");

        Iterator itor = deploymentChildren.iterator();
        try {
            while (itor.hasNext()) {
                ObjectName childName = (ObjectName) itor.next();

                if (server.isInstanceOf(childName, WebApplication.class.getName())
                        &&
                        (server.getAttribute(childName, "Container") == this)) {
                    log.debug("Adding webapp for removal: " + childName);
                    webapps.add(childName);
                } else {
                    log.debug("Skipping " + childName);
                    log.debug("Container=" + server.getAttribute(childName, "Container") + "this=" + this);
                    log.debug("Webapp=" + server.isInstanceOf(childName, WebApplication.class.getName()));
                }
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }


        if (webapps.isEmpty()) {
            //we didn't deploy anything so nothing for us to do
            return false;
        }

        //we have webapps to undeploy, so check if we are able to
        if (getStateInstance() != State.RUNNING)
            throw new DeploymentException("WebContainer " + getObjectName() + " cannot undeploy webapps because it is not RUNNING");

        //put in a stoptask for the deployment unit, which will also stop the webapp(s)
        //because of the dependency between them
        DeploymentPlan stopPlan = new DeploymentPlan();
        stopPlan.addTask(new StopMBeanInstance(server, deploymentName));
        plans.add(stopPlan);

        //destroy each of the webapps
        DeploymentPlan removePlan = new DeploymentPlan();

        itor = webapps.iterator();
        while (itor.hasNext()) {
            //unregister it
            ObjectName webappName = (ObjectName) itor.next();
            removePlan.addTask(new DestroyMBeanInstance(server, webappName));

            //now remove it from the container
            removePlan.addTask(new RemoveWebApplication(server, this, (WebApplication) webAppMap.get(webappName.toString())));
        }

        //unregister the deployment itself
        removePlan.addTask(new DestroyMBeanInstance(server, deploymentName));

        plans.add(removePlan);
        goals.remove(goal);
        return true;
    }




    /* -------------------------------------------------------------------------------------- */
    /**Handle a redeployment.
     *
     * This is going to be tricky, as I believe the Scanner just always
     * inserts a redeploy goal if it scans the directory and finds the same
     * url as was there previously - I don't think it is checking the timestamps.
     * @param goal
     * @param goals
     * @return
     * @throws DeploymentException
     *
     * @jmx.managed-operation
     */
    public boolean redeploy(RedeployURL goal, Set goals)
            throws DeploymentException {
        //TODO
        goals.remove(goal);
        return true;
    }


    /* -------------------------------------------------------------------------------------- */
    /**Create a WebApplication suitable to the container's type.
     * @return WebApplication instance, preferably derived from AbstractWebApplication suitable to the container
     */
    public abstract WebApplication createWebApplication(URI baseURI);




    /* -------------------------------------------------------------------------------------- */
    /**Get the URI of the web defaults.
     * @return the location of the default web.xml file for this container
     */
    public URI getDefaultWebXmlURI() {
        return defaultWebXmlURI;
    }



    /* -------------------------------------------------------------------------------------- */
    /** Set a uri of a web.xml containing defaults for this container.
     * @param uri the location of the default web.xml file
     */
    public void setDefaultWebXmlURI(URI uri) {
        log.debug("DefaultWebXmlURI=" + (uri == null ? "null" : uri.toString()));
        defaultWebXmlURI = uri;
    }


    /* -------------------------------------------------------------------------------------- */
    /** Get the parsed web defaults
     *
     * @return
     */
    public Document getDefaultWebXmlDoc() {
        return defaultWebXmlDoc;
    }



    /* -------------------------------------------------------------------------------------- */
    /**
     * Get a webapp context path from its uri
     *
     * @param uri an <code>URI</code> value
     * @return a <code>String</code> value
     */
    public String extractContextPath(URI uri) {
        String path = uri.getPath();

        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);

        int sepIndex = path.lastIndexOf('/');
        if (sepIndex > 0)
            path = path.substring(sepIndex + 1);

        if (path.endsWith(".war"))
            path = path.substring(0, path.length() - 4);

        return "/" + path;
    }


    /* -------------------------------------------------------------------------------------- */
    /**
     * Parse the web defaults descriptor
     * @throws Exception
     */
    protected void parseWebDefaults() throws Exception {
        if (defaultWebXmlURI == null)
            return;

        //TODO
    }


    /* -------------------------------------------------------------------------------------- */
    /**
     * Add a component to this container's containment hierarchy
     * @see org.apache.geronimo.core.service.Container#addComponent(org.apache.geronimo.core.service.Component)
     */
    public void addComponent(Component component) {
        super.addComponent(component);

        if (component instanceof WebConnector) {
            webConnectorAdded((WebConnector) component);
        } else if (component instanceof WebApplication) {
            webApplicationAdded((WebApplication) component);
        } else if (component instanceof WebAccessLog) {
            webAccessLogAdded((WebAccessLog) component);
        }
    }

    /* -------------------------------------------------------------------------------------- */
    /**
     *  Remove a component from this container's hierarchy
     * @see org.apache.geronimo.core.service.Container#removeComponent(org.apache.geronimo.core.service.Component)
     */
    public void removeComponent(Component component) {
        if (component instanceof WebConnector) {
            webConnectorRemoval((WebConnector) component);
        } else if (component instanceof WebApplication) {
            webApplicationRemoval((WebApplication) component);
        }

        super.removeComponent(component);
    }


    /* -------------------------------------------------------------------------------------- */
    /**
     * Method called by addComponent after a WebConnector has been added.
     * @param connector
     */
    protected void webConnectorAdded(WebConnector connector) {

    }

    /* -------------------------------------------------------------------------------------- */
    /**
     * Method called by addComponment after a WebApplication has been added.
     * @param app
     */
    protected void webApplicationAdded(WebApplication app) {
        webAppMap.put(((AbstractWebApplication) app).getObjectName(), app);
    }

    /* -------------------------------------------------------------------------------------- */
    /**
     * @param log
     */
    protected void webAccessLogAdded(WebAccessLog log) {
    }

    /* -------------------------------------------------------------------------------------- */
    /**
     * Method called by removeComponent before a WebConnector has been removed.
     * @param connector
     */
    protected void webConnectorRemoval(WebConnector connector) {
    }

    /* -------------------------------------------------------------------------------------- */
    /**
     * Method called by removeComponment before a WebApplication has been removed.
     * @param app
     */
    protected void webApplicationRemoval(WebApplication app) {
        webAppMap.remove(((AbstractWebApplication) app).getObjectName());
    }

    /* -------------------------------------------------------------------------------------- */
    /**
     * Remove an access log service from the container
     * @param log
     */
    protected void webAccessLogRemoval(WebAccessLog log) {
    }
}
