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
package org.apache.geronimo.kernel;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
public final class KernelRegistry {
    /**
     * Index of kernel references by kernel name
     */
    private static final Map kernels = new HashMap();

    /**
     * ReferenceQueue that watches the weak references to our kernels
     */
    private static final ReferenceQueue queue = new ReferenceQueue();

    public static Set getKernelNames() {
        synchronized(kernels) {
            return Collections.unmodifiableSet(kernels.keySet());
        }
    }

    /**
     * Get a particular kernel indexed by a name
     *
     * @param name the name of the kernel to be obtained
     * @return the kernel that was registered with that name
     */
    public static Kernel getKernel(String name) {
        if (name == null) {
            return getSingleKernel();
        }
        synchronized (kernels) {
            processQueue();
            KernelReference ref = (KernelReference) kernels.get(name);
            if (ref != null) {
                return (Kernel) ref.get();
            }
        }
        return null;
    }

    /**
     * Obtain the single kernel that's registered.
     * <p/>
     * <p>This method assumes that there is only one kernel registered and will throw an
     * <code>IllegalStateException</code> if more than one has been registered.
     *
     * @return the single kernel that's registered
     * @throws IllegalStateException if more than one
     */
    public static Kernel getSingleKernel() {
        synchronized (kernels) {
            processQueue();

            int size = kernels.size();
            if (size > 1) throw new IllegalStateException("More than one kernel has been registered.");
            if (size < 1) return null;

            Kernel result = (Kernel) ((KernelReference) kernels.values().iterator().next()).get();
            if (result == null) {
                kernels.clear();
            }
            return result;
        }
    }

    public static void registerKernel(Kernel kernel) {
        synchronized (kernels) {
            String kernelName = kernel.getKernelName();
            if (kernels.containsKey(kernelName)) {
                throw new IllegalStateException("A kernel is already running this kernel name: " + kernelName);
            }
            kernels.put(kernelName, new KernelReference(kernelName, kernel));
        }
    }

    public static void unregisterKernel(Kernel kernel) {
        synchronized (kernels) {
            kernels.remove(kernel.getKernelName());
        }
    }

    private static void processQueue() {
        KernelReference kernelRef;
        while ((kernelRef = (KernelReference) queue.poll()) != null) {
            synchronized (kernels) {
                kernels.remove(kernelRef.key);
            }
        }
    }

    private static class KernelReference extends WeakReference {
        private final Object key;

        public KernelReference(Object key, Object kernel) {
            super(kernel, queue);
            this.key = key;
        }
    }
}
