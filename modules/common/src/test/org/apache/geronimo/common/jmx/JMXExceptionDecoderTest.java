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

package org.apache.geronimo.common.jmx;

import junit.framework.TestCase;

import javax.management.ReflectionException;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;


/**
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:04 $
 */
public class JMXExceptionDecoderTest extends TestCase {
    private void testDecode(Throwable t, Throwable expected) {
        assertEquals(expected, JMXExceptionDecoder.decode(t));
    }

    public void testDecodeMBeanException() {
        Exception nested = new Exception();
        MBeanException ex = new MBeanException(nested);
        testDecode(ex, nested);
    }

    public void testDecodeReflectionException() {
        Exception nested = new Exception();
        ReflectionException ex = new ReflectionException(nested);
        testDecode(ex, nested);
    }

    public void testDecodeRuntimeOperationsException() {
        RuntimeException nested = new RuntimeException();
        RuntimeOperationsException ex = new RuntimeOperationsException(nested);
        testDecode(ex, nested);
    }

    public void testDecodeRuntimeMBeanException() {
        RuntimeException nested = new RuntimeException();
        RuntimeMBeanException ex = new RuntimeMBeanException(nested);
        testDecode(ex, nested);
    }

    public void testDecodeRuntimeErrorException() {
        Error nested = new Error();
        RuntimeErrorException ex = new RuntimeErrorException(nested);
        testDecode(ex, nested);
    }

    public void testDecodeOther() {
        Exception ex = new Exception();
        testDecode(ex, ex);
    }

    private void testRethrow(Exception t, Throwable expected) {
        try {
            JMXExceptionDecoder.rethrow(t);
            fail();
        } catch (Throwable ex) {
            assertEquals(expected, ex);
        }
    }

    public void testRethrowMBeanException() {
        Exception nested = new Exception();
        MBeanException ex = new MBeanException(nested);
        testRethrow(ex, nested);
    }

    public void testRethrowReflectionException() {
        Exception nested = new Exception();
        ReflectionException ex = new ReflectionException(nested);
        testRethrow(ex, nested);
    }

    public void testRethrowRuntimeOperationsException() {
        RuntimeException nested = new RuntimeException();
        RuntimeOperationsException ex = new RuntimeOperationsException(nested);
        testRethrow(ex, nested);
    }

    public void testRethrowRuntimeMBeanException() {
        RuntimeException nested = new RuntimeException();
        RuntimeMBeanException ex = new RuntimeMBeanException(nested);
        testRethrow(ex, nested);
    }

    public void testRethrowRuntimeErrorException() {
        Error nested = new Error();
        RuntimeErrorException ex = new RuntimeErrorException(nested);
        testRethrow(ex, nested);
    }

    public void testRethrowOther() {
        Exception ex = new Exception();
        testRethrow(ex, ex);
    }

}
