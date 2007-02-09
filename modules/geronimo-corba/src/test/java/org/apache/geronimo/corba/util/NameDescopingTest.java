/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.corba.util;

import java.io.UnsupportedEncodingException;

import org.apache.geronimo.corba.util.Util;
import junit.framework.TestCase;

/**
 * @version $Rev: 503493 $ $Date: 2007-02-04 13:47:55 -0800 (Sun, 04 Feb 2007) $
 */
public class NameDescopingTest extends TestCase {

    public void testDomainRemoval() throws Exception {
        String scopedName = "username@domain";
        String expected = "username";
        test(scopedName, expected);
    }
    public void testAt() throws Exception {
        String scopedName = "user\\\\name@domain";
        String expected = "user\\name";
        test(scopedName, expected);
    }
    public void testBackslash() throws Exception {
        String scopedName = "user\\@name@domain";
        String expected = "user@name";
        test(scopedName, expected);
    }
    public void testNoDomainRemoval() throws Exception {
        String scopedName = "username";
        String expected = "username";
        test(scopedName, expected);
    }
    public void testNoUsername() throws Exception {
        String scopedName = "@domain";
        String expected = "";
        test(scopedName, expected);
    }

    private void test(String scopedName, String expected) throws UnsupportedEncodingException {
        String user = Util.extractUserNameFromScopedName(scopedName.getBytes());
        assertEquals(expected, user);
    }

    public void testBuildScoped() throws Exception {
        assertEquals("username@domain", Util.buildScopedUserName("username", "domain"));
        assertEquals("user\\@name@domain", Util.buildScopedUserName("user@name", "domain"));
        assertEquals("username@do\\@main", Util.buildScopedUserName("username", "do@main"));
        assertEquals("user\\\\name@domain", Util.buildScopedUserName("user\\name", "domain"));
        assertEquals("username@do\\\\main", Util.buildScopedUserName("username", "do\\main"));
        assertEquals("username", Util.buildScopedUserName("username", null));
        assertEquals("@domain", Util.buildScopedUserName(null, "domain"));
    }
}
