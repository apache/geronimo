/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.webdav.jetty;

import java.lang.reflect.Constructor;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
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
 * @version $Revision: 1.4 $ $Date: 2004/03/10 10:00:41 $
 */
public class JettyConnectorImpl extends AbstractConnector implements GBean, JettyConnector {
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
        if (null == listener) {
            lifeCycleState = undefinedListenerState;
        } else {
            lifeCycleState = definedListenerState;
        }
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        log.info("Starting Jetty Connector");
        lifeCycleState.doStart();
    }

    public void doStop() throws WaitingException, Exception {
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
        infoFactory.addAttribute(new GAttributeInfo("Listener", false));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    private class DefinedListenerState implements GBean {
        public void setGBeanContext(GBeanContext context) {
        }

        public void doStart() throws WaitingException, Exception {
            if (listener.isStarted()) {
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
        public void setGBeanContext(GBeanContext context) {
        }

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

                if (getInterface() != null) {
                    tmpListener.setHost(getInterface());
                }
                if (getMaxConnections() > 0) {
                    ((ThreadedServer) tmpListener).setMaxThreads(getMaxConnections());
                }
                if (getMaxIdleTime() > 0) {
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
