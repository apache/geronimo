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

package org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.transaction.Transaction;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import javax.transaction.RollbackException;

import org.apache.geronimo.connector.outbound.ConnectorTransactionContext;
import org.apache.geronimo.connector.outbound.ManagedConnectionInfo;
import org.apache.geronimo.connector.outbound.ConnectionReleaser;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2003/12/13 23:33:54 $
 *
 * */
public class DefaultTransactionContext implements ConnectorTransactionContext, Synchronization {

    private Map managedConnections;

    private final Transaction transaction;

    public DefaultTransactionContext(Transaction transaction) throws SystemException, RollbackException {
        this.transaction = transaction;
        if (transaction != null) {
            assert transaction.getStatus() == Status.STATUS_ACTIVE;
            transaction.registerSynchronization(this);
        }
    }

    /**
     * Don't try to cache connections if there is no transaction, since there is no
     * event that tells us to release the connection.
     * @param key
     * @param info
     */
    public void setManagedConnectionInfo(ConnectionReleaser key, ManagedConnectionInfo info) {
        if (isActive()) {
            if (managedConnections == null) {
                managedConnections = new HashMap();
            }
            managedConnections.put(key, info);
        }
    }

    public ManagedConnectionInfo getManagedConnectionInfo(ConnectionReleaser key) {
        if (managedConnections == null) {
            return null;
        }
        return (ManagedConnectionInfo) managedConnections.get(key);
    }

    /**
     * I'm not sure I got the condition right here.
     * @return
     */
    public boolean isActive() {
        try {
            return transaction != null && (transaction.getStatus() == Status.STATUS_ACTIVE);
        } catch (SystemException e) {
            return false; //this is doubtful
        }
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void beforeCompletion() {
    }

    public void afterCompletion(int status) {
        if (managedConnections != null) {
            for (Iterator entries = managedConnections.entrySet().iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry) entries.next();
                ConnectionReleaser key = (ConnectionReleaser) entry.getKey();
                key.afterCompletion((ManagedConnectionInfo)entry.getValue());
            }
            //should we clear managedConnections?  might be less work for garbage collector.  any other reason?
        }
    }
}
