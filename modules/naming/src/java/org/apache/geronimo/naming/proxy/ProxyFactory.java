package org.apache.geronimo.naming.proxy;

import javax.naming.NamingException;

/**
 * An instance of ProxyFactory is supplied to the ComponentContextBuilder to let it construct objects
 * to bind in the ReadOnlyContext.  Using the ProxyManager, these would be ExternalProxies registered
 * with the ProxyManager.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/15 16:33:44 $
 *
 * */
public interface ProxyFactory {

    Object getProxy(Class homeInterface, Class remoteInterface, Object targetId) throws NamingException;

    Object getProxy(Class interfaced, Object targetId) throws NamingException;
}
