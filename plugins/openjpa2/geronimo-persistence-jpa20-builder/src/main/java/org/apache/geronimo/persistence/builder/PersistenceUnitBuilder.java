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
package org.apache.geronimo.persistence.builder;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeployableBundle;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.ResourceSource;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.jee.Persistence;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xbean.osgi.bundle.util.DiscoveryRange;
import org.apache.xbean.osgi.bundle.util.ResourceDiscoveryFilter;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class PersistenceUnitBuilder implements ModuleBuilderExtension {

    private static final QName PERSISTENCE_QNAME = new QName("http://java.sun.com/xml/ns/persistence", "persistence");

    private final Environment defaultEnvironment;
    private final String defaultPersistenceProviderClassName;
    private final Properties defaultPersistenceUnitProperties;
    private final AbstractNameQuery defaultJtaDataSourceName;
    private final AbstractNameQuery defaultNonJtaDataSourceName;
    private final AbstractNameQuery extendedEntityManagerRegistryName;
    private static final String ANON_PU_NAME = "AnonymousPersistenceUnit";
    private static final String RESOURCE_SOURCE_CLASS_NAME = ResourceSource.class.getName();
    private final PackageAdmin packageAdmin;

    public PersistenceUnitBuilder(@ParamAttribute(name = "defaultEnvironment") Environment defaultEnvironment,
                                  @ParamAttribute(name = "defaultPersistenceProviderClassName") String defaultPersistenceProviderClassName,
                                  @ParamAttribute(name = "defaultJtaDataSourceName") String defaultJtaDataSourceName,
                                  @ParamAttribute(name = "defaultNonJtaDataSourceName") String defaultNonJtaDataSourceName,
                                  @ParamAttribute(name = "extendedEntityManagerRegistryName") AbstractNameQuery extendedEntityManagerRegistryName,
                                  @ParamAttribute(name = "defaultPersistenceUnitProperties") Properties defaultPersistenceUnitProperties,
                                  @ParamSpecial(type = SpecialAttributeType.bundleContext) BundleContext bundleContext) throws URISyntaxException {
        this.defaultEnvironment = defaultEnvironment;
        this.defaultPersistenceProviderClassName = defaultPersistenceProviderClassName;
        this.defaultJtaDataSourceName = defaultJtaDataSourceName == null ? null : getAbstractNameQuery(defaultJtaDataSourceName);
        this.defaultNonJtaDataSourceName = defaultNonJtaDataSourceName == null ? null : getAbstractNameQuery(defaultNonJtaDataSourceName);
        this.extendedEntityManagerRegistryName = extendedEntityManagerRegistryName;
        this.defaultPersistenceUnitProperties = defaultPersistenceUnitProperties == null ? new Properties() : defaultPersistenceUnitProperties;
        ServiceReference sr = bundleContext.getServiceReference(PackageAdmin.class.getName());
        packageAdmin = (PackageAdmin) bundleContext.getService(sr);
    }

    public void createModule(Module module, Bundle bundle, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    public void initContext(EARContext earContext, Module module, Bundle bundle) throws DeploymentException {
        // don't do anything for Bundle-based deployments
        if (module.getDeployable() instanceof DeployableBundle) {
            return;
        }
        XmlObject container = (XmlObject) module.getVendorDD();
        XmlObject[] raws = container.selectChildren(PERSISTENCE_QNAME);

        Map<String, Persistence.PersistenceUnit> overrides = new HashMap<String, Persistence.PersistenceUnit>();
        try {
            for (XmlObject raw : raws) {
             
                Persistence persistence = fromXmlObject(raw);
                for (Persistence.PersistenceUnit unit : persistence.getPersistenceUnit()) {
                    overrides.put(unit.getName().trim(), unit);
                }
            }
        } catch (JAXBException e) {
            throw new DeploymentException("Parse Persistence configuration file failed", e);
        }
        try {

            final Collection<String> manifestcpCopy = new LinkedHashSet<String> ();
            boolean resolveWARcp = false;
            
            // resolve the classpath for non-standalone war file since module.getClassPath 
            // returns the classpath relative to the war file
            if (!module.isStandAlone() && module.getType() == ConfigurationModuleType.WAR) {
                resolveWARcp = true;
            } 
            
            final Collection<String> manifestcp = module.getClassPath();
            for (String classpath : manifestcp) {
                if (resolveWARcp) {
                    manifestcpCopy.add(module.resolve(classpath).toString());                    
                } else {
                    manifestcpCopy.add(classpath);
                }
            }
            
            // add "" into manifestcpCopy to make META-INF/persistence.xml in standalone ejb be processed
            if (module.isStandAlone() && module.getType() == ConfigurationModuleType.EJB) {
                manifestcpCopy.add("");
            }
            
            BundleResourceFinder finder = new BundleResourceFinder(packageAdmin, bundle, "", "META-INF/persistence.xml", new ResourceDiscoveryFilter() {

                @Override
                public boolean rangeDiscoveryRequired(DiscoveryRange discoveryRange) {
                    return discoveryRange == DiscoveryRange.BUNDLE_CLASSPATH || discoveryRange == DiscoveryRange.FRAGMENT_BUNDLES;
                }

                @Override
                public boolean zipFileDiscoveryRequired(String s) {
                    return manifestcpCopy.contains(s);
                }

                @Override
                public boolean directoryDiscoveryRequired(String s) {
                    
                    boolean found = false;
                    if (manifestcpCopy.contains(s)){
                        found=true;
                    } else if(s.endsWith("/") && manifestcpCopy.contains(s.substring(0,s.length()-1))){
                        found=true;
                    }
                    
                    return found;
                }
            });
            final Map<URL, String> persistenceURLs = new HashMap<URL, String>();
            finder.find(new BundleResourceFinder.ResourceFinderCallback() {

                public boolean foundInDirectory(Bundle bundle, String baseDir, URL url) throws Exception {
                    persistenceURLs.put(url, baseDir);
                    return true;
                }

                public boolean foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream inputStream) throws Exception {
                    URL jarURL = bundle.getEntry(jarName);
                    URL url = new URL("jar:" + jarURL.toString() + "!/" + entry.getName());
                    persistenceURLs.put(url, jarName);
                    return true;
                }
            });
            if (raws.length > 0 || persistenceURLs.size() > 0) {
                EnvironmentBuilder.mergeEnvironments(module.getEnvironment(), defaultEnvironment);
            }
            for (Map.Entry<URL, String> entry : persistenceURLs.entrySet()) {
                URL persistenceUrl = entry.getKey();
                String persistenceLocation = entry.getValue();
                Persistence persistence;
                InputStream in = persistenceUrl.openStream();
                try {
                    persistence = (Persistence) JaxbJavaee.unmarshal(Persistence.class, in, false);
                } catch (JAXBException e) {
                    throw new DeploymentException("Could not parse persistence.xml file: " + persistenceUrl, e);
                } finally {
                    in.close();
                }
                buildPersistenceUnits(persistence, overrides, module, persistenceLocation);
            }
        } catch (Exception e) {
            throw new DeploymentException("Could not look for META-INF/persistence.xml files", e);
        }

        for (Persistence.PersistenceUnit persistenceUnit : overrides.values()) {
            GBeanData data = installPersistenceUnitGBean(persistenceUnit, module, module.getTargetPath());
            respectExcludeUnlistedClasses(data);
        }
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
    }

    private void buildPersistenceUnits(Persistence persistence, Map<String, Persistence.PersistenceUnit> overrides, Module module, String persistenceModulePath) throws DeploymentException {
        List<Persistence.PersistenceUnit> persistenceUnits = persistence.getPersistenceUnit();
        for (Persistence.PersistenceUnit persistenceUnit : persistenceUnits) {
            GBeanData data = installPersistenceUnitGBean(persistenceUnit, module, persistenceModulePath);
            String unitName = persistenceUnit.getName().trim();
            if (overrides.get(unitName) != null) {
                setOverrideableProperties(overrides.remove(unitName), data);
            }
            respectExcludeUnlistedClasses(data);
        }
    }

    private GBeanData installPersistenceUnitGBean(Persistence.PersistenceUnit persistenceUnit, Module module, String persistenceModulePath) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        String persistenceUnitName = persistenceUnit.getName().trim();
        if (persistenceUnitName.length() == 0) {
            persistenceUnitName = ANON_PU_NAME;
        }
        AbstractName abstractName;
        if (persistenceModulePath == null || persistenceModulePath.length() == 0) {
            abstractName = moduleContext.getNaming().createChildName(module.getModuleName(), persistenceUnitName, NameFactory.PERSISTENCE_UNIT);
        } else {
            abstractName = moduleContext.getNaming().createChildName(module.getModuleName(), persistenceModulePath, NameFactory.PERSISTENCE_UNIT_MODULE);
            abstractName = moduleContext.getNaming().createChildName(abstractName, moduleContext.getConfigID(), persistenceUnitName, NameFactory.PERSISTENCE_UNIT);
        }
        GBeanData gbeanData = new GBeanData(abstractName, PersistenceUnitGBean.class);
        try {
            moduleContext.addGBean(gbeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Duplicate persistenceUnit name " + persistenceUnitName, e);
        }
        gbeanData.setAttribute("persistenceUnitName", persistenceUnitName);
        gbeanData.setAttribute("persistenceUnitRoot", persistenceModulePath);
        
        //try to start PU GBean firstly to init the transformer before the entity classes get loaded.
        gbeanData.setPriority(GBeanInfo.PRIORITY_CLASSLOADER);

        //set defaults:
        gbeanData.setAttribute("persistenceProviderClassName", defaultPersistenceProviderClassName);
        //spec 6.2.1.2 the default is JTA
        gbeanData.setAttribute("persistenceUnitTransactionType", "JTA");
        if (defaultJtaDataSourceName != null) {
            gbeanData.setReferencePattern("JtaDataSourceWrapper", defaultJtaDataSourceName);
        }
        if (defaultNonJtaDataSourceName != null) {
            gbeanData.setReferencePattern("NonJtaDataSourceWrapper", defaultNonJtaDataSourceName);
        }

        gbeanData.setAttribute("mappingFileNames", new ArrayList<String>());
        gbeanData.setAttribute("excludeUnlistedClasses", false);
        gbeanData.setAttribute("managedClassNames", new ArrayList<String>());
        gbeanData.setAttribute("jarFileUrls", new ArrayList<String>());
        Properties properties = new Properties();
        gbeanData.setAttribute("properties", properties);
        properties.putAll(defaultPersistenceUnitProperties);
        AbstractNameQuery transactionManagerName = moduleContext.getTransactionManagerName();
        gbeanData.setReferencePattern("TransactionManager", transactionManagerName);
        gbeanData.setReferencePattern("EntityManagerRegistry", extendedEntityManagerRegistryName);
        AbstractName validatorName = moduleContext.getNaming().createChildName(module.getModuleName(), "ValidatorFactory", NameFactory.VALIDATOR_FACTORY);
        gbeanData.setReferencePattern("ValidatorFactory", validatorName);

        setOverrideableProperties(persistenceUnit, gbeanData);
        return gbeanData;
    }

    private void setOverrideableProperties(Persistence.PersistenceUnit persistenceUnit, GBeanData gbeanData) throws DeploymentException {
        if (persistenceUnit.getProvider() != null) {
            gbeanData.setAttribute("persistenceProviderClassName", persistenceUnit.getProvider().trim());
        }
        if (persistenceUnit.getTransactionType() != null) {
            gbeanData.setAttribute("persistenceUnitTransactionType", persistenceUnit.getTransactionType());
        }
        if (persistenceUnit.getJtaDataSource() != null) {
            String jtaDataSourceString = persistenceUnit.getJtaDataSource().trim();
            try {
                AbstractNameQuery jtaDataSourceNameQuery = getAbstractNameQuery(jtaDataSourceString);
                gbeanData.setReferencePattern("JtaDataSourceWrapper", jtaDataSourceNameQuery);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not create jta-data-source AbstractNameQuery from string: " + jtaDataSourceString, e);
            }
        }

        if (persistenceUnit.getNonJtaDataSource() != null) {
            String nonJtaDataSourceString = persistenceUnit.getNonJtaDataSource().trim();
            try {
                AbstractNameQuery nonJtaDataSourceNameQuery = getAbstractNameQuery(nonJtaDataSourceString);
                gbeanData.setReferencePattern("NonJtaDataSourceWrapper", nonJtaDataSourceNameQuery);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not create non-jta-data-source AbstractNameQuery from string: " + nonJtaDataSourceString, e);
            }
        }

        List<String> mappingFileNames = (List<String>) gbeanData.getAttribute("mappingFileNames");
        List<String> mappingFileNameStrings = persistenceUnit.getMappingFile();
        for (String mappingFileNameString : mappingFileNameStrings) {
            mappingFileNames.add(mappingFileNameString.trim());
        }

        if (persistenceUnit.isExcludeUnlistedClasses() != null && persistenceUnit.isExcludeUnlistedClasses()) {
            gbeanData.setAttribute("excludeUnlistedClasses", persistenceUnit.isExcludeUnlistedClasses());
        }

        List<String> managedClassNameStrings = persistenceUnit.getClazz();
        List<String> managedClassNames = (List<String>) gbeanData.getAttribute("managedClassNames");
        for (String managedClassNameString : managedClassNameStrings) {
            managedClassNames.add(managedClassNameString.trim());
        }
        List<String> jarFileUrls = (List<String>) gbeanData.getAttribute("jarFileUrls");
        //add the specified locations in the ear
        List<String> jarFileUrlStrings = persistenceUnit.getJarFile();
        for (String jarFileUrlString : jarFileUrlStrings) {
            jarFileUrls.add(jarFileUrlString.trim());
        }

        if (persistenceUnit.getProperties() != null) {
            Properties properties = (Properties) gbeanData.getAttribute("properties");
            for (Persistence.PersistenceUnit.Properties.Property propertyObject : persistenceUnit.getProperties().getProperty()) {
                String key = propertyObject.getName().trim();
                String value = propertyObject.getValue().trim();
                properties.setProperty(key, value);
            }
        }

    }

    private void respectExcludeUnlistedClasses(GBeanData gbeanData) {
        boolean excludeUnlistedClasses = (Boolean) gbeanData.getAttribute("excludeUnlistedClasses");

        if (excludeUnlistedClasses) {
            gbeanData.clearAttribute("jarFileUrls");
        }
    }

    private AbstractNameQuery getAbstractNameQuery(String dataSourceString) throws URISyntaxException {
        if (dataSourceString.indexOf('=') == -1) {
            dataSourceString = "?name=" + dataSourceString;
        }
        AbstractNameQuery dataSourceNameQuery = new AbstractNameQuery(new URI(dataSourceString + "#" + RESOURCE_SOURCE_CLASS_NAME));
        return dataSourceNameQuery;
    }

    private Persistence fromXmlObject(XmlObject xmlObject) throws JAXBException {
        
        /* 
         * To avoid illegal exception in JAXB. Convert 
         * 
         * <xml-fragment> ... </xml-fragment> 
         * to
         * <persistence xmlns="http://java.sun.com/xml/ns/persistence"> ... </persistence>
         * 
         * before unmarshalling it 
         */
        XmlObject newXmlObject=XmlObject.Factory.newInstance();
        XmlCursor newXmlCursor=newXmlObject.newCursor();
        newXmlCursor.toNextToken();
        newXmlCursor.beginElement(PERSISTENCE_QNAME);
        
        XmlCursor oldXmlCursor=xmlObject.newCursor();
        oldXmlCursor.copyXmlContents(newXmlCursor);
        
        oldXmlCursor.dispose();
        newXmlCursor.dispose();
        
        
        XMLStreamReader reader = newXmlObject.newXMLStreamReader();
        JAXBContext context = JAXBContextFactory.newInstance(Persistence.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Persistence) unmarshaller.unmarshal(reader);
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.singleton(PERSISTENCE_QNAME);
    }

}
