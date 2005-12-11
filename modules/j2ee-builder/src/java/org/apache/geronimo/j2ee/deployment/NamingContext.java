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
package org.apache.geronimo.j2ee.deployment;

import java.util.Set;
import java.net.URI;

import javax.management.ObjectName;

import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public interface NamingContext {

    J2eeContext getJ2eeContext();

    void addGBean(GBeanData gbean);

    Set getGBeanNames();

    Set listGBeans(ObjectName pattern);

    GBeanData getGBeanInstance(ObjectName name) throws GBeanNotFoundException;

    URI getConfigID();

}
