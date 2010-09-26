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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.superbiz.load;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;


@Remote
@Stateless
public class LoadBean implements Load {
	public String nodename =System.getProperty("node.name");
    public void ping() {
    System.out.println("pinging...." + nodename);
        // do nothing
    }

    public int add(int a, int b) {
        System.out.println("adding "+a +" and " + b + " on " + nodename);
        return a + b;
    }

    public int sum(int... items) {
        System.out.println("doing sum...." + " on " + nodename);
        int i = 0;
        for (int item : items) {
            i += item;
        }
        return i;
    }
    
    public String getNodeName() {
    	return nodename;
    }
}
