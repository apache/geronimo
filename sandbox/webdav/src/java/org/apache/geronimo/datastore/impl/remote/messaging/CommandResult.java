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

import java.io.Serializable;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/03 13:10:07 $
 */
public class CommandResult implements Serializable
{

    private final boolean isSuccess;
    
    private final Object opaque;
    
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
    
}
