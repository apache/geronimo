package org.apache.geronimo.connector.outbound;

import java.util.Map;

/**
 * Interface to be implemented by e.g. ejb context objects.  This is used by
 * CachedConnectionManager to store ManagedConnections for each ConnectionManager
 * used by the component.  Storing in the component context should avoid synchronization...
 * enough synchronization occurs in calling the ejb itself.
 *
 * @version $Revision: 1.1 $ $Date: 2003/12/09 04:15:20 $
 *
 * */
public interface ConnectorComponentContext {
    /**
     * IMPORTANT INVARIANT: this should always return a map, never null.
     * @return map of ConnectionManager to (list of ) managed connection info objects.
     */
    public Map getConnectionManagerMap();

}
