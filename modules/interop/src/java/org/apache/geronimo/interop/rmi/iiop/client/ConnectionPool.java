/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop.client;

import java.util.HashMap;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.rmi.RmiTrace;
import org.apache.geronimo.interop.rmi.iiop.ObjectRef;
import org.apache.geronimo.interop.rmi.iiop.Protocol;
import org.apache.geronimo.interop.util.InstancePool;
import org.apache.geronimo.interop.util.StringUtil;

public class ConnectionPool {
    public static ConnectionPool getInstance(ClientNamingContext namingContext) {
        ConnectionPool object = new ConnectionPool();
        object.init(namingContext);
        return object;
    }

    private ClientNamingContext namingContext;
    private HashMap             poolMap;

    public Connection get(int protocol, String endpoint, ObjectRef objectRef) {
        //System.out.println("ConnectionPool.get(): protocol: " + protocol + ", endpoint: " + endpoint + ", objectRef: " + objectRef);

        InstancePool pool = getInstancePool(protocol, endpoint);
        System.out.println("ConnectionPool.get(): pool: " + pool);
        Connection conn = (Connection) pool.get();
        if (conn == null) {
            conn = newConnection(protocol, endpoint, objectRef, pool);
        }
        return conn;

        /*
        HostList hostList = resolve(endpoint, objectRef);
        System.out.println("ConnectionPool.get(): hostlist: " + hostList);
        if (hostList == null) {
            InstancePool pool = getInstancePool(protocol, endpoint);
            System.out.println("ConnectionPool.get(): pool: " + pool);
            Connection conn = (Connection) pool.get();
            if (conn == null) {
                conn = newConnection(protocol, endpoint, objectRef, pool);
            }
            return conn;
        } else {
            for (int pass = 1; pass <= 2; pass++) {
                int baseIndex;
                ArrayList servers;
                if (pass == 1) {
                    baseIndex = hostList.getPreferredIndex();
                    servers = hostList.getPreferredServers();
                } else {
                    baseIndex = hostList.getAlternateIndex();
                    servers = hostList.getAlternateServers();
                }
                int n = servers.size();
                for (int i = 0; i < n; i++) {
                    int tryIndex = (baseIndex + i) % n;
                    String host = (String) servers.get(tryIndex);
                    String hostPort = getEndpoint(protocol, host, objectRef);
                    if (hostPort == null) {
                        continue;
                    }
                    InstancePool pool = getInstancePool(protocol, hostPort);
                    Connection conn = (Connection) pool.get();
                    if (conn == null) {
                        conn = newConnection(protocol, hostPort, objectRef, pool);
                        hostList.countConnect();
                    }
                    return conn;
                }
            }
            // TODO: I18N
            throw new SystemException("CONNECT FAILED: host list = " + hostList);
        }
        */
    }

    public void put(Connection conn) {
        conn.getInstancePool().put(conn);
    }

    protected void init(ClientNamingContext namingContext) {
        this.namingContext = namingContext;
        poolMap = new HashMap();
    }

    /**
     * * Get the endpoint (host:port) for host, which is either a host name,
     * * IP address, or URL. If it is a URL, then return null if its protocol
     * * does not match expectations or if its port number suffix doesn't match
     * * the object reference. This method is used frequently, so given the
     * * above requirements should be as efficient as possible.
     */
    protected String getEndpoint(int protocol, String host, ObjectRef objectRef) {
        System.out.println("ConnectionPool.getEndpoint(): protocol: " + protocol + ", host: " + host + ", objectRef: " + objectRef);

        int ssPos = host.indexOf("://");
        if (ssPos != -1) {
            String scheme = Protocol.getScheme(protocol); // e.g. "iiop:"
            if (!host.startsWith(scheme)) {
                return null;
            }
            int portPos = host.lastIndexOf(':');
            if (portPos > ssPos) {
                int port = 0;
                int n = host.length();
                for (int i = portPos + 1; i < n; i++) {
                    char c = host.charAt(i);
                    if (c == ']') {
                        // Part of IP6 host name, no port number is present.
                        port = -1;
                        break;
                    }
                    port = 10 * port + (c - '0');
                }
                if (port != -1) {
                    if (port % 10 != objectRef.$getPort() % 10) {
                        // TODO: make this configurable via HostInfo?
                        return null;
                    }
                    return host.substring(ssPos + 3);
                }
            }
            StringBuffer hp = new StringBuffer(host.length()); // shouldn't require expansion in append
            hp.append(host.substring(ssPos + 3));
            hp.append(':');
            hp.append(objectRef.$getPort());
            return hp.toString();
        } else {
            StringBuffer hp = new StringBuffer(host.length() + 6); // shouldn't require expansion in append
            hp.append(host);
            hp.append(':');
            hp.append(objectRef.$getPort());
            return hp.toString();
        }
    }

    protected InstancePool getInstancePool(final int protocol, final String endpoint) {
        System.out.println("ConnectionPool.getInstancePool(): protocol: " + protocol + ", endpoint: " + endpoint);

        InstancePool pool = (InstancePool) poolMap.get(endpoint);
        if (pool == null) {
            synchronized (poolMap) {
                pool = (InstancePool) poolMap.get(endpoint);
                if (pool == null) {
                    String poolName = Protocol.getName(protocol) + "://" + endpoint;
                    pool = new InstancePool(poolName);
                    poolMap.put(endpoint, pool);
                }
            }
        }
        return pool;
    }

    protected Connection newConnection(int protocol, String endpoint, ObjectRef objectRef, InstancePool pool) {
        System.out.println("ConnectionPool.newConnection(): protocol: " + protocol + ", endpoint: " + endpoint + ", pool: " + pool);

        if (endpoint == null) {
            // See getEndpoint above, noting that it may return null.
            return null;
        }
        Connection conn;
        // Name Service connections use different pools from ordinary
        // connections. This is achieved using the "ns~" endpoint
        // prefix. Since this is not valid for host names, we must remove
        // it now.
        endpoint = StringUtil.removePrefix(endpoint, "ns~");
        switch (protocol) {
            case Protocol.IIOP:
                conn = iiopConnection(endpoint, objectRef);
                break;
            case Protocol.IIOPS:
                conn = iiopsConnection(endpoint, objectRef);
                break;
            case Protocol.HTTP:
                conn = httpConnection(endpoint, objectRef);
                break;
            case Protocol.HTTPS:
                conn = httpsConnection(endpoint, objectRef);
                break;
            default:
                throw new IllegalArgumentException("protocol = " + protocol);
        }
        conn.setInstancePool(pool);
        if (RmiTrace.CONNECT) {
            RmiTrace.traceConnect(Protocol.getName(protocol) + "://" + endpoint);
        }
        return conn;
    }

    protected Connection iiopConnection(String endpoint, ObjectRef objectRef) {
        System.out.println( "endpoint : " + endpoint );
        System.out.println( "objectRef : " + objectRef );
        System.out.println( "namingContext : " + namingContext );
        return Connection.getInstance(endpoint, objectRef, namingContext.getConnectionProperties());
    }

    protected Connection iiopsConnection(String endpoint, ObjectRef objectRef) {
        throw new SystemException("TODO");
    }

    protected Connection httpConnection(String endpoint, ObjectRef objectRef) {
        throw new SystemException("TODO");
    }

    protected Connection httpsConnection(String endpoint, ObjectRef objectRef) {
        throw new SystemException("TODO");
    }

    /*
    protected HostList resolve(String endpoint, ObjectRef objectRef) {

		//if (objectRef instanceof org.apache.geronimo.interop.rmi.iiop.NameService
		//	&& ! endpoint.startsWith("ns~mh~"))
		//{
		//	return null; // Avoid unbounded recursion
		//}

        HostList hostList = _namingContext.lookupHost(objectRef.$getHost()); // this uses a cache for good performance
        if (hostList != null
            && hostList.getPreferredServers().size() == 0
            && hostList.getAlternateServers().size() == 0) {
            // If the host list doesn't have any elements, we are better
            // off using the original endpoint string.
            hostList = null;
        }
        return hostList;
    }
    */
}
