/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.util;

import junit.framework.TestCase;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class SimpleEncryptionTest extends TestCase {
    public void testSimpleEncryption() {
        Object[] source = new Object[]{"This is a test", new Integer(14)};
        String text = SimpleEncryption.encrypt(source);
        Object[] result = (Object[]) SimpleEncryption.decrypt(text);
        assertEquals(2, result.length);
        assertEquals("This is a test", result[0]);
        assertEquals(new Integer(14), result[1]);
    }
}
