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

package org.apache.geronimo.testsuite.corba.mytime;

import java.util.Calendar;
import org.apache.geronimo.testsupport.SeleniumTestSupport;
import org.testng.annotations.Test;

public class TestMyTime extends SeleniumTestSupport {

    @Test
    public void testManagedBean() throws Exception {
        selenium.open("/MyTime");
        selenium.waitForPageToLoad("99999");

        // Web browser contents should be like the following:
        //
        // This is the time returned from the EJB: Sun Jun 10 00:05:31 EDT 2007

        // Ensure literal found
        assertTrue(selenium.getBodyText().contains("This is the time returned from the EJB:"));

        // Ensure day of week found
        assertTrue(selenium.getBodyText().contains("Mon") ||
                   selenium.getBodyText().contains("Tue") ||
                   selenium.getBodyText().contains("Wed") ||
                   selenium.getBodyText().contains("Thu") ||
                   selenium.getBodyText().contains("Fri") ||
                   selenium.getBodyText().contains("Sat") ||
                   selenium.getBodyText().contains("Sun"));

        // Ensure month found
        assertTrue(selenium.getBodyText().contains("Jan") ||
                   selenium.getBodyText().contains("Feb") ||
                   selenium.getBodyText().contains("Mar") ||
                   selenium.getBodyText().contains("Apr") ||
                   selenium.getBodyText().contains("May") ||
                   selenium.getBodyText().contains("Jun") ||
                   selenium.getBodyText().contains("Jul") ||
                   selenium.getBodyText().contains("Aug") ||
                   selenium.getBodyText().contains("Sep") ||
                   selenium.getBodyText().contains("Oct") ||
                   selenium.getBodyText().contains("Nov") ||
                   selenium.getBodyText().contains("Dec"));

        // Ensure year found
        assertTrue(selenium.getBodyText().contains(Integer.toString(Calendar.getInstance().get(Calendar.YEAR))));
    }
}
