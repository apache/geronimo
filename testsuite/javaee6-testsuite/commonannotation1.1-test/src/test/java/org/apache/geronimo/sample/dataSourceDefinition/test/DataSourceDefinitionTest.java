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

package org.apache.geronimo.sample.dataSourceDefinition.test;

import java.net.URL;

import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.HttpUtils;
import org.apache.geronimo.testsupport.TestSupport;

public class DataSourceDefinitionTest extends TestSupport {

    @Test
    public void testDataSourceDefinitions() throws Exception {
        checkResponse("/DataSourceDefinitionsServlet");
    }

    @Test
    public void testDataSourceDefinitionURL() throws Exception {
        checkResponse("/DataSourceDefinitionUrlServlet");
    }

    @Test
    public void testDataSourceDefinition() throws Exception {
        checkResponse("/DataSourceDefinitionServlet");
    }
    
    private void checkResponse(String address) throws Exception {
        String baseURL = "http://localhost:8080/";
        String appContextStr = System.getProperty("appContext");
        URL url = new URL(baseURL + appContextStr + address);
        String response = HttpUtils.doGET(url);
        assertTrue("Contact1", response.contains("Joe Smith 111 111-"));
        assertTrue("Contact2", response.contains("Jane Doe 222 222-"));
    }
}
