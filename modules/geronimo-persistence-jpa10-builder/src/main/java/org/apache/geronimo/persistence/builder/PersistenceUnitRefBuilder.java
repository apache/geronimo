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
import java.util.HashMap;
import java.util.List;

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
import org.apache.geronimo.xbeans.javaee.PersistenceUnitRefType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceUnitRefBuilder extends AbstractNamingBuilder {
    private static final QName PERSISTENCE_UNIT_REF_QNAME = new QName(JEE_NAMESPACE, "persistence-unit-ref");
    private static final QNameSet PERSISTENCE_UNIT_REF_QNAME_SET = QNameSet.singleton(PERSISTENCE_UNIT_REF_QNAME);
    private static final QName GER_PERSISTENCE_UNIT_REF_QNAME = GerPersistenceUnitRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_PERSISTENCE_UNIT_REF_QNAME_SET = QNameSet.singleton(GER_PERSISTENCE_UNIT_REF_QNAME);
    private static final Set PERSISTENCE_UNIT_INTERFACE_TYPES = Collections.singleton("org.apache.geronimo.persistence.PersistenceUnitGBean");
    private final AbstractNameQuery defaultPersistenceUnitAbstractNameQuery;


    public PersistenceUnitRefBuilder(Environment defaultEnvironment, AbstractNameQuery defaultPersistenceUnitAbstractNameQuery) {
        super(defaultEnvironment);
        this.defaultPersistenceUnitAbstractNameQuery = defaultPersistenceUnitAbstractNameQuery;
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) throws DeploymentException {
        if (specDD != null && specDD.selectChildren(PersistenceUnitRefBuilder.PERSISTENCE_UNIT_REF_QNAME_SET).length > 0) {
            return true;
        }
        return plan != null && plan.selectChildren(PersistenceUnitRefBuilder.GER_PERSISTENCE_UNIT_REF_QNAME_SET).length > 0;
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        List<PersistenceUnitRefType> specPersistenceUnitRefsUntyped = convert(specDD.selectChildren(PersistenceUnitRefBuilder.PERSISTENCE_UNIT_REF_QNAME_SET), NAMING_CONVERTER, PersistenceUnitRefType.class, PersistenceUnitRefType.type);
        Map<String, GerPersistenceUnitRefType> gerPersistenceUnitRefsUntyped = getGerPersistenceUnitRefs(plan);
        for (PersistenceUnitRefType PersistenceUnitRef: specPersistenceUnitRefsUntyped) {
            String persistenceUnitRefName = PersistenceUnitRef.getPersistenceUnitRefName().getStringValue().trim();

            AbstractNameQuery persistenceUnitNameQuery;
            GerPersistenceUnitRefType gerPersistenceUnitRef = gerPersistenceUnitRefsUntyped.get(persistenceUnitRefName);
            if (gerPersistenceUnitRef != null) {
                persistenceUnitNameQuery = findPersistenceUnit(gerPersistenceUnitRef);
            } else if (PersistenceUnitRef.isSetPersistenceUnitName()) {
                String persistenceUnitName = PersistenceUnitRef.getPersistenceUnitName().getStringValue().trim();
                persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
            } else {
                persistenceUnitNameQuery = defaultPersistenceUnitAbstractNameQuery;
            }

            try {
                localConfiguration.findGBeanData(persistenceUnitNameQuery);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery, e);
            }

            PersistenceUnitReference reference = new PersistenceUnitReference(localConfiguration.getId(), persistenceUnitNameQuery);

            ((Map)componentContext.get(JNDI_KEY)).put(ENV + persistenceUnitRefName, reference);

        }


        for (GerPersistenceUnitRefType gerPersistenceUnitRef: gerPersistenceUnitRefsUntyped.values()) {
            String PersistenceUnitRefName = gerPersistenceUnitRef.getPersistenceUnitRefName();

            AbstractNameQuery persistenceUnitNameQuery = findPersistenceUnit(gerPersistenceUnitRef);

            try {
                localConfiguration.findGBeanData(persistenceUnitNameQuery);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery, e);
            }

            PersistenceUnitReference reference = new PersistenceUnitReference(localConfiguration.getId(), persistenceUnitNameQuery);

            ((Map)componentContext.get(JNDI_KEY)).put(ENV + PersistenceUnitRefName, reference);

        }
    }

    private AbstractNameQuery findPersistenceUnit(GerPersistenceUnitRefType gerPersistenceUnitRef) {
        AbstractNameQuery persistenceUnitNameQuery;
        if (gerPersistenceUnitRef.isSetPersistenceUnitName()) {
            String persistenceUnitName = gerPersistenceUnitRef.getPersistenceUnitName();
            persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
        } else {
            GerPatternType gbeanLocator = gerPersistenceUnitRef.getPattern();

            persistenceUnitNameQuery = buildAbstractNameQuery(gbeanLocator, null, null, PERSISTENCE_UNIT_INTERFACE_TYPES);
        }
        return persistenceUnitNameQuery;
    }

    public QNameSet getSpecQNameSet() {
        SchemaConversionUtils.registerNamespaceConversions(Collections.singletonMap(PersistenceUnitRefBuilder.GER_PERSISTENCE_UNIT_REF_QNAME.getLocalPart(), new NamespaceElementConverter(PersistenceUnitRefBuilder.GER_PERSISTENCE_UNIT_REF_QNAME.getNamespaceURI())));
        return PERSISTENCE_UNIT_REF_QNAME_SET;
    }

    public QNameSet getPlanQNameSet() {
        return GER_PERSISTENCE_UNIT_REF_QNAME_SET;
    }

    private Map<String, GerPersistenceUnitRefType> getGerPersistenceUnitRefs(XmlObject plan) throws DeploymentException {
        Map<String, GerPersistenceUnitRefType> map = new HashMap<String, GerPersistenceUnitRefType>();
        if (plan != null) {
            List<GerPersistenceUnitRefType> refs = convert(plan.selectChildren(PersistenceUnitRefBuilder.GER_PERSISTENCE_UNIT_REF_QNAME_SET), NAMING_CONVERTER, GerPersistenceUnitRefType.class, GerPersistenceUnitRefType.type);
            for (GerPersistenceUnitRefType ref: refs) {
                map.put(ref.getPersistenceUnitRefName().trim(), ref);
            }
        }
        return map;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(PersistenceUnitRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("defaultPersistenceUnitAbstractNameQuery", AbstractNameQuery.class, true, true);

        infoBuilder.setConstructor(new String[] {"defaultEnvironment", "defaultPersistenceUnitAbstractNameQuery"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return PersistenceUnitRefBuilder.GBEAN_INFO;
    }
}
