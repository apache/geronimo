package org.apache.geronimo.test.local;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class TestLocalBean implements SessionBean {
	
	public void ejbActivate() throws EJBException, RemoteException {
	}

	public void ejbPassivate() throws EJBException, RemoteException {
	}

	public void ejbRemove() throws EJBException, RemoteException {
	}

	public void ejbCreate() throws EJBException {
	}
	
	public void setSessionContext(SessionContext arg0) throws EJBException, RemoteException {
	}
	
	public String echoLocal(String name)
	{
		System.out.println("Bean local class " + name);
		return "local bean "+name;
	}

}
