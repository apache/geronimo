package org.apache.geronimo.transaction;

import java.util.Map;
import javax.transaction.TransactionManager;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

/**
 */
public interface ExtendedTransactionManager extends TransactionManager {

    Transaction begin(long transactionTimeoutMilliseconds) throws NotSupportedException, SystemException ;

    Map getExternalXids();

}
