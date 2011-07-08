/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.ldapmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpSession;

import org.apache.geronimo.console.util.Tree;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

import uk.ltd.getahead.dwr.WebContext;
import uk.ltd.getahead.dwr.WebContextFactory;

/**
 * The LDAP manager helper
 */
@RemoteProxy
public class LDAPManagerHelper {
    private final static String LDAP_VERSION_KEY = "java.naming.ldap.version";

    private final static String SSL_VALUE = "ssl";

    private final static String NONE_VALUE = "none";

    private final static String INITIAL_CONTEXT_FACTORY_DEFAULT = "com.sun.jndi.ldap.LdapCtxFactory";

    private final static String HOST_DEFAULT = "localhost";

    private final static String PORT_DEFAULT = "1389";

    private final static String BASE_DN_DEFAULT = "ou=system";

    // LDAP Version: "3", "2"
    private final static String LDAP_VERSION_DEFAULT = "3";

    // Security Protocol: "simple", "ssl", "sasl"
    private final static String SECURITY_PROTOCOL_DEFAULT = "simple";

    // Security Authentication: "simple", "none", "strong"
    private final static String SECURITY_AUTHENTICATION_DEFAULT = "simple";

    private final static String SECURITY_PRINCIPAL_DEFAULT = "uid=admin, ou=system";

    private final static String SECURITY_CREDENTIALS_DEFAULT = "secret";

    private final static String ONELEVEL_SCOPE = "onelevel";

    private final static String SUBTREE_SCOPE = "subtree";

    private final static String DIR_CONTEXT_KEY = "LDAPManagerHelper.dirContext";

    private final static String DIR_ENV_KEY = "LDAPManagerHelper.dirEnv";

    private final static String HOST_KEY = "LDAPManagerHelper.host";

    private final static String PORT_KEY = "LDAPManagerHelper.port";

    private final static String BASE_DN_KEY = "LDAPManagerHelper.baseDN";

    private final static String SUCCESS_RESULT = "<SUCCESS>";

    private DirContext dirContext;

    private Hashtable dirEnv;

    private String host;

    private String port;

    private String baseDN;

    /**
     * Construct an LDAP manager helper using config data (default)
     */
    public LDAPManagerHelper() throws Exception {
        dirContext = (DirContext) getSessionAttribute(DIR_CONTEXT_KEY);
        if (dirContext != null) {
            dirEnv = (Hashtable) getSessionAttribute(DIR_ENV_KEY);
            host = (String) getSessionAttribute(HOST_KEY);
            port = (String) getSessionAttribute(PORT_KEY);
            baseDN = (String) getSessionAttribute(BASE_DN_KEY);
        }
    }

    /**
     * Construct an LDAP manager helper using config data (partial)
     */
    public LDAPManagerHelper(String host, String port, String baseDN,
            String securityAuthentication, String userDN, String userPwd)
            throws Exception {
        connect(INITIAL_CONTEXT_FACTORY_DEFAULT, host, port, baseDN,
                LDAP_VERSION_DEFAULT, SECURITY_PROTOCOL_DEFAULT,
                securityAuthentication, userDN, userPwd);
    }

    /**
     * Construct an LDAP manager helper using config data (all)
     */
    public LDAPManagerHelper(String initialContextFactory, String host,
            String port, String baseDN, String ldapVersion,
            String securityProtocol, String securityAuthentication,
            String securityPrincipal, String securityCredentials)
            throws Exception {
        connect(initialContextFactory, host, port, baseDN, ldapVersion,
                securityProtocol, securityAuthentication, securityPrincipal,
                securityCredentials);
    }

    /**
     * Create a directory context using config data
     */
    @RemoteMethod
    public synchronized String connect(String initialContextFactory,
            String host, String port, String baseDN, String ldapVersion,
            String securityProtocol, String securityAuthentication,
            String securityPrincipal, String securityCredentials)
            throws Exception {
        String result = SUCCESS_RESULT;

        Hashtable dirEnv = new Hashtable();
        dirEnv.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
        String providerURL = createLDAPURL(host, port, ""); // Empty Base DN
        dirEnv.put(Context.PROVIDER_URL, providerURL);
        dirEnv.put(LDAP_VERSION_KEY, ldapVersion);
        if (SSL_VALUE.equalsIgnoreCase(securityProtocol)) {
            dirEnv.put(Context.SECURITY_PROTOCOL, SSL_VALUE);
        }
        dirEnv.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);
        if (!(NONE_VALUE.equalsIgnoreCase(securityAuthentication))) {
            // Either "simple" or "strong"
            dirEnv.put(Context.SECURITY_PRINCIPAL, securityPrincipal); // User DN
            dirEnv.put(Context.SECURITY_CREDENTIALS, securityCredentials); // Password
        }

        try {
            DirContext newDirContext = new InitialDirContext(dirEnv);
            // Close old context
            if (dirContext != null) {
                dirContext.close();
            }
            // Save directory data to class vars
            this.dirContext = newDirContext;
            this.dirEnv = dirEnv;
            this.host = host;
            this.port = port;
            this.baseDN = baseDN;
            // Save directory data to session
            setSessionAttribute(DIR_CONTEXT_KEY, dirContext);
            setSessionAttribute(DIR_ENV_KEY, dirEnv);
            setSessionAttribute(HOST_KEY, host);
            setSessionAttribute(PORT_KEY, port);
            setSessionAttribute(BASE_DN_KEY, baseDN);
        } catch (NamingException e) {
            result = "Problem connecting to directory server: "
                    + e.getMessage();
        }

        return result;
    }

    /**
     * Return directory context environment
     */
    @RemoteMethod
    public Map getEnvironment() {
        Map env = new HashMap();
        env.put("host", host);
        env.put("port", port);
        String ldapVersion = (String) dirEnv.get(LDAP_VERSION_KEY);
        env.put("ldapVersion", ldapVersion);
        env.put("baseDN", baseDN);
        String securityProtocol = (String) dirEnv
                .get(Context.SECURITY_PROTOCOL);
        env.put("securityProtocol", securityProtocol);
        String securityAuthentication = (String) dirEnv
                .get(Context.SECURITY_AUTHENTICATION);
        env.put("securityAuthentication", securityAuthentication);
        String securityPrincipal = (String) dirEnv
                .get(Context.SECURITY_PRINCIPAL);
        env.put("securityPrincipal", securityPrincipal);

        return env;
    }

    /**
     * Returns the names bound in the named context
     */
    @RemoteMethod
    public Collection<String[]> list(String name) throws Exception {
        Collection<String[]> result = new ArrayList<String[]>();

        if (dirContext == null) {
            return result;
        }

        try {
            NamingEnumeration list = dirContext.list(name); // can't be ""

            while (list.hasMore()) {
                NameClassPair ncp = (NameClassPair) list.next();
                String childName = ncp.getName();
                String dn = childName + ", " + name;
                String[] pair = { childName, dn };
                result.add(pair);
            }
        } catch (NamingException e) {
            throw new Exception("Problem getting directory list: "
                    + e.getMessage());
        }

        return result;
    }

    /**
     * Returns the names bound in the base DN context
     */
    public Collection<String[]> listBaseDN() throws Exception {
        return list(baseDN);
    }

    /**
     * Enumerates the names bound in the named context and return result as JSON
     */
    public String listJSON(String name) throws Exception {
        return listJSON(name, null);
    }

    /**
     * Enumerates the names bound in the named context and return result as JSON
     */
    public String listJSON(String name, String commonFields) throws Exception {
        // JSON: [{title:"Title1",isFolder:true}, {title:"Title2"}]

        StringBuilder json = new StringBuilder();
        List list = (List) list(name);

        json.append('[');
        int size = list.size();
        for (int i = 0; i < size; i++) {
            String[] entry = (String[]) list.get(i);
            json.append("{title:\"");
            json.append(entry[0]);
            json.append("\",widgetId:\"");
            json.append(entry[1]);
            json.append("\"");
            if (commonFields != null) { // TODO: Do additional testing
                json.append(commonFields);
            }
            json.append("}");
            if ((i + 1) < size) {
                json.append(',');
            }
        }
        json.append("]");

        return json.toString();
    }

    /**
     * Return the attributes of an LDAP entry
     */
    @RemoteMethod
    public Collection getAttributes(String name) throws Exception {
        ArrayList result = new ArrayList();
        
        if (dirContext == null) {
            return result;
        }

        try {
            Attributes attribs = dirContext.getAttributes(name);
            NamingEnumeration attributes = attribs.getAll();
            while (attributes.hasMore()) {
                Attribute attribute = (Attribute) attributes.next();
                String id = attribute.getID();
                NamingEnumeration values = attribute.getAll();
                while (values.hasMore()) {
                    String value = values.next().toString();
                    String[] pair = { id, value };
                    result.add(pair);
                }
            }
        } catch (NamingException e) {
            throw new Exception("Problem retrieving attributes: "
                    + e.getMessage());
        }
        return result;
    }

    /**
     * Execute an LDAP search
     */
    @RemoteMethod
    public Collection search(String searchDN, String filter, String searchScope)
            throws Exception {
        ArrayList result = new ArrayList();

        if (dirContext == null) {
            return result;
        }

        try {
            String ldapURL = createLDAPURL(host, port, searchDN);
            SearchControls sc = new SearchControls();
            if (ONELEVEL_SCOPE.equalsIgnoreCase(searchScope)) {
                sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            } else if (SUBTREE_SCOPE.equalsIgnoreCase(searchScope)) {
                sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
            } else {
                // Default to one level scope
                sc.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            }
            // Filter: "(objectclass=*)"
            boolean isSearchDNAdded = false;
            NamingEnumeration ne = dirContext.search(ldapURL, filter, sc);
            while (ne.hasMore()) {
                SearchResult sr = (SearchResult) ne.next();
                String name = sr.getName();
                String dn = null;
                if (name.length() > 0) {
                    dn = name + "," + searchDN;
                    result.add(dn);
                } else if ((name.length() == 0) && !isSearchDNAdded) {
                    dn = searchDN;
                    result.add(dn);
                    isSearchDNAdded = true;
                }
            }
        } catch (NamingException e) {
            throw new Exception("Problem performing directory search: "
                    + e.getMessage());
        }
        return result;
    }

    /**
     * Close directory context
     */
    public void close() throws Exception {
        if (dirContext != null) {
            try {
                dirContext.close();
                dirContext = null;
            } catch (NamingException e) {
                throw new Exception("Problem closing directory context: "
                                    + e.getMessage());
            }
        }
    }

    /**
     * Return base DN of this directory context
     */
    @RemoteMethod
    public String getBaseDN() {
        return baseDN;
    }

    /**
     * Create an LDAP url using host, port, and base DN
     */
    private String createLDAPURL(String host, String port, String baseDN) {
        StringBuilder url = new StringBuilder();
        url.append("ldap://");
        url.append(host);
        url.append(':');
        url.append(port);
        if ((baseDN != null) && (baseDN.length() >= 3)) {
            if (!baseDN.startsWith("/")) {
                url.append('/');
            }
            url.append(baseDN);
        }
        return url.toString();
    }

    /**
     * Get the HTTP session
     */
    private HttpSession getSession() {
        WebContext ctx = WebContextFactory.get();
        HttpSession session = ctx.getSession();
        return session;
    }

    /**
     * Set an HTTP session attribute
     */
    private void setSessionAttribute(String name, Object value) {
        getSession().setAttribute(name, value);
    }

    /**
     * Get an HTTP session attribute
     */
    private Object getSessionAttribute(String name) {
        return getSession().getAttribute(name);
    }

    /**
     * Dump HTTP session attributes
     */
    private void dumpSession() {
        System.out.println("--- dumpSession()");
        WebContext ctx = WebContextFactory.get();
        HttpSession session = ctx.getSession();
        Enumeration attribNames = session.getAttributeNames();
        while (attribNames.hasMoreElements()) {
            String attribName = (String) attribNames.nextElement();
            System.out.print("--- session: " + attribName + " = ");
            Object attribValue = session.getAttribute(attribName);
            System.out.println(attribValue);
        }
    }

    /**
     * Dump search enumeration
     */
    private void printSearchEnumeration(NamingEnumeration ne) {
        try {
            while (ne.hasMore()) {
                SearchResult sr = (SearchResult) ne.next();
                System.out.println("-->" + sr.getName());
                System.out.println(sr.getAttributes());
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    
}
