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

package org.apache.geronimo.corba.deployment;

import java.util.Collection;
import java.util.Map;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSet;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.CorbaGBeanNameSource;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.reference.ORBReference;
import org.apache.geronimo.naming.reference.HandleDelegateReference;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.SingleElementCollection;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class CorbaRefBuilder extends AbstractNamingBuilder {

    private final SingleElementCollection<CorbaGBeanNameSource> corbaGBeanNameSourceCollection;

    public CorbaRefBuilder(@ParamAttribute(name = "defaultEnvironment")Environment defaultEnvironment, 
                           @ParamReference(name="CorbaGBeanNameSource")Collection<CorbaGBeanNameSource> corbaGBeanNameSource) {
        super(defaultEnvironment);
        this.corbaGBeanNameSourceCollection = new SingleElementCollection<CorbaGBeanNameSource>(corbaGBeanNameSource);
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) throws DeploymentException {
//        if (OpenEjbCorbaRefBuilder.hasCssRefs(plan) || TSSLinkBuilder.hasTssLinks(plan)) {
            return true;
//        }
//        return false;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        if (matchesDefaultEnvironment(module.getEnvironment())) {
            CorbaGBeanNameSource corbaGBeanNameSource = corbaGBeanNameSourceCollection.getElement();
            if (corbaGBeanNameSource != null) {
                AbstractNameQuery corbaName = corbaGBeanNameSource.getCorbaGBeanName();
                if (corbaName != null) {
                    Artifact[] moduleId = module.getConfigId();
                    put("env/ORB", new ORBReference(moduleId, corbaName), JNDI_KEY.get(sharedContext));
                    put("env/HandleDelegate", new HandleDelegateReference(moduleId, corbaName), JNDI_KEY.get(sharedContext));
                }
            }
        }
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

}
