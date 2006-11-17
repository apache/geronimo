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

import java.util.Collection;
import java.util.Map;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.QNameSet;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.CorbaGBeanNameSource;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.reference.ORBReference;
import org.apache.geronimo.naming.reference.HandleDelegateReference;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.SingleElementCollection;

/**
 * TODO consider if this should be in openejb-builder module or a hypothetical corba-builder module
 * @version $Rev$ $Date$
 */
public class CorbaRefBuilder extends AbstractNamingBuilder {

    private final SingleElementCollection corbaGBeanNameSourceCollection;

    public CorbaRefBuilder(Collection corbaGBeanNameSource) {
        this.corbaGBeanNameSourceCollection = new SingleElementCollection(corbaGBeanNameSource);
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        CorbaGBeanNameSource corbaGBeanNameSource = (CorbaGBeanNameSource) corbaGBeanNameSourceCollection.getElement();
        if (corbaGBeanNameSource != null) {
            AbstractNameQuery corbaName = corbaGBeanNameSource.getCorbaGBeanName();
            if (corbaName != null) {
                Artifact moduleId = localConfiguration.getId();
                componentContext.put("ORB", new ORBReference(moduleId, corbaName));
                componentContext.put("HandleDelegate", new HandleDelegateReference(moduleId, corbaName));
            }
        }
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CorbaRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addReference("CorbaGBeanNameSource", CorbaGBeanNameSource.class);
        infoBuilder.setConstructor(new String[] {"CorbaGBeanNameSource"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
