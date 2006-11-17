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
package org.apache.geronimo.jmxremoting;

import java.util.Arrays;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.TextOutputCallback;

import junit.framework.TestCase;

/**
 *
 *
 * @version $Rev$ $Date$
 */
public class CredentialsTest extends TestCase {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "PASSWORD";
    private final Credentials credentials = new Credentials(USERNAME, PASSWORD);

    public void testCallbacks() {
        try {
            NameCallback nameCallback = new NameCallback("user");
            PasswordCallback passwordCallback = new PasswordCallback("password", false);
            Callback[] callbacks = new Callback[]{nameCallback, passwordCallback};
            credentials.handle(callbacks);
            assertEquals(USERNAME, nameCallback.getName());
            assertEquals(PASSWORD, new String(passwordCallback.getPassword()));
        } catch (UnsupportedCallbackException e) {
            fail();
        }

        try {
            credentials.handle(new Callback[]{ new TextOutputCallback(0, "foo")});
            fail();
        } catch (UnsupportedCallbackException e) {
            // ok
        }
    }

    public void testClear() {
        credentials.clear();
        NameCallback nameCallback = new NameCallback("user");
        PasswordCallback passwordCallback = new PasswordCallback("password", false);
        Callback[] callbacks = new Callback[]{nameCallback, passwordCallback};
        try {
            credentials.handle(callbacks);
        } catch (UnsupportedCallbackException e) {
            fail();
        }
        assertEquals(USERNAME, nameCallback.getName());
        assertTrue(Arrays.equals(new char[]{0,0,0,0,0,0,0,0}, passwordCallback.getPassword()));
    }

    protected void setUp() throws Exception {
        super.setUp();
    }
}
