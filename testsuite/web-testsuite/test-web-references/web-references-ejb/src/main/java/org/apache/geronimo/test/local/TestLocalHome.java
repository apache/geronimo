package org.apache.geronimo.test.local;

import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;

public interface TestLocalHome extends EJBLocalHome {
	 public TestLocal create() throws CreateException;

}
