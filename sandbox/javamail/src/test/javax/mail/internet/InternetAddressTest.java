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
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:30 $
 */
public class InternetAddressTest extends TestCase {
    public InternetAddressTest(String arg0) {
        super(arg0);
    }
    public void testInternetAddress() throws AddressException {
        InternetAddress ia =
            new InternetAddress("Alex Blewitt <Alex.Blewitt@bigboy.com>");
        assertEquals("Alex Blewitt", ia.getPersonal());
        assertEquals("Alex.Blewitt@bigboy.com", ia.getAddress());
    }
    public void testInternetAddresses() throws AddressException {
        InternetAddress[] ia =
            InternetAddress.parse(
                "Mr B <Mr.B@bigboy.com>, Mrs B <Mrs.B@biggirl.com>, Milly <Milly@thedog.com>");
        assertEquals(3, ia.length);
        assertEquals("Mr B", ia[0].getPersonal());
        assertEquals("Mr.B@bigboy.com", ia[0].getAddress());
        assertEquals("Mrs B", ia[1].getPersonal());
        assertEquals("Mrs.B@biggirl.com", ia[1].getAddress());
        assertEquals("Milly", ia[2].getPersonal());
        assertEquals("Milly@thedog.com", ia[2].getAddress());
    }
}
