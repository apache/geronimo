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

package org.apache.geronimo.webdav.jetty;

import org.apache.geronimo.webdav.Connector;

import org.mortbay.http.HttpListener;

/**
 * This interface is required by the IoC framework: JettyConnector is an
 * endpoint, whose implementation JettyConnectorImpl does not define a
 * constructor without parameters.
 * <BR>
 * This interface captures the operations and attributes, which are exposed
 * as GBean operations and attributes.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:20 $
 */
public interface JettyConnector extends Connector {
    HttpListener getListener();
}