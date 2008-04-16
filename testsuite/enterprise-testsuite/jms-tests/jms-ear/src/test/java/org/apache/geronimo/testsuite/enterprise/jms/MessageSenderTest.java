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
package org.apache.geronimo.testsuite.enterprise.jms;

import javax.naming.InitialContext;

import org.apache.geronimo.jms.test.sb.JmsSenderRemote;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:tomasz[et]mazan[dot]pl">Tomasz Mazan</a>
 */
@Test
public class MessageSenderTest {

    /**
     * Initialize test configuration
     *
     * @throws Exception if exception occurs
     */
    public void setUp() throws Exception {
    }

    /**
     * Creates customer instance
     *
     * @throws Exception if exception occurs
     */
    @Test
    public void sendRequests() throws Exception {
        InitialContext ctx = new InitialContext();
        String totalShipStr = System.getProperty("total-shipment");
        String msgsPerShipStr = System.getProperty("requests-per-shipment");

        int totalShip = (totalShipStr == null ? 10 : Integer.parseInt(totalShipStr));
        int msgsPerShip = (msgsPerShipStr == null ? 20 : Integer.parseInt(msgsPerShipStr));

        JmsSenderRemote jmsSender = (JmsSenderRemote) ctx.lookup("JmsSenderRemote");
        if (jmsSender != null) {
            System.out.println("JmsSender initialized");
            for (int i = 0; i < totalShip; ++i) {
                String messageName = (i + 1) + ".Request";

                System.out.format("Sending (%1$s) request(s) with name %2$s%n", msgsPerShip, messageName);
                jmsSender.sendMessage(messageName, 0, msgsPerShip);
                System.out.println("Requests sent");
            }
            //enable is receiveMessage is modified to detect messages on request queue
/*
            Thread.sleep(10000);
            Integer id = jmsSender.receiveMessage();
            if (id != null) {
                throw new Exception("received request message: " + id);
            }
*/
            //uncomment to actually run test
            for (int i = 0; i < totalShip * msgsPerShip; i++) {
                Integer id = jmsSender.receiveMessage();
                if (id == null) {
                    throw new Exception("did not receive message: " + i);
//                    System.out.println("Did not receive message: " + i);
//                    Thread.sleep(10000);
                }
            }
        } else {
            throw new Exception("Sender is null");
        }
    }

}
