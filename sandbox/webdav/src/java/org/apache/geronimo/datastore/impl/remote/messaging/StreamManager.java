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

package org.apache.geronimo.datastore.impl.remote.messaging;

import java.io.IOException;
import java.io.InputStream;


/**
 * Tracks InputStream instances.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public interface StreamManager extends Connector
{
    
    /**
     * A StreamManager is registered automatically by a ServerNode and a
     * ServantNode with this name.
     */
    public static final String NAME = "StreamManager";
    
    /**
     * Registers the provided InputStream.
     * 
     * @param anIn InputStream to be tracked.
     * @return An opaque Object identifying this stream. It must be used to 
     * retrieve the registered stream.
     */
    public Object register(InputStream anIn);
    
    /**
     * Retrieves the InputStream having the provided id.
     * 
     * @param anId Identifier.
     * @return InputStream having this id.
     * @exception IOException Indicates that no InputStream is registered for
     * the provided identifier.
     */
    public InputStream retrieve(Object anId) throws IOException;
    
    /**
     * Reads from the InputStream having the specified identifier and returns 
     * the read bytes.
     * 
     * @param anID InputStream identifier.
     * @return byte block read from the InputStream identified by anID.
     * @throws IOException Indicates than an I/O error has occured.
     */
    public byte[] retrieveLocalNext(Object anID) throws IOException;
        
}
