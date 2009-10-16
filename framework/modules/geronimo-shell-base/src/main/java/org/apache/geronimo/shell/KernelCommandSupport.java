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


package org.apache.geronimo.shell;

import org.apache.felix.karaf.shell.console.OsgiCommandSupport;
import org.apache.geronimo.kernel.Kernel;
import org.osgi.framework.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public abstract class KernelCommandSupport extends OsgiCommandSupport {
    protected Object doExecute() throws Exception {

        ServiceReference ref = getBundleContext().getServiceReference(Kernel.class.getName());
        if (ref == null) {
            System.out.println("FeaturesService service is unavailable.");
            return null;
        }
        try {
            Kernel kernel = (Kernel) getBundleContext().getService(ref);
            if (kernel == null) {
                System.out.println("FeaturesService service is unavailable.");
                return null;
            }

            doExecute(kernel);
        }
        finally {
            getBundleContext().ungetService(ref);
        }
        return null;
    }

    protected abstract void doExecute(Kernel kernel) throws Exception;

}
