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

package org.apache.geronimo.webdav.jetty;

import java.lang.reflect.Constructor;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.webdav.AbstractConnector;
import org.mortbay.http.HttpListener;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SunJsseListener;
import org.mortbay.http.ajp.AJP13Listener;
import org.mortbay.util.ThreadedServer;

/**
 * Connector using under the cover a Jetty HttpListener.
 * 
 * @version $Revision: 1.1 $ $Date: 2004/01/20 14:58:08 $
 */
public class JettyConnectorImpl
    extends AbstractConnector
    implements GBean, JettyConnector
{

    private static final String HTTP_PROTOCOL = "http";
    private static final String HTTPS_PROTOCOL = "https";
    private static final String AJP13_PROTOCOL = "ajp13";

    private final static GBeanInfo GBEAN_INFO;

    private final static Class[] EMPTY_FORMAL_PARAM = new Class[]{};
    private final static Object[] EMPTY_ARGS = new Object[]{};

    /**
     * When the underlying listener is undefined, the GBean operations are
     * delegated to this state.
     */
    private final GBean undefinedListenerState;

    /**
     * When the underlying listener is defined, the GBean operations are
     * delegated to this state.
     */
    private final GBean definedListenerState;

    private HttpListener listener;
    private GBean lifeCycleState;

    public JettyConnectorImpl(String aProtocol, String anHost, int aPort,
        int aMaxCon, int aMaxIdle) {
        super(aProtocol, anHost, aPort, aMaxCon, aMaxIdle);
        undefinedListenerState = new UndefinedListenerState();
        definedListenerState = new DefinedListenerState();
        setListener(null);
    }
        
    public HttpListener getListener() {
        return listener;
    }

    private void setListener(HttpListener aListener) {
        listener = aListener;
        if ( null == listener ) {
            lifeCycleState = undefinedListenerState;
        } else {
            lifeCycleState = definedListenerState;
        }
    }

    public void doStart() throws WaitingException, Exception {
        log.info("Starting Jetty Connector");
        lifeCycleState.doStart();
    }

    public void doStop() throws WaitingException {
        log.info("Stopping Jetty Connector");
        lifeCycleState.doStop();
    }

    public void doFail() {
        log.info("Failing Jetty Connector");
        lifeCycleState.doFail();
    }

    static {
        GBeanInfoFactory infoFactory =
            new GBeanInfoFactory("Connector - Jetty",
            JettyConnectorImpl.class.getName(),
            AbstractConnector.getGBeanInfo());
        infoFactory.addAttribute(new GAttributeInfo("Listener"));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private class DefinedListenerState implements GBean {
        public void doStart() throws WaitingException, Exception {
            if ( listener.isStarted() ) {
                return;            
            }
            listener.start();
        }

        public void doStop() throws WaitingException {
            try {
                listener.stop();
            } catch (Exception e) {
                log.error("Problem stopping Jetty Connector", e);
                setListener(null);
            }
        }

        public void doFail() {
            try {
                if (listener.isStarted()) {
                    listener.stop();
                }
            } catch (Exception e) {
                log.error("Can not fail Jetty Connector", e);
            }
            setListener(null);
        }
        
    }

    private class UndefinedListenerState implements GBean {
        public void doStart() throws WaitingException, Exception {
            HttpListener tmpListener;
            try {
                if (null == protocol || protocol.equalsIgnoreCase(HTTP_PROTOCOL)) {
                    tmpListener = new SocketListener();
                } else if (protocol.equalsIgnoreCase(AJP13_PROTOCOL)) {
                    tmpListener = new AJP13Listener();
                } else if (protocol.equalsIgnoreCase(HTTPS_PROTOCOL)) {
                    tmpListener = new SunJsseListener();
                } else {
                    Class listenerClass =
                        Thread.currentThread().getContextClassLoader().loadClass(
                            protocol);
                    Constructor constructor =
                        listenerClass.getConstructor(EMPTY_FORMAL_PARAM);
                    tmpListener =
                        (HttpListener) constructor.newInstance(EMPTY_ARGS);
                }
                tmpListener.setPort(getPort());

                if ( getInterface() != null ) {
                    tmpListener.setHost(getInterface());
                }
                if ( getMaxConnections() > 0 ) {
                    ((ThreadedServer) tmpListener).setMaxThreads(getMaxConnections());
                }
                if ( getMaxIdleTime() > 0 ) {
                    ((ThreadedServer) tmpListener).setMaxIdleTimeMs(getMaxIdleTime());
                }
                ((ThreadedServer) tmpListener).open();
                tmpListener.start();
            } catch (Exception e) {
                log.error("Problem starting Connector", e);
                throw e;
            }
            setListener(tmpListener);
        }

        public void doStop() throws WaitingException {
        }

        public void doFail() {
        }
        
    }
}
