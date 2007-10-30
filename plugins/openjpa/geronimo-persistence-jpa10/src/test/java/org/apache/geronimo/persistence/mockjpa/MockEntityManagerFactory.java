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

package org.apache.geronimo.persistence.mockjpa;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityManager;

/**
 * @version $Rev$ $Date$
 */
public class MockEntityManagerFactory implements EntityManagerFactory {

    private boolean open = true;

    public EntityManager createEntityManager() {
        if (!open) {
            throw new IllegalStateException("closed");
        }
        return new MockEntityManager();
    }

    public EntityManager createEntityManager(Map map) {
        if (!open) {
            throw new IllegalStateException("closed");
        }
        return new MockEntityManager(map);
    }

    public void close() {
        open = false;
    }

    public boolean isOpen() {
        return open;
    }
}
