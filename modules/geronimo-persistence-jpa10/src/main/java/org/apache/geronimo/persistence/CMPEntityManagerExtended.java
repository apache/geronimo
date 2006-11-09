/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.persistence;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

/**
 * @version $Rev$ $Date$
 */
public class CMPEntityManagerExtended implements EntityManager {

    private final TransactionManagerImpl transactionManager;
    private final String persistenceUnit;
    private final EntityManagerFactory entityManagerFactory;
    private final Map entityManagerProperties;
    private final InternalCMPEntityManagerExtended entityManager;

    public CMPEntityManagerExtended(TransactionManagerImpl transactionManager, String persistenceUnit, EntityManagerFactory entityManagerFactory, Map entityManagerProperties) {
        this.transactionManager = transactionManager;
        this.persistenceUnit = persistenceUnit;
        this.entityManagerFactory = entityManagerFactory;
        this.entityManagerProperties = entityManagerProperties;
        entityManager = getEntityManager();
    }

    private InternalCMPEntityManagerExtended getEntityManager() {
        InternalCMPEntityManagerExtended entityManager = EntityManagerExtendedRegistry.getEntityManager(persistenceUnit);
        if (entityManager == null) {
            entityManager = createEntityManager();
            EntityManagerExtendedRegistry.putEntityManager(persistenceUnit, entityManager);
        }
        entityManager.registerBean();
        return entityManager;
    }

    private InternalCMPEntityManagerExtended createEntityManager() {
        EntityManager entityManager;
        if (entityManagerProperties == null) {
            entityManager = entityManagerFactory.createEntityManager();
        } else {
            entityManager = entityManagerFactory.createEntityManager(entityManagerProperties);
        }
        return new InternalCMPEntityManagerExtended(entityManager, persistenceUnit, transactionManager);
    }

    public void beanRemoved() {
        entityManager.beanRemoved();
    }


    public void persist(Object o) {
        entityManager.persist(o);
    }

    public <T>T merge(T t) {
        return entityManager.merge(t);
    }

    public void remove(Object o) {
        entityManager.remove(o);
    }

    public <T>T find(Class<T> aClass, Object o) {
        return entityManager.find(aClass, o);
    }

    public <T>T getReference(Class<T> aClass, Object o) {
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
        throw new IllegalStateException("You cannot call close on a Container Managed Entity Manager");
    }

    public boolean isOpen() {
        return true;
    }

    public EntityTransaction getTransaction() {
        throw new IllegalStateException("You cannot call getTransaction on a container managed EntityManager");
    }

    public void joinTransaction() {
        throw new IllegalStateException("You cannot call joinTransaction on a container managed EntityManager");
    }

    public Object getDelegate() {
        return entityManager.getDelegate();
    }

}
