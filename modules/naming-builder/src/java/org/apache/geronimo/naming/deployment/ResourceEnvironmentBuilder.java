package org.apache.geronimo.naming.deployment;

import java.util.Set;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/06/25 21:35:11 $
 *
 * */
public interface ResourceEnvironmentBuilder {
    Set getUnshareableResources();

    void setUnshareableResources(Set unshareableResources);

    Set getApplicationManagedSecurityResources();

    void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources);
}
