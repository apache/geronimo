package org.apache.geronimo.connector.outbound.security;

import javax.resource.spi.ManagedConnectionFactory;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:10 $
 *
 * */
public interface ManagedConnectionFactoryListener {
    void setManagedConnectionFactory(ManagedConnectionFactory managedConnectionFactory);
}
