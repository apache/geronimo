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

package org.apache.geronimo.cache;

/**
 * A factory for instances of Pooled or Cached objects
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:25 $
 */
public interface InstanceFactory {
    /**
     * Create an instance ready for insertion into the pool. This method
     * should have performed any initialization needed by the object's
     * lifecycle
     * @return an instance ready to be used
     * @throws Exception if there was a problem initializing the instance
     */
    Object createInstance() throws Exception;

    /**
     * Destroy an instance that the pool decided was not needed any longer.
     * This method should perform any shutdown needed by the lifecycle
     * @param instance the instance to destroy
     */
    void destroyInstance(Object instance);
}
