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
package org.apache.geronimo.tomcat.interceptor;

import java.util.Set;

import javax.resource.ResourceException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.geronimo.transaction.DefaultInstanceContext;
import org.apache.geronimo.transaction.InstanceContext;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;

public class InstanceContextBeforeAfter implements BeforeAfter{
    
    private final BeforeAfter next;
    private final int index;
    private final Set unshareableResources;
    private final Set applicationManagedSecurityResources;
    private final TrackedConnectionAssociator trackedConnectionAssociator;

    public InstanceContextBeforeAfter(BeforeAfter next, int index, Set unshareableResources, Set applicationManagedSecurityResources, TrackedConnectionAssociator trackedConnectionAssociator) {
        this.next = next;
        this.index = index;
        this.unshareableResources = unshareableResources;
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
        this.trackedConnectionAssociator = trackedConnectionAssociator;
    }

    public void before(Object[] context, ServletRequest httpRequest, ServletResponse httpResponse) {
        try {
            context[index] = trackedConnectionAssociator.enter(new DefaultInstanceContext(unshareableResources, applicationManagedSecurityResources));
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
        if (next != null) {
            next.before(context, httpRequest, httpResponse);
        }
    }

    public void after(Object[] context, ServletRequest httpRequest, ServletResponse httpResponse) {
        if (next != null) {
            next.after(context, httpRequest, httpResponse);
        }
        try {
            trackedConnectionAssociator.exit((InstanceContext) context[index]);
        } catch (ResourceException e) {
            throw new RuntimeException(e);
        }
    }
}
