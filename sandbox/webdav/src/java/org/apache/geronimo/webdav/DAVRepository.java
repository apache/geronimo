/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.webdav;

import java.util.Map;

/**
 * A DAVRepository defines a WebDAV servlet along with its execution context.
 * <BR>
 * A WebDAV servlet is a servlet, which implements the WebDAV specific methods
 * , e.g. PROPFIND. This servlet is deployed and managed by the DAVServer,
 * which has mounted this repository.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:20 $
 */
public interface DAVRepository {
    /**
     * Gets the host name filter.
     * <BR>
     * If defined, only the requests for this host are forwarded to this
     * repository.
     *
     * @return Host name filter.
     */
    String getHost();

    /**
     * Gets the context of the WebDAV servlet.
     * <BR>
     *
     * @return Context name.
     */
    String getContext();

    /**
     * Gets the WebDAV servlet Class.
     *
     * @return WebDAV servlet class.
     */
    Class getHandlingServlet();

    /**
     * Gets the servlet context attributes.
     *
     * @return Map of attribute name to value.
     */
    Map getServletContextAttr();

    /**
     * Gets the servlet initialization parameters.
     *
     * @return Map of parameter name to value.
     */
    Map getServletInitParam();

}
