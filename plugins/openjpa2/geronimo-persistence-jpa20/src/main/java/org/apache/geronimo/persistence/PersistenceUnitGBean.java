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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.resource.ResourceException;
import javax.sql.DataSource;
import org.apache.geronimo.bval.ValidatorFactoryGBean; 
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.SingleElementCollection;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.classloader.TemporaryClassLoader;
import org.apache.geronimo.naming.ResourceSource;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.transformer.TransformerAgent;
import org.apache.xbean.osgi.bundle.util.BundleClassLoader;
import org.apache.xbean.osgi.bundle.util.BundleResourceClassLoader;
import org.apache.xbean.osgi.bundle.util.BundleResourceHelper;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.PERSISTENCE_UNIT)
public class PersistenceUnitGBean implements GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(PersistenceUnitGBean.class);

    private static final List<URL> NO_URLS = Collections.emptyList();
    private static final List<String> NO_STRINGS = Collections.emptyList();
    private final String persistenceUnitRoot;
    private final PersistenceUnitInfoImpl persistenceUnitInfo;
    private final EntityManagerFactory entityManagerFactory;
    private final TransactionManagerImpl transactionManager;
    private final SingleElementCollection<ExtendedEntityManagerRegistry> entityManagerRegistry;

    public PersistenceUnitGBean(@ParamAttribute(name = "persistenceUnitName") String persistenceUnitName,
                                @ParamAttribute(name = "persistenceProviderClassName") String persistenceProviderClassName,
                                @ParamAttribute(name = "persistenceUnitTransactionType") String persistenceUnitTransactionTypeString,
                                @ParamReference(name = "JtaDataSourceWrapper", namingType = NameFactory.JCA_CONNECTION_MANAGER) ResourceSource<ResourceException> jtaDataSourceWrapper,
                                @ParamReference(name = "NonJtaDataSourceWrapper", namingType = NameFactory.JCA_CONNECTION_MANAGER) ResourceSource<ResourceException> nonJtaDataSourceWrapper,
                                @ParamAttribute(name = "mappingFileNames") List<String> mappingFileNamesUntyped,
                                @ParamAttribute(name = "jarFileUrls") List<String> jarFileUrlsUntyped,
                                @ParamAttribute(name = "persistenceUnitRoot") String persistenceUnitRoot,
                                @ParamAttribute(name = "managedClassNames") List<String> managedClassNames,
                                @ParamAttribute(name = "excludeUnlistedClasses") boolean excludeUnlistedClassesValue,
                                @ParamAttribute(name = "properties") Properties properties,
                                @ParamReference(name = "TransactionManager", namingType = NameFactory.JTA_RESOURCE) TransactionManagerImpl transactionManager,
                                @ParamReference(name = "EntityManagerRegistry", namingType = GBeanInfoBuilder.DEFAULT_J2EE_TYPE) Collection<ExtendedEntityManagerRegistry> entityManagerRegistry,
                                @ParamAttribute(name = "persistenceXMLSchemaVersion") String persistenceXMLSchemaVersion,
                                @ParamAttribute(name = "sharedCacheMode") SharedCacheMode sharedCacheMode,
                                @ParamAttribute(name = "validationMode") ValidationMode validationMode,
                                @ParamReference(name = "ValidatorFactory", namingType = NameFactory.VALIDATOR_FACTORY) ValidatorFactoryGBean validatorFactory,
                                @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle,
                                @ParamSpecial(type = SpecialAttributeType.classLoader) ClassLoader classLoader) throws URISyntaxException, IOException, ResourceException {
        List<String> mappingFileNames = mappingFileNamesUntyped == null ? NO_STRINGS : new ArrayList<String>(mappingFileNamesUntyped);
        this.persistenceUnitRoot = persistenceUnitRoot;
        
        URL rootURL = getPersistenceUnitRoot(bundle, persistenceUnitRoot);
        URI rootUri = rootURL.toURI();
        
        List<URL> jarFileUrls = NO_URLS;
        if (!excludeUnlistedClassesValue) {
            jarFileUrls = new ArrayList<URL>();
            // Per the EJB3.0 Persistence Specification section 6.2.1.6, 
            // the jar-file should be related to the Persistence Unit Root, which is the jar or directory where the persistence.xml is found             
            for (String urlString : jarFileUrlsUntyped) {
                URL url = new URL ("jar:"+ rootUri.resolve(urlString)+"!/");
                if (url != null) {
                    jarFileUrls.add(url);
                } else {
                    log.warn("jar file {} not found in bundle: {}", urlString, bundle.toString());
                }
            }
        }
        
        classLoader = new BundleClassLoader(bundle, BundleResourceHelper.getSearchWiredBundles(false), BundleResourceHelper.getConvertResourceUrls(true));
        
        if (managedClassNames == null) {
            managedClassNames = NO_STRINGS;
        }
        if (properties == null) {
            properties = new Properties();
        }
        // add the module validator factory instance 
        properties.put("javax.persistence.validation.factory", validatorFactory.getFactory());
        PersistenceUnitTransactionType persistenceUnitTransactionType = persistenceUnitTransactionTypeString == null ? PersistenceUnitTransactionType.JTA : PersistenceUnitTransactionType.valueOf(persistenceUnitTransactionTypeString);

        if (persistenceProviderClassName == null) {
            persistenceProviderClassName = "org.apache.openjpa.persistence.PersistenceProviderImpl";
        }
        
        persistenceUnitInfo = new PersistenceUnitInfoImpl(persistenceUnitName,
                persistenceProviderClassName,
                persistenceUnitTransactionType,
                jtaDataSourceWrapper == null ? null : (DataSource) jtaDataSourceWrapper.$getResource(),
                nonJtaDataSourceWrapper == null ? null : (DataSource) nonJtaDataSourceWrapper.$getResource(),
                mappingFileNames,
                jarFileUrls,
                rootURL,
                managedClassNames,
                excludeUnlistedClassesValue,
                properties,
                persistenceXMLSchemaVersion,
                sharedCacheMode,
                validationMode,
                classLoader, bundle);
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
    
    private static URL getPersistenceUnitRoot(Bundle bundle, String persistenceUnitRoot) throws MalformedURLException {
        if (persistenceUnitRoot == null || persistenceUnitRoot.equals(".")) {
            return bundle.getEntry("/");
        } else {
            // according to EJB3.0 Persistence Specification section 6.2
            // 1. the root of a persistence unit could be the WEB-INF/classes directory of a WAR file
            // 2. the root of a persistence unit could be EJB-JAR, APPCLIENT-JAR
            // 3. the persistence unit can be a jar in the following 3 places: WEB-INF/lib directory of a war; root of a EAR; lib directory of a EAR
            return bundle.getEntry(persistenceUnitRoot);
        }       
    }
    
    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager getEntityManager(boolean transactionScoped, Map properties) {
        if (transactionScoped) {
            return new CMPEntityManagerTxScoped(transactionManager, getPersistenceUnitName(), entityManagerFactory, properties);
        } else if (entityManagerRegistry.getElement() != null) {
            return new CMPEntityManagerExtended(entityManagerRegistry.getElement(), entityManagerFactory, properties, persistenceUnitInfo.getPersistenceUnitName());
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
        private final String persistenceXMLSchemaVersion;
        private final SharedCacheMode sharedCacheMode;
        private final ValidationMode validationMode;
        private final Bundle bundle;


        public PersistenceUnitInfoImpl(String persistenceUnitName,
                                       String persistenceProviderClassName,
                                       PersistenceUnitTransactionType persistenceUnitTransactionType,
                                       DataSource jtaDataSource,
                                       DataSource nonJtaDataSource,
                                       List<String> mappingFileNames,
                                       List<URL> jarFileUrls,
                                       URL persistenceUnitRootUrl,
                                       List<String> managedClassNames,
                                       boolean excludeUnlistedClassesValue,
                                       Properties properties,
                                       String persistenceXMLSchemaVersion,
                                       SharedCacheMode sharedCacheMode,
                                       ValidationMode validationMode,
                                       ClassLoader classLoader,
                                       Bundle bundle) {

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
            this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
            this.sharedCacheMode = sharedCacheMode;
            this.validationMode = validationMode;

            this.classLoader = classLoader;
            this.transformers = new ArrayList<TransformerWrapper>();

            // This classloader can only be used during PersistenceProvider.createContainerEntityManagerFactory() calls
            // Possible that it could be cleaned up sooner, but for now it's destroyed when the PUGBean is stopped
            this.tempClassLoader = new TemporaryClassLoader(new BundleClassLoader(bundle,true,false));
            this.bundle = bundle;
        }

        @Override
        public String getPersistenceUnitName() {
            return persistenceUnitName;
        }

        @Override
        public String getPersistenceProviderClassName() {
            return persistenceProviderClassName;
        }

        @Override
        public PersistenceUnitTransactionType getTransactionType() {
            return persistenceUnitTransactionType;
        }

        @Override
        public DataSource getJtaDataSource() {
            return jtaDataSource;
        }

        @Override
        public DataSource getNonJtaDataSource() {
            return nonJtaDataSource;
        }

        @Override
        public List<String> getMappingFileNames() {
            return mappingFileNames;
        }

        @Override
        public List<URL> getJarFileUrls() {
            return jarFileUrls;
        }

        @Override
        public URL getPersistenceUnitRootUrl() {
            return persistenceUnitRootUrl;
        }

        @Override
        public List<String> getManagedClassNames() {
            return managedClassNames;
        }

        @Override
        public boolean excludeUnlistedClasses() {
            return excludeUnlistedClassesValue;
        }

        @Override
        public Properties getProperties() {
            return properties;
        }

        @Override
        public ClassLoader getClassLoader() {
            return classLoader;
        }

        @Override
        public void addTransformer(ClassTransformer classTransformer) {
            TransformerWrapper transformer = new TransformerWrapper(classTransformer, bundle);
            transformers.add(transformer);
            TransformerAgent.addTransformer(transformer);
        }

        @Override
        public ClassLoader getNewTempClassLoader() {
            return tempClassLoader;
        }

        private void destroy() {
            for (TransformerWrapper t : transformers) {
                TransformerAgent.removeTransformer(t);
            }
        }

        /**
         * JPA2 added methods
         */
        @Override
        public String getPersistenceXMLSchemaVersion() {
            return persistenceXMLSchemaVersion;
        }

        @Override
        public SharedCacheMode getSharedCacheMode() {
            return sharedCacheMode;
        }

        @Override
        public ValidationMode getValidationMode() {
            return validationMode;
        }

    }

}
