/**
 *
 * Copyright 2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.main;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Collections;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.io.IOException;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

/**
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2004/09/02 11:03:18 $
 */
public class CommandLineManifest {
    public static final Attributes.Name MAIN_GBEAN = new Attributes.Name("Main-GBean");
    public static final Attributes.Name MAIN_METHOD = new Attributes.Name("Main-Method");
    public static final Attributes.Name CONFIGURATIONS = new Attributes.Name("Configurations");

    public static CommandLineManifest getManifestEntries() {
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
            System.err.println("Manifest does not contain a Main-GBean entry");
            System.exit(1);
            throw new AssertionError();
        }

        ObjectName mainGBean;
        try {
            mainGBean = new ObjectName(mainGBeanString);
        } catch (MalformedObjectNameException e) {
            System.err.println("Invalid Main-GBean name: " + mainGBeanString);
            System.exit(1);
            throw new AssertionError();
        }

        // get the main method
        String mainMethod = mainAttributes.getValue(MAIN_METHOD);
        if (mainGBeanString == null) {
            System.err.println("Manifest does not contain a Main-Method entry");
            System.exit(1);
            throw new AssertionError();
        }

        // get the list of extra configurations to load
        List configurations = new ArrayList();
        String configurationsString = mainAttributes.getValue(CONFIGURATIONS);
        if (configurationsString != null) {
            for (StringTokenizer tokenizer = new StringTokenizer(configurationsString, " "); tokenizer.hasMoreTokens();) {
                String configuration = tokenizer.nextToken();
                try {
                    configurations.add(new URI(configuration));
                } catch (URISyntaxException e) {
                    System.err.println("Invalid URI in Manifest Configurations entry: " + configuration);
                    System.exit(1);
                    throw new AssertionError();
                }
            }
        }
        CommandLineManifest commandLineManifest = new CommandLineManifest(mainGBean, mainMethod, configurations);
        return commandLineManifest;
    }

    private final ObjectName mainGBean;
    private final String mainMethod;
    private final List configurations;

    public CommandLineManifest(ObjectName mainGBean, String mainMethod, List configurations) {
        this.mainGBean = mainGBean;
        this.mainMethod = mainMethod;
        this.configurations = Collections.unmodifiableList(configurations);
    }

    public ObjectName getMainGBean() {
        return mainGBean;
    }

    public String getMainMethod() {
        return mainMethod;
    }

    public List getConfigurations() {
        return configurations;
    }
}
