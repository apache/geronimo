package org.apache.geronimo.web;

import java.util.Arrays;
import java.util.List;

import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;


/**
 * AbstractWebConnector.java
 *
 *
 * Created: Mon Sep  8 20:39:02 2003
 *
 * @version $Revision: 1.5 $ $Date: 2003/12/30 21:18:35 $
 */
public abstract class AbstractWebConnector implements WebConnector {

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String AJP13_PROTOCOL = "ajp13";

    private int _port = 0;
    private String _protocol = null;
    private String _interface = null;
    private int _maxConnections = 0;
    private int _maxIdleTime = 0;
    private List _contexts = null;



    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setPort(int)
     */
    public void setPort(int port)
    {
        _port = port;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getPort()
     */
    public int getPort()
    {
        return _port;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setProtocol(java.lang.String)
     */
    public void setProtocol(String protocol)
    {
        _protocol = protocol;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getProtocol()
     */
    public String getProtocol()
    {
        return _protocol;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setInterface(java.lang.String)
     */
    public void setInterface(String iface)
    {
        _interface = iface;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getInterface()
     */
    public String getInterface()
    {
        return _interface;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setMaxConnections(int)
     */
    public void setMaxConnections(int maxConnects)
    {
        _maxConnections = maxConnects;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getMaxConnections()
     */
    public int getMaxConnections()
    {
        return _maxConnections;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setMaxIdleTime(int)
     */
    public void setMaxIdleTime(int maxIdleTime)
    {
        _maxIdleTime = maxIdleTime;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getMaxIdleTime()
     */
    public int getMaxIdleTime()
    {
        return _maxIdleTime;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setContexts(java.lang.String[])
     */
    public void setContexts(String[] contexts)
    {
        _contexts = Arrays.asList(contexts);
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getContexts()
     */
    public String[] getContexts()
    {
        return (String[])_contexts.toArray(new String[0]);
    }


    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Port", true, true, "port to listen on"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Protocol", true, true, "Protocol (hhtp, https, ftp etc) to use"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Interface", true, true, "Interface to listen on"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("MaxConnections", true, true, "Maximum number of connections"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("MaxIdleTime", true, true, "Maximum idle time (ms??) a connection can be idle before being closed"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Contexts", true, true, "Contexts that must be registered in the web container before this connector will start accepting connections"));
        return mbeanInfo;
    }

}