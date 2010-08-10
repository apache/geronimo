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

package org.apache.geronimo.datasource.deployment;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.connector.deployment.ConnectorModuleBuilder;
import org.apache.geronimo.datasource.DataSourceDescription;
import org.apache.geronimo.datasource.DataSourceGBean;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.JndiReference;
import org.apache.openejb.jee.DataSource;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.IsolationLevel;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.Property;
import org.apache.openejb.jee.Text;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class DataSourceBuilder extends AbstractNamingBuilder {

    private static final Logger log = LoggerFactory.getLogger(DataSourceBuilder.class);

    private final int defaultMaxSize;
    private final int defaultMinSize;
    private final int defaultBlockingTimeoutMilliseconds;
    private final int defaultIdleTimeoutMinutes;
    private final boolean defaultXATransactionCaching;
    private final boolean defaultXAThreadCaching;
    
    private final PackageAdmin packageAdmin;
    private final QNameSet dataSourceQNameSet;

    public DataSourceBuilder(@ParamAttribute(name = "eeNamespaces") String[] eeNamespaces,
                             @ParamAttribute(name = "defaultMaxSize") int defaultMaxSize,
                             @ParamAttribute(name = "defaultMinSize") int defaultMinSize,
                             @ParamAttribute(name = "defaultBlockingTimeoutMilliseconds") int defaultBlockingTimeoutMilliseconds,
                             @ParamAttribute(name = "defaultIdleTimeoutMinutes") int defaultIdleTimeoutMinutes,
                             @ParamAttribute(name = "defaultXATransactionCaching") boolean defaultXATransactionCaching,
                             @ParamAttribute(name = "defaultXAThreadCaching") boolean defaultXAThreadCaching,
                             @ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle) {
        this.defaultMaxSize = defaultMaxSize;
        this.defaultMinSize = defaultMinSize;
        this.defaultBlockingTimeoutMilliseconds = defaultBlockingTimeoutMilliseconds;
        this.defaultIdleTimeoutMinutes = defaultIdleTimeoutMinutes;
        this.defaultXATransactionCaching = defaultXATransactionCaching;
        this.defaultXAThreadCaching = defaultXAThreadCaching;
        
        ServiceReference sr = bundle.getBundleContext().getServiceReference(PackageAdmin.class.getName());
        this.packageAdmin = (PackageAdmin) bundle.getBundleContext().getService(sr);
        
        this.dataSourceQNameSet = buildQNameSet(eeNamespaces, "data-source");
    }
    
    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
                        
        // step 1: process annotations and update deployment descriptor
        if ((module != null) && (module.getClassFinder() != null)) {

            List<Class> classes;        
            classes = module.getClassFinder().findAnnotatedClasses(DataSourceDefinitions.class);
            if (classes != null) {
                for (Class clazz : classes) {
                    DataSourceDefinitions dsDefinitions = (DataSourceDefinitions) clazz.getAnnotation(DataSourceDefinitions.class);
                    for (DataSourceDefinition dsDefinition : dsDefinitions.value()) {
                        processDefinition(dsDefinition, specDD);
                    }

                }
            }
            classes = module.getClassFinder().findAnnotatedClasses(DataSourceDefinition.class);
            if (classes != null) {
                for (Class clazz : classes) {
                    DataSourceDefinition dsDefinition = (DataSourceDefinition) clazz.getAnnotation(DataSourceDefinition.class);
                    processDefinition(dsDefinition, specDD);
                }
            }
        }

        // step 2: bind all defined data sources into jndi
        Collection<DataSource> dataSources = specDD.getDataSource();
        if (dataSources != null) {
            for (DataSource dataSource: dataSources) {
                try {
                    addDataSourceGBean(module, sharedContext, dataSource);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException("Error creating DataSource gbean", e);
                }
            }
        }        
    }

    private void addDataSourceGBean(Module module, Map<EARContext.Key, Object> sharedContext, DataSource ds)
        throws GBeanAlreadyExistsException {
                        
        String jndiName = ds.getKey();
        
        if (lookupJndiContextMap(module, jndiName) != null) {
            return;
        }
        
        String name = jndiName;
        if (name.startsWith("java:")) {
            name = name.substring(5);
        }
                
        EARContext earContext = module.getEarContext();
                       
        AbstractName dataSourceAbstractName = earContext.getNaming().createChildName(module.getModuleName(), name, "GBean");

        GBeanData dataSourceGBean = new GBeanData(dataSourceAbstractName, DataSourceGBean.class);
        
        DataSourceDescription dsDescription = createDataSourceDescription(ds);
        dataSourceGBean.setAttribute("dataSourceDescription", dsDescription);
        
        dataSourceGBean.setAttribute("defaultMaxSize", defaultMaxSize);
        dataSourceGBean.setAttribute("defaultMinSize", defaultMinSize);
        dataSourceGBean.setAttribute("defaultBlockingTimeoutMilliseconds", defaultBlockingTimeoutMilliseconds);
        dataSourceGBean.setAttribute("defaultIdleTimeoutMinutes", defaultIdleTimeoutMinutes);
        
        dataSourceGBean.setAttribute("defaultXATransactionCaching", defaultXATransactionCaching);
        dataSourceGBean.setAttribute("defaultXAThreadCaching", defaultXAThreadCaching);
        
        dataSourceGBean.setReferencePattern("ConnectionTracker", earContext.getConnectionTrackerName());
        dataSourceGBean.setReferencePattern("TransactionManager", earContext.getTransactionManagerName());

        dataSourceGBean.setServiceInterfaces(new String[] { javax.sql.DataSource.class.getName() });
        String osgiJndiName = null;
        if (dsDescription.getProperties() != null) {
            osgiJndiName = dsDescription.getProperties().get(ConnectorModuleBuilder.OSGI_JNDI_SERVICE_NAME);
        }
        if (osgiJndiName == null) {
            osgiJndiName = module.getEarContext().getNaming().toOsgiJndiName(dataSourceAbstractName);
        }
        dataSourceGBean.getServiceProperties().put(ConnectorModuleBuilder.OSGI_JNDI_SERVICE_NAME, osgiJndiName);
        
        earContext.addGBean(dataSourceGBean);
                
        Object ref = new JndiReference("aries:services/" + osgiJndiName);
        put(jndiName, ref, module.getJndiContext(), Collections.<InjectionTarget>emptyList(), sharedContext);
    }
    
    private DataSource processDefinition(DataSourceDefinition dsDefinition, JndiConsumer annotatedApp) {
        DataSource dataSource = findDataSource(dsDefinition, annotatedApp);
        boolean existing = dataSource != null;
        if (!existing) {
            dataSource = new DataSource();
            dataSource.setName(dsDefinition.name());
        }
        
        if (dataSource.getClassName() == null) {
            dataSource.setClassName(dsDefinition.className());
        }
        
        if (dataSource.getDescription() == null && dsDefinition.description().trim().length() > 0) {
            dataSource.setDescriptions(new Text[]{new Text(null, dsDefinition.description().trim())});

        }
        
        if (dataSource.getUrl() == null && dsDefinition.url().trim().length() > 0) {
            dataSource.setUrl(dsDefinition.description().trim());
        }
        
        if (dataSource.getUser() == null && dsDefinition.user().trim().length() > 0) {
            dataSource.setUser(dsDefinition.user().trim());
        }
        
        if (dataSource.getPassword() == null && dsDefinition.password().trim().length() > 0) {
            dataSource.setPassword(dsDefinition.password().trim());
        }
        
        if (dataSource.getDatabaseName() == null && dsDefinition.databaseName().trim().length() > 0) {
            dataSource.setDatabaseName(dsDefinition.databaseName().trim());
        }
        
        if (dataSource.getPortNumber() == null && dsDefinition.portNumber() != -1) {
            dataSource.setPortNumber(dsDefinition.portNumber());
        }
        
        if (dataSource.getServerName() == null && dsDefinition.serverName().trim().length() > 0) {
            dataSource.setServerName(dsDefinition.serverName().trim());
        }
        
        if (dataSource.getUrl() == null && dsDefinition.url().trim().length() > 0) {
            dataSource.setUrl(dsDefinition.url().trim());
        }
        
        if (dataSource.getInitialPoolSize() == null && dsDefinition.initialPoolSize() != -1) {
            dataSource.setInitialPoolSize(dsDefinition.initialPoolSize());
        }
        
        if (dataSource.getMaxPoolSize() == null && dsDefinition.maxPoolSize() != -1) {
            dataSource.setMaxPoolSize(dsDefinition.maxPoolSize());
        }
        
        if (dataSource.getMinPoolSize() == null && dsDefinition.minPoolSize() != -1) {
            dataSource.setMinPoolSize(dsDefinition.minPoolSize());
        }
        
        if (dataSource.getMaxIdleTime() == null && dsDefinition.maxIdleTime() != -1) {
            dataSource.setMaxIdleTime(dsDefinition.maxIdleTime());
        }
        
        if (dataSource.getMaxStatements() == null && dsDefinition.maxStatements() != -1) {
            dataSource.setMaxStatements(dsDefinition.maxStatements());
        }
        
        if (dataSource.getLoginTimeout() == null && dsDefinition.loginTimeout() != 0) {
            dataSource.setLoginTimeout(dsDefinition.loginTimeout());
        }
        
        if (dataSource.getIsolationLevel() == null) {
            dataSource.setIsolationLevel(IsolationLevel.fromFlag(dsDefinition.isolationLevel())); 
        }
        
        if (dataSource.getTransactional() == null) {
            dataSource.setTransactional(dsDefinition.transactional());
        }
        
        if (dataSource.getProperty().size() == 0) {
            String[] properties = dsDefinition.properties();
            if (properties != null) {
                for (String property : properties) {
                    String[] tokens = property.split("=");
                    Property prop = new Property();
                    prop.setName(tokens[0]);
                    prop.setValue(tokens[1]);
                    dataSource.getProperty().add(prop);
                }               
            }
        }
        if (!existing) {
            annotatedApp.getDataSource().add(dataSource);
        }
        return dataSource;
    }

    private DataSource findDataSource(DataSourceDefinition dsDefinition, JndiConsumer annotatedApp) {
        String dsDefinitionName = getJndiName(dsDefinition.name().trim());
        Collection<DataSource> dataSources = annotatedApp.getDataSource();
        for (DataSource ds : dataSources) {
            String dsName = getJndiName(ds.getName().trim());
            if (dsDefinitionName.equals(dsName)) {
                return ds;
            }
        }
        return null;        
    }
    
    private DataSourceDescription createDataSourceDescription(DataSource ds) {
        DataSourceDescription dsDescription = new DataSourceDescription();
        
        dsDescription.setName(ds.getName());
        dsDescription.setClassName(ds.getClassName());
        
        if (ds.getDescription() != null) {
            dsDescription.setDescription(ds.getDescription().trim());
        }
        
        if (ds.getUrl() != null) {
            dsDescription.setUrl(ds.getUrl().trim());
        }
        
        if (ds.getUser() != null) {
            dsDescription.setUser(ds.getUser().trim());
        }
        
        if (ds.getPassword() != null) {
            dsDescription.setPassword(ds.getPassword().trim());
        }
        
        if (ds.getDatabaseName() != null) {
            dsDescription.setDatabaseName(ds.getDatabaseName().trim());
        }
        
        if (ds.getServerName() != null) {
            dsDescription.setServerName(ds.getServerName().trim());
        }
        
        if (ds.getPortNumber() != null) {
            dsDescription.setPortNumber(ds.getPortNumber());
        }
                
        if (ds.getLoginTimeout() != null) {
            dsDescription.setLoginTimeout(ds.getLoginTimeout());
        }
        
        List<Property> props = ds.getProperty();
        if (props != null) {
            Map<String, String> properties = new HashMap<String, String>();
            for (Property prop : props) {
                properties.put(prop.getName().trim(),
                               prop.getValue().trim());
            }
            dsDescription.setProperties(properties);
        }
        
        // transaction properties
        
        if (ds.getTransactional()) {
            dsDescription.setTransactional(ds.getTransactional());
        }
        
        if (ds.getIsolationLevel() != null) {
            switch (ds.getIsolationLevel()) {
            case TRANSACTION_READ_COMMITTED:
                dsDescription.setIsolationLevel(Connection.TRANSACTION_READ_COMMITTED);
                break;
            case TRANSACTION_READ_UNCOMMITTED:
                dsDescription.setIsolationLevel(Connection.TRANSACTION_READ_UNCOMMITTED);
                break;
            case TRANSACTION_REPEATABLE_READ:
                dsDescription.setIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ);
                break;
            case TRANSACTION_SERIALIZABLE:
                dsDescription.setIsolationLevel(Connection.TRANSACTION_SERIALIZABLE);
                break;
            }
        }
        
        // pool properties
        
        if (ds.getInitialPoolSize() != null) {
            dsDescription.setInitialPoolSize(ds.getInitialPoolSize());
        }
        
        if (ds.getMaxPoolSize() != null) {
            dsDescription.setMaxPoolSize(ds.getMaxPoolSize());
        }
        
        if (ds.getMinPoolSize() != null) {
            dsDescription.setMinPoolSize(ds.getMinPoolSize());
        }
        
        if (ds.getMaxStatements() != null) {
            dsDescription.setMaxStatements(ds.getMaxStatements());
        }
        
        if (ds.getMaxIdleTime() != null) {
            dsDescription.setMaxIdleTime(ds.getMaxIdleTime());
        }
        
        return dsDescription;
    }
        
    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY; 
    }

    public QNameSet getSpecQNameSet() {
        return dataSourceQNameSet;
    }
    
    @Override
    public int getPriority() {
        return 20;
    }
    
}
