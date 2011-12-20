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
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.utils.properties.Properties;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.MultiGBeanInfoFactory;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.InvalidGBeanException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.configuration.condition.JexlExpressionParser;
import org.apache.geronimo.system.configuration.condition.ParserUtils;
import org.apache.geronimo.system.plugin.model.AttributesType;
import org.apache.geronimo.system.plugin.model.GbeanType;
import org.apache.geronimo.system.plugin.model.ModuleType;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Stores managed attributes in an XML file on the local filesystem.
 *
 * @version $Rev$ $Date$
 */

@Service()
@Component(metatype = true)
public class LocalAttributeManager implements LocalPluginAttributeStore, PersistentConfigurationList {
    private static final Logger log = LoggerFactory.getLogger(LocalAttributeManager.class);

    private static final String CONFIG_FILE_PROPERTY = "org.apache.geronimo.config.file";
    private final static String SUBSTITUTIONS_FILE_PROPERTY = "org.apache.geronimo.config.substitutions.file";
    private final static String SUBSTITUTION_PREFIX_PREFIX = "org.apache.geronimo.config.substitution.prefix";

    private static final String BACKUP_EXTENSION = ".bak";
    private static final String TEMP_EXTENSION = ".working";
    private static final int SAVE_BUFFER_MS = 5000;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private ServerInfo serverInfo;

    @Property(value = "var/config/config.xml")
    public static final String CONFIG_FILE_KEY = ".configFile";
    private String configFile;

    @Property(value = "var/config/config-substitutions.properties")
    public static final String CONFIG_SUBSTITUTIONS_FILE_KEY = ".configSubstitutionsFile";
    private String configSubstitutionsFileName;


    private JexlExpressionParser expressionParser;

    private File attributeFile;
    private File backupFile;
    private File tempFile;
    private ServerOverride serverOverride = new ServerOverride();

    private Timer timer;
    private TimerTask currentTask;

    private boolean kernelFullyStarted;

    private String prefix;
    private File configSubstitutionsFile;
    private Properties localConfigSubstitutions;
    private String resolvedPropertiesFile;
    private final GBeanInfoFactory infoFactory = new MultiGBeanInfoFactory();
    static final String OVERRIDES_KEY = ".overrides";
    private static final byte[] INSTRUCTION = ("# Put variables and their substitution values in this file. \n"
            + "# They will be used when processing the corresponding config.xml. \n"
            + "# Values in this file can be overridden by environment variables and system properties \n"
            + "# by prefixing the property name with 'org.apache.geronimo.config.substitution.' \n"
            + "# For example, an entry such as hostName=localhost \n"
            + "# can be overridden by an environment variable or system property org.apache.geronimo.config.substitution.hostName=foo \n"
            + "# When running multiple instances of Geronimo choose a PortOffset value such that none of the ports conflict. \n"
            + "# For example, try PortOffset=10 \n").getBytes();

    @Property(value = "org.apache.geronimo.config.substitution.")
    public static final String PREFIX_KEY = ".configSubstitutionsPrefix";

    @Property(boolValue = false)
    public static final String READ_ONLY_KEY = "readOnly";
    private boolean readOnly;

    @Property(value = "default")
    public static final String SERVER_NAME_KEY = "serverName";
    private String serverName;

/*
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

    }
*/

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public void unsetServerInfo(ServerInfo serverInfo) {
        if (serverInfo == this.serverInfo) {
            this.serverInfo = null;
        }
    }

    @Activate
    public void activate(Map<String, Object> properties) throws IOException {
        readOnly = (Boolean) properties.get(READ_ONLY_KEY);
        serverName = (String) properties.get(SERVER_NAME_KEY);
        String configSubstitutionsPrefix = (String) properties.remove(PREFIX_KEY);

        configFile = (String) properties.get(CONFIG_FILE_KEY);
        configFile = System.getProperty(CONFIG_FILE_PROPERTY, configFile);
        configSubstitutionsFileName = (String) properties.get(CONFIG_SUBSTITUTIONS_FILE_KEY);
        resolvedPropertiesFile = System.getProperty(SUBSTITUTIONS_FILE_PROPERTY, configSubstitutionsFileName);
        configSubstitutionsFile = resolvedPropertiesFile == null? null: serverInfo.resolveServer(resolvedPropertiesFile);
        localConfigSubstitutions = loadConfigSubstitutions(configSubstitutionsFile);
        prefix = System.getProperty(SUBSTITUTION_PREFIX_PREFIX, configSubstitutionsPrefix);
        Map<String, Object> configSubstitutions = loadAllConfigSubstitutions(localConfigSubstitutions, prefix);
        expressionParser = new JexlExpressionParser(configSubstitutions);

        load();
        if (!readOnly) {
            timer = new Timer(true);
        }
    }

    @Deactivate
    public synchronized void deactivate() throws Exception {
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

    /**
     * DS modified method.  This is called back every time the config admin Configuration is updated, but asynchronously.
     *
     * @param properties DS properties (which include configuration properties)
     * @throws IOException
     */
    @Modified
    public void modified(Map<String, Object> properties) throws IOException {
        activate(properties);
//        readOnly = (Boolean) properties.get(READ_ONLY_KEY);
//        serverName = (String) properties.get(SERVER_NAME_KEY);
//        String configSubstitutionsPrefix = (String) properties.remove(PREFIX_KEY);
//        String servicePid = (String) properties.get(Constants.SERVICE_PID);
//        this.configuration = configurationAdmin.getConfiguration(servicePid);
//
//        Dictionary<Object, Object> d = getConfigurationProperties();
//        String overrides = (String) d.get(OVERRIDES_KEY);
//        localConfigSubstitutions = loadConfigSubstitutions(d);
//        prefix = System.getProperty(SUBSTITUTION_PREFIX_PREFIX, configSubstitutionsPrefix);
//        Map<String, Object> configSubstitutions = loadAllConfigSubstitutions(localConfigSubstitutions, prefix);
//        expressionParser.setVariables(configSubstitutions);
//
//        load(overrides);
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

    public synchronized Collection<GBeanData> applyOverrides(Artifact configName, Collection<GBeanData> untypedGbeanDatas, Bundle bundle) throws InvalidConfigException {
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
                    StringBuilder message = new StringBuilder("New GBeans must be specified with ");
                    if (gbean.getGBeanInfo() == null) {
                        message.append("a GBeanInfo ");
                        sep = "and ";
                    }
                    if (!(name instanceof AbstractName)) {
                        message.append(sep).append("a full AbstractName ");
                        sep = "and ";
                    }
                    message.append("configuration=").append(configName);
                    message.append(" gbeanName=").append(name);
                    throw new InvalidConfigException(message.toString());
                }
                GBeanInfo gbeanInfo = infoFactory.getGBeanInfo(gbean.getGBeanInfo(), bundle);
                AbstractName abstractName = (AbstractName) name;
                GBeanData gBeanData = new GBeanData(abstractName, gbeanInfo);
                gbeanDatas.add(gBeanData);
            }
        }

        // set the attributes
        for (Iterator<GBeanData> iterator = gbeanDatas.iterator(); iterator.hasNext();) {
            GBeanData data = iterator.next();
            boolean load = setAttributes(data, configuration, configName, bundle);
            if (!load) {
                iterator.remove();
            }
        }
        return gbeanDatas;
    }
/*
    public synchronized Collection<GBeanData> applyOverrides(Artifact configName, Collection<GBeanData> untypedGbeanDatas, Map<String, Bundle> bundleMap) throws InvalidConfigException {
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
                if (gbean.getGBeanInfo() == null || !(name instanceof AbstractName) || gbean.getBundleSymbolicName() == null) {
                    String sep = "";
                    StringBuilder message = new StringBuilder("New GBeans must be specified with ");
                    if (gbean.getGBeanInfo() == null) {
                        message.append("a GBeanInfo ");
                        sep = "and ";
                    }
                    if (!(name instanceof AbstractName)) {
                        message.append(sep).append("a full AbstractName ");
                        sep = "and ";
                    }
                    if (gbean.getBundleSymbolicName() == null) {
                        message.append(sep).append("a bundleSymbolicName ");
                    }
                    message.append("configuration=").append(configName);
                    message.append(" gbeanName=").append(name);
                    throw new InvalidConfigException(message.toString());
                }
                Bundle bundle = bundleMap.get(gbean.getBundleSymbolicName());
                GBeanInfo gbeanInfo = infoFactory.getGBeanInfo(gbean.getGBeanInfo(), bundle);
                AbstractName abstractName = (AbstractName) name;
                GBeanData gBeanData = new GBeanData(abstractName, gbeanInfo);
                gbeanDatas.add(gBeanData);
            }
        }

        // set the attributes
        for (Iterator<GBeanData> iterator = gbeanDatas.iterator(); iterator.hasNext();) {
            GBeanData data = iterator.next();
            String bundleSymbolicName = data.getGBeanInfo().getBundleSymbolicName();
            Bundle bundle = bundleMap.get(bundleSymbolicName);
            boolean load = setAttributes(data, configuration, configName, bundle);
            if (!load) {
                iterator.remove();
            }
        }
        return gbeanDatas;
    }
*/

    /**
     * Set the attributes from the attribute store on a single gbean, and return whether or not to load the gbean.
     *
     * @param data          GBeanData we are going to override attributes on
     * @param configuration the module override the gbean relates to
     * @param configName    name of the module (why can't this be determined from the configuration?)
     * @param bundle
     * @return true if the gbean should be loaded, false otherwise.
     * @throws org.apache.geronimo.kernel.config.InvalidConfigException
     *          if we cannot update the gbeanData
     */
    private synchronized boolean setAttributes(GBeanData data, ConfigurationOverride configuration, Artifact configName, Bundle bundle) throws InvalidConfigException {
        AbstractName gbeanName = data.getAbstractName();
        GBeanOverride gbean = configuration.getGBean(gbeanName);
        if (gbean == null) {
            gbean = configuration.getGBean((String) gbeanName.getName().get("name"));
        }

        if (gbean == null) {
            //no attr info, load by default
            return true;
        }

        return gbean.applyOverrides(data, configName, gbeanName, bundle);
    }

    public synchronized void setModuleGBeans(Artifact moduleName, List<GbeanType> gbeans, boolean load, String condition) throws InvalidGBeanException {
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

    public boolean isModuleInstalled(Artifact artifact) {
        return serverOverride.getConfiguration(artifact) != null;
    }

    public String substitute(String in) {
        return expressionParser.parse(in);
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    public void addConfigSubstitutions(java.util.Properties properties) {
        for (Object key : properties.keySet()) {
            String keyStr = (String) key;
            localConfigSubstitutions.put(keyStr, properties.getProperty(keyStr));
        }
        Map<String, Object> configSubstutions = loadAllConfigSubstitutions(localConfigSubstitutions, prefix);
        storeConfigSubstitutions(configSubstitutionsFile, localConfigSubstitutions);
        expressionParser = new JexlExpressionParser(configSubstutions);
    }


    public synchronized void addConfigSubstitutions(Map<String, String> properties) {
        localConfigSubstitutions.putAll(properties);
        Map<String, Object> configSubstitutions = loadAllConfigSubstitutions(localConfigSubstitutions, prefix);
        //TODO which is correct?
        storeConfigSubstitutions(configSubstitutionsFile, localConfigSubstitutions);
        expressionParser = new JexlExpressionParser(configSubstitutions);
        //or this?
//        expressionParser = new JexlExpressionParser(configSubstitutions);
//        attributeChanged();
    }

    public synchronized void setValue(Artifact configurationName, AbstractName gbeanName, GAttributeInfo attribute, Object value, Bundle bundle) {
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
            gbean.setAttribute(attribute, value, bundle);
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

    public void addGBean(Artifact configurationName, GBeanData gbeanData, Bundle bundle) {
        if (readOnly) {
            return;
        }
        ConfigurationOverride configuration = serverOverride.getConfiguration(configurationName);
        if (configuration == null) {
            log.debug("Can not add GBean; Configuration not found {}", configurationName);
            return;
        }
        try {
            GBeanOverride gbean = new GBeanOverride(gbeanData, expressionParser, bundle);
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

    void write(Writer writer) throws XMLStreamException, JAXBException,
            IOException {
        AttributesType attributes = serverOverride.writeXml();
        AttributesXmlUtil.writeAttributes(attributes, writer);
        writer.flush();
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
        
        // we don't need to write wab configuration to config.xml
        for (Iterator<ModuleType> it = attributes.getModule().iterator(); it.hasNext();) {
            ModuleType module = it.next();
            if (module.getName().endsWith("wab")) {
                it.remove();
            }
        }
        
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
                //write out empty file with instructions as a hint to users.
                storeConfigSubstitutions(configSubstitutionsFile, properties);
            } else {
                try {
                    FileInputStream in = new FileInputStream(configSubstitutionsFile);
                    try {
                        properties.load(in);
                    } finally {
                        in.close();
            }
                } catch (Exception e) {
                    log.error("Caught exception {} trying to read properties file {}", e, configSubstitutionsFile.getAbsolutePath());
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
                    out.write(INSTRUCTION);
                    properties.save(out);
                } finally {
                    out.close();
                }
            } catch (Exception e) {
                log.error("Caught exception {} trying to write properties file {}", e, configSubstitutionsFile.getAbsolutePath());
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
                        vars.put(key, entry.getValue());
                    }
                }
            }
        }
    }

}
