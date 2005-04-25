/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.tomcat.valve;

import java.io.IOException;
import java.util.Set;

import javax.resource.ResourceException;
import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;

/**
 * @version $Rev: $ $Date: $
 */
public class InstanceContextValve extends ValveBase {

    private final Set unshareableResources;

    private final Set applicationManagedSecurityResources;

    private final TrackedConnectionAssociator trackedConnectionAssociator;

    public InstanceContextValve(Set unshareableResources,
            Set applicationManagedSecurityResources,
            TrackedConnectionAssociator trackedConnectionAssociator) {
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void invoke(Request request, Response response) throws IOException,
            ServletException {

        try {
            InstanceContext oldContext = trackedConnectionAssociator
                    .enter(new DefaultInstanceContext(unshareableResources,
                            applicationManagedSecurityResources));

            // Pass this request on to the next valve in our pipeline
            getNext().invoke(request, response);

            // Set the old one back
            trackedConnectionAssociator.exit((InstanceContext) oldContext);
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }
}
