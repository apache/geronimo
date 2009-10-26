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

package org.apache.geronimo.console.jmsmanager;

import java.util.Map;

import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.repository.Artifact;

public abstract class AbstractJMSManager {

    protected static final Artifact ACTIVEMQ_BROKER_ARTIFACT = new Artifact("geronimo", "activemq-broker", org.apache.geronimo.system.serverinfo.ServerConstants.getVersion(), "car");
    protected static final Artifact ACTIVEMQ_ARTIFACT = new Artifact("geronimo", "activemq", org.apache.geronimo.system.serverinfo.ServerConstants.getVersion(), "car");
    protected static final Kernel kernel = KernelRegistry.getSingleKernel();

    protected static final ConfigurationManager configurationManager;
    static {
        try {
            configurationManager = ConfigurationUtil.getConfigurationManager(kernel);
        } catch (GBeanNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    protected final Configuration BROKER_CONFIGURATION = configurationManager.getConfiguration(ACTIVEMQ_BROKER_ARTIFACT);

    protected final Configuration CONNECTOR_CONFIGURATION = configurationManager.getConfiguration(ACTIVEMQ_ARTIFACT);
    
    
    private static final AbstractName earName = kernel.getNaming().createRootName(ACTIVEMQ_ARTIFACT, NameFactory.NULL, NameFactory.J2EE_APPLICATION);
    protected static final AbstractName RESOURCE_ADAPTER_MODULE_NAME = kernel.getNaming().createChildName(earName, ACTIVEMQ_ARTIFACT.toString(), NameFactory.RESOURCE_ADAPTER_MODULE);
    protected static final AbstractName RESOURCE_ADAPTER_NAME = kernel.getNaming().createChildName(RESOURCE_ADAPTER_MODULE_NAME, ACTIVEMQ_ARTIFACT.toString(), NameFactory.RESOURCE_ADAPTER);
    protected static final AbstractName JCA_RESOURCE_NAME = kernel.getNaming().createChildName(RESOURCE_ADAPTER_NAME, ACTIVEMQ_ARTIFACT.toString(), NameFactory.JCA_RESOURCE);
    protected static final AbstractName JCA_CONNECTION_FACTORY_NAME = kernel.getNaming().createChildName(JCA_RESOURCE_NAME, "DefaultActiveMQConnectionFactory", NameFactory.JCA_CONNECTION_FACTORY);
    protected static final AbstractName JCA_MANAGED_CONNECTION_FACTORY_NAME = kernel.getNaming().createChildName(JCA_CONNECTION_FACTORY_NAME, "DefaultActiveMQConnectionFactory", NameFactory.JCA_MANAGED_CONNECTION_FACTORY);


    protected static final String GET_BROKER_ADMIN_FUNCTION = "getBrokerAdmin";

    public static final String TOPIC_TYPE = "Topic";

    public static final String QUEUE_TYPE = "Queue";

    //ViewDestinations attribute names
    protected static final String DESTINATION_LIST = "destinations";

    protected static final String DESTINATION_MSG = "destinationsMsg";

    //CreateDestinations attribute names
    protected static final String DESTINATION_NAME = "destinationMessageDestinationName";

    protected static final String DESTINATION_PHYSICAL_NAME = "destinationPhysicalName";

    protected static final String DESTINATION_TYPE = "destinationType";

    protected static final String DESTINATION_APPLICATION_NAME = "destinationApplicationName";

    protected static final String DESTINATION_MODULE_NAME = "destinationModuleName";

    protected static final String DESTINATION_CONFIG_URI = "destinationConfigURI";

    protected static Object[] no_args = new Object[0];

    protected static String[] no_params = new String[0];


    protected static final String BASE_CONFIG_URI = "runtimedestination/";

    protected GBeanData getResourceAdapterModuleData() throws GBeanNotFoundException {
        return CONNECTOR_CONFIGURATION.findGBeanData(new AbstractNameQuery(RESOURCE_ADAPTER_MODULE_NAME));
    }

    protected GBeanData getQueueGBeanData() throws GBeanNotFoundException {
        GBeanData moduleData = getResourceAdapterModuleData();
        Map adminObjects = (Map) moduleData.getAttribute("adminObjectInfoMap");
        GBeanData queueData = (GBeanData) adminObjects.get(Queue.class.getName());
        return new GBeanData(queueData);
    }

    protected GBeanData getTopicGBeanData() throws GBeanNotFoundException {
        GBeanData moduleData = getResourceAdapterModuleData();
        Map adminObjects = (Map) moduleData.getAttribute("adminObjectInfoMap");
        GBeanData queueData = (GBeanData) adminObjects.get(Topic.class.getName());
        return new GBeanData(queueData);
    }

//    protected ObjectName mBeanName;

//    public static final ObjectName DESTINATION_QUERY;

//    public static final ObjectName ACTIVEMQJCA_RESOURCE_QUERY;

//    public static final String ACTIVEMQJCA_RESOURCE;

//    static {
//        try {
//
//            DESTINATION_QUERY = ObjectName
//                    .getInstance("geronimo.server:j2eeType="
//                            + NameFactory.JCA_ADMIN_OBJECT + ",*");
//            ACTIVEMQJCA_RESOURCE_QUERY = ObjectName
//                    .getInstance("*:j2eeType=JCAManagedConnectionFactory,name=DefaultActiveMQConnectionFactory,*");
//            ACTIVEMQJCA_RESOURCE = getActiveMQJCA_RESOURCE(ACTIVEMQJCA_RESOURCE_QUERY);
//
//            if (null == ACTIVEMQJCA_RESOURCE) {
//                throw new RuntimeException(
//                        "No JCA resource was found for DefaultActiveMQConnectionFactory");
//            }
//
//        } catch (MalformedObjectNameException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    public static final J2eeContext baseContext = new J2eeContextImpl(
//            "geronimo.server", "geronimo", "null", ACTIVEMQJCA_RESOURCE, null,
//            null, NameFactory.JCA_ADMIN_OBJECT);
//
    /**
     * Get the JCA resource name of the activemq bean.
     *
     * @return JCA resource name
     */
//    public static String getActiveMQJCA_RESOURCE(ObjectName obj) {
//
//        Set modules = kernel.listGBeans(obj);
//
//        String JCA_Resource = null;
//
//        for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
//            ObjectName activemqObject = (ObjectName) iterator.next();
//            JCA_Resource = activemqObject
//                    .getKeyProperty(NameFactory.JCA_RESOURCE);
//        }
//
//        return JCA_Resource;
//    }

}
