package org.apache.geronimo.transaction.manager;

import javax.transaction.xa.XAResource;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface NamedXAResource extends XAResource {

    String getName();

}
