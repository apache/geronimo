/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.tomcat.interceptor;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserTransactionBeforeAfter implements BeforeAfter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UserTransaction userTransaction;

    private final BeforeAfter next;

    private final int index;

    public UserTransactionBeforeAfter(BeforeAfter next, int index, UserTransaction userTransaction) {
        this.next = next;
        this.index = index;
        this.userTransaction = userTransaction;
    }

    public void after(Object[] context, ServletRequest httpRequest, ServletResponse httpResponse, int dispatch) {
        if (next != null) {
            next.after(context, httpRequest, httpResponse, dispatch);
        }
        
        boolean active = (Boolean)context[index];
        if ((!active && isMarkedRollback()) || (dispatch == EDGE_SERVLET && isActive())) {
            try {
                userTransaction.rollback();
            } catch (SystemException e) {
                throw new RuntimeException("Error rolling back transaction left open by user program", e);
            }
        }

    }

    public void before(Object[] context, ServletRequest request, ServletResponse response, int dispatch) {
        context[index] = isActive();
        next.before(context, request, response, dispatch);
    }

    private boolean isActive() {
        try {
            return !(userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION || userTransaction.getStatus() == Status.STATUS_COMMITTED);
        } catch (SystemException e) {
            log.error("Could not determine transaction status", e);
            throw new RuntimeException("Could not determine transaction status", e);
        }
    }

    private boolean isMarkedRollback() {
        try {
            return userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException e) {
            log.error("Could not determine transaction status", e);
            throw new RuntimeException("Could not determine transaction status", e);
        }
    }
}
