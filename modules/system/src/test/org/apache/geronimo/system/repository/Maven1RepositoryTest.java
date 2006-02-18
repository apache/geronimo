/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.system.repository;

import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public class Maven1RepositoryTest extends AbstractRepositoryTest {
    protected void setUp() throws Exception {
        super.setUp();
        rootRepoDir = new File("src/test-repo/m1");
        repository = new Maven1Repository(rootRepoDir);
    }
}
