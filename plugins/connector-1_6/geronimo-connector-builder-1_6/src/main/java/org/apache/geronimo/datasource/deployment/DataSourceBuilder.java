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

import java.io.IOException;
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
import org.apache.geronimo.datasource.DataSourceService;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.openejb.jee.DataSource;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.IsolationLevel;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.Property;
import org.apache.openejb.jee.Text;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class DataSourceBuilder extends AbstractNamingBuilder {

    private final int defaultMaxSize;
    private final int defaultMinSize;
    private final int defaultBlockingTimeoutMilliseconds;
    private final int defaultIdleTimeoutMinutes;
    private final boolean defaultXATransactionCaching;
    private final boolean defaultXAThreadCaching;

    private final QNameSet dataSourceQNameSet;

    public DataSourceBuilder(@ParamAttribute(name = "eeNamespaces") String[] eeNamespaces,
                             @ParamAttribute(name = "defaultMaxSize") int defaultMaxSize,
                             @ParamAttribute(name = "defaultMinSize") int defaultMinSize,
                             @ParamAttribute(name = "defaultBlockingTimeoutMilliseconds") int defaultBlockingTimeoutMilliseconds,
                             @ParamAttribute(name = "defaultIdleTimeoutMinutes") int defaultIdleTimeoutMinutes,
                             @ParamAttribute(name = "defaultXATransactionCaching") boolean defaultXATransactionCaching,
                             @ParamAttribute(name = "defaultXAThreadCaching") boolean defaultXAThreadCaching) {
        this.defaultMaxSize = defaultMaxSize;
        this.defaultMinSize = defaultMinSize;
        this.defaultBlockingTimeoutMilliseconds = defaultBlockingTimeoutMilliseconds;
        this.defaultIdleTimeoutMinutes = defaultIdleTimeoutMinutes;
        this.defaultXATransactionCaching = defaultXATransactionCaching;
        this.defaultXAThreadCaching = defaultXAThreadCaching;

        this.dataSourceQNameSet = buildQNameSet(eeNamespaces, "data-source");
    }

    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {

        // step 1: process annotations and update deployment descriptor
        if ((module != null) && (module.getClassFinder() != null)) {

            List<Class<?>> classes;
            classes = module.getClassFinder().findAnnotatedClasses(DataSourceDefinitions.class);
            if (classes != null) {
                for (Class<?> clazz : classes) {
                    DataSourceDefinitions dsDefinitions = clazz.getAnnotation(DataSourceDefinitions.class);
                    for (DataSourceDefinition dsDefinition : dsDefinitions.value()) {
                        processDefinition(dsDefinition, specDD);
                    }

                }
            }
            classes = module.getClassFinder().findAnnotatedClasses(DataSourceDefinition.class);
            if (classes != null) {
                for (Class<?> clazz : classes) {
                    DataSourceDefinition dsDefinition = clazz.getAnnotation(DataSourceDefinition.class);
                    processDefinition(dsDefinition, specDD);
                }
            }
        }

        // step 2: bind all defined data sources into jndi
        Collection<DataSource> dataSources = specDD.getDataSource();
        if (dataSources != null) {
            for (DataSource dataSource: dataSources) {
                addDataSourceGBean(module, sharedContext, dataSource);
            }
        }
    }

    private void addDataSourceGBean(Module module, Map<EARContext.Key, Object> sharedContext, DataSource ds)
        throws DeploymentException {

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

        DataSourceDescription dsDescription = createDataSourceDescription(ds);
        String osgiJndiName = null;
        if (dsDescription.getProperties() != null) {
            osgiJndiName = dsDescription.getProperties().get(ConnectorModuleBuilder.OSGI_JNDI_SERVICE_NAME);
        }
        if (osgiJndiName == null) {
            osgiJndiName = module.getEarContext().getNaming().toOsgiJndiName(dataSourceAbstractName);
        }
        dsDescription.setOsgiServiceName(osgiJndiName);

        try {
            Object ref = DataSourceService.buildReference(dsDescription);
            put(jndiName, ref, ReferenceType.DATA_SOURCE, module.getJndiContext(), Collections.<InjectionTarget>emptySet(), sharedContext);
        } catch (IOException e) {
            throw new DeploymentException("Could not construct Reference for datasource " + dsDescription, e);
        }
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

            dsDescription.setMaxPoolSize(ds.getMaxPoolSize() != null? ds.getMaxPoolSize(): defaultMaxSize);

            dsDescription.setMinPoolSize(ds.getMinPoolSize() != null? ds.getMinPoolSize(): defaultMinSize);

        if (ds.getMaxStatements() != null) {
            dsDescription.setMaxStatements(ds.getMaxStatements());
        }

            dsDescription.setMaxIdleTime(ds.getMaxIdleTime() != null? ds.getMaxIdleTime(): defaultIdleTimeoutMinutes);

        //geronimo specific properties
        dsDescription.setBlockingTimeoutMilliseconds(defaultBlockingTimeoutMilliseconds);
        dsDescription.setXaThreadCaching(defaultXAThreadCaching);
        dsDescription.setXaTransactionCaching(defaultXATransactionCaching);

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
