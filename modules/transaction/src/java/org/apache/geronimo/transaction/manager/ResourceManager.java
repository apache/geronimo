package org.apache.geronimo.transaction.manager;

import java.util.List;

import javax.transaction.SystemException;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/11 19:20:55 $
 *
 * */
public interface ResourceManager {

    NamedXAResource getRecoveryXAResources() throws SystemException;

    void returnResource(NamedXAResource xaResource);

}
