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

import javax.mail.Provider.Type;
import javax.mail.internet.ParameterList;
import javax.mail.internet.ParseException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * @version $Rev$ $Date$
 */
public final class Session {

    private static final Provider[] PROVIDER_ARRAY = new Provider[0];
    private static final Class[] PARAM_TYPES = {Session.class, URLName.class};
    private static final Map addressMap = new HashMap();
    private static final Map providers = new HashMap();
    private static Authenticator DEFAULT_AUTHENTICATOR;
    private static Session DEFAULT_SESSION;

    private Authenticator authenticator;
    private boolean debug;
    private PrintStream debugOut;
    private Map passwordAuthentications = new HashMap();
    private Properties properties = new Properties();


    private Session() {
    }

    public synchronized static Session getDefaultInstance(Properties properties) {
        if (DEFAULT_SESSION == null) {
            DEFAULT_SESSION = getInstance(properties);
        }
        return DEFAULT_SESSION;
    }

    public synchronized static Session getDefaultInstance(Properties properties, Authenticator authenticator) {
        if (DEFAULT_AUTHENTICATOR == null
                || DEFAULT_AUTHENTICATOR == authenticator
                || DEFAULT_AUTHENTICATOR.getClass().getClassLoader() == authenticator.getClass().getClassLoader()) {
            if (DEFAULT_SESSION == null) {
                DEFAULT_SESSION = getInstance(properties, authenticator);
                DEFAULT_AUTHENTICATOR = authenticator;
            }
            return DEFAULT_SESSION;
        } else {
            throw new SecurityException("Cannot access default instance with given authenticator");
        }
    }

    public static Session getInstance(Properties properties) {
        return getInstance(properties, null);
    }

    public static Session getInstance(Properties properties, Authenticator authenticator) {
        Session session = new Session();
        session.authenticator = authenticator;
        session.properties = new Properties(properties);
        session.debug = Boolean.getBoolean(properties.getProperty("mail.debug", "false"));

        return session;
    }


    public boolean getDebug() {
        return debug;
    }

    public PrintStream getDebugOut() {
        return System.err;
    }

    public Folder getFolder(URLName name) throws MessagingException {
        // TODO Implement
        return null;
    }

    public PasswordAuthentication getPasswordAuthentication(URLName name) {
        return (PasswordAuthentication) passwordAuthentications.get(name);
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String property) {
        return getProperties().getProperty(property);
    }

    public Provider getProvider(String protocol) throws NoSuchProviderException {
        // TODO Implement
        // Lookup from 
        return (Provider) providers.get(new ProviderKey(Type.STORE, protocol));
    }

    public Provider[] getProviders() {
        return (Provider[]) providers.values().toArray(PROVIDER_ARRAY);
    }

    public Store getStore() throws NoSuchProviderException {
        return getStore(properties.getProperty("mail.store.protocol"));
    }

    public Store getStore(Provider provider) throws NoSuchProviderException {
        try {
            Class clazz = Class.forName(provider.getClassName());
            Constructor constructor = clazz.getConstructor(PARAM_TYPES);
            return (Store) constructor.newInstance(new Object[]{this, null});
        } catch (Exception e) {
            throw new NoSuchProviderException(e.toString());
        }
    }

    public Store getStore(String protocol) throws NoSuchProviderException {
        if (protocol == null) {
            throw new NoSuchProviderException("No protocol specified in mail.store.protocol property or none given");
        }
        Provider provider = (Provider) providers.get(new ProviderKey(Type.STORE, protocol));
        if (provider == null) {
            throw new NoSuchProviderException("Unknown protocol for " + protocol);
        }
        return getStore(provider);
    }

    public Store getStore(URLName url) throws NoSuchProviderException {
        return getStore(url.getProtocol());
    }

    public Transport getTransport() throws NoSuchProviderException {
        return getTransport(properties.getProperty("mail.transport.protocol"));
    }

    public Transport getTransport(Address address) throws NoSuchProviderException {
        String type = address.getType();
        // type is 'rfc822' -> 'smtp'
        // type is 'news' -> 'nntp'
        return getTransport((String) addressMap.get(type));
    }

    public Transport getTransport(Provider provider) throws NoSuchProviderException {
        try {
            Class clazz = Class.forName(provider.getClassName());
            Constructor constructor = clazz.getConstructor(PARAM_TYPES);
            return (Transport) constructor.newInstance(new Object[]{this, null});
        } catch (Exception e) {
            throw new NoSuchProviderException(e.toString());
        }
    }

    public Transport getTransport(String protocol) throws NoSuchProviderException {
        if (protocol == null) {
            throw new NoSuchProviderException("No protocol specified in mail.store.protocol property or none given");
        }
        Provider provider = (Provider) providers.get(new ProviderKey(Type.TRANSPORT, protocol));
        if (provider == null) {
            throw new NoSuchProviderException("Unknown protocol for " + protocol);
        }
        return getTransport(provider);
    }

    public Transport getTransport(URLName name) throws NoSuchProviderException {
        return getTransport(name.getProtocol());
    }

    public PasswordAuthentication requestPasswordAuthentication(InetAddress host, int port,
                                                                String protocol,
                                                                String prompt,
                                                                String defaultUserName) {
        // TODO Implement this, probably by showing a dialog box of some sorts?
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setDebugOut(PrintStream out) {
        debugOut = out;
    }

    public void setPasswordAuthentication(URLName name, PasswordAuthentication authenticator) {
        passwordAuthentications.put(name, authenticator);
    }

    public void setProvider(Provider provider) throws NoSuchProviderException {
        providers.put(new ProviderKey(provider.getType(), provider.getProtocol()), provider);
    }

    // Read in the files and configure the Providers
    static {
        loadProviders();
        loadAddressMap();
    }

    private static void loadAddressMap() {
        try {
            String slash = System.getProperty("file.separator");
            String home = System.getProperty("java.home");
            loadAddressMap(new FileInputStream(home + slash + "lib" + slash + "javamail.address.map"));
        } catch (RuntimeException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (IOException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        }

        try {
            // Note: this is a class resouce, which always uses /
            loadAddressMap("/META-INF/javamail.address.map");
        } catch (RuntimeException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (IOException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        }

        try {
            // Note: this is a class resouce, which always uses /
            loadAddressMap("/META-INF/javamail.default.address.map");
        } catch (RuntimeException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (IOException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        }
    }

    private static void loadAddressMap(InputStream in) throws IOException {
        if (in == null) {
            return;
        }
        ;

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().equals("") || line.startsWith("#")) {
                continue; // skip blank lines and comments
            }
            int eq = line.indexOf("=");
            String type = line.substring(0, eq).trim();
            String transport = line.substring(eq + 1).trim();
            addressMap.put(type, transport);
        }
        br.close();
    }

    private static void loadAddressMap(String file) throws IOException {
        loadAddressMap(Session.class.getResourceAsStream(file));
    }

    private static void loadProviders() {
        try {
            String slash = System.getProperty("file.separator");
            String home = System.getProperty("java.home");
            loadProviders(new FileInputStream(home + slash + "lib" + slash + "javamail.providers"));
        } catch (RuntimeException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (IOException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            // Note: this is a class resouce, which always uses /
            loadProviders("/META-INF/javamail.providers");
        } catch (RuntimeException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (IOException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            loadProviders("/META-INF/javamail.default.providers");
        } catch (RuntimeException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (IOException e) {
            // continue; we are trying to load a non-existent file; doesn't matter if we get security/IO exceptions
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void loadProviders(InputStream in) throws IOException, ParseException {

        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().equals("") || line.startsWith("#")) {
                continue; // skip blank lines and comments
            }
            ParameterList pl = new ParameterList(line);
            pl.get(line);
            // TODO Continue implementing
            String protocol = pl.get("protocol");
            String className = pl.get("class");
            String typeString = pl.get("type");
            Type type = Provider.Type.getType(typeString);
            String vendor = pl.get("vendor");
            String version = pl.get("version");

            Provider provider = new Provider(protocol, className, type, vendor, version);

            providers.put(new ProviderKey(type, protocol), provider);
        }
        br.close();
    }

    private static void loadProviders(String file) throws ParseException, IOException {
        loadProviders(Session.class.getResourceAsStream(file));
    }

    private static final class ProviderKey {

        private final Type type;
        private final String protocol;

        ProviderKey(Type type, String protocol) {
            this.type = type;
            this.protocol = protocol;
    }

        public int hashCode() {
            return type.hashCode() ^ protocol.hashCode();
    }

        public boolean equals(Object obj) {
            if (!(obj instanceof ProviderKey)) return false;
            ProviderKey key = (ProviderKey) obj;
            return type == key.type & protocol.equals(key.protocol);
    }
    }
}
