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
import org.apache.geronimo.security.realm.providers.PropertiesFileSecurityRealm;
import org.apache.geronimo.security.jacc.EJBModuleConfiguration;
import org.apache.geronimo.security.jacc.ModuleConfiguration;
import org.apache.geronimo.security.jacc.WebModuleConfiguration;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.AssemblyDescriptorType;
import org.apache.geronimo.xbeans.j2ee.WebAppType;
import org.apache.geronimo.xbeans.geronimo.security.GerSecurityType;

/**
 * Unit test for web module configuration
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/12 08:14:05 $
 */
public class SecurityServiceTest extends TestCase {
    SecurityService securityService;

    public void setUp() throws Exception {
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");

        securityService = new SecurityService();

        PropertiesFileSecurityRealm securityRealm = new PropertiesFileSecurityRealm("Foo",
                (new File(new File("."), "src/test-data/data/users.properties")).toURI(),
                (new File(new File("."), "src/test-data/data/groups.properties")).toURI());
        securityRealm.doStart();

        securityService.setRealms(Collections.singleton(securityRealm));
        EjbJarType ejbJar = EjbJarType.Factory.newInstance();
        ejbJar.addNewEnterpriseBeans();
        AssemblyDescriptorType assemblyDescriptor = ejbJar.addNewAssemblyDescriptor();
        assemblyDescriptor.addNewExcludeList();
        GerSecurityType security = GerSecurityType.Factory.newInstance();
        WebAppType webApp = WebAppType.Factory.newInstance();

        securityService.setModuleConfigurations(Arrays.asList(new Object[] {new EJBModuleConfiguration("Foo", ejbJar, security),new WebModuleConfiguration("Bar", webApp, security)}));
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
