/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.concurrent.naming;

import javax.naming.NamingException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.naming.reference.AbstractEntryFactory;

/**
 * @version $Rev: 608311 $ $Date: 2008/02/28 21:05:44 $
 */
public class ResourceReferenceFactory extends AbstractEntryFactory<ResourceReference, ModuleAwareResourceSource> {

    private final String type;
    private final AbstractName moduleID;

    public ResourceReferenceFactory(Artifact[] configId, AbstractNameQuery abstractNameQuery, Class targetClass, AbstractName moduleID) {
        super(configId, abstractNameQuery, ModuleAwareResourceSource.class);
        this.type = targetClass.getName();
        this.moduleID = moduleID;
    }

    public ResourceReference buildEntry(Kernel kernel, ClassLoader classLoader) throws NamingException {
        ModuleAwareResourceSource source = getGBean(kernel);
        return new ResourceReference(source, this.type, this.moduleID);
    }
}
