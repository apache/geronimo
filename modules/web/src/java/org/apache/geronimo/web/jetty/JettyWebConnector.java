/*
 * Created on 22-Aug-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.apache.geronimo.web.jetty;

import org.apache.geronimo.common.AbstractComponent;
import org.apache.geronimo.web.WebConnector;
import org.mortbay.http.HttpListener;

/**
 * @author gregw
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class JettyWebConnector extends AbstractComponent implements WebConnector
{
    private HttpListener listener;

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setPort(int)
     */
    public void setPort(int port)
    {
        listener.setPort(port);
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getPort()
     */
    public int getPort()
    {
        return listener.getPort();
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setProtocol(java.lang.String)
     */
    public void setProtocol(String protocol)
    {
        // TODO. We have a choice here - if the protocol changes then create a new listener 
        // and copy over all the parameters from the old listener  OR this class can store all the 
        // parameters and only create a listener when it is started.
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getProtocol()
     */
    public String getProtocol()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setInterface(java.lang.String)
     */
    public void setInterface(String iface)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getInterface()
     */
    public String getInterface()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setMaxConnections(int)
     */
    public void setMaxConnections(int maxConnects)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getMaxConnections()
     */
    public int getMaxConnections()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setMaxIdleTime(int)
     */
    public void setMaxIdleTime(int maxIdleTime)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getMaxIdleTime()
     */
    public int getMaxIdleTime()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setContexts(java.lang.String[])
     */
    public void setContexts(String[] contexts)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getContexts()
     */
    public String[] getContexts()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
