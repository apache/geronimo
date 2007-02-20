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
package org.apache.geronimo.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.xml.ws.Service;
import org.apache.greeter_control.Greeter;

public class JAXWSClient {

    public static void main(String [] args) throws Exception {
        try {
            test("Tester");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // needs to be forced because of a bug
        System.exit(0);
    }

    public static void test(String name) throws Exception {
        if (name == null) {
            name = "Unknown";
        }
        InitialContext ctx = new InitialContext();
        Service service = (Service)ctx.lookup("java:comp/env/services/Greeter");
        Greeter greeter = service.getPort(Greeter.class);
        System.out.println("WebService returned: " + greeter.greetMe(name));
    }
    
}
