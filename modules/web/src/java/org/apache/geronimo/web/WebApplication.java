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
import org.apache.geronimo.core.service.Component;
import org.apache.geronimo.kernel.management.WebModule;



/**
 * The WebApplication interface represents a web application deployable within a WebContainer.
 *
 * It supports the J2EE Management WebModule attributes 
 *
 * @version  $Revision: 1.10 $ $Date: 2003/10/30 07:47:05 $
 */
public interface WebApplication extends Component, WebModule {
 
    /*-------------------------------------------------------------------------------- */
    /**Get the uri of the webapp
     * @return the URI of the webapp deployment
     */
    public URI getURI();

    
    /* -------------------------------------------------------------------------------------- */
    /** Setter for the parent classloader for this webapp
     * @param loader
     */
    public void setParentClassLoader (ClassLoader loader);

    /* -------------------------------------------------------------------------------------- */
    /** Getter for the parent classloader for this webapp
     * @param loader
     */
    public ClassLoader getParentClassLoader ();

    


    /* -------------------------------------------------------------------------------------- */
    /**Get the context path for the webapp
     * @return 
     */
    public String getContextPath();


    /* -------------------------------------------------------------------------------------- */
    /**Set the context path for the webapp
     * @return 
     */
    public void setContextPath(String path);


    /*-------------------------------------------------------------------------------- */
     /** JSR077 WebModule method to expose the
      *  contents of the webapp's web.xml file
      *
      * @return the contents of the web.xml as a string
      */
    public String getDeploymentDescriptor();
    
    /* -------------------------------------------------------------------------------------- */
     /**Getter for the class loader delegation model for this webapp
      * @return
      */
     public boolean getJava2ClassloadingCompliance ();
     
    /* -------------------------------------------------------------------------------------- */
    /**Set the class loading delegation model for this web application
     * @param state
     */
    public void setJava2ClassloadingCompliance(boolean state);
}
