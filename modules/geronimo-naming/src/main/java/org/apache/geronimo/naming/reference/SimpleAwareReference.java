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
package org.apache.geronimo.naming.reference;

import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Rev$ $Date$
 */
public abstract class SimpleAwareReference extends SimpleReference implements KernelAwareReference, ClassLoaderAwareReference {
    private transient Kernel kernel;
    private transient ClassLoader classLoader;

    public final Kernel getKernel() throws IllegalStateException {
        if (kernel == null) {
            throw new IllegalStateException("Kernel has not been set");
        }
        return kernel;
    }

    public final void setKernel(Kernel kernel) {
        this.kernel = kernel;
    }

    public ClassLoader getClassLoader() {
        if (classLoader == null) {
            throw new IllegalStateException("ClassLoader has not been set");
        }
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
