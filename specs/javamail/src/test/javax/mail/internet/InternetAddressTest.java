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

package javax.mail.internet;
import junit.framework.TestCase;
/**
 * @version $Rev$ $Date$
 */
public class InternetAddressTest extends TestCase {
    private InternetAddress address;

    public void testIsGroup() {
        address.setAddress(":user@host;");
        assertTrue(address.isGroup());

        address.setAddress(":user@host, user2@host;");
        assertTrue(address.isGroup());

        address.setAddress("User Group :user@host;");
        assertTrue(address.isGroup());

        address.setAddress("A \"User Group\" :user@host;");
        assertTrue(address.isGroup());

        address.setAddress("\"Fake:Group\" user@host");
        assertFalse(address.isGroup());
    }

    protected void setUp() throws Exception {
        address = new InternetAddress();
    }
}
