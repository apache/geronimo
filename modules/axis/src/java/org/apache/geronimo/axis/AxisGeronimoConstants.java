/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis;

/**
 * Class AxisGeronimoConstants
 */
public class AxisGeronimoConstants {

    /**
     * Field J2EE_DOMAIN_NAME
     */
    public static final String J2EE_DOMAIN_NAME = "openejb.server";

    /**
     * Field J2EE_SERVER_NAME
     */
    public static final String J2EE_SERVER_NAME = "TestOpenEJBServer";

    /**
     * Field WEB_CONTANER_NAME
     */
    public static final String WEB_CONTANER_NAME =
            "geronimo.jetty:role=Container";

    /**
     * Field WEB_CONNECTOR_NAME
     */
    public static final String WEB_CONNECTOR_NAME =
            "geronimo.jetty:role=Connector";

    /**
     * Field APPLICATION_NAME
     */
    public static final String APPLICATION_NAME = "geronimo.jetty:app=test";

    /**
     * Field TRANSACTION_MANAGER_NAME
     */
    public static final String TRANSACTION_MANAGER_NAME =
            "geronimo.test:role=TransactionManager";

    /**
     * Field CONNTECTION_TRACKING_COORDINATOR
     */
    public static final String CONNTECTION_TRACKING_COORDINATOR =
            "geronimo.test:role=ConnectionTrackingCoordinator";

    /**
     * Field AXIS_CONFIG_STORE
     */
    public static final String AXIS_CONFIG_STORE = "target/config-store";

    /**
     * Field TEMP_OUTPUT
     */
    public static final String TEMP_OUTPUT = "target/temp";

    /**
     * Field AXIS_SERVICE_PORT
     */
    public static final int AXIS_SERVICE_PORT = 5678;
}
