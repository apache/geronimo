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

package org.apache.geronimo.gbean.annotation;

import java.util.Dictionary;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanInfoFactoryException;
import org.apache.geronimo.gbean.InvalidConfigurationException;
import org.osgi.framework.Bundle;


/**
 *
 * @version $Rev:$ $Date:$
 */
public class AnnotationGBeanInfoFactory implements GBeanInfoFactory {

    public GBeanInfo getGBeanInfo(String className, Bundle bundle) throws GBeanInfoFactoryException {
        Class clazz;
        try {
            clazz = bundle.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigurationException("Could not load class " + className + " from bundle " + bundle + " at location " + bundle.getLocation(), e);
        } catch (NoClassDefFoundError e) {
            throw new InvalidConfigurationException("Could not load class " + className + " from bundle " + bundle + " at location " + bundle.getLocation(), e);
        }
        return getGBeanInfo(clazz);
    }

    public GBeanInfo getGBeanInfo(Class clazz) throws GBeanInfoFactoryException {
        AnnotationGBeanInfoBuilder infoFactory = new AnnotationGBeanInfoBuilder(clazz);
        try {
            return infoFactory.buildGBeanInfo();
        } catch (GBeanAnnotationException e) {
            throw new GBeanInfoFactoryException(e);
        }
    }

}
