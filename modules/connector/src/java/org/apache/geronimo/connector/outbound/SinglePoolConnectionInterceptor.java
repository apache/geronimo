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

package org.apache.geronimo.connector.outbound;

import java.util.Collections;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import EDU.oswego.cs.dl.util.concurrent.FIFOSemaphore;

/**
 * SinglePoolConnectionInterceptor.java
 *
 *
 * Created: Thu Oct  9 12:49:18 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 */
public class SinglePoolConnectionInterceptor implements ConnectionInterceptor {

    private static Log log = LogFactory.getLog(GeronimoConnectionEventListener.class.getName());


    private final ConnectionInterceptor next;

    private FIFOSemaphore permits;

    private PoolDeque pool;

    private final Subject defaultSubject;

    private final ConnectionRequestInfo defaultCRI;

    private int maxSize;

    private int blockingTimeout;

    public SinglePoolConnectionInterceptor(
            final ConnectionInterceptor next,
            final Subject defaultSubject,
            final ConnectionRequestInfo defaultCRI,
            int maxSize,
            int blockingTimeout) {
        this.next = next;
        this.defaultSubject = defaultSubject;
        this.defaultCRI = defaultCRI;
        this.maxSize = maxSize;
        this.blockingTimeout = blockingTimeout;
        permits = new FIFOSemaphore(maxSize);
        pool = new PoolDeque(maxSize);
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        try {
            if (permits.attempt(blockingTimeout)) {
                ManagedConnectionInfo newMCI = null;
                synchronized (pool) {
                    if (pool.isEmpty()) {
                        next.getConnection(connectionInfo);
                        if (log.isTraceEnabled()) {
                            log.trace("Returning new connection " + connectionInfo.getManagedConnectionInfo());
                        }
                        return;
                    } else {
                        newMCI = (ManagedConnectionInfo) pool.removeFirst();
                    } // end of else
                    try {
                        ManagedConnection matchedMC =
                                newMCI
                                .getManagedConnectionFactory()
                                .matchManagedConnections(
                                        Collections.singleton(
                                                newMCI.getManagedConnection()),
                                        mci.getSubject(),
                                        mci.getConnectionRequestInfo());
                        if (matchedMC != null) {
                            connectionInfo.setManagedConnectionInfo(newMCI);
                            if (log.isTraceEnabled()) {
                                log.trace("Returning pooled connection " + connectionInfo.getManagedConnectionInfo());
                            }
                            return;
                        } else {
                            //matching failed.
                            ConnectionInfo returnCI = new ConnectionInfo();
                            returnCI.setManagedConnectionInfo(newMCI);
                            returnConnection(
                                    returnCI,
                                    ConnectionReturnAction.RETURN_HANDLE);
                            throw new ResourceException("The pooling strategy does not match the MatchManagedConnections implementation.  Please investigate and reconfigure this pool");
                        }
                    } catch (ResourceException e) {
                        //something is wrong: destroy connection, rethrow, release permit
                        ConnectionInfo returnCI = new ConnectionInfo();
                        returnCI.setManagedConnectionInfo(newMCI);
                        returnConnection(
                                returnCI,
                                ConnectionReturnAction.DESTROY);
                        throw e;
                    } // end of try-catch
                }
            } else {
                throw new ResourceException(
                        "No ManagedConnections available "
                        + "within configured blocking timeout ( "
                        + blockingTimeout
                        + " [ms] )");

            } // end of else

        } catch (InterruptedException ie) {
            throw new ResourceException("Interrupted while requesting permit!");
        } // end of try-catch
    }

    public void returnConnection(
            ConnectionInfo connectionInfo,
            ConnectionReturnAction connectionReturnAction) {
        if (log.isTraceEnabled()) {
            log.trace("returning connection" + connectionInfo.getConnectionHandle());
        }
        boolean wasInPool = false;
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        if (connectionReturnAction == ConnectionReturnAction.DESTROY) {
            synchronized (pool) {
                wasInPool = pool.remove(mci);
            }
        } else {
            if (mci.hasConnectionHandles()) {
                return;
            }
        } // end of else

        ManagedConnection mc = mci.getManagedConnection();
        try {
            mc.cleanup();
        } catch (ResourceException e) {
            connectionReturnAction = ConnectionReturnAction.DESTROY;
        }

        if (connectionReturnAction == ConnectionReturnAction.DESTROY) {
            next.returnConnection(connectionInfo, connectionReturnAction);
        } else {
            synchronized (pool) {
                mci.setLastUsed(System.currentTimeMillis());
                pool.addFirst(mci);
            }

        } // end of else

        if (!wasInPool) {
            permits.release();
        }
    }

    static class PoolDeque {

        private final ManagedConnectionInfo[] deque;
        private int first = 0;
        private int last = -1;

        public PoolDeque(int size) {
            deque = new ManagedConnectionInfo[size];
        }

        public boolean isEmpty() {
            return first > last;
        }

        public ManagedConnectionInfo removeFirst() {
            if (isEmpty()) {
                throw new IllegalStateException("deque is empty");
            }
            return deque[first++];
        }

        public void addFirst(ManagedConnectionInfo mci) {
            if (first == 0) {
                throw new IllegalStateException("deque is at first element already");
            }

            deque[--first] = mci;
        }

        public void addLast(ManagedConnectionInfo mci) {
            if (last == deque.length - 1) {
                throw new IllegalStateException("deque is full");
            }

            deque[++last] = mci;
        }

        public ManagedConnectionInfo peekLast() {
            if (isEmpty()) {
                throw new IllegalStateException("deque is empty");
            }

            return deque[last];
        }

        public ManagedConnectionInfo removeLast() {
            if (isEmpty()) {
                throw new IllegalStateException("deque is empty");
            }

            return deque[last--];
        }

        public boolean remove(ManagedConnectionInfo mci) {
            for (int i = first; i <= last; i++) {
                if (deque[i] == mci) {
                    for (int j = i + 1; j <= last; j++) {
                        deque[j - 1] = deque[j];
                    }
                    last--;
                    return true;
                }

            }
            return false;
        }
    }

} // SinglePoolConnectionInterceptor
