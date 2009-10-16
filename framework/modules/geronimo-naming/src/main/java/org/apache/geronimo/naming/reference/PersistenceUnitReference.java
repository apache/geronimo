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

import javax.naming.NameNotFoundException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitReference extends ConfigurationAwareReference {


    public PersistenceUnitReference(Artifact[] configId, AbstractNameQuery abstractNameQuery) {
        super(configId, abstractNameQuery);
    }

    public String getClassName() {
        return "javax.persistence.EntityManagerFactory";
    }

    public Object getContent() throws NameNotFoundException {
    Kernel kernel = getKernel();

    AbstractName target;
    try {
        target = resolveTargetName();
    } catch (GBeanNotFoundException e) {
        throw (NameNotFoundException) new NameNotFoundException("Could not resolve name query: " + abstractNameQueries).initCause(e);
    }

    Object entityManagerFactory;
    try {
        entityManagerFactory = kernel.getAttribute(target, "entityManagerFactory");
    } catch (Exception e) {
        throw (IllegalStateException) new IllegalStateException("Could not get EntityManagerFactory").initCause(e);
    }
    if (entityManagerFactory == null) {
        throw new IllegalStateException("entity manager not returned. Target " + target + " not started");
    }
    return entityManagerFactory;
}
}
