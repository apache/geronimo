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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.geronimo.common.AbstractComponent;
import org.w3c.dom.Document;

/* -------------------------------------------------------------------------------------- */
/**
 * AbstractWebApplication
 * 
 * Instances are created by a deployer. The deployer finds the 
 * WebContainer and associates it with the WebApplication.
 * 
 * @version $Revision: 1.5 $ $Date: 2003/08/27 10:32:05 $
 */
public abstract class AbstractWebApplication
    extends AbstractComponent
    implements WebApplication
{
    /**
     * Class loading delegation model 
     */
    private boolean java2ClassloadingCompliance = false;

    private URI webXmlURI = null;

    private Document webXmlDoc = null;
    private DocumentBuilder parser = null;

    private URI geronimoXmlURI = null;
    private Document geronimoXmlDoc = null;
    private Context initialContext = null;

    /* -------------------------------------------------------------------------------------- */
    /** Constructor
     * @param location uri of webapp war or dir
     * @param context context path for webapp
     */
    public AbstractWebApplication()
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            parser = factory.newDocumentBuilder();
            initialContext = new InitialContext();
        }
        catch (FactoryConfigurationError e)
        {
            throw new AssertionError("No XML parser available");
        }
        catch (ParserConfigurationException e)
        {
            throw new AssertionError("No XML parser available");
        }
        catch (NamingException e)
        {
            throw new AssertionError("No initial JNDI context");
        }
    }

    /* -------------------------------------------------------------------------------------- */
    /* Start the webapp. Called by the container or management interface
     * @throws Exception
     * @throws IllegalStateException
     * @see org.apache.geronimo.common.Component#start()
     */
    public void doStart() throws Exception
    {
        if (getContainer() == null)
            throw new IllegalStateException("WebApplication must have a container set before START can be called");

        //probably not necessary if deployer wil do this on our behalf
        // set up the JNDI context for this webapp 
        //setupENC();
        
        //setupClassLoader();
    }

    /* -------------------------------------------------------------------------------------- */
    /* Stop the webapp. Called by the container, or by mangement
     * interface
     * 
     * @see org.apache.geronimo.common.Component#stop()
     */
    public void doStop()
    {

    }

    /* -------------------------------------------------------------------------------------- */
    /* Return the list of Servlets of this webapp
     * @return
     * @see org.apache.geronimo.web.WebApplication#getServlets()
     */
    public String[] getServlets()
    {
        Document doc = getDeploymentDescriptorDocument();
        return null;
    }

    /* -------------------------------------------------------------------------------------- */
    /* 
     * @return
     * @see org.apache.geronimo.web.WebApplication#getDeploymentDescriptor()
     */
    public String getDeploymentDescriptor()
    {
        try
        {
            parseWebXml();

        }
        catch (Exception e)
        {
            throw new IllegalStateException(e.toString());
        }

        return null;
    }

    /* -------------------------------------------------------------------------------------- */
    /* Get the URI of the web.xml deployment descriptor
     * @return
     * @see org.apache.geronimo.web.WebApplication#getDeploymentDescriptorURI()
     */
    public URI getDeploymentDescriptorURI()
    {
        return webXmlURI;
    }

    /* -------------------------------------------------------------------------------------- */
    /* Get the Document representing the web.xml descriptor
     * @return
     * @see org.apache.geronimo.web.WebApplication#getDeploymentDescriptorDocument()
     */
    public Document getDeploymentDescriptorDocument()
    {
        //TODO - may not be required depending on deployer interface
        try
        {
            parseWebXml();
            return webXmlDoc;
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e.toString());
        }

    }


    //the geronimo methods may not be required, depending on the interface to the deployer
    public URI getGeronimoDeploymentDescriptorURI()
    {
        return geronimoXmlURI;
    }

    public Document getGeronimoDeploymentDescriptorDocument()
    {
        return geronimoXmlDoc;
    }

    public String getGeronimoDeploymentDescriptor()
    {
        //TODO
        return null;
    }
  
  
    /* -------------------------------------------------------------------------------------- */
    /** Setter for classloading compliance. If true, then classloading will
     * delegate first to parent classloader a la Java2 spec. If false, then
     * webapps wanting to load class will try their own context class loader first.
     * @param state
     */
    public void setJava2ClassloadingCompliance(boolean state)
    {
        java2ClassloadingCompliance = state;
    }

    /* -------------------------------------------------------------------------------------- */
    /**Getter for classloading compliance.
     * @see setJava2ClassloadingCompliance
     * @return
     */
    public boolean getJava2ClassloadingCompliance()
    {
        return java2ClassloadingCompliance;
    }

    /* -------------------------------------------------------------------------------------- */
    /** Parse the deployment descriptor, if it hasn't been already
     *
     * @exception Exception if an error occurs
     */
    protected synchronized void parseWebXml() throws Exception
    {
        if (webXmlURI == null)
            return;

        if (webXmlDoc != null)
            return;

        webXmlDoc = parser.parse(webXmlURI.toString());
    }

    protected synchronized void parseGeronimoXml() throws Exception
    {
        if (geronimoXmlURI == null)
            return;
        if (geronimoXmlDoc != null)
            return;

        geronimoXmlDoc = parser.parse(geronimoXmlURI.toString());
    }

    /*
        protected void setupENC() throws Exception
        {
            //parse the standard descriptor
            parseWebXml();
    
            //parse the geronimo web descriptor?
            parseGeronimoXml();
    
            //create the java:comp/env context
            Context enc = null;
            
            //populate the resources
    
            //populate the ejbs
    
            //populate the UserTransaction
            enc.bind ("UserTransaction",  new LinkRef ("javax.transaction.UserTransaction"));
            
            //populate the security
    
            //secure the context as read-only (if necessary)
    
        }
        */

}
