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

package org.apache.geronimo.cxf.client;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.geronimo.webservices.saaj.SAAJUniverse;

public abstract class SAAJInterceptor extends AbstractPhaseInterceptor<Message> {

    private static boolean interceptorsRegistered = false;
    
    protected SAAJUniverse universe;

    public SAAJInterceptor(String phase, SAAJUniverse universe) {
        super(phase);
        this.universe = universe;
    }
    
    public static synchronized void registerInterceptors() {
        if (!interceptorsRegistered) {
            registerInterceptors(BusFactory.getDefaultBus());
            interceptorsRegistered = true;
        }
    }
    
    public static void registerInterceptors(Bus bus) {
        SAAJUniverse universe = new SAAJUniverse();
        bus.getOutInterceptors().add(new SAAJOutInterceptor(universe));
        bus.getInInterceptors().add(new SAAJInInterceptor(universe));
        bus.getInInterceptors().add(new SAAJInFaultInterceptor(universe));
    }
    
}
