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

//import EDU.oswego.cs.dl.util.concurrent.ConcurrentHashMap;

import java.util.HashMap;
import java.util.Map;

import javax.resource.ResourceException;
import javax.resource.spi.ConnectionRequestInfo;
import javax.security.auth.Subject;

/**
 * MultiPoolConnectionInterceptor.java
 *
 *
 * Created: Fri Oct 10 12:53:11 2003
 *
 * @version 1.0
 */
public class MultiPoolConnectionInterceptor implements ConnectionInterceptor {

    private final ConnectionInterceptor next;

    private int maxSize;

    private int blockingTimeout;

    private final boolean useSubject;

    private final boolean useCRI;

    private final Map pools = new HashMap();

    public MultiPoolConnectionInterceptor(
            final ConnectionInterceptor next,
            int maxSize,
            int blockingTimeout,
            final boolean useSubject,
            final boolean useCRI) {
        this.next = next;
        this.maxSize = maxSize;
        this.blockingTimeout = blockingTimeout;
        this.useSubject = useSubject;
        this.useCRI = useCRI;
    }

    public void getConnection(ConnectionInfo connectionInfo) throws ResourceException {
        ManagedConnectionInfo mci = connectionInfo.getManagedConnectionInfo();
        SubjectCRIKey key =
                new SubjectCRIKey(
                        useSubject ? mci.getSubject() : null,
                        useCRI ? mci.getConnectionRequestInfo() : null);
        SinglePoolConnectionInterceptor poolInterceptor = null;
        synchronized (pools) {
            poolInterceptor = (SinglePoolConnectionInterceptor) pools.get(key);
            if (poolInterceptor == null) {
                poolInterceptor =
                        new SinglePoolConnectionInterceptor(
                                next,
                                mci.getSubject(),
                                mci.getConnectionRequestInfo(),
                                maxSize,
                                blockingTimeout);
                pools.put(key, poolInterceptor);
            } // end of if ()

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
