package org.apache.geronimo.web;

import java.util.Arrays;
import java.util.List;

import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GConstructorInfo;


/**
 * AbstractWebConnector.java
 *
 *
 * Created: Mon Sep  8 20:39:02 2003
 *
 * @version $Revision: 1.9 $ $Date: 2004/01/17 17:02:38 $
 */
public abstract class AbstractWebConnector implements WebConnector {

    public static final String HTTP_PROTOCOL = "http";
    public static final String HTTPS_PROTOCOL = "https";
    public static final String AJP13_PROTOCOL = "ajp13";

    private static final GBeanInfo GBEAN_INFO;

    private int port = 0;
    private String protocol = null;
    private String iface = null;
    private int maxConnections = 0;
    private int maxIdleTime = 0;
    private List contexts = null;

    /**
     *  @deprecated, remove when GBean -only
     */
    public AbstractWebConnector() {

    }

    public AbstractWebConnector(String protocol, String iface, int port, int maxConnections, int maxIdleTime, String[] contexts) {
        this.protocol = protocol;
        this.iface = iface;
        this.port = port;
        this.maxConnections = maxConnections;
        this.maxIdleTime = maxIdleTime;
        if (contexts != null) {
            this.contexts = Arrays.asList(contexts);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setPort(int)
     */
    public void setPort(int port)
    {
        this.port = port;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getPort()
     */
    public int getPort()
    {
        return port;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setProtocol(java.lang.String)
     */
    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getProtocol()
     */
    public String getProtocol()
    {
        return protocol;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setInterface(java.lang.String)
     */
    public void setInterface(String iface)
    {
        this.iface = iface;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getInterface()
     */
    public String getInterface()
    {
        return iface;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setMaxConnections(int)
     */
    public void setMaxConnections(int maxConnects)
    {
        maxConnections = maxConnects;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getMaxConnections()
     */
    public int getMaxConnections()
    {
        return maxConnections;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setMaxIdleTime(int)
     */
    public void setMaxIdleTime(int maxIdleTime)
    {
        this.maxIdleTime = maxIdleTime;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getMaxIdleTime()
     */
    public int getMaxIdleTime()
    {
        return maxIdleTime;
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#setContexts(java.lang.String[])
     */
    public void setContexts(String[] contexts)
    {
        this.contexts = Arrays.asList(contexts);
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.web.WebConnector#getContexts()
     */
    public String[] getContexts()
    {
        return (String[])contexts.toArray(new String[0]);
    }

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(AbstractWebConnector.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("Port", true));
        infoFactory.addAttribute(new GAttributeInfo("Protocol", true));
        infoFactory.addAttribute(new GAttributeInfo("Interface", true));
        infoFactory.addAttribute(new GAttributeInfo("MaxConnections", true));
        infoFactory.addAttribute(new GAttributeInfo("MaxIdleTime", true));
        infoFactory.addAttribute(new GAttributeInfo("Contexts", true));
        infoFactory.setConstructor(new GConstructorInfo(
                Arrays.asList(new Object[] {"Protocol", "Interface", "Port", "MaxConnections", "MaxIdleTime", "Contexts"}),
                Arrays.asList(new Object[] {String.class, String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE, String[].class})
                ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    /**
     *  @deprecated, remove when GBean -only
     */
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