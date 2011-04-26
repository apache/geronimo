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
package org.apache.geronimo.deployment;

import java.util.Collection;
import java.util.Collections;

import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.xmlbeans.QNameSet;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractBuilderCollection<T extends AbstractNamespaceBuilder> {
    protected final Collection<T> builders;
    protected QNameSet specQNames = QNameSet.EMPTY;
    protected QNameSet planQNames = QNameSet.EMPTY;

    protected AbstractBuilderCollection(Collection<T> builders) {
        this.builders = builders == null ? Collections.<T>emptySet() : builders;
        if (builders instanceof ReferenceCollection) {
            ((ReferenceCollection) builders).addReferenceCollectionListener(new ReferenceCollectionListener() {

                public void memberAdded(ReferenceCollectionEvent event) {
                    addBuilder((T) event.getMember());
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                    T builder = (T) event.getMember();
                    QNameSet builderSpecQNames = builder.getSpecQNameSet();
                    specQNames = specQNames.intersect(builderSpecQNames.inverse());
                    QNameSet builderPlanQNames = builder.getPlanQNameSet();
                    planQNames = planQNames.intersect(builderPlanQNames.inverse());
                    XmlBeansUtil.unregisterSubstitutionGroupElements(builder.getBaseQName(), builderPlanQNames);
                }
            });
        }
        for (T builder : this.builders) {
            addBuilder(builder);
        }
    }


    protected void addBuilder(T builder) {
        QNameSet builderSpecQNames = builder.getSpecQNameSet();
        QNameSet builderPlanQNames = builder.getPlanQNameSet();
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
        XmlBeansUtil.registerSubstitutionGroupElements(builder.getBaseQName(), builderPlanQNames);
    }

    public QNameSet getSpecQNameSet() {
        return specQNames;
    }

    public QNameSet getPlanQNameSet() {
        return null;
    }
    
    public String toString(){
        
        return builders.toString();
    }
}
