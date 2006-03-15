/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.j2ee.deployment;

import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.management.ObjectName;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.Naming;

/**
 * @version $Rev:385692 $ $Date$
 */
public class EJBRefContextTest extends TestCase {
    private final String coffee = "some/path/coffee.jar";
    private final String language = "some/where/language.jar";
    private final String car = "foo/bar/car.jar";

    private AbstractName coffee_peaberry;
    private AbstractName coffee_java;

    private AbstractName language_lisp;
    private AbstractName language_java;

    private AbstractName car_enzo;

    private RefContext refContext;
    private Configuration configuration;

    public void testNothing() throws Exception {}

    public void xtestSimpleRefs() throws Exception {
        assertReferenceEqual(coffee_peaberry, refContext.getEJBRemoteRef(null, coffee, "peaberry", null, null, true, null, null, configuration));
        assertReferenceEqual(coffee_peaberry, refContext.getEJBLocalRef(null, coffee, "peaberry", null, null, true, null, null, configuration));
    }

    public void xtestAmbiguousRefs() throws Exception {
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(null, coffee, "java", null, null, true, null, null, configuration));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(null, coffee, "java", null, null, true, null, null, configuration));
        assertReferenceEqual(language_java, refContext.getEJBRemoteRef(null, language, "java", null, null, true, null, null, configuration));
        assertReferenceEqual(language_java, refContext.getEJBLocalRef(null, language, "java", null, null, true, null, null, configuration));

        try {
            refContext.getEJBRemoteRef(null, car, "java", null, null, true, null, null, configuration);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
        try {
            refContext.getEJBLocalRef(null, car, "java", null, null, true, null, null, configuration);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
    }

    public void xtestRelativeRefs() throws Exception {
        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef("../../foo/bar/car.jar", coffee, "enzo", null, null, true, null, null, configuration));
        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef("../../foo/bar/car.jar", coffee, "enzo", null, null, true, null, null, configuration));
        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef("./../funk/../../foo/bar/car.jar", coffee, "enzo", null, null, true, null, null, configuration));
        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef("./../funk/../../foo/bar/car.jar", coffee, "enzo", null, null, true, null, null, configuration));
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef("./coffee.jar", coffee, "java", null, null, true, null, null, configuration));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef("./coffee.jar", coffee, "java", null, null, true, null, null, configuration));
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef("coffee.jar", coffee, "java", null, null, true, null, null, configuration));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef("coffee.jar", coffee, "java", null, null, true, null, null, configuration));

        try {
            refContext.getEJBRemoteRef("not_exist.jar", coffee, "blah", null, null, true, null, null, configuration);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBLocalRef("not_exist.jar", coffee, "blah", null, null, true, null, null, configuration);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBRemoteRef("coffee.jar", coffee, "blah", null, null, true, null, null, configuration);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBLocalRef("coffee.jar", coffee, "blah", null, null, true, null, null, configuration);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBRemoteRef("../../../../foo/bar/car.jar",coffee, "enzo", null, null, true, null, null, configuration);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
        try {
            refContext.getEJBLocalRef("../../../../foo/bar/car.jar",coffee, "enzo", null, null, true, null, null, configuration);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
    }

    public void xtestBasicImplicitRefs() throws Exception {
        assertReferenceEqual(language_lisp, refContext.getEJBRemoteRef(null, coffee, "blah", null, null, true, "LispHome", "LispRemote", configuration));
        assertReferenceEqual(language_lisp, refContext.getEJBLocalRef(null, coffee, "blah", null, null, true, "LispLocalHome", "LispLocal", configuration));
    }

    public void xtestInModuleImplicitRefs() throws Exception {
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(null, coffee, "blah", null, null, true, "LocalHome", "Local", configuration));
        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef(null, car, "blah", null, null, true, "LocalHome", "Local", configuration));
    }

    public void xtestAmbiguousModuleImplicitRefs() throws Exception {
        try {
            refContext.getEJBLocalRef(null, language, "blah", null, null, true, "LocalHome", "Local", configuration);
            fail("should have thrown an UnresolvedEJBRefException");
        } catch (UnresolvedEJBRefException e) {
            // good
        }
    }

    public void xtestNoMatchImplicitRefs() throws Exception {
        try {
            refContext.getEJBLocalRef(null, language, "blah", null, null, true, "foo", "bar", configuration);
            fail("should have thrown an UnresolvedEJBRefException");
        } catch (UnresolvedEJBRefException e) {
            // good
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        refContext = new RefContext(new MockEjbReferenceBuilder(), new MockResourceReferenceBuilder(), new MockServiceReferenceBuilder());

        AbstractName applicationName = Naming.createRootName(new Artifact("test", "stuff", "", "ear"), "app", NameFactory.J2EE_APPLICATION) ;
        ConfigurationData bootstrap = new ConfigurationData(new Artifact("test", "test", "", "car"));

        configuration = null;

        AbstractName coffeeName = Naming.createChildName(applicationName, NameFactory.EJB_MODULE, coffee);
        coffee_peaberry = Naming.createChildName(coffeeName, NameFactory.STATELESS_SESSION_BEAN, "peaberry");
        coffee_java = Naming.createChildName(coffeeName, NameFactory.STATELESS_SESSION_BEAN, "java");

        AbstractName languageName = Naming.createChildName(applicationName, NameFactory.EJB_MODULE, language);
        language_lisp = Naming.createChildName(languageName, NameFactory.STATELESS_SESSION_BEAN, "lisp");
        language_java = Naming.createChildName(languageName, NameFactory.STATELESS_SESSION_BEAN, "java");

        AbstractName carName = Naming.createChildName(applicationName, NameFactory.EJB_MODULE, car);
        AbstractName car_gt = Naming.createChildName(carName, NameFactory.STATELESS_SESSION_BEAN, "gt");
        car_enzo = Naming.createChildName(carName, NameFactory.STATELESS_SESSION_BEAN, "enzo");

//        configuration.addGBean(new GBeanData(coffee_peaberry, null));
//        configuration.addGBean(new GBeanData(coffee_java, null));
//
//        configuration.addGBean(new GBeanData(language_lisp, null));
//        configuration.addGBean(new GBeanData(language_java, null));
//
//        configuration.addGBean(new GBeanData(car_gt, null));
//        configuration.addGBean(new GBeanData(car_enzo, null));
    }

//    private void addEJBRemote(URI modulePath, String name, ObjectName objectName, boolean isSession, String home, String remote ) throws MalformedObjectNameException {
//        GBeanData gBeanData = new GBeanData(objectName, null);
//        configuration.addGBean(gBeanData);
//    }
//
//    private void addEJBLocal(URI modulePath, String name, String containerID, boolean isSession, String home, String remote ) throws MalformedObjectNameException {
//        ObjectName objectName = new ObjectName(containerID);
//        GBeanData gBeanData = new GBeanData(objectName, null);
//        configuration.addGBean(gBeanData);
//    }

    private void assertReferenceEqual(AbstractName expected, Reference reference) {
        FakeReference fakeReference = (FakeReference) reference;
        String containerId = null;
        if (fakeReference != null) {
            containerId = fakeReference.containerId;
        }
        assertEquals(expected.getObjectName().getCanonicalName(), containerId);
    }
//    private void assertReferenceEqual(String expected, Reference reference) {
//        FakeReference fakeReference = (FakeReference) reference;
//        String containerId = null;
//        if (fakeReference != null) {
//            containerId = fakeReference.containerId;
//        }
//        assertEquals(expected, containerId);
//    }

    private class FakeReference extends Reference {
        private String containerId;

        public FakeReference(String containerId) {
            super(null);
            this.containerId = containerId;
        }
    }

    private class MockEjbReferenceBuilder implements EJBReferenceBuilder {
        public Reference createEJBLocalReference(String objectName, GBeanData gbeanData, boolean isSession, String localHome, String local) {
            return new FakeReference(objectName);
        }

        public Reference createEJBRemoteReference(GBeanData gbeanData, boolean isSession, String home, String remote) {
            return new FakeReference(null);
        }

        public Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) throws DeploymentException {
            return new FakeReference(objectName);
        }

        public Object createHandleDelegateReference() {
            return null;
        }

        public Reference createEJBRemoteRef(String requiredModule, String optionalModule, String name, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote, Configuration configuration) throws DeploymentException {
            return null;
        }

        public Reference createEJBLocalRef(String requiredModule, String optionalModule, String name, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local, Configuration configuration) throws DeploymentException {
            return null;
        }

   }

    private static class MockResourceReferenceBuilder implements ResourceReferenceBuilder {
        public Reference createResourceRef(AbstractNameQuery containerId, Class iface, Configuration configuration) {
            return null;
        }

        public Reference createAdminObjectRef(AbstractNameQuery containerId, Class iface, Configuration configuration) {
            return null;
        }

        public ObjectName locateResourceName(ObjectName query) {
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
    }

    private static class MockServiceReferenceBuilder implements ServiceReferenceBuilder {
        //it could return a Service or a Reference, we don't care
        public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) {
            return null;
        }
    }
}
