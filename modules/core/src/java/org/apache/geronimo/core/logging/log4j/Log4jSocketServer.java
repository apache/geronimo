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

package org.apache.geronimo.core.logging.log4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanContext;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanTarget;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoParameterInfo;
import org.apache.log4j.LogManager;
import org.apache.log4j.net.SocketNode;
import org.apache.log4j.spi.LoggerRepository;

/**
 * A Log4j SocketServer service.  Listens for client connections on the
 * specified port and creates a new thread and SocketNode to process the
 * incoming client log messages.
 *
 * <p>
 * The LoggerRepository can be changed based on the clients address
 * by using a custom LoggerRepositoryFactory.  The default factory
 * will simply return the current repository.
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/12/30 21:17:14 $
 */
public class Log4jSocketServer implements GeronimoMBeanTarget {
    /** The port number where the server listens. */
    protected int port = -1;

    /** The listen backlog count. */
    protected int backlog = 50;

    /** The address to bind to. */
    protected InetAddress bindAddress;

    /** True if the socket listener is enabled. */
    protected boolean listenerEnabled = true;

    /** The socket listener thread. */
    protected SocketListenerThread listenerThread;

    /** The server socket which the listener listens on. */
    protected ServerSocket serverSocket;

    /** The factory to create LoggerRepository's for client connections. */
    protected LoggerRepositoryFactory loggerRepositoryFactory;


    public void setPort(final int port)
    {
        this.port = port;
    }

    public int getPort()
    {
        return port;
    }

    public void setBacklog(final int backlog)
    {
        this.backlog = backlog;
    }

    public int getBacklog()
    {
        return backlog;
    }

    public void setBindAddress(final InetAddress addr)
    {
        this.bindAddress = addr;
    }

    public InetAddress getBindAddress()
    {
        return bindAddress;
    }

    public void setListenerEnabled(final boolean enabled)
    {
        listenerEnabled = enabled;
    }

    public boolean getListenerEnabled()
    {
        return listenerEnabled;
    }

    public void setLoggerRepositoryFactoryType(final Class type)
        throws InstantiationException, IllegalAccessException, ClassCastException
    {
        this.loggerRepositoryFactory = (LoggerRepositoryFactory)type.newInstance();
    }

    public Class getLoggerRepositoryFactoryType()
    {
        if (loggerRepositoryFactory == null) {
            return null;
        }

        return loggerRepositoryFactory.getClass();
    }

    public LoggerRepository getLoggerRepository(final InetAddress addr)
    {
        return loggerRepositoryFactory.create(addr);
    }


    ///////////////////////////////////////////////////////////////////////////
    //                             Socket Listener                           //
    ///////////////////////////////////////////////////////////////////////////

    protected class SocketListenerThread
        extends Thread
    {
        protected Log log = LogFactory.getLog(SocketListenerThread.class);
        protected boolean enabled;
        protected boolean shuttingDown;
        protected Object lock = new Object();

        public SocketListenerThread(final boolean enabled)
        {
            super("SocketListenerThread");

            this.enabled = enabled;
        }

        public void setEnabled(boolean enabled)
        {
            this.enabled = enabled;

            synchronized (lock) {
                lock.notifyAll();
            }

            if (log.isDebugEnabled()) {
                log.debug("Notified that enabled: " + enabled);
            }
        }

        public void shutdown()
        {
            enabled = false;
            shuttingDown = true;

            synchronized (lock) {
                lock.notifyAll();
            }

            if (log.isDebugEnabled()) {
                log.debug("Notified to shutdown");
            }
        }

        public void run()
        {
            while (!shuttingDown) {

                if (!enabled) {
                    try {
                        log.debug("Disabled, waiting for notification");
                        synchronized (lock) {
                            lock.wait();
                        }
                    }
                    catch (InterruptedException ignore) {}
                }

                try {
                    doRun();
                }
                catch (Exception e) {
                    log.error("Exception caught from main loop; ignoring", e);
                }
            }
        }

        protected void doRun() throws Exception
        {
            while (enabled) {
                boolean debug = log.isDebugEnabled();

                Socket socket = serverSocket.accept();
                InetAddress addr =  socket.getInetAddress();
                if (debug) {
                    log.debug("Connected to client: " + addr);
                }

                LoggerRepository repo = getLoggerRepository(addr);
                if (debug) {
                    log.debug("Using repository: " + repo);
                }

                //
                // jason: may want to expose socket node as an MBean for management
                //

                log.debug("Starting new socket node");
                SocketNode node = new SocketNode(socket, repo);
                Thread thread = new Thread(node);
                thread.start();
                log.debug("Socket node started");
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    //                         LoggerRepositoryFactory                       //
    ///////////////////////////////////////////////////////////////////////////

    public static interface LoggerRepositoryFactory
    {
        public LoggerRepository create(InetAddress addr);
    }

    /**
     * A simple LoggerRepository factory which simply returns
     * the current repository from the LogManager.
     */
    public static class DefaultLoggerRepositoryFactory
        implements LoggerRepositoryFactory
    {
        private LoggerRepository repo;

        public LoggerRepository create(final InetAddress addr)
        {
            if (repo == null) {
                repo = LogManager.getLoggerRepository();
            }
            return repo;
        }
    }


    ///////////////////////////////////////////////////////////////////////////
    //                    AbstractManagedObject Overrides                    //
    ///////////////////////////////////////////////////////////////////////////

    public void setMBeanContext(GeronimoMBeanContext context) {
    }

    public boolean canStart() {
        return true;
    }

    public void doStart()
    {
        listenerThread = new SocketListenerThread(false);
        listenerThread.setDaemon(true);
        listenerThread.start();
        //log.debug("Socket listener thread started");

        if (loggerRepositoryFactory == null) {
            //log.debug("Using default logger repository factory");
            loggerRepositoryFactory = new DefaultLoggerRepositoryFactory();
        }

        // create a new server socket to handle port number changes
        try {
            if (bindAddress == null) {
                serverSocket = new ServerSocket(port, backlog);
            }
            else {
                serverSocket = new ServerSocket(port, backlog, bindAddress);
            }
        } catch (IOException e) {
            throw new RuntimeException("Problem starting socket server", e);
        }

        //log.info("Listening on " + serverSocket);
        listenerThread.setEnabled(listenerEnabled);
    }

    public boolean canStop() {
        return true;
    }

    public void doStop()
    {
        listenerThread.shutdown();
        listenerThread = null;
        serverSocket = null;
    }

    public void doFail() {
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(Log4jSocketServer.class.getName());
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Port", true, true, "Port to listen on"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Backlog", true, true, "Backlog"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("BindAddress", true, true, "BindAddress to listen on"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("ListenerEnabled", true, true, "whether Listener is enabled"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LoggerRepositoryFactoryType", true, true, "Class for constructing LoggerRepository instances"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getLoggerRepository", new GeronimoParameterInfo[] {
            new GeronimoParameterInfo("address", InetAddress.class, "address to get the repository for")},
                GeronimoOperationInfo.INFO, "Get the logger repository for the supplied address"));
        return mbeanInfo;
    }
}
