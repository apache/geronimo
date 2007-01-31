package org.apache.geronimo.test.local;

import java.rmi.RemoteException;

public interface TestLocal extends javax.ejb.EJBLocalObject {
	public String echoLocal(String name) throws RemoteException;
}
