package org.apache.geronimo.connector.outbound;

import javax.transaction.Transaction;


/**
 * Interface to be implemented by TransactionContext objects so the connector framework can
 * track ManagedConnections enrolled in a particular transaction.  This should avoid excessive
 * synchronization in the ConnectionManager code.
 *
 * This architecture assumes that a threadlocal lookup + hashmap creation + map lookup
 * is faster than synchronization + map lookup.
 *
 * The TransactionContext implementation is responsible for notifying all keys after completion.
 *
 * @version $Revision: 1.2 $ $Date: 2003/12/13 23:33:53 $
 *
 * */
public interface ConnectorTransactionContext {

    void setManagedConnectionInfo(ConnectionReleaser key, ManagedConnectionInfo info);

    ManagedConnectionInfo getManagedConnectionInfo(ConnectionReleaser key);

    boolean isActive();

    Transaction getTransaction();

}
