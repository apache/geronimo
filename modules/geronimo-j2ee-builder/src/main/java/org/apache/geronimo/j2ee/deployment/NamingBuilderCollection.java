/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.j2ee.deployment;

import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
public class NamingBuilderCollection implements NamingBuilder {

    private final Collection builders;
    private final QName basePlanElementName;
    private QNameSet specQNames = QNameSet.EMPTY;
    private QNameSet planQNames = QNameSet.EMPTY;

    public NamingBuilderCollection(Collection builders, final QName basePlanElementName) {
        this.builders = builders == null ? Collections.EMPTY_SET : builders;
        this.basePlanElementName = basePlanElementName;
        if (builders instanceof ReferenceCollection) {
            ((ReferenceCollection) builders).addReferenceCollectionListener(new ReferenceCollectionListener() {

                public void memberAdded(ReferenceCollectionEvent event) {
                    addBuilder(event.getMember());
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                    Object builder = event.getMember();
                    QNameSet builderSpecQNames = ((NamingBuilder) builder).getSpecQNameSet();
                    specQNames = specQNames.intersect(builderSpecQNames.inverse());
                    QNameSet builderPlanQNames = ((NamingBuilder) builder).getPlanQNameSet();
                    planQNames = planQNames.intersect(builderPlanQNames.inverse());
                    XmlBeansUtil.unregisterSubstitutionGroupElements(basePlanElementName, builderPlanQNames);
                }
            });
        }
        for (Iterator iterator = this.builders.iterator(); iterator.hasNext();) {
            Object builder = iterator.next();
            addBuilder(builder);
        }
    }

    private void addBuilder(Object builder) {
        QNameSet builderSpecQNames = ((NamingBuilder) builder).getSpecQNameSet();
        QNameSet builderPlanQNames = ((NamingBuilder) builder).getPlanQNameSet();
        if (builderSpecQNames == null) {
            throw new IllegalStateException("Builder " + builder + " is missing spec qnames");
        }
        if (builderPlanQNames == null) {
            throw new IllegalStateException("Builder " + builder + " is missing plan qnames");
        }
        if (!specQNames.isDisjoint(builderSpecQNames) && !planQNames.isDisjoint(builderPlanQNames)) {
            throw new IllegalArgumentException("Duplicate builderSpecQNames in builder set: " + builderSpecQNames + " and duplicate builderPlanQNames in builder set: " + builderPlanQNames);
        }
        try {
            specQNames = specQNames.union(builderSpecQNames);
            planQNames = planQNames.union(builderPlanQNames);
        } catch (NullPointerException e) {
            throw (IllegalArgumentException) new IllegalArgumentException("could not merge qnamesets for builder " + builder).initCause(e);

        }
        //really?
        XmlBeansUtil.registerSubstitutionGroupElements(basePlanElementName, builderPlanQNames);
    }

    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) throws DeploymentException {
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            NamingBuilder namingBuilder = (NamingBuilder) iterator.next();
            namingBuilder.buildEnvironment(specDD, plan, environment);
        }
    }

    public void initContext(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module) throws DeploymentException {
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            NamingBuilder namingBuilder = (NamingBuilder) iterator.next();
            namingBuilder.initContext(specDD, plan, localConfiguration, remoteConfiguration, module);
        }
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localContext, Configuration remoteContext, Module module, Map componentContext) throws DeploymentException {
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            NamingBuilder namingBuilder = (NamingBuilder) iterator.next();
            namingBuilder.buildNaming(specDD, plan, localContext, remoteContext, module, componentContext);
        }
    }

    public QNameSet getSpecQNameSet() {
        return specQNames;
    }

    public QNameSet getPlanQNameSet() {
        return null;
    }
}
