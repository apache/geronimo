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

/**
 * @version $Rev$ $Date$
 */
public class CMPEntityManagerExtended implements EntityManager {

    private final ExtendedEntityManagerRegistry entityManagerRegistry;
    private final EntityManagerFactory entityManagerFactory;
    private final Map entityManagerProperties;

    public CMPEntityManagerExtended(ExtendedEntityManagerRegistry entityManagerRegistry, EntityManagerFactory entityManagerFactory, Map entityManagerProperties) {
        this.entityManagerRegistry = entityManagerRegistry;
        this.entityManagerFactory = entityManagerFactory;
        this.entityManagerProperties = entityManagerProperties;
    }

    private EntityManager getEntityManager() {
        return entityManagerRegistry.getEntityManager(entityManagerFactory, entityManagerProperties);
    }

    public void persist(Object o) {
        getEntityManager().persist(o);
    }

    public <T>T merge(T t) {
        return getEntityManager().merge(t);
    }

    public void remove(Object o) {
        getEntityManager().remove(o);
    }

    public <T>T find(Class<T> aClass, Object o) {
        return getEntityManager().find(aClass, o);
    }

    public <T>T getReference(Class<T> aClass, Object o) {
        return getEntityManager().getReference(aClass, o);
    }

    public void flush() {
        getEntityManager().flush();
    }

    public void setFlushMode(FlushModeType flushModeType) {
        getEntityManager().setFlushMode(flushModeType);
    }

    public FlushModeType getFlushMode() {
        return getEntityManager().getFlushMode();
    }

    public void lock(Object o, LockModeType lockModeType) {
        getEntityManager().lock(o, lockModeType);
    }

    public void refresh(Object o) {
        getEntityManager().refresh(o);
    }

    public void clear() {
        getEntityManager().clear();
    }

    public boolean contains(Object o) {
        return getEntityManager().contains(o);
    }

    public Query createQuery(String s) {
        return getEntityManager().createQuery(s);
    }

    public Query createNamedQuery(String s) {
        return getEntityManager().createNamedQuery(s);
    }

    public Query createNativeQuery(String s) {
        return getEntityManager().createNativeQuery(s);
    }

    public Query createNativeQuery(String s, Class aClass) {
        return getEntityManager().createNativeQuery(s, aClass);
    }

    public Query createNativeQuery(String s, String s1) {
        return getEntityManager().createNativeQuery(s, s1);
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
        return getEntityManager().getDelegate();
    }

}
