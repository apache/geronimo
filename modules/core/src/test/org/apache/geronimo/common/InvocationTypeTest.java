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

package org.apache.geronimo.common;

import org.apache.geronimo.core.service.InvocationType;

import junit.framework.TestCase;

/**
 * Unit test for org.apache.geronimo.core.service.InvocationType
 *
 * @version $Rev$ $Date$
 */

public class InvocationTypeTest extends TestCase {

    public void testRemote() {

        InvocationType invocationType = InvocationType.REMOTE;
        assertTrue(invocationType.isRemoteInvocation());
        assertFalse(invocationType.isLocalInvocation());
        assertFalse(invocationType.isHomeInvocation());
        assertTrue(invocationType.isBeanInvocation());

        assertEquals("REMOTE", invocationType.toString());
    }

    public void testHome() {

        InvocationType invocationType = InvocationType.HOME;
        assertTrue(invocationType.isRemoteInvocation());
        assertFalse(invocationType.isLocalInvocation());
        assertTrue(invocationType.isHomeInvocation());
        assertFalse(invocationType.isBeanInvocation());

        assertEquals("HOME", invocationType.toString());
    }

    public void testLocal() {

        InvocationType invocationType = InvocationType.LOCAL;
        assertFalse(invocationType.isRemoteInvocation());
        assertTrue(invocationType.isLocalInvocation());
        assertFalse(invocationType.isHomeInvocation());
        assertTrue(invocationType.isBeanInvocation());

        assertEquals("LOCAL", invocationType.toString());
    }

    public void testLocalHome() {

        InvocationType invocationType = InvocationType.LOCALHOME;
        assertTrue(invocationType.isRemoteInvocation());
        assertFalse(invocationType.isLocalInvocation());
        assertFalse(invocationType.isHomeInvocation());
        assertTrue(invocationType.isBeanInvocation());

        assertEquals("LOCALHOME", invocationType.toString());
    }
}
