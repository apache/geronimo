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

//import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

import java.util.HashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;

/**
 * MultiPoolConnectionInterceptor maps the provided subject and connection request info to a
 * "SinglePool".  This can be used to make sure all matches will succeed, avoiding synchronization
 * slowdowns.
 *
 * Created: Fri Oct 10 12:53:11 2003
 *
 * @version $Rev$ $Date$
 */
public class MultiPoolConnectionInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;
    private final PoolingSupport singlePoolFactory;

    private final boolean useSubject;

    private final boolean useCRI;

    private final Map pools = new HashMap();

    public MultiPoolConnectionInterceptor(
            final ConnectionInterceptor next,
            PoolingSupport singlePoolFactory,
            final boolean useSubject,
            final boolean useCRI) {
        this.next = next;
        this.singlePoolFactory = singlePoolFactory;
        this.useSubject = useSubject;
        this.useCRI = useCRI;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        SubjectCRIKey key =
                new SubjectCRIKey(
                        useSubject ? mci.getSubject() : null,
                        useCRI ? mci.getConnectionRequestInfo() : null);
        ConnectionInterceptor poolInterceptor = null;
        synchronized (pools) {
            poolInterceptor = (ConnectionInterceptor) pools.get(key);
            if (poolInterceptor == null) {
                poolInterceptor = singlePoolFactory.addPoolingInterceptors(next);
                pools.put(key, poolInterceptor);
            }
        }
        mci.setPoolInterceptor(poolInterceptor);
        poolInterceptor.getConnection(connectionInfo);
    }

    public void returnConnection(
            ConnectionInfo connectionInfo,
            ConnectionReturnAction connectionReturnAction) {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        ConnectionInterceptor poolInterceptor = mci.getPoolInterceptor();
        poolInterceptor.returnConnection(connectionInfo, connectionReturnAction);
    }

    static class SubjectCRIKey {
        private final Subject subject;
        private final ConnectionRequestInfo cri;
        private final int hashcode;

        public SubjectCRIKey(
                final Subject subject,
                final ConnectionRequestInfo cri) {
            this.subject = subject;
            this.cri = cri;
            this.hashcode =
                    (subject == null ? 17 : subject.hashCode() * 17)
                    ^ (cri == null ? 1 : cri.hashCode());
        }

        public int hashCode() {
            return hashcode;
        }

        public boolean equals(Object other) {
            if (!(other instanceof SubjectCRIKey)) {
                return false;
            } // end of if ()
            SubjectCRIKey o = (SubjectCRIKey) other;
            if (hashcode != o.hashcode) {
                return false;
            } // end of if ()
            return subject == null
                    ? o.subject == null
                    : subject.equals(o.subject)
                    && cri == null ? o.cri == null : cri.equals(o.cri);
        }
    }
} // MultiPoolConnectionInterceptor
