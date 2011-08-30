/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.datasource;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.Hashtable;

import javax.naming.BinaryRefAddr;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.OsgiService;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.manager.RecoverableTransactionManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev:$ $Date:$
 */

@GBean
@OsgiService
public class DataSourceGBeanObjectFactory implements ObjectFactory {
    public static final String OSGI_JNDI_SERVICE_NAME = "osgi.jndi.service.name";
    public static final String	BUNDLE_CONTEXT		= "osgi.service.jndi.bundleContext";
    private BundleContext bundleContext;
    private ConnectionTracker connectionTracker;
    private RecoverableTransactionManager txManager;
    private AbstractName abName;
    
    public DataSourceGBeanObjectFactory(@ParamSpecial(type = SpecialAttributeType.bundleContext)BundleContext bundleContext,
                                        @ParamSpecial(type = SpecialAttributeType.abstractName)AbstractName abName,
                                        @ParamReference(name = "ConnectionTracker", namingType = NameFactory.JCA_CONNECTION_TRACKER)ConnectionTracker connectionTracker,
                                        @ParamReference(name = "TransactionManager", namingType = NameFactory.JTA_RESOURCE)RecoverableTransactionManager txManager) {
        this.bundleContext = bundleContext;
        this.connectionTracker = connectionTracker;
        this.txManager = txManager;
        this.abName = abName;
    }

    @Override
    public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {
        if (o instanceof Reference) {
            Reference ref = (Reference) o;
            if (ref.getClassName().equals(DataSourceService.class.getName())) {
                BinaryRefAddr addr = (BinaryRefAddr) ref.get(0);
                ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) addr.getContent());
                ObjectInputStream in = new ObjectInputStream(bais);
                DataSourceDescription dataSourceDescription = (DataSourceDescription) in.readObject();
                Hashtable dict = new Hashtable();
                dict.put(OSGI_JNDI_SERVICE_NAME, dataSourceDescription.getOsgiServiceName());
                ServiceReference[] serviceReferences = bundleContext.getServiceReferences(javax.sql.DataSource.class.getName(), "(" + OSGI_JNDI_SERVICE_NAME + "=" + dataSourceDescription.getOsgiServiceName() + ")");
                if (serviceReferences != null && serviceReferences.length > 0) {
                    Object result = bundleContext.getService(serviceReferences[0]);
                    return result;
                }
                String objectName = abName.toString();
//                BundleContext bundleContext = (BundleContext) hashtable.get(BUNDLE_CONTEXT);
                DataSourceService dataSourceGBean = new DataSourceService(dataSourceDescription, connectionTracker, txManager, objectName, null);
                bundleContext.registerService(new String[] {javax.sql.DataSource.class.getName()}, dataSourceGBean, dict);
                return dataSourceGBean.$getResource();
            }
        }

        return null;
    }
}
