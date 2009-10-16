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
package org.apache.geronimo.naming.reference;

import org.apache.geronimo.kernel.Kernel;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

/**
 * @version $Rev: 476049 $ $Date: 2006-11-16 20:35:17 -0800 (Thu, 16 Nov 2006) $
 */
public class UserTransactionReference extends SimpleAwareReference {
    public String getClassName() {
        return UserTransaction.class.getName();
    }

    public Object getContent() throws NamingException {
        Kernel kernel = getKernel();
        try {
            return kernel.getGBean(UserTransaction.class);
        } catch (Exception e) {
            throw (NameNotFoundException) new NameNotFoundException("Error getting UserTransaction from kernel").initCause(e);
        }
    }
}
