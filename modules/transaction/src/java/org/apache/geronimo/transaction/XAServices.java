package org.apache.geronimo.transaction;

import javax.transaction.TransactionManager;
import javax.resource.spi.XATerminator;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface XAServices extends XATerminator, XAWork {
}
