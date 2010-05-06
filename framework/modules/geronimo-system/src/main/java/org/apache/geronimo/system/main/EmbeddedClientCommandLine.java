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
package org.apache.geronimo.system.main;

import org.apache.geronimo.cli.client.ClientCLParser;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.util.Main;
import org.osgi.framework.Bundle;

/**
 * @version $Revision: 476049 $ $Date: 2006-11-17 15:35:17 +1100 (Fri, 17 Nov 2006) $
 */
public class EmbeddedClientCommandLine extends ClientCommandLine implements Main {
    
    private final Kernel kernel;
    private final Bundle bundle;

    public EmbeddedClientCommandLine(Kernel kernel, Bundle bundle) {
        if (null == kernel) {
            throw new IllegalArgumentException("kernel is required");
        }
        this.kernel = kernel;
        this.bundle = bundle;
    }

    public int execute(Object opaque) {
        if (! (opaque instanceof ClientCLParser)) {
            throw new IllegalArgumentException("Argument type is [" + opaque.getClass() + "]; expected [" + ClientCLParser.class + "]");
        }
        return super.execute((ClientCLParser) opaque);
    }
    
    @Override
    protected Kernel getBootedKernel() throws Exception {
        return kernel;
    }
    
    @Override
    protected void initializeKernel() throws Exception {
    }
    
    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(EmbeddedClientCommandLine.class, "EmbeddedClientCommandLine");
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("bundle", Bundle.class, false);
        infoFactory.setConstructor(new String[]{"kernel", "bundle"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public void doFail() {
    }
    
}
