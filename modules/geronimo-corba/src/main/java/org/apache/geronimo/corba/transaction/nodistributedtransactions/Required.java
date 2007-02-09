/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.transaction.nodistributedtransactions;

import java.io.Serializable;

import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_REQUIRED;
import org.omg.CosTransactions.PropagationContext;
import org.apache.geronimo.corba.transaction.OperationTxPolicy;

/**
 * Use for:
 * Mandatory
 *
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class Required implements OperationTxPolicy, Serializable {

    public static final OperationTxPolicy INSTANCE = new Required();

    public void importTransaction(PropagationContext propagationContext) {
        if (propagationContext == null) {
            throw new TRANSACTION_REQUIRED("Transaction required");
        }
        throw new INVALID_TRANSACTION("Transaction cannot be imported");
    }
}
