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

import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.corba.transaction.OperationTxPolicy;
import org.apache.openejb.core.transaction.TransactionType;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class NoDTxServerTransactionPolicies {
	private static final Map<TransactionType, OperationTxPolicy> policies = new HashMap<TransactionType, OperationTxPolicy>();
    static {    	
        policies.put(TransactionType.Mandatory, Required.INSTANCE);        
        policies.put(TransactionType.Never, NotRequired.INSTANCE);        
        policies.put(TransactionType.NotSupported, Ignore.INSTANCE);        
        policies.put(TransactionType.Required, NotRequired.INSTANCE);        
        policies.put(TransactionType.RequiresNew, Ignore.INSTANCE);        
        policies.put(TransactionType.Supports, NotRequired.INSTANCE);
    }

    public static OperationTxPolicy getContainerTransactionPolicy(TransactionType transactionType) {
        return policies.get(transactionType);
    }
    
    public static OperationTxPolicy getBeanTransactionPolicy() {
        return Ignore.INSTANCE;
    }

}
