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


import java.io.StringWriter;
import java.net.URI;

import javax.naming.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.AbstractManagedComponent;
import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.deployment.model.geronimo.web.GeronimoWebAppDocument;
import org.apache.geronimo.deployment.model.web.Servlet;
import org.apache.geronimo.deployment.model.web.WebApp;
import org.apache.geronimo.xml.deployment.WebAppLoader;
import org.apache.geronimo.xml.deployment.StorerUtil;
import org.w3c.dom.Document;


/**
 * AbstractWebApplication
 *
 * Instances are created by a deployer. The deployer finds the
 * WebContainer and associates it with the WebApplication.
 * 
 * @jmx:mbean extends="org.apache.geronimo.web.WebApplication, org.apache.geronimo.kernel.management.ManagedObject, org.apache.geronimo.kernel.management.StateManageable, javax.management.MBeanRegistration" 
 * @version $Revision: 1.10 $ $Date: 2003/11/20 09:10:17 $
 */
public abstract class AbstractWebApplication extends AbstractManagedComponent implements WebApplication {

    private final static Log log = LogFactory.getLog(AbstractWebApplication.class);

    //uri of the webapp
    protected URI uri; 
    
    // pojo for web.xml
    protected WebApp webDDObj;
    
    // pojo for geronimo-web.xml
    protected GeronimoWebAppDocument geronimoDDObj;
    
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
    private boolean java2ClassloadingCompliance = false;


    
    /**
     * Creates a new <code>AbstractWebApplication</code> instance.
     *
     */
    public AbstractWebApplication() {
    
    }
    
    
    /**
     * Creates a new <code>AbstractWebApplication</code> instance.
     *
     * @param uri uri of the webapp
     */
    public AbstractWebApplication(URI uri) {
        this.uri = uri;
    }


    
  
    
    /**
     * Start the webapp. Called by the container or management interface
     * @throws Exception
     * @throws IllegalStateException
     */
    public void doStart() throws Exception {
           
    }

    
    /**
     * Stop the webapp. Called by the container, or by mangement
     * interface
     */
    public void doStop() throws Exception {
    }


  

    
    
    /** Get the URI of this webapp
     * @return the URI of the webapp
     * @see org.apache.geronimo.web.WebApplication#getURI()
     */
    public URI getURI (){
        return uri;
    }

 

    
    /**
     * Setter for classloading compliance. If true, then classloading will
     * delegate first to parent classloader a la Java2 spec. If false, then
     * webapps wanting to load class will try their own context class loader first.
     * @param state
     */
    public void setJava2ClassloadingCompliance(boolean state) {
        java2ClassloadingCompliance = state;
    }

    
    /**
     * Getter for classloading compliance.
     * @return truen if application is using Java 2 compliant class loading
     */
    public boolean getJava2ClassloadingCompliance() {
        return java2ClassloadingCompliance;
    }


    
    /** Set the container to which this webapp belongs.
     * In turn, we add ourselves as a component to that container.
     * @param container
     * @see org.apache.geronimo.core.service.Component#setContainer(org.apache.geronimo.core.service.Container)
     */
    public void setContainer (Container container) {
        super.setContainer(container);
        container.addComponent (this);
    }
  
   
     /** JSR077
      * Return the list of Servlets of this webapp
      * @return
      * @see org.apache.geronimo.web.WebApplication#getServlets()
      */
     public String[] getServlets() {
        if (servlets == null) {
            if (webDDObj == null) 
                return null;
           
           Servlet[] servletObjs = webDDObj.getServlet();
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


    
    /* 
     * @return
     * @see org.apache.geronimo.web.WebApplication#getComponentContext()
     */
    public Context getComponentContext() {
        return context;
    }


    /* 
     * @param context
     * @see org.apache.geronimo.web.WebApplication#setComponentContext(javax.naming.Context)
     */
    public void setComponentContext(Context context) {
        this.context = context;
    }
    
    /* 
     * @return
     * @see org.apache.geronimo.web.WebApplication#getContextPath()
     */
    public String getContextPath() {
        return contextPath;
    }

 
    /* 
     * @param path
     * @see org.apache.geronimo.web.WebApplication#setContextPath(java.lang.String)
     */
    public void setContextPath(String path) {
        contextPath = path;
    }
    
    
    /* 
     * @return
     * @see org.apache.geronimo.web.WebApplication#getParentClassLoader()
     */
    public ClassLoader getParentClassLoader() {
        // TODO
        return null;
    }

    

   

   
    /* 
     * @param loader
     * @see org.apache.geronimo.web.WebApplication#setParentClassLoader(java.lang.ClassLoader)
     */
    public void setParentClassLoader(ClassLoader loader) {

    }


 
    
    /* 
     * 
     * @see org.apache.geronimo.web.WebApplication#setGeronimoDDObj()
     */
    public void setGeronimoDDObj(GeronimoWebAppDocument geronimoDDObj) {
        this.geronimoDDObj = geronimoDDObj;
    }
 
 
    /* 
     * @return
     * @see org.apache.geronimo.web.WebApplication#getGeronimoDDObj()
     */
    public GeronimoWebAppDocument getGeronimoDDObj() {
        return geronimoDDObj;
    }

    /* 
     * @param webDDObj
     * @see org.apache.geronimo.web.WebApplication#setWebDDObj(org.apache.geronimo.deployment.model.web.WebApp)
     */
    public void setWebDDObj(WebApp webDDObj) {
        this.webDDObj = webDDObj;
    }
    
    public WebApp getWebDDObj () {
        return webDDObj;
    }

 

}
