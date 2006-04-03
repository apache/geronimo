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

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EJBReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.RefContext;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.j2ee.deployment.ServiceReferenceBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationResolver;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerMessageDestinationType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationRefType;
import org.apache.geronimo.xbeans.j2ee.MessageDestinationType;

import javax.naming.Reference;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev: 385372 $ $Date$
 */
public class MessageDestinationTest extends TestCase {
    private RefContext refContext = new RefContext(new EJBReferenceBuilder() {

        public Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) {
            return null;
        }

        public Object createHandleDelegateReference() {
            return null;
        }

        public Reference createEJBRemoteRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote) {
            return null;
        }

        public Reference createEJBLocalRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local) {
            return null;
        }

    }, new ResourceReferenceBuilder() {
        public Reference createResourceRef(AbstractNameQuery containerId, Class iface, Configuration configuration) {
            return null;
        }

        public Reference createAdminObjectRef(AbstractNameQuery containerId, Class iface, Configuration configuration) {
            return null;
        }

        public GBeanData locateActivationSpecInfo(AbstractNameQuery nameQuery, String messageListenerInterface, Configuration configuration) {
            return null;
        }

        public GBeanData locateResourceAdapterGBeanData(GBeanData resourceAdapterModuleData) {
            return null;
        }

        public GBeanData locateAdminObjectInfo(GBeanData resourceAdapterModuleData, String adminObjectInterfaceName) {
            return null;
        }

        public GBeanData locateConnectionFactoryInfo(GBeanData resourceAdapterModuleData, String connectionFactoryInterfaceName) {
            return null;
        }
    }, new ServiceReferenceBuilder() {
        //it could return a Service or a Reference, we don't care
        public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) {
            return null;
        }
    }

    );
    private static final Naming naming = new Jsr77Naming();
    Configuration configuration;
    AbstractName baseName;

    ComponentContextBuilder builder = new ComponentContextBuilder();

    protected void setUp() throws Exception {
        super.setUp();
        Artifact id = new Artifact("test", "test", "", "car");
        configuration = new Configuration(Collections.EMPTY_LIST,
                new ConfigurationData(id, naming),
                new ConfigurationResolver(id, null));
        baseName = naming.createRootName(configuration.getId(), "testRoot", NameFactory.RESOURCE_ADAPTER_MODULE);
    }

    public void testMessageDestinations() throws Exception {
        MessageDestinationType[] specdests = new MessageDestinationType[]{makeMD("d1"), makeMD("d2")};
        GerMessageDestinationType[] gerdests = new GerMessageDestinationType[]{makeGerMD("d1", "l1"), makeGerMD("d2", "l2")};
        MessageDestinationRefType[] destRefs = new MessageDestinationRefType[]{makeMDR("n1", "d1"), makeMDR("n2", "d2")};
        ENCConfigBuilder.registerMessageDestinations(refContext, "module1", specdests, gerdests);
        AbstractName n1 = naming.createChildName(baseName, "l1", NameFactory.JCA_ADMIN_OBJECT);
        AbstractName n2 = naming.createChildName(baseName, "l2", NameFactory.JCA_ADMIN_OBJECT);
        configuration.addGBean(new GBeanData(n1, null));
        configuration.addGBean(new GBeanData(n2, null));
        ENCConfigBuilder.addMessageDestinationRefs(configuration, refContext, destRefs, this.getClass().getClassLoader(), builder);
        Map context = builder.getContext();
        assertEquals(2, context.size());
    }

    public void testMessageDestinationsWithModule() throws Exception {
        MessageDestinationType[] specdests = new MessageDestinationType[]{makeMD("d1"), makeMD("d2")};
        GerMessageDestinationType[] gerdests = new GerMessageDestinationType[]{makeGerMD("d1", "module1", "l1"), makeGerMD("d2", "module1", "l2")};
        MessageDestinationRefType[] destRefs = new MessageDestinationRefType[]{makeMDR("n1", "d1"), makeMDR("n2", "d2")};
        ENCConfigBuilder.registerMessageDestinations(refContext, "module1", specdests, gerdests);
        AbstractName n1 = naming.createChildName(baseName, "l1", NameFactory.JCA_ADMIN_OBJECT);
        AbstractName n2 = naming.createChildName(baseName, "l2", NameFactory.JCA_ADMIN_OBJECT);
        configuration.addGBean(new GBeanData(n1, null));
        configuration.addGBean(new GBeanData(n2, null));
        ENCConfigBuilder.addMessageDestinationRefs(configuration, refContext, destRefs, this.getClass().getClassLoader(), builder);
        Map context = builder.getContext();
        assertEquals(2, context.size());
    }

    public void testMessageDestinationsMatch() throws Exception {
        MessageDestinationType[] specdests = new MessageDestinationType[]{makeMD("d1")};
        GerMessageDestinationType[] gerdests = new GerMessageDestinationType[]{makeGerMD("d1", "l1"), makeGerMD("d2", "l2")};
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

}
