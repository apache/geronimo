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
package org.apache.geronimo.clustering.wadi;

import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.ParamReference;

/**
 * 
 * @version $Rev$ $Date$
 */

public class WadiStaticMember implements GBeanLifecycle {
    
    private static final Log log = LogFactory.getLog(WadiStaticMember.class);
    
    private final String classname;
    private final int port;
    private final int securePort;
    private final String host;
    private final String domain;
    private final byte[] uniqueId;
    private final WadiStaticMember  nextWadiStaticMember;
    
    private final StaticMember staticMember;
    
    public WadiStaticMember(@ParamAttribute(name=GBEAN_ATTR_CLASSNAME) String classname, 
            @ParamAttribute(name=GBEAN_ATTR_PORT) int port,
            @ParamAttribute(name=GBEAN_ATTR_SECURE_PORT) int securePort,
            @ParamAttribute(name=GBEAN_ATTR_HOST) String host,
            @ParamAttribute(name=GBEAN_ATTR_DOMAIN) String domain,
            @ParamAttribute(name=GBEAN_ATTR_UNIQUE_ID) byte[] uniqueId,
            @ParamReference(name=GBEAN_REF_NEXT_STATIC_MEMBER) WadiStaticMember nextWadiStaticMember) throws Exception {
        
        if (classname == null) {
            throw new IllegalArgumentException("classname is required");
        } else if (host == null) {
            throw new IllegalArgumentException("host is required");
        } else if (domain == null) {
            throw new IllegalArgumentException("domain is required");
        } else if (uniqueId == null) {
            throw new IllegalArgumentException("UniqueId is required");
        }
        
        this.classname = classname;
        this.port = port;
        this.securePort = securePort;
        this.host = host;
        this.domain = domain;
        this.uniqueId = uniqueId;
        this.nextWadiStaticMember = nextWadiStaticMember;
        
        staticMember = (StaticMember) Class.forName(classname).newInstance();
        staticMember.setPort(port);
        staticMember.setSecurePort(securePort);
        staticMember.setHost(host);
        staticMember.setDomain(domain);
        staticMember.setUniqueId(uniqueId);        
        
    }

    public void doStart() throws Exception {
        log.debug("Started WadiStaticMember");
    }
    
    public void doFail() {
        log.debug("Failed to create WadiStaticMember");
    }

    public void doStop() throws Exception {
        log.debug("Stopped WadiStaticMember");

    }
    public Object getNextStaticMember() {
        return nextWadiStaticMember;
    }
    
    public Object getStaticMember() {
        return staticMember;
    }
    
    public static final String GBEAN_ATTR_CLASSNAME = "className";
    public static final String GBEAN_ATTR_PORT = "port";
    public static final String GBEAN_ATTR_SECURE_PORT = "securePort";
    public static final String GBEAN_ATTR_HOST = "host";
    public static final String GBEAN_ATTR_DOMAIN = "domain";
    public static final String GBEAN_ATTR_UNIQUE_ID = "UniqueId";
    public static final String GBEAN_REF_NEXT_STATIC_MEMBER = "nextWadiStaticMember";
    
}
