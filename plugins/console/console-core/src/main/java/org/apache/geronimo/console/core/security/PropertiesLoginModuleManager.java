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

package org.apache.geronimo.console.core.security;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.crypto.EncryptionManager;
import org.apache.geronimo.crypto.encoders.Base64;
import org.apache.geronimo.crypto.encoders.HexTranslator;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.security.SecurityNames;
import org.apache.geronimo.security.jaas.LoginModuleSettings;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public class PropertiesLoginModuleManager implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(PropertiesLoginModuleManager.class);

    private ServerInfo serverInfo;

    private SingleElementCollection<LoginModuleSettings> loginModule;

    private Properties users = new Properties();

    private Properties groups = new Properties();

    private static final String usersKey = "usersURI";

    private static final String groupsKey = "groupsURI";

    private static final String digestKey = "digest";

    private final static String encodingKey = "encoding";    
    
    public PropertiesLoginModuleManager(ServerInfo serverInfo, Collection<LoginModuleSettings> loginModule) {
        this.serverInfo = serverInfo;
        this.loginModule = new SingleElementCollection<LoginModuleSettings>(loginModule);
    }

    public boolean isAvailable() {
        return loginModule.getElement() != null;
    }

    private void refreshUsers() throws GeronimoSecurityException {
        users.clear();
        InputStream in = null;
        try {
            in = serverInfo.resolveServer(getUsersURI()).toURL().openStream();
            users.load(in);
        } catch (Exception e) {
            throw new GeronimoSecurityException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }

    private void refreshGroups() throws GeronimoSecurityException {
        groups.clear();
        InputStream in = null;
        try {
            in = serverInfo.resolveServer(getGroupsURI()).toURL().openStream();
            groups.load(in);
        } catch (Exception e) {
            throw new GeronimoSecurityException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }

    private void clearAll() {
        users.clear();
        groups.clear();
    }

    public void refreshAll() throws GeronimoSecurityException {
        refreshGroups();
        refreshUsers();
    }

    public String[] getUsers() throws GeronimoSecurityException {
        refreshUsers();
        return (String[]) users.keySet().toArray(new String[0]);
    }

    public String[] getGroups() throws GeronimoSecurityException {
        refreshGroups();
        return (String[]) groups.keySet().toArray(new String[0]);
    }

    public void addUserPrincipal(Hashtable<String, String> properties)
            throws GeronimoSecurityException {

        refreshUsers();
        String name = (String) properties.get("UserName");
        if (users.getProperty(name) != null) {
            log.warn("addUserPrincipal() UserName="+name+" already exists.");
            throw new GeronimoSecurityException("User principal="+name+" already exists.");
        }
        try {
            String realPassword = (String) properties.get("Password");
            if (realPassword != null) {
                String digest = getDigest();
                if(digest != null && !digest.equals("")) {
                    realPassword = digestPassword(realPassword, digest, getEncoding());
                }
                realPassword = EncryptionManager.encrypt(realPassword);
            }
            users.setProperty(name, realPassword);
            store(users, serverInfo.resolveServer(getUsersURI()).toURL());
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot add user principal: "
                    + e.getMessage(), e);
        }
    }

    public void removeUserPrincipal(String userPrincipal)
            throws GeronimoSecurityException {
        refreshUsers();
        try {
            users.remove(userPrincipal);
            store(users, serverInfo.resolveServer(getUsersURI()).toURL());
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot remove user principal "
                    + userPrincipal + ": " + e.getMessage(), e);
        }
    }

    public void updateUserPrincipal(Hashtable<String, String> properties)
            throws GeronimoSecurityException {
        refreshUsers();
        String name = (String) properties.get("UserName");
        if (users.getProperty(name) == null) {
            log.warn("updateUserPrincipal() UserName="+name+" does not exist.");
            throw new GeronimoSecurityException("User principal="+name+" does not exist.");
        }
        try {
            String realPassword = (String) properties.get("Password");
            if (realPassword != null) {
                String digest = getDigest();
                if(digest != null && !digest.equals("")) {
                    realPassword = digestPassword(realPassword, digest, getEncoding());
                }
                realPassword = EncryptionManager.encrypt(realPassword);
            }
            users.setProperty(name, realPassword);
            store(users, serverInfo.resolveServer(getUsersURI()).toURL());
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot update user principal: "
                    + e.getMessage(), e);
        }
    }

    public void addGroupPrincipal(Hashtable<String, String> properties)
            throws GeronimoSecurityException {
        refreshGroups();
        String group = (String) properties.get("GroupName");
        if (groups.getProperty(group) != null) {
            log.warn("addGroupPrincipal() GroupName="+group+" already exists.");
            throw new GeronimoSecurityException("Group principal="+group+" already exists.");
        }
        try {
            groups.setProperty(group, (String) properties.get("Members"));
            store(groups, serverInfo.resolveServer(getGroupsURI()).toURL());
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot add group principal: "
                    + e.getMessage(), e);
        }
    }

    public void removeGroupPrincipal(String groupPrincipal)
            throws GeronimoSecurityException {
        refreshGroups();
        try {
            groups.remove(groupPrincipal);
            store(groups, serverInfo.resolveServer(getGroupsURI()).toURL());
        } catch (Exception e) {
            throw new GeronimoSecurityException(
                    "Cannot remove group principal: " + e.getMessage(), e);
        }
    }

    public void updateGroupPrincipal(Hashtable<String, String> properties)
            throws GeronimoSecurityException {
        //same as add group principal
        refreshGroups();
        String group = (String) properties.get("GroupName");
        if (groups.getProperty(group) == null) {
            log.warn("updateGroupPrincipal() GroupName="+group+" does not exist.");
            throw new GeronimoSecurityException("Group principal="+group+" does not exist.");
        }
        try {
            groups.setProperty(group, (String) properties.get("Members"));
            store(groups, serverInfo.resolveServer(getGroupsURI()).toURL());
        } catch (Exception e) {
            throw new GeronimoSecurityException("Cannot update group principal: "
                    + e.getMessage(), e);
        }
    }

    public void addToGroup(String userPrincipal, String groupPrincipal)
            throws GeronimoSecurityException {
        throw new GeronimoSecurityException(
                "Not implemented for properties file security realm...");
    }

    public void removeFromGroup(String userPrincipal, String groupPrincipal)
            throws GeronimoSecurityException {
        throw new GeronimoSecurityException(
                "Not implemented for properties file security realm...");
    }

    public String getPassword(String userPrincipal)
            throws GeronimoSecurityException {
        refreshUsers();
        if (users.getProperty(userPrincipal) == null) {
            log.warn("getPassword() User="+userPrincipal+" does not exist.");
            throw new GeronimoSecurityException("User principal="+userPrincipal+" does not exist.");
        }
        String realPassword = users.getProperty(userPrincipal);
        if (realPassword != null) {
            realPassword = (String) EncryptionManager.decrypt(realPassword);
        }
        return realPassword;
    }

    public Set<String> getGroupMembers(String groupPrincipal)
            throws GeronimoSecurityException {
        Set<String> memberSet = new HashSet<String>();
        // return nothing when the groupPrincipal is null or empty
        if (groupPrincipal == null || groupPrincipal.equals("")) {
            return memberSet;
        }
        refreshGroups();
        if (groups.getProperty(groupPrincipal) == null) {
            log.warn("getGroupMembers() Group="+groupPrincipal+" does not exist.");
            return memberSet;
        }
        String[] members = ((String)groups.getProperty(groupPrincipal)).split(",");

        memberSet.addAll(Arrays.asList(members));
        return memberSet;
    }

    private String getUsersURI() {
        return (String) loginModule.getElement().getOptions().get(usersKey);
    }

    private String getGroupsURI() {
        return (String) loginModule.getElement().getOptions().get(groupsKey);
    }

    private String getDigest() {
        return (String) loginModule.getElement().getOptions().get(digestKey);
    }

    private String getEncoding() {
        return (String) loginModule.getElement().getOptions().get(encodingKey);
    }

    /**
     * Allows the GBean at startup to request that all unencrypted passwords
     * be updated.
     */
    private void encryptAllPasswords() throws GeronimoSecurityException {
        log.debug("Checking passwords to see if any need encrypting");
        refreshAll();
        try {
            String name;
            boolean bUpdates=false;

            for (Enumeration e=users.keys(); e.hasMoreElements(); ) {
                name=(String)e.nextElement();
                String realPassword = users.getProperty(name);
                // Encrypt the password if needed, so we can compare it with the supplied one
                if (realPassword != null) {
                    String pw = EncryptionManager.encrypt(realPassword);
                    if (!realPassword.equals(pw)) {
                        users.setProperty(name, pw);
                        bUpdates = true;
                    }
                }
            }

            // rewrite the users.properties file if we had passwords to encrypt
            if (bUpdates)
            {
                log.debug("Found password(s) that needed encrypting");
                store(users, serverInfo.resolveServer(getUsersURI()).toURL());
            }
        } catch (Exception e) {
            log.error("encryptAllPasswords failed", e);
            throw new GeronimoSecurityException(e);
        }
    }

    private void store(Properties props, URL url) throws Exception {
        OutputStream out = null;
        log.debug("Updating properties file="+url.toExternalForm());
        try {
            try {
                URLConnection con = url.openConnection();
                con.setDoOutput(true);
                out = con.getOutputStream();
            } catch (Exception e) {
                if ("file".equalsIgnoreCase(url.getProtocol()) && e instanceof UnknownServiceException) {
                    out = new FileOutputStream(new File(url.getFile()));
                } else {
                    throw e;
                }
            }
            props.store(out, null);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {
                    // ignored
                }
            }
        }
    }

    /**
     * This method returns the message digest of a specified string.
     * @param password  The string that is to be digested
     * @param algorithm Name of the Message Digest algorithm
     * @param encoding  Encoding to be used for digest data.  Hex by default.
     * @return encoded digest bytes
     * @throws NoSuchAlgorithmException if the Message Digest algorithm is not available
     */
    private String digestPassword(String password, String algorithm, String encoding) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] data = md.digest(password.getBytes());
        if(encoding == null || "hex".equalsIgnoreCase(encoding)) {
            // Convert bytes to hex digits
            byte[] hexData = new byte[data.length * 2];
            HexTranslator ht = new HexTranslator();
            ht.encode(data, 0, data.length, hexData, 0);
            return new String(hexData);
        } else if("base64".equalsIgnoreCase(encoding)) {
            return new String(Base64.encode(data));
        }
        return "";
    }

    public void doFail() {
        log.warn("Failed");
    }

    public void doStart() throws Exception {
        log.debug("Starting gbean");
        if (!isAvailable()) {
            log.warn("Could not find the default properties-login login module");
            return;
        }
        encryptAllPasswords();
        log.debug("Started gbean");
    }

    public void doStop() throws Exception {
        log.debug("Stopping gbean");
        clearAll();
        log.debug("Stopped gbean");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("PropertiesLoginModuleManager", PropertiesLoginModuleManager.class);

        infoFactory.addOperation("addUserPrincipal", new Class[] { Hashtable.class }, void.class.getName());
        infoFactory.addOperation("removeUserPrincipal", new Class[] { String.class }, void.class.getName());
        infoFactory.addOperation("updateUserPrincipal", new Class[] { Hashtable.class }, void.class.getName());
        infoFactory.addOperation("getGroups", String[].class.getName());
        infoFactory.addOperation("getUsers", String[].class.getName());
        infoFactory.addOperation("refreshAll", void.class.getName());

        infoFactory.addOperation("updateUserPrincipal", new Class[] { Hashtable.class }, void.class.getName());

        infoFactory.addOperation("getPassword", new Class[] { String.class }, void.class.getName());
        infoFactory.addOperation("getGroupMembers", new Class[] { String.class }, void.class.getName());
        infoFactory.addOperation("addGroupPrincipal", new Class[] { Hashtable.class }, void.class.getName());
        infoFactory.addOperation("removeGroupPrincipal", new Class[] { String.class }, void.class.getName());
        infoFactory.addOperation("updateGroupPrincipal", new Class[] { Hashtable.class }, void.class.getName());
        infoFactory.addOperation("addToGroup", new Class[] { String.class, String.class }, void.class.getName());
        infoFactory.addOperation("removeFromGroup", new Class[] { String.class, String.class }, void.class.getName());
        infoFactory.addOperation("isAvailable", boolean.class.getName());
        
        infoFactory.addReference("ServerInfo", ServerInfo.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoFactory.addReference("LoginModule", LoginModuleSettings.class, SecurityNames.LOGIN_MODULE);

        infoFactory.setConstructor(new String[]{"ServerInfo", "LoginModule"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
