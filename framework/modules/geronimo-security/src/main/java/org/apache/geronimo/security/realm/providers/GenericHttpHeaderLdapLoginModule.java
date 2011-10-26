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
 
package org.apache.geronimo.security.realm.providers;

import java.io.IOException;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;

public class GenericHttpHeaderLdapLoginModule extends GenericHttpHeaderLoginmodule implements LoginModule {

    private static Log log = LogFactory.getLog(GenericHttpHeaderLdapLoginModule.class);

    private final static String HEADER_NAMES = "headerNames";
    private final static String AUTHENTICATION_AUTHORITY = "authenticationAuthority";
    private static final String INITIAL_CONTEXT_FACTORY = "initialContextFactory";
    private static final String CONNECTION_URL = "connectionURL";
    private static final String CONNECTION_USERNAME = "connectionUsername";
    private static final String CONNECTION_PASSWORD = "connectionPassword";
    private static final String CONNECTION_PROTOCOL = "connectionProtocol";
    private static final String AUTHENTICATION = "authentication";
    private static final String USER_BASE = "userBase";
    private static final String USER_SEARCH_MATCHING = "userSearchMatching";
    private static final String USER_SEARCH_SUBTREE = "userSearchSubtree";
    private static final String ROLE_BASE = "roleBase";
    private static final String ROLE_NAME = "roleName";
    private static final String ROLE_SEARCH_MATCHING = "roleSearchMatching";
    private static final String ROLE_SEARCH_SUBTREE = "roleSearchSubtree";
    private static final String USER_ROLE_NAME = "userRoleName";
    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(
            INITIAL_CONTEXT_FACTORY, CONNECTION_URL, CONNECTION_USERNAME, CONNECTION_PASSWORD, HEADER_NAMES,
            AUTHENTICATION_AUTHORITY, CONNECTION_PROTOCOL, AUTHENTICATION, USER_BASE, USER_SEARCH_MATCHING,
            USER_SEARCH_SUBTREE, ROLE_BASE, ROLE_NAME, ROLE_SEARCH_MATCHING, ROLE_SEARCH_SUBTREE, USER_ROLE_NAME));

    private String initialContextFactory;
    private String connectionURL;
    private String connectionProtocol;
    private String connectionUsername;
    private String connectionPassword;
    private String authentication;
    private String userBase;
    private String roleBase;
    private String roleName;
    private String userRoleName;

    protected DirContext context = null;

    private MessageFormat userSearchMatchingFormat;
    private MessageFormat roleSearchMatchingFormat;

    private boolean userSearchSubtreeBool = false;
    private boolean roleSearchSubtreeBool = false;

    private boolean loginSucceeded;
    private final Set<String> groups = new HashSet<String>();
    private final Set<Principal> allPrincipals = new HashSet<Principal>();

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        for (Object option : options.keySet()) {
            if (!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option)
                    && !WrappingLoginModule.supportedOptions.contains(option)) {
                log.warn("Ignoring option: " + option + ". Not supported.");
            }
        }
        headerNames = (String) options.get(HEADER_NAMES);
        authenticationAuthority = (String) options.get(AUTHENTICATION_AUTHORITY);
        initialContextFactory = (String) options.get(INITIAL_CONTEXT_FACTORY);
        connectionURL = (String) options.get(CONNECTION_URL);
        connectionUsername = (String) options.get(CONNECTION_USERNAME);
        connectionPassword = (String) options.get(CONNECTION_PASSWORD);
        connectionProtocol = (String) options.get(CONNECTION_PROTOCOL);
        authentication = (String) options.get(AUTHENTICATION);
        userBase = (String) options.get(USER_BASE);
        String userSearchMatching = (String) options.get(USER_SEARCH_MATCHING);
        String userSearchSubtree = (String) options.get(USER_SEARCH_SUBTREE);
        roleBase = (String) options.get(ROLE_BASE);
        roleName = (String) options.get(ROLE_NAME);
        String roleSearchMatching = (String) options.get(ROLE_SEARCH_MATCHING);
        String roleSearchSubtree = (String) options.get(ROLE_SEARCH_SUBTREE);
        userRoleName = (String) options.get(USER_ROLE_NAME);
        userSearchMatchingFormat = new MessageFormat(userSearchMatching);
        roleSearchMatchingFormat = new MessageFormat(roleSearchMatching);
        userSearchSubtreeBool = Boolean.valueOf(userSearchSubtree);
        roleSearchSubtreeBool = Boolean.valueOf(roleSearchSubtree);
    }

    /**
     * This LoginModule is not to be ignored. So, this method should never return false.
     * 
     * @return true if authentication succeeds, or throw a LoginException such as FailedLoginException if authentication
     *         fails
     */
    public boolean login() throws LoginException {
        Map<String, String> headerMap = null;
        loginSucceeded = false;
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new RequestCallback();
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        httpRequest = ((RequestCallback) callbacks[0]).getRequest();
        String[] headers = headerNames.split(",");
        try {
            headerMap = matchHeaders(httpRequest, headers);
        } catch (HeaderMismatchException e) {
            throw (LoginException) new LoginException("Header Mistmatch error").initCause(e);
        }

        if (headerMap.isEmpty()) {
            throw new FailedLoginException();
        }

        if (authenticationAuthority.equalsIgnoreCase("Siteminder")) {
            HeaderHandler headerHandler = new SiteminderHeaderHandler();
            username = headerHandler.getUser(headerMap);
        } else if (authenticationAuthority.equalsIgnoreCase("Datapower")) {
            /* To be Done */
        }
        if (username == null || username.equals("")) {
            username = null;
            throw new FailedLoginException();
        }

        try {
            boolean result = authenticate(username);
            if (!result) {
                throw new FailedLoginException();
            }
        } catch (LoginException e) {
            // Clear out the private state
            username = null;
            groups.clear();
            throw e;
        } catch (Exception e) {
            // Clear out the private state
            username = null;
            groups.clear();
            throw (LoginException) new LoginException("LDAP Error").initCause(e);
        }

        loginSucceeded = true;
        return loginSucceeded;
    }

    /*
     * @exception LoginException if login succeeded but commit failed.
     * 
     * @return true if login succeeded and commit succeeded, or false if login failed but commit succeeded.
     */
    public boolean commit() throws LoginException {
        if (loginSucceeded) {
            for (String group : groups) {
                allPrincipals.add(new GeronimoGroupPrincipal(group));
            }
            subject.getPrincipals().addAll(allPrincipals);
        }

        // Clear out the private state
        username = null;
        groups.clear();

        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if (loginSucceeded) {
            // Clear out the private state
            username = null;
            groups.clear();
            allPrincipals.clear();
        }
        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        // Clear out the private state
        loginSucceeded = false;
        username = null;
        groups.clear();
        if (!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
        return true;
    }

    protected void close(DirContext context) {
        try {
            context.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

    protected boolean authenticate(String username) throws Exception {
        DirContext context = open();
        try {

            String filter = userSearchMatchingFormat.format(new String[] { username });
            SearchControls constraints = new SearchControls();
            if (userSearchSubtreeBool) {
                constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            } else {
                constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
            }

            // setup attributes
            String[] attribs;
            if (userRoleName == null) {
                attribs = new String[] {};
            } else {
                attribs = new String[] { userRoleName };
            }
            constraints.setReturningAttributes(attribs);

            NamingEnumeration results = context.search(userBase, filter, constraints);

            if (results == null || !results.hasMore()) {
                log.error("No roles associated with user " + username);
                loginSucceeded = false;
                throw new FailedLoginException();
            }

            SearchResult result = (SearchResult) results.next();

            if (results.hasMore()) {
                // ignore for now
            }
            NameParser parser = context.getNameParser("");
            Name contextName = parser.parse(context.getNameInNamespace());
            Name baseName = parser.parse(userBase);
            Name entryName = parser.parse(result.getName());
            Name name = contextName.addAll(baseName);
            name = name.addAll(entryName);
            String dn = name.toString();

            Attributes attrs = result.getAttributes();
            if (attrs == null) {
                return false;
            }
            ArrayList<String> roles = null;
            if (userRoleName != null) {
                roles = addAttributeValues(userRoleName, attrs, roles);
            }
            // check the credentials by binding to server
            // bindUser(context, dn);
            // if authenticated add more roles
            roles = getRoles(context, dn, username, roles);
            for (String role : roles) {
                groups.add(role);
            }
            if (groups.isEmpty()) {
                log.error("No roles associated with user " + username);
                loginSucceeded = false;
                throw new FailedLoginException();
            } else
                loginSucceeded = true;

        } catch (CommunicationException e) {
            close(context);
            throw (LoginException) new FailedLoginException().initCause(e);
        } catch (NamingException e) {
            close(context);
            throw (LoginException) new FailedLoginException().initCause(e);
        }
        return true;
    }

    protected ArrayList<String> getRoles(DirContext context, String dn, String username, ArrayList<String> list)
            throws NamingException {
        if (list == null) {
            list = new ArrayList<String>();
        }
        if (roleName == null || "".equals(roleName)) {
            return list;
        }
        String filter = roleSearchMatchingFormat.format(new String[] { doRFC2254Encoding(dn), username });

        SearchControls constraints = new SearchControls();
        if (roleSearchSubtreeBool) {
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
        } else {
            constraints.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        }
        NamingEnumeration results = context.search(roleBase, filter, constraints);
        while (results.hasMore()) {
            SearchResult result = (SearchResult) results.next();
            Attributes attrs = result.getAttributes();
            if (attrs == null) {
                continue;
            }
            list = addAttributeValues(roleName, attrs, list);
        }
        return list;
    }

    protected String doRFC2254Encoding(String inputString) {
        StringBuilder buf = new StringBuilder(inputString.length());
        for (int i = 0; i < inputString.length(); i++) {
            char c = inputString.charAt(i);
            switch (c) {
            case '\\':
                buf.append("\\5c");
                break;
            case '*':
                buf.append("\\2a");
                break;
            case '(':
                buf.append("\\28");
                break;
            case ')':
                buf.append("\\29");
                break;
            case '\0':
                buf.append("\\00");
                break;
            default:
                buf.append(c);
                break;
            }
        }
        return buf.toString();
    }

    protected void bindUser(DirContext context, String dn) throws NamingException, FailedLoginException {

        context.addToEnvironment(Context.SECURITY_PRINCIPAL, dn);
        try {
            context.getAttributes("", null);
        } catch (AuthenticationException e) {
            log.debug("Authentication failed for dn=" + dn);
            throw new FailedLoginException();
        } finally {

            if (connectionUsername != null) {
                context.addToEnvironment(Context.SECURITY_PRINCIPAL, connectionUsername);
            } else {
                context.removeFromEnvironment(Context.SECURITY_PRINCIPAL);
            }

            if (connectionPassword != null) {
                context.addToEnvironment(Context.SECURITY_CREDENTIALS, connectionPassword);
            } else {
                context.removeFromEnvironment(Context.SECURITY_CREDENTIALS);
            }
        }
    }

    private ArrayList<String> addAttributeValues(String attrId, Attributes attrs, ArrayList<String> values)
            throws NamingException {

        if (attrId == null || attrs == null) {
            return values;
        }
        if (values == null) {
            values = new ArrayList<String>();
        }
        Attribute attr = attrs.get(attrId);
        if (attr == null) {
            return (values);
        }
        NamingEnumeration e = attr.getAll();
        while (e.hasMore()) {
            String value = (String) e.next();
            values.add(value);
        }
        return values;
    }

    protected DirContext open() throws NamingException {
        if (context != null) {
            return context;
        }
        try {
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);
            if (connectionUsername != null || !"".equals(connectionUsername)) {
                env.put(Context.SECURITY_PRINCIPAL, connectionUsername);
            }
            if (connectionPassword != null || !"".equals(connectionPassword)) {
                env.put(Context.SECURITY_CREDENTIALS, connectionPassword);
            }
            env.put(Context.SECURITY_PROTOCOL, connectionProtocol == null ? "" : connectionProtocol);
            env.put(Context.PROVIDER_URL, connectionURL == null ? "" : connectionURL);
            env.put(Context.SECURITY_AUTHENTICATION, authentication == null ? "" : authentication);
            context = new InitialDirContext(env);

        } catch (NamingException e) {
            log.error(e);
            throw e;
        }
        return context;
    }
}
