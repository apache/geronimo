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
package org.apache.geronimo.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentExceptionTest extends TestCase {
    
    public void testCleanse() throws Exception {
        DeploymentException exception = new DeploymentException("message");
        IllegalArgumentException nested1 = new IllegalArgumentException("nested1");
        exception.initCause(nested1);
        IllegalStateException nested2 = new IllegalStateException("nested2");
        nested1.initCause(nested2);
        
        ByteArrayOutputStream expected = new ByteArrayOutputStream();
        exception.printStackTrace(new PrintStream(expected));

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        DeploymentException newEx = exception.cleanse();
        newEx.printStackTrace(new PrintStream(result));

        byte[] expectedBytes = expected.toByteArray();
        byte[] resultBytes = result.toByteArray();
        assertEquals(expectedBytes.length, resultBytes.length);
        for (int i = 0; i < expectedBytes.length; i++) {
            assertEquals(expectedBytes[i], resultBytes[i]);
        }
    }
}
