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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.resource.spi.XATerminator;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.transaction.XAWork;
import org.apache.geronimo.transaction.manager.Recovery;
import org.apache.geronimo.transaction.manager.XidImporter;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class TransactionContextManager implements XATerminator, XAWork {
    private static final boolean NOT_IN_RECOVERY = false;
    private static final boolean IN_RECOVERY = true;


    private final TransactionManager transactionManager;
    private final XidImporter importer;
    private final Recovery recovery;
    private final Map importedTransactions = new HashMap();

    private boolean recoveryState = NOT_IN_RECOVERY;

    //use as reference endpoint.
    public TransactionContextManager() {
        this(null, null, null);
    }

    public TransactionContextManager(TransactionManager transactionManager, XidImporter importer, Recovery recovery) {
        this.transactionManager = transactionManager;
        this.importer = importer;
        this.recovery = recovery;
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


    /**
     * TODO write and use ImportedTransactionContext for this!
      * @see javax.resource.spi.XATerminator#commit(javax.transaction.xa.Xid, boolean)
      */
     public void commit(Xid xid, boolean onePhase) throws XAException {
         ImportedTransactionInfo txInfo;
         synchronized (importedTransactions) {
             txInfo = (ImportedTransactionInfo) importedTransactions.remove(xid);
         }
         if (txInfo == null) {
             throw new XAException("No imported transaction for xid: " + xid);
         }
         ContainerTransactionContext containerTransactionContext = txInfo.getContainerTransactionContext();

         try {
             int status = containerTransactionContext.getTransaction().getStatus();
             assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
         } catch (SystemException e) {
             throw new XAException();
         }
         importer.commit(containerTransactionContext.getTransaction(), onePhase);
     }

     /**
      * @see javax.resource.spi.XATerminator#forget(javax.transaction.xa.Xid)
      */
     public void forget(Xid xid) throws XAException {
         ImportedTransactionInfo txInfo;
         synchronized (importedTransactions) {
             txInfo = (ImportedTransactionInfo) importedTransactions.remove(xid);
         }
         if (txInfo == null) {
             throw new XAException("No imported transaction for xid: " + xid);
         }
         Transaction tx = txInfo.getContainerTransactionContext().getTransaction();
         //todo is there a correct status test here?
//        try {
//            int status = tx.getStatus();
//            assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
//        } catch (SystemException e) {
//            throw new XAException();
//        }
         importer.forget(tx);
     }

     /**
      * @see javax.resource.spi.XATerminator#prepare(javax.transaction.xa.Xid)
      */
     public int prepare(Xid xid) throws XAException {
         ImportedTransactionInfo txInfo;
         synchronized (importedTransactions) {
             txInfo = (ImportedTransactionInfo) importedTransactions.get(xid);
         }
         if (txInfo == null) {
             throw new XAException("No imported transaction for xid: " + xid);
         }
         Transaction tx = txInfo.getContainerTransactionContext().getTransaction();
         try {
             int status = tx.getStatus();
             assert status == Status.STATUS_ACTIVE;
         } catch (SystemException e) {
             throw new XAException();
         }
         return importer.prepare(tx);
     }

     /**
      * @see javax.resource.spi.XATerminator#recover(int)
      */
     public Xid[] recover(int flag) throws XAException {
         if (recoveryState == NOT_IN_RECOVERY) {
             if ((flag & XAResource.TMSTARTRSCAN) == 0) {
                 throw new XAException(XAException.XAER_PROTO);
             }
             recoveryState = IN_RECOVERY;
         }
         if ((flag & XAResource.TMENDRSCAN) != 0) {
             recoveryState = NOT_IN_RECOVERY;
         }
         //we always return all xids in first call.
         //calling "startrscan" repeatedly starts at beginning of list again.
         if ((flag & XAResource.TMSTARTRSCAN) != 0) {
             Map recoveredXidMap = recovery.getExternalXids();
             Xid[] recoveredXids = new Xid[recoveredXidMap.size()];
             int i = 0;
             synchronized (importedTransactions) {
                 for (Iterator iterator = recoveredXidMap.entrySet().iterator(); iterator.hasNext();) {
                     Map.Entry entry = (Map.Entry) iterator.next();
                     Xid xid = (Xid) entry.getKey();
                     recoveredXids[i++] = xid;
                     ImportedTransactionInfo txInfo = new ImportedTransactionInfo(new ContainerTransactionContext(transactionManager, (Transaction)entry.getValue()));
                     importedTransactions.put(xid, txInfo);
                 }
             }
             return recoveredXids;
         } else {
             return new Xid[0];
         }
     }

     /**
      * @see javax.resource.spi.XATerminator#rollback(javax.transaction.xa.Xid)
      */
     public void rollback(Xid xid) throws XAException {
         ImportedTransactionInfo txInfo;
         synchronized (importedTransactions) {
             txInfo = (ImportedTransactionInfo) importedTransactions.remove(xid);
         }
         if (txInfo == null) {
             throw new XAException("No imported transaction for xid: " + xid);
         }
         Transaction tx = txInfo.getContainerTransactionContext().getTransaction();

         try {
             int status = tx.getStatus();
             assert status == Status.STATUS_ACTIVE || status == Status.STATUS_PREPARED;
         } catch (SystemException e) {
             throw new XAException();
         }
         importer.rollback(tx);
     }


    //XAWork implementation
    public void begin(Xid xid, long txTimeoutMillis) throws XAException, InvalidTransactionException, SystemException {
        ImportedTransactionInfo txInfo;
        boolean old = true;
        synchronized (importedTransactions) {
             txInfo = (ImportedTransactionInfo) importedTransactions.get(xid);
            if (txInfo == null) {
                try {
                    Transaction transaction = importer.importXid(xid);
                    ContainerTransactionContext containerTransactionContext = new ContainerTransactionContext(transactionManager, transaction);
                    txInfo = new ImportedTransactionInfo(containerTransactionContext);
                    old = false;
                } catch (SystemException e) {
                    throw (XAException) new XAException("Could not import xid").initCause(e);
                }
                importedTransactions.put(xid, txInfo);
            }
            if (txInfo.isActive()) {
                throw new XAException("Xid already active");
            }
            txInfo.setActive(true);
        }
        if (old) {
            txInfo.getContainerTransactionContext().resume();
        }
        importer.setTransactionTimeout(txTimeoutMillis);
    }

    public void end(Xid xid) throws XAException, SystemException {
        synchronized (importedTransactions) {
            ImportedTransactionInfo txInfo = (ImportedTransactionInfo) importedTransactions.get(xid);
            if (txInfo == null) {
                throw new XAException("No imported transaction for xid: " + xid);
            }
            if (!txInfo.isActive()) {
                throw new XAException("tx not active for xid: " + xid);
            }
            txInfo.getContainerTransactionContext().suspend();
            txInfo.setActive(false);
        }
    }

    //todo make an imported transaction context with this info.
    private static class ImportedTransactionInfo {
        private final ContainerTransactionContext containerTransactionContext;
        private boolean active;

        public ImportedTransactionInfo(ContainerTransactionContext containerTransactionContext) {
            this.containerTransactionContext = containerTransactionContext;
        }

        public ContainerTransactionContext getContainerTransactionContext() {
            return containerTransactionContext;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
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
        infoFactory.addReference("XidImporter", XidImporter.class);
        infoFactory.addReference("Recovery", Recovery.class);

        infoFactory.addInterface(XATerminator.class);
        infoFactory.addInterface(XAWork.class);

        infoFactory.setConstructor(new String[] {"TransactionManager", "XidImporter", "Recovery"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
