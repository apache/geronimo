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
package org.apache.geronimo.kernel;

import org.apache.geronimo.gbean.GBeanName;

/**
 * @version $Rev$ $Date$
 */
public class GBeanNotFoundException extends KernelException {
    private GBeanName gBeanName;

    public GBeanNotFoundException(GBeanName gBeanName) {
        super(gBeanName+" not found");
        this.gBeanName = gBeanName;
    }

    public GBeanNotFoundException(GBeanName gBeanName, Throwable cause) {
        super(gBeanName+" not found", cause);
        this.gBeanName = gBeanName;
    }

    public GBeanName getGBeanName() {
        return gBeanName;
    }
}
