package org.apache.geronimo.naming.proxy;

/**
 * Interface for connection factory containers.  The id is used by the ProxyManager to correlate which external proxies
 * should be hooked up with which containers.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/15 16:33:44 $
 *
 * */
public interface CFContainer {
    Object getProxy();
    Object getId();
}
