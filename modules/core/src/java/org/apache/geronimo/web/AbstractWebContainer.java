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



import javax.management.ObjectName;

import org.apache.geronimo.common.AbstractContainer;
import org.apache.geronimo.common.Invocation;
import org.apache.geronimo.common.InvocationResult;



/* -------------------------------------------------------------------------------------- */
/**
 * AbstractWebContainer
 * 
 * 
 * @version $Revision: 1.1 $
 */
public class AbstractWebContainer
	extends  AbstractContainer
	implements WebContainer {
        
        private String defaultWebXmlURL = null;
	
    
    
    
    
	

    /* -------------------------------------------------------------------------------------- */
	/* Start the container
	 * @throws Exception
	 * @see org.apache.geronimo.common.AbstractComponent#doStart()
	 */
	public void doStart() throws Exception {
        
    }
	
	
    /* -------------------------------------------------------------------------------------- */
	/* Stop the container
	 * @throws Exception
	 * @see org.apache.geronimo.common.AbstractStateManageable#doStop()
	 */
	public void doStop() throws Exception {
    }


	/* -------------------------------------------------------------------------------------- */
	/* Convenience method. Creates a WebApplication from the url
     * and associates it with this container.
	 * @param url
	 * @throws Exception
	 * @see org.apache.geronimo.web.WebContainer#deploy(java.lang.String)
	 */
	public void deploy(String url) throws Exception {
	 
	}

	


	/* -------------------------------------------------------------------------------------- */
	/* Get the URL of the web defaults
	 * @return
	 * @see org.apache.geronimo.web.WebContainer#getDefaultWebXmlURL()
	 */
	public String getDefaultWebXmlURL() {
		  return defaultWebXmlURL;
	}

	
	
	/* -------------------------------------------------------------------------------------- */
	/* Set a url of a web.xml containing defaults for the continer
	 * @param url
	 * @see org.apache.geronimo.web.WebContainer#setDefaultWebXmlURL(java.lang.String)
	 */
	public void setDefaultWebXmlURL(String url) {
	   defaultWebXmlURL = url;
	}

	/* -------------------------------------------------------------------------------------- */
	/* @todo work out if this method can be delegated to some other subclass
	 * @param invocation
	 * @return
	 * @throws Exception
	 * @see org.apache.geronimo.common.Container#invoke(org.apache.geronimo.common.Invocation)
	 */
	public InvocationResult invoke(Invocation invocation) throws Exception {
		// TODO
		return null;
	}

	/* -------------------------------------------------------------------------------------- */
	/* @todo work out what relationship, if any, this has to Component
	 * @param logicalPluginName
	 * @return
	 * @see org.apache.geronimo.common.Container#getPlugin(java.lang.String)
	 */
	public ObjectName getPlugin(String logicalPluginName) {
		// TODO
		return null;
	}

	/* -------------------------------------------------------------------------------------- */
	/* @todo Work out if this is the method to add Components. 
     * 
	 * @param logicalPluginName
	 * @param objectName
	 * @see org.apache.geronimo.common.Container#putPlugin(java.lang.String, javax.management.ObjectName)
	 */
	public void putPlugin(String logicalPluginName, ObjectName objectName) {
		// TODO
		
	}

	/* -------------------------------------------------------------------------------------- */
	/* @todo work out what relationship if any this has to Components
	 * @param logicalPluginName
	 * @return
	 * @deprecated
	 * @see org.apache.geronimo.common.Container#getPluginObject(java.lang.String)
	 */
	public Object getPluginObject(String logicalPluginName) {
		// TODO
		return null;
	}

	/* -------------------------------------------------------------------------------------- */
	/* @todo workout what relationship, if any, this has to Components
	 * @param logicalPluginName
	 * @param plugin
	 * @deprecated
	 * @see org.apache.geronimo.common.Container#putPluginObject(java.lang.String, java.lang.Object)
	 */
	public void putPluginObject(String logicalPluginName, Object plugin) {
		// TODO
		
	}

}
