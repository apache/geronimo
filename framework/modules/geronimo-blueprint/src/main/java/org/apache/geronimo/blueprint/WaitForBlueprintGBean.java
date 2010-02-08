/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.blueprint;

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.blueprint.container.BlueprintEvent;
import org.osgi.service.blueprint.container.BlueprintListener;

/**
 * Blueprint starts beans asynchornously whereas geronimo starts configurations in a single thread.
 * This bean waits for the blueprint activity to complete before returning.
 *
 * @version $Rev$ $Date$
 */

@GBean
public class WaitForBlueprintGBean {
    private volatile BlueprintEvent event;
    private CountDownLatch latch = new CountDownLatch(1);

    public WaitForBlueprintGBean(@ParamSpecial(type= SpecialAttributeType.bundleContext)BundleContext bundleContext) throws Exception {
        final Bundle bundle = bundleContext.getBundle();
        BlueprintListener listener = new BlueprintListener() {

            @Override
            public void blueprintEvent(BlueprintEvent event) {
                if (event.getBundle() == bundle) {
                    if (event.getType() == BlueprintEvent.CREATED || event.getType() == BlueprintEvent.FAILURE) {
                        WaitForBlueprintGBean.this.event = event;
                        latch.countDown();
                    }
                }
            }
        };
        ServiceRegistration registration = bundleContext.registerService(BlueprintListener.class.getName(), listener, new Hashtable());
        latch.await();
        registration.unregister();
        if (event.getType() == BlueprintEvent.FAILURE) {
            throw new Exception("Could not start blueprint plan", event.getCause());
        }
    }

}
