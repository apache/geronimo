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

import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.LinkedHashSet;
import java.util.jar.JarFile;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ModuleIDBuilder;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.ModuleBuilderExtension;
import org.apache.geronimo.j2ee.deployment.WebModule;
import org.apache.geronimo.deployment.ClassPathList;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.Naming;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.geronimo.xbeans.persistence.PersistenceDocument;
import org.apache.xbean.finder.ResourceFinder;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitBuilder implements ModuleBuilderExtension {
    private static final QName PERSISTENCE_QNAME = PersistenceDocument.type.getDocumentElementName();

    private final Environment defaultEnvironment;
    private final String defaultPersistenceProviderClassName;
    private final Properties defaultPersistenceUnitProperties;
    private final AbstractNameQuery defaultJtaDataSourceName;
    private final AbstractNameQuery defaultNonJtaDataSourceName;
    private final AbstractNameQuery extendedEntityManagerRegistryName;
    private static final String ANON_PU_NAME = "AnonymousPersistenceUnit";

    public PersistenceUnitBuilder(Environment defaultEnvironment,
            String defaultPersistenceProviderClassName,
            String defaultJtaDataSourceName,
            String defaultNonJtaDataSourceName,
            AbstractNameQuery extendedEntityManagerRegistryName, Properties defaultPersistenceUnitProperties) throws URISyntaxException {
        this.defaultEnvironment = defaultEnvironment;
        this.defaultPersistenceProviderClassName = defaultPersistenceProviderClassName;
        this.defaultJtaDataSourceName = defaultJtaDataSourceName == null ? null : getAbstractNameQuery(defaultJtaDataSourceName);
        this.defaultNonJtaDataSourceName = defaultNonJtaDataSourceName == null ? null : getAbstractNameQuery(defaultNonJtaDataSourceName);
        this.extendedEntityManagerRegistryName = extendedEntityManagerRegistryName;
        this.defaultPersistenceUnitProperties = defaultPersistenceUnitProperties == null ? new Properties() : defaultPersistenceUnitProperties;
    }

    public void createModule(Module module, Object plan, JarFile moduleFile, String targetPath, URL specDDUrl, Environment environment, Object moduleContextInfo, AbstractName earName, Naming naming, ModuleIDBuilder idBuilder) throws DeploymentException {
    }

    public void installModule(JarFile earFile, EARContext earContext, Module module, Collection configurationStores, ConfigurationStore targetConfigurationStore, Collection repository) throws DeploymentException {
    }

    public void initContext(EARContext earContext, Module module, ClassLoader cl) throws DeploymentException {
        XmlObject container = module.getVendorDD();
        EARContext moduleContext = module.getEarContext();
        XmlObject[] raws = container.selectChildren(PERSISTENCE_QNAME);
        for (XmlObject raw : raws) {
            PersistenceDocument.Persistence persistence = (PersistenceDocument.Persistence) raw.copy().changeType(PersistenceDocument.Persistence.type);
            buildPersistenceUnits(persistence, module, NameFactory.PERSISTENCE_UNIT_MODULE, module.getTargetPath());
        }
        try {
            //TODO the code that figures out the persistence unit name is incomplete
            File rootBaseFile = module.getRootEarContext().getConfiguration().getConfigurationDir();
            String rootBase = rootBaseFile.toURI().normalize().toString();
            URI moduleBaseURI = moduleContext.getBaseDir().toURI();
            Map rootGeneralData = module.getRootEarContext().getGeneralData();
            ClassPathList manifestcp = (ClassPathList) module.getEarContext().getGeneralData().get(ClassPathList.class);
            if (manifestcp == null) {
                manifestcp = new ClassPathList();
                manifestcp.add(module.getTargetPath());
            }
            URL[] urls = new URL[manifestcp.size()];
            int i = 0;
            for (String path: manifestcp) {
                URL url = moduleBaseURI.resolve(path).toURL();
                urls[i++] = url;
            }
            ResourceFinder finder = new ResourceFinder("", null, urls);
            List<URL> knownPersistenceUrls = (List<URL>) rootGeneralData.get(PersistenceUnitBuilder.class.getName());
            if (knownPersistenceUrls == null) {
                knownPersistenceUrls = new ArrayList<URL>();
                rootGeneralData.put(PersistenceUnitBuilder.class.getName(), knownPersistenceUrls);
            }
            List<URL> persistenceUrls = finder.findAll("META-INF/persistence.xml");
            persistenceUrls.removeAll(knownPersistenceUrls);
            if (raws.length > 0 || persistenceUrls.size() > 0) {
                EnvironmentBuilder.mergeEnvironments(module.getEnvironment(), defaultEnvironment);
            }
            for (URL persistenceUrl : persistenceUrls) {
                String persistenceLocation;
                try {
                    persistenceLocation = persistenceUrl.toURI().toString();
                } catch (URISyntaxException e) {
                    //????
                    continue;
                }
                int pos = persistenceLocation.indexOf(rootBase);
                if (pos < 0) {
                    //not in the ear
                    continue;
                }
                int endPos = persistenceLocation.lastIndexOf("!/");
                if (endPos < 0) {
                    // if unable to find the '!/' marker, try to see if this is
                    // a war file with the persistence.xml directly embeded - no ejb-jar
                    endPos = persistenceLocation.lastIndexOf("META-INF");
                }
                if (endPos >= 0) {
                    //path relative to ear base uri
                    String relative = persistenceLocation.substring(pos + rootBase.length(), endPos);
                    //find path relative to module base uri
                    relative = module.getRelativePath(relative);
                    PersistenceDocument persistenceDocument;
                    try {
                        XmlObject xmlObject = XmlBeansUtil.parse(persistenceUrl, moduleContext.getClassLoader());
                        persistenceDocument = (PersistenceDocument) xmlObject.changeType(PersistenceDocument.type);
                    } catch (XmlException e) {
                        throw new DeploymentException("Could not parse persistence.xml file: " + persistenceUrl, e);
                    }
                    PersistenceDocument.Persistence persistence = persistenceDocument.getPersistence();
                    buildPersistenceUnits(persistence, module, NameFactory.PERSISTENCE_UNIT_MODULE, relative);
                    knownPersistenceUrls.add(persistenceUrl);
                } else {
                    throw new DeploymentException("Could not find persistence.xml file: " + persistenceUrl);
                }
            }
        } catch (IOException e) {
            throw new DeploymentException("Could not look for META-INF/persistence.xml files", e);
        }
    }

    public void addGBeans(EARContext earContext, Module module, ClassLoader cl, Collection repository) throws DeploymentException {
    }

    private void buildPersistenceUnits(PersistenceDocument.Persistence persistence, Module module, String moduleType, String persistenceModulePath) throws DeploymentException {
        PersistenceDocument.Persistence.PersistenceUnit[] persistenceUnits = persistence.getPersistenceUnitArray();
        for (PersistenceDocument.Persistence.PersistenceUnit persistenceUnit : persistenceUnits) {
            installPersistenceUnitGBean(persistenceUnit, module, moduleType, persistenceModulePath);
        }
    }

    private void installPersistenceUnitGBean(PersistenceDocument.Persistence.PersistenceUnit persistenceUnit, Module module, String moduleType, String persistenceModulePath) throws DeploymentException {
        EARContext moduleContext = module.getEarContext();
        String persistenceUnitName = persistenceUnit.getName().trim();
        if (persistenceUnitName.length() == 0) {
            persistenceUnitName = ANON_PU_NAME;
        }
        AbstractName abstractName;
        if (moduleType == null || persistenceModulePath == null || persistenceModulePath.length() == 0) {
            abstractName = moduleContext.getNaming().createChildName(module.getModuleName(), persistenceUnitName, PersistenceUnitGBean.GBEAN_INFO.getJ2eeType());
        } else {
            abstractName = moduleContext.getNaming().createChildName(module.getModuleName(), persistenceModulePath, moduleType);
            abstractName = moduleContext.getNaming().createChildName(abstractName, persistenceUnitName, PersistenceUnitGBean.GBEAN_INFO.getJ2eeType());
        }
        GBeanData gbeanData = new GBeanData(abstractName, PersistenceUnitGBean.GBEAN_INFO);
        try {
            moduleContext.addGBean(gbeanData);
        } catch (GBeanAlreadyExistsException e) {
            throw new DeploymentException("Duplicate persistenceUnit name " + persistenceUnitName, e);
        }
        gbeanData.setAttribute("persistenceUnitName", persistenceUnitName);
        if (persistenceUnit.isSetProvider()) {
            gbeanData.setAttribute("persistenceProviderClassName", persistenceUnit.getProvider().trim());
        } else {
            gbeanData.setAttribute("persistenceProviderClassName", defaultPersistenceProviderClassName);
        }
        if (persistenceUnit.isSetTransactionType()) {
            gbeanData.setAttribute("persistenceUnitTransactionType", persistenceUnit.getTransactionType().toString());
        } else {
            //TODO verify that the default is supposed to be JTA
            gbeanData.setAttribute("persistenceUnitTransactionType", "JTA");
        }
        if (persistenceUnit.isSetJtaDataSource()) {
            String jtaDataSourceString = persistenceUnit.getJtaDataSource().trim();
            try {
                AbstractNameQuery jtaDataSourceNameQuery = getAbstractNameQuery(jtaDataSourceString);
                gbeanData.setReferencePattern("JtaDataSourceWrapper", jtaDataSourceNameQuery);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not create jta-data-source AbstractNameQuery from string: " + jtaDataSourceString, e);
            }
        } else if (defaultJtaDataSourceName != null) {
            gbeanData.setReferencePattern("JtaDataSourceWrapper", defaultJtaDataSourceName);
        }

        if (persistenceUnit.isSetNonJtaDataSource()) {
            String nonJtaDataSourceString = persistenceUnit.getNonJtaDataSource().trim();
            try {
                AbstractNameQuery nonJtaDataSourceNameQuery = getAbstractNameQuery(nonJtaDataSourceString);
                gbeanData.setReferencePattern("NonJtaDataSourceWrapper", nonJtaDataSourceNameQuery);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not create non-jta-data-source AbstractNameQuery from string: " + nonJtaDataSourceString, e);
            }
        } else if (defaultNonJtaDataSourceName != null) {
            gbeanData.setReferencePattern("NonJtaDataSourceWrapper", defaultNonJtaDataSourceName);
        }

        List<String> mappingFileNames = new ArrayList<String>();
        String[] mappingFileNameStrings = persistenceUnit.getMappingFileArray();
        for (String mappingFileNameString : mappingFileNameStrings) {
            mappingFileNames.add(mappingFileNameString.trim());
        }
        gbeanData.setAttribute("mappingFileNames", mappingFileNames);

        boolean excludeUnlistedClasses = persistenceUnit.isSetExcludeUnlistedClasses() && persistenceUnit.getExcludeUnlistedClasses();
        gbeanData.setAttribute("excludeUnlistedClasses", excludeUnlistedClasses);


        gbeanData.setAttribute("persistenceUnitRoot", persistenceModulePath);

        if (excludeUnlistedClasses) {
            String[] managedClassNameStrings = persistenceUnit.getClass1Array();
            List<String> managedClassNames = new ArrayList<String>();
            for (String managedClassNameString : managedClassNameStrings) {
                managedClassNames.add(managedClassNameString.trim());
            }
            gbeanData.setAttribute("managedClassNames", managedClassNames);
        } else {
            List<String> jarFileUrls = new ArrayList<String>();
            //add the artifact the persistence.xml is in
            //TODO since we are setting the root url, this might not be needed.  On the other hand,
            //we might need to add all the manifest cp entries referenced from here.
//            jarFileUrls.add(persistenceModulePath);
            //add the specified locations in the ear
            String[] jarFileUrlStrings = persistenceUnit.getJarFileArray();
            for (String jarFileUrlString : jarFileUrlStrings) {
                jarFileUrls.add(jarFileUrlString.trim());
            }
            gbeanData.setAttribute("jarFileUrls", jarFileUrls);
        }

        Properties properties = new Properties();
        properties.putAll(defaultPersistenceUnitProperties);
        if (persistenceUnit.isSetProperties()) {
            PersistenceDocument.Persistence.PersistenceUnit.Properties.Property[] propertyObjects = persistenceUnit.getProperties().getPropertyArray();
            for (PersistenceDocument.Persistence.PersistenceUnit.Properties.Property propertyObject : propertyObjects) {
                String key = propertyObject.getName().trim();
                String value = propertyObject.getValue().trim();
                properties.setProperty(key, value);
            }
        }

        gbeanData.setAttribute("properties", properties);
        AbstractNameQuery transactionManagerName = moduleContext.getTransactionManagerName();
        gbeanData.setReferencePattern("TransactionManager", transactionManagerName);
        gbeanData.setReferencePattern("EntityManagerRegistry", extendedEntityManagerRegistryName);
    }

    private AbstractNameQuery getAbstractNameQuery(String dataSourceString) throws URISyntaxException {
        if (dataSourceString.indexOf('=') == -1) {
            dataSourceString = "?name=" + dataSourceString;
        }
        AbstractNameQuery dataSourceNameQuery = new AbstractNameQuery(new URI(dataSourceString + "#org.apache.geronimo.connector.outbound.ConnectionFactorySource"));
        return dataSourceNameQuery;
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.singleton(PERSISTENCE_QNAME);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(PersistenceUnitBuilder.class, NameFactory.MODULE_BUILDER);

        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("defaultPersistenceProviderClassName", String.class, true, true);
        infoBuilder.addAttribute("defaultJtaDataSourceName", String.class, true, true);
        infoBuilder.addAttribute("defaultNonJtaDataSourceName", String.class, true, true);
        infoBuilder.addAttribute("extendedEntityManagerRegistryName", AbstractNameQuery.class, true, true);
        infoBuilder.addAttribute("defaultPersistenceUnitProperties", Properties.class, true, true);

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "defaultPersistenceProviderClassName",
                "defaultJtaDataSourceName",
                "defaultNonJtaDataSourceName",
                "extendedEntityManagerRegistryName",
                "defaultPersistenceUnitProperties"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
