/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package javax.mail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.WeakHashMap;


/**
 * OK, so we have a final class in the API with a heck of a lot of implementation required...
 * let's try and figure out what it is meant to do.
 * <p/>
 * It is supposed to collect together properties and defaults so that they can be
 * shared by multiple applications on a desktop; with process isolation and no
 * real concept of shared memory, this seems challenging. These properties and
 * defaults rely on system properties, making management in a app server harder,
 * and on resources loaded from "mail.jar" which may lead to skew between
 * differnet independent implementations of this API.
 *
 * @version $Rev$ $Date$
 */
public final class Session {
    private static final Class[] PARAM_TYPES = {Session.class, URLName.class};
    private static final Map addressMap = new HashMap();
    private static Session DEFAULT_SESSION;

    private Map passwordAuthentications = new HashMap();

    private final Properties properties;
    private final Authenticator authenticator;
    private boolean debug;
    private PrintStream debugOut = System.out;

    private static final WeakHashMap providersByClassLoader = new WeakHashMap();

    /**
     * No public constrcutor allowed.
     */
    private Session(Properties properties, Authenticator authenticator) {
        this.properties = properties;
        this.authenticator = authenticator;
        debug = Boolean.valueOf(properties.getProperty("mail.debug")).booleanValue();
    }

    /**
     * Create a new session initialized with the supplied properties which uses the supplied authenticator.
     * Clients should ensure the properties listed in Appendix A of the JavaMail specification are
     * set as the defaults are unlikey to work in most scenarios; particular attention should be given
     * to:
     * <ul>
     * <li>mail.store.protocol</li>
     * <li>mail.transport.protocol</li>
     * <li>mail.host</li>
     * <li>mail.user</li>
     * <li>mail.from</li>
     * </ul>
     *
     * @param properties    the session properties
     * @param authenticator an authenticator for callbacks to the user
     * @return a new session
     */
    public static Session getInstance(Properties properties, Authenticator authenticator) {
        return new Session(new Properties(properties), authenticator);
    }

    /**
     * Create a new session initialized with the supplied properties with no authenticator.
     *
     * @param properties the session properties
     * @return a new session
     * @see #getInstance(java.util.Properties, Authenticator)
     */
    public static Session getInstance(Properties properties) {
        return getInstance(properties, null);
    }

    /**
     * Get the "default" instance assuming no authenticator is required.
     *
     * @param properties the session properties
     * @return if "default" session
     * @throws SecurityException if the does not have permission to access the default session
     */
    public synchronized static Session getDefaultInstance(Properties properties) {
        return getDefaultInstance(properties, null);
    }

    /**
     * Get the "default" session.
     * If there is not current "default", a new Session is created and installed as the default.
     *
     * @param properties
     * @param authenticator
     * @return if "default" session
     * @throws SecurityException if the does not have permission to access the default session
     */
    public synchronized static Session getDefaultInstance(Properties properties, Authenticator authenticator) {
        if (DEFAULT_SESSION == null) {
            DEFAULT_SESSION = getInstance(properties, authenticator);
        } else {
            if (authenticator != DEFAULT_SESSION.authenticator) {
                if (authenticator == null || DEFAULT_SESSION.authenticator == null || authenticator.getClass().getClassLoader() != DEFAULT_SESSION.authenticator.getClass().getClassLoader()) {
                    throw new SecurityException();
                }
            }
            // todo we should check with the SecurityManager here as well
        }
        return DEFAULT_SESSION;
    }

    /**
     * Enable debugging for this session.
     * Debugging can also be enabled by setting the "mail.debug" property to true when
     * the session is being created.
     *
     * @param debug the debug setting
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Get the debug setting for this session.
     *
     * @return the debug setting
     */
    public boolean getDebug() {
        return debug;
    }

    /**
     * Set the output stream where debug information should be sent.
     * If set to null, System.out will be used.
     *
     * @param out the stream to write debug information to
     */
    public void setDebugOut(PrintStream out) {
        debugOut = out == null ? System.out : out;
    }

    /**
     * Return the debug output stream.
     *
     * @return the debug output stream
     */
    public PrintStream getDebugOut() {
        return debugOut;
    }

    /**
     * Return the list of providers available to this application.
     * This method searches for providers that are defined in the javamail.providers
     * and javamail.default.providers resources available through the current context
     * classloader, or if that is not available, the classloader that loaded this class.
     * <p/>
     * As searching for providers is potentially expensive, this implementation maintains
     * a WeakHashMap of providers indexed by ClassLoader.
     *
     * @return an array of providers
     */
    public Provider[] getProviders() {
        ProviderInfo info = getProviderInfo();
        return (Provider[]) info.all.toArray(new Provider[info.all.size()]);
    }

    /**
     * Return the provider for a specific protocol.
     * This implementation initially looks in the Session properties for an property with the name
     * "mail.<protocol>.class"; if found it attempts to create an instance of the class named in that
     * property throwing a NoSuchProviderException if the class cannot be loaded.
     * If this property is not found, it searches the providers returned by {@link #getProviders()}
     * for a entry for the specified protocol.
     *
     * @param protocol the protocol to get a provider for
     * @return a provider for that protocol
     * @throws NoSuchProviderException
     */
    public Provider getProvider(String protocol) throws NoSuchProviderException {
        ProviderInfo info = getProviderInfo();
        Provider provider;
        String providerName = properties.getProperty("nail." + protocol + ".class");
        if (providerName != null) {
            provider = (Provider) info.byClassName.get(providerName);
        } else {
            provider = (Provider) info.byProtocol.get(protocol);
        }
        if (provider == null) {
            throw new NoSuchProviderException("Unable to locate provider for protocol: " + protocol);
        }
        return provider;
    }

    /**
     * Make the supplied Provider the default for its protocol.
     *
     * @param provider the new default Provider
     * @throws NoSuchProviderException
     */
    public void setProvider(Provider provider) throws NoSuchProviderException {
        ProviderInfo info = getProviderInfo();
        info.byProtocol.put(provider.getProtocol(), provider);
    }

    /**
     * Return a Store for the default protocol defined by the mail.store.protocol property.
     *
     * @return the store for the default protocol
     * @throws NoSuchProviderException
     */
    public Store getStore() throws NoSuchProviderException {
        String protocol = properties.getProperty("mail.store.protocol");
        if (protocol == null) {
            throw new NoSuchProviderException("mail.store.protocol property is not set");
        }
        return getStore(protocol);
    }

    /**
     * Return a Store for the specified protocol.
     *
     * @param protocol the protocol to get a Store for
     * @return a Store
     * @throws NoSuchProviderException if no provider is defined for the specified protocol
     */
    public Store getStore(String protocol) throws NoSuchProviderException {
        Provider provider = getProvider(protocol);
        return getStore(provider);
    }

    /**
     * Return a Store for the protocol specified in the given URL
     *
     * @param url the URL of the Store
     * @return a Store
     * @throws NoSuchProviderException if no provider is defined for the specified protocol
     */
    public Store getStore(URLName url) throws NoSuchProviderException {
        return (Store) getService(getProvider(url.getProtocol()), url);
    }

    /**
     * Return the Store specified by the given provider.
     *
     * @param provider the provider to create from
     * @return a Store
     * @throws NoSuchProviderException if there was a problem creating the Store
     */
    public Store getStore(Provider provider) throws NoSuchProviderException {
        if (Provider.Type.STORE != provider.getType()) {
            throw new NoSuchProviderException("Not a Store Provider: " + provider);
        }
        return (Store) getService(provider, null);
    }

    /**
     * Return a closed folder for the supplied URLName, or null if it cannot be obtained.
     * <p/>
     * The scheme portion of the URL is used to locate the Provider and create the Store;
     * the returned Store is then used to obtain the folder.
     *
     * @param name the location of the folder
     * @return the requested folder, or null if it is unavailable
     * @throws NoSuchProviderException if there is no provider
     * @throws MessagingException      if there was a problem accessing the Store
     */
    public Folder getFolder(URLName name) throws MessagingException {
        Store store = getStore(name);
        return store.getFolder(name);
    }

    /**
     * Return a Transport for the default protocol specified by the
     * <code>mail.transport.protocol</code> property.
     *
     * @return a Transport
     * @throws NoSuchProviderException
     */
    public Transport getTransport() throws NoSuchProviderException {
        String protocol = properties.getProperty("mail.transport.protocol");
        if (protocol == null) {
            throw new NoSuchProviderException("mail.transport.protocol property is not set");
        }
        return getTransport(protocol);
    }

    /**
     * Return a Transport for the specified protocol.
     *
     * @param protocol the protocol to use
     * @return a Transport
     * @throws NoSuchProviderException
     */
    public Transport getTransport(String protocol) throws NoSuchProviderException {
        Provider provider = getProvider(protocol);
        return getTransport(provider);
    }

    /**
     * Return a transport for the protocol specified in the URL.
     *
     * @param name the URL whose scheme specifies the protocol
     * @return a Transport
     * @throws NoSuchProviderException
     */
    public Transport getTransport(URLName name) throws NoSuchProviderException {
        return (Transport) getService(getProvider(name.getProtocol()), name);
    }

    /**
     * Return a transport for the protocol associated with the type of this address.
     *
     * @param address the address we are trying to deliver to
     * @return a Transport
     * @throws NoSuchProviderException
     */
    public Transport getTransport(Address address) throws NoSuchProviderException {
        String type = address.getType();
        return getTransport((String) addressMap.get(type));
    }

    /**
     * Return the Transport specified by a Provider
     *
     * @param provider the defining Provider
     * @return a Transport
     * @throws NoSuchProviderException
     */
    public Transport getTransport(Provider provider) throws NoSuchProviderException {
        return (Transport) getService(provider, null);
    }

    /**
     * Set the password authentication associated with a URL.
     *
     * @param name          the url
     * @param authenticator the authenticator
     */
    public void setPasswordAuthentication(URLName name, PasswordAuthentication authenticator) {
        if (authenticator == null) {
            passwordAuthentications.remove(name);
        } else {
            passwordAuthentications.put(name, authenticator);
        }
    }

    /**
     * Get the password authentication associated with a URL
     *
     * @param name the URL
     * @return any authenticator for that url, or null if none
     */
    public PasswordAuthentication getPasswordAuthentication(URLName name) {
        return (PasswordAuthentication) passwordAuthentications.get(name);
    }

    /**
     * Call back to the application supplied authenticator to get the needed username add password.
     *
     * @param host            the host we are trying to connect to, may be null
     * @param port            the port on that host
     * @param protocol        the protocol trying to be used
     * @param prompt          a String to show as part of the prompt, may be null
     * @param defaultUserName the default username, may be null
     * @return the authentication information collected by the authenticator; may be null
     */
    public PasswordAuthentication requestPasswordAuthentication(InetAddress host, int port, String protocol, String prompt, String defaultUserName) {
        if (authenticator == null) {
            return null;
        }
        return authenticator.authenticate(host, port, protocol, prompt, defaultUserName);
    }

    /**
     * Return the properties object for this Session; this is a live collection.
     *
     * @return the properties for the Session
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Return the specified property.
     *
     * @param property the property to get
     * @return its value, or null if not present
     */
    public String getProperty(String property) {
        return getProperties().getProperty(property);
    }

    private Service getService(Provider provider, URLName name) throws NoSuchProviderException {
        try {
            ClassLoader cl = getClassLoader();
            Class clazz = cl.loadClass(provider.getClassName());
            Constructor ctr = clazz.getConstructor(PARAM_TYPES);
            return (Service) ctr.newInstance(new Object[]{this, name});
        } catch (ClassNotFoundException e) {
            throw (NoSuchProviderException) new NoSuchProviderException("Unable to load class for provider: " + provider).initCause(e);
        } catch (NoSuchMethodException e) {
            throw (NoSuchProviderException) new NoSuchProviderException("Provider class does not have a constructor(Session, URLName): " + provider).initCause(e);
        } catch (InstantiationException e) {
            throw (NoSuchProviderException) new NoSuchProviderException("Unable to instantiate provider class: " + provider).initCause(e);
        } catch (IllegalAccessException e) {
            throw (NoSuchProviderException) new NoSuchProviderException("Unable to instantiate provider class: " + provider).initCause(e);
        } catch (InvocationTargetException e) {
            throw (NoSuchProviderException) new NoSuchProviderException("Exception from constructor of provider class: " + provider).initCause(e.getCause());
        }
    }

    private static ProviderInfo getProviderInfo() {
        ClassLoader cl = getClassLoader();
        ProviderInfo info = (ProviderInfo) providersByClassLoader.get(cl);
        if (info == null) {
            info = loadProviders(cl);
        }
        return info;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = Session.class.getClassLoader();
        }
        return cl;
    }

    private static ProviderInfo loadProviders(ClassLoader cl) {
        ProviderInfo info = new ProviderInfo();
        try {
            File file = new File(System.getProperty("java.home"), "lib/javamail.providers");
            InputStream is = new FileInputStream(file);
            try {
                loadProviders(info, is);
            } finally{
                is.close();
            }
        } catch (SecurityException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        try {
            Enumeration e = cl.getResources("/META-INF/javamail.providers");
            while (e.hasMoreElements()) {
                URL url = (URL) e.nextElement();
                InputStream is = url.openStream();
                try {
                    loadProviders(info, is);
                } finally{
                    is.close();
                }
            }
        } catch (SecurityException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        try {
            Enumeration e = cl.getResources("/META-INF/javamail.default.providers");
            while (e.hasMoreElements()) {
                URL url = (URL) e.nextElement();
                InputStream is = url.openStream();
                try {
                    loadProviders(info, is);
                } finally{
                    is.close();
                }
            }
        } catch (SecurityException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }
        return info;
    }

    private static void loadProviders(ProviderInfo info, InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            StringTokenizer tok = new StringTokenizer(line, ";");
            String protocol = null;
            Provider.Type type = null;
            String className = null;
            String vendor = null;
            String version = null;
            while (tok.hasMoreTokens()) {
                String property = tok.nextToken();
                int index = property.indexOf('=');
                if (index == -1) {
                    continue;
                }
                String key = property.substring(0, index).trim();
                String value = property.substring(index+1).trim();
                if (protocol == null && "protocol".equals(key)) {
                    protocol = value;
                } else if (type == null && "type".equals(key)) {
                    if ("store".equals(value)) {
                        type = Provider.Type.STORE;
                    } else if ("transport".equals(value)) {
                        type = Provider.Type.TRANSPORT;
                    }
                } else if (className == null && "class".equals(key)) {
                    className = value;
                } else if ("vendor".equals(key)) {
                    vendor = value;
                } else if ("version".equals(key)) {
                    version = value;
                }
            }
            if (protocol == null || type == null || className == null) {
                //todo should we log a warning?
                continue;
            }
            Provider provider = new Provider(protocol, className, type, vendor, version);
            if (!info.byClassName.containsKey(className)) {
                info.byClassName.put(className, provider);
            }
            if (!info.byProtocol.containsKey(protocol)) {
                info.byProtocol.put(protocol, provider);
            }
            info.all.add(provider);
        }
    }

    private static class ProviderInfo {
        private final Map byClassName = new HashMap();
        private final Map byProtocol = new HashMap();
        private final List all = new ArrayList();
    }
}
