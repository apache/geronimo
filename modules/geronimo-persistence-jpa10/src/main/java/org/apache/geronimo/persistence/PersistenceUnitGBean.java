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

import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.ArrayList;
import java.net.URL;

import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.transaction.manager.TransactionManagerImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.apache.geronimo.transformer.TransformerAgent;
import org.apache.geronimo.kernel.classloader.JarFileClassLoader;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitGBean implements GBeanLifecycle {
    private final PersistenceUnitInfoImpl persistenceUnitInfo;
    private final EntityManagerFactory entityManagerFactory;
    private final TransactionManagerImpl transactionManager;


    public PersistenceUnitGBean() {
        persistenceUnitInfo = null;
        entityManagerFactory = null;
        transactionManager = null;
    }

    public PersistenceUnitGBean(String persistenceUnitName,
            String persistenceProviderClassName,
            String persistenceUnitTransactionTypeString,
            ConnectionFactorySource jtaDataSourceWrapper,
            ConnectionFactorySource nonJtaDataSourceWrapper,
            List mappingFileNamesUntyped,
            List jarFileUrlsUntyped,
            URL persistenceUnitRootUrl,
            List managedClassNamesUntyped,
            boolean excludeUnlistedClassesValue,
            Properties properties,
            TransactionManagerImpl transactionManager,
            ClassLoader classLoader) {
        List<String> mappingFileNames = mappingFileNamesUntyped == null? new ArrayList<String>(): new ArrayList<String>(mappingFileNamesUntyped);
        List<URL> jarFileUrls = jarFileUrlsUntyped == null? new ArrayList<URL>(): new ArrayList<URL>(jarFileUrlsUntyped);
        List<String> managedClassNames = managedClassNamesUntyped == null? new ArrayList<String>(): new ArrayList<String>(managedClassNamesUntyped);
        PersistenceUnitTransactionType persistenceUnitTransactionType = persistenceUnitTransactionTypeString == null? PersistenceUnitTransactionType.JTA: PersistenceUnitTransactionType.valueOf(persistenceUnitTransactionTypeString);

        if (persistenceProviderClassName == null) persistenceProviderClassName = "org.apache.openjpa.persistence.PersistenceProviderImpl";
        
        persistenceUnitInfo = new PersistenceUnitInfoImpl(persistenceUnitName,
                persistenceProviderClassName,
                persistenceUnitTransactionType,
                jtaDataSourceWrapper == null? null: (DataSource)jtaDataSourceWrapper.$getResource(),
                nonJtaDataSourceWrapper == null? null: (DataSource)nonJtaDataSourceWrapper.$getResource(),
                mappingFileNames,
                jarFileUrls,
                persistenceUnitRootUrl,
                managedClassNames,
                excludeUnlistedClassesValue,
                properties,
                classLoader);
        try {
            Class clazz = classLoader.loadClass(persistenceProviderClassName);
            PersistenceProvider persistenceProvider = (PersistenceProvider) clazz.newInstance();
            entityManagerFactory = persistenceProvider.createContainerEntityManagerFactory(persistenceUnitInfo, properties);
        } catch (ClassNotFoundException e) {
            throw new PersistenceException("Could not locate PersistenceProvider class: " + persistenceProviderClassName + " in classloader " + classLoader, e);
        } catch (InstantiationException e) {
            throw new PersistenceException("Could not create PersistenceProvider instance: " + persistenceProviderClassName + " loaded from classloader " + classLoader, e);
        } catch (IllegalAccessException e) {
            throw new PersistenceException("Could not create PersistenceProvider instance: " + persistenceProviderClassName + " loaded from classloader " + classLoader, e);
        }
        this.transactionManager = transactionManager;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public EntityManager getEntityManager(boolean transactionScoped, Map properties) {
        if (transactionScoped) {
            return new CMPEntityManagerTxScoped(transactionManager, getPersistenceUnitName(), entityManagerFactory, properties);
        } else {
            return new CMPEntityManagerExtended(transactionManager, getPersistenceUnitName(), entityManagerFactory, properties);
        }
    }

    public String getPersistenceUnitName() {
        return persistenceUnitInfo.getPersistenceUnitName();
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
    }

    public void doFail() {
        entityManagerFactory.close();
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
            TransformerAgent.addTransformer(new TransformerWrapper(classTransformer, classLoader));
        }

        public ClassLoader getNewTempClassLoader() {
            return JarFileClassLoader.copy(classLoader);
        }

    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(PersistenceUnitGBean.class);
        infoBuilder.setPriority(GBeanInfo.PRIORITY_CLASSLOADER);

        infoBuilder.addAttribute("persistenceUnitName", String.class, true, true);
        infoBuilder.addAttribute("persistenceProviderClassName", String.class, true, true);
        infoBuilder.addAttribute("persistenceUnitTransactionType", String.class, true, true);
        infoBuilder.addAttribute("mappingFileNames", List.class, true, true);
        infoBuilder.addAttribute("jarFileUrls", List.class, true, true);
        infoBuilder.addAttribute("persistenceUnitRootUrl", URL.class, true, true);
        infoBuilder.addAttribute("managedClassNames", List.class, true, true);
        infoBuilder.addAttribute("excludeUnlistedClasses", boolean.class, true, true);
        infoBuilder.addAttribute("properties", Properties.class, true, true);
        infoBuilder.addReference("TransactionManager", TransactionManagerImpl.class, NameFactory.TRANSACTION_MANAGER);
        infoBuilder.addReference("JtaDataSourceWrapper", ConnectionFactorySource.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoBuilder.addReference("NonJtaDataSourceWrapper", ConnectionFactorySource.class, NameFactory.JCA_MANAGED_CONNECTION_FACTORY);
        infoBuilder.addOperation("getEntityManagerFactory");
        infoBuilder.addOperation("getEntityManager", new Class[] {boolean.class, Map.class});

        infoBuilder.setConstructor(new String[] {
                "persistenceUnitName",
                "persistenceProviderClassName",
                "persistenceUnitTransactionType",
                "JtaDataSourceWrapper",
                "NonJtaDataSourceWrapper",
                "mappingFileNames",
                "jarFileUrls",
                "persistenceUnitRootUrl",
                "managedClassNames",
                "excludeUnlistedClasses",
                "properties",
                "TransactionManager",
                "classLoader"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
