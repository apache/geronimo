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

package org.apache.geronimo.gbean;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoFactory;
import org.osgi.framework.Bundle;


/**
 *
 * @version $Rev:$ $Date:$
 */
public class MultiGBeanInfoFactory implements GBeanInfoFactory, GBeanInfoFactoryRegistry {
    private final static CopyOnWriteArrayList<GBeanInfoFactory> FACTORIES = new CopyOnWriteArrayList<GBeanInfoFactory>();
    
    static {
        FACTORIES.add(new BasicGBeanInfoFactory());
        FACTORIES.add(new AnnotationGBeanInfoFactory());
    }

    public void registerFactory(GBeanInfoFactory factory) {
        FACTORIES.add(0, factory);
    }
    
    public void unregisterFactory(GBeanInfoFactory factory) {
        FACTORIES.remove(factory);
    }
    
    public GBeanInfo getGBeanInfo(Class clazz) throws GBeanInfoFactoryException {
        List<GBeanInfoFactoryException> errors = new ArrayList<GBeanInfoFactoryException>(2);
        for (GBeanInfoFactory factory : FACTORIES) {
            try {
                return factory.getGBeanInfo(clazz);
            } catch (GBeanInfoFactoryException e) {
                errors.add(e);
            }
        }
        throw new GBeanInfoFactoryException("Cannot create a GBeanInfo for [" + clazz + "], errors: " + errors, errors.get(errors.size() - 1));
    }

    public GBeanInfo getGBeanInfo(String className, Bundle bundle) throws GBeanInfoFactoryException {
        List<GBeanInfoFactoryException> errors = new ArrayList<GBeanInfoFactoryException>(2);
        for (GBeanInfoFactory factory : FACTORIES) {
            try {
                return factory.getGBeanInfo(className, bundle);
            } catch (GBeanInfoFactoryException e) {
                errors.add(e);
            }
        }
        throw new GBeanInfoFactoryException("Cannot create a GBeanInfo for [" + className + "], errors: " + errors, errors.get(errors.size() - 1));
    }
    
}