package org.apache.geronimo.connector.outbound;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:10 $
 *
 * */
public interface ConnectionReleaser {
    void afterCompletion(ManagedConnectionInfo managedConnectionInfo);
}
