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

package org.apache.geronimo.connector.outbound.connectionmanagerconfig;

import java.io.Serializable;

import org.apache.geronimo.connector.outbound.ConnectionInterceptor;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/08 17:38:00 $
 *
 * */
public abstract class TransactionSupport implements Serializable {
    public abstract ConnectionInterceptor addXAResourceInsertionInterceptor(ConnectionInterceptor stack, String name);
    public abstract ConnectionInterceptor addTransactionInterceptors(ConnectionInterceptor stack);

}
