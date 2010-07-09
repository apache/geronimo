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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;

/**
 * @version $Rev$ $Date$
 */
public class CMPEntityManagerExtended implements EntityManager {

    private final ExtendedEntityManagerRegistry entityManagerRegistry;
    private final EntityManagerFactory entityManagerFactory;
    private final Map entityManagerProperties;
    private final String persistenceUnitName;

    public CMPEntityManagerExtended(ExtendedEntityManagerRegistry entityManagerRegistry, EntityManagerFactory entityManagerFactory, Map entityManagerProperties, String persistenceUnitName) {
        this.entityManagerRegistry = entityManagerRegistry;
        this.entityManagerFactory = entityManagerFactory;
        this.entityManagerProperties = entityManagerProperties;
        this.persistenceUnitName = persistenceUnitName;
    }

    private EntityManager getEntityManager() {
        return entityManagerRegistry.getEntityManager(entityManagerFactory, entityManagerProperties, persistenceUnitName);
    }

    @Override
    public void persist(Object o) {
        getEntityManager().persist(o);
    }

    @Override
    public <T>T merge(T t) {
        return getEntityManager().merge(t);
    }

    @Override
    public void remove(Object o) {
        getEntityManager().remove(o);
    }

    @Override
    public <T>T find(Class<T> aClass, Object o) {
        return getEntityManager().find(aClass, o);
    }

    @Override
    public <T>T getReference(Class<T> aClass, Object o) {
        return getEntityManager().getReference(aClass, o);
    }

    @Override
    public void flush() {
        getEntityManager().flush();
    }

    @Override
    public void setFlushMode(FlushModeType flushModeType) {
        getEntityManager().setFlushMode(flushModeType);
    }

    @Override
    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    @Override
    public void lock(Object o, LockModeType lockModeType) {
        getEntityManager().lock(o, lockModeType);
    }

    @Override
    public void refresh(Object o) {
        getEntityManager().refresh(o);
    }

    @Override
    public void clear() {
        getEntityManager().clear();
    }

    @Override
    public boolean contains(Object o) {
        return getEntityManager().contains(o);
    }

    @Override
    public Query createQuery(String s) {
        return getEntityManager().createQuery(s);
    }

    @Override
    public Query createNamedQuery(String s) {
        return getEntityManager().createNamedQuery(s);
    }

    @Override
    public Query createNativeQuery(String s) {
        return getEntityManager().createNativeQuery(s);
    }

    @Override
    public Query createNativeQuery(String s, Class aClass) {
        return getEntityManager().createNativeQuery(s, aClass);
    }

    @Override
    public Query createNativeQuery(String s, String s1) {
        return getEntityManager().createNativeQuery(s, s1);
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
        return getEntityManager().getDelegate();
    }

    /**
     * JPA2 added methods
     */
    @Override
    public Metamodel getMetamodel() {
        return getEntityManager().getMetamodel();
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return getEntityManager().getCriteriaBuilder();
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return getEntityManager().getEntityManagerFactory();
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return getEntityManager().createNamedQuery(name, resultClass);
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return getEntityManager().createQuery(criteriaQuery);
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return getEntityManager().createQuery(qlString, resultClass);
    }

    @Override
    public void detach(Object entity) {
        getEntityManager().detach(entity);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return getEntityManager().find(entityClass, primaryKey, properties);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return getEntityManager().find(entityClass, primaryKey, lockMode);
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return getEntityManager().find(entityClass, primaryKey, lockMode, properties);
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return getEntityManager().getLockMode(entity);
    }

    @Override
    public Map<String, Object> getProperties() {
        return getEntityManager().getProperties();
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        getEntityManager().lock(entity, lockMode, properties);
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        getEntityManager().refresh(entity, properties);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        getEntityManager().refresh(entity, lockMode);
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        getEntityManager().refresh(entity, lockMode, properties);
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        getEntityManager().setProperty(propertyName, value);
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return getEntityManager().unwrap(cls);
    }

}
