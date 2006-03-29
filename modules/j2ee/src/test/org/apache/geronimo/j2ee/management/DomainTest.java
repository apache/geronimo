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
package org.apache.geronimo.j2ee.management;

import org.apache.geronimo.management.J2EEDomain;

/**
 * @version $Rev: 383682 $ $Date$
 */
public class DomainTest extends Abstract77Test {
    private J2EEDomain domain;

    public void testStandardInterface() throws Exception {
        assertEquals(DOMAIN_DATA.getAbstractName().getObjectName().toString(), domain.getObjectName());
        assertObjectNamesEqual(new String[]{SERVER_DATA.getAbstractName().getObjectName().toString()}, domain.getServers());
    }

    public void testStandardAttributes() throws Exception {
        assertEquals(DOMAIN_DATA.getAbstractName().getObjectName().toString(), kernel.getAttribute(DOMAIN_DATA.getAbstractName(), "objectName"));
        assertObjectNamesEqual(new String[]{SERVER_DATA.getAbstractName().getObjectName().toString()}, (String[]) kernel.getAttribute(DOMAIN_DATA.getAbstractName(), "servers"));
    }

    protected void setUp() throws Exception {
        super.setUp();
        domain = (J2EEDomain) kernel.getGBean(DOMAIN_DATA.getAbstractName());
    }
}
