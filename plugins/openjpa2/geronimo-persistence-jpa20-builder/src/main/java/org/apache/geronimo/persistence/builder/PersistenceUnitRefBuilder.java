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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;
import org.apache.geronimo.j2ee.deployment.annotation.PersistenceUnitAnnotationHelper;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.PersistenceUnitReference;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceUnitRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerPersistenceUnitRefType;
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
    private final boolean strictMatching;


    public PersistenceUnitRefBuilder(Environment defaultEnvironment, AbstractNameQuery defaultPersistenceUnitAbstractNameQuery, boolean strictMatching) {
        super(defaultEnvironment);
        this.defaultPersistenceUnitAbstractNameQuery = defaultPersistenceUnitAbstractNameQuery;
        this.strictMatching = strictMatching;
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) throws DeploymentException {
        if (specDD != null && specDD.selectChildren(PersistenceUnitRefBuilder.PERSISTENCE_UNIT_REF_QNAME_SET).length > 0) {
            return true;
        }
        return plan != null && plan.selectChildren(PersistenceUnitRefBuilder.GER_PERSISTENCE_UNIT_REF_QNAME_SET).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
        Configuration localConfiguration = module.getEarContext().getConfiguration();
        // Discover and process any @PersistenceUnitRef annotations (if !metadata-complete)
        if (module.getClassFinder() != null) {
            processAnnotations(module);
        }

        List<PersistenceUnitRefType> specPersistenceUnitRefsUntyped = convert(specDD.selectChildren(PersistenceUnitRefBuilder.PERSISTENCE_UNIT_REF_QNAME_SET), JEE_CONVERTER, PersistenceUnitRefType.class, PersistenceUnitRefType.type);
        Map<String, GerPersistenceUnitRefType> gerPersistenceUnitRefsUntyped = getGerPersistenceUnitRefs(plan);
        List<DeploymentException> problems = new ArrayList<DeploymentException>();
        for (PersistenceUnitRefType persistenceUnitRef : specPersistenceUnitRefsUntyped) {
            try {
                String persistenceUnitRefName = persistenceUnitRef.getPersistenceUnitRefName().getStringValue().trim();

                addInjections(persistenceUnitRefName, persistenceUnitRef.getInjectionTargetArray(), componentContext);
                AbstractNameQuery persistenceUnitNameQuery;
                GerPersistenceUnitRefType gerPersistenceUnitRef = gerPersistenceUnitRefsUntyped.remove(persistenceUnitRefName);
                if (gerPersistenceUnitRef != null) {
                    persistenceUnitNameQuery = findPersistenceUnit(gerPersistenceUnitRef);
                    checkForGBean(localConfiguration, persistenceUnitNameQuery, true);
                } else if (persistenceUnitRef.isSetPersistenceUnitName() && persistenceUnitRef.getPersistenceUnitName().getStringValue().trim().length() > 0) {
                    String persistenceUnitName = persistenceUnitRef.getPersistenceUnitName().getStringValue().trim();
                    persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
                    if (!checkForGBean(localConfiguration, persistenceUnitNameQuery, strictMatching)) {
                        persistenceUnitName = "persistence/" + persistenceUnitName;
                        persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.singletonMap("name", persistenceUnitName), PERSISTENCE_UNIT_INTERFACE_TYPES);
                        checkForGBean(localConfiguration, persistenceUnitNameQuery, true);
                    }
                } else {
                    persistenceUnitNameQuery = new AbstractNameQuery(null, Collections.EMPTY_MAP, PERSISTENCE_UNIT_INTERFACE_TYPES);
                    Set<AbstractNameQuery> patterns = Collections.singleton(persistenceUnitNameQuery);
                    LinkedHashSet<GBeanData> gbeans = localConfiguration.findGBeanDatas(localConfiguration, patterns);
                    persistenceUnitNameQuery = checkForDefaultPersistenceUnit(gbeans);
                    if (gbeans.isEmpty()) {
                        gbeans = localConfiguration.findGBeanDatas(patterns);
                        persistenceUnitNameQuery = checkForDefaultPersistenceUnit(gbeans);

                        if (gbeans.isEmpty()) {
                            if (defaultPersistenceUnitAbstractNameQuery == null) {
                                throw new DeploymentException("No default PersistenceUnit specified, and none located");
                            }
                            persistenceUnitNameQuery = defaultPersistenceUnitAbstractNameQuery;
                        }
                    }
                }
                checkForGBean(localConfiguration, persistenceUnitNameQuery, true);

                PersistenceUnitReference reference = new PersistenceUnitReference(module.getConfigId(), persistenceUnitNameQuery);

                put(persistenceUnitRefName, reference, NamingBuilder.JNDI_KEY.get(componentContext));
            } catch (DeploymentException e) {
                problems.add(e);
            }

        }


        for (GerPersistenceUnitRefType gerPersistenceUnitRef : gerPersistenceUnitRefsUntyped.values()) {
            try {
                String PersistenceUnitRefName = gerPersistenceUnitRef.getPersistenceUnitRefName();

                AbstractNameQuery persistenceUnitNameQuery = findPersistenceUnit(gerPersistenceUnitRef);

                checkForGBean(localConfiguration, persistenceUnitNameQuery, true);

                PersistenceUnitReference reference = new PersistenceUnitReference(module.getConfigId(), persistenceUnitNameQuery);

                put(PersistenceUnitRefName, reference, NamingBuilder.JNDI_KEY.get(componentContext));
            } catch (DeploymentException e) {
                problems.add(e);
            }

        }
        if (!problems.isEmpty()) {
            //TODO make DeploymentException accept a list of exceptions as causes.
            throw new DeploymentException("At least one deployment problem:" + problems);
        }
    }

    private AbstractNameQuery checkForDefaultPersistenceUnit(LinkedHashSet<GBeanData> gbeans) throws DeploymentException {
        AbstractNameQuery persistenceUnitNameQuery = null;
        for (java.util.Iterator it = gbeans.iterator(); it.hasNext();) {
            GBeanData gbean = (GBeanData) it.next();
            AbstractName name = gbean.getAbstractName();
            Map nameMap = name.getName();
            if ("cmp".equals(nameMap.get("name"))) {
                it.remove();
            } else {
                persistenceUnitNameQuery = new AbstractNameQuery(name);
            }
        }
        if (gbeans.size() > 1) {
            throw new DeploymentException("Too many matches for no-name persistence unit: " + gbeans);
        }
        return persistenceUnitNameQuery;
    }

    private boolean checkForGBean(Configuration localConfiguration, AbstractNameQuery persistenceUnitNameQuery, boolean complainIfMissing) throws DeploymentException {
        try {
            localConfiguration.findGBeanData(persistenceUnitNameQuery);
            return true;
        } catch (GBeanNotFoundException e) {
            if (complainIfMissing || e.hasMatches()) {
                String reason = e.hasMatches() ? "More than one GBean reference found." : "No GBean references found.";
                throw new DeploymentException("Could not resolve reference at deploy time for query " + persistenceUnitNameQuery + ". " + reason, e);
            }
            return false;
        }
    }

    private void processAnnotations(Module module) throws DeploymentException {
        // Process all the annotations for this naming builder type
        PersistenceUnitAnnotationHelper.processAnnotations(module.getAnnotatedApp(), module.getClassFinder());
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
            for (GerPersistenceUnitRefType ref : refs) {
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
        infoBuilder.addAttribute("strictMatching", boolean.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "defaultPersistenceUnitAbstractNameQuery", "strictMatching"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return PersistenceUnitRefBuilder.GBEAN_INFO;
    }
}
