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

import java.util.*;
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

public class ClientNamingContext implements Context, java.io.Serializable 
{

    public static ClientNamingContext getInstance(Hashtable env)
    {
        ClientNamingContext nc = (ClientNamingContext) contextMap.get(env);
        if (nc == null)
        {
            synchronized (contextMap)
            {
                nc = (ClientNamingContext) contextMap.get(env);
                if (nc == null) 
                {
                    nc = new ClientNamingContext();
                    nc.init(env);
                    contextMap.put(env, nc);
                }
            }
        }
        return nc;
    }


    public static final IntProperty idleConnectionTimeoutProperty =
        new IntProperty(SystemProperties.class, "idleConnectionTimeout")
        .defaultValue(60); // 60 seconds

    public static final IntProperty lookupCacheTimeoutProperty =
        new IntProperty(SystemProperties.class, "lookupCacheTimeout")
        .defaultValue(600); // 10 minutes

    public static final StringProperty usernameSystemProperty =
        new StringProperty(SystemProperties.class, "java.naming.security.principal");

    public static final StringProperty passwordSystemProperty =
        new StringProperty(SystemProperties.class, "java.naming.security.credentials");

    private static long     idleConnectionTimeout;

    private static long     lookupCacheTimeout;

    private static int      socketTimeout;

    private static HashMap  contextMap = new HashMap();

    private static HashMap  hostListCache = new HashMap();

//    private ArrayList       requestKeys;

    private HashMap         cache = new HashMap();

    private Hashtable       env;

    private ConnectionPool  connectionPool;

    private PropertyMap     connectionProperties;

    static private HashMap  nameMap = new HashMap();

    private String          username;

    private String          password;

    private String          namePrefix;

    private org.apache.geronimo.interop.CosNaming.NamingContext serverNamingContext;

    public ConnectionPool getConnectionPool() {
        return connectionPool;
    }

    public PropertyMap getConnectionProperties()
    {
        return connectionProperties;
    }

    public long getIdleConnectionTimeout()
    {
        return idleConnectionTimeout;
    }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
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

        String newName = (String) nameMap.get(name);
        if (newName != null) {
            name = newName;
        }

        NameBinding nb = (NameBinding) cache.get(name);
        if (nb == null) {
            synchronized (cache) {
                nb = (NameBinding) cache.get(name);
                if (nb != null && nb.hasExpired()) {
                    cache.remove(name);
                    nb = null;
                }
                if (nb == null) {
                    nb = resolve(name);
                    cache.put(name, nb);
                }
            }
        }
        return nb.object;
    }

    public HostList lookupHost(String name) {
        NameBinding nb = (NameBinding) hostListCache.get(name);
        if (nb == null) {
            synchronized (hostListCache) {
                nb = (NameBinding) hostListCache.get(name);
                if (nb != null && nb.hasExpired()) {
                    hostListCache.remove(name);
                    nb = null;
                }
                if (nb == null) {                    
                    hostListCache.put(name, nb);
                }
            }
        }
        return (HostList) nb.object;
    }

    public static void bind(String bindName, String name) throws NamingException {
        nameMap.put(bindName, name);
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
        throw new OperationNotSupportedException();
    }

    public NamingEnumeration list(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NamingEnumeration listBindings(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NamingEnumeration listBindings(String name) throws NamingException {
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
        throw new OperationNotSupportedException();
    }

    public Object lookupLink(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NameParser getNameParser(Name name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public NameParser getNameParser(String name) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public Name composeName(Name name, Name prefix) throws NamingException {
        throw new OperationNotSupportedException();
    }

    public String composeName(String name, String prefix) throws NamingException {
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

    protected void init(Hashtable env) 
    {
        this.env = env;
        Object urlObject = env.get(Context.PROVIDER_URL);
        if (urlObject == null) {
            urlObject = SystemProperties.getInstance().getProperty("java.naming.provider.url",
                                                                   "iiop://" + "delafran-t30" + ":2000");
        }
        String url = urlObject.toString();
        UrlInfo urlInfo = UrlInfo.getInstance(url);
        serverNamingContext = (org.apache.geronimo.interop.CosNaming.NamingContext)
                StubFactory.getInstance().getStub(org.apache.geronimo.interop.CosNaming.NamingContext.class);

        namePrefix = urlInfo.getNamePrefix();

        ObjectRef ncRef = (ObjectRef) serverNamingContext;
        ncRef.$setNamingContext(this);
        ncRef.$setProtocol(urlInfo.getProtocol());
        ncRef.$setHost("ns~" + urlInfo.getHost());
        ncRef.$setPort(urlInfo.getPort());
        ncRef.$setObjectKey(urlInfo.getObjectKey());
        connectionPool = ConnectionPool.getInstance(this);
        Object u = env.get(Context.SECURITY_PRINCIPAL);
        Object p = env.get(Context.SECURITY_CREDENTIALS);
        if (u == null)
        {
            u = usernameSystemProperty.getString();
        }
        if (p == null)
        {
            p = passwordSystemProperty.getString();
        }
        username = u != null ? u.toString() : null;
        password = p != null ? p.toString() : null;

        PropertyMap props = urlInfo.getProperties();
        props.putAll(env);
        PropertyMap copyProps = new PropertyMap();
        copyProps.putAll(props);
        for (Iterator i = copyProps.entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry)i.next();
            String property = (String)entry.getKey();
            Object value = entry.getValue();

            String startsWith = "org.apache.geronimo.interop.rmi.";
            if (property.startsWith(startsWith))
            {
                int replace = startsWith.length();
                props.remove(property);
                props.put(property.substring(replace), value);
            }
        }
        for (Iterator i = SystemProperties.getInstance().entrySet().iterator(); i.hasNext();)
        {
            Map.Entry entry = (Map.Entry)i.next();
            String property = (String)entry.getKey();
            Object value = entry.getValue();
            if (property.startsWith("djc."))
            {
                props.put(property.substring(4), value);
            }
        }
        connectionProperties = props;
        idleConnectionTimeout = 1000 * idleConnectionTimeoutProperty.getInt(url, props);
        lookupCacheTimeout = 1000 * lookupCacheTimeoutProperty.getInt(url, props);
    }

    protected NameBinding resolve(String name) throws NamingException
    {
        Object value = org.apache.geronimo.interop.naming.NameService.getInitialContext().lookupReturnNullIfNotFound(name);
        if (value != null)
        {
            NameBinding nb = new NameBinding();
            nb.object = value;
            nb.cacheTimeout = System.currentTimeMillis() + lookupCacheTimeout;
            return nb;
        }
        try
        {
            org.apache.geronimo.interop.CosNaming.NameComponent[] resolveName =
                { new org.apache.geronimo.interop.CosNaming.NameComponent(namePrefix + name, "") };
            org.omg.CORBA.Object object = serverNamingContext.resolve(resolveName);
            NameBinding nb = new NameBinding();
            nb.object = object;
            nb.cacheTimeout = System.currentTimeMillis() + lookupCacheTimeout;
            return nb;
        }
        catch (org.apache.geronimo.interop.CosNaming.NamingContextPackage.NotFound notFound)
        {
            throw new NameNotFoundException(name);
        }
        catch (Exception ex)
        {
            throw new javax.naming.NamingException(ExceptionUtil.getStackTrace(ex));
        }
    }
}
