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

package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.Transaction;
import javax.transaction.SystemException;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:19 $
 *
 * */
public interface XidImporter {

    Transaction importXid(Xid xid) throws XAException, SystemException;

    void commit(Transaction tx, boolean onePhase) throws XAException;
    void forget(Transaction tx) throws XAException;
    int prepare(Transaction tx) throws XAException;
    void rollback(Transaction tx) throws XAException;
    void setTransactionTimeout(long milliseconds);
}
