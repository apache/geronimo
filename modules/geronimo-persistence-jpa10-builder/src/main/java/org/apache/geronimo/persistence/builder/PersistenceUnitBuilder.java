/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.persistence.builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.NamespaceDrivenBuilder;
import org.apache.geronimo.deployment.xbeans.ServiceDocument;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.persistence.PersistenceUnitGBean;
import org.apache.geronimo.xbeans.persistence.PersistenceDocument;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSet;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitBuilder implements NamespaceDrivenBuilder {
    private static final QName PERSISTENCE_QNAME = PersistenceDocument.type.getDocumentElementName();

    private final Environment defaultEnvironment;
    private final String defaultPersistenceProviderClassName;

    public PersistenceUnitBuilder(Environment defaultEnvironment, String defaultPersistenceProviderClassName) {
        this.defaultEnvironment = defaultEnvironment;
        this.defaultPersistenceProviderClassName = defaultPersistenceProviderClassName;
    }

    public void buildEnvironment(XmlObject container, Environment environment) throws DeploymentException {
        XmlObject[] raws = container.selectChildren(PERSISTENCE_QNAME);
        if (raws.length > 0) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        }
    }

    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        XmlObject[] raws = container.selectChildren(PERSISTENCE_QNAME);
        for (XmlObject raw : raws) {
            PersistenceDocument.Persistence persistence = (PersistenceDocument.Persistence) raw.copy().changeType(PersistenceDocument.Persistence.type);
            PersistenceDocument.Persistence.PersistenceUnit[] persistenceUnits = persistence.getPersistenceUnitArray();
            for (PersistenceDocument.Persistence.PersistenceUnit persistenceUnit : persistenceUnits) {
                String persistenceUnitName = persistenceUnit.getName().trim();
                GBeanData gbeanData;
                try {
                    gbeanData = moduleContext.addGBean(persistenceUnitName, PersistenceUnitGBean.GBEAN_INFO);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException("Duplicate persistenceUnit name " + persistenceUnitName, e);
                }
                gbeanData.setAttribute("persistenceUnitName", persistenceUnitName);
                if (persistenceUnit.isSetProvider()) {
                    gbeanData.setAttribute("persistenceProviderClassName", persistenceUnit.getProvider().trim());
                } else {
                    gbeanData.setAttribute("persistenceProviderClassName", defaultPersistenceProviderClassName);
                }
                gbeanData.setAttribute("persistenceUnitTransactionType", persistenceUnit.getTransactionType().toString());
                if (persistenceUnit.isSetJtaDataSource()) {
                    String jtaDataSourceString = persistenceUnit.getJtaDataSource().trim();
                    try {
                        AbstractNameQuery jtaDataSourceNameQuery = new AbstractNameQuery(new URI(jtaDataSourceString + "#org.apache.geronimo.connector.outbound.ConnectionFactorySource"));
                        gbeanData.setReferencePattern("JtaDataSourceWrapper", jtaDataSourceNameQuery);
                    } catch (URISyntaxException e) {
                        throw new DeploymentException("Could not create jta-data-source AbstractNameQuery from string: " + jtaDataSourceString, e);
                    }
                }

                if (persistenceUnit.isSetNonJtaDataSource()) {
                    String nonJtaDataSourceString = persistenceUnit.getNonJtaDataSource().trim();
                    try {
                        AbstractNameQuery nonJtaDataSourceNameQuery = new AbstractNameQuery(new URI(nonJtaDataSourceString + "#org.apache.geronimo.connector.outbound.ConnectionFactorySource"));
                        gbeanData.setReferencePattern("NonJtaDataSourceWrapper", nonJtaDataSourceNameQuery);
                    } catch (URISyntaxException e) {
                        throw new DeploymentException("Could not create non-jta-data-source AbstractNameQuery from string: " + nonJtaDataSourceString, e);
                    }
                }

                List<String> mappingFileNames = new ArrayList<String>();
                String[] mappingFileNameStrings = persistenceUnit.getMappingFileArray();
                for (String mappingFileNameString : mappingFileNameStrings) {
                    mappingFileNames.add(mappingFileNameString.trim());
                }
                gbeanData.setAttribute("mappingFileNames", mappingFileNames);

                List<URL> jarFileUrls = new ArrayList<URL>();
                String[] jarFileUrlStrings = persistenceUnit.getJarFileArray();
                for (String jarFileUrlString1 : jarFileUrlStrings) {
                    String jarFileUrlString = jarFileUrlString1.trim();
                    try {
                        URL jarFileUrl = new URL(jarFileUrlString);
                        jarFileUrls.add(jarFileUrl);
                    } catch (MalformedURLException e) {
                        throw new DeploymentException("could not create URL for jarFileURL", e);
                    }
                }
                gbeanData.setAttribute("jarFileUrls", jarFileUrls);
                //TODO what is this from??
//                URL persistenceUnitRootUrl = new URL(persistenceUnit.get)
//                gbeanData.setAttribute("persistenceUnitRootUrl", persistenceUnitRootUrl);

                String[] managedClassNameStrings = persistenceUnit.getClass1Array();
                List<String> managedClassNames = new ArrayList<String>();
                for (String managedClassNameString : managedClassNameStrings) {
                    managedClassNames.add(managedClassNameString.trim());
                }
                gbeanData.setAttribute("managedClassNames", managedClassNames);
                if (persistenceUnit.isSetExcludeUnlistedClasses()) {
                    gbeanData.setAttribute("excludeUnlistedClasses", persistenceUnit.getExcludeUnlistedClasses());
                } else {
                    gbeanData.setAttribute("excludeUnlistedClassesValue", false);
                }

                Properties properties = new Properties();
                if (persistenceUnit.isSetProperties()) {
                    PersistenceDocument.Persistence.PersistenceUnit.Properties.Property[] propertyObjects = persistenceUnit.getProperties().getPropertyArray();
                    for (PersistenceDocument.Persistence.PersistenceUnit.Properties.Property propertyObject : propertyObjects)
                    {
                        String key = propertyObject.getName().trim();
                        String value = propertyObject.getValue().trim();
                        properties.setProperty(key, value);
                    }
                }

                gbeanData.setAttribute("properties", properties);
                if (moduleContext instanceof EARContext) {
                    AbstractNameQuery transactionManagerName = ((EARContext) moduleContext).getTransactionManagerName();
                    gbeanData.setReferencePattern("TransactionManager", transactionManagerName);
                }
            }
        }
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

        infoBuilder.setConstructor(new String[]{
                "defaultEnvironment",
                "defaultPersistenceProviderClassName"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();

    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }


}
