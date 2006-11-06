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


package org.apache.geronimo.naming.deployment;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.service.EnvironmentBuilder;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Dependency;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.ImportType;
import org.apache.geronimo.naming.reference.EntityManagerFactoryReference;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.naming.GerEntityManagerFactoryRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEntityManagerFactoryRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class EntityManagerFactoryRefBuilder implements NamingBuilder {
    private static final QName ENTITY_MANAGER_FACTORY_REF_QNAME = GerEntityManagerFactoryRefDocument.type.getDocumentElementName();
    private static final QNameSet ENTITY_MANAGER_FACTORY_REF_QNAME_SET = QNameSet.singleton(EntityManagerFactoryRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME);

    private final Environment defaultEnvironment = new Environment();

    public EntityManagerFactoryRefBuilder() {
        defaultEnvironment.addDependency(new Dependency(new Artifact("org.apache.geronimo.modules", "geronimo-persistence-jpa10", (String)null, "jar"), ImportType.CLASSES));
    }

    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) {
        if (getEntityManagerFactoryRefs(plan).length > 0) {
            EnvironmentBuilder.mergeEnvironments(environment, defaultEnvironment);
        }
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] EntityManagerFactoryRefsUntyped = getEntityManagerFactoryRefs(plan);
        for (int i = 0; i < EntityManagerFactoryRefsUntyped.length; i++) {
            XmlObject EntityManagerFactoryRefUntyped = EntityManagerFactoryRefsUntyped[i];
            GerEntityManagerFactoryRefType EntityManagerFactoryRef = (GerEntityManagerFactoryRefType) EntityManagerFactoryRefUntyped.copy().changeType(GerEntityManagerFactoryRefType.type);
            if (EntityManagerFactoryRef == null) {
                throw new DeploymentException("Could not read EntityManagerFactoryRef " + EntityManagerFactoryRefUntyped + " as the correct xml type");
            }
            String EntityManagerFactoryRefName = EntityManagerFactoryRef.getEntityManagerFactoryRefName();

            Set interfaceTypes = Collections.singleton("org.apache.geronimo.persistence.PersistenceUnitGBean");
            AbstractNameQuery persistenceUnitNameQuery;
            if (EntityManagerFactoryRef.isSetPersistenceUnitName()) {
                String persistenceUnitName = EntityManagerFactoryRef.getPersistenceUnitName();
                persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), interfaceTypes);
            } else {
                GerPatternType gbeanLocator = EntityManagerFactoryRef.getPattern();

                persistenceUnitNameQuery = ENCConfigBuilder.buildAbstractNameQuery(gbeanLocator, null, null, interfaceTypes);
            }

            try {
                localConfiguration.findGBeanData(persistenceUnitNameQuery);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery, e);
            }

            EntityManagerFactoryReference reference = new EntityManagerFactoryReference(localConfiguration.getId(), persistenceUnitNameQuery);

            ((Map)componentContext.get(JNDI_KEY)).put(ENV + EntityManagerFactoryRefName, reference);

        }
    }

    public QNameSet getSpecQNameSet() {
        SchemaConversionUtils.registerNamespaceConversions(Collections.singletonMap(EntityManagerFactoryRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME.getLocalPart(), new NamespaceElementConverter(EntityManagerFactoryRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME.getNamespaceURI())));
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return EntityManagerFactoryRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME_SET;
    }

    private XmlObject[] getEntityManagerFactoryRefs(XmlObject plan) {
        return plan == null? NO_REFS: plan.selectChildren(EntityManagerFactoryRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME_SET);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EntityManagerFactoryRefBuilder.class, NameFactory.MODULE_BUILDER);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return EntityManagerFactoryRefBuilder.GBEAN_INFO;
    }
}
