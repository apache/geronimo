package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.XAResource;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/08 17:33:42 $
 *
 * */
public interface NamedXAResource extends XAResource {

    String getName();

}
