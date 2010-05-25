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
package org.apache.geronimo.cxf.pojo;

import org.apache.cxf.Bus;
import org.apache.geronimo.cxf.CXFEndpoint;
import org.apache.geronimo.cxf.CXFWebServiceContainer;
import org.osgi.framework.Bundle;

public class POJOWebServiceContainer extends CXFWebServiceContainer {

    public POJOWebServiceContainer(Bus bus, Class target, Bundle bundle) {
        super(bus, target, bundle);
        this.destination.setPassSecurityContext(true);
    }

    protected CXFEndpoint publishEndpoint(Object target) {
        assert target != null : "null target received";

        POJOEndpoint ep = new POJOEndpoint(bus, (Class) target, bundle);
        ep.publish(null);
        return ep;
    }

}
