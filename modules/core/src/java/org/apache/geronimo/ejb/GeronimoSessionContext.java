/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.ejb;

import java.security.Identity;
import java.security.Principal;
import java.util.Properties;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.xml.rpc.handler.MessageContext;

import org.apache.geronimo.common.Container;
import org.apache.geronimo.ejb.container.EJBPlugins;
import org.apache.geronimo.ejb.context.GeronimoUserTransaction;
import org.apache.geronimo.ejb.metadata.EJBMetadata;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:51:54 $
 */
public class GeronimoSessionContext implements SessionContext {
    private final Container container;
    private final UserTransaction userTransaction;
    private final TransactionManager transactionManager;
//    private String state;

    public GeronimoSessionContext(Container container) {
        this.container = container;
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(container);
        transactionManager = EJBPlugins.getTransactionManager(container);
        if (ejbMetadata.getTransactionDemarcation().isBean()) {
            this.userTransaction = new GeronimoUserTransaction(transactionManager);
        } else {
            this.userTransaction = null;
        }
    }

    public EJBHome getEJBHome() throws IllegalStateException {
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(container);
        if (ejbMetadata.getHomeInterface() == null) {
            throw new IllegalStateException("getEJBHome is not allowed for a bean without a (remote) home interface");
        }
        return (EJBHome) EJBPlugins.getEJBProxyFactoryManager(container).getThreadEJBProxyFactory().getEJBHome();
    }

    public EJBLocalHome getEJBLocalHome() throws IllegalStateException {
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(container);
        if (ejbMetadata.getLocalHomeInterface() == null) {
            throw new IllegalStateException("getEJBLocalHome is not allowed for a bean without a local home interface");
        }
        return (EJBLocalHome) EJBPlugins.getEJBProxyFactoryManager(container).getEJBProxyFactory("local").getEJBHome();
    }

    public EJBObject getEJBObject() throws IllegalStateException {
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(container);
        if (ejbMetadata.getRemoteInterface() == null) {
            throw new IllegalStateException("getEJBObject is not allowed for a bean without a remote interface");
        }
//        if (state.equals("not-exits")) {
//            throw new IllegalStateException("getEJBObject is not allowed until the bean has identity");
//        }
        return (EJBObject) EJBPlugins.getEJBProxyFactoryManager(container).getThreadEJBProxyFactory().getEJBObject();
    }

    public EJBLocalObject getEJBLocalObject() throws IllegalStateException {
        EJBMetadata ejbMetadata = EJBPlugins.getEJBMetadata(container);
        if (ejbMetadata.getLocalHomeInterface() == null) {
            throw new IllegalStateException("getEJBLocalObject is not allowed for a bean without a local interface");
        }
//        if (state.equals("not-exits")) {
//            throw new IllegalStateException("getEJBLocalObject is not allowed until the bean has identity");
//        }
        return (EJBLocalObject) EJBPlugins.getEJBProxyFactoryManager(container).getEJBProxyFactory("local").getEJBObject();
    }

    public Principal getCallerPrincipal() {
        return null;
    }

    public boolean isCallerInRole(String roleName) {
        return false;
    }

    public UserTransaction getUserTransaction() throws IllegalStateException {
        if (userTransaction == null) {
            throw new IllegalStateException("getUserTransaction is not allowed for bean with container-managed transaction demarcation.");
        }
        return userTransaction;
    }

    public boolean getRollbackOnly() throws IllegalStateException {
        if (userTransaction != null) {
            throw new IllegalStateException("getRollbackOnly is not allowed for beans with bean-managed transaction demarcation.");
        }
//        if (!state.equals("method-ready")) {
//            throw new IllegalStateException("getRollbackOnly is only allowed in the method ready state");
//        }
        try {
            int status = transactionManager.getStatus();
            if (status == Status.STATUS_NO_TRANSACTION) {
                throw new IllegalStateException("getRollbackOnly is only allowed during a transaction");
            }
            return status == Status.STATUS_MARKED_ROLLBACK;
        } catch (SystemException e) {
            throw new EJBException("Could not get transaction status", e);
        }
    }

    public void setRollbackOnly() throws IllegalStateException {
        if (userTransaction != null) {
            throw new IllegalStateException("getRollbackOnly is not allowed for beans with bean-managed transaction demarcation.");
        }
//        if (!state.equals("method-ready")) {
//            throw new IllegalStateException("getRollbackOnly is only allowed in the method ready state");
//        }

        try {
            if (transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION) {
                throw new IllegalStateException("getRollbackOnly is only allowed during a transaction");
            }
            transactionManager.setRollbackOnly();
        } catch (SystemException e) {
            throw new EJBException("Could not get transaction status", e);
        }
    }

    /**
     * @deprecated Use JNDI instead
     * @throws EJBException always
     */
    public Properties getEnvironment() {
        throw new EJBException("getEnvironment is no longer supported; use JNDI instead");
    }

    /**
     * @deprecated Use getCallerPrincipal()
     * @throws EJBException always
     */
    public Identity getCallerIdentity() {
        throw new EJBException("getCallerIdentity is no longer supported; use getCallerPrincipal instead");
    }

    /**
     * @deprecated Use isCallerInRole(String roleName)
     * @throws EJBException always
     */
    public boolean isCallerInRole(Identity role) {
        throw new EJBException("isCallerInRole(Identity role) is no longer supported; use isCallerInRole(String roleName) instead");
    }

    public TimerService getTimerService() throws IllegalStateException {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public MessageContext getMessageContext() throws IllegalStateException {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
