package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface TransactionBranchInfo {

    String getResourceName();

    Xid getBranchXid();

}
