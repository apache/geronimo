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
package org.apache.geronimo.tomcat;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Valve;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Embedded;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.webservices.WebServiceContainer;

/**
 * @version $Rev$ $Date$
 */
public class TomcatGeronimoEmbedded extends Embedded{
    
    private static final Log log = LogFactory.getLog(TomcatGeronimoEmbedded.class);
    
    public Context createContext(String path, String docBase, ClassLoader cl) {

        if( log.isDebugEnabled() )
            log.debug("Creating context '" + path + "' with docBase '" +
                       docBase + "'");

        GeronimoStandardContext context = new GeronimoStandardContext();

        context.setDocBase(docBase);
        context.setPath(path);
        
        if (cl != null)
            context.setParentClassLoader(cl);
        
        // Add WAS CE specific authenticators
        InputStream is=this.getClass().getClassLoader().getResourceAsStream("org/apache/geronimo/tomcat/GeronimoCustomAuthenticator.properties");
        Properties props= new Properties();
        try {
            props.load(is);
        } catch (IOException e) {
           log.error("Unable to access GeronimoCustomAuthenticator.properties",e );
        }
        Map<String,String> customAuthenticators= new HashMap<String,String>((Map)props);
        Map<String,Valve> customAuthenticatorValves=new HashMap<String,Valve>();
        Iterator iterator=customAuthenticators.keySet().iterator();
        while(iterator.hasNext()){
            Object key=iterator.next();
            String value=customAuthenticators.get(key);
            Class authenticatorClass=null;
            Valve valve=null;
            try {
                authenticatorClass = Class.forName(value);
            } catch (ClassNotFoundException e) {
                log.error(MessageFormat.format("Unable to access class {0}",value),e);
            }
            try {
                valve = (Valve)authenticatorClass.newInstance();
            } catch (IllegalAccessException e) {
                log.error(MessageFormat.format("Unable to create an instance of the class {0}",value),e);
            } catch (InstantiationException e) {
                log.error(MessageFormat.format("Unable to access the constructor for the class {0}",value),e);
            }            
            customAuthenticatorValves.put((String)key, valve);
        }
        ContextConfig config = new ContextConfig();
        config.setCustomAuthenticators(customAuthenticatorValves);
        ((Lifecycle) context).addLifecycleListener(config);

        context.setDelegate(true);
        return (context);
    }

   public Context createEJBWebServiceContext(String contextPath, 
           WebServiceContainer webServiceContainer, 
           String securityRealmName, 
           String realmName, 
           String transportGuarantee, 
           String authMethod, 
           ClassLoader classLoader) {

        if( log.isDebugEnabled() )
            log.debug("Creating EJBWebService context '" + contextPath + "'.");

        TomcatEJBWebServiceContext context = new TomcatEJBWebServiceContext(contextPath, webServiceContainer, securityRealmName, realmName, transportGuarantee, authMethod, classLoader);

        ContextConfig config = new ContextConfig();
        config.setCustomAuthenticators(authenticators);
        ((Lifecycle) context).addLifecycleListener(config);

        return (context);

    }
    
}
