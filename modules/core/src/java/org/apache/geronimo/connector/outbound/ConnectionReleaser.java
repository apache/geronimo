package org.apache.geronimo.connector.outbound;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/09 04:15:20 $
 *
 * */
public interface ConnectionReleaser {
    void afterCompletion(ManagedConnectionInfo managedConnectionInfo);
}
