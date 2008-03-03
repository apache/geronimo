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

package org.apache.geronimo.persistence;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import javax.resource.ResourceException;

import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.classloader.TemporaryClassLoader;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transformer.TransformerAgent;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitGBean implements GBeanLifecycle {
    private static final List<URL> NO_URLS = Collections.emptyList();
    private static final List<String> NO_STRINGS = Collections.emptyList();
    private final String persistenceUnitRoot;
    private final PersistenceUnitInfoImpl persistenceUnitInfo;
    private final EntityManagerFactory entityManagerFactory;
    private final TransactionManagerImpl transactionManager;
    private final SingleElementCollection<ExtendedEntityManagerRegistry> entityManagerRegistry;


    public PersistenceUnitGBean() {
        persistenceUnitRoot = null;
        persistenceUnitInfo = null;
        entityManagerFactory = null;
        transactionManager = null;
        entityManagerRegistry = null;
    }

    public PersistenceUnitGBean(String persistenceUnitName,
            String persistenceProviderClassName,
            String persistenceUnitTransactionTypeString,
            ConnectionFactorySource jtaDataSourceWrapper,
            ConnectionFactorySource nonJtaDataSourceWrapper,
            List<String> mappingFileNamesUntyped,
            List<String> jarFileUrlsUntyped,
            String persistenceUnitRoot,
            List<String> managedClassNames,
            boolean excludeUnlistedClassesValue,
            Properties properties,
            TransactionManagerImpl transactionManager,
            Collection<ExtendedEntityManagerRegistry > entityManagerRegistry,
            URL configurationBaseURL,
            ClassLoader classLoader) throws URISyntaxException, MalformedURLException, ResourceException {
        List<String> mappingFileNames = mappingFileNamesUntyped == null? NO_STRINGS: new ArrayList<String>(mappingFileNamesUntyped);
        this.persistenceUnitRoot = persistenceUnitRoot;
        URI configurationBaseURI = new File(configurationBaseURL.getFile()).toURI();
        URL rootURL = configurationBaseURI.resolve(persistenceUnitRoot).normalize().toURL();
        List<URL> jarFileUrls = NO_URLS;
        if (!excludeUnlistedClassesValue) {
            jarFileUrls = new ArrayList<URL>();
            for (String urlString: jarFileUrlsUntyped) {
                URL url = configurationBaseURI.resolve(urlString).normalize().toURL();
                jarFileUrls.add(url);
            }
        }
        if (managedClassNames == null) {
            managedClassNames = NO_STRINGS;
        }
        if (properties == null) {
            properties = new Properties();
        }
        PersistenceUnitTransactionType persistenceUnitTransactionType = persistenceUnitTransactionTypeString == null? PersistenceUnitTransactionType.JTA: PersistenceUnitTransactionType.valueOf(persistenceUnitTransactionTypeString);

        if (persistenceProviderClassName == null) persistenceProviderClassName = "org.apache.openjpa.persistence.PersistenceProviderImpl";
        
        persistenceUnitInfo = new PersistenceUnitInfoImpl(persistenceUnitName,
                persistenceProviderClassName,
                persistenceUnitTransactionType,
                jtaDataSourceWrapper == null? null: (DataSource)jtaDataSourceWrapper.$getResource(),
                nonJtaDataSourceWrapper == null? null: (DataSource)nonJtaDataSourceWrapper.$getResource(),
                mappingFileNames,
                jarFileUrls,
                rootURL,
                managedClassNames,
                excludeUnlistedClassesValue,
                properties,
                classLoader);
        try {
            Class clazz = classLoader.loadClass(persistenceProviderClassName);
            PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();
            entityManagerFactory = persistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
        } catch (ClassNotFoundException e) {
            persistenceUnitInfo.destroy();
            throw new PersistenceException("Could not locate PersistenceProvider class: " + persistenceProviderClassName + " in classloader " + classLoader, e);
        } catch (InstantiationException e) {
            persistenceUnitInfo.destroy();
            throw new PersistenceException("Could not create PersistenceProvider instance: " + persistenceProviderClassName + " loaded from classloader " + classLoader, e);
        } catch (IllegalAccessException e) {
            persistenceUnitInfo.destroy();
            throw new PersistenceException("Could not create PersistenceProvider instance: " + persistenceProviderClassName + " loaded from classloader " + classLoader, e);
        }
        this.transactionManager = transactionManager;
        this.entityManagerRegistry = new SingleElementCollection<ExtendedEntityManagerRegistry>(entityManagerRegistry);
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager getEntityManager(boolean transactionScoped, Map properties) {
        if (transactionScoped) {
            return new CMPEntityManagerTxScoped(transactionManager, getPersistenceUnitName(), entityManagerFactory, properties);
        } else if (entityManagerRegistry.getElement() != null) {
            return new CMPEntityManagerExtended(entityManagerRegistry.getElement(), entityManagerFactory, properties);
        } else {
            throw new NullPointerException("No ExtendedEntityManagerRegistry supplied, you cannot use extended persistence contexts");
        }
    }

    public String getPersistenceUnitName() {
        return persistenceUnitInfo.getPersistenceUnitName();
    }


    public String getPersistenceUnitRoot() {
        return persistenceUnitRoot;
    }

    public String getPersistenceProviderClassName() {
        return persistenceUnitInfo.getPersistenceProviderClassName();
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return persistenceUnitInfo.getTransactionType();
    }

    public DataSource getJtaDataSource() {
        return persistenceUnitInfo.getJtaDataSource();
    }

    public DataSource getNonJtaDataSource() {
        return persistenceUnitInfo.getNonJtaDataSource();
    }

    public List<String> getMappingFileNames() {
        return persistenceUnitInfo.getMappingFileNames();
    }

    public List<URL> getJarFileUrls() {
        return persistenceUnitInfo.getJarFileUrls();
    }

    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitInfo.getPersistenceUnitRootUrl();
    }

    public List<String> getManagedClassNames() {
        return persistenceUnitInfo.getManagedClassNames();
    }

    public boolean excludeUnlistedClasses() {
        return persistenceUnitInfo.excludeUnlistedClasses();
    }

    public Properties getProperties() {
        return persistenceUnitInfo.getProperties();
    }

    public ClassLoader getClassLoader() {
        return persistenceUnitInfo.getClassLoader();
    }

    public void addTransformer(ClassTransformer classTransformer) {
        persistenceUnitInfo.addTransformer(classTransformer);
    }

    public ClassLoader getNewTempClassLoader() {
        return persistenceUnitInfo.getNewTempClassLoader();
    }

    public void doStart() throws Exception {
    }

    public void doStop() throws Exception {
        //TODO remove any classtransformers added
        entityManagerFactory.close();
        persistenceUnitInfo.destroy();
    }

    public void doFail() {
        entityManagerFactory.close();
        persistenceUnitInfo.destroy();
    }

    private static class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
        private final String persistenceUnitName;
        private final String persistenceProviderClassName;
        private final PersistenceUnitTransactionType persistenceUnitTransactionType;
        private final DataSource jtaDataSource;
        private final DataSource nonJtaDataSource;
        private final List<String> mappingFileNames;
        private final List<URL> jarFileUrls;
        private final URL persistenceUnitRootUrl;
        private final List<String> managedClassNames;
        private final boolean excludeUnlistedClassesValue;
        private final Properties properties;
        private final ClassLoader classLoader;
        private final TemporaryClassLoader tempClassLoader;
        private final List<TransformerWrapper> transformers;


        public PersistenceUnitInfoImpl(String persistenceUnitName, String persistenceProviderClassName, PersistenceUnitTransactionType persistenceUnitTransactionType, DataSource jtaDataSource, DataSource nonJtaDataSource, List<String> mappingFileNames, List<URL> jarFileUrls, URL persistenceUnitRootUrl, List<String> managedClassNames, boolean excludeUnlistedClassesValue, Properties properties, ClassLoader classLoader) {
            this.persistenceUnitName = persistenceUnitName;
            this.persistenceProviderClassName = persistenceProviderClassName;
            this.persistenceUnitTransactionType = persistenceUnitTransactionType;
            this.jtaDataSource = jtaDataSource;
            this.nonJtaDataSource = nonJtaDataSource;
            this.mappingFileNames = mappingFileNames;
            this.jarFileUrls = jarFileUrls;
            this.persistenceUnitRootUrl = persistenceUnitRootUrl;
            this.managedClassNames = managedClassNames;
            this.excludeUnlistedClassesValue = excludeUnlistedClassesValue;
            this.properties = properties;
            this.classLoader = classLoader;
            this.transformers = new ArrayList<TransformerWrapper>();
            
            // This classloader can only be used during PersistenceProvider.createContainerEntityManagerFactory() calls
            // Possible that it could be cleaned up sooner, but for now it's destroyed when the PUGBean is stopped
            this.tempClassLoader = new TemporaryClassLoader(classLoader); 
        }

        public String getPersistenceUnitName() {
            return persistenceUnitName;
        }

        public String getPersistenceProviderClassName() {
            return persistenceProviderClassName;
        }

        public PersistenceUnitTransactionType getTransactionType() {
            return persistenceUnitTransactionType;
        }

        public DataSource getJtaDataSource() {
            return jtaDataSource;
        }

        public DataSource getNonJtaDataSource() {
            return nonJtaDataSource;
        }

        public List<String> getMappingFileNames() {
            return mappingFileNames;
        }

        public List<URL> getJarFileUrls() {
            return jarFileUrls;
        }

        public URL getPersistenceUnitRootUrl() {
            return persistenceUnitRootUrl;
        }

        public List<String> getManagedClassNames() {
            return managedClassNames;
        }

        public boolean excludeUnlistedClasses() {
            return excludeUnlistedClassesValue;
        }

        public Properties getProperties() {
            return properties;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }

        public void addTransformer(ClassTransformer classTransformer) {
            TransformerWrapper transformer = new TransformerWrapper(classTransformer, classLoader);
            transformers.add(transformer);
            TransformerAgent.addTransformer(transformer);
        }

        public ClassLoader getNewTempClassLoader() {
            return tempClassLoader;
        }

        private void destroy() {
            for (TransformerWrapper t : transformers) {
                TransformerAgent.removeTransformer(t);
            }
        }

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(PersistenceUnitGBean.class, NameFactory.PERSISTENCE_UNIT);
        infoBuilder.setPriority(GBeanInfo.PRIORITY_CLASSLOADER);

        infoBuilder.addAttribute("persistenceUnitName", String.class, true, true);
        infoBuilder.addAttribute("persistenceProviderClassName", String.class, true, true);
        infoBuilder.addAttribute("persistenceUnitTransactionType", String.class, true, true);
        infoBuilder.addAttribute("mappingFileNames", List.class, true, true);
        infoBuilder.addAttribute("jarFileUrls", List.class, true, true);
        infoBuilder.addAttribute("persistenceUnitRoot", String.class, true, true);
        infoBuilder.addAttribute("managedClassNames", List.class, true, true);
        infoBuilder.addAttribute("excludeUnlistedClasses", boolean.class, true, true);
        infoBuilder.addAttribute("properties", Properties.class, true, true);
        infoBuilder.addAttribute("configurationBaseUrl", URL.class, true);

        infoBuilder.addReference("TransactionManager", TransactionManagerImpl.class, NameFactory.TRANSACTION_MANAGER);
        infoBuilder.addReference("JtaDataSourceWrapper", ConnectionFactorySource.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoBuilder.addReference("NonJtaDataSourceWrapper", ConnectionFactorySource.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoBuilder.addReference("EntityManagerRegistry", ExtendedEntityManagerRegistry.class, NameFactory.GERONIMO_SERVICE);

        infoBuilder.setConstructor(new String[] {
                "persistenceUnitName",
                "persistenceProviderClassName",
                "persistenceUnitTransactionType",
                "JtaDataSourceWrapper",
                "NonJtaDataSourceWrapper",
                "mappingFileNames",
                "jarFileUrls",
                "persistenceUnitRoot",
                "managedClassNames",
                "excludeUnlistedClasses",
                "properties",
                "TransactionManager",
                "EntityManagerRegistry",
                "configurationBaseUrl",
                "classLoader"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
