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
package org.apache.geronimo.system.main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.log.GeronimoLogging;


/**
 * @version $Revision: 1.1 $ $Date: 2004/02/24 06:05:37 $
 */
public class CommandLine {
    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.INFO);
    }

    private CommandLine() {
    }

    public static final Attributes.Name MAIN_GBEAN = new Attributes.Name("Main-GBean");
    public static final Attributes.Name MAIN_METHOD = new Attributes.Name("Main-Method");
    public static final Attributes.Name CONFIGURATIONS = new Attributes.Name("Configurations");

    /**
     * Command line entry point called by executable jar
     * @param args command line args
     */
    public static void main(String[] args) {
        try {
            // the interesting entries from the manifest
            ManifestEntries manifestEntries = getManifestEntries();

            // boot the kernel
            Kernel kernel = new Kernel("geronimo.kernel", "geronimo");
            kernel.boot();

            // load and start the configuration in this jar
            ConfigurationManager configurationManager = kernel.getConfigurationManager();
            GBeanMBean config = new GBeanMBean(Configuration.GBEAN_INFO);
            ClassLoader classLoader = CommandLine.class.getClassLoader();
            ObjectInputStream ois = new ObjectInputStream(classLoader.getResourceAsStream("META-INF/config.ser"));
            try {
                Configuration.loadGMBeanState(config, ois);
            } finally {
                ois.close();
            }
            ObjectName configName = configurationManager.load(config, classLoader.getResource("/"));
            kernel.startRecursiveGBean(configName);

            // load and start the configurations listested in the manifest
            for (Iterator iterator = manifestEntries.configurations.iterator(); iterator.hasNext();) {
                URI configurationID = (URI) iterator.next();
                ObjectName configurationName = configurationManager.load(configurationID);
                kernel.startRecursiveGBean(configurationName);
            }

            // invoke the main method
            kernel.invoke(
                    manifestEntries.mainGBean,
                    manifestEntries.mainMethod,
                    new Object[]{args},
                    new String[]{String[].class.getName()});

            // stop this configuration
            kernel.stopGBean(configName);

            // shutdown the kernel
            kernel.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
            throw new AssertionError();
        }
    }

    private static ManifestEntries getManifestEntries() {
        ManifestEntries manifestEntries = new ManifestEntries();

        // find the startup jar
        ClassLoader classLoader = CommandLine.class.getClassLoader();
        URL url = classLoader.getResource("META-INF/startup-jar");
        if (url == null) {
            throw new IllegalArgumentException("Unable to determine location of startup jar");
        }

        // extract the manifest
        Manifest manifest;
        try {
            JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
            manifest = jarConnection.getManifest();
        } catch (IOException e) {
            System.err.println("Startup jar does not contain a manifest: " + url);
            System.exit(1);
            throw new AssertionError();
        }
        Attributes mainAttributes = manifest.getMainAttributes();

        // get the main gbean class
        String mainGBeanString = mainAttributes.getValue(MAIN_GBEAN);
        if (mainGBeanString == null) {
            System.err.println("Manifest does not conatin a Main-GBean entry");
            System.exit(1);
            throw new AssertionError();
        }
        try {
            manifestEntries.mainGBean = new ObjectName(mainGBeanString);
        } catch (MalformedObjectNameException e) {
            System.err.println("Invalid Main-GBean name: " + mainGBeanString);
            System.exit(1);
            throw new AssertionError();
        }

        // get the main method
        manifestEntries.mainMethod = mainAttributes.getValue(MAIN_METHOD);
        if (mainGBeanString == null) {
            System.err.println("Manifest does not conatin a Main-Method entry");
            System.exit(1);
            throw new AssertionError();
        }

        // get the list of extra configurations to load
        String configurationsString = mainAttributes.getValue(CONFIGURATIONS);
        if (configurationsString != null) {
            for (StringTokenizer tokenizer = new StringTokenizer(configurationsString, " "); tokenizer.hasMoreTokens();) {
                String configuration = tokenizer.nextToken();
                try {
                    manifestEntries.configurations.add(new URI(configuration));
                } catch (URISyntaxException e) {
                    System.err.println("Invalid URI in Manifest Configurations entry: " + configuration);
                    System.exit(1);
                    throw new AssertionError();
                }
            }
        }
        return manifestEntries;
    }

    private static class ManifestEntries {
        private ObjectName mainGBean;
        private String mainMethod;
        private final List configurations = new ArrayList();
    }
}
