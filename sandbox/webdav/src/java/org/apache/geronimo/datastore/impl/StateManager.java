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

package org.apache.geronimo.datastore.impl;

import java.io.IOException;

/**
 * Manages the state of a component.
 *
 * @version $Rev$ $Date$
 */
public interface StateManager extends DirtyMarker
{

    /**
     * Prepares the component to be stored. 
     * 
     * @throws IOException If an I/O error has occurred.
     */
    public void prepare() throws IOException;
    
    /**
     * Flushes the component to the data store. This call is performed after
     * prepare. A flush should always succeed and hence does not throw
     * exceptions.
     */
    public void flush();
    
    /**
     * Unflushes the component which has been prepared to be stored. An unflush
     * should always succedd and hence does not throw exceptions.
     */
    public void unflush();
    
}
