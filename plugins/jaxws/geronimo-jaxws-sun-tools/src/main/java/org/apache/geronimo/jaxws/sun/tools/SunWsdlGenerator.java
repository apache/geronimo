/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.jaxws.sun.tools;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.jaxws.builder.wsdl.WsdlGeneratorOptions;

public class SunWsdlGenerator implements org.apache.geronimo.jaxws.builder.wsdl.WsdlGenerator {

    public String generateWsdl(Module module,
                               String serviceClass,
                               DeploymentContext context,
                               WsdlGeneratorOptions options) throws DeploymentException {
        WsdlGenerator generator = new WsdlGenerator(options);                              
        return generator.generateWsdl(module, serviceClass, context, null);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(SunWsdlGenerator.class, GBeanInfoBuilder.DEFAULT_J2EE_TYPE);
        infoBuilder.addInterface(org.apache.geronimo.jaxws.builder.wsdl.WsdlGenerator.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
    
}
