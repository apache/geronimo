/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.j2ee.deployment.annotation;

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.xbeans.javaee6.DataSourceType;
import org.apache.geronimo.xbeans.javaee6.EjbJarType;
import org.apache.geronimo.xbeans.javaee6.EjbLocalRefType;
import org.apache.geronimo.xbeans.javaee6.EjbRefType;
import org.apache.geronimo.xbeans.javaee6.EntityBeanType;
import org.apache.geronimo.xbeans.javaee6.EnvEntryType;
import org.apache.geronimo.xbeans.javaee6.LifecycleCallbackType;
import org.apache.geronimo.xbeans.javaee6.MessageDestinationRefType;
import org.apache.geronimo.xbeans.javaee6.MessageDrivenBeanType;
import org.apache.geronimo.xbeans.javaee6.PersistenceContextRefType;
import org.apache.geronimo.xbeans.javaee6.PersistenceUnitRefType;
import org.apache.geronimo.xbeans.javaee6.ResourceEnvRefType;
import org.apache.geronimo.xbeans.javaee6.ResourceRefType;
import org.apache.geronimo.xbeans.javaee6.ServiceRefType;
import org.apache.geronimo.xbeans.javaee6.SessionBeanType;
import org.apache.xmlbeans.XmlObject;

/**
 * Wrapper class to encapsulate the EjbJarType class with an interface that the various
 * AnnotationHelpers can use
 * <p/>
 * <p><strong>Remaining ToDo(s):</strong>
 * <ul>
 * <li>None
 * </ul>
 *
 * @version $Rev $Date
 * @since Geronimo 2.0
 */
public class AnnotatedEjbJar implements AnnotatedApp {

    private final EjbJarType ejbJar;
    private List<EjbRefType> ambiguousEjbRefs;
    private AnnotatedApp delegate;


    public AnnotatedEjbJar(EjbJarType ejbJar) {
        this.ejbJar = ejbJar;
    }

    public void setBean(XmlObject bean) throws DeploymentException {
        if (bean instanceof EntityBeanType) {
            delegate = new EntityBean((EntityBeanType) bean);
        } else if (bean instanceof MessageDrivenBeanType) {
            delegate = new MessageDriveBean((MessageDrivenBeanType) bean);
        } else if (bean instanceof SessionBeanType) {
            delegate = new SessionBean((SessionBeanType) bean);
        } else {
            throw new DeploymentException("Unrecognized XmlBeans object: " + bean);
        }
    }

    /**
     * EjbJarType methods used for the @EJB, @EJBs annotations
     */
    public EjbLocalRefType[] getEjbLocalRefArray() {
        return delegate.getEjbLocalRefArray();
    }

    public EjbLocalRefType addNewEjbLocalRef() {
        return delegate.addNewEjbLocalRef();
    }

    public EjbRefType[] getEjbRefArray() {
        return delegate.getEjbRefArray();
    }

    public EjbRefType addNewEjbRef() {
        return delegate.addNewEjbRef();
    }


    /**
     * EjbJarType methods used for the @Resource, @Resources annotations
     */
    public EnvEntryType[] getEnvEntryArray() {
        return delegate.getEnvEntryArray();
    }

    public EnvEntryType addNewEnvEntry() {
        return delegate.addNewEnvEntry();
    }

    public ServiceRefType[] getServiceRefArray() {
        return delegate.getServiceRefArray();
    }

    public ServiceRefType addNewServiceRef() {
        return delegate.addNewServiceRef();
    }

    public ResourceRefType[] getResourceRefArray() {
        return delegate.getResourceRefArray();
    }

    public ResourceRefType addNewResourceRef() {
        return delegate.addNewResourceRef();
    }

    public MessageDestinationRefType[] getMessageDestinationRefArray() {
        return delegate.getMessageDestinationRefArray();
    }

    public MessageDestinationRefType addNewMessageDestinationRef() {
        return delegate.addNewMessageDestinationRef();
    }

    public ResourceEnvRefType[] getResourceEnvRefArray() {
        return delegate.getResourceEnvRefArray();
    }

    public ResourceEnvRefType addNewResourceEnvRef() {
        return delegate.addNewResourceEnvRef();
    }

    public LifecycleCallbackType[] getPostConstructArray() {
        return delegate.getPostConstructArray();
    }

    public LifecycleCallbackType addPostConstruct() {
        return delegate.addPostConstruct();
    }

    public LifecycleCallbackType[] getPreDestroyArray() {
        return delegate.getPreDestroyArray();
    }

    public LifecycleCallbackType addPreDestroy() {
        return delegate.addPreDestroy();
    }

    public PersistenceContextRefType[] getPersistenceContextRefArray() {
        return delegate.getPersistenceContextRefArray();
    }

    public PersistenceContextRefType addNewPersistenceContextRef() {
        return delegate.addNewPersistenceContextRef();
    }

    public PersistenceUnitRefType[] getPersistenceUnitRefArray() {
        return delegate.getPersistenceUnitRefArray();
    }

    public PersistenceUnitRefType addNewPersistenceUnitRef() {
        return delegate.addNewPersistenceUnitRef();
    }

    public DataSourceType[] getDataSourceArray() {
        return delegate.getDataSourceArray();
    }
    
    public DataSourceType addNewDataSource() {
        return delegate.addNewDataSource();
    }
    
    public String getComponentType() {
        return null;
    }

    /**
     * ejbJar getter
     *
     * @return String representation of ejbJar
     */
    public String toString() {
        return ejbJar.xmlText();
    }


    public EjbJarType getEjbJar() {
        return ejbJar;
    }

    /**
     * ambiguousRefs getter
     * <p/>
     * <p>There is no corresponding setter method. To add a new item to the list do:
     * <pre>
     *    getAmbiguousEjbRefs().add(ejbRef);
     * </pre>
     *
     * @return ambiguousRefs list
     */
    public List<EjbRefType> getAmbiguousEjbRefs() {
        if (ambiguousEjbRefs == null) {
            ambiguousEjbRefs = new ArrayList<EjbRefType>();
        }
        return this.ambiguousEjbRefs;
    }

    public static class EntityBean implements AnnotatedApp {
        private final EntityBeanType bean;

        public EntityBean(EntityBeanType bean) {
            this.bean = bean;
        }

        public EjbLocalRefType[] getEjbLocalRefArray() {
            return bean.getEjbLocalRefArray();
        }

        public EjbLocalRefType addNewEjbLocalRef() {
            return bean.addNewEjbLocalRef();
        }

        public EjbRefType[] getEjbRefArray() {
            return bean.getEjbRefArray();
        }

        public EjbRefType addNewEjbRef() {
            return bean.addNewEjbRef();
        }

        public EnvEntryType[] getEnvEntryArray() {
            return bean.getEnvEntryArray();
        }

        public EnvEntryType addNewEnvEntry() {
            return bean.addNewEnvEntry();
        }

        public ServiceRefType[] getServiceRefArray() {
            return bean.getServiceRefArray();
        }

        public ServiceRefType addNewServiceRef() {
            return bean.addNewServiceRef();
        }

        public ResourceRefType[] getResourceRefArray() {
            return bean.getResourceRefArray();
        }

        public ResourceRefType addNewResourceRef() {
            return bean.addNewResourceRef();
        }

        public MessageDestinationRefType[] getMessageDestinationRefArray() {
            return bean.getMessageDestinationRefArray();
        }

        public MessageDestinationRefType addNewMessageDestinationRef() {
            return bean.addNewMessageDestinationRef();
        }

        public ResourceEnvRefType[] getResourceEnvRefArray() {
            return bean.getResourceEnvRefArray();
        }

        public ResourceEnvRefType addNewResourceEnvRef() {
            return bean.addNewResourceEnvRef();
        }

        public String toString() {
            return bean.xmlText();
        }

        public List<EjbRefType> getAmbiguousEjbRefs() {
            throw new AssertionError("don't call this");
        }

        public LifecycleCallbackType[] getPostConstructArray() {
            return bean.getPostConstructArray();
        }

        public LifecycleCallbackType addPostConstruct() {
            return bean.addNewPostConstruct();
        }

        public LifecycleCallbackType[] getPreDestroyArray() {
            return bean.getPreDestroyArray();
        }

        public LifecycleCallbackType addPreDestroy() {
            return bean.addNewPreDestroy();
        }

        public PersistenceContextRefType[] getPersistenceContextRefArray() {
            return bean.getPersistenceContextRefArray();
        }

        public PersistenceContextRefType addNewPersistenceContextRef() {
            return bean.addNewPersistenceContextRef();
        }

        public PersistenceUnitRefType[] getPersistenceUnitRefArray() {
            return bean.getPersistenceUnitRefArray();
        }

        public PersistenceUnitRefType addNewPersistenceUnitRef() {
            return bean.addNewPersistenceUnitRef();
        }

        public DataSourceType[] getDataSourceArray() {
            return bean.getDataSourceArray();
        }
        
        public DataSourceType addNewDataSource() {
            return bean.addNewDataSource();
        }
        
        public String getComponentType() {
            return bean.getEjbClass().getStringValue().trim();
        }
    }

    public static class MessageDriveBean implements AnnotatedApp {
        private final MessageDrivenBeanType bean;

        public MessageDriveBean(MessageDrivenBeanType bean) {
            this.bean = bean;
        }

        public EjbLocalRefType[] getEjbLocalRefArray() {
            return bean.getEjbLocalRefArray();
        }

        public EjbLocalRefType addNewEjbLocalRef() {
            return bean.addNewEjbLocalRef();
        }

        public EjbRefType[] getEjbRefArray() {
            return bean.getEjbRefArray();
        }

        public EjbRefType addNewEjbRef() {
            return bean.addNewEjbRef();
        }

        public EnvEntryType[] getEnvEntryArray() {
            return bean.getEnvEntryArray();
        }

        public EnvEntryType addNewEnvEntry() {
            return bean.addNewEnvEntry();
        }

        public ServiceRefType[] getServiceRefArray() {
            return bean.getServiceRefArray();
        }

        public ServiceRefType addNewServiceRef() {
            return bean.addNewServiceRef();
        }

        public ResourceRefType[] getResourceRefArray() {
            return bean.getResourceRefArray();
        }

        public ResourceRefType addNewResourceRef() {
            return bean.addNewResourceRef();
        }

        public MessageDestinationRefType[] getMessageDestinationRefArray() {
            return bean.getMessageDestinationRefArray();
        }

        public MessageDestinationRefType addNewMessageDestinationRef() {
            return bean.addNewMessageDestinationRef();
        }

        public ResourceEnvRefType[] getResourceEnvRefArray() {
            return bean.getResourceEnvRefArray();
        }

        public ResourceEnvRefType addNewResourceEnvRef() {
            return bean.addNewResourceEnvRef();
        }

        public DataSourceType[] getDataSourceArray() {
            return bean.getDataSourceArray();
        }
        
        public DataSourceType addNewDataSource() {
            return bean.addNewDataSource();
        }
        
        public String toString() {
            return bean.xmlText();
        }

        public List<EjbRefType> getAmbiguousEjbRefs() {
            throw new AssertionError("don't call this");
        }

        public LifecycleCallbackType[] getPostConstructArray() {
            return bean.getPostConstructArray();
        }

        public LifecycleCallbackType addPostConstruct() {
            return bean.addNewPostConstruct();
        }

        public LifecycleCallbackType[] getPreDestroyArray() {
            return bean.getPreDestroyArray();
        }

        public LifecycleCallbackType addPreDestroy() {
            return bean.addNewPreDestroy();
        }

        public PersistenceContextRefType[] getPersistenceContextRefArray() {
            return bean.getPersistenceContextRefArray();
        }

        public PersistenceContextRefType addNewPersistenceContextRef() {
            return bean.addNewPersistenceContextRef();
        }

        public PersistenceUnitRefType[] getPersistenceUnitRefArray() {
            return bean.getPersistenceUnitRefArray();
        }

        public PersistenceUnitRefType addNewPersistenceUnitRef() {
            return bean.addNewPersistenceUnitRef();
        }
       
        public String getComponentType() {
            return bean.getEjbClass().getStringValue().trim();
        }
    }

    public static class SessionBean implements AnnotatedApp {
        private final SessionBeanType bean;

        public SessionBean(SessionBeanType bean) {
            this.bean = bean;
        }

        public EjbLocalRefType[] getEjbLocalRefArray() {
            return bean.getEjbLocalRefArray();
        }

        public EjbLocalRefType addNewEjbLocalRef() {
            return bean.addNewEjbLocalRef();
        }

        public EjbRefType[] getEjbRefArray() {
            return bean.getEjbRefArray();
        }

        public EjbRefType addNewEjbRef() {
            return bean.addNewEjbRef();
        }

        public EnvEntryType[] getEnvEntryArray() {
            return bean.getEnvEntryArray();
        }

        public EnvEntryType addNewEnvEntry() {
            return bean.addNewEnvEntry();
        }

        public ServiceRefType[] getServiceRefArray() {
            return bean.getServiceRefArray();
        }

        public ServiceRefType addNewServiceRef() {
            return bean.addNewServiceRef();
        }

        public ResourceRefType[] getResourceRefArray() {
            return bean.getResourceRefArray();
        }

        public ResourceRefType addNewResourceRef() {
            return bean.addNewResourceRef();
        }

        public MessageDestinationRefType[] getMessageDestinationRefArray() {
            return bean.getMessageDestinationRefArray();
        }

        public MessageDestinationRefType addNewMessageDestinationRef() {
            return bean.addNewMessageDestinationRef();
        }

        public ResourceEnvRefType[] getResourceEnvRefArray() {
            return bean.getResourceEnvRefArray();
        }

        public ResourceEnvRefType addNewResourceEnvRef() {
            return bean.addNewResourceEnvRef();
        }

        public String toString() {
            return bean.xmlText();
        }

        public List<EjbRefType> getAmbiguousEjbRefs() {
            throw new AssertionError("don't call this");
        }

        public LifecycleCallbackType[] getPostConstructArray() {
            return bean.getPostConstructArray();
        }

        public LifecycleCallbackType addPostConstruct() {
            return bean.addNewPostConstruct();
        }

        public LifecycleCallbackType[] getPreDestroyArray() {
            return bean.getPreDestroyArray();
        }

        public LifecycleCallbackType addPreDestroy() {
            return bean.addNewPreDestroy();
        }

        public PersistenceContextRefType[] getPersistenceContextRefArray() {
            return bean.getPersistenceContextRefArray();
        }

        public PersistenceContextRefType addNewPersistenceContextRef() {
            return bean.addNewPersistenceContextRef();
        }

        public PersistenceUnitRefType[] getPersistenceUnitRefArray() {
            return bean.getPersistenceUnitRefArray();
        }

        public PersistenceUnitRefType addNewPersistenceUnitRef() {
            return bean.addNewPersistenceUnitRef();
        }

        public DataSourceType[] getDataSourceArray() {
            return bean.getDataSourceArray();
        }
        
        public DataSourceType addNewDataSource() {
            return bean.addNewDataSource();
        }
        
        public String getComponentType() {
            return bean.getEjbClass().getStringValue().trim();
        }
    }
}
