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
import javax.naming.Reference;
import javax.management.ObjectName;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.AmbiguousEJBRefException;
import org.apache.geronimo.common.UnknownEJBRefException;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.deployment.DeploymentContext;

/**
 * @version $Rev$ $Date$
 */
public class EJBRefContextTest extends TestCase {
    private final URI coffee = URI.create("some/path/coffee.jar");
    private final URI language = URI.create("some/where/language.jar");
    private final URI car = URI.create("foo/bar/car.jar");

    private final String coffee_peaberry = "coffee_peaberry";
    private final String coffee_peaberry_local = "coffee_peaberry_Local";
    private final String coffee_java = "coffee_java";
    private final String coffee_java_local = "coffee_java_local";

    private final String language_lisp = "language_lisp";
    private final String language_lisp_local = "language_lisp_local";
    private final String language_java = "language_java";
    private final String language_java_local = "language_java_local";

    private final String car_gt = "car_gt";
    private final String car_gt_local = "car_gt_local";
    private final String car_enzo = "car_enzo";
    private final String car_enzo_local = "car_enzo_local";
    private RefContext refContext;

    public void testSimpleRefs() throws Exception {
        assertReferenceEqual(coffee_peaberry, refContext.getEJBRemoteRef(coffee, "peaberry", true, null, null));
        assertReferenceEqual(coffee_peaberry_local, refContext.getEJBLocalRef(coffee, "peaberry", true, null, null));
    }

    public void testAmbiguousRefs() throws Exception {
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "java", true, null, null));
        assertReferenceEqual(coffee_java_local, refContext.getEJBLocalRef(coffee, "java", true, null, null));
        assertReferenceEqual(language_java, refContext.getEJBRemoteRef(language, "java", true, null, null));
        assertReferenceEqual(language_java_local, refContext.getEJBLocalRef(language, "java", true, null, null));

        try {
            refContext.getEJBRemoteRef(car, "java", true, null, null);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (AmbiguousEJBRefException e) {
            // good
        }
        try {
            refContext.getEJBLocalRef(car, "java", true, null, null);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (AmbiguousEJBRefException e) {
            // good
        }
    }

    public void testRelativeRefs() throws Exception {
        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef(coffee, "../../foo/bar/car.jar#enzo", true, null, null));
        assertReferenceEqual(car_enzo_local, refContext.getEJBLocalRef(coffee, "../../foo/bar/car.jar#enzo", true, null, null));
        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef(coffee, "./../funk/../../foo/bar/car.jar#enzo", true, null, null));
        assertReferenceEqual(car_enzo_local, refContext.getEJBLocalRef(coffee, "./../funk/../../foo/bar/car.jar#enzo", true, null, null));
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "./coffee.jar#java", true, null, null));
        assertReferenceEqual(coffee_java_local, refContext.getEJBLocalRef(coffee, "./coffee.jar#java", true, null, null));
        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(coffee, "coffee.jar#java", true, null, null));
        assertReferenceEqual(coffee_java_local, refContext.getEJBLocalRef(coffee, "coffee.jar#java", true, null, null));

        try {
            refContext.getEJBRemoteRef(coffee, "not_exist.jar#blah", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            refContext.getEJBLocalRef(coffee, "not_exist.jar#blah", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            refContext.getEJBRemoteRef(coffee, "coffee.jar#blah", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            refContext.getEJBLocalRef(coffee, "coffee.jar#blah", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            refContext.getEJBRemoteRef(coffee, "../../../../foo/bar/car.jar#enzo", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }
        try {
            refContext.getEJBLocalRef(coffee, "../../../../foo/bar/car.jar#enzo", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }
    }

    public void testBasicImplicitRefs() throws Exception {
        assertReferenceEqual(language_lisp, refContext.getImplicitEJBRemoteRef(coffee, "blah", true, "LispHome", "LispRemote"));
        assertReferenceEqual(language_lisp_local, refContext.getImplicitEJBLocalRef(coffee, "blah", true, "LispLocalHome", "LispLocal"));
    }

    public void testInModuleImplicitRefs() throws Exception {
        assertReferenceEqual(coffee_java_local, refContext.getImplicitEJBLocalRef(coffee, "blah", true, "LocalHome", "Local"));
        assertReferenceEqual(car_enzo_local, refContext.getImplicitEJBLocalRef(car, "blah", true, "LocalHome", "Local"));
    }

    public void testAmbiguousModuleImplicitRefs() throws Exception {
        try {
            refContext.getImplicitEJBLocalRef(language, "blah", true, "LocalHome", "Local");
            fail("should have thrown an UnresolvedEJBRefException");
        } catch (UnresolvedEJBRefException e) {
            // good
        }
    }

    public void testNoMatchImplicitRefs() throws Exception {
        try {
            refContext.getImplicitEJBLocalRef(language, "blah", true, "foo", "bar");
            fail("should have thrown an UnresolvedEJBRefException");
        } catch (UnresolvedEJBRefException e) {
            // good
        }
    }

    protected void setUp() throws Exception {
        refContext = new RefContext(new EJBReferenceBuilder() {
            public Reference createEJBLocalReference(String objectName, boolean isSession, String localHome, String local) {
                return new FakeReference(objectName);
            }

            public Reference createEJBRemoteReference(String objectName, boolean isSession, String home, String remote) {
                return new FakeReference(objectName);
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

            public GBeanData locateActivationSpecInfo(ObjectName resourceAdapterName, String messageListenerInterface) throws DeploymentException {
                return null;
            }

            public GBeanData locateResourceAdapterGBeanData(ObjectName resourceAdapterModuleName) throws DeploymentException {
                return null;
            }

            public GBeanData locateAdminObjectInfo(ObjectName resourceAdapterModuleName, String adminObjectInterfaceName) throws DeploymentException {
                return null;
            }

            public GBeanData locateConnectionFactoryInfo(ObjectName resourceAdapterModuleName, String connectionFactoryInterfaceName) throws DeploymentException {
                return null;
            }
        }, new ServiceReferenceBuilder() {
            //it could return a Service or a Reference, we don't care
            public Object createService(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlers, DeploymentContext deploymentContext, ClassLoader classLoader) throws DeploymentException {
                return null;
            }
        });

        refContext.addEJBRemoteId(coffee, "peaberry", coffee_peaberry, true, "CoffeeHome", "CoffeeRemote");
        refContext.addEJBLocalId(coffee, "peaberry", coffee_peaberry_local, true, "CoffeeLocalHome", "CoffeeLocal");
        refContext.addEJBRemoteId(coffee, "java", coffee_java, true, "CoffeeHome", "CoffeeRemote");
        refContext.addEJBLocalId(coffee, "java", coffee_java_local, true, "LocalHome", "Local");

        refContext.addEJBRemoteId(language, "lisp", language_lisp, true, "LispHome", "LispRemote");
        refContext.addEJBLocalId(language, "lisp", language_lisp_local, true, "LispLocalHome", "LispLocal");
        refContext.addEJBRemoteId(language, "java", language_java, true, "JavaHome", "JavaRemote");
        refContext.addEJBLocalId(language, "java", language_java_local, true, "JavaLocalHome", "JavaLocal");

        refContext.addEJBRemoteId(car, "gt", car_gt, true, "GTHome", "GTRemote");
        refContext.addEJBLocalId(car, "gt", car_gt_local, true, "GTLocalHome", "GTLocalRemote");
        refContext.addEJBRemoteId(car, "enzo", car_enzo, true, "EnzoHome", "EnzoRemote");
        refContext.addEJBLocalId(car, "enzo", car_enzo_local, true, "LocalHome", "Local");
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
