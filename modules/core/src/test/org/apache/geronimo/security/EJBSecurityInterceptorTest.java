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
package org.apache.geronimo.security;

import junit.framework.TestCase;
import org.apache.geronimo.proxy.ProxyContainer;
import org.apache.geronimo.proxy.ReflexiveInterceptor;
import org.apache.geronimo.proxy.ProxyInvocation;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.EJBInvocationUtil;
import org.apache.geronimo.ejb.SimpleEnterpriseContext;
import org.apache.geronimo.ejb.metadata.EJBMetadataImpl;
import org.apache.geronimo.ejb.metadata.MethodMetadataImpl;
import org.apache.geronimo.core.service.AbstractInterceptor;
import org.apache.geronimo.core.service.InvocationResult;
import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.security.util.ContextManager;

import javax.security.auth.Subject;
import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.PolicyConfigurationFactory;
import javax.security.jacc.PolicyConfiguration;
import javax.ejb.EnterpriseBean;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Policy;
import java.lang.reflect.Method;
import java.util.Collections;


/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/11/18 05:28:27 $
 */
public class EJBSecurityInterceptorTest extends TestCase {

    final String CONTEXT_ID = "Foo Deployment Id";
    final AccessControlContext currentAcc = AccessController.getContext();
    AccessControlContext combinedContext;

    public void setUp() throws Exception {
        System.setProperty("javax.security.jacc.PolicyConfigurationFactory.provider", "org.apache.geronimo.security.GeronimoPolicyConfigurationFactory");

        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        Policy.setPolicy(new GeronimoPolicy(factory));
    }

    public void tearDown() throws Exception {
    }

    public void testEjbName() throws Exception {
        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        PolicyConfiguration configuration = factory.getPolicyConfiguration(CONTEXT_ID, true);
        configuration.addToRole("FooRole", new EJBMethodPermission("Foo", "setName,Local,java.lang.String"));
        configuration.addToRole("FooRole", new EJBMethodPermission("Foo", "getName,Local,"));
        ((RoleMappingConfiguration) configuration).addRoleMapping("FooRole", Collections.singletonList(new RealmPrincipal("Oz", new TestPrincipal("Wizard"))));
        configuration.commit();

        TestPerson person = new TestPerson();
        ITestPerson test = (ITestPerson) createProxy(person);

        Subject subject = new Subject();
        subject.getPrincipals().add(new RealmPrincipal("Oz", new TestPrincipal("Wizard")));
        AccessControlContext context = (AccessControlContext)Subject.doAsPrivileged(subject, new java.security.PrivilegedAction() {
            public Object run() {
                return AccessController.getContext();
            }
        }, null);
        subject.setReadOnly();
        ContextManager.registerContext(subject, context);
        ContextManager.pushSubject(subject);

        test.setName("Izumi");
        String name = test.getName();

        assertEquals("Izumi", name);
    }

    private Object createProxy(EnterpriseBean target) throws Exception {

        // Setup the server side contianer.
        ProxyContainer serverContainer = new ProxyContainer();

        SetupInterceptor setupInterceptor = new SetupInterceptor(target);
        setupInterceptor.setContainer(serverContainer);
        serverContainer.addInterceptor(setupInterceptor);

        EJBSecurityInterceptor securityInterceptor = new EJBSecurityInterceptor();
        securityInterceptor.setContainer(serverContainer);
        serverContainer.addInterceptor(securityInterceptor);

        // Optional interceptor
        PolicyContextHandlerEJBInterceptor pchInterceptor = new PolicyContextHandlerEJBInterceptor();
        serverContainer.addInterceptor(pchInterceptor);

        serverContainer.addInterceptor(new ReflexiveInterceptor(target));

        EJBMetadataImpl ejbMetadata = new EJBMetadataImpl();
        ejbMetadata.setName("Foo");
        ejbMetadata.setPolicyContextId(CONTEXT_ID);
        EJBPlugins.putEJBMetadata(serverContainer, ejbMetadata);

        securityInterceptor.setEjbMetadata(ejbMetadata);

        PolicyConfigurationFactory factory = PolicyConfigurationFactory.getPolicyConfigurationFactory();
        PolicyConfiguration configuration = factory.getPolicyConfiguration(CONTEXT_ID, false);        
        securityInterceptor.setPolicyConfiguration((GeronimoPolicyConfiguration)configuration);

        Method method = findMethod(ITestPerson.class, "setName");
        MethodMetadataImpl methodMetadata = new MethodMetadataImpl();
        methodMetadata.setEJBMethodPermission(new EJBMethodPermission(ejbMetadata.getName(), "Local", method));
        ejbMetadata.putMethodMetadata(method, methodMetadata);

        method = findMethod(ITestPerson.class, "getName");
        methodMetadata = new MethodMetadataImpl();
        methodMetadata.setEJBMethodPermission(new EJBMethodPermission(ejbMetadata.getName(), "Local", method));
        ejbMetadata.putMethodMetadata(method, methodMetadata);

        return serverContainer.createProxy(target.getClass().getClassLoader(), new Class[]{ITestPerson.class});
    }

    class SetupInterceptor extends AbstractInterceptor {
        private final EnterpriseBean bean;

        SetupInterceptor(EnterpriseBean bean) {
            this.bean = bean;
        }

        public InvocationResult invoke(Invocation invocation) throws Throwable {
            ProxyInvocation proxyInvocation = (ProxyInvocation) invocation;
            EJBInvocationUtil.putMethod(invocation, ProxyInvocation.getMethod(proxyInvocation));
            EJBInvocationUtil.putArguments(invocation, ProxyInvocation.getArguments(proxyInvocation));

            SimpleEnterpriseContext enterpriseContext = new SimpleEnterpriseContext();
            enterpriseContext.setContainer(getContainer());
            enterpriseContext.setInstance(bean);
            EJBInvocationUtil.putEnterpriseContext(invocation, enterpriseContext);

            return getNext().invoke(invocation);
        }

    }

    private Method findMethod(Class c, String name) {
        Method[] methods = c.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(name))
                return methods[i];
        }
        return null;
    }
}
