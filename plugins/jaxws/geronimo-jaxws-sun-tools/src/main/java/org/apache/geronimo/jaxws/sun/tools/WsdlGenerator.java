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

package org.apache.geronimo.jaxws.sun.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGeneratorOptions;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGeneratorUtils;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsdlGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(WsdlGenerator.class);

    private JAXWSTools jaxwsTools;
    private WsdlGeneratorOptions options;

    public WsdlGenerator(WsdlGeneratorOptions options) {
        this.options = options;
        this.jaxwsTools = new JAXWSTools();
        this.jaxwsTools.setOverrideContextClassLoader(true);

        if (options.getSAAJ() == WsdlGeneratorOptions.SAAJ.SUN) {
            this.jaxwsTools.setUseSunSAAJ();
        } else if (options.getSAAJ() == WsdlGeneratorOptions.SAAJ.Axis2) {
            this.jaxwsTools.setUseAxis2SAAJ();
        }
    }

    private URL[] getWsgenClasspath(DeploymentContext context) throws Exception {
        ConfigurationManager cm = context.getConfigurationManager();
        Collection<? extends Repository> repositories = cm.getRepositories();
        File[] jars = this.jaxwsTools.getClasspath(repositories);
        return JAXWSTools.toURL(jars);
    }

    private String getEndorsedPath(DeploymentContext context) throws Exception {
        ConfigurationManager cm = context.getConfigurationManager();
        Collection<? extends Repository> repositories = cm.getRepositories();
        return jaxwsTools.getEndorsedPath(repositories);
    }

    private String[] buildArguments(String sei, String classPath, File moduleBaseDir, PortInfo portInfo) throws Exception{
        List<String> arguments = new ArrayList<String>(11);
        arguments.add("-cp");
        arguments.add(classPath);
        arguments.add("-keep");
        arguments.add("-wsdl");
        arguments.add("-d");
        arguments.add(moduleBaseDir.getAbsolutePath());

        QName serviceName = this.options.getWsdlService();
        if (serviceName != null) {
            arguments.add("-servicename");
            arguments.add(serviceName.toString());
        }

        QName portName = this.options.getWsdlPort();
        if (portName != null) {
            arguments.add("-portname");
            arguments.add(portName.toString());
        }

        arguments.add(sei);

        return arguments.toArray(new String[]{});
    }

    private File findWsdlFile(File baseDir, PortInfo portInfo) {
        QName serviceQName = this.options.getWsdlService();
        String serviceName = (serviceQName == null) ? null : serviceQName.getLocalPart();
        return WsdlGeneratorUtils.findWsdlFile(baseDir, serviceName);
    }

    public String generateWsdl(Module module,
                               String serviceClass,
                               DeploymentContext context,
                               PortInfo portInfo) throws DeploymentException {
        //call wsgen tool to generate the wsdl file based on the bindingtype.
        //let's set the outputDir as the module base directory in server repository.
        File moduleBase = module.getEarContext().getInPlaceConfigurationDir();
        if (moduleBase == null) {
            moduleBase = module.getEarContext().getBaseDir();
        }
        File moduleBaseDir = (moduleBase.isFile()) ? moduleBase.getParentFile() : moduleBase;
        File baseDir;

        try {
            
            baseDir = WsdlGeneratorUtils.createTempDirectory(moduleBaseDir);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }

        URL[] urls;
        StringBuilder classPathBuilder = new StringBuilder();
        //let's figure out the classpath for wsgen tools
        try {
             urls = getWsgenClasspath(context);
        } catch (Exception e) {
            throw new DeploymentException("Failed to generate the wsdl file using wsgen: unable to get the location of the required artifact(s).", e);
        }

        //let's figure out the classpath string for the module and wsgen tools.
        if (urls != null && urls.length > 0) {
            for (URL url : urls) {
                classPathBuilder.append(WsdlGeneratorUtils.toFile(url).getAbsolutePath()).append(File.pathSeparator);
            }
        }
        String wsgenToolClassPath = classPathBuilder.toString();

        try {
            WsdlGeneratorUtils.getModuleClasspath(module, context, classPathBuilder);
        } catch (Exception e) {
            throw new DeploymentException("WSDL generation failed: unable to determine module classpath", e);
        }

        try {
            //create arguments;
            String[] arguments = buildArguments(serviceClass, classPathBuilder.toString(), baseDir, portInfo);

            boolean result = false;
            if (this.options.getFork()) {
                String endorsedPath = getEndorsedPath(context);
                result = forkWsgen(wsgenToolClassPath, endorsedPath, arguments);
            } else {
                result = invokeWsgen(urls, arguments);
            }

            if (result) {
                //check to see if the file is created.
                File wsdlFile = findWsdlFile(baseDir, portInfo);
                if (wsdlFile == null) {
                    throw new DeploymentException("Unable to find the service wsdl file");
                }
                if (this.options.getAddToClassPath()) {
                    String wsdlPath = WsdlGeneratorUtils.getRelativeNameOrURL(moduleBase, wsdlFile.getParentFile());
                    if(wsdlPath.endsWith("/")) {
                        wsdlPath = wsdlPath.substring(0, wsdlPath.length() - 1);
                    }
                    context.addToClassPath(wsdlPath);
                }
                return WsdlGeneratorUtils.getRelativeNameOrURL(moduleBase, wsdlFile);
            } else {
                throw new DeploymentException("WSDL generation failed");
            }

        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException("Unable to generate the wsdl file using wsgen.", e);
        }
    }

    private boolean invokeWsgen(URL[] jars, String[] arguments) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        boolean rs = this.jaxwsTools.invokeWsgen(jars, os, arguments);
        os.close();

        if (!rs) {
            LOG.error("WSDL generator failed: {}", getOutput(os));
        } else if (LOG.isDebugEnabled()) {
            LOG.debug("WSDL generator output: {}", getOutput(os));
        }

        return rs;
    }

    private static String getOutput(ByteArrayOutputStream os) {
        byte [] arr = os.toByteArray();
        return new String(arr, 0, arr.length);
    }

    private boolean forkWsgen(String classPath, String endorsedPath, String[] arguments) throws Exception {
        List<String> cmd = new ArrayList<String>(4 + arguments.length);
        cmd.add("-Djava.endorsed.dirs=" + endorsedPath);
        cmd.add("-classpath");
        cmd.add(classPath);
        cmd.add("com.sun.tools.ws.WsGen");
        cmd.addAll(Arrays.asList(arguments));

        try {
            return WsdlGeneratorUtils.execJava(cmd, this.options.getForkTimeout());
        } catch (Exception e) {
            throw new DeploymentException("WSDL generation failed", e);
        }
    }
}
