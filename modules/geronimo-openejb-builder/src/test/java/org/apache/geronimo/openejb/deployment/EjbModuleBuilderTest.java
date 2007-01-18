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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb.deployment;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import javax.transaction.TransactionManager;

import junit.framework.TestCase;
import org.apache.geronimo.openejb.EjbContainer;
import org.apache.geronimo.openejb.OpenEjbSystem;
import org.apache.geronimo.openejb.OpenEjbSystemGBean;
import org.apache.geronimo.transaction.manager.GeronimoTransactionManager;
import org.apache.openejb.alt.config.EjbModule;
import org.apache.openejb.alt.config.ejb.OpenejbJar;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.jee.EjbJar;

/**
 * @version $Rev$ $Date$
 */
public class EjbModuleBuilderTest extends TestCase {
    public void test() throws Exception {
        // create reference to openejb itests
        File file = new File(System.getProperty("user.home") + "/.m2/repository/org/apache/openejb/openejb-itests-beans/3.0-incubating-SNAPSHOT/openejb-itests-beans-3.0-incubating-SNAPSHOT.jar");
        if (!file.canRead()) return;
        JarFile moduleFile = new JarFile(file);

        TransactionManager transactionManager = new GeronimoTransactionManager();
        OpenEjbSystem openEjbSystem = new OpenEjbSystemGBean(transactionManager);

        addEjbContainer(openEjbSystem, "Default Stateless Container");
        addEjbContainer(openEjbSystem, "Default Stateful Container");
        addEjbContainer(openEjbSystem, "Default BMP Container");
        addEjbContainer(openEjbSystem, "Default CMP Container");

        // load ejb-jar.xml
        String ejbJarXml = XmlUtil.loadEjbJarXml(null, moduleFile);
        assertNotNull(ejbJarXml);
        EjbJar ejbJar = XmlUtil.unmarshal(EjbJar.class, ejbJarXml);

        // load openejb-jar.xml
        String openejbJarXml = XmlUtil.loadOpenejbJarXml(null, moduleFile);
        OpenejbJar openejbJar = XmlUtil.unmarshal(OpenejbJar.class, openejbJarXml);

        // create the module object
        ClassLoader classLoader = new URLClassLoader(new URL[] {file.toURL()}, getClass().getClassLoader());
        EjbModule ejbModule = new EjbModule(classLoader, moduleFile.getName(), ejbJar, openejbJar);

        // configure the application
//        EjbJarInfo ejbJarInfo = openEjbSystem.configureApplication(ejbModule);

//        openEjbSystem.createEjbJar(ejbJarInfo, classLoader);
    }

    private void addEjbContainer(OpenEjbSystem openEjbSystem, String id) throws Exception {
        EjbContainer ejbContainer = new EjbContainer();
        ejbContainer.setOpenEjbSystem(openEjbSystem);
        ejbContainer.setId(id);
        ejbContainer.doStart();
    }
}
