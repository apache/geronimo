/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;

/**
 * The JtaQuery is a wrapper around a TypedQuery and and entity manager that automatically closes the entity managers
 * when the query is finished.  This implementation is only for non-transaction queries.
 */
public class NoTxTypedQueryWrapper<X> extends NoTxQueryWrapper implements TypedQuery<X> {
    
    private final TypedQuery<X> query;
    
    public NoTxTypedQueryWrapper(EntityManager entityManager, TypedQuery<X> query) {
        super(entityManager, query);
        this.query = query;
    }

    public List<X> getResultList() {
        try {
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    public X getSingleResult() {
        try {
            return query.getSingleResult();
        } finally {
            entityManager.close();
        }
    }

    public TypedQuery<X> setMaxResults(int maxResult) {
        query.setMaxResults(maxResult);
        return this;
    }

    public TypedQuery<X> setFirstResult(int startPosition) {
        query.setFirstResult(startPosition);
        return this;
    }

    public TypedQuery<X> setHint(String hintName, Object value) {
        query.setHint(hintName, value);
        return this;
    }

    public <T> TypedQuery<X> setParameter(Parameter<T> param, T value) {
        query.setParameter(param, value);
        return this;
    }

    public TypedQuery<X> setParameter(Parameter<Calendar> param,
                                      Calendar value,
                                      TemporalType temporalType) {
        query.setParameter(param, value, temporalType);
        return this;
    }

    public TypedQuery<X> setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        query.setParameter(param, value, temporalType);
        return this;
    }

    public TypedQuery<X> setParameter(String name, Object value) {
        query.setParameter(name, value);
        return this;
    }

    public TypedQuery<X> setParameter(String name, Calendar value, TemporalType temporalType) {
        query.setParameter(name, value, temporalType);
        return this;
    }

    public TypedQuery<X> setParameter(String name, Date value, TemporalType temporalType) {
        query.setParameter(name, value, temporalType);
        return this;
    }

    public TypedQuery<X> setParameter(int position, Object value) {
        query.setParameter(position, value);
        return this;
    }

    public TypedQuery<X> setParameter(int position, Calendar value, TemporalType temporalType) {
        query.setParameter(position, value, temporalType);
        return this;
    }

    public TypedQuery<X> setParameter(int position, Date value, TemporalType temporalType) {
        query.setParameter(position, value, temporalType);
        return this;
    }

    public TypedQuery<X> setFlushMode(FlushModeType flushMode) {
        query.setFlushMode(flushMode);
        return this;
    }

    public TypedQuery<X> setLockMode(LockModeType lockMode) {
        query.setLockMode(lockMode);
        return this;
    }

}
