/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.j2ee.j2eeobjectnames;

import java.util.Properties;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @version $Rev: $ $Date: $
 */
public class NameFactory {

    //name components
    public static String J2EE_SERVER = "J2EEServer";
    public static String J2EE_APPLICATION = "J2EEApplication";
    public static String J2EE_MODULE = "J2EEModule";
    public static String J2EE_TYPE = "j2eeType";
    public static String J2EE_NAME = "name";

    //types
    public static String J2EE_DOMAIN = "J2EEDomain";
    public static String JVM = "JVM";
    public static String APP_CLIENT_MODULE = "AppClientModule";

    public static String EJB = "EJB";
    public static String EJB_MODULE = "EJBModule";
    public static String MESSAGE_DRIVEN_BEAN = "MessageDrivenBean";
    public static String ENTITY_BEAN = "EntityBean";
    public static String STATEFUL_SESSION_BEAN = "StatefulSessionBean";
    public static String STATELESS_SESSION_BEAN = "StatelessSessionBean";

    public static String WEB_MODULE = "WebModule";
    public static String SERVLET = "Servlet";

    public static String RESOURCE_ADAPTER_MODULE = "ResourceAdapterModule";
    public static String RESOURCE_ADAPTER = "ResourceAdapter";
    public static String JAVA_MAIL_RESOURCE = "JavaMailResource";
    public static String JCA_RESOURCE = "JCAResource";
    public static String JCA_CONNECTION_FACTORY = "JCAConnectionFactory";
    public static String JCA_MANAGED_CONNECTION_FACTORY = "JCAManagedConnectionFactory";
    public static String JDBC_RESOURCE = "JDBCResource";
    public static String JDBC_DATASOURCE = "JDBCDataSource";
    public static String JDBC_DRIVER = "JDBCDriver";
    public static String JMS_RESOURCE = "JMSResource";

    public static String JNDI_RESOURCE = "JNDIResource";
    public static String JTA_RESOURCE = "JTAResource";

    public static String RMI_IIOP_RESOURCE = "RMI_IIOPResource";
    public static String URL_RESOURCE = "URLResource";

    //used for J2EEApplication= when component is not deployed in an ear.
    public static String NULL = "null";

    //geronimo extensions
    public static final String JCA_ADMIN_OBJECT = "JCAAdminObject";
    public static final String JCA_ACTIVATION_SPEC = "JCAActivationSpec";
    public static final String JCA_RESOURCE_ADAPTER = "JCAResourceAdapter";
    public static final String JCA_WORK_MANAGER = "JCAWorkManager";
    public static final String JCA_CONNECTION_MANAGER = "JCAConnectionManager";

    public static ObjectName getDomainName(String j2eeDomainName, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, J2EE_DOMAIN);
        props.put(J2EE_NAME, context.getJ2eeDomainName(j2eeDomainName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    public static ObjectName getServerName(String j2eeDomainName, String j2eeServerName, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, J2EE_SERVER);
        props.put(J2EE_NAME, context.getJ2eeServerName(j2eeServerName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    public static ObjectName getApplicationName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, J2EE_APPLICATION);
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_NAME, context.getJ2eeApplicationName(j2eeApplicationName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    public static ObjectName getModuleName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, j2eeType);
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_APPLICATION, context.getJ2eeApplicationName(j2eeApplicationName));
        props.put(J2EE_NAME, context.getJ2eeModuleName(j2eeModuleName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    public static ObjectName getEjbComponentName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, context.getJ2eeType(j2eeType));
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_APPLICATION, context.getJ2eeApplicationName(j2eeApplicationName));
        props.put(EJB_MODULE, context.getJ2eeModuleName(j2eeModuleName));
        props.put(J2EE_NAME, context.getJ2eeName(j2eeName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    public static String getEjbComponentNameString(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        return getEjbComponentName(j2eeDomainName, j2eeServerName, j2eeApplicationName, j2eeModuleName, j2eeName, j2eeType, context).getCanonicalName();
    }

    public static ObjectName getResourceComponentName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, context.getJ2eeType(j2eeType));
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_APPLICATION, context.getJ2eeApplicationName(j2eeApplicationName));
//        props.put(RESOURCE_ADAPTER_MODULE, context.getJ2eeModuleName(j2eeModuleName));
        props.put(JCA_RESOURCE, context.getJ2eeModuleName(j2eeModuleName));
        props.put(J2EE_NAME, context.getJ2eeName(j2eeName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    public static String getResourceComponentNameString(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        return getResourceComponentName(j2eeDomainName, j2eeServerName, j2eeApplicationName, j2eeModuleName, j2eeName, j2eeType, context).getCanonicalName();
    }

    /**
     * Creates a query for components that are in no application with given name.
     *
     * @param j2eeDomainName
     * @param j2eeServerName
     * @param j2eeName
     * @param j2eeType
     * @param context
     * @return
     * @throws MalformedObjectNameException
     */

    public static ObjectName getComponentRestrictedQueryName(String j2eeDomainName, String j2eeServerName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
//        Properties props = new Properties();
//        props.put(J2EE_TYPE, context.getJ2eeType(j2eeType));
//        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
//        props.put(J2EE_APPLICATION, NULL;
//        props.put(RESOURCE_ADAPTER_MODULE, context.getJ2eeModuleName(j2eeModuleName));
//        props.put(JCA_RESOURCE, context.getJ2eeModuleName(j2eeModuleName));
//        props.put(J2EE_NAME, context.getJ2eeName(j2eeName));
//        try {
//            return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
//        } catch (MalformedObjectNameException e) {
//            throw new DeploymentException("Invalid component name", e);
//        }
        StringBuffer buffer = new StringBuffer(context.getJ2eeDomainName(j2eeDomainName))
                .append(":" + J2EE_TYPE + "=").append(context.getJ2eeType(j2eeType))
                .append("," + J2EE_SERVER + "=").append(context.getJ2eeServerName(j2eeServerName))
                .append("," + J2EE_APPLICATION + "=" + NULL)
                .append("," + J2EE_NAME + "=").append(context.getJ2eeName(j2eeName))
                .append(",*");
        return new ObjectName(buffer.toString());
    }

    public static ObjectName getWebComponentName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, context.getJ2eeType(j2eeType));
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_APPLICATION, context.getJ2eeApplicationName(j2eeApplicationName));
        props.put(WEB_MODULE, context.getJ2eeModuleName(j2eeModuleName));
        props.put(J2EE_NAME, context.getJ2eeName(j2eeName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    public static String getWebComponentNameString(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        return getResourceComponentName(j2eeDomainName, j2eeServerName, j2eeApplicationName, j2eeModuleName, j2eeName, j2eeType, context).getCanonicalName();
    }

    //for non-j2ee-deployable resources such as the transaction manager
    public static ObjectName getComponentName(String j2eeDomainName, String j2eeServerName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, context.getJ2eeType(j2eeType));
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_NAME, context.getJ2eeName(j2eeName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

}
