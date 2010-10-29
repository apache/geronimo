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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.crypto.EncryptionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of PluginRepositoryList that downloads plugins from
 * an Apache web site.
 *
 * @version $Rev$ $Date$
 */
@GBean
public class PluginRepositoryDownloader implements PluginRepositoryList {
    private static final Logger log = LoggerFactory.getLogger(PluginRepositoryDownloader.class);
    private List<String> downloadRepositories = new ArrayList<String>();
    private Map<String, String[]> userRepositories = new HashMap<String, String[]>();
    private final File userRepositoryList;
    private final boolean restrictToKnownRepositories;
    private final Kernel kernel;
    private final AbstractName name;
    private final URL repositoryList;
    private static final String COMMENT = "List of known plugin repositories. Fomat:  <url>=<username>=<password>";

    public PluginRepositoryDownloader(@ParamAttribute(name = "downloadRepositories")List<String> downloadRepositories,
                                      @ParamAttribute(name = "userRepositoryList")String userRepositoryList,
                                      @ParamAttribute(name = "repositoryList")URL repositoryList,
                                      @ParamAttribute(name = "restrictToKnownRepositories")boolean restrictToKnownRepositories,
                                      @ParamReference(name = "ServerInfo")ServerInfo serverInfo,
                                      @ParamSpecial(type = SpecialAttributeType.kernel)Kernel kernel,
                                      @ParamSpecial(type = SpecialAttributeType.abstractName)AbstractName name) {
        if (downloadRepositories != null) this.downloadRepositories = downloadRepositories;
        this.repositoryList = repositoryList;
        this.userRepositoryList = serverInfo.resolveServer(userRepositoryList);
        this.restrictToKnownRepositories = restrictToKnownRepositories;
        this.kernel = kernel;
        this.name = name;
        loadUserRepositories();
    }

    public PluginRepositoryDownloader(Map<String, String[]> userRepositories, boolean restrictToKnownRepositories) {
        this.userRepositories.putAll(userRepositories);
        this.restrictToKnownRepositories = restrictToKnownRepositories;
        userRepositoryList = null;
        kernel = null;
        name = null;
        repositoryList = null;
    }

    /**
     * The list of repositories that were downloaded from central.
     */
    public void setDownloadRepositories(List<String> downloadRepositories) {
        this.downloadRepositories = downloadRepositories;
        if (this.downloadRepositories == null) this.downloadRepositories = new ArrayList<String>();
    }

    private synchronized void loadUserRepositories() {
        userRepositories.clear();
        Properties properties = new Properties();
        boolean modified = false;
        try {
            InputStream in = new FileInputStream(userRepositoryList);
            try {
                properties.load(in);
                for (Map.Entry entry : properties.entrySet()) {
                    String url = (String) entry.getKey();
                    if (!url.endsWith("/")) {
                        url = url + "/";
                        modified = true;
                    }
                    String rawCreds = (String) entry.getValue();
                    String[] creds = null;
                    if (rawCreds.length() > 0) {
                        creds = rawCreds.split("=");
                        if (creds.length != 2) {
                            continue;
                        }
                        String password = creds[1];
                        creds[1] = (String) EncryptionManager.decrypt(password);
                        if (password.equals(creds[1])) {
                            //unencrypted password found
                            modified = true;
                        }
                    }
                    userRepositories.put(url, creds);
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            //not much to do
        }
        if (modified) {
            saveUserRepositories();
        }
    }

    private synchronized void saveUserRepositories() {
        Properties properties = new Properties();
        for (Map.Entry<String, String[]> entry : userRepositories.entrySet()) {
            String url = entry.getKey();
            String[] creds = entry.getValue();
            if (creds == null || creds.length != 2) {
                properties.setProperty(url, "");
            } else {
                properties.setProperty(url, creds[0] + "=" + EncryptionManager.encrypt(creds[1]));
            }
        }
        try {
            OutputStream out = new FileOutputStream(userRepositoryList);
            try {
                properties.store(out, COMMENT);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            //not much to do
        }
    }


    /**
     * Gets the union of centrally-listed repositories and user-added repositories.
     */
    public List<URL> getRepositories() {
        List<URL> list = new ArrayList<URL>();
        if (!restrictToKnownRepositories) {
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
        }
        for (String userRepository : userRepositories.keySet()) {
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

    public SourceRepository getSourceRepository(String repo) {
        if (repo == null) {
            throw new IllegalArgumentException("No repo supplied");
        }
        URI repoURI = resolveRepository(repo);
        if (repoURI == null) {
            throw new IllegalStateException("Can't locate repo " + repo);
        }
        if (!repo.endsWith("/")) {
            repo = repo + "/";
        }
        String scheme = repoURI.getScheme();
        if (scheme.startsWith("http")) {
            String[] creds = userRepositories.get(repo);
            if (creds == null) {
                return new RemoteSourceRepository(repoURI, null, null);
            } else {
                return new RemoteSourceRepository(repoURI, creds[0], creds[1]);
            }
        } else if ("file".equals(scheme)) {
            return new LocalSourceRepository(new File(repoURI));
        }
        throw new IllegalStateException("Cannot identify desired repo type for " + repo);
    }

    private URI resolveRepository(String userRepository) {
        if (!userRepository.endsWith("/")) {
            userRepository = userRepository + "/";
        }
        if (restrictToKnownRepositories && !userRepositories.containsKey(userRepository)) {
            return null;
        }
        try {
            userRepository = userRepository.replaceAll(" ", "%20");
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
    public synchronized void addUserRepository(URL repo, String userName, String password) {
        userRepositories.put(repo.toString(), userName == null ? null : new String[]{userName, password});
        saveUserRepositories();
    }

}
