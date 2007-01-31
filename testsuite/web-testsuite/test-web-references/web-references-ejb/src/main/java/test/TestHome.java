package test;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.EJBHome;

public interface TestHome extends EJBHome {

	public Test create() throws RemoteException, CreateException;
}
