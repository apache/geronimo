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

package org.apache.geronimo.axis2.builder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentConfigurationManager;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Version;

public class Axis2BuilderUtil {

    private final static Artifact AXIS2_JAXWS_API_ARTIFACT = new Artifact("org.apache.axis2","axis2-jaxws-api", (Version)null, "jar");
    private final static Artifact AXIS2_SAAJ_API_ARTIFACT = new Artifact("org.apache.axis2","axis2-saaj-api", (Version)null, "jar");
    private final static Artifact AXIS2_SAAJ_ARTIFACT = new Artifact("org.apache.axis2","axis2-saaj", (Version)null, "jar");
    private final static Artifact JAXB_API_ARTIFACT = new Artifact("javax.xml.bind","jaxb-api", (Version)null, "jar");
    private final static Artifact JAXB_IMPL_ARTIFACT = new Artifact("com.sun.xml.bind","jaxb-impl", (Version)null, "jar");
    private final static Artifact JAXB_XJC_ARTIFACT = new Artifact("com.sun.xml.bind","jaxb-xjc", (Version)null, "jar");    
    private final static Artifact JAXWS_TOOLS_ARTIFACT = new Artifact("com.sun.xml.ws","jaxws-tools", (Version)null, "jar");
    private final static Artifact JAXWS_RT_ARTIFACT = new Artifact("com.sun.xml.ws","jaxws-rt", (Version)null, "jar");
    private final static Artifact GERONIMO_ACTIVATION_SPEC_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-activation_1.1_spec", (Version)null, "jar");    
    private final static Artifact GERONIMO_ANNOTATION_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-annotation_1.0_spec", (Version)null, "jar");     
    private final static Artifact GERONIMO_WS_METADATA_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-ws-metadata_2.0_spec", (Version)null, "jar");    
    private final static Artifact SAAJRI_IMPL_ARTIFACT = new Artifact("com.sun.xml.messaging.saaj","saaj-impl", (Version)null, "jar");
    private final static String TOOLS = "tools.jar";
    
    protected static URL[] getWsgenClasspath(DeploymentContext context) 
        throws DeploymentException, MalformedURLException {
        ArrayList<URL> jars = new ArrayList();
        
        DeploymentConfigurationManager cm = (DeploymentConfigurationManager)context.getConfigurationManager();
        Collection<ListableRepository> repositories = cm.getRepositories();

        jars.add(getLocation(repositories, JAXB_API_ARTIFACT));
        jars.add(getLocation(repositories, JAXB_IMPL_ARTIFACT));
        jars.add(getLocation(repositories, JAXB_XJC_ARTIFACT));
        jars.add(getLocation(repositories, JAXWS_TOOLS_ARTIFACT));
        jars.add(getLocation(repositories, JAXWS_RT_ARTIFACT));
        jars.add(getLocation(repositories, AXIS2_JAXWS_API_ARTIFACT));
        jars.add(getLocation(repositories, AXIS2_SAAJ_API_ARTIFACT));
//        jars.add(getLocation(repositories, AXIS2_SAAJ_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_ACTIVATION_SPEC_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_ANNOTATION_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_WS_METADATA_ARTIFACT));
        jars.add(getLocation(repositories, SAAJRI_IMPL_ARTIFACT));
        jars.add(new File(getToolsJarLoc()).toURL());
         
        return jars.toArray(new URL[jars.size()]);
        
    }
    
    protected static String getModuleClasspath(Module module, DeploymentContext context) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        String baseDir = moduleContext.getBaseDir().getAbsolutePath();
        List<String> moduleClassPath = context.getConfiguration().getClassPath();
        String classpath = "";
        for (String s : moduleClassPath) {
            if (s.contains("/"))
                s = s.replace("/", java.io.File.separator);
            classpath += baseDir + java.io.File.separator + s + java.io.File.pathSeparator;
        }
        return classpath;
    }
    
    private static URL getLocation(Collection<ListableRepository> repositories, Artifact artifactQuery) throws DeploymentException, MalformedURLException {
        File file = null;
        
        for (ListableRepository repository : repositories) {
            SortedSet artifactSet = repository.list(artifactQuery);
            // if we have exactly one artifact found
            if (artifactSet.size() == 1) {
                file = repository.getLocation((Artifact) artifactSet.first());
                return file.getAbsoluteFile().toURL();
            } else if (artifactSet.size() > 1) {// if we have more than 1 artifacts found use the latest one.
                file = repository.getLocation((Artifact) artifactSet.last());
                return file.getAbsoluteFile().toURL();
            } 
        }
        if (file == null) {
            throw new DeploymentException("Missing artifact in repositories: " + artifactQuery.toString());
        }
        return null;
    }
    
    private static String getToolsJarLoc() throws DeploymentException {
        //create a new File then check exists()
        String jreHomePath = System.getProperty("java.home");
        String javaHomePath = "";
        int jreHomePathLength = jreHomePath.length();
        if (jreHomePathLength > 0) {
            int i = jreHomePath.substring(0, jreHomePathLength -1).lastIndexOf(java.io.File.separator);
            javaHomePath = jreHomePath.substring(0, i);
        }
        File jdkhomelib = new File(javaHomePath + java.io.File.separator + "lib");
        if (!jdkhomelib.exists()) {
            throw new DeploymentException("Missing " + jdkhomelib.getAbsolutePath() 
                    + ". This is required for wsgen to run. ");
        }
        else {
            File tools = new File(jdkhomelib + java.io.File.separator + TOOLS);
            if (!tools.exists()) {
                throw new DeploymentException("Missing tools.jar in" + jdkhomelib.getAbsolutePath() 
                        + ". This is required for wsgen to run. ");                
            } else {
                return tools.getAbsolutePath();
            }               
        }
    }
    
    protected static File toFile(URL url) {
        if (url == null || !url.getProtocol().equals("file")) {
            return null;
        } else {
            String filename = url.getFile().replace('/', File.separatorChar);
            int pos =0;
            while ((pos = filename.indexOf('%', pos)) >= 0) {
                if (pos + 2 < filename.length()) {
                    String hexStr = filename.substring(pos + 1, pos + 3);
                    char ch = (char) Integer.parseInt(hexStr, 16);
                    filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
                }
            }
            return new File(filename);
        }
    }
    
    protected static String[] buildArguments(String classPath, String sei, String bindingType, File moduleBaseDir, PortInfo portInfo) {
        String[] arguments = null; 
        QName serviceName = portInfo.getWsdlService();
        QName portName = portInfo.getWsdlPort();
        
        if(bindingType == null || bindingType.equals("") || bindingType.equals(
        SOAPBinding.SOAP11HTTP_BINDING) || bindingType.equals(
                SOAPBinding.SOAP11HTTP_MTOM_BINDING)) {
            if (serviceName == null && portName == null)
                arguments = new String[]{"-cp", classPath, sei, "-keep", "-wsdl:soap1.1", "-d",
                    moduleBaseDir.getAbsolutePath()};
            else if (serviceName == null)
                arguments = new String[]{"-cp", classPath, sei, "-keep", "-wsdl:soap1.1", "-d",
                    moduleBaseDir.getAbsolutePath(), "-servicename", serviceName.toString()};
            else if (portName == null)
                arguments = new String[]{"-cp", classPath, sei, "-keep", "-wsdl:soap1.1", "-d",
                    moduleBaseDir.getAbsolutePath(), "-portname", portName.toString()};
            else 
                arguments = new String[]{"-cp", classPath, sei, "-keep", "-wsdl:soap1.1", "-d",
                    moduleBaseDir.getAbsolutePath(), "-servicename", serviceName.toString(), 
                    "-portname", portName.toString()};
        } else if (bindingType.equals(SOAPBinding.SOAP12HTTP_BINDING) || bindingType.equals(
            SOAPBinding.SOAP12HTTP_MTOM_BINDING)) { 
            //Xsoap1.2 is not standard and can only be
            //used in conjunction with the -extension option
            if (serviceName == null && portName == null)
                arguments =  new String[]{"-cp", classPath, sei, "-keep", "-extension",
                    "-wsdl:Xsoap1.2", "-d", moduleBaseDir.getAbsolutePath()};
            else if (serviceName == null)
                arguments =  new String[]{"-cp", classPath, sei, "-keep", "-extension",
                    "-wsdl:Xsoap1.2", "-d", moduleBaseDir.getAbsolutePath(), "-servicename", 
                    serviceName.toString()};  
            else if (portName == null)
                arguments =  new String[]{"-cp", classPath, sei, "-keep", "-extension",
                    "-wsdl:Xsoap1.2", "-d", moduleBaseDir.getAbsolutePath(), "-portname", 
                    portName.toString()}; 
            else
                arguments =  new String[]{"-cp", classPath, sei, "-keep", "-extension",
                    "-wsdl:Xsoap1.2", "-d", moduleBaseDir.getAbsolutePath(), "-servicename", 
                    serviceName.toString(), "-portname", portName.toString()}; 
        } else {
            throw new WebServiceException("The bindingType specified by " + sei 
                + " is not supported and cannot be used to generate a wsdl");
       }
        
        return arguments;
    }
    
    protected static String getWsdlFileLoc(File moduleBaseDir, PortInfo portInfo) {
        QName serviceName = portInfo.getWsdlService();

        if (serviceName != null) {
            //check if serviceName.wsdl locates at the moduleBaseDir, if so, return its path.
            File wsdlFile = new File(moduleBaseDir.getAbsolutePath() + java.io.File.separator + 
                    serviceName.getLocalPart() + ".wsdl");
            if (wsdlFile.exists())
                return serviceName.getLocalPart() + ".wsdl";
        } else {//scan the moduleBaseDir and return the first wsdl file found
            if(moduleBaseDir.isDirectory()) {
                File[] files = moduleBaseDir.listFiles();
                for(File file : files) {
                    String fileName = file.getName();
                    if(fileName.endsWith(".wsdl")) {
                        return fileName;
                    }
                }
            }
        }
       //unable to find the wsdl file.
       return "";   
    }
}
