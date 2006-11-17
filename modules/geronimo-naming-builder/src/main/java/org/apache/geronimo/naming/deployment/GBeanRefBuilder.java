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

package org.apache.geronimo.naming.deployment;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSet;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.naming.reference.GBeanReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerGbeanRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.NamingBuilder;

/**
 * @version $Rev$ $Date$
 */
public class GBeanRefBuilder implements NamingBuilder {
    private static final QName GBEAN_REF_QNAME = GerGbeanRefDocument.type.getDocumentElementName();
    private static final QNameSet GBEAN_REF_QNAME_SET = QNameSet.singleton(GBEAN_REF_QNAME);

    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) {
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        if (plan == null) {
            return;
        }
        XmlObject[] gbeanRefsUntyped = plan == null? NO_REFS: plan.selectChildren(GBEAN_REF_QNAME_SET);
        for (int i = 0; i < gbeanRefsUntyped.length; i++) {
            XmlObject gbeanRefUntyped = gbeanRefsUntyped[i];
            GerGbeanRefType gbeanRef = (GerGbeanRefType) gbeanRefUntyped.copy().changeType(GerGbeanRefType.type);
            if (gbeanRef == null) {
                throw new DeploymentException("Could not read gbeanRef " + gbeanRefUntyped + " as the correct xml type");
            }
            GerPatternType[] gbeanLocatorArray = gbeanRef.getPatternArray();

            String[] interfaceTypesArray = gbeanRef.getRefTypeArray();
            Set interfaceTypes = new HashSet(Arrays.asList(interfaceTypesArray));
            Set queries = new HashSet();
            for (int j = 0; j < gbeanLocatorArray.length; j++) {
                GerPatternType patternType = gbeanLocatorArray[j];
                AbstractNameQuery abstractNameQuery = ENCConfigBuilder.buildAbstractNameQuery(patternType, null, null, interfaceTypes);
                queries.add(abstractNameQuery);
            }

            GBeanData gBeanData;
            try {
                gBeanData = localConfiguration.findGBeanData(queries);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for queries " + queries, e);
            }

            if (interfaceTypes.isEmpty()) {
                interfaceTypes.add(gBeanData.getGBeanInfo().getClassName());
            }
            ClassLoader cl = module.getEarContext().getClassLoader();
            Class gBeanType;
            try {
                gBeanType = ClassLoading.loadClass(gBeanData.getGBeanInfo().getClassName(), cl);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Cannot load GBean class", e);
            }

            String refName = gbeanRef.getRefName();

            ((Map)componentContext.get(JNDI_KEY)).put(ENV + refName, new GBeanReference(localConfiguration.getId(), queries, gBeanType));

        }
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return GBEAN_REF_QNAME_SET;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(GBeanRefBuilder.class, NameFactory.MODULE_BUILDER);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
