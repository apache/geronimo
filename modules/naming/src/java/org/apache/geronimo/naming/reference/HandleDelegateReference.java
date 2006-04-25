/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.naming.reference;

import javax.management.ObjectName;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;

/**
 * @version $Rev: 356097 $ $Date: 2005-12-11 17:29:03 -0800 (Sun, 11 Dec 2005) $
 */
public class HandleDelegateReference extends ConfigurationAwareReference {

    public HandleDelegateReference(Artifact configId, AbstractNameQuery abstractNameQuery) {
        super(configId, abstractNameQuery);
    }

    public String getClassName() {
        return "javax.ejb.spi.HandleDelegate";
    }

    public Object getContent() throws NamingException {
        Kernel kernel = getKernel();
        try {
            AbstractName targetName = resolveTargetName();
            return kernel.getAttribute(targetName, "handleDelegate");
        } catch (Exception e) {
            throw (NameNotFoundException) new NameNotFoundException("Error getting handle delegate attribute from CORBAGBean: name query =" + abstractNameQuery).initCause(e);
        }
    }
}
