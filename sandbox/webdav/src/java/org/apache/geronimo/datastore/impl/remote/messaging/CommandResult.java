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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Encapsulates the result of a CommandRequest.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/11 15:36:14 $
 */
public class CommandResult
    implements Externalizable
{

    /**
     * Indicates if the request has been successful.
     */
    private boolean isSuccess;
    
    /**
     * CommandRequest result.
     */
    private Object opaque;
    
    /**
     * Required for Externalization.
     */
    public CommandResult() {}
    
    /**
     * Creates a result of a CommandRequest.
     * 
     * @param anIsSuccess true if the request has been successful - no 
     * exception has been raised. 
     * @param anOpaque Result of the invocation.
     */
    public CommandResult(boolean anIsSuccess, Object anOpaque) {
        isSuccess = anIsSuccess;
        if ( !isSuccess && !(anOpaque instanceof Exception) ) {
            throw new IllegalArgumentException(
                "If failure, opaque must be an Exception");
        }
        opaque = anOpaque;
    }

    public boolean isSuccess() {
        return isSuccess;
    }
    
    public Exception getException() {
        return (Exception) opaque; 
    }
    
    public Object getResult() {
        return opaque;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(isSuccess);
        out.writeObject(opaque);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        isSuccess = in.readBoolean();
        opaque = in.readObject();
    }
    
}
