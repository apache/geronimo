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

import javax.management.ObjectName;
import javax.naming.Context;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.deployment.model.geronimo.web.GeronimoWebAppDocument;
import org.apache.geronimo.deployment.model.web.Servlet;
import org.apache.geronimo.deployment.model.web.WebApp;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.transaction.manager.UserTransactionImpl;
import org.w3c.dom.Document;


/**
 * AbstractWebApplication
 *
 * Instances are created by a deployer. The deployer finds the
 * WebContainer and associates it with the WebApplication.
 *
 * @version $Revision: 1.12 $ $Date: 2003/12/30 08:28:57 $
 */
public abstract class AbstractWebApplication implements WebApplication {

    //uri of the webapp
    protected final URI uri;

    // pojo for web.xml
    protected WebApp webApp;

    // pojo for geronimo-web.xml
    protected GeronimoWebAppDocument geronimoWebAppDoc;

    // parsed web.xml
    protected Document deploymentDescriptorDoc;

    // deployment descriptor as a string
    protected String deploymentDescriptorStr;

    //jndi context for webapp
    protected Context context;

    //servlet definitions
    protected String[] servlets;

    // context path of webapp
    protected String contextPath;

    //class loading delegation model. Default to web-app scope
    private boolean java2ClassloadingCompliance;
    private ClassLoader parentClassLoader;
    private UserTransactionImpl userTransaction;

    public AbstractWebApplication(WebApplicationContext webApplicationContext) {
        uri = webApplicationContext.uri;
        parentClassLoader = webApplicationContext.parentClassLoader;
        webApp = webApplicationContext.webApp;
        geronimoWebAppDoc = webApplicationContext.geronimoWebAppDoc;
        contextPath = webApplicationContext.contextPath;
        context = webApplicationContext.context;
        userTransaction = webApplicationContext.userTransaction;
        java2ClassloadingCompliance = webApplicationContext.java2ClassLoadingCompliance;
    }

    public TransactionManager getTransactionManager() {
        return userTransaction.getTransactionManager();
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        userTransaction.setTransactionManager(transactionManager);
    }

    public TrackedConnectionAssociator getTrackedConnectionAssociator() {
        return userTransaction.getTrackedConnectionAssociator();
    }

    public void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator) {
        userTransaction.setTrackedConnectionAssociator(trackedConnectionAssociator);
    }

    /** Get the URI of this webapp
     * @return the URI of the webapp
     * @see org.apache.geronimo.web.WebApplication#getURI()
     */
    public URI getURI (){
        return uri;
    }

    /**
     * Getter for classloading compliance. If true, then classloading will
     * delegate first to parent classloader a la Java2 spec. If false, then
     * webapps wanting to load class will try their own context class loader first.
     * @return true if application is using Java 2 compliant class loading
     */
    public boolean getJava2ClassloadingCompliance() {
        return java2ClassloadingCompliance;
    }


    /*
     * @return
     * @see org.apache.geronimo.web.WebApplication#getComponentContext()
     */
    public Context getComponentContext() {
        return context;
    }

    /*
     * @return
     * @see org.apache.geronimo.web.WebApplication#getContextPath()
     */
    public String getContextPath() {
        return contextPath;
    }

    /*
     * @return
     * @see org.apache.geronimo.web.WebApplication#getParentClassLoader()
     */
    public ClassLoader getParentClassLoader() {
        return parentClassLoader;
    }

    /*
     * @return
     * @see org.apache.geronimo.web.WebApplication#getGeronimoDDObj()
     */
    public GeronimoWebAppDocument getGeronimoWebAppDoc() {
        return geronimoWebAppDoc;
    }

    public WebApp getWebApp () {
        return webApp;
    }
    //computed info:

    /** JSR077
     * Return the list of Servlets of this webapp
     * @return
     * @see org.apache.geronimo.web.WebApplication#getServlets()
     */
    public String[] getServlets() {
       if (servlets == null) {
           if (webApp == null)
               return null;

          Servlet[] servletObjs = webApp.getServlet();
          servlets = new String[servletObjs.length];
          for (int i=0; i<servletObjs.length; i++) {
              servlets[i] = servletObjs[i].getServletName();
          }
       }

       return servlets;
    }

    /** JSR077
     * TODO: This method should be able to be implemented based on the pojos.
     *  Need a method to get from pojo->xml->string
     * @return web.xml as a string
     * @see org.apache.geronimo.web.WebApplication#getDeploymentDescriptor()
     */
    public abstract String getDeploymentDescriptor();

   /**JSR077
    * @return ObjectName(s) as string of JVM(s) on which this webapp is deployed
    * @see org.apache.geronimo.kernel.management.J2EEModule#getJavaVMs()
    */
   public String[] getJavaVMs() {
       // TODO
       return null;
   }


   /** JSR077
    * @return ObjectName as string of Geronimo server  on which this webapp is deployed
    * @see org.apache.geronimo.kernel.management.J2EEDeployedObject#getServer()
    */
   public String getServer() {
       // TODO
       return null;
   }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo(String containerName) throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        //should do this individually with comments
        mbeanInfo.addOperationsDeclaredIn(WebApplication.class);
            mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("WebContainer",
                    AbstractWebContainer.class.getName(),
                    ObjectName.getInstance(AbstractWebContainer.BASE_WEB_CONTAINER_NAME + AbstractWebContainer.CONTAINER_CLAUSE + containerName),
                    true));
            mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("TransactionManager",
                    TransactionManager.class.getName(),
                    ObjectName.getInstance("geronimo.transaction:role=TransactionManager"),
                    true));
            mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("TrackedConnectionAssociator",
                    TrackedConnectionAssociator.class.getName(),
                    ObjectName.getInstance("geronimo.connector:role=ConnectionTrackingCoordinator"),
                    true));
        return mbeanInfo;
    }

}
