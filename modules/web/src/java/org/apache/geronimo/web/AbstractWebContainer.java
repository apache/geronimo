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


import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.relation.RelationServiceMBean;
import javax.management.relation.Role;
import org.apache.geronimo.core.service.AbstractContainer;
import org.apache.geronimo.core.service.Component;
import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.DeploymentGoal;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.scanner.URLType;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.task.RegisterMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StartMBeanInstance;
import org.apache.geronimo.kernel.management.State;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * AbstractWebContainer
 *
 * Base class for web containers.
 * @jmx:mbean extends="org.apache.geronimo.kernel.deployment.DeploymentPlanner, org.apache.geronimo.web.WebContainer, org.apache.geronimo.kernel.management.StateManageable, javax.management.MBeanRegistration" 
 * @version $Revision: 1.9 $ $Date: 2003/09/28 22:30:58 $
 */
public abstract class AbstractWebContainer
    extends AbstractContainer
    implements WebContainer, AbstractWebContainerMBean, MBeanRegistration
{
    private final static Log log = LogFactory.getLog(AbstractWebContainer.class);

    /**
     * Location of the default web.xml file
     */
    private URI defaultWebXmlURI = null;

    /**
     * Parsed default web.xml
     */
    private Document defaultWebXmlDoc = null;

    
   

    /* -------------------------------------------------------------------------------------- */
    /**
     *  Constructor
     */
    public AbstractWebContainer()
    {   
    }


    /**
     * Get our mbean name from a pre-registration call.
     *
     * @param mBeanServer a <code>MBeanServer</code> value
     * @param objectName an <code>ObjectName</code> value
     * @return an <code>ObjectName</code> value
     * @exception Exception if an error occurs
     */
   public ObjectName preRegister(MBeanServer server, ObjectName objectName) 
       throws Exception 
    
    {
        return super.preRegister(server, objectName);
    }


    /**
     * Establish this webcontainer as a deployment planner
     *
     * @param aBoolean a <code>Boolean</code> value
     */
    public void postRegister(Boolean aBoolean) {
        try {
            super.postRegister(aBoolean);
            List planners = relationService.getRole("DeploymentController-DeploymentPlanner", "DeploymentPlanner");

            planners.add(objectName);
            relationService.setRole("DeploymentController-DeploymentPlanner",
                                    new Role("DeploymentPlanner", planners));
            log.trace ("Registered WebContainer as a DeploymentPlanner");
        } catch (Exception e) {
            IllegalStateException e1 = new IllegalStateException();
            e1.initCause(e);
            throw e1;
        }
    }


    /**
     * Do nothing for now
     *
     * @exception Exception if an error occurs
     */
    public void preDeregister() throws Exception {
    }



    /**
     * Do nothing for now
     *
     */
    public void postDeregister() {
    }


    /**
     * Monitor JMX notifications to find Web components
     *
     * @param n a <code>Notification</code> value
     * @param o an <code>Object</code> value
     */
    public void handleNotification(Notification n, Object o) 
    {
        ObjectName source = null;

        try
        {       

            //check for registrations of web connectors and web logs
            if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(n.getType())) 
            {
                MBeanServerNotification notification = (MBeanServerNotification) n;
                source = notification.getMBeanName();
                if (server.isInstanceOf (source, WebConnector.class.getName()))
                {
                    log.debug ("Received registration notification for webconnecter="+source);

                    // set up the Container on the Connector to be us
                    // this will result in the Connector adding itself to
                    // our containment hierarchy
                    server.setAttribute (source, 
                                         new Attribute ("Container",
                                                        (Container)this));
                   
                }
                else if (server.isInstanceOf (source, WebAccessLog.class.getName()))
                {
                    // set up the Container on the WebAccessLog to be us
                    // this will result in the WebAccessLog adding itself to
                    // our containment hierarchy 
                    log.debug ("Received registration notification for weblog="+source);
                    server.setAttribute (source, 
                                   new Attribute ("Container",
                                                  (Container)this));
                }
                else if (server.isInstanceOf (source, WebApplication.class.getName()))
                {
                    //add the webapp as a component of the container 
                    log.debug ("Received registration notification for webapplication="+source);
                    server.setAttribute (source, 
                                         new Attribute ("Container",
                                                        (Container)this));
                }
                else
                    log.debug ("Ignoring registration of mbean="+source);
            }
        }
        catch (InstanceNotFoundException e)
        {
            log.debug ("Registration notification received for non-existant object: "+ source);
        }
        catch (MBeanException e)
        {
            throw new IllegalStateException (e.toString());
        }
        catch (ReflectionException e)
        {
            throw new IllegalStateException (e.toString());
        }
        catch (Exception e)
        {
            throw new IllegalStateException (e.toString());
        }

        super.handleNotification (n, o);
    }





    /**
     * Deploy/redeploy/undeploy a webapplication
     *
     * @param goals a <code>Set</code> value
     * @param plans a <code>Set</code> value
     * @return a <code>boolean</code> value
     * @exception DeploymentException if an error occurs
     */
    public boolean plan(Set goals, Set plans) throws DeploymentException
    {
        boolean progress = false;
        Set x = new HashSet(goals);
        for (Iterator i = x.iterator(); i.hasNext();) {
            DeploymentGoal goal = (DeploymentGoal) i.next();
            if (goal instanceof DeployURL) {
                progress = deploy((DeployURL) goal, goals, plans);
            } else if (goal instanceof RedeployURL) {
                progress = redeploy((RedeployURL) goal, goals);
            } else if (goal instanceof UndeployURL) {
                progress = remove((UndeployURL) goal, goals, plans);
            }
        }
        return progress;
    }



    /**
     * Fresh deployment to handle
     *
     * @param goal a <code>DeployURL</code> value
     * @param goals a <code>Set</code> value
     * @param plans a <code>Set</code> value
     * @return a <code>boolean</code> value
     * @exception DeploymentException if an error occurs
     */
    public  boolean deploy (DeployURL goal, Set goals, Set plans) throws DeploymentException {

        if (getStateInstance() != State.RUNNING)
            throw new DeploymentException ("WebContainer is not in RUNNING state");

        InputStream is;
        URL url = goal.getUrl();
        URI baseURI = URI.create(url.toString()).normalize();;

        URLType type = goal.getType();
        URL webXmlURL = null;
 

        //check if this is a deployable webapp. This is either a directory or a
        //war file that contains a WEB-INF directory
        if (type == URLType.PACKED_ARCHIVE) 
        {
            //check it ends with ".war" 
            if (!url.getPath().endsWith(".war"))
                return false;

            InputStream stream = null;
            try
            {
                URL webInfURL = new URL ("jar:"+url.toExternalForm()+"!/WEB-INF/web.xml");
                stream = webInfURL.openStream();
            }
            catch (IOException e) 
            {
                throw new DeploymentException("Failed to open stream for URL: " + url, e);
            }
            finally
            {
                try
                {
                    if (stream != null)
                        stream.close();
                }
                catch (IOException iox)
                {
                    throw new DeploymentException ("Failed to close stream for URL: "+url, iox);
                }
            }
        }
        else if (type == URLType.UNPACKED_ARCHIVE) 
        {
            // check if there is a WEB-INF
            InputStream stream = null;
            try
            {
                URL webInfURL = new URL (url, "WEB-INF/web.xml");
                stream = webInfURL.openStream();
            }
            catch (IOException e) 
            {
                throw new DeploymentException("Failed to open stream for URL: " + url, e);
            }
            finally
            {
                try
                {
                    if (stream != null)
                        stream.close();
                }
                catch (IOException iox)
                {
                    throw new DeploymentException ("Failed to close stream for URL: "+url, iox);
                }
            }
        } 
        else 
        {
            //we can't deploy any other structure
            return false;
        }

        log.debug (Thread.currentThread()+":Identified webapp to deploy");

        //check to see if there is already a deployment for the  webapp 
        ObjectName deploymentName = null;
        try 
        {
            deploymentName = new ObjectName("geronimo.deployment:role=DeploymentUnit,url=" 
                                            + ObjectName.quote(url.toString())+",type=WebApplication");
        } 
        catch (MalformedObjectNameException e) 
        {
            throw new DeploymentException(e);
        }

        if (server.isRegistered(deploymentName))
            throw new DeploymentException ("A web application deployment is already registered at:"+deploymentName.toString());
        
        //Set up the deployment itself in JMX
        DeploymentPlan deploymentUnitPlan = new DeploymentPlan();
        WebDeployment deploymentInfo = new WebDeployment (deploymentName, null, url);
        deploymentUnitPlan.addTask (new RegisterMBeanInstance(server, deploymentName, deploymentInfo));
        MBeanMetadata deploymentUnitMetadata = new MBeanMetadata (deploymentName);
        deploymentUnitPlan.addTask (new StartMBeanInstance (server, deploymentUnitMetadata));
        plans.add (deploymentUnitPlan);
        
        
        //Deploy the webapp itself
        // create a webapp typed to the concrete type of the web container
        WebApplication webapp = createWebApplication (baseURI);
        ObjectName webappName;
        try
        {
            webappName = new ObjectName (webapp.getObjectName());
        }
        catch (MalformedObjectNameException e)
        {
            throw new DeploymentException (e);
        }
      
        // Create a deployment plan for the webapp
        DeploymentPlan webappPlan = new DeploymentPlan();
        webappPlan.addTask (new RegisterMBeanInstance(server, webappName, webapp));
     
        MBeanMetadata webappMetadata = new MBeanMetadata (webappName);
        webappMetadata.setParentName (deploymentName);                
        dependencyService.addStartDependency(webappName, deploymentName);

        // crack open the war and read the web.xml and geronimo-web.xml into the POJOs
        //TBD


        //contextPath can come from:
        //  application.xml
        //  geronimo-web.xml
        //  name of the war (without .war extension) or webapp dir
        //For now, only support the name of the webapp
        webapp.setContextPath(extractContextPath (baseURI));

        // Set up the parent classloader for the webapp
        webapp.setParentClassLoader (getClass().getClassLoader());



        //Set up the ENC etc
        //TBD

        
        //Add a task to start the webapp which will finish configuring it
        //NOTE: the class loader will be null, which will cause the
        //mbean server's class loader to be used for the start call.
        //Is this going to be a problem? 
        webappPlan.addTask (new StartMBeanInstance (server, webappMetadata));
        plans.add (webappPlan);
        
        goals.remove(goal);
        return true;
    }


    // TODO
    private boolean remove(UndeployURL goal, Set goals, Set plans) throws DeploymentException { 
        goals.remove(goal);
        return true;
    }

    // TODO
    private boolean redeploy(RedeployURL goal, Set goals) throws DeploymentException {
        goals.remove(goal);
        return true;
    }


    /**
     * Create a WebApplication suitable to the container's type.
     * @return WebApplication instance, preferably derived from AbstractWebApplication suitable to the container
     */
    public abstract WebApplication createWebApplication(URI baseURI);

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
    public void setDefaultWebXmlURI(URI uri)
    {
        log.debug ("DefaultWebXmlURI="+(uri==null?"null":uri.toString()));
        defaultWebXmlURI = uri;
        
    }

    /**
     * Get the parsed web defaults
     * @return
     */
    public Document getDefaultWebXmlDoc() {
        return defaultWebXmlDoc;
    }


    /**
     * Get a webapp context path from its uri
     *
     * @param uri an <code>URI</code> value
     * @return a <code>String</code> value
     */
    public String extractContextPath (URI uri)
    {

        String path = uri.getPath();
        
        if (path.endsWith("/"))
            path = path.substring (0, path.length() - 1);

        int sepIndex = path.lastIndexOf ('/');
        if (sepIndex > 0)
            path = path.substring (sepIndex+1);

        if (path.endsWith(".war"))
            path = path.substring (0, path.length()-4);

        return "/"+path;
    }


    /**
     * Parse the web defaults descriptor
     * @throws Exception
     */
    protected void parseWebDefaults() throws Exception {
        if (defaultWebXmlURI == null)
            return;

        //TODO
    }



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

    /**
     * Method called by addComponent after a WebConnector has been added.
     * @param connector
     */
    protected void webConnectorAdded(WebConnector connector) {
     
    }

    /**
     * Method called by addComponment after a WebApplication has been added.
     * @param connector
     */
    protected void webApplicationAdded(WebApplication connector) {
    }

    /**
     * @param log
     */
    protected void webAccessLogAdded(WebAccessLog log) {
    }

    /**
     * Method called by removeComponent before a WebConnector has been removed.
     * @param connector
     */
    protected void webConnectorRemoval(WebConnector connector) {
    }

    /**
     * Method called by removeComponment before a WebApplication has been removed.
     * @param connector
     */
    protected void webApplicationRemoval(WebApplication connector) {
    }

    /**
     * Remove an access log service from the container
     * @param log
     */
    protected void webAccessLogRemoval(WebAccessLog log) {
    }
}
