package javax.management.j2ee;

import javax.ejb.EJBHome;
import javax.ejb.CreateException;
import java.rmi.RemoteException;

/**
 *
 *
 *
 * @version $Revision: 1.1 $
 */
public interface ManagementHome extends EJBHome {
    public Management create() throws CreateException, RemoteException;
}