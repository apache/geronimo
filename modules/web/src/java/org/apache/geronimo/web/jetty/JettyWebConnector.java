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

import java.lang.reflect.Constructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.core.service.AbstractComponent;
import org.apache.geronimo.core.service.Container;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.web.AbstractWebConnector;
import org.apache.geronimo.web.WebConnector;
import org.mortbay.http.HttpListener;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SunJsseListener;
import org.mortbay.http.ajp.AJP13Listener;
import org.mortbay.util.ThreadedServer;
import org.mortbay.jetty.Server;

/**
 * @jmx:mbean extends="org.apache.geronimo.web.AbstractWebConnectorMBean"
 * @version $Revision: 1.4 $ $Date: 2003/09/14 12:09:44 $
 */
public class JettyWebConnector extends AbstractWebConnector implements JettyWebConnectorMBean
{
    private final static Log log = LogFactory.getLog(JettyWebConnector.class);
    private final static Class[] _defaultConstructorSignature = new Class[]{};
    private final static Object[] _defaultConstructorArgs = new Object[]{};
    private HttpListener _listener = null;
    private Server _jetty = null;



    /**
     * Set the parent Container for this  component
     *
     * @param container a <code>Container</code> value
     */
    public void setContainer (Container container)
    {
        super.setContainer(container);

        //get the Jetty instance
        if (! (container instanceof JettyWebContainer))
            throw new IllegalStateException ("Non Jetty container set as parent on connector");

        _jetty = ((JettyWebContainer)container).getJettyServer();
    }


    /**
     * Set up the port for the Connector to listen on.
     * It is not permitted to change the port if the
     * connector is in operation.
     *
     * @param port an <code>int</code> value
     */
    public void setPort (int port)
    {
        if (getStateInstance() != State.STOPPED) 
            throw new IllegalStateException ("Cannot change the connector port after the connector is started");

        super.setPort(port);
    }


    /**
     *  Set up the protocol for the connector.
     * It is not permitted to change the protocol if
     * the connector is operating.
     *
     * @param protocol a <code>String</code> value
     */
    public void setProtocol (String protocol)
    {
         if (getStateInstance() != State.STOPPED) 
            throw new IllegalStateException ("Cannot change protocol after connector has started");

        super.setProtocol(protocol);
    }


    /**
     * Set up the host/interface to listen on.
     * It is not permitted to change this once the
     * connector has started.
     *
     * @param iface a <code>String</code> value
     */
    public void setInterface (String iface)
    {     
        if (getStateInstance() != State.STOPPED) 
            throw new IllegalStateException ("Cannot change interface after connector has started");
        super.setInterface(iface);
    }

    /**
     * Start the connector
     *
     * @exception Exception if an error occurs
     */
    public void doStart() throws Exception
    {
        try
        {
        if (getContainer() == null)
            throw new IllegalStateException ("Container not set on connector");

        //configure the listener 
        if ((getProtocol() == null) || (getProtocol().equalsIgnoreCase(HTTP_PROTOCOL)))
        {
            _listener = new SocketListener();
        }      
        else if (getProtocol().equalsIgnoreCase (AJP13_PROTOCOL))
        {
            _listener = new AJP13Listener();
        }
        else if (getProtocol().equalsIgnoreCase (HTTPS_PROTOCOL))
        {
            _listener = new SunJsseListener();
        }
        else
        {
            //maybe the protocol is a classname of a listener 
            //implementing a particular protocol
            Class listenerClass = Thread.currentThread().getContextClassLoader().loadClass(getProtocol());
            //get the default constructor, if it has one
            Constructor constructor = listenerClass.getConstructor (_defaultConstructorSignature);
            _listener = (HttpListener)constructor.newInstance (_defaultConstructorArgs);
        }

        log.debug ("About to start WebConnector: "+getObjectName());
        //NB: need to check if port is set - what to use instead of 0?
        _listener.setPort(getPort());

        if (getInterface() != null)
            _listener.setHost(getInterface());

        //take the listener default unless connections have been set
        //what to use instead of 0?
        if (getMaxConnections() > 0)
            ((ThreadedServer)_listener).setMaxThreads(getMaxConnections());

        //take the listener default unless idle time has been set
        //what to use instead of 0?
        if (getMaxIdleTime() > 0)
            ((ThreadedServer)_listener).setMaxIdleTimeMs (getMaxIdleTime());

        //open the underlying Jetty socket, if these calls fail,
        //the connector will not transit to STARTED
        ((ThreadedServer)_listener).open();
        ((ThreadedServer)_listener).start();
        log.debug ("Listener on port="+getPort()+" started.");

        //at this point, when the method returns, the WebConnector will be STARTED
        //even though the underlying socket may not be handling
        //connections because it's criteria to listen may not
        //be met etc
        
        //if the start succeeded, set up the listener on the
        //Jetty instance
        _jetty.addListener(_listener);
        log.debug ("Listener on port="+getPort()+" added to Jetty");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw e;
        }
    }

  
    /**
     * Stop the connector
     *
     * @exception Exception if an error occurs
     */
    public void doStop () throws Exception
    {
        _listener.stop();
    }

    /**
     * Get the underlying Jetty listener
     *
     * @return the Jetty listener
     */
    HttpListener getListener ()
    {
        return _listener;
    }
}
