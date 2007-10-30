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
import java.util.HashMap;
import java.util.Iterator;

import javax.transaction.Transaction;

/**
 * @version $Rev$ $Date$
 */
public class EntityManagerExtendedRegistry {

    private static final ThreadLocal<Map<String, InternalCMPEntityManagerExtended>> entityManagerMaps = new ThreadLocal<Map<String, InternalCMPEntityManagerExtended>>() {
        protected Map<String, InternalCMPEntityManagerExtended> initialValue() {
            return new HashMap<String, InternalCMPEntityManagerExtended>();
        }
    };

    public static InternalCMPEntityManagerExtended getEntityManager(String persistenceUnit) {
        Map<String, InternalCMPEntityManagerExtended> entityManagerMap = entityManagerMaps.get();
        return entityManagerMap.get(persistenceUnit);
    }

    public static void putEntityManager(String persistenceUnit, InternalCMPEntityManagerExtended entityManager) {
        Map<String, InternalCMPEntityManagerExtended> entityManagerMap = entityManagerMaps.get();
        InternalCMPEntityManagerExtended oldEntityManager = entityManagerMap.put(persistenceUnit, entityManager);
        if (oldEntityManager != null) {
            throw new IllegalStateException("There was already an EntityManager registered for persistenceUnit " + persistenceUnit);
        }
    }

    public static void clearEntityManager(String persistenceUnit) {
        Map<String, InternalCMPEntityManagerExtended> entityManagerMap = entityManagerMaps.get();
        entityManagerMap.remove(persistenceUnit);
    }

    public static void threadAssociated(Transaction transaction) {
        Map<String, InternalCMPEntityManagerExtended> entityManagerMap = entityManagerMaps.get();
        for (Iterator i = entityManagerMap.values().iterator(); i.hasNext(); ) {
            InternalCMPEntityManagerExtended entityManager = (InternalCMPEntityManagerExtended) i.next();
            entityManager.joinTransaction();
        }
    }

    public static void threadUnassociated(Transaction transaction) {
        //Any way to unassociate?
    }
}
