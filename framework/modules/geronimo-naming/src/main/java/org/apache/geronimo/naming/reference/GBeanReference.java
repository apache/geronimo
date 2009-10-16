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

import java.util.Set;

import javax.naming.NameNotFoundException;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev$ $Date$
 */
public class GBeanReference extends ConfigurationAwareReference {
    private final Class type;

    public GBeanReference(Artifact[] configId, Set abstractNameQueries, Class type) {
        super(configId, abstractNameQueries);
        this.type = type;
    }

    public String getClassName() {
        return type.getName();
    }

    public Object getContent() throws IllegalStateException, NameNotFoundException {
        AbstractName target;
        try {
            target = resolveTargetName();
        } catch (GBeanNotFoundException e) {
            throw (NameNotFoundException)new NameNotFoundException("Could not resolve gbean from name query: " + abstractNameQueries).initCause(e);
        }
        try {
            return getKernel().getGBean(target);
        } catch (GBeanNotFoundException e) {
            IllegalStateException illegalStateException = new IllegalStateException();
            illegalStateException.initCause(e);
            throw illegalStateException;
        }
    }
}
