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

import java.io.File;
import java.net.URI;
import javax.naming.Reference;

import junit.framework.TestCase;

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
    private File carFile;
    private EJBRefContext ejbRefContext;

    public void testSimpleRefs() throws Exception {
        assertReferenceEqual(coffee_peaberry, ejbRefContext.getEJBRemoteRef(coffee, "peaberry", true, null, null));
        assertReferenceEqual(coffee_peaberry_local, ejbRefContext.getEJBLocalRef(coffee, "peaberry", true, null, null));
    }

    public void testAmbiguousRefs() throws Exception {
        assertReferenceEqual(coffee_java, ejbRefContext.getEJBRemoteRef(coffee, "java", true, null, null));
        assertReferenceEqual(coffee_java_local, ejbRefContext.getEJBLocalRef(coffee, "java", true, null, null));
        assertReferenceEqual(language_java, ejbRefContext.getEJBRemoteRef(language, "java", true, null, null));
        assertReferenceEqual(language_java_local, ejbRefContext.getEJBLocalRef(language, "java", true, null, null));

        try {
            ejbRefContext.getEJBRemoteRef(car, "java", true, null, null);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (AmbiguousEJBRefException e) {
            // good
        }
        try {
            ejbRefContext.getEJBLocalRef(car, "java", true, null, null);
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (AmbiguousEJBRefException e) {
            // good
        }
    }

    public void testRelativeRefs() throws Exception {
        assertReferenceEqual(car_enzo, ejbRefContext.getEJBRemoteRef(coffee, "../../foo/bar/car.jar#enzo", true, null, null));
        assertReferenceEqual(car_enzo_local, ejbRefContext.getEJBLocalRef(coffee, "../../foo/bar/car.jar#enzo", true, null, null));
        assertReferenceEqual(car_enzo, ejbRefContext.getEJBRemoteRef(coffee, "./../funk/../../foo/bar/car.jar#enzo", true, null, null));
        assertReferenceEqual(car_enzo_local, ejbRefContext.getEJBLocalRef(coffee, "./../funk/../../foo/bar/car.jar#enzo", true, null, null));
        assertReferenceEqual(coffee_java, ejbRefContext.getEJBRemoteRef(coffee, "./coffee.jar#java", true, null, null));
        assertReferenceEqual(coffee_java_local, ejbRefContext.getEJBLocalRef(coffee, "./coffee.jar#java", true, null, null));
        assertReferenceEqual(coffee_java, ejbRefContext.getEJBRemoteRef(coffee, "coffee.jar#java", true, null, null));
        assertReferenceEqual(coffee_java_local, ejbRefContext.getEJBLocalRef(coffee, "coffee.jar#java", true, null, null));

        try {
            ejbRefContext.getEJBRemoteRef(coffee, "not_exist.jar#blah", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            ejbRefContext.getEJBLocalRef(coffee, "not_exist.jar#blah", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            ejbRefContext.getEJBRemoteRef(coffee, "coffee.jar#blah", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            ejbRefContext.getEJBLocalRef(coffee, "coffee.jar#blah", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            ejbRefContext.getEJBRemoteRef(coffee, "../../../../foo/bar/car.jar#enzo", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }
        try {
            ejbRefContext.getEJBLocalRef(coffee, "../../../../foo/bar/car.jar#enzo", true, null, null);
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }
    }

    protected void setUp() throws Exception {
        carFile = File.createTempFile("EARTest", ".car");
        ejbRefContext = new EJBRefContext(new EJBReferenceBuilder() {
            public Reference createEJBLocalReference(String objectName, boolean isSession, String localHome, String local) {
                return new FakeReference(objectName);
            }

            public Reference createEJBRemoteReference(String objectName, boolean isSession, String home, String remote) {
                return new FakeReference(objectName);
            }
        });

        ejbRefContext.addEJBRemoteId(coffee, "peaberry", coffee_peaberry);
        ejbRefContext.addEJBLocalId(coffee, "peaberry", coffee_peaberry_local);
        ejbRefContext.addEJBRemoteId(coffee, "java", coffee_java);
        ejbRefContext.addEJBLocalId(coffee, "java", coffee_java_local);

        ejbRefContext.addEJBRemoteId(language, "lisp", language_lisp);
        ejbRefContext.addEJBLocalId(language, "lisp", language_lisp_local);
        ejbRefContext.addEJBRemoteId(language, "java", language_java);
        ejbRefContext.addEJBLocalId(language, "java", language_java_local);

        ejbRefContext.addEJBRemoteId(car, "gt", car_gt);
        ejbRefContext.addEJBLocalId(car, "gt", car_gt_local);
        ejbRefContext.addEJBRemoteId(car, "enzo", car_enzo);
        ejbRefContext.addEJBLocalId(car, "enzo", car_enzo_local);
    }

    protected void tearDown() throws Exception {
        carFile.delete();
    }

    private void assertReferenceEqual(String expected, Reference reference) {
        FakeReference fakeReference = (FakeReference)reference;
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
