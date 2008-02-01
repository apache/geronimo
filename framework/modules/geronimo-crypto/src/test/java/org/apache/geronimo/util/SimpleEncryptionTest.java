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
package org.apache.geronimo.crypto;

import org.apache.geronimo.testsupport.TestSupport;

/**
 * @version $Rev$ $Date$
 */
public class SimpleEncryptionTest extends TestSupport {
    public void testSimpleEncryption() {
        Object[] source = new Object[]{"This is a test", new Integer(14)};
        String text = SimpleEncryption.INSTANCE.encrypt(source);
        Object[] result = (Object[]) SimpleEncryption.INSTANCE.decrypt(text);
        assertEquals(2, result.length);
        assertEquals("This is a test", result[0]);
        assertEquals(new Integer(14), result[1]);
    }
}
