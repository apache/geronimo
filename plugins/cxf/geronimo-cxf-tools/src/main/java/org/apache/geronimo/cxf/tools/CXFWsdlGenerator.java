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

package org.apache.geronimo.cxf.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.wsdl.WsdlGenerator;
import org.apache.geronimo.jaxws.wsdl.WsdlGeneratorOptions;
import org.apache.geronimo.jaxws.wsdl.WsdlGeneratorUtils;

/**
 * Generate WSDL and other JAX-WS artifacts using CXF tools. 
  */
public class CXFWsdlGenerator implements WsdlGenerator {

    private ClassLoader classLoader;
    
    public CXFWsdlGenerator(AbstractName abstractName, ClassLoader classLoader) { 
        this.classLoader = classLoader;
    }
            
    public String generateWsdl(Module module,
                               String serviceClass,
                               DeploymentContext context,
                               WsdlGeneratorOptions options) throws DeploymentException {
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
                
        LinkedHashSet<URL> jars = new LinkedHashSet<URL>();
        WsdlGeneratorUtils.getClassLoaderClasspath(this.classLoader, jars);
        try {
            WsdlGeneratorUtils.getModuleClasspath(module, context, jars);
        } catch (Exception e) {
            throw new DeploymentException("WSDL generation failed: unable to determine module classpath", e);
        }
        String classpath = WsdlGeneratorUtils.buildClasspath(jars);
               
        List<String> cmd = buildArguments(serviceClass, classpath, baseDir, options);
        boolean result;
        try {
            result = WsdlGeneratorUtils.execJava(cmd, options.getForkTimeout());
        } catch (Exception e) {
            throw new DeploymentException("WSDL generation failed", e);
        }

        if (result) {
            //check to see if the file is created.
            String serviceName = getLocalPart(options.getWsdlService());
            File wsdlFile = WsdlGeneratorUtils.findWsdlFile(baseDir, serviceName);
            if (wsdlFile == null) {
                throw new DeploymentException("Unable to find the service wsdl file");
            }
            if (options.getAddToClassPath()) {
                try {
                    context.getConfiguration().addToClassPath(baseDir.getName());
                } catch (IOException e) {
                    throw new DeploymentException("Failed to update module classpath");
                }
            }
            return WsdlGeneratorUtils.getRelativeNameOrURL(moduleBase, wsdlFile);
        } else {
            throw new DeploymentException("WSDL generation failed");
        }         
    }

    private List<String> buildArguments(String sei, String classpath, File moduleBaseDir, WsdlGeneratorOptions options) {
        List<String> arguments = new ArrayList<String>(); 
        
        // java args
        arguments.add("-classpath");
        arguments.add(classpath);
        arguments.add("-Dorg.apache.cxf.nofastinfoset=true");
        arguments.add("org.apache.cxf.tools.java2ws.JavaToWS");
        
        // cmd args
        arguments.add("-d");
        arguments.add(moduleBaseDir.getAbsolutePath());
        arguments.add("-classdir");
        arguments.add(moduleBaseDir.getAbsolutePath());
        arguments.add("-server");
        arguments.add("-wsdl");
        arguments.add("-wrapperbean");
        arguments.add("-frontend");
        arguments.add("jaxws");
        
        String serviceName = getLocalPart(options.getWsdlService());        
        if (serviceName != null) {
            arguments.add("-servicename");
            arguments.add(serviceName);
        }
        
        String portName = getLocalPart(options.getWsdlPort());                
        if (portName != null) {
            arguments.add("-port");
            arguments.add(portName);
        }   
        
        String tns = getNamespace(options.getWsdlService(), options.getWsdlPort());
        if (tns != null) {
            arguments.add("-t");
            arguments.add(tns);
        }
        
        arguments.add(sei);
        
        return arguments;
    }
    
    private static String getLocalPart(QName name) {
        return (name == null) ? null : name.getLocalPart();
    }
    
    private static String getNamespace(QName service, QName port) {
        String ns = null;
        if (service != null) {
            ns = service.getNamespaceURI();
            if (ns.length() > 0) {
                return ns;
            }
        }
        if (port != null) {
            ns = port.getNamespaceURI();
            if (ns.length() > 0) {
                return ns;
            }
        }
        return ns;
    }
    
    /*
    private void invokeWsgen() {
        ToolContext env = new ToolContext();

        env.put(ToolConstants.CFG_CLASSNAME, serviceClass);
        env.put(ToolConstants.CFG_OUTPUTDIR, baseDir.getAbsolutePath());
        
        StringBuilder classPath = new StringBuilder();
        WsdlGeneratorUtils.getModuleClasspath(module, context, classPath);
        
        env.put(ToolConstants.CFG_CLASSPATH, cp + classPath.toString());
        
        String serviceName = getLocalPart(options.getWsdlService());        
        if (serviceName != null) {
            env.put(ToolConstants.CFG_SERVICENAME, serviceName);
        }
        
        String portName = getLocalPart(options.getWsdlPort());                
        if (portName != null) {
            env.put(ToolConstants.CFG_PORT, portName);
        }
        
        String tns = getNamespace(options.getWsdlService(), options.getWsdlPort());
        if (tns != null) {
            env.put(ToolConstants.CFG_TNS, tns);            
        }
        
        env.put(ToolConstants.CFG_WRAPPERBEAN, Boolean.TRUE);
        env.put(ToolConstants.CFG_WSDL, Boolean.TRUE);
        
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        
        Processor processor = new JavaToWSDLProcessor();
        processor.setEnvironment(env);
        Thread.currentThread().setContextClassLoader(this.classLoader);
        try {
            processor.process();
        } catch (ToolException e) {
            throw new DeploymentException("WSDL generation failed", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }        
    }
    */
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CXFWsdlGenerator.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addInterface(WsdlGenerator.class);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("abstractName", AbstractName.class, false);
        infoBuilder.setConstructor(new String [] {"abstractName", "classLoader"});
        
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
