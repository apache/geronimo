/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http:www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security.providers;

import org.apache.geronimo.security.AbstractSecurityRealm;
import org.apache.geronimo.security.GeronimoSecurityException;
import org.apache.geronimo.kernel.management.State;
import org.apache.regexp.RE;

import javax.security.auth.login.AppConfigurationEntry;
import java.util.Set;
import java.util.Properties;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.Collections;
import java.util.HashMap;
import java.net.URI;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/18 05:17:18 $
 */
public class PropertiesFileSecurityRealm extends AbstractSecurityRealm {
    private URI usersURI;
    private URI groupsURI;
    Properties users = new Properties();
    Properties groups = new Properties();

    final static String REALM = "org.apache.geronimo.security.providers.PropertiesFileSecurityRealm";

    protected void doStart() throws Exception {
        if (usersURI == null) throw  new IllegalStateException("Users URI not set");
        if (groupsURI == null) throw  new IllegalStateException("Groups URI not set");

        refresh();
    }

    protected void doStop() throws Exception {
        usersURI = null;
        groupsURI = null;

        users.clear();
        groups.clear();
    }

    public URI getUsersURI() {
        return usersURI;
    }

    public void setUsersURI(URI usersURI) {
        if (getStateInstance() != State.STOPPED)
            throw new IllegalStateException("Cannot change the Users URI after the realm is started");

        this.usersURI = usersURI;
    }

    public URI getGroupsURI() {
        return groupsURI;
    }

    public void setGroupsURI(URI groupsURI) {
        if (getStateInstance() != State.STOPPED)
            throw new IllegalStateException("Cannot change the Groups URI after the realm is started");

        this.groupsURI = groupsURI;
    }

    public Set getGroupPrincipals() throws GeronimoSecurityException {
        if (getStateInstance() != State.RUNNING)
            throw new IllegalStateException("Cannot obtain Groups until the realm is started");

        return Collections.unmodifiableSet(groups.keySet());
    }

    public Set getGroupPrincipals(RE regexExpression) throws GeronimoSecurityException {
        if (getStateInstance() != State.RUNNING)
            throw new IllegalStateException("Cannot obtain Groups until the realm is started");

        HashSet result = new HashSet();
        Enumeration enum = groups.keys();
        String group;
        while (enum.hasMoreElements()) {
            group = (String) enum.nextElement();

            if (regexExpression.match(group)) {
                result.add(group);
            }
        }

        return result;
    }

    public Set getUserPrincipals() throws GeronimoSecurityException {
        if (getStateInstance() != State.RUNNING)
            throw new IllegalStateException("Cannot obtain Users until the realm is started");

        return Collections.unmodifiableSet(users.keySet());
    }

    public Set getUserPrincipals(RE regexExpression) throws GeronimoSecurityException {
        if (getStateInstance() != State.RUNNING)
            throw new IllegalStateException("Cannot obtain Users until the realm is started");

        HashSet result = new HashSet();
        Enumeration enum = users.keys();
        String user;
        while (enum.hasMoreElements()) {
            user = (String) enum.nextElement();

            if (regexExpression.match(user)) {
                result.add(user);
            }
        }

        return result;
    }

    public void refresh() throws GeronimoSecurityException {
        try {
            users.load(new FileInputStream(new File(usersURI)));
            groups.load(new FileInputStream(new File(groupsURI)));
        } catch (IOException e) {
            throw new GeronimoSecurityException(e);
        }
    }

    public AppConfigurationEntry[] getAppConfigurationEntry() {
        HashMap options = new HashMap();

        options.put(REALM, this);
        AppConfigurationEntry entry = new AppConfigurationEntry("org.apache.geronimo.security.providers.PropertiesFileLoginModule",
                                                                AppConfigurationEntry.LoginModuleControlFlag.SUFFICIENT,
                                                                options);
        AppConfigurationEntry[] configuration = {entry};

        return configuration;
    }
}
