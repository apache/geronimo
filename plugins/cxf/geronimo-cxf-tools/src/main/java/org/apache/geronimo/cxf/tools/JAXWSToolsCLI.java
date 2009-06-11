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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.MultiParentClassLoader;
import org.apache.geronimo.kernel.config.SimpleConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Maven2Repository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.configuration.RepositoryConfigurationStore;

public class JAXWSToolsCLI {
               
    private static final String CONFIG_ID = "org.apache.geronimo.configs/cxf-tools//car";
    
    enum Command { JAVA2WS, WSDL2JAVA }
    
    private static final String USAGE_MSG =
        "Usage: cxf-tools <toolName> <tool options>\n\n" +
        "where <toolName> is:\n" +
        "  java2ws     - generate portable artifacts from class\n" +
        "  wsdl2java   - generate portable artifacts from WSDL\n";
           
    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            System.err.println(USAGE_MSG);
            System.exit(1);
        }
        
        Command cmd = null;
        if (args[0].equalsIgnoreCase("java2ws")) {
            cmd = Command.JAVA2WS;
        } else if (args[0].equalsIgnoreCase("wsdl2java")) {
            cmd = Command.WSDL2JAVA;
        } else {
            System.err.println("Error: Unsupported toolName [" + args[0] + "].");
            System.err.println();
            System.err.println(USAGE_MSG);
            System.exit(1);
        }

        String geronimoHome = getGeronimoHome();
        
        String[] arguments = getCmdArguments(args); 
        boolean rs = run(cmd, geronimoHome, arguments);
        System.exit( (rs) ? 0 : 1 );
    }
    
    static boolean run(Command cmd, String geronimoHome, String[] args) throws Exception {   
        // check java.io.tmpdir
        resolveTmpDir(geronimoHome);
        // disable fastinfoset support
        System.setProperty("org.apache.cxf.nofastinfoset", "true");

        String repository = System.getProperty("Xorg.apache.geronimo.repository.boot.path", "repository");
        Maven2Repository bootRepository = new Maven2Repository(new File(geronimoHome, repository));
        Collection<Repository> repositories = Collections.<Repository>singleton(bootRepository);
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(new DefaultArtifactManager(), bootRepository);
        RepositoryConfigurationStore repositoryStore = new RepositoryConfigurationStore(bootRepository);
        Collection<ConfigurationStore> repositoryStores = Collections.<ConfigurationStore>singleton(repositoryStore);
        SimpleConfigurationManager manager = new SimpleConfigurationManager(repositoryStores, artifactResolver, repositories);
        
        Artifact id = Artifact.create(CONFIG_ID);
        id = artifactResolver.queryArtifact(id);
        manager.loadConfiguration(id);
        
        Configuration config = manager.getConfiguration(id);
                
        // set context classloader
        ClassLoader classLoader = config.getConfigurationClassLoader();
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();        
        Thread.currentThread().setContextClassLoader(classLoader);
                           
        // set "java.class.path" system property
        String classPath = getClasspath(classLoader);
        String oldClassPath = System.getProperty("java.class.path");
        System.setProperty("java.class.path", classPath);  
        
        try {
            if (cmd.equals(Command.JAVA2WS)) {
                invokeJava2WS(classLoader, args);
            } else if (cmd.equals(Command.WSDL2JAVA)) {
                invokeWSDL2Java(classLoader, args);
            } else {
                throw new IllegalArgumentException("Invalid command: " + cmd);
            }
        } catch (InvocationTargetException e) {
            Throwable exception = e.getTargetException();
            if (exception instanceof Exception) {
                throw (Exception)exception;
            } else {
                throw e;
            }
        } finally {
            // restore "java.class.path" system property
            System.setProperty("java.class.path", oldClassPath);
            // restore context classloader
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        
        return true;
    }
     
    private static void invokeJava2WS(ClassLoader classLoader, String [] args) throws Exception {
        Class clazz = classLoader.loadClass("org.apache.cxf.tools.java2ws.JavaToWS");
        Method method = clazz.getMethod("main", new Class[] {String[].class});
        method.invoke(null, new Object [] {args});  
    }
    
    private static void invokeWSDL2Java(ClassLoader classLoader, String [] args) throws Exception {
        Class clazz = classLoader.loadClass("org.apache.cxf.tools.wsdlto.WSDLToJava");
        Method method = clazz.getMethod("main", new Class[] {String[].class});
        method.invoke(null, new Object [] {args}); 
    }
    
    private static String[] getCmdArguments(String[] args) {
        String [] cmdArgs = new String[args.length - 1];
        System.arraycopy(args, 1, cmdArgs, 0, args.length - 1);
        return cmdArgs;
    }
    
    private static String getGeronimoHome() {
        String geronimoHome = System.getProperty("org.apache.geronimo.home.dir");
        if (geronimoHome != null) {
            return geronimoHome;
        }
        
        // guess from the location of the jar
        URL url = JAXWSToolsCLI.class.getClassLoader().getResource("META-INF/startup-jar");
        if (url != null) {
            try {
                JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
                url = jarConnection.getJarFileURL();

                URI baseURI = new URI(url.toString()).resolve("..");
                File dir = new File(baseURI);                
                return dir.getAbsolutePath();
            } catch (Exception ignored) {
                // ignore
            }
        }
        
        // cannot determine the directory, return parent directory
        return "..";        
    }
    
    private static void resolveTmpDir(String geronimoHome) throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir");
        File tmpDirFile = new File(tmpDir);
        if (!tmpDirFile.exists()) {
            if (tmpDirFile.isAbsolute()) {
                throw new Exception("The temporary directory set does not exist: " + tmpDir);
            } 
            File newTmpDirFile = new File(geronimoHome, tmpDir); 
            if (!newTmpDirFile.exists()) {
                throw new Exception("Unable to resolve temporary directory " + tmpDir + " in " + geronimoHome);
            }
            System.setProperty("java.io.tmpdir", newTmpDirFile.getAbsolutePath());
        }      
    }
    
    public static Set<URL> getClassLoaderClasspath(ClassLoader loader) {
        LinkedHashSet<URL> jars = new LinkedHashSet<URL>();
        getClassLoaderClasspath(loader, jars);
        return jars;        
    }
    
    public static void getClassLoaderClasspath(ClassLoader loader, LinkedHashSet<URL> classpath) {
        if (loader == null || loader == ClassLoader.getSystemClassLoader()) {
            return;
        } else if (loader instanceof MultiParentClassLoader) {
            MultiParentClassLoader cl = (MultiParentClassLoader)loader;
            for (ClassLoader parent : cl.getParents()) {   
                getClassLoaderClasspath(parent, classpath);
            }
            for (URL u : cl.getURLs()) {
                classpath.add(u);
            }
        } else if (loader instanceof URLClassLoader) {
            URLClassLoader cl = (URLClassLoader)loader;
            getClassLoaderClasspath(cl.getParent(), classpath);
            for (URL u : cl.getURLs()) {
                classpath.add(u);
            }
        } else {
            getClassLoaderClasspath(loader.getParent(), classpath);
        }
    }
    
    public static String buildClasspath(Set<URL> files) {
        StringBuilder classpath = new StringBuilder();
        buildClasspath(files, classpath);
        return classpath.toString();
    }
    
    public static void buildClasspath(Set<URL> files, StringBuilder classpath) {
        for (URL url: files) {
            if ("file".equals(url.getProtocol())) {
                String path = toFileName(url);
                classpath.append(path);
                classpath.append(File.pathSeparator);
            }
        }
    }
    
    public static String getClasspath(ClassLoader loader) {
        Set<URL> jars = getClassLoaderClasspath(loader);
        return buildClasspath(jars);
    }
    
    public static String toFileName(URL url) {
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
}
