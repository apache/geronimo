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

package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.XAException;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * Used to provide the GBean metadata for the GeronimoTransactionManager class
 *
 * @version $Rev$ $Date$
 */
public class GeronimoTransactionManagerGBean extends GeronimoTransactionManager {

    /**
     * TODO NOTE!!! this should be called in an unspecified transaction context, but we cannot enforce this restriction!
     */
    public GeronimoTransactionManagerGBean(int defaultTransactionTimeoutSeconds, XidFactory xidFactory, TransactionLog transactionLog) throws XAException {
        super(defaultTransactionTimeoutSeconds == 0 ? DEFAULT_TIMEOUT : defaultTransactionTimeoutSeconds, 
                xidFactory,
                transactionLog
        );
    }



    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(GeronimoTransactionManagerGBean.class,
                TransactionManagerImplGBean.GBEAN_INFO,
                NameFactory.TRANSACTION_MANAGER);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
