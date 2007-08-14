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

package org.apache.geronimo.jaxws.builder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentConfigurationManager;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Version;

public class WsdlGenerator {

    private static final Log LOG = LogFactory.getLog(WsdlGenerator.class);
    
    private final static String FORK_WSGEN_PROPERTY = "org.apache.geronimo.jaxws.wsgen.fork";
    
    private final static Artifact AXIS2_JAXWS_API_ARTIFACT = new Artifact("org.apache.axis2","axis2-jaxws-api", (Version)null, "jar");
    private final static Artifact AXIS2_SAAJ_API_ARTIFACT = new Artifact("org.apache.axis2","axis2-saaj-api", (Version)null, "jar");
    private final static Artifact AXIS2_SAAJ_IMPL_ARTIFACT = new Artifact("org.apache.axis2","axis2-saaj", (Version)null, "jar");
    private final static Artifact JAXB_API_ARTIFACT = new Artifact("javax.xml.bind","jaxb-api", (Version)null, "jar");
    private final static Artifact JAXB_IMPL_ARTIFACT = new Artifact("com.sun.xml.bind","jaxb-impl", (Version)null, "jar");
    private final static Artifact JAXB_XJC_ARTIFACT = new Artifact("com.sun.xml.bind","jaxb-xjc", (Version)null, "jar");    
    private final static Artifact JAXWS_TOOLS_ARTIFACT = new Artifact("com.sun.xml.ws","jaxws-tools", (Version)null, "jar");
    private final static Artifact JAXWS_RT_ARTIFACT = new Artifact("com.sun.xml.ws","jaxws-rt", (Version)null, "jar");
    private final static Artifact GERONIMO_ACTIVATION_SPEC_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-activation_1.1_spec", (Version)null, "jar");    
    private final static Artifact GERONIMO_ANNOTATION_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-annotation_1.0_spec", (Version)null, "jar");     
    private final static Artifact GERONIMO_WS_METADATA_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-ws-metadata_2.0_spec", (Version)null, "jar");  
    private final static Artifact GERONIMO_EJB_SPEC_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-ejb_3.0_spec", (Version)null, "jar");
    private final static Artifact SUN_SAAJ_IMPL_ARTIFACT = new Artifact("com.sun.xml.messaging.saaj","saaj-impl", (Version)null, "jar");
    private final static String TOOLS = "tools.jar";

    private Artifact saajImpl;
    private QName wsdlService;
    private QName wsdlPort;
    private boolean forkWsgen = getForkWsgen();
        
    private static boolean getForkWsgen() {
        String value = System.getProperty(FORK_WSGEN_PROPERTY);
        if (value != null) {
            return Boolean.valueOf(value).booleanValue();
        } else {
            String osName = System.getProperty("os.name");
            if (osName == null) {
                return false;
            }
            osName = osName.toLowerCase();
            // Fork on Windows only
            return (osName.indexOf("windows") != -1);            
        }
    }
    
    public WsdlGenerator() {
    }
    
    public void setSunSAAJ() {
        this.saajImpl = SUN_SAAJ_IMPL_ARTIFACT;
    }
    
    public void setAxis2SAAJ() {
        this.saajImpl = AXIS2_SAAJ_IMPL_ARTIFACT;
    }
    
    public void setWsdlService(QName name) {
        this.wsdlService = name;
    }
    
    public QName getWsdlService() {
        return this.wsdlService;        
    }
    
    public void setWsdlPort(QName port) {
        this.wsdlPort = port;
    }
    
    public QName getWsdlPort() {
        return this.wsdlPort;
    }
    
    private URL[] getWsgenClasspath(DeploymentContext context) 
        throws DeploymentException, MalformedURLException {
        ArrayList<URL> jars = new ArrayList<URL>();
        
        DeploymentConfigurationManager cm = (DeploymentConfigurationManager)context.getConfigurationManager();
        Collection<ListableRepository> repositories = cm.getRepositories();

        jars.add(getLocation(repositories, JAXB_API_ARTIFACT));
        jars.add(getLocation(repositories, JAXB_IMPL_ARTIFACT));
        jars.add(getLocation(repositories, JAXB_XJC_ARTIFACT));
        jars.add(getLocation(repositories, JAXWS_TOOLS_ARTIFACT));
        jars.add(getLocation(repositories, JAXWS_RT_ARTIFACT));
        jars.add(getLocation(repositories, AXIS2_JAXWS_API_ARTIFACT));
        jars.add(getLocation(repositories, AXIS2_SAAJ_API_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_ACTIVATION_SPEC_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_ANNOTATION_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_WS_METADATA_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_EJB_SPEC_ARTIFACT));
        if (this.saajImpl != null) {
            jars.add(getLocation(repositories, this.saajImpl));
        }
        jars.add(new File(getToolsJarLoc()).toURL());
         
        return jars.toArray(new URL[jars.size()]);        
    }
    
    private static String getModuleClasspath(Module module, DeploymentContext context) throws DeploymentException {
        File moduleBase = module.getEarContext().getBaseDir();
        File moduleBaseDir = (moduleBase.isFile()) ? moduleBase.getParentFile() : moduleBase;
        String baseDir = moduleBaseDir.getAbsolutePath();
        List<String> moduleClassPath = context.getConfiguration().getClassPath();
        StringBuilder classpath = new StringBuilder();
        for (String s : moduleClassPath) {          
            s = s.replace("/", File.separator);
                        
            classpath.append(baseDir);
            classpath.append(File.separator);
            classpath.append(s);
            classpath.append(File.pathSeparator);
        }
        return classpath.toString();
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
    
    private static File toFile(URL url) {
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
    
    private String[] buildArguments(String sei, String classPath, File moduleBaseDir, PortInfo portInfo) {
        List<String> arguments = new ArrayList<String>();
        
        arguments.add("-cp");
        arguments.add(classPath);
        arguments.add("-keep");
        arguments.add("-wsdl");
        arguments.add("-d");
        arguments.add(moduleBaseDir.getAbsolutePath());
        
        QName serviceName = getWsdlService();
        if (serviceName != null) {
            arguments.add("-servicename");
            arguments.add(serviceName.toString());
        }

        QName portName = getWsdlPort();
        if (portName != null) {
            arguments.add("-portname");
            arguments.add(portName.toString());
        }
        
        arguments.add(sei);
        
        return arguments.toArray(new String[]{});
    }
    
    private File getFirstWsdlFile(File baseDir) throws IOException {
        LOG.debug("Looking for service wsdl file in " + baseDir.getAbsolutePath());
        File[] files = baseDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".wsdl"));
            }
        });

        if (files.length == 1) {
            return files[0];
        } else {
            return null;
        }
    }
    
    private File findWsdlFile(File baseDir, PortInfo portInfo) throws IOException {
        QName serviceName = getWsdlService();

        if (serviceName != null) {
            // check if serviceName.wsdl locates at the baseDir, if so, return its path.
            String wsdlFileName = serviceName.getLocalPart() + ".wsdl";
            if (Character.isLowerCase(wsdlFileName.charAt(0))) {
                wsdlFileName = Character.toUpperCase(wsdlFileName.charAt(0)) + wsdlFileName.substring(1);
            }
            File wsdlFile = new File(baseDir, wsdlFileName);
            if (wsdlFile.exists()) {
                return wsdlFile;
            } else {
                return getFirstWsdlFile(baseDir);
            }
        } else {
            return getFirstWsdlFile(baseDir);
        }
    }
    
    private static String getRelativeNameOrURL(File baseDir, File file) {
        String basePath = baseDir.getAbsolutePath();
        String path = file.getAbsolutePath();
        
        if (path.startsWith(basePath)) {
            if (File.separatorChar == path.charAt(basePath.length())) {
                return path.substring(basePath.length() + 1);
            } else {
                return path.substring(basePath.length());
            }
        } else {
            return file.toURI().toString();
        }
    }
    
    private static File createTempDirectory(File baseDir) throws IOException {
        Random rand = new Random();       
        while(true) {
            String dirName = String.valueOf(Math.abs(rand.nextInt()));        
            File dir = new File(baseDir, dirName);
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    throw new IOException("Failed to create temporary directory: " + dir);
                } else {
                    return dir;
                }
            }
        }               
    }
    
    public String generateWsdl(Module module, 
                               String serviceClass, 
                               DeploymentContext context, 
                               PortInfo portInfo) throws DeploymentException {
        //call wsgen tool to generate the wsdl file based on the bindingtype.
        //let's set the outputDir as the module base directory in server repository.
        File moduleBase = module.getEarContext().getBaseDir();
        File moduleBaseDir = (moduleBase.isFile()) ? moduleBase.getParentFile() : moduleBase;
        File baseDir;
        
        try {
            baseDir = createTempDirectory(moduleBaseDir);
        } catch (IOException e) {
            throw new DeploymentException(e);
        }
        
        URL[] urls;
        StringBuilder classPath = new StringBuilder();
        //let's figure out the classpath for wsgen tools
        try {
             urls = getWsgenClasspath(context);
        } catch (MalformedURLException e) {
            throw new DeploymentException("unable to generate the wsdl file using wsgen. - unable to get the location of the required artifact(s).", e);
        } 
        //let's figure out the classpath string for the module and wsgen tools.
        if (urls != null && urls.length > 0) {
            for (int i = 0; i< urls.length; i++) {
                classPath.append(toFile(urls[i]).getAbsolutePath() + File.pathSeparator);
            }
        }
        classPath.append(getModuleClasspath(module, context));

        //create arguments;
        String[] arguments = buildArguments(serviceClass, classPath.toString(), baseDir, portInfo);
        
        try {
            boolean result = false;
            
            if (this.forkWsgen) {
                result = forkWsgen(classPath, arguments);
            } else {
                result = invokeWsgen(urls, arguments);
            }
            
            if (result) {
                //check to see if the file is created.
                File wsdlFile = findWsdlFile(baseDir, portInfo);
                if (wsdlFile == null) {
                    throw new DeploymentException("Unable to find the service wsdl file");
                }
                return getRelativeNameOrURL(moduleBase, wsdlFile);
            } else {
                throw new DeploymentException("wsgen failed");
            }            
                                 
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException("Unable to generate the wsdl file using wsgen.", e);
        }
    }

    private boolean invokeWsgen(URL[] urls, String[] arguments) throws Exception {
        URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        Class clazz = loader.loadClass("com.sun.tools.ws.spi.WSToolsObjectFactory");
        Method method = clazz.getMethod("newInstance");
        Object factory = method.invoke(null);
        Method method2 = clazz.getMethod("wsgen", OutputStream.class, String[].class);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        LOG.debug("Invoking wsgen");
        
        Boolean result = (Boolean) method2.invoke(factory, os, arguments);
        os.close();
        
        byte [] arr = os.toByteArray();
        String wsgenOutput = new String(arr, 0, arr.length);
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("wsgen output: " + wsgenOutput);
        }
        
        return result;
    }
    
    private boolean forkWsgen(StringBuilder classPath, String[] arguments) throws Exception {           
        List<String> cmd = new ArrayList<String>();
        String javaHome = System.getProperty("java.home");                       
        String java = javaHome + File.separator + "bin" + File.separator + "java";
        cmd.add(java);
        cmd.add("-classpath");
        cmd.add(classPath.toString());
        cmd.add("com.sun.tools.ws.WsGen");
        cmd.addAll(Arrays.asList(arguments));
        
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing wsgen: " + cmd);
        }
                
        String [] cmdArray = (String[]) cmd.toArray(new String[] {});
        
        Process process = Runtime.getRuntime().exec(cmdArray);
        int errorCode = process.waitFor();
        
        if (errorCode == 0) {
            return true;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("wsgen error code: " + errorCode);
            }
            return false;
        }
    }
}
