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

/**
 * Outbound Msg interceptor. It allows to push Msg to various target: a queue,
 * an OutputStream, another outbound Msg interceptor et cetera.
 * <BR>
 * It is also in charge of adding various specificities to Msg to be pushed.
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public interface MsgOutInterceptor
{

    /**
     * Pushes a Msg.
     * 
     * @param aMsg Msg to be pushed.
     */
    public void push(Msg aMsg);
    
}
