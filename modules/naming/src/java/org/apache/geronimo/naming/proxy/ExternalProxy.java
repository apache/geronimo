package org.apache.geronimo.naming.proxy;

/**
 * ExternalProxies are supplied by the ProxyFactory to the  ReadOnlyContext, and registered
 * with the ProxyManager together with an id.  When a gbean supplying the same id is registered
 * with the ProxyManager, the ProxyManager calls setTarget on the ExternalProxy.  The externalProxy
 * is expected to use the internal proxy supplied to make itself usable.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/15 16:33:44 $
 *
 * */
public interface ExternalProxy {
    void setTarget(Object internalProxy);
}
