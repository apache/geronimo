/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.Handler;
import org.apache.axis.deployment.wsdd.WSDDProvider;
import org.apache.axis.deployment.wsdd.WSDDService;

/**
 * refering to axis comment of pluggable providers
 * Look for file META-INF/services/org.apache.axis.deployment.wsdd.Provider
 * in all the JARS, get the classes listed in those files and add them to
 * providers list if they are valid providers.
 * Here is how the scheme would work.
 * A company providing a new provider will jar up their provider related
 * classes in a JAR file. The following file containing the name of the new
 * provider class is also made part of this JAR file.
 * META-INF/services/org.apache.axis.deployment.wsdd.Provider
 * By making this JAR part of the webapp, the new provider will be
 * automatically discovered.
 *
 * @author Srinath Perera(hemapani@opensource.lk)
 */
public class WSDDJavaGeronimoProvider extends WSDDProvider {
    public static final String PROVIDER_EWS = "geronimo";

    public String getName() {
        return PROVIDER_EWS;
    }

    public Handler newProviderInstance(WSDDService service,
                                       EngineConfiguration registry)
            throws Exception {
        return new GeronimoProvider();
    }
}
