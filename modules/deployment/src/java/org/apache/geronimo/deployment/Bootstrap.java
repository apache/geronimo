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
import java.io.FileOutputStream;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.geronimo.deployment.service.ServiceConfigBuilder;
import org.apache.geronimo.deployment.xbeans.ConfigurationDocument;
import org.apache.geronimo.deployment.xbeans.ConfigurationType;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.system.main.CommandLine;
import org.apache.geronimo.system.repository.ReadOnlyRepository;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlObject;

/**
 * Helper class to bootstrap the Geronimo deployer.
 *
 * @version $Revision: 1.10 $ $Date: 2004/02/25 08:03:53 $
 */
public class Bootstrap {
    private String deployerJar;
    private String storeDir;
    private String repositoryDir;
    private String deployerSystemPlan;
    private String j2eeDeployerPlan;
    private String deployerClassPath;
    private String deployerGBean;

    public String getDeployerJar() {
        return deployerJar;
    }

    public void setDeployerJar(String deployerJar) {
        this.deployerJar = deployerJar;
    }

    public String getStoreDir() {
        return storeDir;
    }

    public void setStoreDir(String storeDir) {
        this.storeDir = storeDir;
    }

    public String getRepositoryDir() {
        return repositoryDir;
    }

    public void setRepositoryDir(String repositoryDir) {
        this.repositoryDir = repositoryDir;
    }

    public String getDeployerSystemPlan() {
        return deployerSystemPlan;
    }

    public void setDeployerSystemPlan(String deployerSystemPlan) {
        this.deployerSystemPlan = deployerSystemPlan;
    }

    public String getJ2eeDeployerPlan() {
        return j2eeDeployerPlan;
    }

    public void setJ2eeDeployerPlan(String j2eeDeployerPlan) {
        this.j2eeDeployerPlan = j2eeDeployerPlan;
    }

    public String getDeployerClassPath() {
        return deployerClassPath;
    }

    public void setDeployerClassPath(String deployerClassPath) {
        this.deployerClassPath = deployerClassPath;
    }

    public String getDeployerGBean() {
        return deployerGBean;
    }

    public void setDeployerGBean(String deployerGBean) {
        this.deployerGBean = deployerGBean;
    }

    public void bootstrap() throws Exception {
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Bootstrap.class.getClassLoader());
        try {
            // parse the deployment-system and j2ee-deployer plans
            XmlObject deployerSystemXML = XmlBeans.getContextTypeLoader().parse(new File(deployerSystemPlan), null, null);
            XmlObject j2eeDeployerXML = XmlBeans.getContextTypeLoader().parse(new File(j2eeDeployerPlan), null, null);
            ConfigurationType j2eeDeployerConfig = ((ConfigurationDocument) j2eeDeployerXML).getConfiguration();

            // create the service builder, repository and config store objects
            LocalConfigStore configStore = new LocalConfigStore(new File(storeDir));
            ReadOnlyRepository repository = new ReadOnlyRepository(new File(repositoryDir));
            ServiceConfigBuilder builder = new ServiceConfigBuilder(repository);

            // create the manifext
            Manifest manifest = new Manifest();
            Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.putValue(Attributes.Name.MANIFEST_VERSION.toString(), "1.0");
            mainAttributes.putValue(Attributes.Name.MAIN_CLASS.toString(), CommandLine.class.getName());
            mainAttributes.putValue(Attributes.Name.CLASS_PATH.toString(), deployerClassPath);
            mainAttributes.putValue(CommandLine.MAIN_GBEAN.toString(), deployerGBean);
            mainAttributes.putValue(CommandLine.MAIN_METHOD.toString(), "deploy");
            mainAttributes.putValue(CommandLine.CONFIGURATIONS.toString(), j2eeDeployerConfig.getConfigId());

            // build and install the deployer-system configuration
            // write the deployer system out to a jar
            File outputFile = new File(deployerJar);
            JarOutputStream jos = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)), manifest);
            try {
                // add the startup jar entry which allows us to locat the startup directory
                jos.putNextEntry(new ZipEntry("META-INF/startup-jar"));
                jos.closeEntry();

                // add the deployment system configuration to the jar
                builder.buildConfiguration(jos, deployerSystemXML);
            } finally {
                jos.close();
            }
            configStore.install(outputFile.toURL());

            // build and install the j2ee-deployer configuration
            File tempFile = File.createTempFile("j2ee-deployer", ".car");
            try {
                builder.buildConfiguration(tempFile, (JarInputStream)null, j2eeDeployerXML);
                configStore.install(tempFile.toURL());
            } finally {
                tempFile.delete();
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCL);
        }
    }
}
