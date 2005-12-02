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
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationInfo;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.log.GeronimoLogging;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.system.repository.ReadOnlyRepository;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationDump {
    private static Log log;
    static {
        // This MUST be done before the first log is acquired
        GeronimoLogging.initialize(GeronimoLogging.WARN);
        log = LogFactory.getLog(ConfigurationDump.class.getName());
    }
    private static ClassLoader classLoader = ConfigurationDump.class.getClassLoader();

    public static void main(String[] args) throws Exception {
        File geronimoBaseDir = new File(args[0]);
        System.setProperty("geronimo.base.dir", geronimoBaseDir.getAbsolutePath());

        // create the kernel
        Kernel kernel = KernelFactory.newInstance().createKernel("geronimo");
        kernel.boot();

        PrintWriter out = new PrintWriter(System.out, true);
        try {
            ObjectName serverInfoName = new ObjectName("configdump:name=ServerInfo");
            GBeanData serverInfoData = new GBeanData(serverInfoName, BasicServerInfo.GBEAN_INFO);
            startGBean(kernel, serverInfoData);

            // add the repositories
            ObjectName geronimoRepositoryName = new ObjectName("configdump:name=Repository,type=Geronimo");
            GBeanData geronimoRepositoryData = new GBeanData(geronimoRepositoryName, ReadOnlyRepository.GBEAN_INFO);
            geronimoRepositoryData.setAttribute("root", URI.create("repository"));
            geronimoRepositoryData.setReferencePattern("ServerInfo", serverInfoName);
            startGBean(kernel, geronimoRepositoryData);

            String mavenLocalRepo = System.getProperty("maven.repo.local");
            File mavenRepositoryDir;
            if (mavenLocalRepo != null) {
                mavenRepositoryDir = new File(mavenLocalRepo);
            } else {
                mavenRepositoryDir = new File(System.getProperty("user.home"), ".maven");
                mavenRepositoryDir = new File(mavenRepositoryDir, "repository");
            }
            if (mavenRepositoryDir.isDirectory()) {
                ObjectName mavenRepositoryName = new ObjectName("configdump:name=Repository,type=Maven");
                GBeanData mavenRepositoryData = new GBeanData(mavenRepositoryName, ReadOnlyRepository.GBEAN_INFO);
                mavenRepositoryData.setAttribute("root", mavenRepositoryDir.getAbsoluteFile().toURI());
                mavenRepositoryData.setReferencePattern("ServerInfo", serverInfoName);
                startGBean(kernel, mavenRepositoryData);
            }

            ObjectName localConfigStoreName = new ObjectName("configdump:name=LocalConfigStore");
            GBeanData localConfigStoreData = new GBeanData(localConfigStoreName, LocalConfigStore.GBEAN_INFO);
            localConfigStoreData.setAttribute("root", URI.create("config-store"));
            localConfigStoreData.setReferencePattern("ServerInfo", serverInfoName);
            startGBean(kernel, localConfigStoreData);

            ConfigurationStore configurationStore = (ConfigurationStore) kernel.getProxyManager().createProxy(localConfigStoreName, classLoader);
            List configurationInfos = configurationStore.listConfigurations();
            for (Iterator iterator = configurationInfos.iterator(); iterator.hasNext();) {
                ConfigurationInfo configurationInfo = (ConfigurationInfo) iterator.next();
                URI configID = configurationInfo.getConfigID();
                dumpConfiguration(kernel, configurationStore, configID, out);
            }

        } catch (StartUpError e) {
            // ignore
        } finally {
            // shutdown the kernel
            kernel.shutdown();
        }
    }

    private static void startGBean(Kernel kernel, GBeanData gbeanData) throws Exception {
        kernel.loadGBean(gbeanData, classLoader);
        kernel.startGBean(gbeanData.getName());
        if (kernel.getGBeanState(gbeanData.getName()) != State.RUNNING_INDEX) {
            System.out.println("Failed to start " + gbeanData.getName());
            throw new StartUpError();
        }
    }

    private static void dumpConfiguration(Kernel kernel, ConfigurationStore configurationStore, URI id, PrintWriter out) throws Exception {
        out.println("==================================================");
        out.println("= " + id);
        out.println("==================================================");

        loadRecursive(kernel, configurationStore, id);
        ObjectName name = null;
        try {
            name = Configuration.getConfigurationObjectName(id);
        } catch (MalformedObjectNameException e) {
        }
        out.println("objectName: " + name);

        GBeanData config = kernel.getGBeanData(name);
        ConfigurationModuleType moduleType = (ConfigurationModuleType) config.getAttribute("type");
        out.println("type: " + moduleType);

        String domain = (String) config.getAttribute("domain");
        out.println("domain: " + domain);

        String server = (String) config.getAttribute("server");
        out.println("server: " + server);

        URI[] parentIds = (URI[]) config.getAttribute("parentId");
        if (parentIds != null && parentIds.length > 0) {
            out.println("parents: ");
            for (int i = 0; i < parentIds.length; i++) {
                URI parentId = parentIds[i];
                out.println("  " + parentId);
            }
        } else {
            out.println("parents: none");
        }

        List dependencies = (List) config.getAttribute("dependencies");
        if (dependencies != null && !dependencies.isEmpty()) {
            out.println("dependencies: ");
            for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
                URI path = (URI) iterator.next();
                out.println("  " + path);
            }
        } else {
            out.println("dependencies: none");
        }

        List classPath = (List) config.getAttribute("classPath");
        if (classPath != null && !classPath.isEmpty()) {
            out.println("classPath: ");
            for (Iterator iterator = classPath.iterator(); iterator.hasNext();) {
                URI path = (URI) iterator.next();
                out.println("  " + path);
            }
        } else {
            out.println("classPath: none");
        }

        boolean inverseClassLoading = ((Boolean) config.getAttribute("inverseClassLoading")).booleanValue();
        out.println("inverseClassLoading: " + inverseClassLoading);


        String[] hiddenClasses = (String[]) config.getAttribute("hiddenClasses");
        if (hiddenClasses != null && hiddenClasses.length > 0) {
            out.println("hiddenClasses: ");
            for (int i = 0; i < hiddenClasses.length; i++) {
                String hiddenClass = hiddenClasses[i];
                out.println("  " + hiddenClass);
            }
        } else {
            out.println("hiddenClasses: none");
        }

        String[] nonOverridableClasses = (String[]) config.getAttribute("nonOverridableClasses");
        if (nonOverridableClasses != null && nonOverridableClasses.length > 0) {
            out.println("nonOverridableClasses: ");
            for (int i = 0; i < hiddenClasses.length; i++) {
                String nonOverridableClass = nonOverridableClasses[i];
                out.println("  " + nonOverridableClass);
            }
        } else {
            out.println("nonOverridableClasses: none");
        }

        kernel.startGBean(name);
        if (kernel.getGBeanState(name) != State.RUNNING_INDEX) {
            System.out.println("Failed to start " + name);
            return;
        }

        Configuration configuration = (Configuration) kernel.getProxyManager().createProxy(name, Configuration.class);
        Collection collection = configuration.loadGBeans();
        for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
            GBeanData gbeanData = (GBeanData) iterator.next();
            out.println();
            out.println();
            out.println("gbean: " + gbeanData.getName());
            out.println("gbeanInfo: " + gbeanData.getGBeanInfo().getSourceClass());

            Map attributes = gbeanData.getAttributes();
            if (attributes != null && !attributes.isEmpty()) {
                out.println("attributes: ");
                for (Iterator attributeIterator = attributes.entrySet().iterator(); attributeIterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) attributeIterator.next();
                    String attributeName = (String) entry.getKey();
                    Object attributeValue = entry.getValue();
                    out.println("    " + attributeName + " := " + attributeValue);
                }
            } else {
                out.println("attributes: none");
            }
            Map references = gbeanData.getReferences();
            if (references != null && !attributes.isEmpty()) {
                out.println("references: ");
                for (Iterator referenceIterator = references.entrySet().iterator(); referenceIterator.hasNext();) {
                    Map.Entry entry = (Map.Entry) referenceIterator.next();
                    String referenceName = (String) entry.getKey();
                    Collection referencePatterns = (Collection) entry.getValue();
                    if (referencePatterns.size() == 1) {
                        out.println("    " + referenceName + " := " + referencePatterns.iterator().next());
                    } else {
                        out.println("    " + referenceName + " := ");
                        for (Iterator patternIterator = referencePatterns.iterator(); patternIterator.hasNext();) {
                            ObjectName pattern = (ObjectName) patternIterator.next();
                            out.println("        " + pattern);
                        }
                    }
                }
            } else {
                out.println("references: none");
            }
        }
    }

    private static final ObjectName CONFIGURATION_NAME_QUERY;
    static {
        try {
            CONFIGURATION_NAME_QUERY = new ObjectName("geronimo.config:*");
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("could not create object name... bug", e);
        }
    }

    public static void loadRecursive(Kernel kernel, ConfigurationStore configurationStore, URI configID) throws Exception {
        LinkedList ancestors = new LinkedList();
        Set preloaded = kernel.listGBeans(CONFIGURATION_NAME_QUERY);
        loadRecursive(kernel, configurationStore, configID, ancestors, preloaded);

        for (Iterator iterator = ancestors.iterator(); iterator.hasNext();) {
            URI name = (URI) iterator.next();
            ObjectName configName = Configuration.getConfigurationObjectName(name);
            kernel.startGBean(configName);
        }
    }

    private static void loadRecursive(Kernel kernel, ConfigurationStore configurationStore, URI configID, LinkedList ancestors, Set preloaded) throws Exception {
        ObjectName name = Configuration.getConfigurationObjectName(configID);
        if (preloaded.contains(name)) {
            return;
        }
        if (!kernel.isLoaded(name)) {
            configurationStore.loadConfiguration(configID);
        }
        //put the earliest ancestors first, even if we have already started them.
        ancestors.remove(configID);
        ancestors.addFirst(configID);
        URI[] parents = (URI[]) kernel.getAttribute(name, "parentId");
        if (parents != null) {
            for (int i = 0; i < parents.length; i++) {
                URI parent = parents[i];
                loadRecursive(kernel, configurationStore, parent, ancestors, preloaded);
            }
        }
    }

    private static class StartUpError extends Error {
    }
}
