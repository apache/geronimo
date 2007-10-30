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

package org.apache.geronimo.naming.reference;

import java.util.Map;

import javax.naming.NameNotFoundException;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceContextReference extends ConfigurationAwareReference {

    private boolean transactionScoped;
    private Map properties;

    public PersistenceContextReference(Artifact[] configId, AbstractNameQuery abstractNameQuery, boolean transactionScoped, Map properties) {
        super(configId, abstractNameQuery);
        this.transactionScoped = transactionScoped;
        this.properties = properties;
    }

    public String getClassName() {
        return "javax.persistence.EntityManager";
    }
    
    public Object getContent() throws NameNotFoundException {
    Kernel kernel = getKernel();

    AbstractName target;
    try {
        target = resolveTargetName();
    } catch (GBeanNotFoundException e) {
        throw (NameNotFoundException) new NameNotFoundException("Could not resolve name query: " + abstractNameQueries).initCause(e);
    }

    Object entityManager;
    try {
        entityManager = kernel.invoke(target, "getEntityManager", new Object[] {Boolean.valueOf(transactionScoped), properties}, new String[] {boolean.class.getName(), Map.class.getName()});
    } catch (Exception e) {
        throw (IllegalStateException) new IllegalStateException("Could not get entityManager").initCause(e);
    }
    if (entityManager == null) {
        throw new IllegalStateException("entity manager not returned. Target " + target + " not started");
    }
    return entityManager;
}
}
