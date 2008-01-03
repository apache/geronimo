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


package org.apache.geronimo.naming.reference;

import javax.naming.NamingException;

import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.naming.ResourceSource;

/**
 * @version $Rev$ $Date$
 */
public class ResourceReferenceFactory<E extends Throwable> extends AbstractEntryFactory<ResourceReference<E>, ResourceSource> {

    private static final long serialVersionUID = 7366848211161204771L;
    private final String type;

    public ResourceReferenceFactory(Artifact[] configId, AbstractNameQuery abstractNameQuery, Class targetClass) {
        super(configId, abstractNameQuery, ResourceSource.class);
        type = targetClass.getName();
    }

    public ResourceReference buildEntry(Kernel kernel, ClassLoader classLoader) throws NamingException {
        ResourceSource<E> source = getGBean(kernel);
        return new ResourceReference<E>(source, type);
    }
}
