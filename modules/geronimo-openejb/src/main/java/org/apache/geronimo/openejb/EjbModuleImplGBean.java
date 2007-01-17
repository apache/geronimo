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
package org.apache.geronimo.openejb;

import java.util.Collection;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.EJB;
import org.apache.openejb.assembler.classic.EjbJarInfo;

/**
 * @version $Revision$ $Date$
 */
public final class EjbModuleImplGBean extends EjbModuleImpl implements GBeanLifecycle {
    public EjbModuleImplGBean(String objectName, J2EEServer server, J2EEApplication application, String deploymentDescriptor, Collection<? extends EJB> ejbs, ClassLoader classLoader, OpenEjbSystem openEjbSystem, EjbJarInfo ejbJarInfo) {
        super(objectName, server, application, deploymentDescriptor, ejbs, classLoader, openEjbSystem, ejbJarInfo);
    }

    public void doStart() throws Exception {
        start();
    }

    public void doStop() throws Exception {
        stop();
    }

    public void doFail() {
        stop();
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EjbModuleImpl.class, NameFactory.EJB_MODULE);
        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);

        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);

        infoBuilder.addReference("EJBCollection", EJB.class);

        infoBuilder.addAttribute("classloader", ClassLoader.class, false);

        infoBuilder.addReference("OpenEjbSystem", OpenEjbSystem.class);
        infoBuilder.addAttribute("ejbJarInfo", EjbJarInfo.class, true);

        infoBuilder.setConstructor(new String[]{
                "objectName",
                "J2EEServer",
                "J2EEApplication",
                "deploymentDescriptor",
                "EJBCollection",
                "classloader",
                "OpenEjbSystem",
                "ejbJarInfo"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
