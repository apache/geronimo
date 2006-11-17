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

package org.apache.geronimo.directory;

import junit.framework.TestCase;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;

public class RunningTest extends TestCase {

    private static final String PRINCIPAL = "uid=admin,ou=system";
    private static final String CREDENTIALS = "secret";
    private ClassLoader cl = this.getClass().getClassLoader();

    private DirectoryGBean directory;

    public void testRunning() throws Exception {

        Hashtable env = new Hashtable();
        env.put(Context.PROVIDER_URL, "ldap://localhost:9389");
        String ldapContextFactory = System.getProperty("initial.context.factory");
        if (ldapContextFactory == null) ldapContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                ldapContextFactory);
        //env.put( Context.SECURITY_AUTHENTICATION, "simple");
        env.put( Context.SECURITY_PRINCIPAL, PRINCIPAL);
        env.put( Context.SECURITY_CREDENTIALS, CREDENTIALS);
        DirContext ctx = new InitialDirContext(env);

        // Perform search using URL
        // NamingEnumeration answer = ctx.search(
        // "ldap://localhost:389/ou=system", "(uid=admin)", null);
        HashSet set = new HashSet();

        NamingEnumeration list = ctx.list("ou=system");

        while (list.hasMore()) {
            NameClassPair ncp = (NameClassPair) list.next();
            set.add(ncp.getName());
        }

        assertTrue(set.contains("uid=admin"));
        assertTrue( set.contains( "ou=users" ) );
        assertTrue( set.contains( "ou=groups" ) );
        assertTrue( set.contains( "ou=configuration" ) );
        assertTrue( set.contains( "prefNodeName=sysPrefRoot" ) );

    }

    protected void setUp() throws Exception {

        URL configURL = cl.getResource("directory.xml");
        if (configURL == null) {
            throw new Exception("Can't find config file on classpath");
        }
        String path = configURL.getPath();
        path = path.substring(0, path.lastIndexOf("/"));
        ServerInfo serverInfo = new BasicServerInfo(path);
        directory = new DirectoryGBean(cl, null, true, "directory.xml", serverInfo);
        directory.setEnableNetworking(true);
        directory.setPort(9389);
        directory.setProviderURL("ou=system");
        directory.setSecurityAuthentication("simple");
        directory.setSecurityCredentials(CREDENTIALS);
        directory.setSecurityPrincipal(PRINCIPAL);
        directory.doStart();


    }

    protected void tearDown() throws Exception {
        directory.doStop();
    }

}
