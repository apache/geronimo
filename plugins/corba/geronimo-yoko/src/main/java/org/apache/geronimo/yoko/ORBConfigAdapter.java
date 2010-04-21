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
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package org.apache.geronimo.yoko;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.rmi.CORBA.UtilDelegate;
import org.apache.geronimo.corba.CORBABean;
import org.apache.geronimo.corba.CSSBean;
import org.apache.geronimo.corba.NameService;
import org.apache.geronimo.corba.ORBConfiguration;
import org.apache.geronimo.corba.security.config.ConfigAdapter;
import org.apache.geronimo.corba.security.config.ConfigException;
import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.corba.security.config.tss.TSSSSLTransportConfig;
import org.apache.geronimo.corba.security.config.tss.TSSTransportMechConfig;
import org.apache.geronimo.corba.util.UtilDelegateImpl;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.yoko.orb.CosNaming.tnaming.TransientNameService;
import org.apache.yoko.orb.CosNaming.tnaming.TransientServiceException;
import org.apache.yoko.orb.OB.ZERO_PORT_POLICY_ID;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A ConfigAdapter instance for the Apache Yoko
 * CORBA support.
 * @version $Revision: 497125 $ $Date: 2007-01-17 10:51:30 -0800 (Wed, 17 Jan 2007) $
 */
@GBean(j2eeType = NameFactory.ORB_CONFIG)
public class ORBConfigAdapter implements ConfigAdapter {

    private final Logger log = LoggerFactory.getLogger(ORBConfigAdapter.class);

    /**
     * Start the config adapter GBean.  This is basically
     * an opportunity to set any system properties
     * required to make the ORB hook ups.  In particular,
     * this makes the ORB hookups for the RMI over IIOP
     * support.
     *
     * @exception Exception due to class loading problems or narrow not working
     */
    public ORBConfigAdapter(@ParamSpecial(type = SpecialAttributeType.bundle) Bundle bundle) throws Exception {
        // define the default ORB for ORB.init();
//        System.setProperty("org.omg.CORBA.ORBClass", "org.apache.yoko.orb.CORBA.ORB");
//        System.setProperty("org.omg.CORBA.ORBSingletonClass", "org.apache.yoko.orb.CORBA.ORBSingleton");

        // redirect the RMI implementation to use the Yoko ORB.
//        System.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass", "org.apache.yoko.rmi.impl.PortableRemoteObjectImpl");
//        System.setProperty("javax.rmi.CORBA.StubClass", "org.apache.yoko.rmi.impl.StubImpl");
        // this hooks the util class and allows us to override certain functions
//        System.setProperty("javax.rmi.CORBA.UtilClass", "org.apache.geronimo.corba.util.UtilDelegateImpl");
        // this tells the openejb UtilDelegateImpl which implementation to delegate non-overridden
        // operations to.
//        System.setProperty("org.apache.geronimo.corba.UtilDelegateClass", "org.apache.yoko.rmi.impl.UtilImpl");
        // this allows us to hook RMI stub invocation/serialization events.
//        UtilDelegateImpl.setDelegateClass(bundle.loadClass("org.apache.yoko.rmi.impl.UtilImpl").asSubclass(UtilDelegate.class));
//        System.setProperty("org.apache.yoko.rmi.RMIStubInitializerClass", "org.apache.geronimo.yoko.RMIStubHandlerFactory");

        // ok, now we have a potential classloading problem because of where our util delegates are located.
        // by forcing these classes to load now using our class loader, we can ensure things are properly initialized
//        Class clazz = bundle.loadClass("javax.rmi.PortableRemoteObject");
//        Method m = clazz.getMethod("narrow", Object.class, Class.class);
//        m.invoke(null, new Object(), Object.class);


        log.debug("Started  Yoko ORBConfigAdapter");
    }

    /**
     * Create an ORB for a CORBABean server context.
     *
     * @param server The CORBABean that owns this ORB's configuration.
     *
     * @return An ORB instance configured for the CORBABean.
     * @exception ConfigException
     */
    public ORB createServerORB(CORBABean server)  throws ConfigException {
        ORB orb = createORB(server.getURI(), server, translateToArgs(server), translateToProps(server));

        // check the tss config for a transport mech definition.  If we have one, then 
        // the port information will be passed in that config, and the port in the IIOP profile 
        // needs to be zero. 
        TSSConfig config = server.getTssConfig();
        TSSTransportMechConfig transportMech = config.getTransport_mech();
        if (transportMech != null) {
            if (transportMech instanceof TSSSSLTransportConfig) {
                Any any = orb.create_any();
                any.insert_boolean(true);

                try {
                    Policy portPolicy = orb.create_policy(ZERO_PORT_POLICY_ID.value, any);
                    Policy[] overrides = new Policy [] { portPolicy };
                    server.setPolicyOverrides(overrides);
                } catch (org.omg.CORBA.PolicyError e) {
                    // shouldn't happen, but we'll let things continue with no policy set. 
                }

            }
        }

        return orb;
    }

    /**
     * Create an ORB for a CSSBean client context.
     *
     * @param client The configured CSSBean used for access.
     *
     * @return An ORB instance configured for this client access.
     * @exception ConfigException
     */
    public ORB createClientORB(CSSBean client)  throws ConfigException {
        return createORB(client.getURI(), client, translateToArgs(client), translateToProps(client));
    }

    /**
     * Create an ORB for a CSSBean name service client context.
     *
     * @param client The configured CSSBean used for access.
     *
     * @return An ORB instance configured for this client access.
     * @exception ConfigException
     */
    public ORB createNameServiceClientORB(CSSBean client)  throws ConfigException {
        return createORB(client.getURI(), client, translateToArgs(client), translateToNameServiceProps(client));
    }

    /**
     * Create a transient name service instance using the
     * specified host name and port.
     *
     * @param host   The String host name.
     * @param port   The port number of the listener.
     *
     * @return An opaque object that represents the name service.
     * @exception ConfigException
     */
    public Object createNameService(String host, int port) throws ConfigException {
        try {
            // create a name service using the supplied host and publish under the name "NameService"
            TransientNameService service = new TransientNameService(host, port, "NameService") {
                public void run() throws TransientServiceException {
                    // Create an ORB object
                    java.util.Properties props = new Properties();
                    props.putAll(System.getProperties());

                    props.put("org.omg.CORBA.ORBServerId", "1000000" ) ;
                    props.put("org.omg.CORBA.ORBClass", "org.apache.yoko.orb.CORBA.ORB");
                    props.put("org.omg.CORBA.ORBSingletonClass", "org.apache.yoko.orb.CORBA.ORBSingleton");
                    props.put("yoko.orb.oa.endpoint", "iiop --bind " + host  + " --host " + host + " --port " + port );

                    createdOrb = ORB.init((String[])null, props) ;

                    // now initialize the service
                    initialize(createdOrb);
                }
            };
            service.run();
            log.debug("Creating ORB endpoint with host=" + host + ", port=" + port);            
            // the service instance is returned as an opaque object.
            return service;
        } catch (TransientServiceException e) {
            throw new ConfigException("Error starting transient name service on port " + port, e);
        }
    }

    /**
     * Destroy a name service instance created by a
     * prior call to createNameService().
     *
     * @param ns     The opaque name service object returned from a
     *               prior call to createNameService().
     */
    public void destroyNameService(Object ns) {
        // The name service instance handles its own shutdown.
        ((TransientNameService)ns).destroy();
    }

    /**
     * Create an ORB instance using the configured argument
     * and property bundles.
     *
     * @param name   The String name of the configuration GBean used to
     *               create this ORB.
     * @param config The GBean configuration object required by the
     *               SocketFactory instance.
     * @param args   The String arguments passed to ORB.init().
     * @param props  The property bundle passed to ORB.init().
     *
     * @return An ORB constructed from the provided args and properties.
     */
    private ORB createORB(String name, ORBConfiguration config, String[] args, Properties props) {
        return ORB.init(args, props);
    }

    /**
     * Translate a CORBABean configuration into an
     * array of arguments used to configure the ORB
     * instance.
     *
     * @param server The CORBABean we're creating an ORB instance for.
     *
     * @return A String{} array containing the initialization
     *         arguments.
     * @exception ConfigException if configuration cannot be interpreted
     */
    private String[] translateToArgs(CORBABean server) throws ConfigException {
        ArrayList<String> list = new ArrayList<String>();
//TODO GERONIMO-2687, I don't think it makes sense to associate a default principal with  a tss config, but if we need it
        //here's the disfunctional code.
//        TSSConfig config = server.getTssConfig();

        // if the TSSConfig includes principal information, we need to add argument values
        // for this information.
//        DefaultPrincipal principal = config.getDefaultPrincipal();
//        if (principal != null) {
//            if (principal instanceof DefaultRealmPrincipal) {
//                DefaultRealmPrincipal realmPrincipal = (DefaultRealmPrincipal) principal;
//                list.add("default-realm-principal::" + realmPrincipal.getRealm() + ":" + realmPrincipal.getDomain() + ":"
//                         + realmPrincipal.getPrincipal().getClassName() + ":" + realmPrincipal.getPrincipal().getPrincipalName());
//            } else if (principal instanceof DefaultDomainPrincipal) {
//                DefaultDomainPrincipal domainPrincipal = (DefaultDomainPrincipal) principal;
//                list.add("default-domain-principal::" + domainPrincipal.getDomain() + ":"
//                         + domainPrincipal.getPrincipal().getClassName() + ":" + domainPrincipal.getPrincipal().getPrincipalName());
//            } else {
//                list.add("default-principal::" + principal.getPrincipal().getClassName() + ":" + principal.getPrincipal().getPrincipalName());
//            }
//        }

        // enable the connection plugin
        enableSocketFactory(server.getURI(), list);

        NameService nameService = server.getNameService();
        // if we have a name service to enable as an initial ref, add it to the init processing.
        if (nameService != null) {
            list.add("-ORBInitRef");
            list.add("NameService=" + nameService.getURI());
        }

        if (log.isDebugEnabled()) {
            for (String configArg : list) {
                log.debug(configArg);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    private Properties translateToProps(CORBABean server) throws ConfigException {
        Properties result = new Properties();

        result.put("org.omg.CORBA.ORBClass", "org.apache.yoko.orb.CORBA.ORB");
        result.put("org.omg.CORBA.ORBSingletonClass", "org.apache.yoko.orb.CORBA.ORBSingleton");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.geronimo.corba.transaction.TransactionInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.geronimo.corba.security.SecurityInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.geronimo.yoko.ORBInitializer", "");
        // don't specify the port if we're allowing this to default.
        if (server.getPort() > 0) {
            result.put("yoko.orb.oa.endpoint", "iiop --bind " + server.getHost() + " --host " + server.getHost() + " --port " + server.getPort());
        }
        else {
            result.put("yoko.orb.oa.endpoint", "iiop --bind " + server.getHost()+ " --host " + server.getHost());
        }
        
        // this gives us a connection we can use to retrieve the ORB configuration in the 
        // interceptors. 
        result.put("yoko.orb.id", server.getURI()); 

        // check the tss config for a transport mech definition.  If we have one, then 
        // the port information will be passed in that config, and the port in the IIOP profile 
        // needs to be zero. 
        TSSConfig config = server.getTssConfig();
        TSSTransportMechConfig transportMech = config.getTransport_mech();
        if (transportMech != null) {
            if (transportMech instanceof TSSSSLTransportConfig) {
                result.put("yoko.orb.policy.zero_port", "true");
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("translateToProps(TSSConfig)");
            for (Enumeration iter = result.keys(); iter.hasMoreElements();) {
                String key = (String) iter.nextElement();
                log.debug(key + " = " + result.getProperty(key));
            }
        }
        return result;
    }

    /**
     * Translate a CSSBean configuration into the
     * argument bundle needed to instantiate the
     * ORB instance.
     *
     * @param client The CSSBean holding the configuration.
     *
     * @return A String array to be passed to ORB.init().
     * @exception ConfigException if configuration cannot be interpreted
     */
    private String[] translateToArgs(CSSBean client) throws ConfigException {
        ArrayList<String> list = new ArrayList<String>();

        // enable the connection plugin
        enableSocketFactory(client.getURI(), list);

        if (log.isDebugEnabled()) {
            for (String configArg : list) {
                log.debug(configArg);
            }
        }

        return list.toArray(new String[list.size()]);
    }

    /**
     * Add arguments to the ORB.init() argument list
     * required to enable the SocketFactory used for
     * SSL support.
     *
     * @param uri    The URI name of the configuration GBean (either a
     *               CSSBean or a CORBABean).
     * @param args configuration arguments to add to
     */
    private void enableSocketFactory(String uri, List<String> args) {
        args.add("-IIOPconnectionHelper");
        args.add("org.apache.geronimo.yoko.SocketFactory");
        args.add("-IIOPconnectionHelperArgs");
        args.add(uri);
    }


    /**
     * Translate a CSSBean configuration into the
     * property bundle necessary to configure the
     * ORB instance.
     *
     * @param client The CSSBean holding the configuration.
     *
     * @return A property bundle that can be passed to ORB.init();
     * @exception ConfigException if configuration cannot be interpreted
     */
    private Properties translateToProps(CSSBean client) throws ConfigException {
        Properties result = new Properties();

        result.put("org.omg.CORBA.ORBClass", "org.apache.yoko.orb.CORBA.ORB");
        result.put("org.omg.CORBA.ORBSingletonClass", "org.apache.yoko.orb.CORBA.ORBSingleton");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.geronimo.corba.transaction.TransactionInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.geronimo.corba.security.SecurityInitializer", "");
        result.put("org.omg.PortableInterceptor.ORBInitializerClass.org.apache.geronimo.yoko.ORBInitializer", "");

        // this gives us a connection we can use to retrieve the ORB configuration in the 
        // interceptors. 
        result.put("yoko.orb.id", client.getURI()); 

        if (log.isDebugEnabled()) {
            log.debug("translateToProps(CSSConfig)");
            for (Enumeration iter = result.keys(); iter.hasMoreElements();) {
                String key = (String) iter.nextElement();
                log.debug(key + " = " + result.getProperty(key));
            }
        }
        return result;
    }


    /**
     * Translate a CSSBean configuration into the
     * property bundle necessary to configure the
     * ORB instance.
     *
     * @param client The CSSBean holding the configuration.
     *
     * @return A property bundle that can be passed to ORB.init();
     * @exception ConfigException if configuration cannot be interpreted
     */
    private Properties translateToNameServiceProps(CSSBean client) throws ConfigException {
        Properties result = new Properties();

        result.put("org.omg.CORBA.ORBClass", "org.apache.yoko.orb.CORBA.ORB");
        result.put("org.omg.CORBA.ORBSingletonClass", "org.apache.yoko.orb.CORBA.ORBSingleton");

        if (log.isDebugEnabled()) {
            log.debug("translateToNameServiceProps(CSSConfig)");
            for (Enumeration iter = result.keys(); iter.hasMoreElements();) {
                String key = (String) iter.nextElement();
                log.debug(key + " = " + result.getProperty(key));
            }
        }
        return result;
    }
}
