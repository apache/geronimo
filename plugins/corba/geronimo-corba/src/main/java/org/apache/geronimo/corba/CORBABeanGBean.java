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
package org.apache.geronimo.corba;

import java.net.InetSocketAddress;

import javax.ejb.spi.HandleDelegate;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.corba.security.config.ConfigAdapter;
import org.apache.geronimo.corba.security.config.ssl.SSLConfig;
import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.openejb.OpenEjbSystem; 
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

/**
 * @version $Revision$ $Date$
 */
public final class CORBABeanGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CORBABeanGBean.class, "OpenEJB ORB Adapter", CORBABean.class, NameFactory.CORBA_SERVICE);

        infoBuilder.addAttribute("abstractName", AbstractName.class, false);
        infoBuilder.addAttribute("host", String.class, true);
        infoBuilder.addAttribute("port", int.class, true);
        infoBuilder.addAttribute("tssConfig", TSSConfig.class, true);

        infoBuilder.addAttribute("listenAddress", InetSocketAddress.class, false);
        infoBuilder.addAttribute("ORB", ORB.class, false);
        infoBuilder.addAttribute("rootPOA", POA.class, false);

        infoBuilder.addAttribute("handleDelegate", HandleDelegate.class, false);

        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.addReference("ConfigAdapter", ConfigAdapter.class, NameFactory.ORB_CONFIG);
        infoBuilder.addReference("SSLConfig", SSLConfig.class, NameFactory.CORBA_SSL);
        infoBuilder.addReference("NameService", NameService.class, NameFactory.CORBA_NAME_SERVICE);

        infoBuilder.setConstructor(new String[]{"abstractName", "ConfigAdapter", "host", "port", "classLoader", "NameService", "SSLConfig"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
