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
package org.apache.geronimo.jaxws.client;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;

public class EndpointInfo implements Serializable {

    private URL location;
    private String credentialsName;
    private boolean mtomEnabled;
    private Map<String, Object> properties;
    
    public EndpointInfo(URL location, String credentialsName, boolean mtomEnabled, Map<String, Object> properties) {
        this.location = location;
        this.credentialsName = credentialsName;  
        this.mtomEnabled = mtomEnabled;        
        this.properties = properties;
    }

    public boolean isMTOMEnabled() {
        return this.mtomEnabled;
    }
    
    public URL getLocation() {
        return this.location;
    }
    
    public String getCredentialsName() {
        return this.credentialsName;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public String toString() {
        return this.location + " " + this.credentialsName + " " + this.mtomEnabled + " " + this.properties;
    }
}
