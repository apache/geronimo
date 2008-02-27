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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentConfigurationManager;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.PortInfo;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationResolver;
import org.apache.geronimo.kernel.config.NoSuchConfigException;
import org.apache.geronimo.kernel.repository.Repository;

public class WsdlGenerator {

    private static final Log LOG = LogFactory.getLog(WsdlGenerator.class);
    
    private final static String FORK_WSGEN_PROPERTY = 
        "org.apache.geronimo.jaxws.wsgen.fork";
    
    private final static String FORK_TIMEOUT_WSGEN_PROPERTY = 
        "org.apache.geronimo.jaxws.wsgen.fork.timeout";
    
    private final static long FORK_POLL_FREQUENCY = 1000 * 2; // 2 seconds
    
    private QName wsdlService;
    private QName wsdlPort;
    private boolean forkWsgen = getForkWsgen();
    private long forkTimeout = getForTimeout();
    private JAXWSTools jaxwsTools;
        
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
    
    private static long getForTimeout() {
        String value = System.getProperty(FORK_TIMEOUT_WSGEN_PROPERTY);
        if (value != null) {
            return Long.parseLong(value);
        } else {
            return 1000 * 60; // 60 seconds
        }
    }
    
    public WsdlGenerator() {
        this.jaxwsTools = new JAXWSTools();
    }
    
    public void setSunSAAJ() {
        this.jaxwsTools.setUseSunSAAJ();
    }
    
    public void setAxis2SAAJ() {
        this.jaxwsTools.setUseAxis2SAAJ();
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
    
    private URL[] getWsgenClasspath(DeploymentContext context) throws Exception {
        DeploymentConfigurationManager cm = (DeploymentConfigurationManager)context.getConfigurationManager();
        Collection<? extends Repository> repositories = cm.getRepositories();
        File[] jars = this.jaxwsTools.getClasspath(repositories);
        return JAXWSTools.toURL(jars);
    }
    
    private static void getModuleClasspath(Module module, DeploymentContext context, StringBuilder classpath) throws DeploymentException {        
        getModuleClasspath(classpath, module.getEarContext());       
        if (module.getRootEarContext() != module.getEarContext()) {
            getModuleClasspath(classpath, module.getRootEarContext());
        }         
    }

    private static void getModuleClasspath(StringBuilder classpath, DeploymentContext deploymentContext) throws DeploymentException {
        Configuration configuration = deploymentContext.getConfiguration();
        ConfigurationResolver resolver = configuration.getConfigurationResolver();
        List<String> moduleClassPath = configuration.getClassPath();
        for (String pattern : moduleClassPath) {
            try {
                Set<URL> files = resolver.resolve(pattern);
                for (URL url: files) {
                    String path = toFileName(url);
                    classpath.append(path).append(File.pathSeparator);
                }
            } catch (MalformedURLException e) {
                throw new DeploymentException("Could not resolve pattern: " + pattern, e);
            } catch (NoSuchConfigException e) {
                throw new DeploymentException("Could not resolve pattern: " + pattern, e);
            }
        }
    }

    private static File toFile(URL url) {
        if (url == null || !url.getProtocol().equals("file")) {
            return null;
        } else {
            String filename = toFileName(url);
            return new File(filename);
        }
    }

    private static String toFileName(URL url) {
        String filename = url.getFile().replace('/', File.separatorChar);
        int pos =0;
        while ((pos = filename.indexOf('%', pos)) >= 0) {
            if (pos + 2 < filename.length()) {
                String hexStr = filename.substring(pos + 1, pos + 3);
                char ch = (char) Integer.parseInt(hexStr, 16);
                filename = filename.substring(0, pos) + ch + filename.substring(pos + 3);
            }
        }
        return filename;
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
        } catch (Exception e) {
            throw new DeploymentException("Failed to generate the wsdl file using wsgen: unable to get the location of the required artifact(s).", e);
        } 
        //let's figure out the classpath string for the module and wsgen tools.
        if (urls != null && urls.length > 0) {
            for (URL url : urls) {
                classPath.append(toFile(url).getAbsolutePath()).append(File.pathSeparator);
            }
        }
        getModuleClasspath(module, context, classPath);

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
        
        if (LOG.isDebugEnabled()) {
            byte [] arr = os.toByteArray();
            String wsgenOutput = new String(arr, 0, arr.length);
            LOG.debug("wsgen output: " + wsgenOutput);
        }
        
        return rs;
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
              
        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);
                
        Process process = builder.start();
        return waitFor(process);
    }
    
    private boolean waitFor(Process process) throws DeploymentException {  
        CaptureOutputThread outputThread = new CaptureOutputThread(process.getInputStream());
        outputThread.start();        
                
        long sleepTime = 0;        
        while(sleepTime < this.forkTimeout) {            
            try {
                int errorCode = process.exitValue();
                if (errorCode == 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("wsgen output: " + outputThread.getOutput());
                    }
                    return true;
                } else {
                    LOG.error("WSDL generation process failed");
                    LOG.error(outputThread.getOutput()); 
                    return false;
                }
            } catch (IllegalThreadStateException e) {
                // still running
                try {
                    Thread.sleep(FORK_POLL_FREQUENCY);
                } catch (InterruptedException ee) {
                    // interrupted
                    process.destroy();
                    throw new DeploymentException("WSDL generation process was interrupted");
                }
                sleepTime += FORK_POLL_FREQUENCY;
            }
        }
        
        // timeout;
        process.destroy();
        
        LOG.error("WSDL generation process timed out");
        LOG.error(outputThread.getOutput());          
        
        throw new DeploymentException("WSDL generation process timed out");
    }
    
    private static class CaptureOutputThread extends Thread {
        
        private InputStream in;
        private ByteArrayOutputStream out;
        
        public CaptureOutputThread(InputStream in) {
            this.in = in;
            this.out = new ByteArrayOutputStream();
        }
        
        public String getOutput() {
            // make sure the thread is done
            try {
                join(10 * 1000);
                
                // if it's still not done, interrupt it
                if (isAlive()) {
                    interrupt();
                }
            } catch (InterruptedException e) {
                // that's ok
            }            
            
            // get the output
            byte [] arr = this.out.toByteArray();
            String output = new String(arr, 0, arr.length);
            return output;
        }
        
        public void run() {
            try {
                copyAll(this.in, this.out);
            } catch (IOException e) {
                // ignore
            } finally {
                try { this.out.close(); } catch (IOException ee) {}
                try { this.in.close(); } catch (IOException ee) {}
            }
        }
        
        private static void copyAll(InputStream in, OutputStream out) throws IOException {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.flush();
        }
    }
}
