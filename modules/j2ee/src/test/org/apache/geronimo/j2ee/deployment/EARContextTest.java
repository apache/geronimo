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
import java.io.FileOutputStream;
import java.net.URI;
import java.util.jar.JarOutputStream;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.config.ConfigurationModuleType;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.5 $ $Date: 2004/08/06 22:44:37 $
 */
public class EARContextTest extends TestCase {
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
    private EARContext earContext;

    public void testSimpleRefs() throws Exception {
        assertEquals(coffee_peaberry, earContext.getEJBRef(coffee, "peaberry"));
        assertEquals(coffee_peaberry_local, earContext.getEJBLocalRef(coffee, "peaberry"));
    }

    public void testAmbiguousRefs() throws Exception {
        assertEquals(coffee_java, earContext.getEJBRef(coffee, "java"));
        assertEquals(coffee_java_local, earContext.getEJBLocalRef(coffee, "java"));
        assertEquals(language_java, earContext.getEJBRef(language, "java"));
        assertEquals(language_java_local, earContext.getEJBLocalRef(language, "java"));

        try {
            earContext.getEJBRef(car, "java");
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (AmbiguousEJBRefException e) {
            // good
        }
        try {
            earContext.getEJBLocalRef(car, "java");
            fail("should have thrown an AmbiguousEJBRefException");
        } catch (AmbiguousEJBRefException e) {
            // good
        }
    }

    public void testRelativeRefs() throws Exception {
        assertEquals(car_enzo, earContext.getEJBRef(coffee, "../../foo/bar/car.jar#enzo"));
        assertEquals(car_enzo_local, earContext.getEJBLocalRef(coffee, "../../foo/bar/car.jar#enzo"));
        assertEquals(car_enzo, earContext.getEJBRef(coffee, "./../funk/../../foo/bar/car.jar#enzo"));
        assertEquals(car_enzo_local, earContext.getEJBLocalRef(coffee, "./../funk/../../foo/bar/car.jar#enzo"));
        assertEquals(coffee_java, earContext.getEJBRef(coffee, "./coffee.jar#java"));
        assertEquals(coffee_java_local, earContext.getEJBLocalRef(coffee, "./coffee.jar#java"));
        assertEquals(coffee_java, earContext.getEJBRef(coffee, "coffee.jar#java"));
        assertEquals(coffee_java_local, earContext.getEJBLocalRef(coffee, "coffee.jar#java"));

        try {
            earContext.getEJBRef(coffee, "not_exist.jar#blah");
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            earContext.getEJBLocalRef(coffee, "not_exist.jar#blah");
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            earContext.getEJBRef(coffee, "coffee.jar#blah");
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            earContext.getEJBLocalRef(coffee, "coffee.jar#blah");
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }

        try {
            earContext.getEJBRef(coffee, "../../../../foo/bar/car.jar#enzo");
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }
        try {
            earContext.getEJBLocalRef(coffee, "../../../../foo/bar/car.jar#enzo");
            fail("should have thrown an UnknownEJBRefException");
        } catch (UnknownEJBRefException e) {
            // good
        }
    }

    protected void setUp() throws Exception {
        carFile = File.createTempFile("EARTest", ".car");
        earContext = new EARContext(new JarOutputStream(new FileOutputStream(carFile)),
                URI.create("configId"),
                ConfigurationModuleType.EAR,
                URI.create("parentId"),
                null,
                "j2eeDomain",
                "j2eeServer",
                "j2eeApplicationName",
                ObjectName.getInstance("j2eeDomain:type=TransactionManager"),
                ObjectName.getInstance("j2eeDomain:type=ConnectionTracker"),
                ObjectName.getInstance("j2eeDomain:type=TransactionalTimer"),
                ObjectName.getInstance("j2eeDomain:type=NonTransactionalTimer"),
                null);

        earContext.addEJBRef(coffee, "peaberry", coffee_peaberry);
        earContext.addEJBLocalRef(coffee, "peaberry", coffee_peaberry_local);
        earContext.addEJBRef(coffee, "java", coffee_java);
        earContext.addEJBLocalRef(coffee, "java", coffee_java_local);

        earContext.addEJBRef(language, "lisp", language_lisp);
        earContext.addEJBLocalRef(language, "lisp", language_lisp_local);
        earContext.addEJBRef(language, "java", language_java);
        earContext.addEJBLocalRef(language, "java", language_java_local);

        earContext.addEJBRef(car, "gt", car_gt);
        earContext.addEJBLocalRef(car, "gt", car_gt_local);
        earContext.addEJBRef(car, "enzo", car_enzo);
        earContext.addEJBLocalRef(car, "enzo", car_enzo_local);
    }

    protected void tearDown() throws Exception {
        carFile.delete();
    }
}
