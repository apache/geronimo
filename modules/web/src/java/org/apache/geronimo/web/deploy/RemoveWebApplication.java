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


package org.apache.geronimo.web.deploy;

import javax.management.MBeanServer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.task.DeploymentTask;
import org.apache.geronimo.web.WebApplication;
import org.apache.geronimo.web.WebContainer;

/* -------------------------------------------------------------------------------------- */
/**
 * RemoveWebApplication
 * 
 * 
 * @version $Revision: 1.1 $ $Date: 2003/10/05 01:39:48 $
 */
public class RemoveWebApplication implements DeploymentTask
{
    private Log log = LogFactory.getLog (RemoveWebApplication.class);
    private MBeanServer server;
    private WebContainer container;
    private WebApplication webapp;

    public RemoveWebApplication ( MBeanServer server, WebContainer container, WebApplication webapp)
    {
        this.server = server;
        this.container = container;
        this.webapp = webapp;
    }
    
    
    /* -------------------------------------------------------------------------------------- */
    /* Check if it is ok to remove the webapp from the container
     * @return
     * @throws DeploymentException
     * @see org.apache.geronimo.kernel.deployment.task.DeploymentTask#canRun()
     */
    public boolean canRun() throws DeploymentException
    {
        
        if (container == null)
        {
            log.debug ("Container==null");
            return false;
        }
            
            
        if (webapp == null)
        {
            log.debug ("webapp==null");
            return false;
        }
            
        
        log.debug ("RemoveWebApplication task can run");    
        return true;    
    }

    /* -------------------------------------------------------------------------------------- */
    /* Perform the removal of the webapp from the container
     * @throws DeploymentException
     * @see org.apache.geronimo.kernel.deployment.task.DeploymentTask#perform()
     */
    public void perform() throws DeploymentException
    {
        try
        {
            log.debug ("Performing removal of webapp");
            container.removeComponent(webapp);               
        }
        catch (Exception e)
        {
            log.debug ("Error removing webapp from container", e);
            throw new DeploymentException (e);
        }
    }


    /* -------------------------------------------------------------------------------------- */
    /* Undo the removal of the webapp.
     * This could cause problems potentially??
     * @see org.apache.geronimo.kernel.deployment.task.DeploymentTask#undo()
     */
    public void undo()
    {
        //this could be dodgy????
        try
        {
            log.info ("Undo called on remove web app");
            container.addComponent(webapp);
        }
        catch (Exception e)
        {
            log.error("Problem undoing removal of webapp from container", e);
        }
    }

}
