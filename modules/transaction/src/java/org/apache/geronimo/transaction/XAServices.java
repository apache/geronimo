package org.apache.geronimo.transaction;

import javax.transaction.TransactionManager;
import javax.resource.spi.XATerminator;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/07/11 21:55:33 $
 *
 * */
public interface XAServices extends XATerminator, XAWork {
}
