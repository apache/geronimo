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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.MultiGBeanInfoFactory;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.apache.geronimo.system.configuration.condition.ParserUtils;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.xml.sax.SAXException;

/**
 * Stores managed attributes in an XML file on the local filesystem.
 *
 * @version $Rev$ $Date$
 */
public class LocalAttributeManager implements LocalPluginAttributeStore, PersistentConfigurationList, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(LocalAttributeManager.class);

    private static final String CONFIG_FILE_PROPERTY = "org.apache.geronimo.config.file";
    private final static String SUBSTITUTIONS_FILE_PROPERTY = "org.apache.geronimo.config.substitutions.file";
    private final static String SUBSTITUTION_PREFIX_PREFIX = "org.apache.geronimo.config.substitution.prefix";

    private static final String BACKUP_EXTENSION = ".bak";
    private static final String TEMP_EXTENSION = ".working";
    private static final int SAVE_BUFFER_MS = 5000;

    private final ServerInfo serverInfo;
    private final String configFile;
    private final boolean readOnly;
    private final JexlExpressionParser expressionParser;

    private File attributeFile;
    private File backupFile;
    private File tempFile;
    private ServerOverride serverOverride;

    private Timer timer;
    private TimerTask currentTask;

    private boolean kernelFullyStarted;

    private String prefix;
    private File configSubstitutionsFile;
    private Properties localConfigSubstitutions;
    private String resolvedPropertiesFile;
    private final GBeanInfoFactory infoFactory;

    public LocalAttributeManager(String configFile, String configSubstitutionsFileName, String configSubstitutionsPrefix, boolean readOnly, ServerInfo serverInfo) {
        this.configFile = System.getProperty(CONFIG_FILE_PROPERTY, configFile);
        resolvedPropertiesFile = System.getProperty(SUBSTITUTIONS_FILE_PROPERTY, configSubstitutionsFileName);
        configSubstitutionsFile = resolvedPropertiesFile == null? null: serverInfo.resolveServer(resolvedPropertiesFile);
        localConfigSubstitutions = loadConfigSubstitutions(configSubstitutionsFile);
        prefix = System.getProperty(SUBSTITUTION_PREFIX_PREFIX, configSubstitutionsPrefix);
        Map<String, Object> configSubstitutions = loadAllConfigSubstitutions(localConfigSubstitutions, prefix);
        expressionParser = new JexlExpressionParser(configSubstitutions);
        this.readOnly = readOnly;
        this.serverInfo = serverInfo;
        serverOverride = new ServerOverride();
        
        log.debug("setting configSubstitutionsFile to: {}", configSubstitutionsFile);
        
        infoFactory = newGBeanInfoFactory();
    }

    protected GBeanInfoFactory newGBeanInfoFactory() {
        return new MultiGBeanInfoFactory();
    }

    public boolean isReadOnly() {
        return readOnly;
    }


    public String getConfigFile() {
        return configFile;
    }

    public String getConfigSubstitutionsFile() {
        return resolvedPropertiesFile;
    }

    public String getConfigSubstitutionsPrefix() {
        return prefix;
    }

    public synchronized Collection applyOverrides(Artifact configName, Collection<GBeanData> untypedGbeanDatas, ClassLoader classLoader) throws InvalidConfigException {
        // clone the datas since we will be modifying this collection
        Collection<GBeanData> gbeanDatas = new ArrayList<GBeanData>(untypedGbeanDatas);

        ConfigurationOverride configuration = serverOverride.getConfiguration(configName);
        if (configuration == null) {
            return gbeanDatas;
        }

        // index the incoming datas
        Map<Object, GBeanData> datasByName = new HashMap<Object, GBeanData>();
        for (GBeanData gbeanData : gbeanDatas) {
            datasByName.put(gbeanData.getAbstractName(), gbeanData);
            datasByName.put(gbeanData.getAbstractName().getName().get("name"), gbeanData);
        }

        // add the new GBeans
        for (Object o : configuration.getGBeans().entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object name = entry.getKey();
            GBeanOverride gbean = (GBeanOverride) entry.getValue();
            if (!datasByName.containsKey(name) && gbean.isLoad()) {
                if (gbean.getGBeanInfo() == null || !(name instanceof AbstractName)) {
                    String sep = "";
                    StringBuffer message = new StringBuffer("New GBeans must be specified with ");
                    if (gbean.getGBeanInfo() == null) {
                        message.append("a GBeanInfo ");
                        sep = "and ";
                    }
                    if (!(name instanceof AbstractName)) {
                        message.append(sep).append("a full AbstractName ");
                    }
                    message.append("configuration=").append(configName);
                    message.append(" gbeanName=").append(name);
                    throw new InvalidConfigException(message.toString());
                }
                GBeanInfo gbeanInfo = infoFactory.getGBeanInfo(gbean.getGBeanInfo(), classLoader);
                AbstractName abstractName = (AbstractName) name;
                GBeanData gBeanData = new GBeanData(abstractName, gbeanInfo);
                gbeanDatas.add(gBeanData);
            }
        }

        // set the attributes
        for (Iterator iterator = gbeanDatas.iterator(); iterator.hasNext();) {
            GBeanData data = (GBeanData) iterator.next();
            boolean load = setAttributes(data, configuration, configName, classLoader);
            if (!load) {
                iterator.remove();
            }
        }
        return gbeanDatas;
    }

    /**
     * Set the attributes from the attribute store on a single gbean, and return whether or not to load the gbean.
     *
     * @param data          GBeanData we are going to override attributes on
     * @param configuration the module override the gbean relates to
     * @param configName    name of the module (why can't this be determined from the configuration?)
     * @param classLoader   ClassLoader to use for property objects/PropertyEditors
     * @return true if the gbean should be loaded, false otherwise.
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException
     *          if we cannot update the gbeanData
     */
    private synchronized boolean setAttributes(GBeanData data, ConfigurationOverride configuration, Artifact configName, ClassLoader classLoader) throws InvalidConfigException {
        AbstractName gbeanName = data.getAbstractName();
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean((String) gbeanName.getName().get("name"));
        }

        if (gbean == null) {
            //no attr info, load by default
            return true;
        }

        return gbean.applyOverrides(data, configName, gbeanName, classLoader);
    }

    public void setModuleGBeans(Artifact moduleName, List<GbeanType> gbeans, boolean load, String condition) throws InvalidGBeanException {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(moduleName, true);
        if (gbeans != null) {
            for (GbeanType gbean : gbeans) {
                GBeanOverride override = new GBeanOverride(gbean, expressionParser);
                configuration.addGBean(override);
            }
        }
        configuration.setLoad(load);
        configuration.setCondition(condition);
        
        log.debug("Added gbeans for module: {}  load: {}",  moduleName, load);
        
        attributeChanged();
    }

    public void addConfigSubstitutions(Properties properties) {
        localConfigSubstitutions.putAll(properties);
        Map<String, Object> configSubstutions = loadAllConfigSubstitutions(localConfigSubstitutions, prefix);
        storeConfigSubstitutions(configSubstitutionsFile, localConfigSubstitutions);
        expressionParser.setVariables(configSubstutions);
    }

    public synchronized void setValue(Artifact configurationName, AbstractName gbeanName, GAttributeInfo attribute, Object value, ClassLoader classLoader) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean((String) gbeanName.getName().get("name"));
            if (gbean == null) {
                gbean = new GBeanOverride(gbeanName, true, expressionParser);
                configuration.addGBean(gbeanName, gbean);
            }
        }

        try {
            gbean.setAttribute(attribute.getName(), value, attribute.getType(), classLoader);
            attributeChanged();
        } catch (InvalidAttributeException e) {
            // attribute can not be represented as a string
            log.error(e.getMessage());
        }
    }

    public synchronized void setReferencePatterns(Artifact configurationName, AbstractName gbeanName, GReferenceInfo reference, ReferencePatterns patterns) {
        if (readOnly) {
            return;
        }

        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean((String) gbeanName.getName().get("name"));
            if (gbean == null) {
                gbean = new GBeanOverride(gbeanName, true, expressionParser);
                configuration.addGBean(gbeanName, gbean);
            }
        }
        gbean.setReferencePatterns(reference.getName(), patterns);
        attributeChanged();
    }

    public synchronized void setShouldLoad(Artifact configurationName, AbstractName gbeanName, boolean load) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, true);

        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            // attempt to lookup by short name
            gbean = configuration.getGBean((String) gbeanName.getName().get("name"));
        }

        if (gbean == null) {
            gbean = new GBeanOverride(gbeanName, load, expressionParser);
            configuration.addGBean(gbeanName, gbean);
        } else {
            gbean.setLoad(load);
        }
        attributeChanged();
    }

    public void addGBean(Artifact configurationName, GBeanData gbeanData, ClassLoader classLoader) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName);
        if (configuration == null) {
            log.debug("Can not add GBean; Configuration not found {}", configurationName);
            return;
        }
        try {
            GBeanOverride gbean = new GBeanOverride(gbeanData, expressionParser, classLoader);
            configuration.addGBean(gbean);
            attributeChanged();
        } catch (InvalidAttributeException e) {
            // attribute can not be represented as a string
            log.error(e.getMessage());
        }
    }

    public synchronized void load() throws IOException {
        ensureParentDirectory();
        if (!attributeFile.exists()) {
            return;
        }
        Reader input = new BufferedReader(new FileReader(attributeFile));

        try {
            serverOverride = read(input, expressionParser);

        } catch (SAXException e) {
            log.error("Unable to read saved manageable attributes", e);
        } catch (ParserConfigurationException e) {
            log.error("Unable to read saved manageable attributes", e);
        } catch (InvalidGBeanException e) {
            log.error("Unable to read saved manageable attributes", e);
        } catch (JAXBException e) {
            log.error("Unable to read saved manageable attributes", e);
        } catch (XMLStreamException e) {
            log.error("Unable to read saved manageable attributes", e);
        } finally {
            // input is always non-null
            input.close();
        }
    }

    static ServerOverride read(Reader input, JexlExpressionParser expressionParser) throws ParserConfigurationException, IOException, SAXException, JAXBException, XMLStreamException, InvalidGBeanException {
        AttributesType attributes = AttributesXmlUtil.loadAttributes(input);
        return new ServerOverride(attributes, expressionParser);
    }

    public synchronized void save() throws IOException {
        if (readOnly) {
            return;
        }
        ensureParentDirectory();
        if (!tempFile.exists() && !tempFile.createNewFile()) {
            throw new IOException("Unable to create manageable attribute working file for save " + tempFile.getAbsolutePath());
        }
        if (!tempFile.canWrite()) {
            throw new IOException("Unable to write to manageable attribute working file for save " + tempFile.getAbsolutePath());
        }

        // write the new configuration to the temp file
        saveXmlToFile(tempFile, serverOverride);

        // delete the current backup file
        if (backupFile.exists()) {
            if (!backupFile.delete()) {
                throw new IOException("Unable to delete old backup file in order to back up current manageable attribute working file for save");
            }
        }

        // rename the existing configuration file to the backup file
        if (attributeFile.exists()) {
            if (!attributeFile.renameTo(backupFile)) {
                throw new IOException("Unable to rename " + attributeFile.getAbsolutePath() + " to " + backupFile.getAbsolutePath() + " in order to back up manageable attribute save file");
            }
        }

        // rename the temp file the the configuration file
        if (!tempFile.renameTo(attributeFile)) {
            throw new IOException(
                    "EXTREMELY CRITICAL!  Unable to move manageable attributes working file to proper file name!  Configuration will revert to defaults unless this is manually corrected!  (could not rename " + tempFile.getAbsolutePath() + " to " + attributeFile.getAbsolutePath() + ")");
        }
    }

    private static void saveXmlToFile(File file, ServerOverride serverOverride) {
        try {
            Writer fileOut = new FileWriter(file);
            try {
                Writer writer = new BufferedWriter(fileOut);
                write(serverOverride, writer);
            } catch (JAXBException e) {
                log.error("Unable to write config.xml", e);
            } catch (XMLStreamException e) {
                log.error("Unable to write config.xml", e);
            } finally {
                fileOut.close();
            }
        } catch (IOException e) {
            log.error("Unable to write config.xml", e);
        }
   }

    static void write(ServerOverride serverOverride, Writer writer) throws XMLStreamException, JAXBException, IOException {
        AttributesType attributes = serverOverride.writeXml();
        AttributesXmlUtil.writeAttributes(attributes, writer);
        writer.flush();
    }

    //PersistentConfigurationList
    public synchronized boolean isKernelFullyStarted() {
        return kernelFullyStarted;
    }

    public synchronized void setKernelFullyStarted(boolean kernelFullyStarted) {
        this.kernelFullyStarted = kernelFullyStarted;
    }

    public synchronized List<Artifact> restore() throws IOException {
        List<Artifact> configs = new ArrayList<Artifact>();
        for (Map.Entry<Artifact, ConfigurationOverride> entry : serverOverride.getConfigurations().entrySet()) {
            ConfigurationOverride configuration = entry.getValue();
            if (configuration.isLoad()) {
                Artifact configID = entry.getKey();
                configs.add(configID);
            }
        }
        return configs;
    }

    public void startConfiguration(Artifact configurationName) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, false);
        if (configuration == null) {
            return;
        }
        configuration.setLoad(true);
        attributeChanged();
    }

    public synchronized void addConfiguration(Artifact configurationName) {
        if (readOnly) {
            return;
        }
        // Check whether we have it already
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName, false);
        // If not, initialize it
        if (configuration == null) {
            configuration = serverOverride.getConfiguration(configurationName, true);
            configuration.setLoad(false);
            attributeChanged();
        }
    }

    public synchronized void removeConfiguration(Artifact configName) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configName);
        if (configuration == null) {
            return;
        }
        serverOverride.removeConfiguration(configName);
        attributeChanged();
    }

    public Artifact[] getListedConfigurations(Artifact query) {
        return serverOverride.queryConfigurations(query);
    }

    public void stopConfiguration(Artifact configName) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configName);
        if (configuration == null) {
            return;
        }
        configuration.setLoad(false);
        attributeChanged();
    }

    public void migrateConfiguration(Artifact oldName, Artifact newName, Configuration configuration) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configInfo = serverOverride.getConfiguration(oldName);
        if (configInfo == null) {
            throw new IllegalArgumentException("Trying to migrate unknown configuration: " + oldName);
        }
        serverOverride.removeConfiguration(oldName);
        configInfo = new ConfigurationOverride(configInfo, newName);
        //todo: check whether all the attributes are still valid for the new configuration
        serverOverride.addConfiguration(configInfo);
        attributeChanged();
    }

    /**
     * This method checks if there are any custom gbean attributes in the configuration.
     *
     * @param configName Name of the configuration
     * @return true if the configuration contains any custom gbean attributes
     */
    public boolean hasGBeanAttributes(Artifact configName) {
        ConfigurationOverride configInfo = serverOverride.getConfiguration(configName);
        return configInfo != null && !configInfo.getGBeans().isEmpty();
    }

    //GBeanLifeCycle
    public synchronized void doStart() throws Exception {
        load();
        if (!readOnly) {
            timer = new Timer(true);
        }
        log.debug("Started LocalAttributeManager with data on {} configurations", serverOverride.getConfigurations().size());
    }

    public synchronized void doStop() throws Exception {
        boolean doSave = false;
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                if (currentTask != null) {
                    currentTask.cancel();
                    doSave = true;
                }
            }
        }
        if (doSave) {
            save();
        }
        log.debug("Stopped LocalAttributeManager with data on {} configurations", serverOverride.getConfigurations().size());
        serverOverride = new ServerOverride();
    }

    public synchronized void doFail() {
        synchronized (this) {
            if (timer != null) {
                timer.cancel();
                if (currentTask != null) {
                    currentTask.cancel();
                }
            }
        }
        serverOverride = new ServerOverride();
    }

    private synchronized void ensureParentDirectory() throws IOException {
        if (attributeFile == null) {
            attributeFile = serverInfo.resolveServer(configFile);
            tempFile = new File(attributeFile.getAbsolutePath() + TEMP_EXTENSION);
            backupFile = new File(attributeFile.getAbsolutePath() + BACKUP_EXTENSION);
        }
        File parent = attributeFile.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Unable to create directory for list:" + parent);
            }
        }
        if (!parent.canRead()) {
            throw new IOException("Unable to read manageable attribute files in directory " + parent.getAbsolutePath());
        }
        if (!readOnly && !parent.canWrite()) {
            throw new IOException("Unable to write manageable attribute files to directory " + parent.getAbsolutePath());
        }
    }

    private synchronized void attributeChanged() {
        if (currentTask != null) {
            currentTask.cancel();
        }
        if (timer != null) {
            currentTask = new TimerTask() {

                public void run() {
                    try {
                        LocalAttributeManager.this.save();
                    } catch (IOException e) {
                        log.error("IOException occurred while saving attributes", e);
                    } catch (Throwable t) {
                        log.error("Error occurred during execution of attributeChanged TimerTask", t);
                    }
                }
            };
            timer.schedule(currentTask, SAVE_BUFFER_MS);
        }
    }

    private static Map<String, Object> loadAllConfigSubstitutions(Properties configSubstitutions, String prefix) {
        Map<String, Object> vars = new HashMap<String, Object>();
        //most significant are the command line system properties
        addGeronimoSubstitutions(vars, System.getProperties(), prefix);
        //environment variables are next
        addGeronimoSubstitutions(vars, System.getenv(), prefix);
        //properties file is least significant
        if (configSubstitutions != null) {
            addGeronimoSubstitutions(vars, configSubstitutions, "");
        }
        ParserUtils.addDefaultVariables(vars);
        return vars;
    }

    private static Properties loadConfigSubstitutions(File configSubstitutionsFile) {
        Properties properties = new Properties();
        if (configSubstitutionsFile != null) {
            if (!configSubstitutionsFile.exists()) {
                log.warn("Could not find the config substitution file: "
                        + configSubstitutionsFile.getAbsolutePath());
            } else {
                try {
                    FileInputStream in = new FileInputStream(configSubstitutionsFile);
                    try {
                        properties.load(in);
                    } finally {
                        in.close();
                    }
                } catch (Exception e) {
                    log.error("Caught exception " + e
                            + " trying to open properties file " + configSubstitutionsFile.getAbsolutePath());
                }
            }
        }
        return properties;
    }

    private static void storeConfigSubstitutions(File configSubstitutionsFile, Properties properties) {
        if (configSubstitutionsFile != null) {
            try {
                FileOutputStream out = new FileOutputStream(configSubstitutionsFile);
                try {
                    properties.store(out, null);
                } finally {
                    out.close();
                }
            } catch (Exception e) {
                log.error("Caught exception {} trying to open properties file {}", e, configSubstitutionsFile.getAbsolutePath());
            }
        }
    }

    private static void addGeronimoSubstitutions(Map<String, Object> vars, Map props, String prefix) {
        if (prefix != null) {
            int start = prefix.length();
            for (Object o : props.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                if (((String) entry.getKey()).startsWith(prefix)) {
                    String key = ((String) entry.getKey()).substring(start);
                    if (!vars.containsKey(key)) {
                        vars.put(key, (String) entry.getValue());
                    }
                }
            }
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(LocalAttributeManager.class, "AttributeStore");
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.addAttribute("configFile", String.class, true);
        infoFactory.addAttribute("readOnly", boolean.class, true);
        infoFactory.addAttribute("substitutionsFile", String.class, true);
        infoFactory.addAttribute("substitutionPrefix", String.class, true);
        infoFactory.addInterface(ManageableAttributeStore.class);
        infoFactory.addInterface(PersistentConfigurationList.class);

        infoFactory.setConstructor(new String[]{"configFile", "substitutionsFile", "substitutionPrefix", "readOnly", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
