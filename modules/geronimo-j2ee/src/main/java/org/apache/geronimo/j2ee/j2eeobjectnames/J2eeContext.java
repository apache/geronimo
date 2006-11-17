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
package org.apache.geronimo.j2ee.j2eeobjectnames;

/**
 * @version $Rev$ $Date$
 */
public interface J2eeContext {

    String getJ2eeDomainName();

    String getJ2eeServerName();

    String getJ2eeApplicationName();

    String getJ2eeModuleName();

    String getJ2eeName();

    String getJ2eeType();

    //these override methods return the argument it if is non-null, otherwise the same value as
    //the corresponding method above.

    String getJ2eeDomainName(String override);

    String getJ2eeServerName(String override);

    String getJ2eeApplicationName(String override);

    String getJ2eeModuleName(String override);

    String getJ2eeName(String override);

    String getJ2eeType(String override);

    String getJ2eeModuleType();

    String getJ2eeModuleType(String override);
}
