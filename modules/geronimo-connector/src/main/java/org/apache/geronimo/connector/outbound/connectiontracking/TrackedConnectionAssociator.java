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

package org.apache.geronimo.connector.outbound.connectiontracking;

import javax.resource.ResourceException;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 */
public interface TrackedConnectionAssociator {
    /**
     * If true, ConnectorInstanceContext instance does not have to be kept on a per component basis; otherwise the
     * same instance must be passed to enter each time the specific component instance is entered.
     * @return true if connections are proxied and only connect when invoked
     */
    boolean isLazyConnect();

    ConnectorInstanceContext enter(ConnectorInstanceContext newConnectorInstanceContext) throws ResourceException;

    void newTransaction() throws ResourceException;

    void exit(ConnectorInstanceContext connectorInstanceContext) throws ResourceException;
}
