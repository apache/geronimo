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

package org.apache.geronimo.connector.outbound.connectiontracking.defaultimpl;

import java.util.HashMap;
import java.util.Map;

import org.apache.geronimo.transaction.InstanceContext;


/**
 * Simple implementation of ComponentContext satisfying invariant.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:57:11 $
 *
 * */
public class DefaultComponentContext implements InstanceContext {

    private final Map connectionManagerMap = new HashMap();

    public Object getId() {
        return null;
    }

    public void setId(Object id) {
    }

    public Object getContainer() {
        return null;
    }

    public void associate() throws Exception {
    }

    public void flush() throws Exception {
    }

    public void beforeCommit() throws Exception {
    }

    public void afterCommit(boolean status) throws Exception {
    }

    public Map getConnectionManagerMap() {
        return connectionManagerMap;
    }

}
