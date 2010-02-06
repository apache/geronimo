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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ejb.EJBException;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;
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

    @Override
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    public void persist(Object o) {
        entityManager.persist(o);
    }

    @Override
    public <T> T merge(T t) {
        return entityManager.merge(t);
    }

    @Override
    public void remove(Object o) {
        entityManager.remove(o);
    }

    @Override
    public <T> T find(Class<T> aClass, Object o) {
        return entityManager.find(aClass, o);
    }

    @Override
    public <T> T getReference(Class<T> aClass, Object o) {
        return entityManager.getReference(aClass, o);
    }

    @Override
    public void flush() {
        entityManager.flush();
    }

    @Override
    public void setFlushMode(FlushModeType flushModeType) {
        entityManager.setFlushMode(flushModeType);
    }

    @Override
    public FlushModeType getFlushMode() {
        return entityManager.getFlushMode();
    }

    @Override
    public void lock(Object o, LockModeType lockModeType) {
        entityManager.lock(o, lockModeType);
    }

    @Override
    public void refresh(Object o) {
        entityManager.refresh(o);
    }

    @Override
    public void clear() {
        entityManager.clear();
    }

    @Override
    public boolean contains(Object o) {
        return entityManager.contains(o);
    }

    @Override
    public Query createQuery(String s) {
        return entityManager.createQuery(s);
    }

    @Override
    public Query createNamedQuery(String s) {
        return entityManager.createNamedQuery(s);
    }

    @Override
    public Query createNativeQuery(String s) {
        return entityManager.createNativeQuery(s);
    }

    @Override
    public Query createNativeQuery(String s, Class aClass) {
        return entityManager.createNativeQuery(s, aClass);
    }

    @Override
    public Query createNativeQuery(String s, String s1) {
        return entityManager.createNativeQuery(s, s1);
    }

    @Override
    public void close() {
        //a no-op
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public EntityTransaction getTransaction() {
        throw new IllegalStateException("You cannot call getTransaction on a container managed EntityManager");
    }

    @Override
    public void joinTransaction() {
        //This checks section 5.6.3.1, throwing an EJBException if there is already a PersistenceContext.
        if (transactionManager.getResource(persistenceUnit) != null) {
            throw new EJBException("EntityManager " + transactionManager.getResource(persistenceUnit) + " for persistenceUnit " + persistenceUnit + " already associated with this transaction " + transactionManager.getTransactionKey());
        }
        transactionManager.putResource(persistenceUnit, this);
        entityManager.joinTransaction();
    }

    @Override
    public Object getDelegate() {
        return entityManager.getDelegate();
    }

    @Override
    public void beforeCompletion() {
    }

    @Override
    public void afterCompletion(int i) {
        //close is a no-op
    }

    /**
     * JPA2 added methods
     */
    @Override
    public Metamodel getMetamodel() {
        return entityManager.getMetamodel();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return entityManager.getCriteriaBuilder();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManager.getEntityManagerFactory();
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return entityManager.createNamedQuery(name, resultClass);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return entityManager.createQuery(criteriaQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return entityManager.createQuery(qlString, resultClass);
    }

    @Override
    public void detach(Object entity) {
        entityManager.detach(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return entityManager.find(entityClass, primaryKey, properties);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return entityManager.find(entityClass, primaryKey, lockMode);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return entityManager.find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return entityManager.getLockMode(entity);
    }

    @Override
    public Map<String, Object> getProperties() {
        return entityManager.getProperties();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        entityManager.lock(entity, lockMode, properties);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        entityManager.refresh(entity, properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        entityManager.refresh(entity, lockMode);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        entityManager.refresh(entity, lockMode, properties);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        entityManager.setProperty(propertyName, value);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return entityManager.unwrap(cls);
    }

}

