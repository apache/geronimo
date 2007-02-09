/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.util;

import java.util.Arrays;
import java.util.List;
import javax.rmi.CORBA.Stub;

import junit.framework.TestCase;

/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class DynamicStubClassLoaderTest extends TestCase {
    public void testGeneration() throws Exception {
        DynamicStubClassLoader dynamicStubClassLoader = new DynamicStubClassLoader();
        dynamicStubClassLoader.doStart();
        Class c = dynamicStubClassLoader.loadClass("org.omg.stub.org.apache.geronimo.corba.compiler._Simple_Stub");
        verifyStub(c);
        verifyStub(c);
        verifyStub(c);
        verifyStub(c);

    }

    private void verifyStub(final Class c) throws Exception {
        final Exception[] exception = new Exception[1];
        Runnable verify = new Runnable() {
            public void run() {
                try {
                    Stub stub = (Stub) c.newInstance();
                    String[] strings = stub._ids();
                    assertNotNull(strings);
                    assertEquals(2, strings.length);
                    List ids = Arrays.asList(strings);
                    assertTrue(ids.contains("RMI:org.apache.geronimo.corba.compiler.Simple:0000000000000000"));
                    assertTrue(ids.contains("RMI:org.apache.geronimo.corba.compiler.Special:0000000000000000"));
                } catch (Exception e) {
                    exception[0] = e;
                }
            }
        };
        Thread thread = new Thread(verify);
        thread.start();
        thread.join();
        if (exception[0] != null) {
            throw exception[0];
        }
    }
}
