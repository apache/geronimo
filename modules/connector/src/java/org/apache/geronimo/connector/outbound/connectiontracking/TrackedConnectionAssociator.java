/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.util.Set;

import javax.resource.ResourceException;

import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TransactionContext;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:11 $
 *
 * */
public interface TrackedConnectionAssociator {

    InstanceContext enter(InstanceContext newInstanceContext
            )
            throws ResourceException;

    void exit(InstanceContext reenteringInstanceContext,
            Set unshareableResources)
            throws ResourceException;

    TransactionContext setTransactionContext(TransactionContext newTransactionContext) throws ResourceException;

    Set setUnshareableResources(Set unshareableResources);

    void resetTransactionContext(TransactionContext transactionContext);
}
