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

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class TransactionContextManager {

    private final TransactionManager transactionManager;

    //use as reference endpoint.
    public TransactionContextManager() {
        transactionManager = null;
    }

    public TransactionContextManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public TransactionContext getContext() {
        return TransactionContext.getContext();
    }

    public void setContext(TransactionContext transactionContext) {
        TransactionContext.setContext(transactionContext);
    }

    public ContainerTransactionContext newContainerTransactionContext() throws NotSupportedException, SystemException {
        ContainerTransactionContext transactionContext = new ContainerTransactionContext(transactionManager);
        TransactionContext.setContext(transactionContext);
        transactionContext.begin();
        return transactionContext;
    }

    public BeanTransactionContext newBeanTransactionContext() throws NotSupportedException, SystemException {
        TransactionContext ctx = TransactionContext.getContext();
        if (ctx instanceof UnspecifiedTransactionContext == false) {
            throw new NotSupportedException("Previous Transaction has not been committed");
        }
        UnspecifiedTransactionContext oldContext = (UnspecifiedTransactionContext) ctx;
        BeanTransactionContext transactionContext = new BeanTransactionContext(transactionManager, oldContext);
        oldContext.suspend();
        try {
            transactionContext.begin();
        } catch (SystemException e) {
            oldContext.resume();
            throw e;
        } catch (NotSupportedException e) {
            oldContext.resume();
            throw e;
        }
        TransactionContext.setContext(transactionContext);
        return transactionContext;
    }

    public UnspecifiedTransactionContext newUnspecifiedTransactionContext() {
        UnspecifiedTransactionContext transactionContext = new UnspecifiedTransactionContext();
        TransactionContext.setContext(transactionContext);
        return transactionContext;
    }

    public int getStatus() throws SystemException {
        return transactionManager.getStatus();
    }

    public void setRollbackOnly() throws SystemException {
        transactionManager.setRollbackOnly();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        transactionManager.setTransactionTimeout(seconds);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(TransactionContextManager.class);
        infoFactory.addOperation("getContext");
        infoFactory.addOperation("setContext", new Class[] {TransactionContext.class});
        infoFactory.addOperation("newContainerTransactionContext");
        infoFactory.addOperation("newBeanTransactionContext");
        infoFactory.addOperation("newUnspecifiedTransactionContext");

        infoFactory.addReference("TransactionManager", TransactionManager.class);

        infoFactory.setConstructor(new String[] {"TransactionManager"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
