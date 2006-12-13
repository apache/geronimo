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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.PersistenceUnitReference;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceUnitRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceUnitRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitRefBuilder extends AbstractNamingBuilder {
    private static final QName ENTITY_MANAGER_FACTORY_REF_QNAME = GerPersistenceUnitRefDocument.type.getDocumentElementName();
    private static final QNameSet ENTITY_MANAGER_FACTORY_REF_QNAME_SET = QNameSet.singleton(PersistenceUnitRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME);


    public PersistenceUnitRefBuilder(Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) throws DeploymentException {
        return getPersistenceUnitRefs(plan).length > 0;
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] PersistenceUnitRefsUntyped = getPersistenceUnitRefs(plan);
        for (int i = 0; i < PersistenceUnitRefsUntyped.length; i++) {
            GerPersistenceUnitRefType PersistenceUnitRef = (GerPersistenceUnitRefType) PersistenceUnitRefsUntyped[i];
            if (PersistenceUnitRef == null) {
                throw new DeploymentException("Could not read PersistenceUnitRef number " + i + " as the correct xml type");
            }
            String PersistenceUnitRefName = PersistenceUnitRef.getPersistenceUnitRefName();

            Set interfaceTypes = Collections.singleton("org.apache.geronimo.persistence.PersistenceUnitGBean");
            AbstractNameQuery persistenceUnitNameQuery;
            if (PersistenceUnitRef.isSetPersistenceUnitName()) {
                String persistenceUnitName = PersistenceUnitRef.getPersistenceUnitName();
                persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), interfaceTypes);
            } else {
                GerPatternType gbeanLocator = PersistenceUnitRef.getPattern();

                persistenceUnitNameQuery = buildAbstractNameQuery(gbeanLocator, null, null, interfaceTypes);
            }

            try {
                localConfiguration.findGBeanData(persistenceUnitNameQuery);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery, e);
            }

            PersistenceUnitReference reference = new PersistenceUnitReference(localConfiguration.getId(), persistenceUnitNameQuery);

            ((Map)componentContext.get(JNDI_KEY)).put(ENV + PersistenceUnitRefName, reference);

        }
    }

    public QNameSet getSpecQNameSet() {
        SchemaConversionUtils.registerNamespaceConversions(Collections.singletonMap(PersistenceUnitRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME.getLocalPart(), new NamespaceElementConverter(PersistenceUnitRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME.getNamespaceURI())));
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return PersistenceUnitRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME_SET;
    }

    private XmlObject[] getPersistenceUnitRefs(XmlObject plan) throws DeploymentException {
        return plan == null? NO_REFS: convert(plan.selectChildren(PersistenceUnitRefBuilder.ENTITY_MANAGER_FACTORY_REF_QNAME_SET), NAMING_CONVERTER, GerPersistenceUnitRefType.type);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(PersistenceUnitRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[] {"defaultEnvironment"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return PersistenceUnitRefBuilder.GBEAN_INFO;
    }
}
