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

package org.apache.geronimo.transaction;

import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @version $Revision: 1.6 $ $Date: 2004/05/31 23:37:05 $
 *
 * */
public interface InstanceContext {
    Object getId();

    void setId(Object id);

    Object getContainerId();

    void associate() throws Throwable;

    void flush() throws Throwable;

    void beforeCommit() throws Exception;

    void afterCommit(boolean status) throws Exception;

    /**
     * IMPORTANT INVARIANT: this should always return a map, never null.
     * @return map of ConnectionManager to (list of ) managed connection info objects.
     */
    Map getConnectionManagerMap();

    Set getUnshareableResources();

    Set getApplicationManagedSecurityResources();

}
