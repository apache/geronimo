/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.console.databasemanager.wizard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.repository.WriteableRepository;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;

import java.net.URL;
import java.net.URI;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/**
 * A utility that handles listing and downloading available JDBC driver JARs.
 * It can handle straight JARs and also JARs in ZIP files.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class DriverDownloader {
    private final static Log log = LogFactory.getLog(DriverDownloader.class);
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
            String rename = props.getProperty("driver."+driver+".rename");
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
                info.setRenameTo(rename);
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
    public void loadDriver(WriteableRepository repo, DriverInfo driver, FileWriteMonitor monitor) throws IOException {
        try {
            int urlIndex = 0;
            if(driver.urls.length > 1) {
                if(random == null) {
                    random = new Random();
                }
                urlIndex = random.nextInt(driver.urls.length);
            }
            URL url = driver.urls[urlIndex];
            InputStream in;
            String fileName = driver.getRepositoryPath();
            if(driver.unzipPath != null) {
                byte[] buf = new byte[1024];
                int size;
                int total = 0;
                int threshold = 10240;
                InputStream net = url.openStream();
                JarFile jar = null;
                File download = null;
                try {
                    download = File.createTempFile("geronimo-driver-download", ".zip");
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(download));
                    if(monitor != null) {
                        monitor.writeStarted("Download driver archive to "+download);
                    }
                    try {
                        while((size = net.read(buf)) > -1) {
                            out.write(buf, 0, size);
                            if(monitor != null) {
                                total += size;
                                if(total > threshold) {
                                    monitor.writeProgress(total);
                                    threshold += 10240;
                                }
                            }
                        }
                        out.flush();
                        out.close();
                    } finally {
                        if(monitor != null) {
                            monitor.writeComplete(total);
                        }
                    }
                    jar = new JarFile(download);
                    JarEntry entry = jar.getJarEntry(driver.unzipPath);
                    if(entry == null) {
                        log.error("Cannot extract driver JAR "+driver.unzipPath+" from download file "+url);
                    } else {
                        in = jar.getInputStream(entry);
                        repo.copyToRepository(in, new URI(fileName), monitor);
                    }
                } finally {
                    if(jar != null) try{jar.close();}catch(IOException e) {log.error("Unable to close JAR file", e);}
                    if(download != null) {download.delete();}
                }
            } else {
                in = url.openStream();
                repo.copyToRepository(in, new URI(fileName), monitor);
            }
        } catch (URISyntaxException e) {
            throw new IOException("Unable to save to repository URI: "+e.getMessage());
        }
    }

//    public static void main(String[] args) {
//        Random random = new Random();
//        DriverDownloader test = new DriverDownloader();
//        try {
//            DriverInfo[] all = test.loadDriverInfo(new URL("file:///Users/ammulder/driver-downloads.properties"));
//            org.apache.geronimo.system.serverinfo.ServerInfo info = new org.apache.geronimo.system.serverinfo.BasicServerInfo("/Users/ammulder");
//            org.apache.geronimo.system.repository.FileSystemRepository repo = new org.apache.geronimo.system.repository.FileSystemRepository(new URI("temp/"), info);
//            repo.doStart();
//            test.loadDriver(repo, all[random.nextInt(all.length)], new FileWriteMonitor() {
//                public void writeStarted(String description) {
//                    System.out.println("Writing "+description);
//                }
//
//                public void writeProgress(int bytes) {
//                    System.out.print("\r"+(bytes/1024)+"kB complete");
//                    System.out.flush();
//                }
//
//                public void writeComplete(int bytes) {
//                    System.out.println();
//                    System.out.println("Finished writing: "+bytes+"b");
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static class DriverInfo implements Comparable {
        private String name;
        private String repositoryDir;
        private String unzipPath;
        private String renameTo;
        private URL[] urls;

        public DriverInfo(String name, String repositoryDir) {
            this.name = name;
            this.repositoryDir = repositoryDir;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRepositoryDir() {
            return repositoryDir;
        }

        public void setRepositoryDir(String repositoryDir) {
            this.repositoryDir = repositoryDir;
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

        public String getRenameTo() {
            return renameTo;
        }

        public void setRenameTo(String renameTo) {
            this.renameTo = renameTo;
        }

        public String getRepositoryPath() {
            String fileName;
            if(unzipPath != null) {
                if(renameTo != null) {
                    fileName = renameTo;
                } else {
                    fileName = unzipPath;
                }
            } else {
                fileName = urls[0].toString();
            }
            int pos = fileName.lastIndexOf('/');
            if(pos > -1) {
                fileName = fileName.substring(pos+1);
            }
            return repositoryDir+"/jars/"+fileName;
        }

        public int compareTo(Object o) {
            return name.compareTo(((DriverInfo)o).name);
        }
    }
}
