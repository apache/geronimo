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
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class PersistenceContextRefBuilder extends AbstractNamingBuilder {
    private static final QName PERSISTENCE_CONTEXT_REF_QNAME = GerPersistenceContextRefDocument.type.getDocumentElementName();
    private static final QNameSet PERSISTENCE_CONTEXT_REF_QNAME_SET = QNameSet.singleton(PERSISTENCE_CONTEXT_REF_QNAME);

    public PersistenceContextRefBuilder(Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) throws DeploymentException {
        return getPersistenceContextRefs(plan).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] persistenceContextRefsUntyped = getPersistenceContextRefs(plan);
        for (int i = 0; i < persistenceContextRefsUntyped.length; i++) {
            GerPersistenceContextRefType persistenceContextRef = (GerPersistenceContextRefType) persistenceContextRefsUntyped[i];
            if (persistenceContextRef == null) {
                throw new DeploymentException("Could not read persistenceContextRef number " + i + " as the correct xml type");
            }
            String persistenceContextRefName = persistenceContextRef.getPersistenceContextRefName();
            boolean transactionScoped = !persistenceContextRef.getPersistenceContextType().equals(GerPersistenceContextTypeType.EXTENDED);
            GerPropertyType[] propertyTypes = persistenceContextRef.getPropertyArray();
            Map properties = new HashMap();
            for (int j = 0; j < propertyTypes.length; j++) {
                GerPropertyType propertyType = propertyTypes[j];
                String key = propertyType.getKey();
                String value = propertyType.getValue();
                properties.put(key, value);
            }


            Set interfaceTypes = Collections.singleton("org.apache.geronimo.persistence.PersistenceUnitGBean");
            AbstractNameQuery persistenceUnitNameQuery;
            if (persistenceContextRef.isSetPersistenceUnitName()) {
                String persistenceUnitName = persistenceContextRef.getPersistenceUnitName();
                persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), interfaceTypes);
            } else {
                GerPatternType gbeanLocator = persistenceContextRef.getPattern();

                persistenceUnitNameQuery = buildAbstractNameQuery(gbeanLocator, null, null, interfaceTypes);
            }

            try {
                localConfiguration.findGBeanData(persistenceUnitNameQuery);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery, e);
            }

            PersistenceContextReference reference = new PersistenceContextReference(localConfiguration.getId(), persistenceUnitNameQuery, transactionScoped, properties);

            getJndiContextMap(componentContext).put(ENV + persistenceContextRefName, reference);

        }
    }

    public QNameSet getSpecQNameSet() {
        SchemaConversionUtils.registerNamespaceConversions(Collections.singletonMap(PERSISTENCE_CONTEXT_REF_QNAME.getLocalPart(), new NamespaceElementConverter(PERSISTENCE_CONTEXT_REF_QNAME.getNamespaceURI())));
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return PERSISTENCE_CONTEXT_REF_QNAME_SET;
    }

    private XmlObject[] getPersistenceContextRefs(XmlObject plan) throws DeploymentException {
        return plan == null? NO_REFS: convert(plan.selectChildren(PersistenceContextRefBuilder.PERSISTENCE_CONTEXT_REF_QNAME_SET), NAMING_CONVERTER, GerPersistenceContextRefType.type);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(PersistenceContextRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[] {"defaultEnvironment"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
