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
package org.apache.geronimo.console.databasemanager.wizard;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.util.FileUtils;
import org.apache.geronimo.kernel.util.IOUtils;
import org.apache.geronimo.system.plugin.PluginInstallerGBean;

/**
 * A utility that handles listing and downloading available JDBC driver JARs.
 * It can handle straight JARs and also JARs in ZIP files.
 *
 * @version $Rev$ $Date$
 */
public class DriverDownloader {
    private static final Logger log = LoggerFactory.getLogger(DriverDownloader.class);
    Random random;

    public Properties readDriverFile(URL url) {
        try {
            InputStream in = url.openStream();
            Properties props = new Properties();
            props.load(in);
            in.close();
            return props;
        } catch (IOException e) {
            log.error("Unable to download driver properties", e);
            return null;
        }
    }

    public DriverInfo[] loadDriverInfo(URL driverInfoFile) {
        List list = new ArrayList();
        Properties props = readDriverFile(driverInfoFile);
        if(props == null) {
            return new DriverInfo[0];
        }
        Set drivers = new HashSet();
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            if(!key.startsWith("driver.")) {
                continue;
            }
            int pos = key.indexOf('.', 7);
            if(pos > -1) {
                drivers.add(key.substring(7, pos));
            }
        }
        List urls = new ArrayList();
        for (Iterator it = drivers.iterator(); it.hasNext();) {
            String driver = (String) it.next();
            String name = props.getProperty("driver."+driver+".name");
            String repository = props.getProperty("driver."+driver+".repository");
            String unzip = props.getProperty("driver."+driver+".unzip");
            urls.clear();
            int index = 1;
            while(true) {
                String url = props.getProperty("driver."+driver+".url."+index);
                if(url != null) {
                    ++index;
                    try {
                        urls.add(new URL(url));
                    } catch (MalformedURLException e) {
                        log.error("Unable to process URL from driver list", e);
                    }
                } else {
                    break;
                }
            }
            if(name != null && repository != null && urls.size() > 0) {
                DriverInfo info = new DriverInfo(name, repository);
                info.setUnzipPath(unzip);
                info.setUrls((URL[]) urls.toArray(new URL[urls.size()]));
                list.add(info);
            }
        }
        Collections.sort(list);
        return (DriverInfo[]) list.toArray(new DriverInfo[list.size()]);
    }

    /**
     * Downloads a driver and loads it into the local repository.
     */
    public void loadDriver(PluginInstallerGBean installer, DriverInfo driver, FileWriteMonitor monitor) throws IOException {
        int urlIndex = 0;
        if (driver.urls.length > 1) {
            if (random == null) {
                random = new Random();
            }
            urlIndex = random.nextInt(driver.urls.length);
        }
        URL url = driver.urls[urlIndex];
        InputStream in;
        String uri = driver.getRepositoryURI();
        if (driver.unzipPath != null) {
            byte[] buf = new byte[1024];
            int size;
            int total = 0;
            int threshold = 10240;
            URLConnection uc = url.openConnection();
            int filesize = uc.getContentLength();
            InputStream net = uc.getInputStream();
            JarFile jar = null;
            File download = null;
            try {
                download = File.createTempFile("geronimo-driver-download", ".zip");
                OutputStream out = new BufferedOutputStream(new FileOutputStream(download));
                if (monitor != null) {
                    monitor.writeStarted("Download driver archive to " + download, filesize);
                }
                try {
                    while ((size = net.read(buf)) > -1) {
                        out.write(buf, 0, size);
                        if (monitor != null) {
                            total += size;
                            if (total > threshold) {
                                monitor.writeProgress(total);
                                threshold += 10240;
                            }
                        }
                    }
                    out.flush();
                    out.close();
                } finally {
                    if (monitor != null) {
                        monitor.writeComplete(total);
                    }
                }
                jar = new JarFile(download);
                JarEntry entry = jar.getJarEntry(driver.unzipPath);
                if (entry == null) {
                    log.error("Cannot extract driver JAR " + driver.unzipPath + " from download file " + url);
                } else {
                    install(installer, jar.getInputStream(entry), Artifact.create(uri));
                }
            } finally {
                if (jar != null) try {
                    jar.close();
                } catch (IOException e) {
                    log.error("Unable to close JAR file", e);
                }
                if (download != null) {
                    download.delete();
                }
            }
        } else {
            URLConnection con = url.openConnection();
            in = con.getInputStream();
            install(installer, in, Artifact.create(uri));
        }
    }
    
    private void install(PluginInstallerGBean installer, InputStream is, Artifact artifact) throws IOException{
        // convert input stream to file
        File file = File.createTempFile("geronimo-driver-to-install", "");
        file.deleteOnExit();
        OutputStream os = new FileOutputStream(file);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        IOUtils.close(os);
        IOUtils.close(is);
        
        // install
        installer.installLibrary(file, artifact);
    }
    

    public static class DriverInfo implements Comparable, Serializable {
        private final static long serialVersionUID = -1202452382992975449L;
        
        private String name;
        private String repositoryURI;
        private String unzipPath;
        private URL[] urls;

        public DriverInfo(String name, String repositoryURI) {
            this.name = name;
            this.repositoryURI = repositoryURI;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRepositoryURI() {
            return repositoryURI;
        }

        public void setRepositoryURI(String repositoryURI) {
            this.repositoryURI = repositoryURI;
        }

        public String getUnzipPath() {
            return unzipPath;
        }

        public void setUnzipPath(String unzipPath) {
            this.unzipPath = unzipPath;
        }

        public URL[] getUrls() {
            return urls;
        }

        public void setUrls(URL[] urls) {
            this.urls = urls;
        }

        public int compareTo(Object o) {
            return name.compareTo(((DriverInfo)o).name);
        }
    }
}
