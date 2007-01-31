package test;

import java.rmi.RemoteException;

import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

public class TestBean implements SessionBean {

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
	
	public String echo(String name)
	{
		System.out.println("Bean class " + name);
		return "bean "+name;
	}
}
