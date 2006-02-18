/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.system.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;

/**
 * @version $Rev$ $Date$
 */
public final class ExecutableConfigurationUtil {
    private ExecutableConfigurationUtil() {
    }

    public static void createExecutableConfiguration(ConfigurationData configurationData, Manifest manifest, File destinationFile) throws IOException, InvalidConfigException {
        File configurationDir = configurationData.getConfigurationDir();
        JarOutputStream out = null;
        try {
            byte[] buffer = new byte[4096];

            if (manifest != null) {
                out = new JarOutputStream(new FileOutputStream(destinationFile), manifest);

                // add the startup file which allows us to locate the startup directory
                out.putNextEntry(new ZipEntry("META-INF/startup-jar"));
                out.closeEntry();
            } else {
                out = new JarOutputStream(new FileOutputStream(destinationFile));
            }

            // write the configurationData
            ExecutableConfigurationUtil.writeConfiguration(configurationData, out);

            URI baseURI = configurationDir.getAbsoluteFile().toURI();
            Collection files = listRecursiveFiles(configurationDir);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                String relativePath = baseURI.relativize(file.toURI()).getPath();
                InputStream in = new FileInputStream(file);
                try {
                    out.putNextEntry(new ZipEntry(relativePath));
                    try {
                        int count;
                        while ((count = in.read(buffer)) > 0) {
                            out.write(buffer, 0, count);
                        }
                    } finally {
                        out.closeEntry();
                    }
                } finally {
                    close(in);
                }
            }
        } finally {
            close(out);
        }
    }

    public static void writeConfiguration(ConfigurationData configurationData, JarOutputStream out) throws IOException, InvalidConfigException {

        // convert the configuration data to a gbeandata object
        GBeanData configurationGBeanData = ExecutableConfigurationUtil.getConfigurationGBeanData(configurationData);

        // save the persisted form in the source directory
        out.putNextEntry(new ZipEntry("META-INF/config.ser"));
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(out);
            configurationGBeanData.writeExternal(objectOutputStream);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidConfigException("Unable to save configuration state", e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.flush();
                } catch (IOException ignored) {
                }
            }
            out.closeEntry();
        }
    }

    public static void writeConfiguration(ConfigurationData configurationData, File source) throws InvalidConfigException, IOException {
        // convert the configuration data to a gbeandata object
        GBeanData configurationGBeanData = getConfigurationGBeanData(configurationData);

        // save the persisted form in the source directory
        File metaInf = new File(source, "META-INF");
        metaInf.mkdirs();
        File configSer = new File(metaInf, "config.ser");
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(new FileOutputStream(configSer));
            try {
                configurationGBeanData.writeExternal(out);
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new InvalidConfigException("Unable to save configuration state", e);
            }
        } finally {
            if (out != null) {
                try {
                    out.flush();
                } catch (Exception ignored) {
                }
                try {
                    out.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static GBeanData getConfigurationGBeanData(ConfigurationData configurationData) throws InvalidConfigException {
        try {
            Artifact id = configurationData.getId();
            GBeanData config = new GBeanData(Configuration.getConfigurationObjectName(id), Configuration.GBEAN_INFO);
//            config.setAttribute("id", id);
            config.setAttribute("type", configurationData.getModuleType());
            //TODO configid this might need further improvmement
//            Map nameKeys = configurationData.getNameKeys();
//            config.setAttribute("nameKeys", nameKeys);

            Environment environment = configurationData.getEnvironment();
            config.setAttribute("environment", environment);
            config.setAttribute("gBeanState", Configuration.storeGBeans(configurationData.getGBeans()));
            config.setReferencePatterns("Repositories", Collections.singleton(new ObjectName("*:name=Repository,*")));
//            config.setAttribute("dependencies", configurationData.getDependencies());
            config.setAttribute("classPath", configurationData.getClassPath());
//            config.setAttribute("inverseClassLoading", Boolean.valueOf(configurationData.isInverseClassloading()));
//            Set set = configurationData.getHiddenClasses();
//            config.setAttribute("hiddenClasses", set.toArray(new String[set.size()]));
//            set = configurationData.getNonOverridableClasses();
//            config.setAttribute("nonOverridableClasses", set.toArray(new String[set.size()]));

            return config;
        } catch (MalformedObjectNameException e) {
            throw new InvalidConfigException(e);
        }
    }

    private static Collection listRecursiveFiles(File file) {
        LinkedList list = new LinkedList();
        listRecursiveFiles(file, list);
        return Collections.unmodifiableCollection(list);
    }

    private static void listRecursiveFiles(File file, Collection collection) {
        File[] files = file.listFiles();
        if (null == files) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                listRecursiveFiles(files[i], collection);
            } else {
                collection.add(files[i]);
            }
        }
    }

    private static void close(InputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static void close(OutputStream thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }
}
