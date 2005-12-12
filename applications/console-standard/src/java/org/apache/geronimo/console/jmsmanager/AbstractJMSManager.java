/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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

import java.util.Iterator;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;

public abstract class AbstractJMSManager {

    protected static final String JMS_SERVER_MBEAN_NAME = "geronimo.server:J2EEApplication=null,J2EEModule=geronimo/activemq-broker/1.0/car,J2EEServer=geronimo,j2eeType=JMSServer,name=ActiveMQl";

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

    protected static final String CONNECTION_FACTORY_NAME = "geronimo.server:J2EEApplication=null,J2EEServer=geronimo,JCAResource=geronimo/activemq/1.0/car,j2eeType=JCAManagedConnectionFactory,name=DefaultActiveMQConnectionFactory";

    protected static Object[] no_args = new Object[0];

    protected static String[] no_params = new String[0];

    protected static Kernel kernel = KernelRegistry.getSingleKernel();

    protected static final String BASE_CONFIG_URI = "runtimedestination/";

    protected ObjectName mBeanName;

    public static final ObjectName DESTINATION_QUERY;

    public static final ObjectName ACTIVEMQJCA_RESOURCE_QUERY;

    public static final String ACTIVEMQJCA_RESOURCE;

    static {
        try {

            DESTINATION_QUERY = ObjectName
                    .getInstance("geronimo.server:j2eeType="
                            + NameFactory.JCA_ADMIN_OBJECT + ",*");
            ACTIVEMQJCA_RESOURCE_QUERY = ObjectName
                    .getInstance("*:j2eeType=JCAManagedConnectionFactory,name=DefaultActiveMQConnectionFactory,*");
            ACTIVEMQJCA_RESOURCE = getActiveMQJCA_RESOURCE(ACTIVEMQJCA_RESOURCE_QUERY);

            if (null == ACTIVEMQJCA_RESOURCE) {
                throw new RuntimeException(
                        "No JCA resource was found for DefaultActiveMQConnectionFactory");
            }

        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
    }

    public static final J2eeContext baseContext = new J2eeContextImpl(
            "geronimo.server", "geronimo", "null", ACTIVEMQJCA_RESOURCE, null,
            null, NameFactory.JCA_ADMIN_OBJECT);

    /**
     * Get the JCA resource name of the activemq bean.
     *
     * @return JCA resource name
     */
    public static String getActiveMQJCA_RESOURCE(ObjectName obj) {

        Set modules = kernel.listGBeans(obj);

        String JCA_Resource = null;

        for (Iterator iterator = modules.iterator(); iterator.hasNext();) {
            ObjectName activemqObject = (ObjectName) iterator.next();
            JCA_Resource = activemqObject
                    .getKeyProperty(NameFactory.JCA_RESOURCE);
        }

        return JCA_Resource;
    }

}
