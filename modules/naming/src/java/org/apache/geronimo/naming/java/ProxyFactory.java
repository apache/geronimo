package org.apache.geronimo.naming.java;

import javax.naming.NamingException;

/**
 *
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/12 20:38:18 $
 *
 * */
public interface ProxyFactory {

    Object getProxy(Class homeInterface, Class remoteInterface, Object targetId) throws NamingException;

    Object getProxy(Class interfaced, Object targetId) throws NamingException;
}
