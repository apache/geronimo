/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.webservices.jaxr;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import javax.xml.registry.ConnectionFactory;
import javax.xml.registry.JAXRException;

/**
 * Simple GBean to provide access to a JAXR ConnectionFactory
 *
 * @version $Rev$ $Date$
 *
 */
public class JAXRGBean {

   public static final GBeanInfo GBEAN_INFO;

    private final Log log = LogFactory.getLog(JAXRGBean.class);

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(JAXRGBean.class,
                NameFactory.JAXR_CONNECTION_FACTORY);

        infoFactory.addOperation("$getResource");

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


    /**
     *  Returns a fresh, new implementation of javax.xml.registry.ConnectionFactory
     *
     * @return
     */
    public Object $getResource() {

        try {
            return ConnectionFactory.newInstance();
        }
        catch(JAXRException e) {
            log.error("Error creating ConnectionFactory", e);
        }

        return null;
    }
}
