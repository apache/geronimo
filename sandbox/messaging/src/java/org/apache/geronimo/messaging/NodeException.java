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

package org.apache.geronimo.messaging;

/**
 * Exception to be raised when an problem occur when a node can not performed
 * a specified operation.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:41 $
 */
public class NodeException extends Exception
{

    public NodeException(String aMsg) {
        super(aMsg);
    }
    
    public NodeException(String aMsg, Throwable aNested) {
        super(aMsg, aNested);
    }
    
    public NodeException(Throwable aNested) {
        super(aNested);
    }
    
}
