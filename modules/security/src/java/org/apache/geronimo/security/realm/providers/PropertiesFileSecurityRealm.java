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

package org.apache.geronimo.security.realm.providers;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import javax.security.auth.login.AppConfigurationEntry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.common.GeronimoSecurityException;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.realm.AutoMapAssistant;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.regexp.RE;


/**
 * @version $Rev$ $Date$
 */
public class PropertiesFileSecurityRealm extends AbstractSecurityRealm implements AutoMapAssistant {

    private static Log log = LogFactory.getLog(PropertiesFileSecurityRealm.class);

    private boolean running = false;
    private final ServerInfo serverInfo;
    private final URI usersURI;
    private final URI groupsURI;
    final Properties users = new Properties();
    final Properties groups = new Properties();
    private final String defaultPrincipal;

    final static String USERS_URI = "org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm.USERS_URI";
    final static String GROUPS_URI = "org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm.GROUPS_URI";

    public PropertiesFileSecurityRealm(String realmName, URI usersURI, URI groupsURI, String defaultPrincipal, ServerInfo serverInfo) {
        super(realmName);

        assert serverInfo != null;
        assert usersURI != null;
        assert groupsURI != null;

        this.serverInfo = serverInfo;
        this.usersURI = usersURI;
        this.groupsURI = groupsURI;
        this.defaultPrincipal = defaultPrincipal;
    }

    public void doStart() {
        refresh();
        running = true;

        log.info("Properties File Realm - " + getRealmName() + " - started");
    }

    public void doStop() {
        users.clear();
        groups.clear();
        running = false;

        log.info("Properties File Realm - " + getRealmName() + " - stopped");
    }

    public URI getUsersURI() {
        return usersURI;
    }

    public URI getGroupsURI() {
        return groupsURI;
    }

    public String getDefaultPrincipal() {
        return defaultPrincipal;
    }

    public Set getGroupPrincipals() throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Groups until the realm is started");
        }
        return Collections.unmodifiableSet(groups.keySet());
    }

    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Groups until the realm is started");
        }
        HashSet result = new HashSet();
        Enumeration e = groups.keys();
        String group;
        while (e.hasMoreElements()) {
            group = (String) e.nextElement();

            if (regexExpression.match(group)) {
                result.add(group);
            }
        }

        return result;
    }

    public Set getUserPrincipals() throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Users until the realm is started");
        }
        return Collections.unmodifiableSet(users.keySet());
    }

    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException {
        if (!running) {
            throw new IllegalStateException("Cannot obtain Users until the realm is started");
        }
        HashSet result = new HashSet();
        Enumeration e = users.keys();
        String user;
        while (e.hasMoreElements()) {
            user = (String) e.nextElement();

            if (regexExpression.match(user)) {
                result.add(user);
            }
        }

        return result;
    }

    public void refresh() throws GeronimoSecurityException {
        try {
            users.load(serverInfo.resolve(usersURI).toURL().openStream());

            Properties temp = new Properties();
            temp.load(serverInfo.resolve(groupsURI).toURL().openStream());

            Enumeration e = temp.keys();
            while (e.hasMoreElements()) {
                String groupName = (String) e.nextElement();
                String[] userList = ((String) temp.get(groupName)).split(",");

                Set userset = (Set) groups.get(groupName);
                if (userset == null) {
                    userset = new HashSet();
                    groups.put(groupName, userset);
                }

                for (int i = 0; i < userList.length; i++) {
                    userset.add(userList[i]);
                }
            }

            log.info("Properties File Realm - " + getRealmName() + " - refresh");
        } catch (IOException e) {
            log.info("Properties File Realm - " + getRealmName() + " - refresh failed");
            throw new GeronimoSecurityException(e);
        }
    }

    public AppConfigurationEntry[] getAppConfigurationEntries() {
        HashMap options = new HashMap();

        options.put(USERS_URI, serverInfo.resolve(usersURI));
        options.put(GROUPS_URI, serverInfo.resolve(groupsURI));
        AppConfigurationEntry entry = new AppConfigurationEntry("org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule",
                AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                options);

        return new AppConfigurationEntry[]{entry};
    }

    public boolean isLoginModuleLocal() {
        return true;
    }

    /**
     * Provides the default principal to be used when an unauthenticated
     * subject uses a container.
     *
     * @return the default principal
     */
    public Principal obtainDefaultPrincipal() {
        Principal principal = new Principal();

        principal.setClassName(PropertiesFileUserPrincipal.class.getName());
        principal.setPrincipalName(defaultPrincipal);

        return principal;
    }

    /**
     * Provides a set of principal class names to be used when automatically
     * mapping principals to roles.
     *
     * @return a set of principal class names
     */
    public Set obtainRolePrincipalClasses() {
        Set principals = new HashSet();

        principals.add(PropertiesFileGroupPrincipal.class.getName());

        return principals;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(PropertiesFileSecurityRealm.class, AbstractSecurityRealm.GBEAN_INFO);

        infoFactory.addInterface(AutoMapAssistant.class);
        infoFactory.addAttribute("usersURI", URI.class, true);
        infoFactory.addAttribute("groupsURI", URI.class, true);
        infoFactory.addAttribute("defaultPrincipal", String.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class);

        infoFactory.setConstructor(new String[]{"realmName", "usersURI", "groupsURI", "defaultPrincipal", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
