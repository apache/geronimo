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

package org.apache.geronimo.directory;

import java.util.HashSet;
import java.util.Hashtable;

import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import junit.framework.TestCase;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;

public class RunningTest extends TestCase {

    private static final String PRINCIPAL = "uid=admin,ou=system";
    private static final String CREDENTIALS = "secret";
    private ClassLoader cl = this.getClass().getClassLoader();

    private Kernel kernel;

    private ObjectName serverInfoName;

    private GBeanData serverInfoGBean;

    private ObjectName directoryName;

    private GBeanData directoryGBean;

    public void testRunning() throws Exception {

        Hashtable env = new Hashtable();
        env.put(Context.PROVIDER_URL, "ldap://localhost:9389");
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
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

    private void start(GBeanData instance) throws Exception {
        kernel.loadGBean(instance, cl);
        kernel.startGBean(instance.getName());
    }

    private void stop(ObjectName name) throws Exception {
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    protected void setUp() throws Exception {
        super.setUp();

        kernel = KernelFactory.newInstance().createKernel("test.kernel");
        kernel.boot();

        // ServerInfo
        serverInfoName = new ObjectName("geronimo.system:role=ServerInfo");
        serverInfoGBean = new GBeanData(serverInfoName,
                BasicServerInfo.GBEAN_INFO);
        serverInfoGBean.setAttribute("baseDirectory", "./target");
        start(serverInfoGBean);

        // DirectoryGBean
        directoryName = new ObjectName("geronimo.system:type=Directory");
        directoryGBean = new GBeanData(directoryName, DirectoryGBean.GBEAN_INFO);
        directoryGBean.setReferencePattern("ServerInfo", serverInfoName);
        directoryGBean.setAttribute("classLoader", cl);
        directoryGBean.setAttribute("providerURL", "ou=system");
        directoryGBean.setAttribute("securityAuthentication", "simple");
        directoryGBean.setAttribute("securityPrincipal", PRINCIPAL);
        directoryGBean.setAttribute("securityCredentials", CREDENTIALS);
        directoryGBean.setAttribute("anonymousAccess", new Boolean(true));
        directoryGBean.setAttribute("enableNetworking", new Boolean(true));
        directoryGBean.setAttribute("port", new Integer(9389));
        directoryGBean.setAttribute("configFile", "var/directory.xml");

        start(directoryGBean);

    }

    protected void tearDown() throws Exception {
        super.tearDown();

        stop(directoryName);
        stop(serverInfoName);
        kernel.shutdown();
    }

}
