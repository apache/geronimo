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
 
 package org.apache.geronimo.console.bundlemanager;

import java.util.Arrays;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

public class ServiceInfo {
    private long serviceId;
    private String servicePid = null;
    private String[] objectClass;
    
    public ServiceInfo(ServiceReference ref){
        serviceId = (Long)ref.getProperty(Constants.SERVICE_ID);
        servicePid = (String)ref.getProperty(Constants.SERVICE_PID);
        objectClass = (String[])ref.getProperty(Constants.OBJECTCLASS);
        sortObjectClass();
    }
    
    public long getServiceId() {
        return serviceId;
    }

    public String getServicePid() {
        return servicePid;
    }

    public String[] getObjectClass() {
        return objectClass;
    }

    public String[] sortObjectClass(){
        Arrays.sort(objectClass);
        return objectClass;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        final ServiceInfo other = (ServiceInfo) o;
        if (this.serviceId != other.serviceId ) {
            return false;
        }

        return true;
    }
    
    @Override
    public int hashCode() {
        int hash = 11;
        hash = 17* hash + 19 * (int)serviceId;

        return hash;
    }
}
