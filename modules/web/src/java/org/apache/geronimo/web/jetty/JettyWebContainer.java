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

package org.apache.geronimo.web.jetty;

import java.net.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.kernel.management.StateManageable;
import org.apache.geronimo.web.AbstractWebContainer;
import org.apache.geronimo.web.WebApplication;
import org.apache.geronimo.web.WebConnector;
import org.mortbay.jetty.Server;
import org.mortbay.http.SocketListener;

/**
 * Base class for jetty web containers.
 * @jmx:mbean extends="org.apache.geronimo.web.AbstractWebContainerMBean"
 *
 *
 * @version $Revision: 1.6 $ $Date: 2003/09/28 22:30:58 $
 */
public class JettyWebContainer extends AbstractWebContainer implements JettyWebContainerMBean {
  
    private final Log _log = LogFactory.getLog(JettyWebContainer.class);

    private Server _jettyServer = null;


    public JettyWebContainer ()
    {
        _jettyServer = new Server();
    }


    /**
     * Start the Jetty server
     *
     * @exception Exception if an error occurs
     */
    public void doStart() throws Exception
    {
        try
        {
            _log.debug ("Jetty Server starting");
            
            _jettyServer.start();

            _log.debug ("Jetty Server started");
        }
        catch (Exception e)
        {
            _log.error ("Exception in doStart()", e);
        }
    }

 
    public WebApplication createWebApplication (URI uri)
    { 
        return new JettyWebApplication(uri);
    }

 

    /**
     * Get the Jetty server delegate
     *
     * @return the Jetty server
     */
    Server getJettyServer ()
    {
        return _jettyServer;
    }


    
    /**
     * Handle addition of a web connector.
     *
     * @param connector a <code>WebConnector</code> value
     */
    protected void webConnectorAdded(WebConnector connector)
    {
        _log.debug ("Web connector="+connector.getObjectName()+" added");
        super.webConnectorAdded(connector);
    }




    /**
     * Handle removal of a web connector
     *
     * @param connector a <code>WebConnector</code> value
     */
    protected void webConnectorRemoved (WebConnector connector)
    {
        try
        {
            //stop the connector
            if (connector instanceof StateManageable)
                ((StateManageable)connector).stop();
        }
        catch (Exception e)
        {
            _log.warn("Ignoring exception on stopping connector", e);
        }

        //remove the listener
        _jettyServer.removeListener (((JettyWebConnector)connector).getListener());
    }


    protected void webApplicationAdded (WebApplication webapp)
    {
        _log.debug ("Web application="+webapp.getObjectName()+" added to Jetty");
        _jettyServer.addContext (((JettyWebApplication)webapp).getJettyContext());
        ((JettyWebApplication)webapp).getJettyContext().setExtractWAR(true);
        super.webApplicationAdded (webapp);
    }
}
