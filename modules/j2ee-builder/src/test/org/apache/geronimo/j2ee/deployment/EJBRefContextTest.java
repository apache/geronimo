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
import java.util.Map;
import java.util.List;
import java.io.File;
import javax.naming.Reference;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.AmbiguousEJBRefException;
import org.apache.geronimo.common.UnknownEJBRefException;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Rev$ $Date$
 */
public class EJBRefContextTest extends TestCase {
    private final URI coffee = URI.create("some/path/coffee.jar");
    private final URI language = URI.create("some/where/language.jar");
    private final URI car = URI.create("foo/bar/car.jar");

    private final ObjectName coffee_peaberry;
//    private final String coffee_peaberry_local = "foo:name=coffee_peaberry_Local";
    private final ObjectName coffee_java;
//    private final String coffee_java_local = "foo:name=coffee_java_local";

    private final ObjectName language_lisp;
//    private final String language_lisp_local = "foo:name=language_lisp_local";
    private final ObjectName language_java;
//    private final String language_java_local = "foo:name=language_java_local";

    private final ObjectName car_gt;
//    private final String car_gt_local = "foo:name=car_gt_local";
    private final ObjectName car_enzo;
//    private final String car_enzo_local = "foo:name=car_enzo_local";
    private Kernel kernel = null;
    private J2eeContext j2eeContext;
    private RefContext refContext;
    private NamingContext earContext;

    public EJBRefContextTest(String s) throws MalformedObjectNameException {
        super(s);
        refContext = new RefContext(new EJBReferenceBuilder() {
            public Reference createEJBLocalReference(String objectName, GBeanData gbeanData, boolean isSession, String localHome, String local) {
                return new FakeReference(objectName);
            }

            public Reference createEJBRemoteReference(String objectName, GBeanData gbeanData, boolean isSession, String home, String remote) {
                return new FakeReference(objectName);
            }

            public Reference createCORBAReference(URI corbaURL, String objectName, ObjectName containerName, String home) throws DeploymentException {
                return new FakeReference(objectName);
            }

            public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext context) throws DeploymentException {
                return null;
            }

            public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext context) throws DeploymentException {
                return null;
            }
        }, new ResourceReferenceBuilder() {

            public Reference createResourceRef(String containerId, Class iface) {
                return null;
            }

            public Reference createAdminObjectRef(String containerId, Class iface) {
                return null;
            }

            public ObjectName locateResourceName(ObjectName query) throws DeploymentException {
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
        }, kernel);

        j2eeContext = new J2eeContextImpl("domain", "server", "app", "module", NameFactory.EJB_MODULE, null, null);
        earContext = new MockNamingContext(j2eeContext);

        coffee_peaberry = NameFactory.getEjbComponentName(null, null, null, coffee.getPath(), "peaberry", NameFactory.STATELESS_SESSION_BEAN, j2eeContext);
        coffee_java = NameFactory.getEjbComponentName(null, null, null, coffee.getPath(), "java", NameFactory.STATELESS_SESSION_BEAN, j2eeContext);
        language_lisp = NameFactory.getEjbComponentName(null, null, null, language.getPath(), "lisp", NameFactory.STATELESS_SESSION_BEAN, j2eeContext);
        language_java = NameFactory.getEjbComponentName(null, null, null, language.getPath(), "java", NameFactory.STATELESS_SESSION_BEAN, j2eeContext);
        car_gt = NameFactory.getEjbComponentName(null, null, null, car.getPath(), "gt", NameFactory.STATELESS_SESSION_BEAN, j2eeContext);
        car_enzo = NameFactory.getEjbComponentName(null, null, null, car.getPath(), "enzo", NameFactory.STATELESS_SESSION_BEAN, j2eeContext);
    }

    public void testSimpleRefs() throws Exception {
        assertReferenceEqual(coffee_peaberry, refContext.getEJBRemoteRef(coffee, "peaberry", true, null, null, earContext));
        assertReferenceEqual(coffee_peaberry, refContext.getEJBLocalRef(coffee, "peaberry", true, null, null, earContext));
    }

    public void testAmbiguousRefs() throws Exception {
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "java", true, null, null, earContext));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(coffee, "java", true, null, null, earContext));
        assertReferenceEqual(language_java, refContext.getEJBRemoteRef(language, "java", true, null, null, earContext));
        assertReferenceEqual(language_java, refContext.getEJBLocalRef(language, "java", true, null, null, earContext));

        try {
            refContext.getEJBRemoteRef(car, "java", true, null, null, earContext);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
        try {
            refContext.getEJBLocalRef(car, "java", true, null, null, earContext);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
    }

    public void testRelativeRefs() throws Exception {
        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef(coffee, "../../foo/bar/car.jar#enzo", true, null, null, earContext));
        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef(coffee, "../../foo/bar/car.jar#enzo", true, null, null, earContext));
        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef(coffee, "./../funk/../../foo/bar/car.jar#enzo", true, null, null, earContext));
        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef(coffee, "./../funk/../../foo/bar/car.jar#enzo", true, null, null, earContext));
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "./coffee.jar#java", true, null, null, earContext));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(coffee, "./coffee.jar#java", true, null, null, earContext));
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "coffee.jar#java", true, null, null, earContext));
        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(coffee, "coffee.jar#java", true, null, null, earContext));

        try {
            refContext.getEJBRemoteRef(coffee, "not_exist.jar#blah", true, null, null, earContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBLocalRef(coffee, "not_exist.jar#blah", true, null, null, earContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBRemoteRef(coffee, "coffee.jar#blah", true, null, null, earContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBLocalRef(coffee, "coffee.jar#blah", true, null, null, earContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }

        try {
            refContext.getEJBRemoteRef(coffee, "../../../../foo/bar/car.jar#enzo", true, null, null, earContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
        try {
            refContext.getEJBLocalRef(coffee, "../../../../foo/bar/car.jar#enzo", true, null, null, earContext);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnresolvedReferenceException e) {
            // good
        }
    }

    public void xtestBasicImplicitRefs() throws Exception {
        assertReferenceEqual(language_lisp, refContext.getImplicitEJBRemoteRef(coffee, "blah", true, "LispHome", "LispRemote", earContext));
        assertReferenceEqual(language_lisp, refContext.getImplicitEJBLocalRef(coffee, "blah", true, "LispLocalHome", "LispLocal", earContext));
    }

    public void xtestInModuleImplicitRefs() throws Exception {
        assertReferenceEqual(coffee_java, refContext.getImplicitEJBLocalRef(coffee, "blah", true, "LocalHome", "Local", earContext));
        assertReferenceEqual(car_enzo, refContext.getImplicitEJBLocalRef(car, "blah", true, "LocalHome", "Local", earContext));
    }

    public void xtestAmbiguousModuleImplicitRefs() throws Exception {
        try {
            refContext.getImplicitEJBLocalRef(language, "blah", true, "LocalHome", "Local", earContext);
            fail("should have thrown an UnresolvedEJBRefException");
        } catch (UnresolvedEJBRefException e) {
            // good
        }
    }

    public void xtestNoMatchImplicitRefs() throws Exception {
        try {
            refContext.getImplicitEJBLocalRef(language, "blah", true, "foo", "bar", earContext);
            fail("should have thrown an UnresolvedEJBRefException");
        } catch (UnresolvedEJBRefException e) {
            // good
        }
    }

    protected void setUp() throws Exception {

        addEJBRemote(coffee, "peaberry", coffee_peaberry, true, "CoffeeHome", "CoffeeRemote");
//        addEJBLocal(coffee, "peaberry", coffee_peaberry_local, true, "CoffeeLocalHome", "CoffeeLocal");
        addEJBRemote(coffee, "java", coffee_java, true, "CoffeeHome", "CoffeeRemote");
//        addEJBLocal(coffee, "java", coffee_java_local, true, "LocalHome", "Local");

        addEJBRemote(language, "lisp", language_lisp, true, "LispHome", "LispRemote");
//        addEJBLocal(language, "lisp", language_lisp_local, true, "LispLocalHome", "LispLocal");
        addEJBRemote(language, "java", language_java, true, "JavaHome", "JavaRemote");
//        addEJBLocal(language, "java", language_java_local, true, "JavaLocalHome", "JavaLocal");

        addEJBRemote(car, "gt", car_gt, true, "GTHome", "GTRemote");
//        addEJBLocal(car, "gt", car_gt_local, true, "GTLocalHome", "GTLocalRemote");
        addEJBRemote(car, "enzo", car_enzo, true, "EnzoHome", "EnzoRemote");
//        addEJBLocal(car, "enzo", car_enzo_local, true, "LocalHome", "Local");
    }

    private void addEJBRemote(URI modulePath, String name, ObjectName objectName, boolean isSession, String home, String remote ) throws MalformedObjectNameException {
        GBeanData gBeanData = new GBeanData(objectName, null);
        earContext.addGBean(gBeanData);
    }
    private void addEJBRemote(URI modulePath, String name, String containerID, boolean isSession, String home, String remote ) throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(containerID);
        GBeanData gBeanData = new GBeanData(objectName, null);
        earContext.addGBean(gBeanData);
    }

    private void addEJBLocal(URI modulePath, String name, String containerID, boolean isSession, String home, String remote ) throws MalformedObjectNameException {
        ObjectName objectName = new ObjectName(containerID);
        GBeanData gBeanData = new GBeanData(objectName, null);
        earContext.addGBean(gBeanData);
    }

    private void assertReferenceEqual(ObjectName expected, Reference reference) {
        FakeReference fakeReference = (FakeReference) reference;
        String containerId = null;
        if (fakeReference != null) {
            containerId = fakeReference.containerId;
        }
        assertEquals(expected.getCanonicalName(), containerId);
    }
    private void assertReferenceEqual(String expected, Reference reference) {
        FakeReference fakeReference = (FakeReference) reference;
        String containerId = null;
        if (fakeReference != null) {
            containerId = fakeReference.containerId;
        }
        assertEquals(expected, containerId);
    }

    private class FakeReference extends Reference {
        private String containerId;

        public FakeReference(String containerId) {
            super(null);
            this.containerId = containerId;
        }
    }
}
