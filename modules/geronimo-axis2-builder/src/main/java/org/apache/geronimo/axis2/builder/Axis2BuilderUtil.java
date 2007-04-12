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

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentConfigurationManager;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Repository;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Axis2BuilderUtil {

    //TODO: need to update to released jars when they are avail.
    private final static Artifact AXIS2_JAXWS_API_ARTIFACT = new Artifact("org.apache.axis2","axis2-jaxws-api", "SNAPSHOT", "jar");
    private final static Artifact AXIS2_SAAJ_API_ARTIFACT = new Artifact("org.apache.axis2","axis2-saaj-api", "SNAPSHOT", "jar");
    private final static Artifact AXIS2_SAAJ_ARTIFACT = new Artifact("org.apache.axis2","axis2-saaj", "SNAPSHOT", "jar");
    private final static Artifact JAXB_API_ARTIFACT = new Artifact("javax.xml.bind","jaxb-api", "2.0", "jar");
    private final static Artifact JAXB_IMPL_ARTIFACT = new Artifact("com.sun.xml.bind","jaxb-impl", "2.0.3", "jar");
    private final static Artifact JAXB_XJC_ARTIFACT = new Artifact("com.sun.xml.bind","jaxb-xjc", "2.0.3", "jar");    
    private final static Artifact JAXWS_TOOLS_ARTIFACT = new Artifact("com.sun.xml.ws","jaxws-tools", "2.0", "jar");
    private final static Artifact JAXWS_RT_ARTIFACT = new Artifact("com.sun.xml.ws","jaxws-rt", "2.0", "jar");
    private final static Artifact GERONIMO_ACTIVATION_SPEC_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-activation_1.1_spec", "1.0-SNAPSHOT", "jar");    
    private final static Artifact GERONIMO_ANNOTATION_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-annotation_1.0_spec", "1.0", "jar");     
    private final static Artifact GERONIMO_WS_METADATA_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-ws-metadata_2.0_spec", "1.1-SNAPSHOT", "jar");    
    private final static String TOOLS = "tools.jar";
    
    protected static URL[] getWsgenClasspath(DeploymentContext context) throws DeploymentException, MalformedURLException {
        ArrayList<URL> jars = new ArrayList();
        
        DeploymentConfigurationManager cm = (DeploymentConfigurationManager)context.getConfigurationManager();
        Collection<Repository> repositories = cm.getRepositories();

        //start classpath with path to tools.jar
        jars.add(getLocation(repositories, JAXB_API_ARTIFACT));
        jars.add(getLocation(repositories, JAXB_IMPL_ARTIFACT));
        jars.add(getLocation(repositories, JAXB_XJC_ARTIFACT));
        jars.add(getLocation(repositories, JAXWS_TOOLS_ARTIFACT));
        jars.add(getLocation(repositories, JAXWS_RT_ARTIFACT));
        jars.add(getLocation(repositories, AXIS2_JAXWS_API_ARTIFACT));
        jars.add(getLocation(repositories, AXIS2_SAAJ_API_ARTIFACT));
        jars.add(getLocation(repositories, AXIS2_SAAJ_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_ACTIVATION_SPEC_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_ANNOTATION_ARTIFACT));
        jars.add(getLocation(repositories, GERONIMO_WS_METADATA_ARTIFACT));
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
    
    private static URL getLocation(Collection<Repository> repositories, Artifact artifact) throws DeploymentException, MalformedURLException {
        File file = null;
        
        for (Repository repository : repositories) {
            if (repository.contains(artifact)) {
                file = repository.getLocation(artifact);
                return file.getAbsoluteFile().toURL();
            }
        }
        if (file == null) {
            throw new DeploymentException("Missing artifact in repositories: " + artifact.toString());
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
}
