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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import org.apache.geronimo.corba.security.config.ConfigAdapter;

import java.net.InetSocketAddress;

/**
 * @version $Revision: 465108 $ $Date: 2006-10-17 17:23:40 -0700 (Tue, 17 Oct 2006) $
 */
public final class NameServiceGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(NameServiceGBean.class, "CORBA Naming Service", NameService.class, NameFactory.CORBA_NAME_SERVICE);

        infoFactory.addReference("ServerInfo", ServerInfo.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoFactory.addReference("ConfigAdapter", ConfigAdapter.class, NameFactory.ORB_CONFIG);
        infoFactory.addAttribute("host", String.class, true);
        infoFactory.addAttribute("port", int.class, true);
        infoFactory.addAttribute("address", InetSocketAddress.class, false);
        infoFactory.addAttribute("local", boolean.class, true);
        infoFactory.setConstructor(new String[]{"ServerInfo", "ConfigAdapter", "host", "port"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
