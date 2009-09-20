/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.connector.deployment.annotation;

import javax.annotation.Resource;
import javax.annotation.Resources;

@Resources ({
                @Resource(name = "Resource1",                   // 2. env-entry
                          type = java.lang.String.class,
                          authenticationType = Resource.AuthenticationType.APPLICATION,
                          shareable = true,
                          description = "description1",
                          mappedName = "mappedName1"),
                @Resource(name = "Resource2",                   // 3. service-ref
                          type = javax.xml.ws.Service.class,
                          authenticationType = Resource.AuthenticationType.APPLICATION)
            })
public class ResourceAnnotationExample {

    @Resource(name = "Resource3",
              type = java.lang.Object.class,
              authenticationType = Resource.AuthenticationType.CONTAINER,
              shareable = true,
              mappedName = "mappedName3")
    boolean annotatedField1;

    @Resource(name = "Resource4",
              type = javax.xml.ws.Service.class,                // 3. service-ref
              shareable = false,
              description = "description4",
              mappedName = "mappedName4")
    String annotatedField2;

    //------------------------------------------------------------------------------------------
    // Method name (for setter-based injection) must follow JavaBeans conventions:
    // -- Must start with "set"
    // -- Have one parameter
    // -- Return void
    //------------------------------------------------------------------------------------------
    @Resource(name = "Resource5",                               // 1. resource-ref
              type = javax.sql.DataSource.class,
              authenticationType = Resource.AuthenticationType.CONTAINER,
              description = "description5",
              mappedName = "mappedName5")
    public void setAnnotatedMethod1(String string) {
    }

    @Resource                                                   // 2. env-entry
    public void setAnnotatedMethod2(String string) {
    }

}
