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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.AbstractBuilderCollection;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.openejb.jee.DataSource;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class NamingBuilderCollection extends AbstractBuilderCollection<NamingBuilder> implements NamingBuilder {

    public NamingBuilderCollection(@ParamReference(name = "NamingBuilders", namingType = NameFactory.MODULE_BUILDER)Collection<NamingBuilder> builders) {
        super(builders);
    }

    public void buildEnvironment(JndiConsumer specDD, XmlObject plan, Environment environment) throws DeploymentException {
        if(specDD == null){
            // java ee 5 and 6 might not have spec DD, adding this to avoid the NPE.
            specDD = new JndiConsumerNonNull();
        }
        
        for (NamingBuilder namingBuilder : getSortedBuilders()) {
            namingBuilder.buildEnvironment(specDD, plan, environment);
        }
    }

    public void initContext(JndiConsumer specDD, XmlObject plan, Module module) throws DeploymentException {
        
        if(specDD == null){
            // java ee 5 and 6 might not have spec DD, adding this to avoid the NPE.
            specDD = new JndiConsumerNonNull();
        }
        
        for (NamingBuilder namingBuilder : getSortedBuilders()) {
            namingBuilder.initContext(specDD, plan, module);
        }
    }

    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        
        if(specDD == null){
            // java ee 5 and 6 might not have spec DD, adding this to avoid the NPE.
            specDD = new JndiConsumerNonNull();
        }
        
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
    
    private final class JndiConsumerNonNull implements JndiConsumer {
        @Override
        public String getJndiConsumerName() {
            return null;
        }

        @Override
        public Collection<EnvEntry> getEnvEntry() {
            return new ArrayList<EnvEntry>();
        }

        @Override
        public Map<String, EnvEntry> getEnvEntryMap() {
            return new HashMap<String, EnvEntry>();
        }

        @Override
        public Collection<EjbRef> getEjbRef() {
            return new ArrayList<EjbRef>();
        }

        @Override
        public Map<String, EjbRef> getEjbRefMap() {
             return new HashMap<String, EjbRef>();
        }

        @Override
        public Collection<EjbLocalRef> getEjbLocalRef() {
            return new ArrayList<EjbLocalRef>();
        }

        @Override
        public Map<String, EjbLocalRef> getEjbLocalRefMap() {
            return new HashMap<String, EjbLocalRef>();
        }

        @Override
        public Collection<ServiceRef> getServiceRef() {
            return new ArrayList<ServiceRef>();
        }

        @Override
        public Map<String, ServiceRef> getServiceRefMap() {
            return new HashMap<String, ServiceRef>();
        }

        @Override
        public Collection<ResourceRef> getResourceRef() {
            return new ArrayList<ResourceRef>();
        }

        @Override
        public Map<String, ResourceRef> getResourceRefMap() {
            return new HashMap<String, ResourceRef>();
        }

        @Override
        public Collection<ResourceEnvRef> getResourceEnvRef() {
            return new ArrayList<ResourceEnvRef>();
        }

        @Override
        public Map<String, ResourceEnvRef> getResourceEnvRefMap() {
            return new HashMap<String, ResourceEnvRef>();
        }

        @Override
        public Collection<MessageDestinationRef> getMessageDestinationRef() {
            return new ArrayList<MessageDestinationRef>();
        }

        @Override
        public Map<String, MessageDestinationRef> getMessageDestinationRefMap() {
            return new HashMap<String, MessageDestinationRef>();
        }

        @Override
        public Collection<PersistenceContextRef> getPersistenceContextRef() {
            return new ArrayList<PersistenceContextRef>();
        }

        @Override
        public Map<String, PersistenceContextRef> getPersistenceContextRefMap() {
            return new HashMap<String, PersistenceContextRef>();
        }

        @Override
        public Collection<PersistenceUnitRef> getPersistenceUnitRef() {
            return new ArrayList<PersistenceUnitRef>();
        }

        @Override
        public Map<String, PersistenceUnitRef> getPersistenceUnitRefMap() {
            return new HashMap<String, PersistenceUnitRef>();
        }

        @Override
        public Collection<DataSource> getDataSource() {
            return new ArrayList<DataSource>();
        }

        @Override
        public Map<String, DataSource> getDataSourceMap() {
            return new HashMap<String, DataSource>();
        }

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
        return new QName("foo");
    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY;
    }

}
