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

package org.apache.geronimo.gbean.jmx;

import java.util.Set;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.WaitingException;

/**
 * @version $Revision: 1.5 $ $Date: 2004/05/27 01:05:59 $
 */
public interface Proxy {
    void destroy();

    Object getProxy();

    Set getTargets();

    void addTarget(ObjectName target);

    void removeTarget(ObjectName target);

    void start() throws WaitingException;

    void stop();
}
