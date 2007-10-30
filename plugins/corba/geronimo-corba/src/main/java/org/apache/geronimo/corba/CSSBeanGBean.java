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
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.corba.security.config.ConfigAdapter;
import org.apache.geronimo.corba.security.config.css.CSSConfig;
import org.apache.geronimo.corba.security.config.ssl.SSLConfig;
import org.omg.CORBA.ORB;

import java.net.URI;

import javax.transaction.TransactionManager;

/**
 * @version $Revision$ $Date$
 */
public final class CSSBeanGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(CSSBeanGBean.class, CSSBean.class, NameFactory.CORBA_CSS);

        infoFactory.addAttribute("description", String.class, true);
        infoFactory.addAttribute("cssConfig", CSSConfig.class, true);
        infoFactory.addAttribute("ORB", ORB.class, false);
        infoFactory.addOperation("getHome", new Class[]{URI.class, String.class});

        infoFactory.addReference("TransactionManager", TransactionManager.class, NameFactory.JTA_RESOURCE);
        infoFactory.addReference("SSLConfig", SSLConfig.class, NameFactory.CORBA_SSL);
        infoFactory.addReference("ConfigAdapter", ConfigAdapter.class, NameFactory.ORB_CONFIG);
        infoFactory.addAttribute("abstractName", AbstractName.class, false);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.setConstructor(new String[]{"ConfigAdapter", "TransactionManager", "SSLConfig", "abstractName", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
