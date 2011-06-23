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
package org.apache.geronimo.system.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.ManifestException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
public final class ExecutableConfigurationUtil {
    private static final Logger log = LoggerFactory.getLogger(ExecutableConfigurationUtil.class);

    private static final String META_INF = "META-INF";
    private static final String CONFIG_SER = "config.ser";
    private static final String CONFIG_INFO = "config.info";

    private static final String MANIFEST_MF = "MANIFEST.MF";
    private static final String META_INF_MANIFEST = META_INF + "/" + MANIFEST_MF;
    private static final String META_INF_STARTUP_JAR = META_INF + "/startup-jar";
    private static final String META_INF_CONFIG_SER = META_INF + "/" + CONFIG_SER;
    private static final String META_INF_CONFIG_SER_SHA1 = META_INF_CONFIG_SER + ".sha1";
    private static final String META_INF_CONFIG_INFO = META_INF + "/" + CONFIG_INFO;

    private static final Collection EXCLUDED = Arrays.asList(new String[] {META_INF_STARTUP_JAR, META_INF_CONFIG_SER, META_INF_CONFIG_SER_SHA1, META_INF_CONFIG_INFO});

    private ExecutableConfigurationUtil() {
    }

    public static void createExecutableConfiguration(ConfigurationData configurationData, Manifest manifest, File destinationFile) throws IOException {
        log.debug("createExecutableConfiguration: id: {} destination: {}", configurationData.getId(), destinationFile.getAbsolutePath());
        File configurationDir = configurationData.getConfigurationDir();
        
        // ensure parent directories have been created
        File parent = destinationFile.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        
        FileOutputStream fos = null;
        JarOutputStream out = null;
        try {
            byte[] buffer = new byte[4096];

            fos = new FileOutputStream(destinationFile, false);
            
            if (manifest != null) {
                out = new JarOutputStream(fos, manifest); 

                // add the startup file which allows us to locate the startup directory
                out.putNextEntry(new ZipEntry(META_INF_STARTUP_JAR));
                // intentionally empty ZipEntry
                out.closeEntry();
            } else {
                out = new JarOutputStream(fos);
            }

            // write the configurationData
            ExecutableConfigurationUtil.writeConfiguration(configurationData, out);

            URI baseURI = configurationDir.getAbsoluteFile().toURI();
            Collection files = listRecursiveFiles(configurationDir);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File file = (File) iterator.next();
                String relativePath = baseURI.relativize(file.toURI()).getPath();
                if (!EXCLUDED.contains(relativePath)) {
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
            }
        } finally {
            close(out);
            close(fos); // do this in case JarOutputStream contructor threw an exception
        }
    }

    public static void writeConfiguration(ConfigurationData configurationData, JarOutputStream out) throws IOException {
        out.putNextEntry(new ZipEntry(META_INF_MANIFEST));
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
            configurationData.getManifest().write(writer);
            writer.flush();
        } catch (ManifestException e) {
            throw new IOException("Could not write manifest", e);
        } finally {
            out.closeEntry();
        }
        // save the persisted form in the source directory
        out.putNextEntry(new ZipEntry(META_INF_CONFIG_SER));
        ConfigurationStoreUtil.ChecksumOutputStream sumOut = null;
        try {
            sumOut = new ConfigurationStoreUtil.ChecksumOutputStream(out);
            ConfigurationUtil.writeConfigurationData(configurationData, sumOut);
        } finally {
            out.closeEntry();
        }

        // write the checksum file
        out.putNextEntry(new ZipEntry(META_INF_CONFIG_SER_SHA1));
        try {
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(sumOut.getChecksum());
            writer.flush();
        } finally {
            out.closeEntry();
        }

        // write the info file
        out.putNextEntry(new ZipEntry(META_INF_CONFIG_INFO));
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
            ConfigurationUtil.writeConfigInfo(writer, configurationData);
            writer.flush();
        } finally {
            out.closeEntry();
        }
    }

    public static void writeConfiguration(ConfigurationData configurationData, File source) throws IOException {
        log.debug("writeConfiguration: id: {} source: {}", configurationData.getId(), source.getAbsolutePath());
        // save the persisted form in the source directory
        File metaInf = new File(source, META_INF);
        metaInf.mkdirs();

        PrintWriter writer = null;

        writer = new PrintWriter(new FileWriter(new File(metaInf, MANIFEST_MF)));
        try {
            configurationData.getManifest().write(writer);
            writer.flush();
        } catch (ManifestException e) {
            throw new IOException("Could not write manifest", e);
        } finally {
            writer.close();
        }
        
        File configSer = new File(metaInf, CONFIG_SER);

        OutputStream out = new FileOutputStream(configSer);
        try {
            ConfigurationUtil.writeConfigurationData(configurationData, out);
        } finally {
            flush(out);
            close(out);
        }

        // write the check sum file
        ConfigurationStoreUtil.writeChecksumFor(configSer);

        // write the info file
        try {
            writer = new PrintWriter(new File(metaInf, CONFIG_INFO), "UTF-8");
            ConfigurationUtil.writeConfigInfo(writer, configurationData);
        } finally {
            flush(writer);
            close(writer);
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

    private static void flush(OutputStream thing) {
        if (thing != null) {
            try {
                thing.flush();
            } catch (Exception ignored) {
            }
        }
    }

    private static void flush(Writer thing) {
        if (thing != null) {
            try {
                thing.flush();
            } catch (Exception ignored) {
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

    private static void close(Writer thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (Exception ignored) {
            }
        }
    }
}
