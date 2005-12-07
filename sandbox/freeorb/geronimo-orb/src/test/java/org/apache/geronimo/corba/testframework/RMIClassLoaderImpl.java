package org.apache.geronimo.corba.testframework;

import java.net.MalformedURLException;
import java.rmi.server.RMIClassLoaderSpi;

public class RMIClassLoaderImpl extends RMIClassLoaderSpi {

	public Class loadClass(String arg0, String arg1, ClassLoader arg2)
			throws MalformedURLException, ClassNotFoundException {
		return Class.forName(arg1);
	}

	public Class loadProxyClass(String arg0, String[] arg1, ClassLoader arg2)
			throws MalformedURLException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	public ClassLoader getClassLoader(String arg0) throws MalformedURLException {
		return this.getClass().getClassLoader();
	}

	public String getClassAnnotation(Class arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
