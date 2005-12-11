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

package org.apache.geronimo.transaction.context;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.ExtendedTransactionManager;
import org.apache.geronimo.transaction.XAWork;
import org.apache.geronimo.transaction.manager.XidImporter;

import javax.resource.spi.XATerminator;

/**
 * Used to provide the GBean metadata for the TransactionContextManager class
 *
 * @version $Rev$ $Date$
 */
public class TransactionContextManagerGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(TransactionContextManagerGBean.class, TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);

        infoFactory.addOperation("getTransactionManager");
        infoFactory.addOperation("getContext");
        infoFactory.addOperation("setContext", new Class[]{TransactionContext.class});
        infoFactory.addOperation("newContainerTransactionContext");
        infoFactory.addOperation("newBeanTransactionContext", new Class[] {long.class});
        infoFactory.addOperation("newUnspecifiedTransactionContext");
        infoFactory.addOperation("resumeBeanTransactionContext",  new Class[] {TransactionContext.class});
        infoFactory.addOperation("suspendBeanTransactionContext");
        infoFactory.addOperation("getStatus");
        infoFactory.addOperation("setRollbackOnly");
        infoFactory.addOperation("setTransactionTimeout", new Class[] {int.class});

        infoFactory.addReference("TransactionManager", ExtendedTransactionManager.class, NameFactory.TRANSACTION_MANAGER);
        infoFactory.addReference("XidImporter", XidImporter.class, NameFactory.TRANSACTION_MANAGER);

        infoFactory.addInterface(XATerminator.class);
        infoFactory.addInterface(XAWork.class);

        infoFactory.setConstructor(new String[]{"TransactionManager", "XidImporter"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
