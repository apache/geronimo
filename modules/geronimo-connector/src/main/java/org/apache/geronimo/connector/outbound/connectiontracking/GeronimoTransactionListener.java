/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.connector.outbound.connectiontracking;

import javax.resource.ResourceException;
import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.transaction.manager.TransactionManagerMonitor;

/**
 * @version $Rev$ $Date$
 */
public class GeronimoTransactionListener implements TransactionManagerMonitor {
    private static final Log log = LogFactory.getLog(GeronimoTransactionListener.class);
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    public GeronimoTransactionListener(TrackedConnectionAssociator trackedConnectionAssociator) {
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void threadAssociated(Transaction transaction) {
        try {
            trackedConnectionAssociator.newTransaction();
        } catch (ResourceException e) {
            log.warn("Error notifying connection tranker of transaction association", e);
        }
    }

    public void threadUnassociated(Transaction transaction) {
    }
}
