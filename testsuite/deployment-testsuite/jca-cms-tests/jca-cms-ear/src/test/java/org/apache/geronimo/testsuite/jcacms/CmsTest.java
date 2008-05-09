/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.testsuite.jcacms;

import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Test
public class CmsTest
    extends SeleniumTestSupport
{
    @Test
    public void testPageContent1() throws Exception {
        selenium.open("http://localhost:8080/jca-cms/default-subject-servlet");
        waitForPageLoad();

        String body = selenium.getText("xpath=/html/body");

        assertTrue(body.endsWith(
//                "Current subject: Subject:\n" +
//                "\tPrincipal: org.apache.geronimo.connector.outbound.security.ResourcePrincipal@cb1c722f\n" +
//                "\tPrincipal: org.apache.geronimo.security.IdentificationPrincipal[[1186174499145:0x607c7eb7837eabcd6b759e6f9d29e7eee72622d6]]\n" +
//                "\tPrincipal: org.apache.geronimo.security.IdentificationPrincipal[[1186174499146:0x7622e0831ed59a4cd6c277a53491de74f1489311]]\n" +
//                "\tPrivate Credential: javax.resource.spi.security.PasswordCredential@23f33e5c\n" +
//                "\n" +
//                "Next subject:    Subject:\n" +
//                "\tPrincipal: org.apache.geronimo.connector.outbound.security.ResourcePrincipal@cb1c722f\n" +
//                "\tPrincipal: org.apache.geronimo.security.IdentificationPrincipal[[1186174499145:0x607c7eb7837eabcd6b759e6f9d29e7eee72622d6]]\n" +
//                "\tPrincipal: org.apache.geronimo.security.IdentificationPrincipal[[1186174499146:0x7622e0831ed59a4cd6c277a53491de74f1489311]]\n" +
//                "\tPrivate Credential: javax.resource.spi.security.PasswordCredential@23f33e5c\n" +
//                "\n" +
                "Successfully got configured connection\n" +
                "\n" +
                "Successfully got container managed connection"));

        String expectedPrincipal = "Principal: george";
        int pos1 = body.indexOf(expectedPrincipal);
        assertTrue("Expected current subject principal", pos1 > 0);
        int pos2 = body.indexOf(expectedPrincipal, pos1 + expectedPrincipal.length());
        assertTrue("Expected next subject principal", pos2 > 0);
    }

    @Test
    public void testPageContent2() throws Exception {
        selenium.open("http://localhost:8080/jca-cms/run-as-servlet");
        waitForPageLoad();

        String body = selenium.getText("xpath=/html/body");

        assertTrue(body.endsWith(
                "Successfully got configured connection\n" +
                "\n" +
                "Successfully got container managed connection"));

        String expectedPrincipal = "Principal: george";
        int pos1 = body.indexOf(expectedPrincipal);
        assertTrue("Expected current subject principal", pos1 > 0);
        int pos2 = body.indexOf("Principal: gracie", pos1 + expectedPrincipal.length());
        assertTrue("Expected next subject principal", pos2 > 0);
    }
}

