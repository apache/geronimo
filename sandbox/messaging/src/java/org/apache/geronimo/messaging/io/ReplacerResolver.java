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

package org.apache.geronimo.messaging.io;

import java.io.IOException;

/**
 * "Unifies" the resolveObject and replaceObject contracts used during 
 * deserialization and serialization respectively.
 * <BR>
 * ReplacerResolvers are chained components.
 *
 * @version $Revision: 1.2 $ $Date: 2004/05/27 14:18:13 $
 */
public interface ReplacerResolver {

    /**
     * Replaces obj with another object.
     * 
     * @param obj Object to be replaced.
     * @return Substitute. If obj is not to be replaced, then obj must be
     * returned.
     * @throws IOException Indicates an I/O error.
     */
    public Object replaceObject(Object obj) throws IOException;
        
    /**
     * Resolves/replaces obj with another object.
     * 
     * @param obj Object to be resolved/replaced.
     * @return Substitute. If obj is not to be replaced, then obj must be
     * returned.
     * @throws IOException Indicates an I/O error.
     */
    public Object resolveObject(Object obj) throws IOException;

    /**
     * Appends a ReplacerResolver to this instance.
     * 
     * @param aNext Next ReplacerResolver of the chain.
     * @return aNext.
     */
    public ReplacerResolver append(ReplacerResolver aNext);
    
    /**
     * Gets the next online element of the chain.
     * 
     * @return Next element.
     */
    public ReplacerResolver getNext();

    /**
     * Sets this instance online.
     * <BR>
     * This instance can be added to a chain.
     */
    public void online();
    
    /**
     * Sets this instance offline.
     * <BR>
     * This instance does no more belong to the chain.
     */
    public void offline();

    /**
     * Indicates if this instance if online.
     * 
     * @return true if online - it belongs to a chain.
     */
    public boolean isOffline();
    
}
