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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.sql.DataSourceDefinition;
import javax.annotation.sql.DataSourceDefinitions;
import javax.resource.ResourceException;
import javax.sql.DataSource;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.datasource.DataSourceDescription;
import org.apache.geronimo.datasource.DataSourceGBean;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.deployment.annotation.AnnotatedApp;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.osgi.BundleAnnotationFinder;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.ResourceReferenceFactory;
import org.apache.geronimo.xbeans.javaee6.DataSourceType;
import org.apache.geronimo.xbeans.javaee6.IsolationLevelType;
import org.apache.geronimo.xbeans.javaee6.PropertyType;
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
    
    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
                        
        Bundle bundle = module.getEarContext().getDeploymentBundle();
        
        BundleAnnotationFinder classFinder;
        try {
            classFinder = new BundleAnnotationFinder(packageAdmin, bundle);
        } catch (Exception e) {
            throw new DeploymentException("could not create class finder " + bundle, e);
        }
        
        AnnotatedApp app = module.getAnnotatedApp();
        
        // step 1: process annotations and update deployment descriptor
        List<Class> classes;        
        classes = classFinder.findAnnotatedClasses(DataSourceDefinitions.class);
        if (classes != null) {
            for (Class clazz : classes) {
                DataSourceDefinitions dsDefinitions = (DataSourceDefinitions) clazz.getAnnotation(DataSourceDefinitions.class);
                for (DataSourceDefinition dsDefinition : dsDefinitions.value()) {
                    processDefinition(dsDefinition, app);
                }
                
            }
        }
        classes = classFinder.findAnnotatedClasses(DataSourceDefinition.class);
        if (classes != null) {
            for (Class clazz : classes) {
                DataSourceDefinition dsDefinition = (DataSourceDefinition) clazz.getAnnotation(DataSourceDefinition.class);
                processDefinition(dsDefinition, app);
            }
        }
        
        // step 2: bind all defined data sources into jndi
        DataSourceType[] dataSources = app.getDataSourceArray();
        if (dataSources != null) {
            for (int i = 0; i < dataSources.length; i++) {
                try {
                    addDataSourceGBean(module, componentContext, dataSources[i], "DataSource-" + i);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException("Error creating DataSource gbean", e);
                }
            }
        }        
    }

    private void addDataSourceGBean(Module module, Map componentContext, DataSourceType ds, String name)
        throws GBeanAlreadyExistsException {
                        
        String jndiName = ds.getName().getStringValue();
        
        if (lookupJndiContextMap(componentContext, jndiName) != null) {
            return;
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
        
        earContext.addGBean(dataSourceGBean);
                
        Object ref = new ResourceReferenceFactory<ResourceException>(module.getConfigId(), new AbstractNameQuery(dataSourceAbstractName), DataSource.class);
        put(jndiName, ref, getJndiContextMap(componentContext));
    }
    
    private DataSourceType processDefinition(DataSourceDefinition dsDefinition, AnnotatedApp annotatedApp) {
        DataSourceType dataSource = findDataSource(dsDefinition, annotatedApp);
        if (dataSource == null) {
            dataSource = annotatedApp.addNewDataSource();
            dataSource.addNewName().setStringValue(dsDefinition.name());
        }
        
        if (!dataSource.isSetClassName()) {
            dataSource.addNewClassName().setStringValue(dsDefinition.className());
        }
        
        if (!dataSource.isSetDescription() && dsDefinition.description().trim().length() > 0) {
            dataSource.addNewDescription().setStringValue(dsDefinition.description().trim());            
        }
        
        if (!dataSource.isSetUrl() && dsDefinition.url().trim().length() > 0) {
            dataSource.addNewUrl().setStringValue(dsDefinition.description().trim());
        }
        
        if (!dataSource.isSetUser() && dsDefinition.user().trim().length() > 0) {
            dataSource.addNewUser().setStringValue(dsDefinition.user().trim());
        }
        
        if (!dataSource.isSetPassword() && dsDefinition.password().trim().length() > 0) {
            dataSource.addNewPassword().setStringValue(dsDefinition.password().trim());
        }
        
        if (!dataSource.isSetDatabaseName() && dsDefinition.databaseName().trim().length() > 0) {
            dataSource.addNewDatabaseName().setStringValue(dsDefinition.databaseName().trim());
        }
        
        if (!dataSource.isSetPortNumber() && dsDefinition.portNumber() != -1) {
            dataSource.addNewPortNumber().setStringValue(String.valueOf(dsDefinition.portNumber()));
        }
        
        if (!dataSource.isSetServerName() && dsDefinition.serverName().trim().length() > 0) {
            dataSource.addNewServerName().setStringValue(dsDefinition.serverName().trim());
        }
        
        if (!dataSource.isSetUrl() && dsDefinition.url().trim().length() > 0) {
            dataSource.addNewUrl().setStringValue(dsDefinition.url().trim());
        }
        
        if (!dataSource.isSetInitialPoolSize() && dsDefinition.initialPoolSize() != -1) {
            dataSource.addNewInitialPoolSize().setStringValue(String.valueOf(dsDefinition.initialPoolSize()));            
        }
        
        if (!dataSource.isSetMaxPoolSize() && dsDefinition.maxPoolSize() != -1) {
            dataSource.addNewMaxPoolSize().setStringValue(String.valueOf(dsDefinition.maxPoolSize()));            
        }
        
        if (!dataSource.isSetMinPoolSize() && dsDefinition.minPoolSize() != -1) {
            dataSource.addNewMinPoolSize().setStringValue(String.valueOf(dsDefinition.minPoolSize()));            
        }
        
        if (!dataSource.isSetMaxIdleTime() && dsDefinition.maxIdleTime() != -1) {
            dataSource.addNewMaxIdleTime().setStringValue(String.valueOf(dsDefinition.maxIdleTime()));            
        }
        
        if (!dataSource.isSetMaxStatements() && dsDefinition.maxStatements() != -1) {
            dataSource.addNewMaxStatements().setStringValue(String.valueOf(dsDefinition.maxStatements()));            
        }
        
        if (!dataSource.isSetLoginTimeout() && dsDefinition.loginTimeout() != 0) {
            dataSource.addNewLoginTimeout().setStringValue(String.valueOf(dsDefinition.loginTimeout()));            
        }
        
        if (!dataSource.isSetIsolationLevel() && dsDefinition.isolationLevel() != -1) {
            dataSource.setIsolationLevel(IsolationLevelType.Enum.forInt(dsDefinition.isolationLevel()));
        }
        
        if (!dataSource.isSetTransactional()) {
            dataSource.addNewTransactional().setBooleanValue(dsDefinition.transactional());
        }
        
        if (dataSource.getPropertyArray() == null || dataSource.getPropertyArray().length == 0) {
            String[] properties = dsDefinition.properties();
            if (properties != null) {
                for (String property : properties) {
                    String[] tokens = property.split("=");
                    PropertyType propertyType = dataSource.addNewProperty();
                    propertyType.addNewName().setStringValue(tokens[0]);
                    propertyType.addNewValue().setStringValue(tokens[1]);                    
                }               
            }
        }
        
        return dataSource;
    }

    private DataSourceType findDataSource(DataSourceDefinition dsDefinition, AnnotatedApp annotatedApp) {
        String dsDefinitionName = getJndiName(dsDefinition.name().trim());
        DataSourceType[] dataSources = annotatedApp.getDataSourceArray();
        for (DataSourceType ds : dataSources) {
            String dsName = getJndiName(ds.getName().getStringValue().trim());
            if (dsDefinitionName.equals(dsName)) {
                return ds;
            }
        }
        return null;        
    }
    
    private DataSourceDescription createDataSourceDescription(DataSourceType ds) {
        DataSourceDescription dsDescription = new DataSourceDescription();
        
        dsDescription.setName(ds.getName().getStringValue());
        dsDescription.setClassName(ds.getClassName().getStringValue());
        
        if (ds.isSetDescription()) {
            dsDescription.setDescription(ds.getDescription().getStringValue().trim());
        }
        
        if (ds.isSetUrl()) {
            dsDescription.setUrl(ds.getUrl().getStringValue().trim());
        }
        
        if (ds.isSetUser()) {
            dsDescription.setUser(ds.getUser().getStringValue().trim());
        }
        
        if (ds.isSetPassword()) {
            dsDescription.setPassword(ds.getPassword().getStringValue().trim());
        }
        
        if (ds.isSetDatabaseName()) {
            dsDescription.setDatabaseName(ds.getDatabaseName().getStringValue().trim());
        }
        
        if (ds.isSetServerName()) {
            dsDescription.setServerName(ds.getServerName().getStringValue().trim());
        }
        
        if (ds.isSetPortNumber()) {
            dsDescription.setPortNumber(ds.getPortNumber().getBigIntegerValue().intValue());
        }
                
        if (ds.isSetLoginTimeout()) {
            dsDescription.setLoginTimeout(ds.getLoginTimeout().getBigIntegerValue().intValue());      
        }
        
        PropertyType[] props = ds.getPropertyArray();
        if (props != null) {
            Map<String, String> properties = new HashMap<String, String>();
            for (PropertyType prop : props) {
                properties.put(prop.getName().getStringValue().trim(), 
                               prop.getValue().getStringValue().trim());
            }
            dsDescription.setProperties(properties);
        }
        
        // transaction properties
        
        if (ds.isSetTransactional()) {
            dsDescription.setTransactional(ds.getTransactional().getBooleanValue());
        }
        
        if (ds.isSetIsolationLevel()) {
            // dsDefinition.setIsolationLevel(ds.getIsolationLevel().intValue());
        }
        
        // pool properties
        
        if (ds.isSetInitialPoolSize()) {
            dsDescription.setInitialPoolSize(ds.getInitialPoolSize().getBigIntegerValue().intValue());
        }
        
        if (ds.isSetMaxPoolSize()) {
            dsDescription.setMaxPoolSize(ds.getMaxPoolSize().getBigIntegerValue().intValue());
        }
        
        if (ds.isSetMinPoolSize()) {
            dsDescription.setMinPoolSize(ds.getMinPoolSize().getBigIntegerValue().intValue());
        }
        
        if (ds.isSetMaxStatements()) {
            dsDescription.setMaxStatements(ds.getMaxStatements().getBigIntegerValue().intValue());
        }
        
        if (ds.isSetMaxIdleTime()) {
            dsDescription.setMaxIdleTime(ds.getMaxIdleTime().getBigIntegerValue().intValue());
        }
        
        return dsDescription;
    }
    
    private static String getJndiName(String name) {
        if (name.startsWith("java:")) {
            return name;
        } else {
            return "java:comp/env/" + name;
        }
    }
    
    public QNameSet getPlanQNameSet() {
        return QNameSet.EMPTY; 
    }

    public QNameSet getSpecQNameSet() {
        return dataSourceQNameSet;
    }
}
