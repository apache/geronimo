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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.geronimo.gbean.annotation.AnnotationGBeanInfoFactory;


/**
 *
 * @version $Rev:$ $Date:$
 */
public class MultiGBeanInfoFactory extends AbstractGBeanInfoFactory implements GBeanInfoFactoryRegistry {
    private final static CopyOnWriteArrayList<GBeanInfoFactory> factories = new CopyOnWriteArrayList<GBeanInfoFactory>();
    
    static {
        factories.add(new BasicGBeanInfoFactory());
        factories.add(new AnnotationGBeanInfoFactory());
    }

    public void registerFactory(GBeanInfoFactory factory) {
        factories.add(0, factory);
    }
    
    public void unregisterFactory(GBeanInfoFactory factory) {
        factories.remove(factory);
    }
    
    public GBeanInfo getGBeanInfo(Class clazz) throws GBeanInfoFactoryException {
        for (GBeanInfoFactory factory : factories) {
            try {
                return factory.getGBeanInfo(clazz);
            } catch (GBeanInfoFactoryException e) {
            }
        }
        throw new GBeanInfoFactoryException("Cannot create a GBeanInfo for [" + clazz + "]");
    }

}