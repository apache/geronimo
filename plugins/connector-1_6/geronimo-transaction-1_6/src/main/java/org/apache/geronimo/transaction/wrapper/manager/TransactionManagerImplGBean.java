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

package org.apache.geronimo.transaction.wrapper.manager;

import javax.transaction.xa.XAException;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.manager.TransactionLog;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transaction.manager.TransactionTimer;
import org.apache.geronimo.transaction.manager.XidFactory;

/**
 * Simple implementation of a transaction manager.  This does not include XATerminator or XAWork functionality:
 * use GeronimoTransactionManagerGBean if you need to import transactions.
 *
 * @version $Rev$ $Date$
 */
@GBean(j2eeType=NameFactory.JTA_RESOURCE)
public class TransactionManagerImplGBean extends TransactionManagerImpl {

    /**
     * TODO NOTE!!! this should be called in an unspecified transaction context, but we cannot enforce this restriction!
     */
    public TransactionManagerImplGBean(@ParamAttribute(name="defaultTransactionTimeoutSeconds") int defaultTransactionTimeoutSeconds,
                                       @ParamReference(name="XidFactory", namingType=NameFactory.XID_FACTORY) XidFactory xidFactory, 
                                       @ParamReference(name="TransactionLog", namingType=NameFactory.TRANSACTION_LOG) TransactionLog transactionLog) throws XAException {
        super(defaultTransactionTimeoutSeconds, xidFactory, transactionLog);
        // Start the TransactionTimer$CurrentTime thread. This should avoid potential ClassLoader 
        // memory leaks caused by InheritableThreadLocals on the CurrentTime thread. See GERONIMO-4869 for more info.
        TransactionTimer.getCurrentTime();
    }

}
