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

package org.apache.geronimo.j2ee.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.Deployable;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.system.plugin.model.PluginType;
import org.osgi.framework.Bundle;

/**
 * FragmentContext is a simple wrapper for the EARContext, it delegates most of the requests to its host EARContext, and it does not have initialize/close actions
 * As now, all the modules (except for client module) share the same EARContext, which brings issues for the general data for each module.
 * @version $Rev$ $Date$
 */
public class FragmentContext extends EARContext {

    private EARContext hostContext;

    public FragmentContext(EARContext hostContext, ConfigurationModuleType configurationModuleType) throws DeploymentException {
        super(hostContext.getBaseDir(), hostContext.getInPlaceConfigurationDir(), hostContext.getEnvironment(), hostContext.getModuleType(), hostContext.getModuleName(), hostContext);
        this.hostContext = hostContext;
    }

    @Override
    public AbstractNameQuery getServerName() {
        return hostContext.getServerName();
    }

    @Override
    public AbstractNameQuery getTransactionManagerName() {
        return hostContext.getTransactionManagerName();
    }

    @Override
    public AbstractNameQuery getConnectionTrackerName() {
        return hostContext.getConnectionTrackerName();
    }

    @Override
    public AbstractNameQuery getCORBAGBeanName() {
        return hostContext.getCORBAGBeanName();
    }

    @Override
    public Map getContextIDToPermissionsMap() {
        return hostContext.getContextIDToPermissionsMap();
    }

    @Override
    public void addSecurityContext(String contextID, Object componentPermissions) throws DeploymentException {
        hostContext.addSecurityContext(contextID, componentPermissions);
    }

    @Override
    public void setSecurityConfiguration(Object securityConfiguration) throws DeploymentException {
        hostContext.setSecurityConfiguration(securityConfiguration);
    }

    @Override
    public Object getSecurityConfiguration() {
        return hostContext.getSecurityConfiguration();
    }

    @Override
    public void registerMessageDestionations(String moduleName, Map nameMap) throws DeploymentException {
        hostContext.registerMessageDestionations(moduleName, nameMap);
    }

    @Override
    public Map getMessageDestinations() {
        return hostContext.getMessageDestinations();
    }

    @Override
    public boolean isHasSecurity() {
        return hostContext.isHasSecurity();
    }

    @Override
    public void setHasSecurity(boolean hasSecurity) {
        hostContext.setHasSecurity(hasSecurity);
    }

    @Override
    public Set<String> getSubModuleNames() {
        return hostContext.getSubModuleNames();
    }

    @Override
    public void addAdditionalDeployment(ConfigurationData configurationData) {
        hostContext.addAdditionalDeployment(configurationData);
    }

    @Override
    public void addChildConfiguration(String moduleName, ConfigurationData configurationData) {
        hostContext.addChildConfiguration(moduleName, configurationData);
    }

    @Override
    public void addFile(URI targetPath, URL source) throws IOException {
        hostContext.addFile(targetPath, source);
    }

    @Override
    public void addFile(URI targetPath, File source) throws IOException {
        hostContext.addFile(targetPath, source);
    }

    @Override
    public void addFile(URI targetPath, String source) throws IOException {
        hostContext.addFile(targetPath, source);
    }

    @Override
    public void addFile(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        hostContext.addFile(targetPath, zipFile, zipEntry);
    }

    @Override
    public void addGBean(GBeanData gbean) throws GBeanAlreadyExistsException {
        hostContext.addGBean(gbean);
    }

    @Override
    public GBeanData addGBean(String name, GBeanInfo gbeanInfo) throws GBeanAlreadyExistsException {
        return hostContext.addGBean(name, gbeanInfo);
    }

    @Override
    public void addInclude(URI targetPath, URL source) throws IOException {
        hostContext.addInclude(targetPath, source);
    }

    @Override
    public void addInclude(URI targetPath, File source) throws IOException {
        hostContext.addInclude(targetPath, source);
    }

    @Override
    public void addInclude(URI targetPath, ZipFile zipFile, ZipEntry zipEntry) throws IOException {
        hostContext.addInclude(targetPath, zipFile, zipEntry);
    }

    @Override
    public void addIncludeAsPackedJar(URI targetPath, JarFile jarFile) throws IOException {
        hostContext.addIncludeAsPackedJar(targetPath, jarFile);
    }

    @Override
    public void addManifestClassPath(JarFile moduleFile, URI moduleBaseUri, Collection<String> manifestClasspath) throws DeploymentException {
        hostContext.addManifestClassPath(moduleFile, moduleBaseUri, manifestClasspath);
    }

    @Override
    public void addToClassPath(String target) {
        hostContext.addToClassPath(target);
    }

    @Override
    public void close() throws IOException, DeploymentException {
        //DO nothing
    }

    @Override
    public AbstractName findGBean(AbstractNameQuery pattern) throws GBeanNotFoundException {
        return hostContext.findGBean(pattern);
    }

    @Override
    public AbstractName findGBean(Set<AbstractNameQuery> patterns) throws GBeanNotFoundException {
        return hostContext.findGBean(patterns);
    }

    @Override
    public LinkedHashSet<GBeanData> findGBeanDatas(Set<AbstractNameQuery> patterns) {
        return hostContext.findGBeanDatas(patterns);
    }

    @Override
    public LinkedHashSet<GBeanData> findGBeanDatas(Configuration configuration, AbstractNameQuery pattern) {
        return hostContext.findGBeanDatas(configuration, pattern);
    }

    @Override
    public LinkedHashSet<AbstractName> findGBeans(AbstractNameQuery pattern) {
        return hostContext.findGBeans(pattern);
    }

    @Override
    public LinkedHashSet<AbstractName> findGBeans(Set<AbstractNameQuery> patterns) {
        return hostContext.findGBeans(patterns);
    }

    @Override
    public void flush() throws IOException {
        hostContext.flush();
    }

    @Override
    public List<ConfigurationData> getAdditionalDeployment() {
        return hostContext.getAdditionalDeployment();
    }

    @Override
    public File getBaseDir() {
        return hostContext.getBaseDir();
    }

    @Override
    public LinkedHashSet<String> getBundleClassPath() {

        return hostContext.getBundleClassPath();
    }

    @Override
    public void getCompleteManifestClassPath(Deployable deployable, URI moduleBaseUri, URI resolutionUri, Collection<String> classpath, Collection<String> exclusions) throws DeploymentException {
        hostContext.getCompleteManifestClassPath(deployable, moduleBaseUri, resolutionUri, classpath, exclusions);
    }

    @Override
    public Artifact getConfigID() {
        return hostContext.getConfigID();
    }

    @Override
    public Configuration getConfiguration() {
        return hostContext.getConfiguration();
    }

    @Override
    public ConfigurationData getConfigurationData() throws DeploymentException {
        return hostContext.getConfigurationData();
    }

    @Override
    public ConfigurationManager getConfigurationManager() {
        return hostContext.getConfigurationManager();
    }

    @Override
    public Bundle getDeploymentBundle() throws DeploymentException {
        return hostContext.getDeploymentBundle();
    }

    @Override
    public GBeanData getGBeanInstance(AbstractName name) throws GBeanNotFoundException {
        return hostContext.getGBeanInstance(name);
    }

    @Override
    public Set<AbstractName> getGBeanNames() {
        return hostContext.getGBeanNames();
    }

    @Override
    public File getInPlaceConfigurationDir() {
        return hostContext.getInPlaceConfigurationDir();
    }

    @Override
    public AbstractName getModuleName() {
        return hostContext.getModuleName();
    }

    @Override
    public Naming getNaming() {
        return hostContext.getNaming();
    }

    @Override
    public PluginType getPluginMetadata() {
        return hostContext.getPluginMetadata();
    }

    @Override
    public File getTargetFile(URI targetPath) {
        return hostContext.getTargetFile(targetPath);
    }

    @Override
    public URL getTargetURL(URI targetPath) {
        return hostContext.getTargetURL(targetPath);
    }

    @Override
    public void initializeConfiguration() throws DeploymentException {
        //DO Nothing
    }

    @Override
    public Set<AbstractName> listGBeans(AbstractNameQuery pattern) {
        return hostContext.listGBeans(pattern);
    }

    @Override
    public void removeGBean(AbstractName name) throws GBeanNotFoundException {
        hostContext.removeGBean(name);
    }

    @Override
    public List<String> verify(Configuration arg0) throws DeploymentException {
        return hostContext.verify(arg0);
    }
}
