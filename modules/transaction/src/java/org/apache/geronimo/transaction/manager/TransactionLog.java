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

package org.apache.geronimo.transaction.manager;

import java.util.Collection;
import java.util.List;

import javax.transaction.xa.Xid;


/**
 * Interface used to notify a logging subsystem of transaction events.
 *
 * @version $Rev$ $Date$
 */
public interface TransactionLog {

    void begin(Xid xid) throws LogException;

    /**
     * log prepare for the global xid xid and the list of TransactionBranchInfo branches
     * @param xid global xid for the transactions
     * @param branches List of TransactionBranchInfo
     * @throws LogException
     */
    long prepare(Xid xid, List branches) throws LogException;

    void commit(Xid xid, long logMark) throws LogException;

    void rollback(Xid xid, long logMark) throws LogException;

    /**
     * Recovers the log, returning a map of (top level) xid to List of TransactionBranchInfo for the branches.
     * Uses the XidFactory to reconstruct the xids.
     *
     * @param xidFactory
     * @return Map of recovered xid to List of TransactionBranchInfo representing the branches.
     * @throws LogException
     */
    Collection recover(XidFactory xidFactory) throws LogException;

    String getXMLStats();

    int getAverageForceTime();

    int getAverageBytesPerForce();
}
