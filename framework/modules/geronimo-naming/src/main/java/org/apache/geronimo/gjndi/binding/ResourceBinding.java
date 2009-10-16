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
package org.apache.geronimo.gjndi.binding;

import javax.naming.NamingException;
import javax.naming.Name;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.ResourceSource;

/**
 * @version $Rev$ $Date$
 */
public class ResourceBinding extends GBeanFormatBinding {

    public ResourceBinding(String format, String namePattern, String nameInNamespace, AbstractNameQuery abstractNameQuery, Kernel kernel) throws NamingException {
        super(format, namePattern, nameInNamespace, abstractNameQuery, kernel);
    }

    /**
     * Preprocess the value before it is bound.  This is usefult for wrapping values with reference objects.
     * By default, this method simply return the value.
     *
     * @param abstractName the abstract name of the gbean to bind
     * @param value        the gbean instance
     * @return the value to bind  or null if there was a problem
     */
    @Override
    protected Object preprocessVaue(AbstractName abstractName, Name name, Object value) {
        if (!(value instanceof ResourceSource)) {
            log.info("value at " + abstractName + " is not a ResourceSource: " + value.getClass().getName());
            return null;
        }
        try {
            return ((ResourceSource) value).$getResource();
        } catch (Throwable throwable) {
            log.info("Could not get resource from gbean at " + abstractName,throwable);
            return null;
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(ResourceBinding.class, GBeanFormatBinding.GBEAN_INFO, "Context");
        GBEAN_INFO = builder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}