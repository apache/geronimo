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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.AbstractComponent;
import org.apache.geronimo.core.service.Container;


/**
 * AbstractWebApplication
 *
 * Instances are created by a deployer. The deployer finds the
 * WebContainer and associates it with the WebApplication.
 * 
 * @jmx:mbean extends="org.apache.geronimo.web.WebApplication, org.apache.geronimo.kernel.management.StateManageable, javax.management.MBeanRegistration" 
 * @version $Revision: 1.8 $ $Date: 2003/09/28 22:30:58 $
 */
public abstract class AbstractWebApplication extends AbstractComponent implements WebApplication {

     private final static Log log = LogFactory.getLog(AbstractWebApplication.class);

    protected URI uri; 


   /**
     * Class loading delegation model
     */
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


  

    
    public URI getURI ()
    {
        return uri;
    }

    /**
     * Return the list of Servlets of this webapp
     * @return
     * @see org.apache.geronimo.web.WebApplication#getServlets()
     */
    public String[] getServlets() {
        //TODO
        return null;
    }

    /**
     * @return
     * @see org.apache.geronimo.web.WebApplication#getDeploymentDescriptor()
     */
    public String getDeploymentDescriptor() {
        //TODO
        return null;
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


    public void setContainer (Container container)
    {
        super.setContainer(container);
        
        container.addComponent (this);
    }
  

  
}
