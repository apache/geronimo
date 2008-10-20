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
package org.apache.geronimo.connector.outbound;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.geronimo.JCAConnectionFactory;
import org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory;

/**
 * 
 * @version $Revision$
 */
public class JCAConnectionFactoryImplGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(JCAConnectionFactoryImplGBean.class, JCAConnectionFactoryImpl.class, NameFactory.JCA_CONNECTION_FACTORY);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addReference("JCAManagedConnectionFactory", JCAManagedConnectionFactory.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoFactory.addInterface(JCAConnectionFactory.class);

        infoFactory.setConstructor(new String[]{"objectName", "JCAManagedConnectionFactory"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
