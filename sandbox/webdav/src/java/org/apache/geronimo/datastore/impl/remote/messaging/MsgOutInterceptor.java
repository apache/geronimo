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
 * Outbound Msg interceptor. It allows to push Msg to various targets: a queue,
 * an OutputStream, another outbound Msg interceptor et cetera.
 * <BR>
 * It is also in charge of adding various specificities to Msg to be pushed.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/11 15:36:14 $
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
