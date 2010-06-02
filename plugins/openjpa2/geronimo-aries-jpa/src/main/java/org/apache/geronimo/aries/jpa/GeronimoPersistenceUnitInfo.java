/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.geronimo.aries.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.spi.ClassTransformer;

import org.apache.aries.jpa.container.parsing.ParsedPersistenceUnit;
import org.apache.aries.jpa.container.unit.impl.PersistenceUnitInfoImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class GeronimoPersistenceUnitInfo extends PersistenceUnitInfoImpl {

    private final List<TransformerWrapper> transformers;
    private final Bundle bundle;
    
    public GeronimoPersistenceUnitInfo(Bundle bundle,
                                       ParsedPersistenceUnit parsedData,
                                       ServiceReference providerRef) {
        super(bundle, parsedData, providerRef);
        this.bundle = bundle;
        this.transformers = new ArrayList<TransformerWrapper>();
    }

    @Override
    public void addTransformer(ClassTransformer classTransformer) {
        TransformerWrapper transformer = new TransformerWrapper(classTransformer, bundle);
        org.apache.geronimo.transformer.TransformerAgent.addTransformer(transformer);
        transformers.add(transformer);
    }

    public void destroy() {
        for (TransformerWrapper transformer : transformers) {
            org.apache.geronimo.transformer.TransformerAgent.removeTransformer(transformer);
        }
        transformers.clear();
    }
}
