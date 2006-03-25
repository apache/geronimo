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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.WriteableRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A GBean that knows how to download configurations from a Maven repository.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ConfigInstallerGBean implements ConfigurationInstaller {
    private final static Log log = LogFactory.getLog(ConfigInstallerGBean.class);
    private Collection configurations;
    private WriteableRepository writeableRepo;
    private ConfigurationStore configStore;
    private Map configIdToFile = new HashMap();

    public ConfigInstallerGBean(Collection configurations, WriteableRepository writeableRepo, ConfigurationStore configStore) {
        this.configurations = configurations;
        this.writeableRepo = writeableRepo;
        this.configStore = configStore;
    }

    public ConfigurationMetadata[] listConfigurations(URL mavenRepository) throws IOException {
        String repository = mavenRepository.toString();
        if(!repository.endsWith("/")) {
            repository = repository+"/";
        }
        URL url = new URL(repository+"geronimo-configurations.properties");
        Set set = new HashSet();
        for (Iterator it = configurations.iterator(); it.hasNext();) {
            Configuration config = (Configuration) it.next();
            set.add(config.getId().toString());
        }
        InputStream in = url.openStream();
        Properties props = new Properties();
        props.load(in);
        in.close();
        List results = new ArrayList();
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            int pos = key.indexOf('.');
            String type = key.substring(0, pos);
            String configId = key.substring(pos + 1);
            try {
                results.add(new ConfigurationMetadata(new URI(configId), props.getProperty(key), type, set.contains(configId)));
            } catch (URISyntaxException e) {
                throw new IOException("Unable to create configID URI: "+e.getMessage());
            }
        }
        return (ConfigurationMetadata[]) results.toArray(new ConfigurationMetadata[results.size()]);
    }

    public ConfigurationMetadata loadDependencies(URL mavenRepository, ConfigurationMetadata source) throws IOException {
        String conf = source.getConfigId().toString();
        File file = File.createTempFile("geronimo-download", "." + conf.substring(conf.lastIndexOf("/")+1));
        file.deleteOnExit();
        String url = getURL(conf, mavenRepository.toString());
        downloadFile(url, file); //todo: download only SNAPSHOTS if previously available?
        configIdToFile.put(source.getConfigId(), file);
        ZipFile zip = new ZipFile(file);
        try {
            ZipEntry entry = zip.getEntry("META-INF/config.ser");
            ObjectInputStream serIn = new ObjectInputStream(zip.getInputStream(entry));
            GBeanData config = new GBeanData();
            config.readExternal(serIn);
            URI[] parentIds = (URI[]) config.getAttribute("parentId");
            List dependencies = (List) config.getAttribute("dependencies");
            source.setDependencies((URI[]) dependencies.toArray(new URI[dependencies.size()]));
            source.setParents(parentIds);
            return source;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to deserialize GBeanData (can't load class "+e.getMessage()+")");
        } finally {
            zip.close();
        }
    }

    public DownloadResults install(URL mavenRepository, URI configId) throws IOException {
        Set set = new HashSet();
        for (Iterator it = configurations.iterator(); it.hasNext();) {
            Configuration next = (Configuration) it.next();
            set.add(next.getId());
        }
        DownloadResults results = new DownloadResults();
        processConfiguration(configId,writeableRepo,mavenRepository.toString(),set,results);
        return results;
    }




    private String getURL(String configId, String baseRepositoryURL) {
        String[] parts = configId.split("/");
        return baseRepositoryURL+parts[0]+"/"+parts[3]+"s/"+parts[1]+"-"+parts[2]+"."+parts[3];
    }

    private void downloadFile(String url, File target) throws IOException {
        log.info("Downloading "+url+" to "+target.getAbsolutePath());
        byte[] buf = new byte[10240];
        InputStream in = new URL(url).openStream();
        FileOutputStream out = new FileOutputStream(target);
        int count;
        while((count = in.read(buf)) > -1) {
            out.write(buf, 0, count);
        }
        in.close();
        out.close();
    }

    private void processConfiguration(URI configId, WriteableRepository repo, String repoURL, Set configurations, DownloadResults results) throws IOException {
        // Make sure we have a local copy of the CAR
        String id = configId.toString();
        File file;
        if(configIdToFile.containsKey(configId)) {
            file = (File) configIdToFile.get(configId);
        } else {
            file = File.createTempFile("geronimo-download", "." + id.substring(id.lastIndexOf("/")+1));
            file.deleteOnExit();
        }
        String configUrl = getURL(id, repoURL);
        downloadFile(configUrl, file);
        results.addConfigurationInstalled(configId);

        // Process the contents of the CAR
        ZipFile zip = new ZipFile(file);
        try {
            ZipEntry entry = zip.getEntry("META-INF/config.ser");
            ObjectInputStream serIn = new ObjectInputStream(zip.getInputStream(entry));
            GBeanData config = new GBeanData();
            config.readExternal(serIn);
            URI[] parentIds = (URI[]) config.getAttribute("parentId");
            List dependencies = (List) config.getAttribute("dependencies");
            // Download the dependencies
            for (int i = 0; i < dependencies.size(); i++) {
                URI dep = (URI) dependencies.get(i);
                if(repo.hasURI(dep)) {
                    results.addDependencyPresent(dep);
                    continue;
                }
                String url = getURL(dep.toString(), repoURL);
                log.info("Downloading "+url+" to local repository");
                repo.copyToRepository(new URL(url).openStream(), dep, null);
                results.addDependencyInstalled(dep);
            }
            // Download the parents
            for (int i = 0; i < parentIds.length; i++) {
                URI uri = parentIds[i];
                if(configurations.contains(uri)) {
                    results.addConfigurationPresent(uri);
                    continue;
                }
                processConfiguration(uri, repo, repoURL, configurations, results);
            }
            // Install the configuration
            configStore.install(file.toURL());
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to deserialize GBeanData: "+e.getMessage());
        } catch (InvalidConfigException e) {
            throw new IOException("Unable to install configuration: "+e.getMessage());
        } finally {
            zip.close();
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ConfigInstallerGBean.class);
        infoFactory.addReference("AllConfigurations", Configuration.class);
        infoFactory.addReference("DependencyInstallTarget", WriteableRepository.class, "GBean");
        infoFactory.addReference("ConfigurationInstallTarget", ConfigurationStore.class, "ConfigurationStore");
        infoFactory.addInterface(ConfigurationInstaller.class);

        infoFactory.setConstructor(new String[]{"AllConfigurations", "DependencyInstallTarget", "ConfigurationInstallTarget"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
