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
package org.apache.geronimo.corba;

import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.apache.openejb.ContainerType;

/**
 * @version $Revision: 477657 $ $Date: 2006-11-21 04:54:49 -0800 (Tue, 21 Nov 2006) $
 */
public final class AdapterWrapper {
    private final static Map<String,Adapter> adapters = new HashMap<String,Adapter>();
    private final TSSLink tssLink;
    private Adapter generator;

    public AdapterWrapper(TSSLink tssLink) {
        this.tssLink = tssLink;

    }

    public void start(ORB orb, POA poa, Policy securityPolicy) throws CORBAException {
        
        if (tssLink.getDeployment() == null || tssLink.getDeployment().getDeploymentInfo() == null) {
            
            throw new CORBAException("tssLink's ejb deployment info is not ready");

        }
        
        ContainerType containerType = tssLink.getDeployment().getContainer().getContainerType();
        switch (containerType) {
            case STATELESS:
                generator = new AdapterStateless(tssLink, orb, poa, securityPolicy);
                break;
            case STATEFUL:
                generator = new AdapterStateful(tssLink, orb, poa, securityPolicy);
                break;
            case BMP_ENTITY:
            case CMP_ENTITY:
                generator = new AdapterEntity(tssLink, orb, poa, securityPolicy);
                break;
            default:
                throw new CORBAException("CORBA Adapter does not handle MDB containers");
        }
        adapters.put(tssLink.getContainerId(), generator);
    }

    public void stop() throws CORBAException {
        
        if (generator != null) {
            generator.stop();
        }
        adapters.remove(tssLink.getContainerId());
    }

    public static RefGenerator getRefGenerator(String containerId) {
        return (RefGenerator) adapters.get(containerId);
    }
}
