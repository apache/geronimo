package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.Xid;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/11 19:20:55 $
 *
 * */
public interface TransactionBranchInfo {

    String getResourceName();

    Xid getBranchXid();

}
