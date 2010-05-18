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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.namespace.QName;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ClassPathList;
import org.apache.geronimo.deployment.DeployableBundle;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
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
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.ResourceSource;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.persistence20.PersistenceDocument;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.xbean.osgi.bundle.util.BundleResourceFinder;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
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

    private static final EARContext.Key<List<URL>> PERSISTENCE_URL_LIST_KEY = new EARContext.Key<List<URL>>() {

        @Override
        public List<URL> get(Map<EARContext.Key, Object> keyObjectMap) {
            List<URL> list = (List<URL>) keyObjectMap.get(this);
            if (list == null) {
                list = new ArrayList<URL>();
                keyObjectMap.put(this, list);
            }
            return list;
        }
    };

    private static final QName PERSISTENCE_QNAME = PersistenceDocument.type.getDocumentElementName();

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
        EARContext moduleContext = module.getEarContext();
        XmlObject[] raws = container.selectChildren(PERSISTENCE_QNAME);

        Map<String, PersistenceDocument.Persistence.PersistenceUnit> overrides = new HashMap<String, PersistenceDocument.Persistence.PersistenceUnit>();
        try {
            for (XmlObject raw : raws) {
                PersistenceDocument.Persistence persistence = convertToPersistenceUnit(raw.copy());
                for (PersistenceDocument.Persistence.PersistenceUnit unit : persistence.getPersistenceUnitArray()) {
                    overrides.put(unit.getName().trim(), unit);
                }
            }
        } catch (XmlException e) {
            throw new DeploymentException("Parse Persistence configuration file failed", e);
        }
        try {
            URI moduleBaseURI = moduleContext.getBaseDir().toURI();
            ClassPathList manifestcp = EARContext.CLASS_PATH_LIST_KEY.get(module.getEarContext().getGeneralData());
            if (manifestcp == null) {
                manifestcp = new ClassPathList();
                manifestcp.add(module.getTargetPath());
            }
//            URL[] urls = new URL[manifestcp.size()];
//            int i = 0;
//            for (String path : manifestcp) {
//                //TODO either this encoding is unnecessary or incomplete
//                path = path.replaceAll(" ", "%20");
//                URL url = moduleBaseURI.resolve(path).toURL();
//                urls[i++] = url;
//            }
            BundleResourceFinder finder = new BundleResourceFinder(packageAdmin, bundle, "", "META-INF/persistence.xml");
            List<URL> knownPersistenceUrls = PERSISTENCE_URL_LIST_KEY.get(module.getRootEarContext().getGeneralData());
            final Map<URL, String> persistenceURLs = new HashMap<URL, String>();
            finder.find(new BundleResourceFinder.ResourceFinderCallback() {

                @Override
                public void foundInDirectory(Bundle bundle, String baseDir, URL url) throws Exception {
                    persistenceURLs.put(url, baseDir);
                }

                @Override
                public void foundInJar(Bundle bundle, String jarName, ZipEntry entry, InputStream inputStream) throws Exception {
                    URL jarURL = bundle.getEntry(jarName);
                    URL url = new URL("jar:" + jarURL.toString() + "!/" + entry.getName());
                    persistenceURLs.put(url, jarName);
                }
            });
            persistenceURLs.keySet().removeAll(knownPersistenceUrls);
            if (raws.length > 0 || persistenceURLs.size() > 0) {
                EnvironmentBuilder.mergeEnvironments(module.getEnvironment(), defaultEnvironment);
            }
            for (Map.Entry<URL, String> entry : persistenceURLs.entrySet()) {
                URL persistenceUrl = entry.getKey();
                String persistenceLocation = entry.getValue();
                PersistenceDocument persistenceDocument;
                try {
                    XmlObject xmlObject = XmlBeansUtil.parse(persistenceUrl, new BundleClassLoader(moduleContext.getDeploymentBundle()));
                    persistenceDocument = convertToPersistenceDocument(xmlObject);
                } catch (XmlException e) {
                    throw new DeploymentException("Could not parse persistence.xml file: " + persistenceUrl, e);
                }
                PersistenceDocument.Persistence persistence = persistenceDocument.getPersistence();
                buildPersistenceUnits(persistence, overrides, module, persistenceLocation);
                knownPersistenceUrls.add(persistenceUrl);
            }
        } catch (Exception e) {
            throw new DeploymentException("Could not look for META-INF/persistence.xml files", e);
        }

        for (PersistenceDocument.Persistence.PersistenceUnit persistenceUnit : overrides.values()) {
            GBeanData data = installPersistenceUnitGBean(persistenceUnit, module, module.getTargetPath());
            respectExcludeUnlistedClasses(data);
        }
    }

    public void addGBeans(EARContext earContext, Module module, Bundle bundle, Collection repository) throws DeploymentException {
    }

    protected PersistenceDocument convertToPersistenceDocument(XmlObject xmlObject) throws XmlException {
        XmlCursor cursor = null;
        try {
            cursor = xmlObject.newCursor();
            cursor.toStartDoc();
            cursor.toFirstChild();
            SchemaConversionUtils.convertSchemaVersion(cursor, SchemaConversionUtils.JPA_PERSISTENCE_NAMESPACE, "http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd", "2.0");
            XmlObject result = xmlObject.changeType(PersistenceDocument.type);
            XmlBeansUtil.validateDD(result);
            return (PersistenceDocument) result;
        } finally {
            if (cursor != null) {
                try {
                    cursor.dispose();
                } catch (Exception e) {
                    //ignore
                }
            }
        }
    }

    protected PersistenceDocument.Persistence convertToPersistenceUnit(XmlObject xmlObject) throws XmlException {
//        XmlCursor cursor = null;
//        try {
//            cursor = xmlObject.newCursor();
//            cursor.toStartDoc();
//            cursor.toFirstChild();
//            SchemaConversionUtils.convertSchemaVersion(cursor, SchemaConversionUtils.JPA_PERSISTENCE_NAMESPACE, "http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd", "2.0");
        XmlObject result = xmlObject.changeType(PersistenceDocument.Persistence.type);
        XmlBeansUtil.validateDD(result);
        return (PersistenceDocument.Persistence) result;
//        } finally {
//            if (cursor != null) {
//                try {
//                    cursor.dispose();
//                } catch (Exception e) {
//                }
//            }
//        }
    }

    private void buildPersistenceUnits(PersistenceDocument.Persistence persistence, Map<String, PersistenceDocument.Persistence.PersistenceUnit> overrides, Module module, String persistenceModulePath) throws DeploymentException {
        PersistenceDocument.Persistence.PersistenceUnit[] persistenceUnits = persistence.getPersistenceUnitArray();
        for (PersistenceDocument.Persistence.PersistenceUnit persistenceUnit : persistenceUnits) {
            GBeanData data = installPersistenceUnitGBean(persistenceUnit, module, persistenceModulePath);
            String unitName = persistenceUnit.getName().trim();
            if (overrides.get(unitName) != null) {
                setOverrideableProperties(overrides.remove(unitName), data);
            }
            respectExcludeUnlistedClasses(data);
        }
    }

    private GBeanData installPersistenceUnitGBean(PersistenceDocument.Persistence.PersistenceUnit persistenceUnit, Module module, String persistenceModulePath) throws DeploymentException {
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

        setOverrideableProperties(persistenceUnit, gbeanData);
        return gbeanData;
    }

    private void setOverrideableProperties(PersistenceDocument.Persistence.PersistenceUnit persistenceUnit, GBeanData gbeanData) throws DeploymentException {
        if (persistenceUnit.isSetProvider()) {
            gbeanData.setAttribute("persistenceProviderClassName", persistenceUnit.getProvider().trim());
        }
        if (persistenceUnit.isSetTransactionType()) {
            gbeanData.setAttribute("persistenceUnitTransactionType", persistenceUnit.getTransactionType().toString());
        }
        if (persistenceUnit.isSetJtaDataSource()) {
            String jtaDataSourceString = persistenceUnit.getJtaDataSource().trim();
            try {
                AbstractNameQuery jtaDataSourceNameQuery = getAbstractNameQuery(jtaDataSourceString);
                gbeanData.setReferencePattern("JtaDataSourceWrapper", jtaDataSourceNameQuery);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not create jta-data-source AbstractNameQuery from string: " + jtaDataSourceString, e);
            }
        }

        if (persistenceUnit.isSetNonJtaDataSource()) {
            String nonJtaDataSourceString = persistenceUnit.getNonJtaDataSource().trim();
            try {
                AbstractNameQuery nonJtaDataSourceNameQuery = getAbstractNameQuery(nonJtaDataSourceString);
                gbeanData.setReferencePattern("NonJtaDataSourceWrapper", nonJtaDataSourceNameQuery);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not create non-jta-data-source AbstractNameQuery from string: " + nonJtaDataSourceString, e);
            }
        }

        List<String> mappingFileNames = (List<String>) gbeanData.getAttribute("mappingFileNames");
        String[] mappingFileNameStrings = persistenceUnit.getMappingFileArray();
        for (String mappingFileNameString : mappingFileNameStrings) {
            mappingFileNames.add(mappingFileNameString.trim());
        }

        if (persistenceUnit.isSetExcludeUnlistedClasses()) {
            gbeanData.setAttribute("excludeUnlistedClasses", persistenceUnit.getExcludeUnlistedClasses());
        }

        String[] managedClassNameStrings = persistenceUnit.getClass1Array();
        List<String> managedClassNames = (List<String>) gbeanData.getAttribute("managedClassNames");
        for (String managedClassNameString : managedClassNameStrings) {
            managedClassNames.add(managedClassNameString.trim());
        }
        List<String> jarFileUrls = (List<String>) gbeanData.getAttribute("jarFileUrls");
        //add the specified locations in the ear
        String[] jarFileUrlStrings = persistenceUnit.getJarFileArray();
        for (String jarFileUrlString : jarFileUrlStrings) {
            jarFileUrls.add(jarFileUrlString.trim());
        }

        if (persistenceUnit.isSetProperties()) {
            Properties properties = (Properties) gbeanData.getAttribute("properties");
            PersistenceDocument.Persistence.PersistenceUnit.Properties.Property[] propertyObjects = persistenceUnit.getProperties().getPropertyArray();
            for (PersistenceDocument.Persistence.PersistenceUnit.Properties.Property propertyObject : propertyObjects) {
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

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.singleton(PERSISTENCE_QNAME);
    }

}
