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
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;

/**
 * @version $Rev$ $Date$
 */
public class EJBRefContextTest extends TestCase {
    private final URI coffee = URI.create("some/path/coffee.jar");
    private final URI language = URI.create("some/where/language.jar");
    private final URI car = URI.create("foo/bar/car.jar");

    private AbstractName coffee_peaberry;
    private AbstractName coffee_java;

    private AbstractName language_lisp;
    private AbstractName language_java;

    private AbstractName car_enzo;

    private RefContext refContext;
    private NamingContext namingContext;

    public void testSimpleRefs() throws Exception {
        assertReferenceEqual(coffee_peaberry, refContext.getEJBRemoteRef(coffee, "peaberry", true, null, null, namingContext));
        assertReferenceEqual(coffee_peaberry, refContext.getEJBLocalRef(coffee, "peaberry", true, null, null, namingContext));
    }

    public void testAmbiguousRefs() throws Exception {
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "java", true, null, null, namingContext));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(coffee, "java", true, null, null, namingContext));
        assertReferenceEqual(language_java, refContext.getEJBRemoteRef(language, "java", true, null, null, namingContext));
        assertReferenceEqual(language_java, refContext.getEJBLocalRef(language, "java", true, null, null, namingContext));

        try {
            refContext.getEJBRemoteRef(car, "java", true, null, null, namingContext);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
        try {
            refContext.getEJBLocalRef(car, "java", true, null, null, namingContext);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
    }

    public void testRelativeRefs() throws Exception {
        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef(coffee, "../../foo/bar/car.jar#enzo", true, null, null, namingContext));
        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef(coffee, "../../foo/bar/car.jar#enzo", true, null, null, namingContext));
        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef(coffee, "./../funk/../../foo/bar/car.jar#enzo", true, null, null, namingContext));
        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef(coffee, "./../funk/../../foo/bar/car.jar#enzo", true, null, null, namingContext));
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "./coffee.jar#java", true, null, null, namingContext));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(coffee, "./coffee.jar#java", true, null, null, namingContext));
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "coffee.jar#java", true, null, null, namingContext));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(coffee, "coffee.jar#java", true, null, null, namingContext));

        try {
            refContext.getEJBRemoteRef(coffee, "not_exist.jar#blah", true, null, null, namingContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBLocalRef(coffee, "not_exist.jar#blah", true, null, null, namingContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBRemoteRef(coffee, "coffee.jar#blah", true, null, null, namingContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBLocalRef(coffee, "coffee.jar#blah", true, null, null, namingContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBRemoteRef(coffee, "../../../../foo/bar/car.jar#enzo", true, null, null, namingContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
        try {
            refContext.getEJBLocalRef(coffee, "../../../../foo/bar/car.jar#enzo", true, null, null, namingContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
    }

    public void xtestBasicImplicitRefs() throws Exception {
        assertReferenceEqual(language_lisp, refContext.getImplicitEJBRemoteRef(coffee, "blah", true, "LispHome", "LispRemote", namingContext));
        assertReferenceEqual(language_lisp, refContext.getImplicitEJBLocalRef(coffee, "blah", true, "LispLocalHome", "LispLocal", namingContext));
    }

    public void xtestInModuleImplicitRefs() throws Exception {
        assertReferenceEqual(coffee_java, refContext.getImplicitEJBLocalRef(coffee, "blah", true, "LocalHome", "Local", namingContext));
        assertReferenceEqual(car_enzo, refContext.getImplicitEJBLocalRef(car, "blah", true, "LocalHome", "Local", namingContext));
    }

    public void xtestAmbiguousModuleImplicitRefs() throws Exception {
        try {
            refContext.getImplicitEJBLocalRef(language, "blah", true, "LocalHome", "Local", namingContext);
            fail("should have thrown an UnresolvedEJBRefException");
        } catch (UnresolvedEJBRefException e) {
            // good
        }
    }

    public void xtestNoMatchImplicitRefs() throws Exception {
        try {
            refContext.getImplicitEJBLocalRef(language, "blah", true, "foo", "bar", namingContext);
            fail("should have thrown an UnresolvedEJBRefException");
        } catch (UnresolvedEJBRefException e) {
            // good
        }
    }

    protected void setUp() throws Exception {
        super.setUp();

        refContext = new RefContext(new MockEjbReferenceBuilder(), new MockResourceReferenceBuilder(), new MockServiceReferenceBuilder(), null);

        AbstractName applicationName = NameFactory.getRootName(new Artifact("test", "stuff", "", "ear"), "app", NameFactory.J2EE_APPLICATION) ;
        namingContext = new MockNamingContext(applicationName);

        AbstractName coffeeName = NameFactory.getChildName(applicationName, NameFactory.EJB_MODULE, coffee.getPath(), null);
        coffee_peaberry = NameFactory.getChildName(coffeeName, NameFactory.STATELESS_SESSION_BEAN, "peaberry", null);
        coffee_java = NameFactory.getChildName(coffeeName, NameFactory.STATELESS_SESSION_BEAN, "java", null);

        AbstractName languageName = NameFactory.getChildName(applicationName, NameFactory.EJB_MODULE, language.getPath(), null);
        language_lisp = NameFactory.getChildName(languageName, NameFactory.STATELESS_SESSION_BEAN, "lisp", null);
        language_java = NameFactory.getChildName(languageName, NameFactory.STATELESS_SESSION_BEAN, "java", null);

        AbstractName carName = NameFactory.getChildName(applicationName, NameFactory.EJB_MODULE, car.getPath(), null);
        AbstractName car_gt = NameFactory.getChildName(carName, NameFactory.STATELESS_SESSION_BEAN, "gt", null);
        car_enzo = NameFactory.getChildName(carName, NameFactory.STATELESS_SESSION_BEAN, "enzo", null);

        namingContext.addGBean(new GBeanData(coffee_peaberry, null));
        namingContext.addGBean(new GBeanData(coffee_java, null));

        namingContext.addGBean(new GBeanData(language_lisp, null));
        namingContext.addGBean(new GBeanData(language_java, null));

        namingContext.addGBean(new GBeanData(car_gt, null));
        namingContext.addGBean(new GBeanData(car_enzo, null));
    }

//    private void addEJBRemote(URI modulePath, String name, ObjectName objectName, boolean isSession, String home, String remote ) throws MalformedObjectNameException {
//        GBeanData gBeanData = new GBeanData(objectName, null);
//        namingContext.addGBean(gBeanData);
//    }
//
//    private void addEJBLocal(URI modulePath, String name, String containerID, boolean isSession, String home, String remote ) throws MalformedObjectNameException {
//        ObjectName objectName = new ObjectName(containerID);
//        GBeanData gBeanData = new GBeanData(objectName, null);
//        namingContext.addGBean(gBeanData);
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

        public Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home, String remote) {
            return new FakeReference(objectName);
        }

        public Reference createCORBAReference(URI corbaURL, String objectName, AbstractName containerName, String home) {
            return new FakeReference(objectName);
        }

        public Object createHandleDelegateReference() {
            return null;
        }

        public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext context) {
            return null;
        }

        public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext context) {
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

        public GBeanData locateActivationSpecInfo(GBeanData resourceAdapterModuleData, String messageListenerInterface) {
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
