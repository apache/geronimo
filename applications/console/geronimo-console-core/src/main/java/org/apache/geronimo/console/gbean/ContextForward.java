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
package org.apache.geronimo.console.gbean;

/**
 * Interface for a GBean that describes a context forward (that is, a
 * certain URL pattern in the console should be forwarded to a similar URL in
 * one of the portlet web applications without requiring a separate login).
 *
 * This is managed by a single servlet in the portal web app (say, mapped to
 * /forwards/*).  Therefore each ContextForward must specify three pieces of
 * information -- the prefix under the portal servlet that should be forwarded,
 * the context to forward to, and the path to forward to within the context.
 * For example, if it specified "myimages/", "myportlet", and "images" then a
 * request to /console/forwards/myimages/foo.png would be forwarded to
 * /myportlet/images/foo.png
 *
 * @version $Rev$ $Date$
 */
public interface ContextForward {
    /**
     * Gets the prefix for the portal servlet's path that should be forwarded
     * according to this definition.  This is "myimages/" in the example above.
     */
    String getPortalPathPrefix();
    /**
     * Gets the context root that this request should forward to ("myportlet"
     * in the example above).
     */
    String getPortletContextPath();

    /**
     * Gets the servlet path that this request should forward to in the
     * destination context.  Any data after the servlet path and portal path
     * prefix in the original URL will be appended to the portlet context
     * path and servlet path to construct the destination URL.
     */
    String getPortletServletPath();
}
