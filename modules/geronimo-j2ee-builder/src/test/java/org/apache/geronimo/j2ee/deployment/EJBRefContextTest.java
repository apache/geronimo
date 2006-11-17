/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import javax.management.ObjectName;
import javax.naming.Reference;

import junit.framework.TestCase;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Jsr77Naming;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;

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

    private Configuration configuration;

    public void testNothing() throws Exception {}

//    public void xtestSimpleRefs() throws Exception {
//        assertReferenceEqual(coffee_peaberry, refContext.getEJBRemoteRef(null, configuration, "peaberry", null, coffee, null, null, true, null, null));
//        assertReferenceEqual(coffee_peaberry, refContext.getEJBLocalRef(null, configuration, "peaberry", null, coffee, null, null, true, null, null));
//    }
//
//    public void xtestAmbiguousRefs() throws Exception {
//        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(null, configuration, "java", null, coffee, null, null, true, null, null));
//        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(null, configuration, "java", null, coffee, null, null, true, null, null));
//        assertReferenceEqual(language_java, refContext.getEJBRemoteRef(null, configuration, "java", null, language, null, null, true, null, null));
//        assertReferenceEqual(language_java, refContext.getEJBLocalRef(null, configuration, "java", null, language, null, null, true, null, null));
//
//        try {
//            refContext.getEJBRemoteRef(null, configuration, "java", null, car, null, null, true, null, null);
//            fail("should have thrown an AmbiguousEJBRefException");
//        } catch (UnresolvedReferenceException e) {
//            // good
//        }
//        try {
//            refContext.getEJBLocalRef(null, configuration, "java", null, car, null, null, true, null, null);
//            fail("should have thrown an AmbiguousEJBRefException");
//        } catch (UnresolvedReferenceException e) {
//            // good
//        }
//    }
//
//    public void xtestRelativeRefs() throws Exception {
//        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef(null, configuration, "enzo", "../../foo/bar/car.jar", coffee, null, null, true, null, null));
//        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef(null, configuration, "enzo", "../../foo/bar/car.jar", coffee, null, null, true, null, null));
//        assertReferenceEqual(car_enzo, refContext.getEJBRemoteRef(null, configuration, "enzo", "./../funk/../../foo/bar/car.jar", coffee, null, null, true, null, null));
//        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef(null, configuration, "enzo", "./../funk/../../foo/bar/car.jar", coffee, null, null, true, null, null));
//        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(null, configuration, "java", "./coffee.jar", coffee, null, null, true, null, null));
//        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(null, configuration, "java", "./coffee.jar", coffee, null, null, true, null, null));
//        assertReferenceEqual(coffee_java, refContext.getEJBRemoteRef(null, configuration, "java", "coffee.jar", coffee, null, null, true, null, null));
//        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(null, configuration, "java", "coffee.jar", coffee, null, null, true, null, null));
//
//        try {
//            refContext.getEJBRemoteRef(null, configuration, "blah", "not_exist.jar", coffee, null, null, true, null, null);
//            fail("should have thrown an UnknownEJBRefException");
//        } catch (UnresolvedReferenceException e) {
//            // good
//        }
//
//        try {
//            refContext.getEJBLocalRef(null, configuration, "blah", "not_exist.jar", coffee, null, null, true, null, null);
//            fail("should have thrown an UnknownEJBRefException");
//        } catch (UnresolvedReferenceException e) {
//            // good
//        }
//
//        try {
//            refContext.getEJBRemoteRef(null, configuration, "blah", "coffee.jar", coffee, null, null, true, null, null);
//            fail("should have thrown an UnknownEJBRefException");
//        } catch (UnresolvedReferenceException e) {
//            // good
//        }
//
//        try {
//            refContext.getEJBLocalRef(null, configuration, "blah", "coffee.jar", coffee, null, null, true, null, null);
//            fail("should have thrown an UnknownEJBRefException");
//        } catch (UnresolvedReferenceException e) {
//            // good
//        }
//
//        try {
//            refContext.getEJBRemoteRef(null, configuration, "enzo", "../../../../foo/bar/car.jar",coffee, null, null, true, null, null);
//            fail("should have thrown an UnknownEJBRefException");
//        } catch (UnresolvedReferenceException e) {
//            // good
//        }
//        try {
//            refContext.getEJBLocalRef(null, configuration, "enzo", "../../../../foo/bar/car.jar",coffee, null, null, true, null, null);
//            fail("should have thrown an UnknownEJBRefException");
//        } catch (UnresolvedReferenceException e) {
//            // good
//        }
//    }
//
//    public void xtestBasicImplicitRefs() throws Exception {
//        assertReferenceEqual(language_lisp, refContext.getEJBRemoteRef(null, configuration, "blah", null, coffee, null, null, true, "LispHome", "LispRemote"));
//        assertReferenceEqual(language_lisp, refContext.getEJBLocalRef(null, configuration, "blah", null, coffee, null, null, true, "LispLocalHome", "LispLocal"));
//    }
//
//    public void xtestInModuleImplicitRefs() throws Exception {
//        assertReferenceEqual(coffee_java, refContext.getEJBLocalRef(null, configuration, "blah", null, coffee, null, null, true, "LocalHome", "Local"));
//        assertReferenceEqual(car_enzo, refContext.getEJBLocalRef(null, configuration, "blah", null, car, null, null, true, "LocalHome", "Local"));
//    }
//
//    public void xtestAmbiguousModuleImplicitRefs() throws Exception {
//        try {
//            refContext.getEJBLocalRef(null, configuration, "blah", null, language, null, null, true, "LocalHome", "Local");
//            fail("should have thrown an UnresolvedEJBRefException");
//        } catch (UnresolvedEJBRefException e) {
//            // good
//        }
//    }
//
//    public void xtestNoMatchImplicitRefs() throws Exception {
//        try {
//            refContext.getEJBLocalRef(null, configuration, "blah", null, language, null, null, true, "foo", "bar");
//            fail("should have thrown an UnresolvedEJBRefException");
//        } catch (UnresolvedEJBRefException e) {
//            // good
//        }
//    }
//
    protected void setUp() throws Exception {
        super.setUp();

        Naming naming = new Jsr77Naming();
        AbstractName applicationName = naming.createRootName(new Artifact("test", "stuff", "", "ear"), "app", NameFactory.J2EE_APPLICATION) ;


        AbstractName coffeeName = naming.createChildName(applicationName, coffee, NameFactory.EJB_MODULE);
        coffee_peaberry = naming.createChildName(coffeeName, "peaberry", NameFactory.STATELESS_SESSION_BEAN);
        coffee_java = naming.createChildName(coffeeName, "java", NameFactory.STATELESS_SESSION_BEAN);

        AbstractName languageName = naming.createChildName(applicationName, language, NameFactory.EJB_MODULE);
        language_lisp = naming.createChildName(languageName, "lisp", NameFactory.STATELESS_SESSION_BEAN);
        language_java = naming.createChildName(languageName, "java", NameFactory.STATELESS_SESSION_BEAN);

        AbstractName carName = naming.createChildName(applicationName, car, NameFactory.EJB_MODULE);
        AbstractName car_gt = naming.createChildName(carName, "gt", NameFactory.STATELESS_SESSION_BEAN);
        car_enzo = naming.createChildName(carName, "enzo", NameFactory.STATELESS_SESSION_BEAN);

//        configuration = null;        
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
        assertEquals(expected.toURI().toString(), containerId);
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

    private static class MockActivationSpecInfoLocator implements ActivationSpecInfoLocator {
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

}
