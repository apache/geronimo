package org.apache.geronimo.transaction.manager;

import javax.transaction.SystemException;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public interface ResourceManager {

    NamedXAResource getRecoveryXAResources() throws SystemException;

    void returnResource(NamedXAResource xaResource);

}
