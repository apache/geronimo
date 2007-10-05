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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Maven2Repository;

public class JAXWSToolsCLI {

    private static final String USAGE_MSG =
        "Usage: jaxws-tools <toolName> <tool options>\n\n" +
        "where <toolName> is:\n" +
        "  wsgen       - generate portable artifacts from class\n" +
        "  wsimport    - generate portable artifacts from WSDL\n";
           
    public static void main(String[] args) throws Throwable {
        if (args.length == 0) {
            System.err.println(USAGE_MSG);
            System.exit(1);
        }

        String geroninoHome = getGeronimoHome();
        String repository = System.getProperty("Xorg.apache.geronimo.repository.boot.path", "repository");
        Maven2Repository mavenRepository = new Maven2Repository((new File(geroninoHome, repository)).getCanonicalFile());
        ArrayList<ListableRepository> repositories = new ArrayList<ListableRepository>(1);
        repositories.add(mavenRepository);

        JAXWSTools tools = new JAXWSTools();
        tools.setUseSunSAAJ();
        tools.setOverrideContextClassLoader(true);
        
        File [] jars = tools.getClasspath(repositories);
                       
        System.setProperty("java.class.path", JAXWSTools.toString(jars));
        
        URL[] jarUrls = JAXWSTools.toURL(jars);

        boolean rs = false;
        
        try {
            if (args[0].equalsIgnoreCase("wsgen")) {
                rs = tools.invokeWsgen(jarUrls, System.out, getCmdArguments(args));
            } else if (args[0].equalsIgnoreCase("wsimport")) {
                rs = tools.invokeWsimport(jarUrls, System.out, getCmdArguments(args));
            } else {
                System.err.println("Error: Unsupported toolName [" + args[0] + "].");
                System.err.println();
                System.err.println(USAGE_MSG);
                System.exit(1);
            }
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
        
        System.exit( (rs) ? 0 : 1 );
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

}
