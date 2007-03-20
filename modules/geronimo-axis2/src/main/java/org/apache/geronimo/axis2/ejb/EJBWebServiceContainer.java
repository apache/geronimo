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

package org.apache.geronimo.axis2.ejb;

import org.apache.axis2.util.JavaUtils;
import org.apache.geronimo.axis2.Axis2WebServiceContainer;
import org.apache.geronimo.jaxws.PortInfo;

import javax.naming.Context;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public class EJBWebServiceContainer extends Axis2WebServiceContainer {

    private String contextRoot = null;
    
    public EJBWebServiceContainer(PortInfo portInfo,
                                    String endpointClassName,
                                    ClassLoader classLoader,
                                    Context context,
                                    URL configurationBaseUrl) {
        super(portInfo, endpointClassName, classLoader, context, configurationBaseUrl);
    }
    
    protected void initContextRoot(Request request) {       
        String servicePath = portInfo.getLocation();
        
        if (contextRoot == null || "".equals(contextRoot)) {
            String[] parts = JavaUtils.split(request.getContextPath(), '/');
            if (parts != null) {
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].length() > 0) {
                        contextRoot = parts[i];
                        break;
                    }
                }
            }
            if (contextRoot == null || request.getContextPath().equals("/")) {
                contextRoot = "/";
            } else { //when contextRoot is not "/"
                //set the servicePath here for EJB.
                //check if portInfo.getLocation() contains contextRoot, if so, strip it.
                int i = servicePath.indexOf(contextRoot);
                if (i > -1) {
                    servicePath = servicePath.substring(i + contextRoot.length() + 1);
                    servicePath.trim();
                }
            }
            configurationContext.setServicePath(servicePath);
            
            //need to setContextRoot after servicePath as cachedServicePath is only built 
            //when setContextRoot is called.
            configurationContext.setContextRoot(contextRoot);  
        } 
    }
}
