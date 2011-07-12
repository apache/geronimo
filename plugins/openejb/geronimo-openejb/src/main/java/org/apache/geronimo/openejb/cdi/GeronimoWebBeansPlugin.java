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
package org.apache.geronimo.openejb.cdi;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.jws.WebService;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionListener;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.webbeans.exception.WebBeansConfigurationException;
import org.apache.webbeans.spi.TransactionService;
import org.apache.webbeans.spi.plugins.AbstractOwbPlugin;
import org.apache.webbeans.spi.plugins.OpenWebBeansJavaEEPlugin;

public class GeronimoWebBeansPlugin extends AbstractOwbPlugin implements OpenWebBeansJavaEEPlugin {
 //OpenWebBeansEjbPlugin,
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

    @Override
    public void isManagedBean(Class<?> clazz) {
        if(Servlet.class.isAssignableFrom(clazz) ||
                Filter.class.isAssignableFrom(clazz) ||
                ServletContextListener.class.isAssignableFrom(clazz) ||
                ServletContextAttributeListener.class.isAssignableFrom(clazz) ||
                HttpSessionActivationListener.class.isAssignableFrom(clazz) ||
                HttpSessionAttributeListener.class.isAssignableFrom(clazz) ||
                HttpSessionBindingListener.class.isAssignableFrom(clazz) ||
                HttpSessionListener.class.isAssignableFrom(clazz) ||
                ServletRequestListener.class.isAssignableFrom(clazz) ||
                ServletRequestAttributeListener.class.isAssignableFrom(clazz) ||
                AsyncListener.class.isAssignableFrom(clazz) )
        {
            throw new WebBeansConfigurationException("Given class  : " + clazz.getName() + " is not managed bean");
        }
    }

    @Override
    public boolean supportsJavaEeComponentInjections(Class<?> clazz) {
        if(Servlet.class.isAssignableFrom(clazz) ||
                Filter.class.isAssignableFrom(clazz) ||
                ServletContextListener.class.isAssignableFrom(clazz) ||
                ServletContextAttributeListener.class.isAssignableFrom(clazz) ||
                HttpSessionActivationListener.class.isAssignableFrom(clazz) ||
                HttpSessionAttributeListener.class.isAssignableFrom(clazz) ||
                HttpSessionBindingListener.class.isAssignableFrom(clazz) ||
                HttpSessionListener.class.isAssignableFrom(clazz) ||
                ServletRequestListener.class.isAssignableFrom(clazz) ||
                ServletRequestAttributeListener.class.isAssignableFrom(clazz) ||
                clazz.isAnnotationPresent(WebService.class) ||
                AsyncListener.class.isAssignableFrom(clazz) )
        {
            return true;
        }

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

    @Override
    public <T> T getSupportedService(Class<T> serviceClass) {
        if (serviceClass == TransactionService.class) {
            return serviceClass.cast(this);
        }
        return super.getSupportedService(serviceClass);
    }
}
