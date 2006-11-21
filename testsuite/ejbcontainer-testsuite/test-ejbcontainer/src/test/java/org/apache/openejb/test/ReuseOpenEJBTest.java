/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.test;

import junit.framework.TestCase;
import junit.framework.Test;

/**
 *
 * NOTE: reuse the junit tests in openejb2/itests
 *
 * @see ClientTestSuite
 * @version $Revision$ $Date$
 */
public class ReuseOpenEJBTest extends TestCase {

    public static Test suite() {
        System.setProperty("openejb.assembler", org.apache.openejb.assembler.Assembler.class.getName());
        System.setProperty("openejb.test.server", org.apache.openejb.test.IvmTestServer.class.getName());
        System.setProperty("openejb.test.database", org.apache.openejb.test.InstantDbTestDatabase.class.getName());
        return ClientTestSuite.suite();
    }
}
