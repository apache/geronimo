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

package org.apache.geronimo.deployment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.IllegalConfigurationException;
import org.apache.geronimo.deployment.util.osgi.DummyExportPackagesSelector;
import org.apache.geronimo.deployment.util.osgi.OSGiMetaDataBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.Manifest;
import org.apache.geronimo.kernel.config.ManifestException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.plugin.model.ArtifactType;
import org.apache.geronimo.system.plugin.model.DependencyType;
import org.apache.geronimo.system.plugin.model.PluginArtifactType;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.apache.geronimo.system.plugin.model.PluginXmlUtil;
import org.apache.xbean.osgi.bundle.util.BundleUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev:385232 $ $Date$
 */
public class DeploymentContext {

    private static final Logger log = LoggerFactory.getLogger(DeploymentContext.class);

    protected final File baseDir;
    protected final File inPlaceConfigurationDir;
    protected final ResourceContext resourceContext;
    protected final Map<String, ConfigurationData> childConfigurationDatas = new LinkedHashMap<String, ConfigurationData>();
    protected final ConfigurationManager configurationManager;
    protected final Naming naming;
    protected final List<ConfigurationData> additionalDeployment = new ArrayList<ConfigurationData>();
    protected final AbstractName moduleName;
    protected final LinkedHashSet<String> bundleClassPath = new LinkedHashSet<String>();
    protected final ConfigurationModuleType moduleType;
    protected final Environment environment;
    //This provides services such as loading more bundles, it is NOT for the configuration we are constructing here.
    //It should be a disposable nested framework so as to not pollute the main framework with stuff we load as deployment parents.
    private final BundleContext bundleContext;
    protected Configuration configuration;
    private Bundle tempBundle;


    public DeploymentContext(File baseDir,
                             File inPlaceConfigurationDir,
                             Environment environment,
                             AbstractName moduleName,
                             ConfigurationModuleType moduleType,
                             Naming naming,
                             ConfigurationManager configurationManager,
                             Collection<Repository> repositories,
                             BundleContext bundleContext) throws DeploymentException {
        this(baseDir, inPlaceConfigurationDir, environment, moduleName, moduleType, naming,
             createConfigurationManager(configurationManager, repositories, bundleContext), bundleContext);
    }

    public DeploymentContext(File baseDir,
                             File inPlaceConfigurationDir,
                             Environment environment,
                             AbstractName moduleName,
                             ConfigurationModuleType moduleType,
                             Naming naming,
                             ConfigurationManager configurationManager,
                             BundleContext bundleContext) throws DeploymentException {
        if (environment == null) throw new NullPointerException("environment is null");
        if (moduleType == null) throw new NullPointerException("type is null");
        if (configurationManager == null) throw new NullPointerException("configurationManager is null");
        if (baseDir == null) throw new NullPointerException("baseDir is null");
        if (!baseDir.exists() && !baseDir.mkdirs()) {
            throw new DeploymentException("Could not create directory for deployment context assembly: " + baseDir);
        }
        if (bundleContext == null) {
            throw new NullPointerException("no bundle context");
        }

        this.baseDir = baseDir;
        this.inPlaceConfigurationDir = inPlaceConfigurationDir;
        this.moduleName = moduleName;
        this.naming = naming;
        this.moduleType = moduleType;
        this.environment = environment;
        this.configurationManager = createConfigurationManager(configurationManager, Collections.<Repository> emptyList(), bundleContext);
        this.bundleContext = bundleContext;

        if (null == inPlaceConfigurationDir) {
            this.resourceContext = new CopyResourceContext(this, baseDir);
        } else {
            this.resourceContext = new InPlaceResourceContext(this, inPlaceConfigurationDir);
        }
    }

    // For sub-classes only
    protected DeploymentContext(File baseDir,
                                File inPlaceConfigurationDir,
                                Environment environment,
                                AbstractName moduleName,
                                ConfigurationModuleType moduleType,
                                Naming naming,
                                ConfigurationManager configurationManager,
                                ResourceContext resourceContext,
                                BundleContext bundleContext) throws DeploymentException {
        if (bundleContext == null) {
            throw new NullPointerException("no bundle context");
        }
        this.baseDir = baseDir;
        this.inPlaceConfigurationDir = inPlaceConfigurationDir;
        this.moduleName = moduleName;
        this.naming = naming;
        this.moduleType = moduleType;
        this.environment = environment;
        this.configurationManager = createConfigurationManager(configurationManager, Collections.<Repository> emptyList(), bundleContext);
        this.resourceContext = resourceContext;
        this.bundleContext = bundleContext;
    }

    private static ConfigurationManager createConfigurationManager(ConfigurationManager configurationManager, Collection<Repository> repositories, BundleContext bundleContext) {
        if (configurationManager instanceof DeploymentConfigurationManager) {
            return configurationManager;
        }
        return new DeploymentConfigurationManager(configurationManager, repositories, bundleContext);
    }

    /**
     * call to set up the (empty) Configuration we will use to store the gbeans we add to the context. It will use a temporary BundleContext/Bundle for classloading.
     * @throws DeploymentException if configuration cannot be loaded successfully
     */
    public void initializeConfiguration() throws DeploymentException {
        this.configuration = createTempConfiguration();
    }

    private Configuration createTempConfiguration() throws DeploymentException {
        LinkedHashSet<Artifact> resolvedParentIds = null;
        try {
            ConfigurationData configurationData = new ConfigurationData(moduleType, null, childConfigurationDatas, environment, baseDir, inPlaceConfigurationDir, naming);
            createTempManifest();
            createPluginMetadata();
            String location = "reference:" + getConfigurationDir().toURI().toURL();
            tempBundle = bundleContext.installBundle(location);
            if (BundleUtils.canStart(tempBundle)) {
                tempBundle.start(Bundle.START_TRANSIENT);
            }
            configurationData.setBundleContext(tempBundle.getBundleContext());
            configurationManager.loadConfiguration(configurationData);
            return configurationManager.getConfiguration(environment.getConfigId());
        } catch (Exception e) {
            throw new DeploymentException("Unable to create configuration for deployment: dependencies: " + resolvedParentIds, e);
        }
    }

    private void createPluginMetadata() throws IOException, JAXBException, XMLStreamException {
        PluginType pluginType = getPluginMetadata();
        File metaInf = new File(getConfigurationDir(), "META-INF");
        metaInf.mkdirs();
        OutputStream out = new FileOutputStream(new File(metaInf, "geronimo-plugin.xml"));
        try {
            PluginXmlUtil.writePluginMetadata(pluginType, out);
        } finally {
            out.close();
        }
    }

    public PluginType getPluginMetadata() {
        PluginType pluginType = new PluginType();
        pluginType.setName("Temporary Plugin metadata for deployment");
        PluginArtifactType instance = new PluginArtifactType();
        instance.setModuleId(ArtifactType.newArtifactType(environment.getConfigId()));
        List<DependencyType> dependenciees = instance.getDependency();
        for (Dependency dependency: environment.getDependencies()) {
            dependenciees.add(DependencyType.newDependencyType(dependency));
        }
        pluginType.getPluginArtifact().add(instance);
        return pluginType;
    }

    private void createTempManifest() throws DeploymentException, IOException {
        Environment env = new Environment(environment);
        Artifact id = env.getConfigId();
        env.setConfigId(new Artifact(id.getGroupId(), id.getArtifactId() + "-DEPLOYMENT", id.getVersion(), id.getType()));
        env.addToBundleClassPath(bundleClassPath);
        env.setBundleActivator(null);
        env.addDynamicImportPackage("*");

        OSGiMetaDataBuilder osgiMetaDataBuilder = new OSGiMetaDataBuilder(bundleContext, new DummyExportPackagesSelector());
        try {
            osgiMetaDataBuilder.build(env);
        } catch (IllegalConfigurationException e) {
            throw new DeploymentException(e);
        }
        Manifest manifest;
        try {
            manifest = env.getManifest();
        } catch (ManifestException e) {
            throw new DeploymentException(e);
        }

        File metaInf = new File(getConfigurationDir(), "META-INF");
        metaInf.mkdirs();
        FileWriter fw = new FileWriter(new File(metaInf, "MANIFEST.MF"));
        PrintWriter pw = new PrintWriter(fw);
        try {
            manifest.write(pw);
        } finally {
            pw.close();
            fw.close();
        }
    }

    private File getConfigurationDir() {
        return (inPlaceConfigurationDir == null) ? baseDir : inPlaceConfigurationDir;
    }

    public ConfigurationManager getConfigurationManager() {
        return configurationManager;
    }

    public Artifact getConfigID() {
        return environment.getConfigId();
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getInPlaceConfigurationDir() {
        return inPlaceConfigurationDir;
    }

    public LinkedHashSet<String> getBundleClassPath() {
        return new LinkedHashSet<String>(bundleClassPath);
    }

    public Naming getNaming() {
        return naming;
    }

    public GBeanData addGBean(String name, GBeanInfo gbeanInfo) throws GBeanAlreadyExistsException {
        if (name == null) throw new NullPointerException("name is null");
        if (gbeanInfo == null) throw new NullPointerException("gbean is null");
        GBeanData gbean = new GBeanData(gbeanInfo);
        configuration.addGBean(name, gbean);
        return gbean;
    }

    public void addGBean(GBeanData gbean) throws GBeanAlreadyExistsException {
        if (gbean == null) throw new NullPointerException("gbean is null");
        if (gbean.getAbstractName() == null) throw new NullPointerException("gbean.getAbstractName() is null");
        configuration.addGBean(gbean);
    }

    public void removeGBean(AbstractName name) throws GBeanNotFoundException {
        if (name == null) throw new NullPointerException("name is null");
        configuration.removeGBean(name);
    }

    public Set<AbstractName> getGBeanNames() {
        return new HashSet<AbstractName>(configuration.getGBeans().keySet());
    }

    /**
     * @param pattern Search for gbeans whose name matches pattern.
     * @return the set of gbeans whose name matches the pattern.
     * @deprecated use findGBeans(pattern)
     */
    public Set<AbstractName> listGBeans(AbstractNameQuery pattern) {
        return findGBeans(pattern);
    }

    public AbstractName findGBean(AbstractNameQuery pattern) throws GBeanNotFoundException {
        return configuration.findGBean(pattern);
    }

    public AbstractName findGBean(Set<AbstractNameQuery> patterns) throws GBeanNotFoundException {
        return configuration.findGBean(patterns);
    }

    public LinkedHashSet<AbstractName> findGBeans(AbstractNameQuery pattern) {
        return configuration.findGBeans(pattern);
    }

    public LinkedHashSet<GBeanData> findGBeanDatas(Set<AbstractNameQuery> patterns) {
        return configuration.findGBeanDatas(patterns);
    }

    public LinkedHashSet<GBeanData> findGBeanDatas(Configuration configuration, AbstractNameQuery pattern) {
        return configuration.findGBeanDatas(configuration, Collections.singleton(pattern));
    }

    public LinkedHashSet<AbstractName> findGBeans(Set<AbstractNameQuery> patterns) {
        return configuration.findGBeans(patterns);
    }

    public GBeanData getGBeanInstance(AbstractName name) throws GBeanNotFoundException {
        Map<AbstractName, GBeanData> gbeans = configuration.getGBeans();
        GBeanData gbeanData = gbeans.get(name);
        if (gbeanData == null) {
            throw new GBeanNotFoundException(name);
        }
        return gbeanData;
    }

    /**
     * Add a packed jar file into the deployment context and place it into the
     * path specified in the target path.  The newly added packed jar is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the packed jar file should be placed
     * @param jarFile    the jar file to copy
     * @throws IOException if there's a problem copying the jar file
     */
    public void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException {
        resourceContext.addIncludeAsPackedJar(targetPath, jarFile);
    }

    /**
     * Add a ZIP file entry into the deployment context and place it into the
     * path specified in the target path.  The newly added entry is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the ZIP file entry should be placed
     * @param zipFile    the ZIP file
     * @param zipEntry   the ZIP file entry
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        resourceContext.addInclude(targetPath, zipFile, zipEntry);
    }

    /**
     * Add a file into the deployment context and place it into the
     * path specified in the target path.  The newly added file is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the file should be placed
     * @param source     the URL of file to be copied
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, URL source) throws IOException {
        resourceContext.addInclude(targetPath, source);
    }

    /**
     * Add a file into the deployment context and place it into the
     * path specified in the target path.  The newly added file is added
     * to the classpath of the configuration.
     *
     * @param targetPath where the file should be placed
     * @param source     the file to be copied
     * @throws IOException if there's a problem copying the ZIP entry
     */
    public void addInclude(URI targetPath, File source) throws IOException {
        resourceContext.addInclude(targetPath, source);
    }

    private class DefaultJarFileFactory implements ClassPathUtils.JarFileFactory {

        public JarFile newJarFile(URI relativeURI) throws IOException {
            File targetFile = getTargetFile(relativeURI);
            try {
                return new JarFile(targetFile);
            } catch (IOException e) {
                throw (IOException) new IOException("Could not create JarFile for file: " + targetFile).initCause(e);
            }
        }

        public String getManifestClassPath(JarFile jarFile) throws IOException {
            java.util.jar.Manifest manifest = jarFile.getManifest();
            if (manifest == null) {
                return null;
            }
            return manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        }

        public boolean isDirectory(URI relativeURI) {
            File targetFile = getTargetFile(relativeURI);
            return targetFile.isDirectory();
        }

        public File[] listFiles(URI relativeURI) throws IOException {
            File targetFile = getTargetFile(relativeURI);
            if (targetFile.isDirectory()) {
                File[] files = targetFile.listFiles();
                return (files == null) ? new File[0] : files;
            } else {
                throw new IOException(targetFile + " is not a directory");
            }
        }
    }

    public void getCompleteManifestClassPath(Deployable deployable, URI moduleBaseUri, URI resolutionUri, Collection<String> classpath, Collection<String> exclusions) throws DeploymentException {
        if (!(deployable instanceof DeployableJarFile)) {
            throw new IllegalArgumentException("Expected DeployableJarFile");
        }
        JarFile moduleFile = ((DeployableJarFile) deployable).getJarFile();
        ClassPathUtils.getCompleteManifestClassPath(moduleFile, moduleBaseUri, resolutionUri, classpath, exclusions, new DefaultJarFileFactory());
    }

    public void addManifestClassPath(JarFile moduleFile, URI moduleBaseUri, Collection<String> manifestClasspath) throws DeploymentException {
        ClassPathUtils.addManifestClassPath(moduleFile, moduleBaseUri, manifestClasspath, new DefaultJarFileFactory());
    }

    public void addToClassPath(String target) {
        bundleClassPath.add(target);
    }

    public void addFile(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        resourceContext.addFile(targetPath, zipFile, zipEntry);
    }

    public void addFile(URI targetPath, URL source) throws IOException {
        resourceContext.addFile(targetPath, source);
    }

    public void addFile(URI targetPath, File source) throws IOException {
        resourceContext.addFile(targetPath, source);
    }

    public void addFile(URI targetPath, String source) throws IOException {
        resourceContext.addFile(targetPath, source);
    }

    public File getTargetFile(URI targetPath) {
        return resourceContext.getTargetFile(targetPath);
    }

    public URL getTargetURL(URI targetPath) {
        return resourceContext.getTargetURL(targetPath);
    }

    public Bundle getDeploymentBundle() throws DeploymentException {
        return configuration.getBundle();
    }

    public ConfigurationModuleType getModuleType() {
        return moduleType;
    }

    public Environment getEnvironment() {
        return environment;
    }

    protected BundleContext getBundleContext() {
        return bundleContext;
    }

    public Configuration getConfiguration() {
        if (configuration == null) throw new IllegalStateException("configuration not initialized");
        return configuration;
    }

    public void flush() throws IOException {
        resourceContext.flush();
    }

    public void close() throws IOException, DeploymentException {
        if (configurationManager != null && configuration != null) {
            try {
                configurationManager.unloadConfiguration(configuration.getId());
            } catch (Exception ignored) {
            }
        }
        if (tempBundle != null) {
            try {
                tempBundle.uninstall();
            } catch (BundleException e) {
            }
        }
    }

    public void addChildConfiguration(String moduleName, ConfigurationData configurationData) {
        childConfigurationDatas.put(moduleName, configurationData);
    }

    /**
     * Extract the completed ConfigurationData ready for serialization
     * @return final ConfigurationData
     * @throws DeploymentException if configuration is invalid
     */
    public ConfigurationData getConfigurationData() throws DeploymentException {
        List<String> failures = verify(configuration);
        if (!failures.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (String failure : failures) {
                if (message.length() > 0) message.append("\n");
                message.append(failure);
            }
            throw new DeploymentException(message.toString());
        }

        // TODO OSGI figure out exports
        environment.addToBundleClassPath(bundleClassPath);

        List<GBeanData> gbeans = new ArrayList<GBeanData>(configuration.getGBeans().values());
        Collections.sort(gbeans, new GBeanData.PriorityComparator());

        OSGiMetaDataBuilder osgiMetaDataBuilder = null;
        //TODO Import package calculation is only used for deployed applications, should be use the same way for car package later
        if (System.getProperty("geronimo.build.car") == null) {
            osgiMetaDataBuilder = new OSGiMetaDataBuilder(bundleContext);
            //Hack Codes Here For RAR module, will remove while the connector refactoring is done
            if (configuration.getModuleType() == ConfigurationModuleType.RAR) {
                environment.addDynamicImportPackage("*");
            }
        } else {
            LinkedHashSet<String> imports = getImports(gbeans);
            if (environment.getBundleActivator() != null) {
                addImport(imports, environment.getBundleActivator());
            }
            environment.addImportPackages(imports);
            environment.addDynamicImportPackage("*");
            osgiMetaDataBuilder = new OSGiMetaDataBuilder(bundleContext, new DummyExportPackagesSelector());
        }

        try {
            osgiMetaDataBuilder.build(environment, configuration.getModuleType() == ConfigurationModuleType.CAR);
        } catch (IllegalConfigurationException e) {
            throw new DeploymentException(e);
        }

        if (tempBundle != null) {
            try {
                createPluginMetadata();
            } catch (Exception e) {
                throw new DeploymentException("Failed to update geronimo-plugin.xml", e);
            }
        }

        ConfigurationData configurationData = new ConfigurationData(configuration.getModuleType(),
                gbeans,
                childConfigurationDatas,
                configuration.getEnvironment(),
                baseDir,
                inPlaceConfigurationDir,
                naming
        );

        for (ConfigurationData ownedConfiguration : additionalDeployment) {
            configurationData.addOwnedConfigurations(ownedConfiguration.getId());
        }

        return configurationData;
    }

    public static LinkedHashSet<String> getImports(List<GBeanData> gbeans) {
        LinkedHashSet<String> imports = new LinkedHashSet<String>();
        for (GBeanData data: gbeans) {
            GBeanInfo info = data.getGBeanInfo();
            addImport(imports, info.getClassName());
            for (GAttributeInfo attInfo: info.getAttributes()) {
                addImport(imports, attInfo.getType());
            }
            for (GReferenceInfo refInfo: info.getReferences()) {
                addImport(imports, refInfo.getReferenceType());
            }
            for (GOperationInfo opInfo: info.getOperations()) {
                addImport(imports, opInfo.getReturnType());
                for (String paramType : opInfo.getParameterList()) {
                    addImport(imports, paramType);
                }
            }
        }
        return imports;
    }

    private static void addImport(LinkedHashSet<String> imports, String className) {
        String packageName = getImportPackageName(className);
        if (packageName == null || packageName.startsWith("java.")) {
            return;
        }
        imports.add(packageName);
    }

    private static String getImportPackageName(String className) {
        int pos = className.lastIndexOf('.');
        if (pos < 0) {
            return null;
        }
        int count = 0;
        while (className.charAt(count) == '[') {
            count++;
        }
        if (className.charAt(count) == 'L') {
            count++;
        }
        className = className.substring(count, pos);
        return className;
    }

    public void addAdditionalDeployment(ConfigurationData configurationData) {
        additionalDeployment.add(configurationData);
    }

    public List<ConfigurationData> getAdditionalDeployment() {
        return additionalDeployment;
    }

    public AbstractName getModuleName() {
        return moduleName;
    }

    public List<String> verify(Configuration configuration) throws DeploymentException {
        List<String> failures = new ArrayList<String>();
        for (Map.Entry<AbstractName, GBeanData> entry : this.configuration.getGBeans().entrySet()) {
            AbstractName name = entry.getKey();
            GBeanData gbean = entry.getValue();

            for (Map.Entry<String, ReferencePatterns> referenceEntry : gbean.getReferences().entrySet()) {
                String referenceName = referenceEntry.getKey();
                ReferencePatterns referencePatterns = referenceEntry.getValue();

                String failure = verifyReference(gbean, referenceName, referencePatterns, configuration);
                if (failure != null) {
                    failures.add(failure);
                }
            }

            for (ReferencePatterns referencePatterns : gbean.getDependencies()) {
                String failure = verifyDependency(name, referencePatterns, configuration);
                if (failure != null) {
                    failures.add(failure);
                }
            }
        }
        return failures;
    }

    private String verifyReference(GBeanData gbean, String referenceName, ReferencePatterns referencePatterns, Configuration configuration) {
        GReferenceInfo referenceInfo = gbean.getGBeanInfo().getReference(referenceName);

        // if there is no reference info we can't verify
        if (referenceInfo == null) return null;

        // A collection valued reference doesn't need to be verified
        if (referenceInfo.getProxyType().equals(Collection.class.getName())) return null;

        String message = isVerifyReference(referencePatterns, configuration);
        if (message != null) {
            return "Unable to resolve reference \"" + referenceName + "\"\n" +
                    "    in gbean " + gbean.getAbstractName() + "\n    to a gbean matching the pattern " + referencePatterns.getPatterns() + "\n    due to: " + message;
        }
        return null;
    }

    private String verifyDependency(AbstractName name, ReferencePatterns referencePatterns, Configuration configuration) {
        String message = isVerifyReference(referencePatterns, configuration);
        if (message != null) {
            return "Unable to resolve dependency in gbean " + name +
                    "\n    to a gbean matching the pattern " + referencePatterns.getPatterns() + "\n    due to: " + message;
        }

        return null;
    }

    private String isVerifyReference(ReferencePatterns referencePatterns, Configuration configuration) {
        // we can't verify a resolved reference since it will have a specific artifact already set...
        // hopefully the deployer won't generate bad resolved references
        if (referencePatterns.isResolved()) return null;

        // Do not verify the reference if it has an explicit depenency on another artifact, because it it likely
        // that the other artifact is not in the "environment" (if it were you wouldn't use the long form)
        Set<AbstractNameQuery> patterns = referencePatterns.getPatterns();
        for (AbstractNameQuery query : patterns) {
            if (query.getArtifact() != null) return null;
        }

        // attempt to find the bean
        try {
            configuration.findGBean(patterns);
            return null;
        } catch (GBeanNotFoundException e) {
            //TODO bug!! GERONIMO-3140 Multiple matches may be caused by using an already-loaded configuration rather than reloading one
            // using the client_artifact_aliases.properties which e.g. remap the server tm to the client tm.
            if (e.hasMatches()) {
                return null;
            }
            return e.getMessage();
        }
    }
}
