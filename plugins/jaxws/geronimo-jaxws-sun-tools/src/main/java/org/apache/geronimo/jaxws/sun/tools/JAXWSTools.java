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

package org.apache.geronimo.jaxws.sun.tools;

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.SortedSet;

import org.apache.geronimo.kernel.config.Os;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.Version;
import org.apache.xbean.classloader.JarFileClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JAXWSTools {

    private static final Logger LOG = LoggerFactory.getLogger(JAXWSTools.class);

    private final static String [][] LIBS =
    {
        { "org.apache.geronimo.specs", "geronimo-jaxws_2.2_spec" },
        { "org.apache.geronimo.specs", "geronimo-saaj_1.3_spec" },
        { "org.apache.geronimo.specs", "geronimo-jaxb_2.2_spec" },
        { "org.apache.geronimo.bundles", "jaxb-impl" },
        { "com.sun.xml.bind", "jaxb-xjc" },
        { "com.sun.xml.ws",   "jaxws-tools" },
        { "com.sun.xml.ws",   "jaxws-rt" },
        { "com.sun.xml.ws",   "policy" },
        { "com.sun.xml.stream.buffer",    "streambuffer" },
        { "org.jvnet.staxex",             "stax-ex" },
        { "org.apache.geronimo.javamail", "geronimo-javamail_1.4_mail"},
        { "org.apache.geronimo.specs",    "geronimo-activation_1.1_spec"},
        { "org.apache.geronimo.specs",    "geronimo-annotation_1.1_spec"},
        { "org.apache.geronimo.specs",    "geronimo-atinject_1.0_spec"},
        { "org.apache.geronimo.specs",    "geronimo-ws-metadata_2.0_spec"},
        { "org.apache.geronimo.specs",    "geronimo-ejb_3.1_spec"},
        { "org.apache.geronimo.specs",    "geronimo-interceptor_1.1_spec"},
        { "org.apache.geronimo.specs",    "geronimo-stax-api_1.2_spec"},
        { "org.apache.geronimo.specs",    "geronimo-jpa_2.0_spec"},
        { "org.apache.geronimo.specs",    "geronimo-j2ee-connector_1.6_spec"},
        { "org.apache.geronimo.specs",    "geronimo-jms_1.1_spec"},
        { "org.apache.geronimo.specs",    "geronimo-jta_1.1_spec"},
        { "org.apache.geronimo.specs",    "geronimo-j2ee-management_1.1_spec"},
        { "org.apache.geronimo.bundles",  "woodstox-core-asl" },
        { "org.codehaus.woodstox",        "stax2-api" },
        { "org.apache.geronimo.modules",  "geronimo-webservices" },
        { "org.ops4j.pax.logging", "pax-logging-api" },
        { "commons-lang", "commons-lang" },
        { "org.apache.openjpa", "openjpa"}
    };

    private final static String[] HIDDEN_CLASSES =
    {
        "javax.xml.bind",
        "javax.xml.ws"
    };

    private final static String[][] ENDORSED_ARTIFACTS = {
        { "org.apache.geronimo.specs", "geronimo-jaxws_2.2_spec" },
        { "org.apache.geronimo.specs", "geronimo-jaxb_2.2_spec" }
    };

    private final static Artifact SUN_SAAJ_IMPL_ARTIFACT = new Artifact("org.apache.geronimo.bundles","saaj-impl", (Version)null, "jar");
    private final static Artifact AXIS2_SAAJ_IMPL_ARTIFACT = new Artifact("org.apache.geronimo.bundles","axis2-saaj", (Version)null, "jar");
    private final static String TOOLS = "tools.jar";

    private Artifact saajImpl;
    private boolean overrideContextClassLoader;
    private ClassLoader parentClassLoader;

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

    public void setParentClassLoader(ClassLoader parentClassLoader) {
        this.parentClassLoader = parentClassLoader;
    }

    public ClassLoader getParentClassLoader() {
        return this.parentClassLoader;
    }

    public static URL[] toURL(File[] jars) throws MalformedURLException {
        URL [] urls = new URL[jars.length];
        for (int i = 0; i < jars.length; i++) {
            urls[i] = jars[i].toURI().toURL();
        }
        return urls;
    }

    public static String toString(File [] jars) {
        StringBuilder buf = new StringBuilder();
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
        for (String[] lib : LIBS) {
            Artifact artifact = new Artifact(lib[0], lib[1], (Version)null, "jar");
            jars.add(getLocation(repositories, artifact));
        }
        if (this.saajImpl != null) {
            jars.add(getLocation(repositories, this.saajImpl));
        }
        // add tools.jar to classpath except on Mac OS. On Mac OS there is classes.jar with the
        // same contents as tools.jar and is automatically included in the classpath.
        if (!Os.isFamily(Os.FAMILY_MAC)) {
            addToolsJarLocation(jars);
        }

        return jars.toArray(new File[jars.size()]);
    }

    private static File getLocation(Collection<? extends Repository> repositories, Artifact artifactQuery) throws Exception {
        File file = null;

        for (Repository arepository : repositories) {
            if (arepository instanceof ListableRepository) {
                ListableRepository repository = (ListableRepository) arepository;
                SortedSet<Artifact> artifactSet = repository.list(artifactQuery);
                // if we have exactly one artifact found
                if (artifactSet.size() == 1) {
                    file = repository.getLocation(artifactSet.first());
                    return file.getAbsoluteFile();
                } else if (artifactSet.size() > 1) {// if we have more than 1 artifacts found use the latest one.
                    file = repository.getLocation(artifactSet.last());
                    return file.getAbsoluteFile();
                }
            }
        }

        throw new Exception("Missing artifact in repositories: " + artifactQuery.toString());
    }

    private static void addToolsJarLocation(ArrayList<File> jars) {
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
            LOG.warn("Missing " + jdkhomelib.getAbsolutePath()
                    + ". This may be required for wsgen to run. ");
        }
        else {
            File tools = new File(jdkhomelib, TOOLS);
            if (!tools.exists()) {
                LOG.warn("Missing tools.jar in" + jdkhomelib.getAbsolutePath()
                        + ". This may be required for wsgen to run. ");
            } else {
                jars.add(tools.getAbsoluteFile());
            }
        }
    }

    public String getEndorsedPath(Collection<? extends Repository> repositories) throws Exception {
        StringBuilder endorsedDirectories = new StringBuilder();
        for (String[] lib : ENDORSED_ARTIFACTS) {
            Artifact artifact = new Artifact(lib[0], lib[1], (Version) null, "jar");
            if (endorsedDirectories.length() > 0) {
                endorsedDirectories.append(File.pathSeparator);
            }
            endorsedDirectories.append(getLocation(repositories, artifact).getParent());
        }
        String defaultEndorsedDirectory = System.getProperty("java.home") + File.separator + "lib" + File.separator + "endorsed";
        endorsedDirectories.append(File.pathSeparator).append(defaultEndorsedDirectory);
        return endorsedDirectories.toString();
    }

    public boolean invokeWsgen(URL[] jars, OutputStream os, String[] arguments) throws Exception {
        return invoke("wsgen", jars, os, arguments);

    }

    public boolean invokeWsimport(URL[] jars, OutputStream os, String[] arguments) throws Exception {
        return invoke("wsimport", jars, os, arguments);
    }

    private boolean invoke(String toolName, URL[] jars, OutputStream os, String[] arguments) throws Exception {
        ClassLoader oldClassLoader = null;
        JarFileClassLoader loader = new JarFileClassLoader(null, jars, ClassLoader.getSystemClassLoader(), false, HIDDEN_CLASSES, new String[0]);
        if (overrideContextClassLoader) {
            oldClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(loader);
        }
        try {
            return invoke(toolName, loader, os, arguments);
        } finally {
            if (overrideContextClassLoader) {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
            try {
                loader.destroy();
            } catch (Exception e) {
            }
        }
    }

    private boolean invoke(String toolName, ClassLoader loader, OutputStream os, String[] arguments) throws Exception {
        LOG.debug("Invoking " + toolName);
        Class<?> clazz = loader.loadClass("com.sun.tools.ws.spi.WSToolsObjectFactory");
        Method method = clazz.getMethod("newInstance");
        Object factory = method.invoke(null);
        Method method2 = clazz.getMethod(toolName, OutputStream.class, String[].class);

        Boolean result = (Boolean) method2.invoke(factory, os, arguments);

        return result;
    }

}
