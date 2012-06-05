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
package org.apache.geronimo.hook.equinox;

import java.util.LinkedHashSet;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;

public class BundleUtils {

    public static LinkedHashSet<Bundle> getWiredBundles(Bundle bundle) {
        LinkedHashSet<Bundle> wiredBundles = null;
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring != null) {
            wiredBundles = new LinkedHashSet<Bundle>();
            List<BundleWire> wires;
            wires = wiring.getRequiredWires(BundleRevision.PACKAGE_NAMESPACE);
            for (BundleWire wire : wires) {
                wiredBundles.add(wire.getProviderWiring().getBundle());
            }
            wires = wiring.getRequiredWires(BundleRevision.BUNDLE_NAMESPACE);
            for (BundleWire wire : wires) {
                wiredBundles.add(wire.getProviderWiring().getBundle());
            }
            wiredBundles.remove(bundle);
        } else {
            wiredBundles = new LinkedHashSet<Bundle>(0);
        }
        return wiredBundles;
    }
    
}
