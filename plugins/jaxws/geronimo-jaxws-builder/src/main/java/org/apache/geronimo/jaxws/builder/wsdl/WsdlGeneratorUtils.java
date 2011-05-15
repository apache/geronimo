/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.jaxws.builder.wsdl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WsdlGeneratorUtils {

    private static final Logger LOG = LoggerFactory.getLogger(WsdlGeneratorUtils.class);

    public static void getModuleClasspath(Module module, DeploymentContext context, StringBuilder classPathBuilder) throws Exception {
        LinkedHashSet<URL> jars = new LinkedHashSet<URL>();
        getModuleClasspath(module, context, jars);
        buildClasspath(jars, classPathBuilder);
    }

    public static void getModuleClasspath(Module module, DeploymentContext context, LinkedHashSet<URL> classpath) throws Exception {
        getModuleClasspath(module.getEarContext(), classpath);
        if (module.getRootEarContext() != module.getEarContext()) {
            getModuleClasspath(module.getRootEarContext(), classpath);
        }
    }

    public static void getModuleClasspath(DeploymentContext deploymentContext, LinkedHashSet<URL> classpath) throws Exception {
        File configurationBaseDir = deploymentContext.getBaseDir();
        if (deploymentContext.getBundleClassPath() == null || deploymentContext.getBundleClassPath().isEmpty()){
            // the default bundle class path is the root of the bundle if no bundle-classpath specified.
            classpath.add(configurationBaseDir.toURI().toURL());
        } else {
            for (String bundleClassPath : deploymentContext.getBundleClassPath()) {
                classpath.add(new File(configurationBaseDir, bundleClassPath).toURI().toURL());
            }
        }
    }

    public static void getModuleClasspath(Configuration configuration, LinkedHashSet<URL> classpath) throws Exception {
//        ConfigurationResolver resolver = configuration.getConfigurationResolver();
//        List<String> moduleClassPath = configuration.getClassPath();
//        for (String pattern : moduleClassPath) {
//            try {
//                Set<URL> files = resolver.resolve(pattern);
//                classpath.addAll(files);
//            } catch (MalformedURLException e) {
//                throw new Exception("Could not resolve pattern: " + pattern, e);
//            } catch (NoSuchConfigException e) {
//                throw new Exception("Could not resolve pattern: " + pattern, e);
//            }
//        }
    }

    public static Set<URL> getClassLoaderClasspath(ClassLoader loader) {
        LinkedHashSet<URL> jars = new LinkedHashSet<URL>();
        getClassLoaderClasspath(loader, jars);
        return jars;
    }

    public static void getClassLoaderClasspath(ClassLoader loader, LinkedHashSet<URL> classpath) {
        if (loader == null || loader == ClassLoader.getSystemClassLoader()) {
            return;
//        } else if (loader instanceof MultiParentClassLoader) {
//            MultiParentClassLoader cl = (MultiParentClassLoader)loader;
//            for (ClassLoader parent : cl.getParents()) {
//                getClassLoaderClasspath(parent, classpath);
//            }
//            for (URL u : cl.getURLs()) {
//                classpath.add(u);
//            }
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

    public static File toFile(URL url) {
        if (url == null || !url.getProtocol().equals("file")) {
            return null;
        } else {
            String filename = toFileName(url);
            return new File(filename);
        }
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

    public static File createTempDirectory(File baseDir) throws IOException {
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

    private static File[] getWsdlFiles(File baseDir) {
        File[] files = baseDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".wsdl"));
            }
        });
        return files;
    }

    public static File findWsdlFile(File baseDir, String serviceName) {
        File[] files = getWsdlFiles(baseDir);
        if (files == null || files.length == 0) {
            // no wsdl files found
            return null;
        } else {
            if (files.length == 1) {
                // found one wsdl file, must be it
                return files[0];
            } else if (serviceName != null) {
                // found multiple wsdl files, check filenames to match serviceName
                String wsdlFileName = serviceName + ".wsdl";
                for (File file : files) {
                    if (wsdlFileName.equalsIgnoreCase(file.getName())) {
                        return file;
                    }
                }
                return null;
            } else {
                // found multiple wsdl files and serviceName is not specified
                // so we don't know which wsdl file is the right one
                return null;
            }
        }
    }

    public static String getRelativeNameOrURL(File baseDir, File file) {
        String basePath = baseDir.getAbsolutePath();
        String path = file.getAbsolutePath();

        if (path.startsWith(basePath)) {
            return baseDir.toURI().relativize(file.toURI()).toString();
        } else {
            return file.toURI().toString();
        }
    }

    public static boolean execJava(List<String> arguments, long timeout) throws Exception {
        List<String> cmd = new ArrayList<String>();
        String javaHome = System.getProperty("java.home");
        String java = javaHome + File.separator + "bin" + File.separator + "java";
        cmd.add(java);
        cmd.addAll(arguments);
        return exec(cmd, timeout);
    }

    public static boolean exec(List<String> cmd, long timeout) throws Exception {
        LOG.debug("Executing process: {}", cmd);

        ProcessBuilder builder = new ProcessBuilder(cmd);
        builder.redirectErrorStream(true);

        Process process = builder.start();
        return waitFor(process, timeout);
    }

    private static boolean waitFor(Process process, long timeout) throws Exception {
        CaptureOutputThread outputThread = new CaptureOutputThread(process.getInputStream());
        outputThread.start();

        long sleepTime = 0;
        while(sleepTime < timeout) {
            try {
                int errorCode = process.exitValue();
                if (errorCode == 0) {
                    System.err.println("Process output: {}" +outputThread.getOutput() );
                    return true;
                } else {
                    System.err.println("Process output: {}" +outputThread.getOutput() );
                    return false;
                }
            } catch (IllegalThreadStateException e) {
                // still running
                try {
                    Thread.sleep(WsdlGeneratorOptions.FORK_POLL_FREQUENCY);
                } catch (InterruptedException ee) {
                    // interrupted
                    process.destroy();
                    throw new Exception("Process was interrupted");
                }
                sleepTime += WsdlGeneratorOptions.FORK_POLL_FREQUENCY;
            }
        }

        // timeout;
        process.destroy();

        LOG.error("Process timed out: {}", outputThread.getOutput());

        throw new Exception("Process timed out");
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
