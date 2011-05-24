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

import java.io.IOException;
import java.net.URL;

import org.apache.geronimo.testsupport.HttpUtils;
import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class JSFTest extends TestSupport {

    private String baseURL = "http://localhost:8080/";

    @Test
    public void testJSF() throws Exception {
        checkReply("/hello.jsf");
    }

    private void checkReply(String address)
        throws Exception {
        String warName = System.getProperty("webAppName");
        assertNotNull(warName);
        URL url = new URL(baseURL + warName + address);
        /**
         * WABs are deployed asynchronously so it might take a
         * bit to actually invoke the servlet.
         */
        String reply = doGET(url, 6, 10 * 1000);
        
        assertTrue("WAB heading tag", 
                   reply.contains("Hello JSF in WAB"));
        
        assertTrue("WAB date tag", 
                reply.contains("Today is:"));
    }
  
    private String doGET(URL url, int repeat, long delay) {
        for (int i = 0; i < repeat; i++) {
            try {
                return HttpUtils.doGET(url); 
            } catch (IOException e) {
                // ignore
                try {
                    Thread.sleep(delay);
                } catch (Exception ee) {
                    break;
                }
            }
        }
        fail("Did not get servlet response in time");
        return "";
    }
        
}
