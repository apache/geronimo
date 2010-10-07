/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.testsuite.beanvalidation.ejb;

import org.testng.annotations.Test;
import org.apache.geronimo.testsupport.HttpUtils;
import org.apache.geronimo.testsupport.TestSupport;
public class ValidationEJBTest extends TestSupport {

    private String baseURL = "http://localhost:8080/";
          
    @Test
    public void testInjection() throws Exception {
    	 String appContextStr = System.getProperty("appContext");
    	 String response = HttpUtils.doGET(new java.net.URL(baseURL+appContextStr));
    	 assertTrue(response.indexOf("hasInjectedValidatorFactory = true") != -1);
    	 assertTrue(response.indexOf("hasInjectedValidator = true") != -1);
    	 assertTrue(response.indexOf("hasJNDIValidatorFactory = true") != -1);
    	 assertTrue(response.indexOf("hasJNDIValidator = true") != -1);
    }
}
