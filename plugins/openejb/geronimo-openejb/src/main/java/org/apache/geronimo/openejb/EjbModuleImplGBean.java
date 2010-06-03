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
import java.util.Map;

import javax.naming.NamingException;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.jndi.ApplicationJndi;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.EJB;
import org.apache.openejb.assembler.classic.EjbJarInfo;

/**
 * This starts before the ejb gbeans
 * 
 * @version $Revision$ $Date$
 */
@GBean(j2eeType = NameFactory.EJB_MODULE)
public final class EjbModuleImplGBean extends EjbModuleImpl implements GBeanLifecycle {
    public EjbModuleImplGBean(@ParamSpecial(type = SpecialAttributeType.objectName) String objectName,
                              @ParamReference(name = "J2EEServer", namingType = NameFactory.J2EE_SERVER) J2EEServer server,
                              @ParamReference(name = "J2EEApplication", namingType = NameFactory.J2EE_APPLICATION) J2EEApplication application,
                              @ParamReference(name = "ApplicationJndi", namingType = "GBEAN") ApplicationJndi applicationJndi,
                              @ParamAttribute(name = "moduleJndi") Map<String, Object> moduleJndi,
                              @ParamAttribute(name = "deploymentDescriptor") String deploymentDescriptor,
                              @ParamReference(name = "EJBCollection") Collection<? extends EJB> ejbs,
                              @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader,
                              @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel, @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
                              @ParamAttribute(name = "ejbJarInfo") EjbJarInfo ejbJarInfo) throws NamingException {
        super(objectName, server, application, applicationJndi, moduleJndi, deploymentDescriptor, ejbs, classLoader, kernel, openEjbSystem, ejbJarInfo);
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

}
