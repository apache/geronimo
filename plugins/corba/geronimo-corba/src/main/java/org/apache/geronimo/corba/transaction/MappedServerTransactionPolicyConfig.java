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
package org.apache.geronimo.corba.transaction;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.PropagationContext;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class MappedServerTransactionPolicyConfig extends AbstractServerTransactionPolicyConfig {
    private static Logger log = LoggerFactory.getLogger(MappedServerTransactionPolicyConfig.class);
    private final Map operationToPolicyMap;
    public MappedServerTransactionPolicyConfig(Map operationToPolicyMap) {
        this.operationToPolicyMap = operationToPolicyMap;
    }

    protected void importTransaction(String operation, PropagationContext propagationContext) throws SystemException {
        OperationTxPolicy operationTxPolicy = (OperationTxPolicy) operationToPolicyMap.get(operation);
        if (operationTxPolicy == null) {
            //TODO figure out if there is some way to detect if the method should be mapped or shouldn't
            //e.g. _is_a shows up but should not be mapped.
            log.info("No tx mapping for operation: " + operation);
            return;
        }
        operationTxPolicy.importTransaction(propagationContext);
     }

}
