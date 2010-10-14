/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.openwebbeans;

import java.security.Principal;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.webbeans.spi.SecurityService;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.plugins.AbstractOwbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansEjbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;

public class GeronimoWebBeansPlugin 
    extends AbstractOwbPlugin 
    implements OpenWebBeansJavaEEPlugin, OpenWebBeansEjbPlugin, TransactionService, SecurityService {

    public <T> Bean<T> defineSessionBean(Class<T> clazz,
                                         ProcessAnnotatedType<T> processAnnotateTypeEvent) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getSessionBeanProxy(Bean<?> bean,
                                      Class<?> iface,
                                      CreationalContext<?> creationalContext) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isSessionBean(Class<?> clazz) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSingletonBean(Class<?> clazz) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isStatefulBean(Class<?> clazz) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isStatelessBean(Class<?> clazz) {
        // TODO Auto-generated method stub
        return false;
    }

    public Transaction getTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    public TransactionManager getTransactionManager() {
        // TODO Auto-generated method stub
        return null;
    }

    public UserTransaction getUserTransaction() {
        // TODO Auto-generated method stub
        return null;
    }

    public void registerTransactionSynchronization(TransactionPhase phase,
                                                   ObserverMethod<? super Object> observer,
                                                   Object event) throws Exception {
        // TODO Auto-generated method stub        
    }

    public Principal getCurrentPrincipal() {
        // TODO Auto-generated method stub
        return null;
    }

}
