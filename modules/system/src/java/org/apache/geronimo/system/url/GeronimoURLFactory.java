/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.system.url;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandler;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.WaitingException;

/**
 * This service replaces the URLStreamHandlerFactory used in URL, which gives Geronimo
 * complete control over the URLs creted in the server.  This allows us to replace the
 * broken implementation of the "file" protocol.
 *
 * @version $Revision: 1.4 $ $Date: 2004/06/05 07:14:30 $
 */
public class GeronimoURLFactory implements GBean {
    private static final URLStreamHandlerFactory factory = new URLStreamHandlerFactory();
    private static boolean installed = false;

    public void doStart() throws WaitingException, Exception {
        // verify that our factory is installed... if our factory can not be
        // installed the gbean will fail
        install();
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }

    /**
     * Assigns the handler to the specified protocol.
     *
     * @param protocol the prototcol name
     * @param handler the url handler for the protocol
     * @throws IllegalStateException if a handler is alreayd assigned to the protocol
     */
    public void registerHandler(String protocol, URLStreamHandler handler) {
        factory.registerHandler(protocol, handler);
    }

    /**
     * Gets the handler registered for the specified protocol
     *
     * @param protocol the protocol name
     * @return the registered handler or null if no handler is registerd for the protocol
     */
    public URLStreamHandler getRegisteredHandler(String protocol) {
        return factory.getRegisteredHandler(protocol);
    }

    /**
     * Gets a map of all registered handlers keyed by protocol name.
     *
     * @return a map from protocol name to handler
     */
    public Map getRegisteredHandlers() {
        return factory.getRegisteredHandlers();
    }

    /**
     * Is our factory installed?
     *
     * @return true if our factory is installed; false otherwise
     */
    public static boolean isInstalled() {
        return installed;
    }

    /**
     * Installs the factory into URL using the setURLStreamHandlerFactory method.
     * This will fail is some other code already installed a factory.
     * <p/>
     * This should be called from your main method to assure the factory is installed
     * before another code has a chance to take the slot.
     *
     * @throws Error if the application has already set a factory
     * @throws SecurityException if a security manager exists and its checkSetFactory method doesn't allow the operation
     */
    public static void install() throws Error, SecurityException {
        if (!installed) {
            URL.setURLStreamHandlerFactory(factory);
            installed = true;
        }
    }

    /**
     * Installs the factory into directly URL's private and package protected fields using
     * Field.setAccessable(true).  This is the "naughty" way of installing a factory but works
     * on most platforms.  If the platform is not using Sun's implementation of URL, this code is
     * likely to not work, but all known platforms use Sun's URL code.
     * <p/>
     * You should not use this method unless you absolutely have to.
     *
     * @throws Error if the application has already set a factory
     * @throws SecurityException if a security manager exists and its checkSetFactory method doesn't allow the operation
     */
    public static void forceInstall() throws Error, SecurityException {
        if (!installed) {
            // This way is "naughty" but works great
            Throwable t = (Throwable) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        // get a reference to the URL stream handler lock... we need to
                        // synchronize on this field to be safe
                        Field streamHandlerLockField = URL.class.getDeclaredField("streamHandlerLock");
                        streamHandlerLockField.setAccessible(true);
                        Object streamHandlerLock = streamHandlerLockField.get(null);

                        synchronized (streamHandlerLock) {
                            // get a reference to the factory field and change the permissions
                            // to make it accessable (factory is a package protected field)
                            Field factoryField = URL.class.getDeclaredField("factory");
                            factoryField.setAccessible(true);

                            // get a reference to the handlers field and change the permissions
                            // to make it accessable (handlers is a package protected field)
                            Field handlersField = URL.class.getDeclaredField("handlers");
                            handlersField.setAccessible(true);

                            // the the handlers map first
                            Map handlers = (Map) handlersField.get(null);

                            // set the factory field to our factory
                            factoryField.set(null, factory);

                            // clear the handlers
                            handlers.clear();
                        }
                    } catch (Throwable e) {
                        return e;
                    }
                    return null;
                }
            });

            if (t != null) {
                if (t instanceof SecurityException) {
                    throw (SecurityException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                }
                throw new Error("Unknown error while force installing URL factory", t);
            }
            installed = true;
        }
    }

    private static class URLStreamHandlerFactory implements java.net.URLStreamHandlerFactory {
        private final Map handlers = new HashMap();
        private final List handlerPackages = new LinkedList();

        private URLStreamHandlerFactory() {
            // add the packages listed in the standard system property
            String systemPackages = System.getProperty("java.protocol.handler.pkgs");
            if (systemPackages != null) {
                StringTokenizer stok = new StringTokenizer(systemPackages, "|");
                while (stok.hasMoreTokens()) {
                    handlerPackages.add(stok.nextToken().trim());
                }
            }
            // always add the sun handlers
            handlerPackages.add("sun.net.www.protocol");

            // register our well known protocol handlers
            // URL permanently caches our handlers so we may want to register a wrapper
            // around the true handler to enable changing the implementation at runtime
            handlers.put("file", new org.apache.geronimo.system.url.file.Handler());
            handlers.put("resource", new org.apache.geronimo.system.url.resource.Handler());
        }

        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (protocol == null) {
                throw new IllegalArgumentException("protocol is null");
            }
            protocol = protocol.trim();

            URLStreamHandler handler;

            // first check the registered handlers
            synchronized (this) {
                handler = (URLStreamHandler) handlers.get(protocol);
            }
            if (handler != null) {
                return handler;
            }

            // try to get the stream handler from the registered package list
            Class type = findProtocolHandler(protocol);
            if (type == null) {
                throw new IllegalArgumentException("unknown protocol: " + protocol);
            }

            try {
                return (URLStreamHandler) type.newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Error constructing protocol handler:" +
                        "protocol " + protocol +
                        " messgae=" + e.getMessage());
            }
        }

        private synchronized void registerHandler(String protocol, URLStreamHandler handler) {
            assert protocol != null: "protocol is null";
            assert handler != null: "handler is null";
            if (handlers.containsKey(protocol)) {
                throw new IllegalStateException("Protocol already has a registered handler: " + protocol);
            }
            handlers.put(protocol, handler);
        }

        private synchronized URLStreamHandler getRegisteredHandler(String protocol) {
            return (URLStreamHandler) handlers.get(protocol);
        }

        private synchronized Map getRegisteredHandlers() {
            return new HashMap(handlers);
        }

        /**
         * Searches each registered package for specified protocol handler. The class name
         * is packageName + "." + protocolName + ".Handler"
         *
         * @param protocol the desired protocol handler
         * @return the protocol handler or null if none is found
         */
        private Class findProtocolHandler(final String protocol) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            for (Iterator iterator = handlerPackages.iterator(); iterator.hasNext();) {
                String pkg = (String) iterator.next();
                String classname = pkg + "." + protocol + ".Handler";

                try {
                    return classLoader.loadClass(classname);
                } catch (Throwable e) {
                    // ignore
                }
            }
            return null;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(GeronimoURLFactory.class);
        infoFactory.addOperation("registerHandler", new Class[]{String.class, URLStreamHandler.class});
        infoFactory.addOperation("getRegisteredHandler", new Class[]{String.class});
        infoFactory.addOperation("getRegisteredHandlers", new Class[]{});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
