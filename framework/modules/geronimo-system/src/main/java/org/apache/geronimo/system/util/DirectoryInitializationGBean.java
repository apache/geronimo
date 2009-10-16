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
package org.apache.geronimo.system.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class DirectoryInitializationGBean {


    public DirectoryInitializationGBean(String prefix, String path, ServerInfo serverInfo, ClassLoader classLoader) throws IOException {

        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        int prefixLength = prefix.length();
        if (!path.endsWith("/")) {
            path = path + "/";
        }

        URL sourceURL = classLoader.getResource(prefix + path);
        URLConnection conn = sourceURL.openConnection();
        JarURLConnection jarConn = (JarURLConnection) conn;
        JarFile jarFile = jarConn.getJarFile();
        JarEntry sourceEntry = jarConn.getJarEntry();
        byte[] buf = new byte[1024 * 8];
        for (Enumeration entries = jarFile.entries(); entries.hasMoreElements();) {
            JarEntry entry = (JarEntry) entries.nextElement();
            if (entry.getName().startsWith(sourceEntry.getName())) {
                String entryName = entry.getName();
                String entryPath = entryName.substring(prefixLength);
                File targetPath = serverInfo.resolveServer(entryPath);
                if (!targetPath.exists()) {
                    if (entry.isDirectory()) {
                        targetPath.mkdirs();
                    } else {
                        InputStream in = jarFile.getInputStream(entry);
                        try {
                            OutputStream out = new FileOutputStream(targetPath);
                            try {
                                int chunk;
                                while ((chunk = in.read(buf)) > 0) {
                                    out.write(buf, 0, chunk);
                                }
                            } finally {
                                out.close();
                            }
                        } finally {
                            in.close();
                        }
                    }
                }
            }
        }

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(DirectoryInitializationGBean.class, "GBean");
        infoBuilder.addAttribute("prefix", String.class, true);
        infoBuilder.addAttribute("path", String.class, true);
        infoBuilder.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.setConstructor(new String[]{"prefix", "path", "ServerInfo", "classLoader"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}


