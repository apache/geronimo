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
package org.apache.geronimo.management.geronimo;

import org.apache.geronimo.management.J2EEResource;
import org.apache.geronimo.management.J2EEModule;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.AppClientModule;

/**
 * Represents the JSR-77 type with the same name
 *
 * @version $Rev$ $Date$
 */
public interface J2EEApplication extends org.apache.geronimo.management.J2EEApplication {
    /**
     * A list of J2EEResources deployed with this application.  This is not
     * a standard JSR-77 feature, I guess because most servers deploy most
     * resources at the top level, but RARs can always be embedded...
     */
    J2EEResource[] getResources();

    J2EEModule[] getModulesInstances();

    WebModule[] getWebModules();

    EJBModule[] getEJBModules();

    ResourceAdapterModule[] getRAModules();

    AppClientModule[] getClientModules();
}
