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

package org.apache.geronimo.naming.proxy;

/**
 * ExternalProxies are supplied by the ProxyFactory to the  ReadOnlyContext, and registered
 * with the ProxyManager together with an id.  When a gbean supplying the same id is registered
 * with the ProxyManager, the ProxyManager calls setTarget on the ExternalProxy.  The externalProxy
 * is expected to use the internal proxy supplied to make itself usable.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:57 $
 *
 * */
public interface ExternalProxy {
    void setTarget(Object internalProxy);
}
