/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.naming.deployment;

import java.net.URI;
import java.util.Map;
import java.util.List;
import java.util.Set;
import javax.naming.Reference;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.NamingContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.GBeanDataRegistry;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.naming.java.ComponentContextBuilder;

/**
 * @version $Rev$ $Date$
 */
public class MessageDestinationTest extends TestCase {
    private Kernel kernel = null;
    private RefContext refContext =  new RefContext(new EJBReferenceBuilder() {
            public Reference createEJBLocalReference(String objectName, GBeanData gbeanData, boolean isSession, String localHome, String local) throws DeploymentException {
                return null;
            }

            public Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home, String remote) throws DeploymentException {
                return null;
            }

            public Reference createCORBAReference(URI corbaURL, String objectName, ObjectName containerName, String home) throws DeploymentException {
                return null;
            }

            public Object createHandleDelegateReference() throws DeploymentException {
                return null;
            }

            public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext context) throws DeploymentException {
                return null;
            }

            public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext context) throws DeploymentException {
                return null;
            }
        }, new ResourceReferenceBuilder() {
            public Reference createResourceRef(String containerId, Class iface) throws DeploymentException {
                return null;
            }

            public Reference createAdminObjectRef(String containerId, Class iface) throws DeploymentException {
                return null;
            }

            public GBeanData locateActivationSpecInfo(GBeanData resourceAdapterModuleData, String messageListenerInterface) throws DeploymentException {
                return null;
            }

            public GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData) throws DeploymentException {
                return null;
            }

            public GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName) throws DeploymentException {
                return null;
            }

            public GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) throws DeploymentException {
                return null;
            }
        }, new ServiceReferenceBuilder() {
            //it could return a Service or a Reference, we don't care
            public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
                return null;
            }
        }, kernel

                );
    J2eeContext j2eeContext = new J2eeContextImpl("domain", "server", "app", NameFactory.JCA_RESOURCE, "module1", null, null);
    NamingContext namingContext = new MockNamingContext(j2eeContext);

    ComponentContextBuilder builder = new ComponentContextBuilder();

    public void testMessageDestinations() throws Exception {
        MessageDestinationType[] specdests = new MessageDestinationType[] {makeMD("d1"), makeMD("d2")};
        GerMessageDestinationType[] gerdests = new GerMessageDestinationType[] {makeGerMD("d1", "l1"), makeGerMD("d2", "l2")};
        MessageDestinationRefType[] destRefs = new MessageDestinationRefType[] {makeMDR("n1", "d1"), makeMDR("n2", "d2")};
        ENCConfigBuilder.registerMessageDestinations(refContext, "module1", specdests, gerdests);
        ObjectName n1 = NameFactory.getComponentName(null, null, null, null, null, "l1", NameFactory.JCA_ADMIN_OBJECT, j2eeContext);
        ObjectName n2 = NameFactory.getComponentName(null, null, null, null, null, "l2", NameFactory.JCA_ADMIN_OBJECT, j2eeContext);
        namingContext.addGBean(new GBeanData(n1, null));
        namingContext.addGBean(new GBeanData(n2, null));
        ENCConfigBuilder.addMessageDestinationRefs(refContext, namingContext, destRefs, this.getClass().getClassLoader(), builder);
        Map context = builder.getContext();
        assertEquals(2, context.size());
    }

    public void testMessageDestinationsWithModule() throws Exception {
        MessageDestinationType[] specdests = new MessageDestinationType[] {makeMD("d1"), makeMD("d2")};
        GerMessageDestinationType[] gerdests = new GerMessageDestinationType[] {makeGerMD("d1", "module1", "l1"), makeGerMD("d2", "module1", "l2")};
        MessageDestinationRefType[] destRefs = new MessageDestinationRefType[] {makeMDR("n1", "d1"), makeMDR("n2", "d2")};
        ENCConfigBuilder.registerMessageDestinations(refContext, "module1", specdests, gerdests);
        ObjectName n1 = NameFactory.getComponentName(null, null, null, null, null, "l1", NameFactory.JCA_ADMIN_OBJECT, j2eeContext);
        ObjectName n2 = NameFactory.getComponentName(null, null, null, null, null, "l2", NameFactory.JCA_ADMIN_OBJECT, j2eeContext);
        namingContext.addGBean(new GBeanData(n1, null));
        namingContext.addGBean(new GBeanData(n2, null));
        ENCConfigBuilder.addMessageDestinationRefs(refContext, namingContext, destRefs, this.getClass().getClassLoader(), builder);
        Map context = builder.getContext();
        assertEquals(2, context.size());
    }

    public void testMessageDestinationsMatch() throws Exception {
        MessageDestinationType[] specdests = new MessageDestinationType[] {makeMD("d1")};
        GerMessageDestinationType[] gerdests = new GerMessageDestinationType[] {makeGerMD("d1", "l1"), makeGerMD("d2", "l2")};
        try {
            ENCConfigBuilder.registerMessageDestinations(refContext, "module1", specdests, gerdests);
            fail("tried to register a GerMessageDestination witout a MessageDestination and it succeeded");
        } catch (DeploymentException e) {

        }
    }

    private MessageDestinationRefType makeMDR(String name, String link) {
        MessageDestinationRefType mdr = MessageDestinationRefType.Factory.newInstance();
        mdr.addNewMessageDestinationRefName().setStringValue(name);
        mdr.addNewMessageDestinationType().setStringValue(Object.class.getName());
        mdr.addNewMessageDestinationLink().setStringValue(link);
        return mdr;
    }

    private MessageDestinationType makeMD(String name) {
        MessageDestinationType d1 = MessageDestinationType.Factory.newInstance();
        d1.addNewMessageDestinationName().setStringValue(name);
        return d1;
    }

    private GerMessageDestinationType makeGerMD(String name, String link) {
        GerMessageDestinationType d1 = GerMessageDestinationType.Factory.newInstance();
        d1.setMessageDestinationName(name);
        d1.setAdminObjectLink(link);
        return d1;
    }

    private GerMessageDestinationType makeGerMD(String name, String module, String link) {
        GerMessageDestinationType d1 = makeGerMD(name, link);
        d1.setAdminObjectModule(module);
        return d1;
    }
    class MockNamingContext implements NamingContext {

        private final GBeanDataRegistry gbeans = new GBeanDataRegistry();
        private final J2eeContext j2eeContext;

        public MockNamingContext(J2eeContext j2eeContext) {
            this.j2eeContext = j2eeContext;
        }

        public J2eeContext getJ2eeContext() {
            return j2eeContext;
        }

        public void addGBean(GBeanData gbean) {
            gbeans.register(gbean);
        }

        public Set getGBeanNames() {
            return gbeans.getGBeanNames();
        }

        public Set listGBeans(ObjectName pattern) {
            return gbeans.listGBeans(pattern);
        }

        public GBeanData getGBeanInstance(ObjectName name) throws GBeanNotFoundException {
            return gbeans.getGBeanInstance(name);
        }

        public Artifact getConfigID() {
            return new Artifact("groupId", "MockNamingContextID", "1", "car", true);
        }
    }


}
