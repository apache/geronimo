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

package org.apache.geronimo.testsuite.jsps;

import java.net.URL;

import org.apache.geronimo.testsupport.HttpUtils;
import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class TestJsps extends TestSupport {

    @Test
    public void testDeferral() throws Exception {
        URL url = new URL("http://localhost:8080/jsp21/testDeferral.jsp");
        String reply = HttpUtils.doGET(url);
        assertTrue("testDeferral", reply.contains("OneTwo"));
    }

    @Test
    public void testScopes() throws Exception {
        URL url = new URL("http://localhost:8080/jsp21/testScopes.jsp");
        String reply = HttpUtils.doGET(url);
        assertTrue("testScopes", reply.contains("value1 value2 value3 value4"));
    }

    @Test
    public void testTaglibs() throws Exception {
        URL url = new URL("http://localhost:8080/jsp21/testTaglibs.jsp");
        String reply = HttpUtils.doGET(url);
        assertTrue("testTagLibs", reply.contains("Hello"));
    }

    @Test
    public void testTrimWhitespace() throws Exception {
        URL url = new URL("http://localhost:8080/jsp21/testTrimWhitespace.jsp");
        String reply = HttpUtils.doGET(url);
        assertTrue("testTrimWhitespace", reply.contains("source html of this page should not contain empty lines"));
    }

}
