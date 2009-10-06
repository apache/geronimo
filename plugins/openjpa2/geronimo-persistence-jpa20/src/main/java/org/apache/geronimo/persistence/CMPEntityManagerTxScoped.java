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

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.*;
import javax.persistence.TransactionRequiredException;
import javax.transaction.*;
import org.apache.geronimo.transaction.manager.TransactionImpl;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;

/**
 * @version $Rev$ $Date$
 */
public class CMPEntityManagerTxScoped implements EntityManager {

    private final TransactionManagerImpl transactionManager;
    private final String persistenceUnit;
    private final EntityManagerFactory entityManagerFactory;
    private final Map entityManagerProperties;

    public CMPEntityManagerTxScoped(TransactionManagerImpl transactionManager, String persistenceUnit, EntityManagerFactory entityManagerFactory, Map entityManagerProperties) {
        this.transactionManager = transactionManager;
        this.persistenceUnit = persistenceUnit;
        this.entityManagerFactory = entityManagerFactory;
        this.entityManagerProperties = entityManagerProperties;
    }

    private EntityManager getEntityManager(boolean activeRequired) {
        TransactionImpl transaction = (TransactionImpl) transactionManager.getTransaction();
        if (activeRequired && (transaction == null || transaction.getStatus() != Status.STATUS_ACTIVE)) {
            throw new TransactionRequiredException("No active transaction");
        }
        if (transaction == null) {
            return null;
        }
        EntityManagerWrapper entityManagerWrapper = (EntityManagerWrapper) transactionManager.getResource(persistenceUnit);
        if (entityManagerWrapper == null) {
            EntityManager entityManager = createEntityManager();
            entityManagerWrapper = new EntityManagerWrapperTxScoped(entityManager);
            transactionManager.putResource(persistenceUnit, entityManagerWrapper);
            try {
                transaction.registerSynchronization(entityManagerWrapper);
            } catch (javax.transaction.RollbackException e) {
                throw (TransactionRequiredException) new TransactionRequiredException("No active transaction").initCause(e);
            } catch (SystemException e) {
                throw (TransactionRequiredException) new TransactionRequiredException("No active transaction").initCause(e);
            }
        }
        return entityManagerWrapper.getEntityManager();
    }

    private EntityManager createEntityManager() {
        EntityManager entityManager;
        if (entityManagerProperties == null) {
            entityManager = entityManagerFactory.createEntityManager();
        } else {
            entityManager = entityManagerFactory.createEntityManager(entityManagerProperties);
        }
        return entityManager;
    }


    @Override
    public void persist(Object o) {
        EntityManager entityManager = getEntityManager(true);
        if (entityManager != null) {
            entityManager.persist(o);
        } else {
            entityManager = createEntityManager();
            try {
                entityManager.persist(o);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public <T> T merge(T t) {
        EntityManager entityManager = getEntityManager(true);
        if (entityManager != null) {
            return entityManager.merge(t);
        } else {
            entityManager = createEntityManager();
            try {
                return entityManager.merge(t);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public void remove(Object o) {
        EntityManager entityManager = getEntityManager(true);
        if (entityManager != null) {
            entityManager.remove(o);
        } else {
            entityManager = createEntityManager();
            try {
                entityManager.remove(o);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public <T> T find(Class<T> aClass, Object o) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.find(aClass, o);
        } else {
            entityManager = createEntityManager();
            try {
                return entityManager.find(aClass, o);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public <T> T getReference(Class<T> aClass, Object o) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.getReference(aClass, o);
        } else {
            entityManager = createEntityManager();
            try {
                return entityManager.getReference(aClass, o);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public void flush() {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            entityManager.flush();
        } else {
            entityManager = createEntityManager();
            try {
                entityManager.flush();
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public void setFlushMode(FlushModeType flushModeType) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            entityManager.setFlushMode(flushModeType);
        } else {
            entityManager = createEntityManager();
            try {
                entityManager.setFlushMode(flushModeType);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public FlushModeType getFlushMode() {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.getFlushMode();
        } else {
            entityManager = createEntityManager();
            try {
                return entityManager.getFlushMode();
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public void lock(Object o, LockModeType lockModeType) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            entityManager.lock(o, lockModeType);
        } else {
            entityManager = createEntityManager();
            try {
                entityManager.lock(o, lockModeType);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public void refresh(Object o) {
        EntityManager entityManager = getEntityManager(true);
        if (entityManager != null) {
            entityManager.refresh(o);
        } else {
            entityManager = createEntityManager();
            try {
                entityManager.refresh(o);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public void clear() {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            entityManager.clear();
        } else {
            entityManager = createEntityManager();
            try {
                entityManager.clear();
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public boolean contains(Object o) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.contains(o);
        } else {
            entityManager = createEntityManager();
            try {
                return entityManager.contains(o);
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public Query createQuery(String s) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.createQuery(s);
        } else {
            entityManager = createEntityManager();
            return new NoTxQueryWrapper(entityManager, entityManager.createQuery(s));
        }
    }

    @Override
    public Query createNamedQuery(String s) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.createNamedQuery(s);
        } else {
            entityManager = createEntityManager();
            return new NoTxQueryWrapper(entityManager, entityManager.createNamedQuery(s));
        }
    }

    @Override
    public Query createNativeQuery(String s) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.createNativeQuery(s);
        } else {
            entityManager = createEntityManager();
            return new NoTxQueryWrapper(entityManager, entityManager.createNativeQuery(s));
        }
    }

    @Override
    public Query createNativeQuery(String s, Class aClass) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.createNativeQuery(s, aClass);
        } else {
            entityManager = createEntityManager();
            return new NoTxQueryWrapper(entityManager, entityManager.createNativeQuery(s, aClass));
        }
    }

    @Override
    public Query createNativeQuery(String s, String s1) {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.createNativeQuery(s, s1);
        } else {
            entityManager = createEntityManager();
            return new NoTxQueryWrapper(entityManager, entityManager.createNativeQuery(s, s1));
        }
    }

    @Override
    public void close() {
        throw new IllegalStateException("You cannot call close on a Container Managed Entity Manager");
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
        throw new IllegalStateException("You cannot call joinTransaction on a container managed EntityManager");
    }

    @Override
    public Object getDelegate() {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.getDelegate();
        } else {
            entityManager = createEntityManager();
            try {
                return entityManager.getDelegate();
            } finally {
                entityManager.close();
            }
        }
    }

    private static class EntityManagerWrapperTxScoped implements EntityManagerWrapper, Synchronization {
        private final EntityManager entityManager;

        public EntityManagerWrapperTxScoped(EntityManager entityManager) {
            if (entityManager == null) {
                throw new IllegalArgumentException("Need a non-null entity manager");
            }
            this.entityManager = entityManager;
        }

        public void close() {
            entityManager.close();
        }

        public EntityManager getEntityManager() {
            return entityManager;
        }

        public void beforeCompletion() {
        }

        public void afterCompletion(int i) {
            close();
        }
    }

    /**
     * JPA2 added methods
     */
    @Override
    public Metamodel getMetamodel() {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.getMetamodel();
        } else {
            entityManager = createEntityManager();
            try {
                return entityManager.getMetamodel();
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public QueryBuilder getQueryBuilder() {
        EntityManager entityManager = getEntityManager(false);
        if (entityManager != null) {
            return entityManager.getQueryBuilder();
        } else {
            entityManager = createEntityManager();
            try {
                return entityManager.getQueryBuilder();
            } finally {
                entityManager.close();
            }
        }
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void detach(Object entity) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey,
        Map<String, Object> properties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey,
        LockModeType lockMode) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey,
        LockModeType lockMode, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getSupportedProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void lock(Object entity, LockModeType lockMode,
        Map<String, Object> properties) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode,
        Map<String, Object> properties) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        // TODO Auto-generated method stub
        return null;
    }
}
