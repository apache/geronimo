/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.openejb;

import javax.naming.NameNotFoundException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.naming.reference.ConfigurationAwareReference;

public class EjbReference extends ConfigurationAwareReference {
    private final boolean remote;

    public EjbReference(Artifact[] artifact, AbstractNameQuery abstractNameQuery, boolean remote) {
        super(artifact, abstractNameQuery);
        this.remote = remote;
    }

    public Object getContent() throws NameNotFoundException {
        try {
            AbstractName abstractName = resolveTargetName();
            EjbDeployment ejbDeployment = (EjbDeployment) getKernel().getGBean(abstractName);
            if (remote) {
                return ejbDeployment.getEJBHome();
            } else {
                return ejbDeployment.getEJBLocalHome();
            }
        } catch (GBeanNotFoundException e) {
            throw (NameNotFoundException) new NameNotFoundException().initCause(e);
        }
    }
}