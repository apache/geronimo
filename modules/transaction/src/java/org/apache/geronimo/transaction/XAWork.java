package org.apache.geronimo.transaction;

import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;

/**
 * primarily an interface between the WorkManager/ExecutionContext and the tm.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/23 20:28:43 $
 *
 * */
public interface XAWork {
    void begin(Xid xid, long txTimeout) throws XAException;
    void end(Xid xid) throws XAException;
}
