/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.deployment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import javax.management.ObjectName;

import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.LocalConfigStore;
import org.apache.geronimo.system.repository.ReadOnlyRepository;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * Helper class to bootstrap the Geronimo deployer.
 *
 * @version $Revision: 1.7 $ $Date: 2004/02/20 07:19:13 $
 */
public class Bootstrap {
    public static final URI CONFIG_ID = URI.create("org/apache/geronimo/ServiceDeployer");

    /**
     * Invoked from maven.xml during the build to create the first Deployment Configuration
     * @param car the configuration file to generate
     * @param store the store to install the configuration into
     */
    public static void bootstrap(String car, String store, String systemJar) {
        File carfile = new File(car);
        File storeDir = new File(store);
        File system = new File(systemJar);

        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Bootstrap.class.getClassLoader());
        try {
            Map gbeans = new HashMap();

            // Install ServerInfo GBean
            ObjectName serverName = new ObjectName("geronimo.deployer:role=ServerInfo");
            GBeanMBean server = new GBeanMBean(ServerInfo.getGBeanInfo());
            gbeans.put(serverName, server);

            // Install default local Repository
            ObjectName repoName = new ObjectName("geronimo.deployer:role=Repository,root=repository");
            GBeanMBean localRepo = new GBeanMBean(ReadOnlyRepository.GBEAN_INFO);
            localRepo.setAttribute("Root", URI.create("repository/"));
            localRepo.setReferencePatterns("ServerInfo", Collections.singleton(serverName));
            gbeans.put(repoName, localRepo);

            // Install ServiceConfigBuilder
            ObjectName builderName = new ObjectName("geronimo.deployer:role=Builder,type=Service,id=" + CONFIG_ID.toString());
            GBeanMBean serviceBuilder = new GBeanMBean(ServiceConfigBuilder.GBEAN_INFO);
            serviceBuilder.setReferencePatterns("Repository", Collections.singleton(repoName));
            serviceBuilder.setReferencePatterns("Kernel", Collections.singleton(Kernel.KERNEL));
            gbeans.put(builderName, serviceBuilder);

            // Install Deployer
            ObjectName deployerName = Deployer.getDeployerName(CONFIG_ID);
            GBeanMBean deployer = new GBeanMBean(Deployer.GBEAN_INFO);
            deployer.setReferencePatterns("Kernel", Collections.singleton(Kernel.KERNEL));
            deployer.setReferencePatterns("Builders", Collections.singleton(new ObjectName("geronimo.deployer:role=Builder,id=" + CONFIG_ID.toString() + ",*")));
            gbeans.put(deployerName, deployer);

            List classPath = new ArrayList();

            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(carfile)));
            try {
                URI path = URI.create(system.getName());
                addFile(jos, path, system);
                classPath.add(path);

                GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
                config.setAttribute("ID", CONFIG_ID);
                config.setReferencePatterns("Parent", null);
                config.setAttribute("ClassPath", classPath);
                config.setAttribute("GBeanState", Configuration.storeGBeans(gbeans));
                config.setAttribute("Dependencies", Collections.EMPTY_LIST);

                jos.putNextEntry(new ZipEntry("META-INF/config.ser"));
                ObjectOutputStream ois = new ObjectOutputStream(jos);
                Configuration.storeGMBeanState(config, ois);
                ois.flush();
                jos.closeEntry();
            } finally {
                jos.close();
            }

            LocalConfigStore configStore = new LocalConfigStore(storeDir);
            configStore.install(carfile.toURL());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }

    private static void addFile(JarOutputStream jos, URI path, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        try {
            jos.putNextEntry(new JarEntry(path.toString()));
            byte[] buffer = new byte[4096];
            int count;
            while ((count = fis.read(buffer)) > 0) {
                jos.write(buffer, 0, count);
            }
            jos.closeEntry();
        } finally {
            fis.close();
        }
    }
}
