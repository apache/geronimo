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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.JndiPlan;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.model.naming.GbeanRefType;
import org.apache.geronimo.j2ee.deployment.model.naming.PatternType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.naming.reference.GBeanReference;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class GBeanRefBuilder extends AbstractNamingBuilder {

    @Override
    public void buildNaming(JndiConsumer specDD, JndiPlan plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        if (plan == null) {
            return;
        }
        List<GbeanRefType> gbeanRefsUntyped = plan.getGBeanRef();
        for (GbeanRefType gbeanRef : gbeanRefsUntyped) {
            List<PatternType> gbeanLocatorArray = gbeanRef.getPattern();

            List<String> interfaceTypes = gbeanRef.getRefType();
            Set<AbstractNameQuery> queries = new HashSet<AbstractNameQuery>();
            for (PatternType patternType : gbeanLocatorArray) {
                AbstractNameQuery abstractNameQuery = ENCConfigBuilder.buildAbstractNameQuery(patternType, null, null, interfaceTypes);
                queries.add(abstractNameQuery);
            }

            GBeanData gBeanData;
            try {
                gBeanData = module.getEarContext().getConfiguration().findGBeanData(queries);
            } catch (GBeanNotFoundException e) {
                throw new DeploymentException("Could not resolve reference at deploy time for queries " + queries, e);
            }

            if (interfaceTypes.isEmpty()) {
                interfaceTypes.add(gBeanData.getGBeanInfo().getClassName());
            }
            Bundle bundle = module.getEarContext().getDeploymentBundle();
            Class gBeanType;
            try {
                gBeanType = ClassLoading.loadClass(gBeanData.getGBeanInfo().getClassName(), bundle);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Cannot load GBean class", e);
            }

            String refName = gbeanRef.getRefName();

            put(refName, new GBeanReference(module.getConfigId(), queries, gBeanType), module.getJndiContext(), Collections.<InjectionTarget>emptyList(), sharedContext);

        }
    }

//    @Override
//    public QNameSet getSpecQNameSet() {
//        return QNameSet.EMPTY;
//    }

//    @Override
//    public QNameSet getPlanQNameSet() {
//        return GBEAN_REF_QNAME_SET;
//    }

}
