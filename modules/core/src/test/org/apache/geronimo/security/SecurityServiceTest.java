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
 *        Apache Software Foundation (http://www.apache.org/)."
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
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.security;

import java.io.File;
import java.util.Collections;
import java.util.Arrays;

import junit.framework.TestCase;
import org.apache.geronimo.security.providers.PropertiesFileSecurityRealm;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.geronimo.web.WebApp;
import org.apache.geronimo.deployment.model.geronimo.j2ee.Security;
import org.apache.geronimo.deployment.model.ejb.AssemblyDescriptor;
import org.apache.geronimo.deployment.model.ejb.ExcludeList;


/**
 * Unit test for web module configuration
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/16 02:10:46 $
 */
public class SecurityServiceTest extends TestCase {
    SecurityService securityService;

    public void setUp() throws Exception {
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", "org.apache.geronimo.security.GeronimoPolicyConfigurationFactory");

        securityService = new SecurityService();

        PropertiesFileSecurityRealm securityRealm = new PropertiesFileSecurityRealm();
        securityRealm.setRealmName("Foo");
        securityRealm.setUsersURI((new File(new File("."), "src/test-data/data/users.properties")).toURI());
        securityRealm.setGroupsURI((new File(new File("."), "src/test-data/data/groups.properties")).toURI());

        securityService.setRealms(Collections.singleton(securityRealm));
        EjbJar ejbJar = new EjbJar();
        ejbJar.setEnterpriseBeans(new EnterpriseBeans());
        AssemblyDescriptor assemblyDescriptor = new AssemblyDescriptor();
        assemblyDescriptor.setExcludeList(new ExcludeList());
        ejbJar.setAssemblyDescriptor(assemblyDescriptor);
        ejbJar.setSecurity(new Security());
        WebApp webApp = new WebApp();
        webApp.setSecurity(new Security());
        securityService.setModuleConfigurations(Arrays.asList(new Object[] {new EJBModuleConfiguration("Foo", ejbJar),new WebModuleConfiguration("Bar", webApp)}));
    }

    public void tearDown() throws Exception {
    }

    public void testConfig() throws Exception {
        ModuleConfiguration ejbModuleConfiguration = securityService.getModuleConfiguration("Foo", false);
        assertTrue("expected an ejbModuleConfiguration", ejbModuleConfiguration != null);
        ModuleConfiguration webModuleConfiguration = securityService.getModuleConfiguration("Bar", false);
        assertTrue("expected a webModuleConfiguration", webModuleConfiguration != null);
    }
}
