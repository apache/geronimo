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
import java.util.Hashtable;
import java.util.Random;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;

import org.apache.geronimo.interop.properties.IntProperty;
import org.apache.geronimo.interop.properties.PropertyMap;
import org.apache.geronimo.interop.properties.StringProperty;
import org.apache.geronimo.interop.properties.SystemProperties;
import org.apache.geronimo.interop.rmi.iiop.ObjectRef;
import org.apache.geronimo.interop.rmi.iiop.compiler.StubFactory;
import org.apache.geronimo.interop.util.ExceptionUtil;


public class ClientNamingContext implements Context, java.io.Serializable {
    public static ClientNamingContext getInstance(Hashtable env) {
        ClientNamingContext nc = (ClientNamingContext) _contextMap.get(env);
        if (nc == null) {
            synchronized (_contextMap) {
                nc = (ClientNamingContext) _contextMap.get(env);
                if (nc == null) {
                    nc = new ClientNamingContext();
                    nc.init(env);
                    _contextMap.put(env, nc);
                }
            }
        }
        return nc;
    }

    // -----------------------------------------------------------------------
    // properties
    // -----------------------------------------------------------------------

    public static final StringProperty usernameProperty =
            new StringProperty(SystemProperties.class, "java.naming.security.principal");

    public static final StringProperty passwordProperty =
            new StringProperty(SystemProperties.class, "java.naming.security.credentials");

    public static final IntProperty idleConnectionTimeoutProperty =
            new IntProperty(SystemProperties.class, "org.apache.geronimo.interop.rmi.idleConnectionTimeout")
            .defaultValue(60); // seconds

    // -----------------------------------------------------------------------
    // private data
    // -----------------------------------------------------------------------

    private static int _idleConnectionTimeout =
            idleConnectionTimeoutProperty.getInt();

    private static int _namingContextCacheTimeout =
            SystemProperties.rmiNamingContextCacheTimeoutProperty.getInt();

    private static HashMap _contextMap = new HashMap();

    private static HashMap _hostListCache = new HashMap();

    private static HashMap _multiHostMap = new HashMap();

    private static Random _random = new Random();

    private HashMap _cache = new HashMap();

    private Hashtable _env;

    private ConnectionPool _connectionPool;

    private PropertyMap _connectionProperties;

    private org.apache.geronimo.interop.CosNaming.NamingContext _serverNamingContext;
    static private HashMap _nameMap = new HashMap();


    private String _prefix;

    private String _username;

    private String _password;

    // -----------------------------------------------------------------------
    // public methods
    // -----------------------------------------------------------------------

    public ConnectionPool getConnectionPool() {
        return _connectionPool;
    }

    public PropertyMap getConnectionProperties() {
        return _connectionProperties;
    }

    public int getIdleConnectionTimeout() {
        return _idleConnectionTimeout;
    }

    public String getUsername() {
        return _username;
    }

    public String getPassword() {
        return _password;
    }

    // -----------------------------------------------------------------------
    // public methods from interface javax.naming.Context
    // -----------------------------------------------------------------------

    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    public Object lookup(String name) throws NamingException {
        if (name.startsWith("java:comp/env/")) {
            name = name.substring(14);
        }

        String newName = (String) _nameMap.get(name);
        if (newName != null) {
            name = newName;
        }

        NameBinding nb = (NameBinding) _cache.get(name);
        if (nb == null) {
            synchronized (_cache) {
                nb = (NameBinding) _cache.get(name);
                if (nb != null && nb.hasExpired()) {
                    _cache.remove(name);
                    nb = null;
                }
                if (nb == null) {
                    nb = resolve(name);
                    _cache.put(name, nb);
                }
            }
        }
        return nb.object;
    }

    public HostList lookupHost(String name) {
        NameBinding nb = (NameBinding) _hostListCache.get(name);
        if (nb == null) {
            synchronized (_hostListCache) {
                nb = (NameBinding) _hostListCache.get(name);
                if (nb != null && nb.hasExpired()) {
                    _hostListCache.remove(name);
                    nb = null;
                }
                if (nb == null) {
                    nb = resolve_host(name);
                    _hostListCache.put(name, nb);
                }
            }
        }
        return (HostList) nb.object;
    }

    public static void bind(String bindName, String name) throws NamingException {
        _nameMap.put(bindName, name);
    }

    public void bind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void bind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rebind(Name name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rebind(String name, Object obj) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void unbind(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void unbind(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rename(Name oldName, Name newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void rename(String oldName, String newName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NamingEnumeration list(Name name) throws NamingException {
        // TODO: support this
        throw new OperationNotSupportedException();
    }

    public NamingEnumeration list(String name) throws NamingException {
        // TODO: support this
        throw new OperationNotSupportedException();
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        // TODO: support this
        throw new OperationNotSupportedException();
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
        // TODO: support this
        throw new OperationNotSupportedException();
    }

    public void destroySubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void destroySubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Context createSubcontext(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Context createSubcontext(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Object lookupLink(Name name) throws NamingException {
        // TODO: support this
        throw new OperationNotSupportedException();
    }

    public Object lookupLink(String name) throws NamingException {
        // TODO: support this
        throw new OperationNotSupportedException();
    }

    public NameParser getNameParser(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        // TODO: support this
        throw new OperationNotSupportedException();
    }

    public String composeName(String name, String prefix) throws NamingException {
        // TODO: support this
        throw new OperationNotSupportedException();
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Object removeFromEnvironment(String propName) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Hashtable getEnvironment() throws NamingException {
        throw new OperationNotSupportedException();
    }

    public String getNameInNamespace() throws NamingException {
        throw new OperationNotSupportedException();
    }

    public void close() throws NamingException {
        throw new OperationNotSupportedException();
    }

    // -----------------------------------------------------------------------
    // protected methods
    // -----------------------------------------------------------------------

    protected void init(Hashtable env) {
        _env = env;
        Object urlObject = env.get(Context.PROVIDER_URL);
        if (urlObject == null) {
            System.out.println("ClientNamingContext.init(): TODO: urlObject was null, create one based on the current hostname.");
            urlObject = SystemProperties.getInstance().getProperty("java.naming.provider.url",
                                                                   "iiop://" + "delafran-t30" + ":2000");
        }
        String url = urlObject.toString();
        UrlInfo urlInfo = UrlInfo.getInstance(url);
        _serverNamingContext = (org.apache.geronimo.interop.CosNaming.NamingContext)
                StubFactory.getInstance().getStub(org.apache.geronimo.interop.CosNaming.NamingContext.class);
        ObjectRef ncRef = (ObjectRef) _serverNamingContext;
        ncRef.$setNamingContext(this);
        ncRef.$setProtocol(urlInfo.getProtocol());
        ncRef.$setHost("ns~" + urlInfo.getHost());
        ncRef.$setPort(urlInfo.getPort());
        ncRef.$setObjectKey(urlInfo.getObjectKey());
        _connectionProperties = urlInfo.getProperties();
        _connectionPool = ConnectionPool.getInstance(this);
        Object u = env.get(Context.SECURITY_PRINCIPAL);
        Object p = env.get(Context.SECURITY_CREDENTIALS);
        if (u == null) {
            u = usernameProperty.getString();
        }
        if (p == null) {
            p = passwordProperty.getString();
        }
        _username = u != null ? u.toString() : null;
        _password = p != null ? p.toString() : null;

        /*
        if (_serverNamingContext._is_a("IDL:org.apache.geronimo.interop/rmi/iiop/NameService:1.0"))
        {
            _serverNamingContext = (org.apache.geronimo.interop.rmi.iiop.NameService)
                StubFactory.getInstance().getStub(org.apache.geronimo.interop.rmi.iiop.NameService.class);
            ncRef = (ObjectRef)_serverNamingContext;
            ncRef.$setNamingContext(this);
            ncRef.$setProtocol(urlInfo.getProtocol());
            ncRef.$setHost("ns~" + urlInfo.getHost());
            ncRef.$setPort(urlInfo.getPort());
            ncRef.$setObjectKey(urlInfo.getObjectKey());
        }
        */
    }

    protected NameBinding resolve(String name) throws NamingException {
        Object value = org.apache.geronimo.interop.naming.NameService.getInitialContext().lookupReturnNullIfNotFound(name);
        if (value != null) {
            NameBinding nb = new NameBinding();
            nb.object = value;
            nb.cacheTimeout = System.currentTimeMillis() + _namingContextCacheTimeout;
            return nb;
        }
        try {
            org.apache.geronimo.interop.CosNaming.NameComponent[] resolveName =
                    {new org.apache.geronimo.interop.CosNaming.NameComponent(name, "")};
            org.omg.CORBA.Object object = _serverNamingContext.resolve(resolveName);
            NameBinding nb = new NameBinding();
            nb.object = object;
            nb.cacheTimeout = System.currentTimeMillis() + _namingContextCacheTimeout;
            return nb;
        } catch (org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound notFound) {
            throw new NameNotFoundException(name);
        } catch (Exception ex) {
            throw new javax.naming.NamingException(ExceptionUtil.getStackTrace(ex));
        }
    }

    protected NameBinding resolve_host(String host) {
        HostList list = null;
        if (_serverNamingContext instanceof org.apache.geronimo.interop.rmi.iiop.NameService) {
            org.apache.geronimo.interop.rmi.iiop.NameService nameService = (org.apache.geronimo.interop.rmi.iiop.NameService) _serverNamingContext;
            list = new HostList(nameService.resolve_host(host));
        }
        NameBinding nb = new NameBinding();
        nb.object = list;
        //nb.cacheTimeout = System.currentTimeMillis()
        //   + (list != null ? list.getCacheTimeout() : _namingContextCacheTimeout);
        return nb;
    }
}
