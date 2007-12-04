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
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;

public class JAXWSTools {

    private static final Log LOG = LogFactory.getLog(JAXWSTools.class);
        
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
    private final static Artifact GERONIMO_STAX_API_ARTIFACT = new Artifact("org.apache.geronimo.specs","geronimo-stax-api_1.0_spec", (Version)null, "jar");
    private final static String TOOLS = "tools.jar";

    private Artifact saajImpl;
    private boolean overrideContextClassLoader;
    
    public JAXWSTools() {
    }
    
    public void setUseSunSAAJ() {
        this.saajImpl = SUN_SAAJ_IMPL_ARTIFACT;
    }
    
    public void setUseAxis2SAAJ() {
        this.saajImpl = AXIS2_SAAJ_IMPL_ARTIFACT;
    }
    
    public void setOverrideContextClassLoader(boolean overrideContextClassLoader) {
        this.overrideContextClassLoader = overrideContextClassLoader;
    }
    
    public boolean getOverrideContextClassLoader() {
        return this.overrideContextClassLoader;
    }
       
    public static URL[] toURL(File[] jars) throws MalformedURLException {
        URL [] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            urls[i] = jars[i].toURL();
        }
        return urls;
    }
    
    public static String toString(File [] jars) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < jars.length; i++) {
            buf.append(jars[i].getAbsolutePath());
            if (i+1 < jars.length) {
                buf.append(File.pathSeparatorChar);
            }
        }
        return buf.toString();
    }
    
    public File[] getClasspath(Collection<? extends Repository> repositories) throws Exception {
        ArrayList<File> jars = new ArrayList<File>();
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
        jars.add(getLocation(repositories, GERONIMO_STAX_API_ARTIFACT));
        if (this.saajImpl != null) {
            jars.add(getLocation(repositories, this.saajImpl));
        }
        jars.add(getToolsJarLocation());
        
        return jars.toArray(new File[jars.size()]);
    }
       
    private static File getLocation(Collection<? extends Repository> repositories, Artifact artifactQuery) throws Exception {
        File file = null;
        
        for (Repository arepository : repositories) {
            if (arepository instanceof ListableRepository) {
                ListableRepository repository = (ListableRepository) arepository;
                SortedSet artifactSet = repository.list(artifactQuery);
                // if we have exactly one artifact found
                if (artifactSet.size() == 1) {
                    file = repository.getLocation((Artifact) artifactSet.first());
                    return file.getAbsoluteFile();
                } else if (artifactSet.size() > 1) {// if we have more than 1 artifacts found use the latest one.
                    file = repository.getLocation((Artifact) artifactSet.last());
                    return file.getAbsoluteFile();
                }
            }
        }
        
        throw new Exception("Missing artifact in repositories: " + artifactQuery.toString());
    }
    
    private static File getToolsJarLocation() throws Exception {
        //create a new File then check exists()
        String jreHomePath = System.getProperty("java.home");
        String javaHomePath = "";
        int jreHomePathLength = jreHomePath.length();
        if (jreHomePathLength > 0) {
            int i = jreHomePath.substring(0, jreHomePathLength -1).lastIndexOf(java.io.File.separator);
            javaHomePath = jreHomePath.substring(0, i);
        }
        File jdkhomelib = new File(javaHomePath, "lib");
        if (!jdkhomelib.exists()) {
            throw new Exception("Missing " + jdkhomelib.getAbsolutePath() 
                    + ". This is required for wsgen to run. ");
        }
        else {
            File tools = new File(jdkhomelib, TOOLS);
            if (!tools.exists()) {
                throw new Exception("Missing tools.jar in" + jdkhomelib.getAbsolutePath() 
                        + ". This is required for wsgen to run. ");                
            } else {
                return tools.getAbsoluteFile();
            }               
        }
    }
                           
    public boolean invokeWsgen(URL[] jars, OutputStream os, String[] arguments) throws Exception {
        return invoke("wsgen", jars, os, arguments);
    
    }
    public boolean invokeWsimport(URL[] jars, OutputStream os, String[] arguments) throws Exception {
        return invoke("wsimport", jars, os, arguments);
    }
    
    private boolean invoke(String toolName, URL[] jars, OutputStream os, String[] arguments) throws Exception {        
        URLClassLoader loader = new URLClassLoader(jars, ClassLoader.getSystemClassLoader());
        if (this.overrideContextClassLoader) {
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
            try {
                return invoke(toolName, loader, os, arguments);
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }            
        } else {
            return invoke(toolName, loader, os, arguments);
        }
    }
    
    private boolean invoke(String toolName, ClassLoader loader, OutputStream os, String[] arguments) throws Exception {
        LOG.debug("Invoking " + toolName);
        Class clazz = loader.loadClass("com.sun.tools.ws.spi.WSToolsObjectFactory");
        Method method = clazz.getMethod("newInstance");
        Object factory = method.invoke(null);
        Method method2 = clazz.getMethod(toolName, OutputStream.class, String[].class);
        
        Boolean result = (Boolean) method2.invoke(factory, os, arguments);
        
        return result;
    }
    
}
