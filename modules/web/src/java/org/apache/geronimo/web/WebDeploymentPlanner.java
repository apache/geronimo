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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.model.geronimo.web.GeronimoWebAppDocument;
import org.apache.geronimo.deployment.model.web.WebApp;
import org.apache.geronimo.kernel.classspace.ClassSpaceUtil;
import org.apache.geronimo.kernel.deployment.AbstractDeploymentPlanner;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.DeploymentHelper;
import org.apache.geronimo.kernel.deployment.DeploymentInfo;
import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.task.CreateClassSpace;
import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.deployment.task.DestroyMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StartMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StopMBeanInstance;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.ReferenceFactory;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.transaction.manager.UserTransactionImpl;
import org.apache.geronimo.xml.deployment.GeronimoWebAppLoader;
import org.apache.geronimo.xml.deployment.LoaderUtil;
import org.apache.geronimo.xml.deployment.WebAppLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/12/30 08:28:57 $
 *
 * */
public class WebDeploymentPlanner extends AbstractDeploymentPlanner {


    private final static Log log = LogFactory.getLog(WebDeploymentPlanner.class);
    /**
     * We delegate to this guy pending refactoring
     */
    private String webApplicationClass;
    private String containerName;


    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = AbstractDeploymentPlanner.getGeronimoMBeanInfo(WebDeploymentPlanner.class.getName());
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("WebApplicationClass", true, true, "Name of the class of WebApplications"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ContainerName", true, true, "Name of the Web Container"));
        return mbeanInfo;
    }

    public String getWebApplicationClass() {
        return webApplicationClass;
    }

    public void setWebApplicationClass(String webApplicationClass) {
        this.webApplicationClass = webApplicationClass;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * Something to be deployed.
     * We will deploy it if it looks and smells like a webapp. That is, it
     * is either a packed war with a ".war" suffix or a directory, and
     * in either case contains a WEB-INF/web.xml file.
     */
    protected boolean addURL(DeployURL deployURL, Set goals, Set plans) throws DeploymentException {

        URL url = deployURL.getUrl();
        DeploymentHelper deploymentHelper = new DeploymentHelper(url, deployURL.getType(), "WebApplication", "web.xml", "geronimo-web.xml", "WEB-INF");
        URL geronimoDDURL = deploymentHelper.locateGeronimoDD();
        URL webDDURL = deploymentHelper.locateJ2EEDD();

        // Is the specific URL deployable?
        if (null == geronimoDDURL) {
//            log.info("Looking at and rejecting url " + url);
            return false;
        }
        URI baseURI = URI.create(url.toString()).normalize();

        log.trace("Planning the web module deployment " + url);

        // One can deploy the specified URL. One removes it from the current deployURL set.
        //goals.remove(deployURL);

        ObjectName deploymentUnitName = deploymentHelper.buildDeploymentName();

        // Add a task to deploy and start the deployment unit mbean
        DeploymentPlan deploymentInfoPlan = DeploymentInfo.planDeploymentInfo(getServer(), null, deploymentUnitName, null, url);

        // Define the ClassSpace for the archives.
        ClassSpaceMetadata classSpaceMetaData = deploymentHelper.buildClassSpace();
        // @todo we need to be in the ear's classspace
        deploymentInfoPlan.addTask(new CreateClassSpace(getServer(), classSpaceMetaData));
        plans.add(deploymentInfoPlan);

        // Load the geronimo-web.xml descriptor into our POJO
        log.trace("Loading deployment descriptor " + geronimoDDURL.toString());

        GeronimoWebAppDocument geronimoWebAppDoc = null;
        try {
            Document document = LoaderUtil.parseXML(new InputStreamReader(geronimoDDURL.openStream()));
            geronimoWebAppDoc = GeronimoWebAppLoader.load(document);
        } catch (FileNotFoundException e) {
//            throw new DeploymentException("geronimo-web.xml not found", e);
        } catch (SAXException e) {
            throw new DeploymentException("geronimo-web.xml malformed", e);
        } catch (IOException e) {
            throw new DeploymentException("Deployment descriptor not readable", e);
        }

        //load the web.xml descriptor into a POJO
        WebApp webAppDoc = null;
        try {
            Document doc = LoaderUtil.parseXML (new InputStreamReader (webDDURL.openStream()));
            webAppDoc = WebAppLoader.load(doc);
        } catch (FileNotFoundException e) {
            throw new DeploymentException ("web.xml file not found", e);
        }catch (SAXException e) {
            throw new DeploymentException ("web.xml malformed", e);
        } catch (IOException e) {
            throw new DeploymentException ("web.xml not readable", e);
        }

        ObjectName webApplicationName = getWebApplicationObjectName(baseURI);

        // Create a deployment plan for the webapp
        DeploymentPlan webApplicationPlan = new DeploymentPlan();

        WebApplicationContext webApplicationContext = new WebApplicationContext();
        webApplicationContext.uri = baseURI;
        // @todo we need to be in the ear's classspace
        //todo one of these is wrong for sure
        //now we are using jetty classloading.
        //webApplicationContext.classLoader = ClassSpaceUtil.getClassLoader(getServer(), classSpaceMetaData.getName());
        webApplicationContext.parentClassLoader = ClassSpaceUtil.getClassLoader(getServer(), classSpaceMetaData.getName());
        webApplicationContext.webApp = webAppDoc;
        webApplicationContext.geronimoWebAppDoc = geronimoWebAppDoc;
        webApplicationContext.contextPath = getContextPath(baseURI);
        webApplicationContext.userTransaction = new UserTransactionImpl();
        webApplicationContext.context = getComponentContext(geronimoWebAppDoc, webApplicationContext.userTransaction);

        MBeanMetadata metadata = new MBeanMetadata(webApplicationName,
                null,
                classSpaceMetaData.getName(),
                deploymentUnitName,
                new Object[] {webApplicationContext},
                new String[] {WebApplicationContext.class.getName()});

        metadata.setGeronimoMBeanDescriptor(webApplicationClass);

        webApplicationPlan.addTask(new DeployGeronimoMBean(getServer(), metadata));



        // Add a task to start the webapp which will finish configuring it
        webApplicationPlan.addTask(new StartMBeanInstance(getServer(), metadata));
        plans.add(webApplicationPlan);

        goals.remove(deployURL);
        return true;
    }


    private ObjectName getWebApplicationObjectName(URI baseURI) throws DeploymentException {
        try {
            return ObjectName.getInstance(AbstractWebContainer.BASE_WEB_APPLICATION_NAME + AbstractWebContainer.CONTAINER_CLAUSE + containerName + ",module=" + ObjectName.quote(baseURI.toString()));
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Could not construct ObjectName for web application", e);
        }
    }

    private String getContextPath(URI baseURI) {
        String path = baseURI.getPath();

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        int sepIndex = path.lastIndexOf('/');
        if (sepIndex > 0) {
            path = path.substring(sepIndex + 1);
        }

        if (path.endsWith(".war")) {
            path = path.substring(0, path.length() - 4);
        }

        return "/" + path;
    }


    private  ReadOnlyContext getComponentContext(GeronimoWebAppDocument geronimoWebAppDoc, UserTransaction userTransaction) throws DeploymentException {
        if (geronimoWebAppDoc != null) {
            ReferenceFactory referenceFactory = new JMXReferenceFactory(JMXKernel.getMBeanServerId(getServer()));
            ComponentContextBuilder builder = new ComponentContextBuilder(referenceFactory, userTransaction);
            ReadOnlyContext context = builder.buildContext(geronimoWebAppDoc.getWebApp());
            return context;
        } else {
            return null;
        }
    }

    /**
     * Remove the deployment of a webapp.
     *
     */
    protected boolean removeURL(UndeployURL undeployURL, Set goals, Set plans) throws DeploymentException {
        //work out what the name of the deployment would be, assuming it is a webapp
        URL url = undeployURL.getUrl();

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
        if (!getServer().isRegistered(deploymentName)) {
            log.debug("No deployment registered at: " + deploymentName);
            return false;
        }

        log.debug("Deployment is registered");

        //It is a webapp, and it is registered, so we should maybe undeploy it
        //so find out if it was in fact us that deployed it (could have been a different web container)
        Collection deploymentChildren = null;

        //deploymentChildren = dependencyService.getStartChildren(deploymentName);

        List webapps = new ArrayList();

        log.debug("there are " + deploymentChildren.size() + " children");

        Iterator itor = deploymentChildren.iterator();
        try {
            while (itor.hasNext()) {
                ObjectName childName = (ObjectName) itor.next();

                if (getServer().isInstanceOf(childName, WebApplication.class.getName())
                        &&
                        (getServer().getAttribute(childName, "Container") == this)) {
                    log.debug("Adding webapp for removal: " + childName);
                    webapps.add(childName);
                } else {
                    log.debug("Skipping " + childName);
                    log.debug("Container=" + getServer().getAttribute(childName, "Container") + "this=" + this);
                    log.debug("Webapp=" + getServer().isInstanceOf(childName, WebApplication.class.getName()));
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

        //put in a stoptask for the deployment unit, which will also stop the webapp(s)
        //because of the dependency between them
        DeploymentPlan stopPlan = new DeploymentPlan();
        stopPlan.addTask(new StopMBeanInstance(getServer(), deploymentName));
        plans.add(stopPlan);

        //destroy each of the webapps
        DeploymentPlan removePlan = new DeploymentPlan();

        itor = webapps.iterator();
        while (itor.hasNext()) {
            //unregister it
            ObjectName webappName = (ObjectName) itor.next();
            removePlan.addTask(new DestroyMBeanInstance(getServer(), webappName));

            //now remove it from the container
            //removePlan.addTask(new RemoveWebApplication(getServer(), this, (WebApplication) webAppMap.get(webappName.toString())));
        }

        //unregister the deployment itself
        removePlan.addTask(new DestroyMBeanInstance(getServer(), deploymentName));

        plans.add(removePlan);
        goals.remove(undeployURL);
        return true;
    }

    /**
     * Handle a redeployment.
     *
     * This is going to be tricky, as I believe the Scanner just always
     * inserts a redeploy undeployURL if it scans the directory and finds the same
     * url as was there previously - I don't think it is checking the timestamps.
     */
    protected boolean redeployURL(RedeployURL redeployURL, Set goals) throws DeploymentException {
        //TODO
        goals.remove(redeployURL);
        return true;
    }
}
