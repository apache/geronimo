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

package org.apache.geronimo.connector.mock;

import javax.resource.ResourceException;
import javax.resource.cci.LocalTransaction;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class MockCCILocalTransaction extends MockSPILocalTransaction implements LocalTransaction {

    private final MockConnection mockConnection;

    public MockCCILocalTransaction(MockConnection mockConnection) {
        this.mockConnection = mockConnection;
    }

    public void begin() throws ResourceException {
        super.begin();
        mockConnection.getManagedConnection().localTransactionStartedEvent(mockConnection);
    }

    public void commit() throws ResourceException {
        super.commit();
        mockConnection.getManagedConnection().localTransactionCommittedEvent(mockConnection);
    }

    public void rollback() throws ResourceException {
        super.rollback();
        mockConnection.getManagedConnection().localTransactionRolledBackEvent(mockConnection);
    }
}
