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
package org.apache.geronimo.system.plugin;

import java.net.URL;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * An implementation of PluginRepositoryList that downloads plugins from
 * an Apache web site.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class PluginRepositoryDownloader implements PluginRepositoryList {
    private final static Log log = LogFactory.getLog(PluginRepositoryDownloader.class);
    private List downloadRepositories = new ArrayList();
    private List userRepositories = new ArrayList();
    private Kernel kernel;
    private AbstractName name;
    private URL repositoryList;

    public PluginRepositoryDownloader(List downloadRepositories, List userRepositories, URL repositoryList, Kernel kernel, AbstractName name) {
        if(downloadRepositories != null) this.downloadRepositories = downloadRepositories;
        if(userRepositories != null) this.userRepositories = userRepositories;
        this.repositoryList = repositoryList;
        this.kernel = kernel;
        this.name = name;
    }

    /**
     * The list of repositories that were downloaded from central.
     */
    public void setDownloadRepositories(List downloadRepositories) {
        this.downloadRepositories = downloadRepositories;
        if(this.downloadRepositories == null) this.downloadRepositories = new ArrayList();
    }

    /**
     * Any repositories that the user added manually
     */
    public void setUserRepositories(List userRepositories) {
        this.userRepositories = userRepositories;
        if(this.userRepositories == null) this.userRepositories = new ArrayList();
    }

    /**
     * Gets the union of centrally-listed repositories and user-added repositories.
     */
    public URL[] getRepositories() {
        List list = new ArrayList();
        for (int i = 0; i < downloadRepositories.size(); i++) {
            String url = (String) downloadRepositories.get(i);
            try {
                list.add(new URL(url));
            } catch (MalformedURLException e) {
                log.error("Unable to format plugin repository URL "+url, e);
            }
        }
        for (int i = 0; i < userRepositories.size(); i++) {
            String url = (String) userRepositories.get(i);
            try {
                list.add(new URL(url));
            } catch (MalformedURLException e) {
                log.error("Unable to format plugin repository URL "+url, e);
            }
        }
        return (URL[]) list.toArray(new URL[list.size()]);
    }

    /**
     * Go download a fresh copy of the repository list.
     */
    public void refresh() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(repositoryList.openStream()));
            String line;
            List list = new ArrayList();
            while((line = in.readLine()) != null) {
                line = line.trim();
                if(!line.equals("") && !line.startsWith("#")) {
                    list.add(line);
                }
            }
            in.close();
            kernel.setAttribute(name, "downloadRepositories", list);
        } catch (Exception e) {
            log.error("Unable to save download repositories", e);
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
        infoFactory.setConstructor(new String[]{"downloadRepositories","userRepositories","repositoryList","kernel","abstractName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
