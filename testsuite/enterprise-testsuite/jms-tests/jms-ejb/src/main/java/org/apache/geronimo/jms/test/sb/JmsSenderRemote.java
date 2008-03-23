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

package org.apache.geronimo.jms.test.sb;

import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

/**
 * @author <a href="mailto:tomasz[et]mazan[dot]pl">Tomasz Mazan</a>
 */
@Remote
public interface JmsSenderRemote {

    /**
     * Sends request with specified <code>requestName</code> indexed with current number between 0 (inclusive) and <code>counter</code> (exclusive)
     *
     * @param requestName name of request
     * @param priority    message priority
     * @param counter     number of requests to send
     * @return return confirmation code
     */
    String sendMessage(String requestName, int priority, int counter);

    /**
     * receives one message, returning its id
     * @return id of message received.
     */
    Integer receiveMessage();
}
