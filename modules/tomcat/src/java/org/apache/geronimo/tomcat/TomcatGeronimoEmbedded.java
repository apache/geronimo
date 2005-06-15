/**
*
* Copyright 2003-2004 The Apache Software Foundation
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
package org.apache.geronimo.tomcat;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Embedded;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.webservices.WebServiceContainer;

public class TomcatGeronimoEmbedded extends Embedded{
    
    private static final Log log = LogFactory.getLog(TomcatGeronimoEmbedded.class);
    
    public Context createContext(String path, String docBase, ClassLoader cl) {

        if( log.isDebugEnabled() )
            log.debug("Creating context '" + path + "' with docBase '" +
                       docBase + "'");

        GeronimoStandardContext context = new GeronimoStandardContext();

        context.setDocBase(docBase);
        context.setPath(path);
        context.setParentClassLoader(cl);
        
        ContextConfig config = new ContextConfig();
        config.setCustomAuthenticators(authenticators);
        ((Lifecycle) context).addLifecycleListener(config);

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
