package org.apache.geronimo.test.remote;

import java.rmi.RemoteException;

public interface Test extends javax.ejb.EJBObject {

	public String echo(String name) throws RemoteException;
}
