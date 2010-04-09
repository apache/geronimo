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

package org.apache.geronimo.j2ee.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.AbstractBuilderCollection;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class NamingBuilderCollection extends AbstractBuilderCollection<NamingBuilder> implements NamingBuilder {

    public NamingBuilderCollection(@ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER)Collection<NamingBuilder> builders) {
        super(builders);
    }

    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) throws DeploymentException {
        for (NamingBuilder namingBuilder : getSortedBuilders()) {
            namingBuilder.buildEnvironment(specDD, plan, environment);
        }
    }

    public void initContext(XmlObject specDD, XmlObject plan, Module module) throws DeploymentException {
        for (NamingBuilder namingBuilder : getSortedBuilders()) {
            namingBuilder.initContext(specDD, plan, module);
        }
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        for (NamingBuilder namingBuilder : getSortedBuilders()) {
            if (EARConfigBuilder.createPlanMode.get().booleanValue()) {
                try {
                    namingBuilder.buildNaming(specDD, plan, module, sharedContext);
                } catch (Exception e) {
                    // ignore exceptions & continue processing with rest of the builders
                }
            } else {
                namingBuilder.buildNaming(specDD, plan, module, sharedContext);
            }
        }
    }
    
    private List<NamingBuilder> getSortedBuilders() {
        List<NamingBuilder> list = new ArrayList<NamingBuilder>(this.builders);
        Collections.sort(list, new NamingBuilderComparator());
        return list;        
    }
    
    private static class NamingBuilderComparator implements Comparator<NamingBuilder> {
        public int compare(NamingBuilder o1, NamingBuilder o2) {
            return o1.getPriority() - o2.getPriority();
        }
    }
    
    public int getPriority() {
        return NORMAL_PRIORITY;
    }

    public QName getBaseQName() {
        throw new IllegalStateException("Don't call this");
    }

}
