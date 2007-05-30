/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.testsuite.corba.helloworld;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.annotations.Test;

public class ClientCORBATest extends TestSupport {

    @Test
    public void testClient() throws Exception {
        String clientLogFile = System.getProperty("clientLogFile");
        assertNotNull(clientLogFile);
        FileInputStream in = null;

        try {
            in = new FileInputStream(clientLogFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String inputLine;
            boolean found = false;

            while ((inputLine = reader.readLine()) != null) {
                System.out.println(inputLine);

                if (found == false &&
                    inputLine.indexOf("Hello World from Geronimo via CORBA !!") != -1) {
                    found = true;
                }
            }
            in.close();

            assertTrue(found);
        }
        finally {
            if (in != null) {
                in.close();
            }
        }
    }

}
