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
 * Only exception which can be thrown by a GFileDAO implementation.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:16 $
 */
public class DAOException
    extends IOException
{

    /**
     * Exception with message.
     * 
     * @param aMessage Message.
     */
    public DAOException(String aMessage) {
        super(aMessage);
    }
    
    /**
     * Exception with nested throwable.
     * 
     * @param aThrowable Throwable.
     */
    public DAOException(Throwable aThrowable) {
        initCause(aThrowable);
    }
    
}
