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

package org.apache.geronimo.connector.outbound;

import javax.resource.ResourceException;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.geronimo.transaction.manager.NamedXAResource;

/**
 * ConnectionManagerFactory
 *
 * @version $Revision: 1.6 $ $Date: 2004/06/11 19:22:04 $
 */
public interface ConnectionManagerFactory {

    Object createConnectionFactory(ManagedConnectionFactory mcf) throws ResourceException;

    ReturnableXAResource getRecoveryXAResource(ManagedConnectionFactory managedConnectionFactory) throws ResourceException;

    public class ReturnableXAResource implements NamedXAResource {
        private final ConnectionInterceptor stack;
        private final ConnectionInfo connectionInfo;
        private final NamedXAResource delegate;

        public ReturnableXAResource(NamedXAResource delegate, ConnectionInterceptor stack, ConnectionInfo connectionInfo) {
            this.delegate = delegate;
            this.stack = stack;
            this.connectionInfo = connectionInfo;
        }

        public void returnConnection() {
            stack.returnConnection(connectionInfo, ConnectionReturnAction.DESTROY);
        }

        public String getName() {
            return delegate.getName();
        }

        public void commit(Xid xid, boolean onePhase) throws XAException {
            delegate.commit(xid, onePhase);
        }

        public void end(Xid xid, int flags) throws XAException {
            delegate.end(xid, flags);
        }

        public void forget(Xid xid) throws XAException {
            delegate.forget(xid);
        }

        public int getTransactionTimeout() throws XAException {
            return delegate.getTransactionTimeout();
        }

        public boolean isSameRM(XAResource other) throws XAException {
            if (other instanceof ReturnableXAResource) {
                return delegate.isSameRM(((ReturnableXAResource)other).delegate);
            }
            return delegate.isSameRM(other);
        }

        public int prepare(Xid xid) throws XAException {
            return delegate.prepare(xid);
        }

        public Xid[] recover(int flag) throws XAException {
            return delegate.recover(flag);
        }

        public void rollback(Xid xid) throws XAException {
            delegate.rollback(xid);
        }

        public boolean setTransactionTimeout(int seconds) throws XAException {
            return delegate.setTransactionTimeout(seconds);
        }

        public void start(Xid xid, int flags) throws XAException {
            delegate.start(xid, flags);
        }


    }

}
