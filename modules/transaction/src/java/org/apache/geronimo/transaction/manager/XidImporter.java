package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import javax.transaction.Transaction;
import javax.transaction.SystemException;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/23 20:28:43 $
 *
 * */
public interface XidImporter {

    Transaction importXid(Xid xid) throws XAException, SystemException;

    void commit(Transaction tx, boolean onePhase) throws XAException;
    void forget(Transaction tx) throws XAException;
    int prepare(Transaction tx) throws XAException;
    void rollback(Transaction tx) throws XAException;
    void setTransactionTimeout(long milliseconds);
}
