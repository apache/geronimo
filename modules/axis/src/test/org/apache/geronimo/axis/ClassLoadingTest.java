/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis;

import org.apache.axis.utils.ClassUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author hemapani@opensource.lk
 */
public class ClassLoadingTest extends AbstractTestCase {
    /**
     * @param testName
     */
    public ClassLoadingTest(String testName) {
        super(testName);
    }

    public void testClassLoading() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class class0 = Class.forName("org.apache.axis.utils.tcpmon", true, cl);

        File jarfile = new File(getTestFile("target/generated/samples/echo-ewsimpl.jar"));

        ClassLoader cl1 = new URLClassLoader(new URL[]{jarfile.toURL()}, cl);
        ClassLoader cl2 = new URLClassLoader(new URL[]{jarfile.toURL()}, cl);

        Class class1 = Class.forName("org.apache.ws.echosample.EchoStruct", true, cl1);
        Class class2 = Class.forName("org.apache.ws.echosample.EchoStruct", true, cl2);
        Class class3 = Class.forName("org.apache.ws.echosample.EchoStruct", true, cl1);
        Class class4 = Class.forName("org.apache.axis.utils.tcpmon", true, cl1);

        assertNotSame(class1, class2);
        assertSame(class1, class3);
        assertSame(class0, class4);

        String className = "org.apache.ws.echosample.EchoPortSoapBindingImpl";
        ClassUtils.setClassLoader(className, cl1);
        ClassUtils.forName(className);
    }
}
