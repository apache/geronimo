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

package org.apache.geronimo.testsuite.jpa;

import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.SeleniumTestSupport;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
@Test
public class JPATest
    extends SeleniumTestSupport
{
    @Test
    public void testIndexContent() throws Exception {
        selenium.open("/jpa/servlet");
        waitForPageLoad();
        //assertEquals("Hello J2EE 1.4", selenium.getTitle());
        assertEquals("TestServlet\n" +
                "Test EJB container managed entity manager test OK: true\n" +
                "Test EJB app managed entity manager factory test OK: true\n" +
                "Test servlet container managed entity manager test OK: true\n" +
                "Test servlet app managed entity manager factory test OK: true\n" +
                "commit OK", selenium.getText("xpath=/html/body"));

    }
}

