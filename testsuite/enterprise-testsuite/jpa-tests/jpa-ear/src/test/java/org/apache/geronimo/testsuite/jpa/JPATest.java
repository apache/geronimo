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

import java.net.URL;

import org.apache.geronimo.testsupport.HttpUtils;
import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

/**
 * @version $Rev$ $Date$
 */
public class JPATest extends TestSupport {

    @Test
    public void testEjb() throws Exception {
        URL url = new URL("http://localhost:8080/jpa/servlet?test=testEjb");
        String reply = HttpUtils.doGET(url);
        assertTrue("EJB container managed", reply.contains("Test EJB container managed entity manager test OK: true"));
        assertTrue("EJB app managed", reply.contains("Test EJB app managed entity manager factory test OK: true"));
    }
    
    @Test
    public void testServlet() throws Exception {
        URL url = new URL("http://localhost:8080/jpa/servlet?test=testServlet");
        String reply = HttpUtils.doGET(url);
        assertTrue("Servlet container managed", reply.contains("Test servlet container managed entity manager test OK: true"));
        assertTrue("Serlvet app managed", reply.contains("Test servlet app managed entity manager factory test OK: true"));
        assertTrue("Commit", reply.contains("commit OK"));
    }
}
