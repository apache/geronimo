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

package org.apache.geronimo.webdav;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;

/**
 * Base implementation for the Connector contracts.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/20 14:58:08 $
 */
public abstract class AbstractConnector
    implements Connector, GBean
{

    private final static GBeanInfo GBEAN_INFO;

    protected final Log log = LogFactory.getLog(getClass());

    /**
     * Port.
     */    
    protected int port;
    
    /**
     * Protocol.
     */
    protected String protocol;
    
    /**
     * Host.
     */
    private String host;
    
    /**
     * Maximum number of connections.
     */
    protected int maxCon;
    
    /**
     * Maximum idle time.
     */
    protected int maxIdle;

    /**
     * Creates a connector having the specified specificities.
     * 
     * @param aProtocol Protocol.
     * @param anHost Host.
     * @param aPort Port.
     * @param aMaxCon Maximum number of connections.
     * @param aMaxIdle Maximum idle time.
     */
    public AbstractConnector(String aProtocol, String anHost, int aPort,
        int aMaxCon, int aMaxIdle) {
        protocol = aProtocol;
        host = anHost;
        port = aPort;
        maxCon = aMaxCon;
        maxIdle = aMaxIdle;
    }

    public void setPort(int aPort) {
        port = aPort;
    }

    public int getPort() {
        return port;
    }

    public void setProtocol(String aProtocol) {
        protocol = aProtocol;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setInterface(String anInterface) {
        host = anInterface;
    }

    /**
     * Gets the interface/host of this Connector.
     * <BR>
     * If it has not been set explicitely, then the host name of the localhost
     * is set and returned.
     * 
     * @return Interface. 
     */
    public synchronized String getInterface() {
        if ( null != host ) {
            return host;
        }
        try {
            host = InetAddress.getLocalHost().getHostName();
            return host;
        } catch (UnknownHostException e) {
            // Should not happen.
            log.error(e);
            throw new RuntimeException(e);
        }
    }

    public void setMaxConnections(int aMaxConnects) {
        maxCon = aMaxConnects;        
    }

    public int getMaxConnections() {
        return maxCon;
    }

    public void setMaxIdleTime(int aMaxIdleTime) {
        maxIdle = aMaxIdleTime;
    }

    public int getMaxIdleTime() {
        return maxIdle;
    }

    static {
        GBeanInfoFactory infoFactory =
            new GBeanInfoFactory("Abstract Connector",
            AbstractConnector.class.getName());
        infoFactory.addAttribute(new GAttributeInfo("Port", true));
        infoFactory.addAttribute(new GAttributeInfo("Protocol", true));
        infoFactory.addAttribute(new GAttributeInfo("Interface", true));
        infoFactory.addAttribute(new GAttributeInfo("MaxConnections", true));
        infoFactory.addAttribute(new GAttributeInfo("MaxIdleTime", true));
        infoFactory.setConstructor(new GConstructorInfo(
            Arrays.asList(new Object[] {"Protocol", "Interface",
                "Port", "MaxConnections", "MaxIdleTime"}),
            Arrays.asList(new Object[] {String.class, String.class,
                Integer.TYPE, Integer.TYPE, Integer.TYPE})
                ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
