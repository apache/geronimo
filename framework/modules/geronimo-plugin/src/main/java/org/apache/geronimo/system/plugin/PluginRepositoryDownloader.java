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
package org.apache.geronimo.system.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;

/**
 * An implementation of PluginRepositoryList that downloads plugins from
 * an Apache web site.
 *
 * @version $Rev$ $Date$
 */
public class PluginRepositoryDownloader implements PluginRepositoryList {
    private static final Logger log = LoggerFactory.getLogger(PluginRepositoryDownloader.class);
    private List<String> downloadRepositories = new ArrayList<String>();
    private List<String> userRepositories = new ArrayList<String>();
    private Kernel kernel;
    private AbstractName name;
    private URL repositoryList;

    public PluginRepositoryDownloader(List<String> downloadRepositories, List<String> userRepositories, URL repositoryList, Kernel kernel, AbstractName name) {
        if (downloadRepositories != null) this.downloadRepositories = downloadRepositories;
        if (userRepositories != null) this.userRepositories = userRepositories;
        this.repositoryList = repositoryList;
        this.kernel = kernel;
        this.name = name;
    }

    /**
     * The list of repositories that were downloaded from central.
     */
    public void setDownloadRepositories(List<String> downloadRepositories) {
        this.downloadRepositories = downloadRepositories;
        if (this.downloadRepositories == null) this.downloadRepositories = new ArrayList<String>();
    }

    /**
     * Any repositories that the user added manually
     */
    public void setUserRepositories(List<String> userRepositories) {
        this.userRepositories = userRepositories;
        if (this.userRepositories == null) this.userRepositories = new ArrayList<String>();
    }

    /**
     * Gets the union of centrally-listed repositories and user-added repositories.
     */
    public List<URL> getRepositories() {
        List<URL> list = new ArrayList<URL>();
        for (String url : downloadRepositories) {
            url = url.trim();
            if (!url.endsWith("/")) {
                url = url + "/";
            }
            try {
                list.add(new URL(url));
            } catch (MalformedURLException e) {
                log.error("Unable to format plugin repository URL " + url, e);
            }
        }
        for (String userRepository : userRepositories) {
            userRepository = userRepository.trim();
            URL url = null;
            try {
                url = resolveRepository(userRepository).toURL();
            } catch (MalformedURLException e) {
                log.error("Unable to format plugin repository URL " + userRepository, e);
            }
            if (url != null) {
                list.add(url);
            }
        }
        return list;
    }

    static URI resolveRepository(String userRepository) {
        if (!userRepository.endsWith("/")) {
           userRepository = userRepository + "/";
        }
        try {
            URI uri = new URI(userRepository);
            if (!uri.isAbsolute()) {
                if (userRepository.startsWith("/")) {
                    return new URI("file", userRepository, null);
                } else if (userRepository.startsWith("~")) {
                    return new File(System.getProperty("user.home")).getAbsoluteFile().toURI().resolve(userRepository.substring(2));
                } else {
                    log.error("Can't interpret path: " + userRepository);
                }
            } else {
                return uri;
            }
        } catch (URISyntaxException e) {
            log.error("Unable to format plugin repository URL " + userRepository, e);
        }
        return null;
    }

    /**
     * Go download a fresh copy of the repository list.
     */
    public void refresh() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(repositoryList.openStream()));
            String line;
            List<String> list = new ArrayList<String>();
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (!line.equals("") && !line.startsWith("#")) {
                    list.add(line);
                }
            }
            in.close();
            in = null;
            //this saves the new value in config.xml
            kernel.setAttribute(name, "downloadRepositories", list);
        } catch (Exception e) {
            log.error("Unable to save download repositories", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Adds a new repository that the user put in manually.
     */
    public void addUserRepository(URL repo) {
        userRepositories.add(repo.toString());
        try {
            kernel.setAttribute(name, "userRepositories", userRepositories);
        } catch (Exception e) {
            log.error("Unable to save user repositories", e);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(PluginRepositoryDownloader.class);

        infoFactory.addAttribute("downloadRepositories", List.class, true);
        infoFactory.addAttribute("userRepositories", List.class, true);
        infoFactory.addAttribute("repositoryList", URL.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("abstractName", AbstractName.class, false);
        infoFactory.addInterface(PluginRepositoryList.class);
        infoFactory.setConstructor(new String[]{"downloadRepositories", "userRepositories", "repositoryList", "kernel", "abstractName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
