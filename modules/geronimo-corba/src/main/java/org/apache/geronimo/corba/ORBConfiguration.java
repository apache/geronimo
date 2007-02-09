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
package org.apache.geronimo.corba;

import org.apache.geronimo.corba.security.config.tss.TSSConfig;
import org.apache.geronimo.corba.security.config.ssl.SSLConfig;

/**
 * Interface implemented by both CORBABean and CSSBean
 * to provide common configuration information to
 * ConfigAdapter instances.
 * @version $Revision: 485135 $ $Date: 2006-12-09 20:25:29 -0800 (Sat, 09 Dec 2006) $
 */
public interface ORBConfiguration {
    /**
     * Provide the SSLConfig information to the ConfigAdapter.
     *
     * @return A configured SSLConfig GBean.
     */
    SSLConfig getSslConfig();
    /**
     * Retrieve the TSSConfig settings to a ConfigAdapter instance.
     *
     * @return An appropriate TSSConfig object containing transport-level
     *         security information.
     */
    TSSConfig getTssConfig();
}
