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

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.omg.CORBA.Any;
import org.omg.CORBA.INTERNAL;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.PropagationContextHelper;
import org.omg.CosTransactions.TransIdentity;
import org.omg.CosTransactions.otid_t;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TransactionService;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.apache.geronimo.corba.transaction.ClientTransactionPolicyConfig;
import org.apache.geronimo.corba.util.Util;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class NoDTxClientTransactionPolicyConfig implements ClientTransactionPolicyConfig {

    private static final long serialVersionUID = 3330069139634001416L;
    private static final TransIdentity[] NO_PARENTS = new TransIdentity[0];
    private static final otid_t NULL_XID = new otid_t(0, 0, new byte[0]);

    private final TransactionManager transactionManager;

    
    public static boolean isTransactionActive(TransactionManager transactionManager) {
        try {
            int status = transactionManager.getStatus();
            return status == Status.STATUS_ACTIVE || status == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException ignored) {
            return false;
        }
    }

    public NoDTxClientTransactionPolicyConfig(TransactionManager transactionManager) {
        if (transactionManager == null) {
            throw new IllegalArgumentException("transactionManager must not be null");
        }
        this.transactionManager = transactionManager;
    }

    public void exportTransaction(ClientRequestInfo ri) {
        if (isTransactionActive(transactionManager)) {
            //19.6.2.1 (1) propagate an "empty" transaction context.
            //but, it needs an xid!
            TransIdentity transIdentity = new TransIdentity(null, null, NULL_XID);
            int timeout = 0;
            Any implementationSpecificData = Util.getORB().create_any();
            PropagationContext propagationContext = new PropagationContext(timeout, transIdentity, NO_PARENTS, implementationSpecificData);
            Codec codec = Util.getCodec();
            Any any = Util.getORB().create_any();
            PropagationContextHelper.insert(any, propagationContext);
            byte[] encodedPropagationContext;
            try {
                encodedPropagationContext = codec.encode_value(any);
            } catch (InvalidTypeForEncoding invalidTypeForEncoding) {
                throw (INTERNAL)new INTERNAL("Could not encode propagationContext").initCause(invalidTypeForEncoding);
            }
            ServiceContext otsServiceContext = new ServiceContext(TransactionService.value, encodedPropagationContext);
            ri.add_request_service_context(otsServiceContext, true);
        }

    }
}
