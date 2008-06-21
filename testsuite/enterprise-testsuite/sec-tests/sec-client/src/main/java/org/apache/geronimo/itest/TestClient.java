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
package org.apache.geronimo.itest;

import java.rmi.AccessException;
import java.rmi.RemoteException;

import javax.ejb.EJB;

import org.apache.geronimo.security.ContextManager;

public class TestClient {
    
    @EJB(name="TestSession")
    private static TestSessionHome sessionHome;

    public static void main(String [] args) throws Exception {

        try {
            new TestClient().test();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // needs to be forced because of a bug
        System.exit(0);
    }

    public void test() throws Exception {
        System.out.println("Hello World!");
        System.out.println("Context: " + ContextManager.getCurrentCaller());
        try {
            TestSession session = sessionHome.create();
            System.out.print(session.testAccessBar());
            try {
                System.out.println(session.testAccessFoo());
            } catch (AccessException e) {
                System.out.println("Correctly received security exception on testAccessFoo method");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    
}
