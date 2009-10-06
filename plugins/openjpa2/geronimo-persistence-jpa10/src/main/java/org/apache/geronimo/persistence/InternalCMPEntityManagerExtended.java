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

package org.apache.geronimo.persistence;

import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.EJBException;
import javax.persistence.*;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

/**
 * InternalCMPEntityManagerExtended is an EntityManager wrapper that CMPEntityManagerExtended wraps the
 * real EntityManager with and registers with the transaction.
 *
 * @version $Rev$ $Date$
 */
public class InternalCMPEntityManagerExtended implements EntityManager, EntityManagerWrapper {

    private final EntityManager entityManager;
    private final String persistenceUnit;
    private final TransactionManagerImpl transactionManager;
    //Does this need to be thread safe???
    private final AtomicInteger count = new AtomicInteger();

    public InternalCMPEntityManagerExtended(EntityManager entityManager, String persistenceUnit, TransactionManagerImpl transactionManager) {
        this.entityManager = entityManager;
        this.persistenceUnit = persistenceUnit;
        this.transactionManager = transactionManager;
        if (transactionManager.getTransaction() != null) {
            joinTransaction();
        }
    }

    void registerBean() {
        count.getAndIncrement();
    }

    void beanRemoved() {
        if (count.decrementAndGet() == 0) {
            entityManager.close();
            EntityManagerExtendedRegistry.clearEntityManager(persistenceUnit);
        }

    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void persist(Object o) {
        entityManager.persist(o);
    }

    public <T> T merge(T t) {
        return entityManager.merge(t);
    }

    public void remove(Object o) {
        entityManager.remove(o);
    }

    public <T> T find(Class<T> aClass, Object o) {
        return entityManager.find(aClass, o);
    }

    public <T> T getReference(Class<T> aClass, Object o) {
        return entityManager.getReference(aClass, o);
    }

    public void flush() {
        entityManager.flush();
    }

    public void setFlushMode(FlushModeType flushModeType) {
        entityManager.setFlushMode(flushModeType);
    }

    public FlushModeType getFlushMode() {
        return entityManager.getFlushMode();
    }

    public void lock(Object o, LockModeType lockModeType) {
        entityManager.lock(o, lockModeType);
    }

    public void refresh(Object o) {
        entityManager.refresh(o);
    }

    public void clear() {
        entityManager.clear();
    }

    public boolean contains(Object o) {
        return entityManager.contains(o);
    }

    public Query createQuery(String s) {
        return entityManager.createQuery(s);
    }

    public Query createNamedQuery(String s) {
        return entityManager.createNamedQuery(s);
    }

    public Query createNativeQuery(String s) {
        return entityManager.createNativeQuery(s);
    }

    public Query createNativeQuery(String s, Class aClass) {
        return entityManager.createNativeQuery(s, aClass);
    }

    public Query createNativeQuery(String s, String s1) {
        return entityManager.createNativeQuery(s, s1);
    }

    public void close() {
        //a no-op
    }

    public boolean isOpen() {
        return true;
    }

    public EntityTransaction getTransaction() {
        throw new IllegalStateException("You cannot call getTransaction on a container managed EntityManager");
    }

    public void joinTransaction() {
        //This checks section 5.6.3.1, throwing an EJBException if there is already a PersistenceContext.
        if (transactionManager.getResource(persistenceUnit) != null) {
            throw new EJBException("EntityManager " + transactionManager.getResource(persistenceUnit) + " for persistenceUnit " + persistenceUnit + " already associated with this transaction " + transactionManager.getTransactionKey());
        }
        transactionManager.putResource(persistenceUnit, this);
        entityManager.joinTransaction();
    }

    public Object getDelegate() {
        return entityManager.getDelegate();
    }

    public void beforeCompletion() {
    }

    public void afterCompletion(int i) {
        //close is a no-op
    }
}
