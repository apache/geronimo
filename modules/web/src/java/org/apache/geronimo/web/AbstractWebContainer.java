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

import org.apache.geronimo.common.AbstractContainer;
import org.apache.geronimo.common.Component;

/**
 * Base class for web containers.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/21 15:02:45 $
 */
public class AbstractWebContainer extends AbstractContainer implements WebContainer {
    /**
     * Location of the defualt web.xml file
     */
    private String defaultWebXmlURL;

    /**
     * Creates a WebApplication from the url and associates it with this container.
     * @param url the location of the web application to deploy
     * @throws Exception
     * @see org.apache.geronimo.web.WebContainer#deploy(java.lang.String)
     */
    public void deploy(String url) throws Exception {
    }

    /**
     * Get the URL of the web defaults.
     * @return the location of the default web.xml file for this container
     * @see org.apache.geronimo.web.WebContainer#getDefaultWebXmlURL()
     */
    public String getDefaultWebXmlURL() {
        return defaultWebXmlURL;
    }

    /**
     * Set a url of a web.xml containing defaults for this continer.
     * @param url the location of the default web.xml file
     * @see org.apache.geronimo.web.WebContainer#setDefaultWebXmlURL(java.lang.String)
     */
    public void setDefaultWebXmlURL(String url) {
        defaultWebXmlURL = url;
    }
    
    /* (non-Javadoc)
     * @see org.apache.geronimo.common.Container#addComponent(org.apache.geronimo.common.Component)
     */
    public void addComponent(Component component)
    {
        super.addComponent(component);
        
        if (component instanceof WebConnector)
            webConnectorAdded((WebConnector)component);
        else if (component instanceof WebApplication)
            webApplicationAdded((WebApplication)component);
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.common.Container#removeComponent(org.apache.geronimo.common.Component)
     */
    public void removeComponent(Component component) throws Exception
    {
        if (component instanceof WebConnector)
            webConnectorRemoval((WebConnector)component);
        else if (component instanceof WebApplication)
            webApplicationRemoval((WebApplication)component);
            
        super.removeComponent(component);
    }
    
    /**
     * Method called by addComponent after a WebConnector has been added.
     * @param connector
     */
    protected void webConnectorAdded(WebConnector connector)
    {
    }

    
    /**
     * Method called by addComponment after a WebApplication has been added.
     * @param connector
     */
    protected void webApplicationAdded(WebApplication connector)
    {
    }
    
    
    /**
     * Method called by addComponent before a WebConnector has been removed.
     * @param connector
     */
    protected void webConnectorRemoval(WebConnector connector)
    {
    }

    
    /**
     * Method called by removeComponment before a WebApplication has been removed.
     * @param connector
     */
    protected void webApplicationRemoval(WebApplication connector)
    {
    }
    

}
