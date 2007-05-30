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

import javax.ejb.SessionContext;
import javax.ejb.SessionBean;

import java.util.ArrayList;


public class HelloWorldEJBSessionBean implements SessionBean {

    private SessionContext ctx;

    public void setSessionContext(SessionContext ctx) {
        this.ctx = ctx;
    }

    public void ejbCreate() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void ejbRemove() {
    }

    public String [] getGreetings() {

        String [] greetings = new String[5];
        greetings[0] = "##############################################";
        greetings[1] = "##                                          ##";
        greetings[2] = "##  Hello World from Geronimo via CORBA !!  ##";
        greetings[3] = "##                                          ##";
        greetings[4] = "##############################################";

        return greetings;
    }

}
