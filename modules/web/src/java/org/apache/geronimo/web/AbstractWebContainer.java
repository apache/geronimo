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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.geronimo.common.AbstractContainer;
import org.apache.geronimo.common.Component;
import org.w3c.dom.Document;

/* -------------------------------------------------------------------------------------- */
/**
 * AbstractWebContainer
 * 
 * Base class for web containers.
 *
 * @version $Revision: 1.5 $ $Date: 2003/08/24 10:12:45 $
 */
public abstract class AbstractWebContainer
    extends AbstractContainer
    implements WebContainer
{
    /**
     * Location of the default web.xml file
     */
    private URI defaultWebXmlURI = null;

    /**
     * Parsed default web.xml 
     */
    private Document defaultWebXmlDoc = null;


    /**
     * Controls unpacking of wars to tmp runtime location
     */
    private boolean unpackWars = true;


    private final DocumentBuilder parser;



    /* -------------------------------------------------------------------------------------- */
    /**
     *  Constructor
     */
    public AbstractWebContainer()
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try
        {
            parser = factory.newDocumentBuilder();
        }
        catch (Exception e)
        {
            throw new AssertionError("No XML parser available");
        }
    }

    /* -------------------------------------------------------------------------------------- */
    /**
    * Creates a WebApplication from the url and associates it with this container.
    * @param url the location of the web application to deploy
    * @throws Exception
    * @see org.apache.geronimo.web.WebContainer#deploy(java.lang.String)
    * @todo this is likely to change when the deployment interface becomes available
    */
    public void deploy(String uri) throws Exception
    {
        //TODO what will be the interface to the deployer?
        
        //sort out the contextPath  - if the geronimo web descriptor doesn't
        //provide one, and there is no application descriptor, then it will be
        //the name of the webapp. NOTE, we need to somehow access
        //these descriptors - is it by JSR88 beans or by xml?
        String contextPath = null;
        
        //this is only necessary for compilation, the interface to the deployer will change
        URI location = new URI(uri);
        
        WebApplication webapp = createWebApplication ();
        webapp.setURI(location);
        webapp.setContextPath(contextPath);
        addComponent (webapp);
    }


    /* -------------------------------------------------------------------------------------- */
    /** Create a WebApplication suitable to the container's type.
     * @return WebApplication instance, preferably derived from AbstractWebApplication suitable to the container
     */
    public abstract WebApplication createWebApplication ();
    
    
    
    /* -------------------------------------------------------------------------------------- */
    /**
     * Get the URI of the web defaults.
     * @return the location of the default web.xml file for this container
     */
    public URI getDefaultWebXmlURI()
    {
        return defaultWebXmlURI;
    }

    /* -------------------------------------------------------------------------------------- */
    /**Set a uri of a web.xml containing defaults for this container.
     * @param uri the location of the default web.xml file
     */
    public void setDefaultWebXmlURI(URI uri)
    {
        defaultWebXmlURI = uri;
    }

    /* -------------------------------------------------------------------------------------- */
    /**Get the parsed web defaults
     * @return
     */
    public Document getDefaultWebXmlDoc()
    {
        return defaultWebXmlDoc;
    }


    /* -------------------------------------------------------------------------------------- */
    /**Parse the web defaults descriptor
     * @throws Exception
     */
    protected void parseWebDefaults() throws Exception
    {
        if (defaultWebXmlURI == null)
            return;

        defaultWebXmlDoc = parser.parse(defaultWebXmlURI.toString());
    }

    /* -------------------------------------------------------------------------------------- */
    /* 
     * @return
     * @see org.apache.geronimo.web.WebContainer#getUnpackWars()
     */
    //public boolean getUnpackWars()
    //{
    //    return unpackWars;
    //}

    /* -------------------------------------------------------------------------------------- */
    /* 
     * @param state
     * @see org.apache.geronimo.web.WebContainer#setUnpackWars(boolean)
     */
    //public void setUnpackWars(boolean state)
    //{
    //    unpackWars = state;
    //}

    /* -------------------------------------------------------------------------------------- */
    /* Add a component to this container's containment hierarchy
     * @see org.apache.geronimo.common.Container#addComponent(org.apache.geronimo.common.Component)
     */
    public void addComponent(Component component)
    {
        super.addComponent(component);
        
        if (component instanceof WebConnector)
            webConnectorAdded((WebConnector)component);
        else if (component instanceof WebApplication)
            webApplicationAdded((WebApplication)component);
        else if (component instanceof WebAccessLog)
            webAccessLogAdded ((WebAccessLog)component);
    }
    
    /* -------------------------------------------------------------------------------------- */
    /* Remove a component from this container's hierarchy
     * @see org.apache.geronimo.common.Container#removeComponent(org.apache.geronimo.common.Component)
     */
    public void removeComponent(Component component)
    {
        if (component instanceof WebConnector)
            webConnectorRemoval((WebConnector)component);
        else if (component instanceof WebApplication)
            webApplicationRemoval((WebApplication)component);
            
        super.removeComponent(component);
    }
    
    
    /* -------------------------------------------------------------------------------------- */
    /**
     * Method called by addComponent after a WebConnector has been added.
     * @param connector
     */
    protected void webConnectorAdded(WebConnector connector)
    {
    }


    /* -------------------------------------------------------------------------------------- */
    /**
     * Method called by addComponment after a WebApplication has been added.
     * @param connector
     */
    protected void webApplicationAdded(WebApplication connector)
    {
    }
    
    
    /* -------------------------------------------------------------------------------------- */
    /**
     * @param log
     */
    protected void webAccessLogAdded (WebAccessLog log)
    {
    }
    
    /* -------------------------------------------------------------------------------------- */
    /**
     * Method called by removeComponent before a WebConnector has been removed.
     * @param connector
     */
    protected void webConnectorRemoval(WebConnector connector)
    {
    }

    /* -------------------------------------------------------------------------------------- */   
    /**
     * Method called by removeComponment before a WebApplication has been removed.
     * @param connector
     */
    protected void webApplicationRemoval(WebApplication connector)
    {
    }
    
    /* -------------------------------------------------------------------------------------- */
    /** Remove an access log service from the container
     * @param log
     */
    protected void webAccessLogRemoval (WebAccessLog log)
    {
    }
}
