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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectiontracking.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.TransactionContext;
import org.apache.geronimo.transaction.InstanceContext;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/01/31 19:27:16 $
 *
 * */
public class DefaultComponentInterceptor implements DefaultInterceptor {

    private final DefaultInterceptor next;
    private final TrackedConnectionAssociator cachedConnectionAssociator;
    private final Set unshareableResources;
    private final TransactionManager transactionManager;
    private final Map transactionToTransactionContextMap = new HashMap();

    public DefaultComponentInterceptor(DefaultInterceptor next,
            TrackedConnectionAssociator cachedConnectionManager,
            Set unshareableResources,
            TransactionManager transactionManager) {
        this.next = next;
        this.cachedConnectionAssociator = cachedConnectionManager;
        this.unshareableResources = unshareableResources;
        this.transactionManager = transactionManager;
    }

    public Object invoke(InstanceContext newInstanceContext) throws Throwable {
        Transaction transaction = transactionManager.getTransaction();
        TransactionContext newTransactionContext;
        if (transaction == null || transaction.getStatus() == Status.STATUS_COMMITTED || transaction.getStatus() == Status.STATUS_ROLLEDBACK) {
            newTransactionContext = new DefaultTransactionContext(null);
        } else {
            newTransactionContext = (TransactionContext) transactionToTransactionContextMap.get(transaction);
            if (newTransactionContext == null) {
                newTransactionContext = new DefaultTransactionContext(transaction);
                transactionToTransactionContextMap.put(transaction, newTransactionContext);
            }
        }
        Set oldUnshareableResources = cachedConnectionAssociator.setUnshareableResources(unshareableResources);
        InstanceContext oldInstanceContext = cachedConnectionAssociator.enter(newInstanceContext);
        TransactionContext oldTransactionContext = cachedConnectionAssociator.setTransactionContext(newTransactionContext);
        try {
            return next.invoke(newInstanceContext);
        } finally {
            cachedConnectionAssociator.exit(oldInstanceContext, unshareableResources);
            cachedConnectionAssociator.resetTransactionContext(oldTransactionContext);
            cachedConnectionAssociator.setUnshareableResources(oldUnshareableResources);
        }
    }
}
