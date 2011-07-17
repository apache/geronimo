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
import java.math.BigInteger;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.geronimo.crypto.encoders.Base64;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.WrappingLoginModule;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Login module for Spnego authentication in geronimo
 * @version $Rev$ $Date$
 */
public class SpnegoLoginModule implements LoginModule {

    public final static String TARGET_NAME = "targetName";

    public final static String LDAP_URL = "ldapUrl";

    public final static String LDAP_LOGIN_NAME = "ldapLoginName";

    public final static String LDAP_LOGIN_PASSWORD = "ldapLoginPassword";

    public final static String SEARCH_BASE = "searchBase";

    public final static String LDAP_CONTEXT_FACTORY = "ldapContextFactory";

    public final static List<String> supportedOptions = Collections.unmodifiableList(Arrays.asList(TARGET_NAME, LDAP_URL, LDAP_LOGIN_NAME, LDAP_LOGIN_PASSWORD, SEARCH_BASE, LDAP_CONTEXT_FACTORY));

    private String username;

    private boolean loginSucceeded;

    private GSSName srcName;

    private final Set<Principal> allPrincipals = new HashSet<Principal>();

    private Subject subject;

    private CallbackHandler callbackHandler;

    private String targetName;

    private String ldapUrl;

    private String ldapLoginName;

    private String ldapLoginPassword;

    private String searchBase;

    private String ldapContextFactory;

    private static Logger log = LoggerFactory.getLogger(SpnegoLoginModule.class);

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        try {
            this.subject = subject;
            this.callbackHandler = callbackHandler;
            for (Object option : options.keySet()) {
                if (!supportedOptions.contains(option) && !JaasLoginModuleUse.supportedOptions.contains(option) && !WrappingLoginModule.supportedOptions.contains(option)) {
                    log.warn("Ignoring option: " + option + ". Not supported.");
                }
            }
            this.targetName = (String) options.get(TARGET_NAME);
            this.ldapUrl = (String) options.get(LDAP_URL);
            this.ldapLoginName = (String) options.get(LDAP_LOGIN_NAME);
            this.ldapLoginPassword = (String) options.get(LDAP_LOGIN_PASSWORD);
            this.searchBase = (String) options.get(SEARCH_BASE);
            this.ldapContextFactory = (String) options.get(LDAP_CONTEXT_FACTORY);
            if (ldapContextFactory == null || ldapContextFactory.length() == 0) {
                ldapContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
            }
        } catch (Exception e) {
            log.error("Initialization failed", e);
            throw new IllegalArgumentException("Unable to configure Spnego login module: " + e.getMessage(), e);
        }
    }

    public boolean login() throws LoginException {
        loginSucceeded = false;
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new NameCallback("User name");
        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw (LoginException) new LoginException().initCause(ioe);
        } catch (UnsupportedCallbackException uce) {
            throw (LoginException) new LoginException().initCause(uce);
        }
        username = ((NameCallback) callbacks[0]).getName();
        if (username == null || username.equals("")) {
            username = null;
            throw new FailedLoginException();
        }
        byte[] token = Base64.decode(username);
        try {
            GSSManager manager = GSSManager.getInstance();
            Oid krb5Oid = new Oid("1.3.6.1.5.5.2");
            GSSName gssName = manager.createName(targetName, GSSName.NT_USER_NAME);
            GSSCredential serverCreds = manager.createCredential(gssName, GSSCredential.INDEFINITE_LIFETIME, krb5Oid, GSSCredential.ACCEPT_ONLY);
            GSSContext gContext = manager.createContext(serverCreds);
            if (gContext == null) {
                log.debug("Failed to create a GSSContext");
            } else {
                while (!gContext.isEstablished()) {
                    token = gContext.acceptSecContext(token, 0, token.length);
                }
                if (gContext.isEstablished()) {
                    loginSucceeded = true;
                    srcName = gContext.getSrcName();
                    log.debug("A security context is successfully established" + gContext);
                    return loginSucceeded;
                } else {
                    log.error("Failed to establish a security context");
                    throw new LoginException("Failed to establish a security context");
                }
            }
        } catch (GSSException e) {
            log.error(e.getMessage());
            throw (LoginException) new LoginException().initCause(e);
        }
        return loginSucceeded;
    }

    public boolean commit() throws LoginException {
        if (loginSucceeded) {
            if (srcName != null) {
                String at = "@";
                DirContext ctx = null;
                int indexOfAt = srcName.toString().indexOf(at);
                String userName = srcName.toString().substring(0, indexOfAt);
                SearchControls searchCtls = new SearchControls();
                String returnedAtts[] = { "primaryGroupID", "memberOf", "objectSid;binary" };
                String searchFilter = "(&(objectClass=user)(cn=" + userName + "))";
                String groupSearchFilter = null;
                int totalResults = 0;
                try {
                    ctx = getConnection();
                    if (ctx == null) {
                        log.info("Failed to get a directory context object");
                        throw new LoginException("Failed to get a directory context object");
                    }
                    searchCtls.setReturningAttributes(returnedAtts);
                    searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
                    // Search for objects using the filter
                    NamingEnumeration<SearchResult> answer = ctx.search(searchBase, searchFilter, searchCtls);
                    // Loop through the search results
                    while (answer.hasMoreElements()) {
                        SearchResult sr = answer.next();
                        totalResults++;
                        Attributes attrs = sr.getAttributes();
                        if (attrs != null) {
                            try {
                                byte[] userSid = (byte[]) attrs.get("objectSid;binary").get();
                                Integer primaryGroupId = new Integer((String) attrs.get("primaryGroupID").get());
                                byte[] groupRid = integerToFourBytes(primaryGroupId);
                                byte[] groupSid = userSid.clone();
                                // Replace the last four bytes to construct
                                // groupSid
                                for (int i = 0; i < 4; ++i) {
                                    groupSid[groupSid.length - 1 - i] = groupRid[i];
                                }
                                groupSearchFilter = "(&(objectSid=" + binaryToStringSID(groupSid) + "))";
                                Attribute answer1 = attrs.get("memberOf");
                                for (int i = 0; i < answer1.size(); i++) {
                                    String str = answer1.get(i).toString();
                                    String str1[] = str.split("CN=");
                                    allPrincipals.add(new GeronimoGroupPrincipal(str1[1].substring(0, str1[1].indexOf(","))));
                                }
                            } catch (NullPointerException e) {
                                throw new LoginException("Errors listing attributes: " + e);
                            }
                        }
                    }
                    // Search for objects using the group search filter
                    NamingEnumeration<SearchResult> answer2 = ctx.search(searchBase, groupSearchFilter, searchCtls);
                    // Loop through the search results
                    while (answer2.hasMoreElements()) {
                        SearchResult sr = answer2.next();
                        String str1[] = sr.getName().split("CN=");
                        allPrincipals.add(new GeronimoGroupPrincipal(str1[1].substring(0, str1[1].indexOf(","))));
                    }
                } catch (NamingException e) {
                    throw (LoginException) new LoginException().initCause(e);
                } finally {
                    if (ctx != null) {
                        try {
                            ctx.close();
                        } catch (Exception e) {
                        }
                    }
                }
                allPrincipals.add(new GeronimoUserPrincipal(srcName.toString()));
                subject.getPrincipals().addAll(allPrincipals);
            }
        }
        return loginSucceeded;
    }

    public boolean abort() throws LoginException {
        if (loginSucceeded) {
            // Clear out the private state
            username = null;
            allPrincipals.clear();
        }
        return loginSucceeded;
    }

    public boolean logout() throws LoginException {
        loginSucceeded = false;
        username = null;
        if (!subject.isReadOnly()) {
            // Remove principals added by this LoginModule
            subject.getPrincipals().removeAll(allPrincipals);
        }
        allPrincipals.clear();
        return true;
    }

    /*
     * Establishes a connection with the Ldap server
     */
    private DirContext getConnection() throws NamingException {
        Hashtable<String, String> env = new Hashtable<String, String>();
        DirContext ctx = null;
        env.put(Context.INITIAL_CONTEXT_FACTORY, ldapContextFactory);
        if (ldapLoginName != null && ldapLoginName.length() > 0) {
            env.put(Context.SECURITY_PRINCIPAL, ldapLoginName);
        }
        if (ldapLoginPassword != null && ldapLoginPassword.length() > 0) {
            env.put(Context.SECURITY_CREDENTIALS, ldapLoginPassword);
        }
        env.put(Context.PROVIDER_URL, ldapUrl);
        try {
            ctx = new InitialLdapContext(env, null);
        } catch (NamingException e) {
            throw new NamingException("Instantiation of Ldap Context failed");
        }
        return ctx;
    }

    /**
     * Converts a binary SID to a string
     */
    private static String binaryToStringSID(byte[] sidBytes) {
        StringBuilder sidString = new StringBuilder();
        sidString.append("S-");
        // Add SID revision
        sidString.append(Byte.toString(sidBytes[0]));
        // Next six bytes are issuing authority value
        sidString.append("-0x");
        sidString.append(new BigInteger(new byte[] { 127, sidBytes[6], sidBytes[5], sidBytes[4], sidBytes[3], sidBytes[2], sidBytes[1] }).toString(16).substring(2));
        // Next byte is the sub authority count including RID
        int saCount = sidBytes[7];
        // Get sub authority values as groups of 4 bytes
        for (int i = 0; i < saCount; ++i) {
            int idxAuth = 8 + i * 4;
            sidString.append("-0x");
            sidString.append(new BigInteger(new byte[] { 127, sidBytes[idxAuth + 3], sidBytes[idxAuth + 2], sidBytes[idxAuth + 1], sidBytes[idxAuth] }).toString(16).substring(2));
        }
        return sidString.toString();
    }

    /**
     * Convert an integer to four bytes
     */
    private static byte[] integerToFourBytes(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) ((i & 0xff000000) >>> 24);
        b[1] = (byte) ((i & 0x00ff0000) >>> 16);
        b[2] = (byte) ((i & 0x0000ff00) >>> 8);
        b[3] = (byte) ((i & 0x000000ff));
        return b;
    }
}
