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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
import org.apache.geronimo.naming.reference.PersistenceContextReference;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceContextRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceContextRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceContextTypeType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPropertyType;
import org.apache.geronimo.xbeans.javaee.PersistenceContextRefType;
import org.apache.geronimo.xbeans.javaee.PropertyType;
import org.apache.geronimo.xbeans.javaee.PersistenceContextTypeType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceContextRefBuilder extends AbstractNamingBuilder {
    private static final QName PERSISTENCE_CONTEXT_REF_QNAME = new QName(JEE_NAMESPACE, "persistence-context-ref");
    private static final QNameSet PERSISTENCE_CONTEXT_REF_QNAME_SET = QNameSet.singleton(PERSISTENCE_CONTEXT_REF_QNAME);
    private static final QName GER_PERSISTENCE_CONTEXT_REF_QNAME = GerPersistenceContextRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_PERSISTENCE_CONTEXT_REF_QNAME_SET = QNameSet.singleton(GER_PERSISTENCE_CONTEXT_REF_QNAME);
    private static final Set<String> PERSISTENCE_UNIT_INTERFACE_TYPES = Collections.singleton("org.apache.geronimo.persistence.PersistenceUnitGBean");
    private final AbstractNameQuery defaultPersistenceUnitAbstractNameQuery;

    public PersistenceContextRefBuilder(Environment defaultEnvironment, AbstractNameQuery defaultPersistenceUnitAbstractNameQuery) {
        super(defaultEnvironment);
        this.defaultPersistenceUnitAbstractNameQuery = defaultPersistenceUnitAbstractNameQuery;
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) throws DeploymentException {
        return plan != null && plan.selectChildren(PersistenceContextRefBuilder.GER_PERSISTENCE_CONTEXT_REF_QNAME_SET).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        List<PersistenceContextRefType> specPersistenceContextRefsUntyped = convert(specDD.selectChildren(PERSISTENCE_CONTEXT_REF_QNAME_SET), JEE_CONVERTER, PersistenceContextRefType.class, PersistenceContextRefType.type);
        Map<String, GerPersistenceContextRefType> gerPersistenceContextRefsUntyped = getGerPersistenceContextRefs(plan);
        for (PersistenceContextRefType persistenceContextRef : specPersistenceContextRefsUntyped) {
            String persistenceContextRefName = persistenceContextRef.getPersistenceContextRefName().getStringValue().trim();

            PersistenceContextTypeType persistenceContextType = persistenceContextRef.getPersistenceContextType();
            boolean transactionScoped = persistenceContextType == null || !persistenceContextType.getStringValue().equalsIgnoreCase("extended");

            PropertyType[] propertyTypes = persistenceContextRef.getPersistencePropertyArray();
            Map properties = new HashMap();
            for (PropertyType propertyType : propertyTypes) {
                String key = propertyType.getName().getStringValue();
                String value = propertyType.getValue().getStringValue();
                properties.put(key, value);
            }

            AbstractNameQuery persistenceUnitNameQuery;
            GerPersistenceContextRefType gerPersistenceContextRef = gerPersistenceContextRefsUntyped.remove(persistenceContextRefName);
            if (gerPersistenceContextRef != null) {
                persistenceUnitNameQuery = findPersistenceUnit(gerPersistenceContextRef);
                addProperties(gerPersistenceContextRef, properties);
            } else if (persistenceContextRef.isSetPersistenceUnitName()) {
                String persistenceUnitName = persistenceContextRef.getPersistenceUnitName().getStringValue().trim();
                persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
            } else {
                persistenceUnitNameQuery = defaultPersistenceUnitAbstractNameQuery;
            }

            try {
                localConfiguration.findGBeanData(persistenceUnitNameQuery);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery, e);
            }

            PersistenceContextReference reference = new PersistenceContextReference(localConfiguration.getId(), persistenceUnitNameQuery, transactionScoped, properties);

            ((Map) componentContext.get(JNDI_KEY)).put(ENV + persistenceContextRefName, reference);

        }

        for (GerPersistenceContextRefType gerPersistenceContextRef : gerPersistenceContextRefsUntyped.values()) {
            String persistenceContextRefName = gerPersistenceContextRef.getPersistenceContextRefName();
            boolean transactionScoped = !gerPersistenceContextRef.getPersistenceContextType().equals(GerPersistenceContextTypeType.EXTENDED);
            Map properties = new HashMap();
            addProperties(gerPersistenceContextRef, properties);


            AbstractNameQuery persistenceUnitNameQuery = findPersistenceUnit(gerPersistenceContextRef);

            try {
                localConfiguration.findGBeanData(persistenceUnitNameQuery);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery, e);
            }

            PersistenceContextReference reference = new PersistenceContextReference(localConfiguration.getId(), persistenceUnitNameQuery, transactionScoped, properties);

            getJndiContextMap(componentContext).put(ENV + persistenceContextRefName, reference);

        }
    }

    private void addProperties(GerPersistenceContextRefType persistenceContextRef, Map properties) {
        GerPropertyType[] propertyTypes = persistenceContextRef.getPropertyArray();
        for (GerPropertyType propertyType : propertyTypes) {
            String key = propertyType.getKey();
            String value = propertyType.getValue();
            properties.put(key, value);
        }
    }

    private Map<String, GerPersistenceContextRefType> getGerPersistenceContextRefs(XmlObject plan) throws DeploymentException {
        Map<String, GerPersistenceContextRefType> map = new HashMap<String, GerPersistenceContextRefType>();
        if (plan != null) {
            List<GerPersistenceContextRefType> refs = convert(plan.selectChildren(GER_PERSISTENCE_CONTEXT_REF_QNAME_SET), NAMING_CONVERTER, GerPersistenceContextRefType.class, GerPersistenceContextRefType.type);
            for (GerPersistenceContextRefType ref : refs) {
                map.put(ref.getPersistenceContextRefName().trim(), ref);
            }
        }
        return map;
    }

    private AbstractNameQuery findPersistenceUnit(GerPersistenceContextRefType persistenceContextRef) {
        AbstractNameQuery persistenceUnitNameQuery;
        if (persistenceContextRef.isSetPersistenceUnitName()) {
            String persistenceUnitName = persistenceContextRef.getPersistenceUnitName();
            persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
        } else {
            GerPatternType gbeanLocator = persistenceContextRef.getPattern();

            persistenceUnitNameQuery = buildAbstractNameQuery(gbeanLocator, null, null, PERSISTENCE_UNIT_INTERFACE_TYPES);
        }
        return persistenceUnitNameQuery;
    }

    public QNameSet getSpecQNameSet() {
        SchemaConversionUtils.registerNamespaceConversions(Collections.singletonMap(GER_PERSISTENCE_CONTEXT_REF_QNAME.getLocalPart(), new NamespaceElementConverter(GER_PERSISTENCE_CONTEXT_REF_QNAME.getNamespaceURI())));
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return GER_PERSISTENCE_CONTEXT_REF_QNAME_SET;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(PersistenceContextRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);
        infoBuilder.addAttribute("defaultPersistenceUnitAbstractNameQuery", AbstractNameQuery.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "defaultPersistenceUnitAbstractNameQuery"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
