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

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.Naming;

import java.util.Properties;
import java.util.Map;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Collections;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @version $Rev:385692 $ $Date$
 */
public class NameFactory {

    public static final Object JSR77_BASE_NAME_PROPERTY = "org.apache.geronimo.name.javax.management.j2ee.BaseName";


    // Manadatory key properties from JSR77.3.1.1.1.3
    public static final String J2EE_TYPE = "j2eeType";
    public static final String J2EE_NAME = "name";

    // ManagedObject j2eeTypes from JSR77.3-1
    public static final String J2EE_DOMAIN = "J2EEDomain";
    public static final String J2EE_SERVER = "J2EEServer";
    public static final String J2EE_APPLICATION = "J2EEApplication";
    public static final String APP_CLIENT_MODULE = "AppClientModule";
    public static final String EJB_MODULE = "EJBModule";
    public static final String WEB_MODULE = "WebModule";
    public static final String RESOURCE_ADAPTER_MODULE = "ResourceAdapterModule";
    public static final String ENTITY_BEAN = "EntityBean";
    public static final String STATEFUL_SESSION_BEAN = "StatefulSessionBean";
    public static final String STATELESS_SESSION_BEAN = "StatelessSessionBean";
    public static final String MESSAGE_DRIVEN_BEAN = "MessageDrivenBean";
    public static final String SERVLET = "Servlet";
    public static final String RESOURCE_ADAPTER = "ResourceAdapter";
    public static final String JAVA_MAIL_RESOURCE = "JavaMailResource";
    public static final String JCA_RESOURCE = "JCAResource";
//    public static final String JCA_RESOURCE_ADAPTER defined in geronimo extensions
    public static final String JCA_CONNECTION_FACTORY = "JCAConnectionFactory";
    public static final String JCA_MANAGED_CONNECTION_FACTORY = "JCAManagedConnectionFactory";
    public static final String JDBC_RESOURCE = "JDBCResource";
    public static final String JDBC_DATASOURCE = "JDBCDataSource";
    public static final String JDBC_DRIVER = "JDBCDriver";
    public static final String JMS_RESOURCE = "JMSResource";
    public static final String JNDI_RESOURCE = "JNDIResource";
    public static final String JTA_RESOURCE = "JTAResource";
    public static final String RMI_IIOP_RESOURCE = "RMI_IIOPResource";
    public static final String URL_RESOURCE = "URLResource";
    public static final String JVM = "JVM";

    // abstract name components
    public static final String J2EE_DEPLOYABLE_OBJECT = "J2EEDeployableObject";
    public static final String J2EE_MODULE = "J2EEModule";
    public static final String EJB = "EJB";
    public static final String SESSION_BEAN = "SessionBean";
    public static final String J2EE_RESOURCE = "J2EEResource";

    //used for J2EEApplication= when component is not deployed in an ear.
    public static final String NULL = "null";

    //geronimo extensions
    // todo should these really be j2eeType or should we have a Geronimo-specific property?
    public static final String TRANSACTION_MANAGER = "TransactionManager";
    public static final String TRANSACTION_CONTEXT_MANAGER = "TransactionContextManager";
    public static final String TRANSACTION_LOG = "TransactionLog";
    public static final String XID_FACTORY = "XIDFactory";
    public static final String XID_IMPORTER = "XIDImporter";
    public static final String JCA_CONNECTION_TRACKER = "JCAConnectionTracker";
    public static final String JCA_ADMIN_OBJECT = "JCAAdminObject";
    public static final String JCA_ACTIVATION_SPEC = "JCAActivationSpec";
    //TODO shouldn't we use the RESOURCE_ADAPTER string?
    public static final String JCA_RESOURCE_ADAPTER = "JCAResourceAdapter";
    public static final String JCA_WORK_MANAGER = "JCAWorkManager";
    public static final String JCA_CONNECTION_MANAGER = "JCAConnectionManager";
    public static final String WEB_FILTER = "WebFilter";
    public static final String URL_WEB_FILTER_MAPPING = "URLWebFilterMapping";
    public static final String SERVLET_WEB_FILTER_MAPPING = "ServletWebFilterMapping";
    public static final String URL_PATTERN = "URLPattern";
    public static final String GERONIMO_SERVICE = "GBean"; //copied in GBeanInfoBuilder to avoid dependencies in the wrong direction.
    public static final String CORBA_SERVICE = "CORBABean";
    public static final String JACC_MANAGER = "JACCManager";
    public static final String SYSTEM_LOG = "SystemLog";
    public static final String JAXR_CONNECTION_FACTORY = "JAXRConnectionFactory";
    public static final String CONFIG_BUILDER = "ConfigBuilder";
    public static final String MODULE_BUILDER = "ModuleBuilder";
    public static final String SECURITY_REALM = "SecurityRealm";
    public static final String LOGIN_MODULE = "LoginModule";
    public static final String APP_CLIENT = "AppClient";
    //jsr 88 configurer
    public static final String DEPLOYMENT_CONFIGURER = "DeploymentConfigurer";
    public static final String CONFIGURATION_STORE = "ConfigurationStore";
    public static final String DEPLOYER = "Deployer"; //duplicated in Deployer
    public static final String REALM_BRIDGE = "RealmBridge";
    public static final String CONFIGURATION_ENTRY = "ConfigurationEntry";
    public static final String PERSISTENT_CONFIGURATION_LIST = "PersistentConfigurationList"; //duplicated in FileConfigurationList
//    public static final String URL_PATTERN = "URLPattern";
    public static final String DEFAULT_SERVLET = "DefaultServlet";
    public static final String SERVLET_WEB_SERVICE_TEMPLATE = "ServletWebServiceTemplate";
    public static final String CORBA_CSS = "CORBACSS";
    public static final String CORBA_TSS = "CORBATSS";
    public static final String WEB_SERVICE_LINK = "WSLink";

    private static String[] moduleTypeNames = new String[] {
            J2EE_APPLICATION, //null?? this should never happen
            EJB_MODULE,
            APP_CLIENT_MODULE,
            RESOURCE_ADAPTER_MODULE,
            WEB_MODULE,
            J2EE_MODULE,  //this is a bad name here
            J2EE_MODULE   //should be SpringModule?
    };

    public static AbstractName buildModuleName(Map properties, Artifact artifact, ConfigurationModuleType moduleType, String moduleName) throws MalformedObjectNameException {
        String moduleTypeString = moduleTypeNames[moduleType.getValue()];
        String baseNameString = (String) properties.get(JSR77_BASE_NAME_PROPERTY);
        ObjectName baseName = ObjectName.getInstance(baseNameString);
        String domain = baseName.getDomain();
        Hashtable keys = baseName.getKeyPropertyList();
        String serverName = (String) keys.get(J2EE_SERVER);
        if (serverName == null) {
            throw new MalformedObjectNameException("No J2EEServer key in " + baseNameString);
        }
        Map nameMap = new HashMap();
        if (moduleName == null) {
            //this is a standalone module
            keys.put(J2EE_APPLICATION, NULL);
            keys.put(J2EE_TYPE, moduleTypeString);
            keys.put(J2EE_NAME, artifact.toString());
        } else {
            //this is part of an application
            keys.put(J2EE_APPLICATION, artifact.toString());
            keys.put(J2EE_TYPE, moduleTypeString);
            keys.put(J2EE_NAME, moduleName);
            nameMap.put("module", moduleName);
        }
        ObjectName moduleObjectName = ObjectName.getInstance(domain, keys);
        return new AbstractName(artifact, nameMap, Collections.EMPTY_SET, moduleObjectName);
    }


    /**
     *
     * @deprecated
     * @param j2eeDomainName
     * @param j2eeServerName
     * @param j2eeApplicationName
     * @param context
     * @return
     * @throws MalformedObjectNameException
     */
    public static ObjectName getApplicationName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, J2EE_APPLICATION);
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_NAME, context.getJ2eeApplicationName(j2eeApplicationName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    /**
     *
     * @deprecated
     * @param j2eeDomainName
     * @param j2eeServerName
     * @param j2eeApplicationName
     * @param j2eeModuleType
     * @param j2eeModuleName
     * @param context
     * @return
     * @throws MalformedObjectNameException
     */
    public static ObjectName getModuleName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleType, String j2eeModuleName, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        //N.B.! module context will have the module's j2eeType as its module type attribute.
        props.put(J2EE_TYPE, context.getJ2eeModuleType(j2eeModuleType));
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_APPLICATION, context.getJ2eeApplicationName(j2eeApplicationName));
        props.put(J2EE_NAME, context.getJ2eeModuleName(j2eeModuleName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    /**
     *
     * @deprecated
     * @param j2eeDomainName
     * @param j2eeServerName
     * @param j2eeApplicationName
     * @param j2eeModuleType
     * @param j2eeModuleName
     * @param j2eeName
     * @param j2eeType
     * @param context
     * @return
     * @throws MalformedObjectNameException
     */
    public static ObjectName getComponentName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleType, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, context.getJ2eeType(j2eeType));
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_APPLICATION, context.getJ2eeApplicationName(j2eeApplicationName));
        props.put(context.getJ2eeModuleType(j2eeModuleType), context.getJ2eeModuleName(j2eeModuleName));
        props.put(J2EE_NAME, context.getJ2eeName(j2eeName));
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

    /**
     *
     * @deprecated
     * @param j2eeDomainName
     * @param j2eeServerName
     * @param j2eeApplicationName
     * @param j2eeModuleName
     * @param j2eeName
     * @param j2eeType
     * @param context
     * @return
     * @throws MalformedObjectNameException
     */
    public static ObjectName getEjbComponentName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        return getComponentName(j2eeDomainName, j2eeServerName, j2eeApplicationName, EJB_MODULE, j2eeModuleName, j2eeName, j2eeType, context);
    }

    /**
     *
     * @deprecated
     * @param name
     * @param type
     * @param context
     * @return AbstractNameQuery
     */
    public static AbstractNameQuery getComponentNameQuery(String name, String type, AbstractName context) {
        return getComponentNameQuery(null, null, name, type, context);
    }

    /**
     *
     * @deprecated
     * @param moduleName
     * @param moduleType
     * @param name
     * @param type
     * @param context
     * @return AbstractNameQuery
     */
    public static AbstractNameQuery getComponentNameQuery(String moduleName, String moduleType, String name, String type, AbstractName context) {
        Map nameProperties = new HashMap(context.getName());

        // from the existing name properties define a new propertye ${j2eeType}=${name}
        String parentName = (String) nameProperties.remove(J2EE_NAME);
        String parentJ2eeType = (String) nameProperties.remove(J2EE_TYPE);
        if (parentName != null && parentJ2eeType != null) {
            nameProperties.put(parentJ2eeType, parentName);
        }

        if (!"*".equals(moduleName)) {
            nameProperties.put(moduleType, moduleName);
        }

        if (!"*".equals(name)) {
            nameProperties.put(J2EE_NAME, name);
        }

        if (type != null) {
            nameProperties.put(J2EE_TYPE, type);
        }

        return new AbstractNameQuery(context.getArtifact(), nameProperties, (String) null);
    }

    /**
     * @param j2eeDomainName
     * @param j2eeServerName
     * @param j2eeApplicationName
     * @param j2eeModuleName
     * @param j2eeName
     * @param j2eeType
     * @param context
     * @return
     * @throws MalformedObjectNameException
     * @deprecated
     */
    public static ObjectName getWebComponentName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        return getComponentName(j2eeDomainName, j2eeServerName, j2eeApplicationName, WEB_MODULE, j2eeModuleName, j2eeName, j2eeType, context);
    }


    /**
     * @param j2eeDomainName
     * @param j2eeServerName
     * @param j2eeApplicationName
     * @param j2eeModuleName
     * @param j2eeName
     * @param j2eeType
     * @param context
     * @return
     * @throws MalformedObjectNameException
     * @deprecated
     */
    //for non-j2ee-deployable resources such as the transaction manager
    public static ObjectName getComponentName(String j2eeDomainName, String j2eeServerName, String j2eeApplicationName, String j2eeModuleName, String j2eeName, String j2eeType, J2eeContext context) throws MalformedObjectNameException {
        Properties props = new Properties();
        props.put(J2EE_TYPE, context.getJ2eeType(j2eeType));
        props.put(J2EE_SERVER, context.getJ2eeServerName(j2eeServerName));
        props.put(J2EE_NAME, context.getJ2eeName(j2eeName));
        props.put(J2EE_APPLICATION, context.getJ2eeApplicationName(j2eeApplicationName));
        //TODO add module type
        if (context.getJ2eeModuleName(j2eeModuleName) != null) {
            props.put(J2EE_MODULE, context.getJ2eeModuleName(j2eeModuleName));
        }
        return ObjectName.getInstance(context.getJ2eeDomainName(j2eeDomainName), props);
    }

}
