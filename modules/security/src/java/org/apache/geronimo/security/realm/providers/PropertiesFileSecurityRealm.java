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
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.regexp.RE;


/**
 * @version $Rev$ $Date$
 */
public class PropertiesFileSecurityRealm extends AbstractSecurityRealm {
    private static Log log = LogFactory.getLog(PropertiesFileSecurityRealm.class);

    private final ServerInfo serverInfo;

    private boolean running = false;
    private URI usersURI;
    private URI groupsURI;
    final Properties users = new Properties();
    final Properties groups = new Properties();

    final static String REALM_INSTANCE = "org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm";

    public PropertiesFileSecurityRealm(String realmName, URI usersURI, URI groupsURI, ServerInfo serverInfo) {
        super(realmName);
        this.serverInfo = serverInfo;
        setUsersURI(usersURI);
        setGroupsURI(groupsURI);
    }

    public void doStart() {
        if (usersURI == null) throw  new IllegalStateException("Users URI not set");
        if (groupsURI == null) throw  new IllegalStateException("Groups URI not set");

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

    public void setUsersURI(URI usersURI) {
        if (running) {
            throw new IllegalStateException("Cannot change the Users URI after the realm is started");
        }
        this.usersURI = usersURI;
    }

    public URI getGroupsURI() {
        return groupsURI;
    }

    public void setGroupsURI(URI groupsURI) {
        if (running) {
            throw new IllegalStateException("Cannot change the Groups URI after the realm is started");
        }
        this.groupsURI = groupsURI;
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

        options.put(REALM_INSTANCE, this);
        AppConfigurationEntry entry = new AppConfigurationEntry("org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule",
                AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                options);

        return new AppConfigurationEntry[]{entry};
    }

    public boolean isLoginModuleLocal() {
        return true;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(PropertiesFileSecurityRealm.class, AbstractSecurityRealm.GBEAN_INFO);

        infoFactory.addAttribute("usersURI", URI.class, true);
        infoFactory.addAttribute("groupsURI", URI.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class);

        infoFactory.addOperation("isLoginModuleLocal");

        infoFactory.setConstructor(new String[]{"realmName", "usersURI", "groupsURI", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
