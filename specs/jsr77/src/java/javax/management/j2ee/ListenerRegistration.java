package javax.management.j2ee;

import javax.management.*;
import java.io.Serializable;
import java.rmi.RemoteException;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public class ListenerRegistration implements Serializable {
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, RemoteException {
        /*@todo implement*/
    }

    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException, RemoteException {
        /*@todo implement*/
    }
}