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


package org.apache.geronimo.jetty7.handler;

import java.io.IOException;

import javax.servlet.ServletException;
//import javax.servlet.DispatcherType;
import org.eclipse.jetty.server.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;

/**
 * @version $Rev$ $Date$
 */
public class UserTransactionHandler extends AbstractImmutableHandler {
    private final UserTransaction userTransaction;

    public UserTransactionHandler(Handler next, UserTransaction userTransaction) {
        super(next);
        this.userTransaction = userTransaction;
    }

    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        boolean active = isActive();
        try {
            next.handle(target, baseRequest, request, response);
        } finally {
             DispatcherType dispatch = ((Request)request).getDispatcherType();
             if ((!active && isMarkedRollback()) || (DispatcherType.REQUEST.equals(dispatch) && isActive())) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e) {
                    throw new ServletException("Error rolling back transaction left open by user program", e);
                }
            }
        }
    }

    public void lifecycleCommand(LifecycleCommand lifecycleCommand) throws Exception {
        boolean active = isActive();
        try {
            super.lifecycleCommand(lifecycleCommand);
        } finally {
            if (!active && isActive()) {
                try {
                    userTransaction.rollback();
                } catch (SystemException e) {
                    throw new ServletException("Error rolling back transaction left open by user program", e);
                }
            }
        }
    }

    private boolean isActive() throws ServletException {
        try {
            return !(userTransaction.getStatus() == Status.STATUS_NO_TRANSACTION
                    || userTransaction.getStatus() == Status.STATUS_COMMITTED);
        } catch (SystemException e) {
            throw new ServletException("Could not determine transaction status", e);
        }
    }
    private boolean isMarkedRollback() throws ServletException {
        try {
            return userTransaction.getStatus() == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException e) {
            throw new ServletException("Could not determine transaction status", e);
        }
    }
}
