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

package org.apache.geronimo.network.protocol;

/**
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:13 $
 */
public interface Protocol extends Cloneable {

    Protocol getUp();

    void setUp(Protocol up);

    Protocol getDown();

    void setDown(Protocol down);

    void clearLinks();

    Protocol cloneProtocol() throws CloneNotSupportedException;

    void doStart() throws ProtocolException;

    void doStop() throws ProtocolException;

    void sendUp(UpPacket packet) throws ProtocolException;

    void sendDown(DownPacket packet) throws ProtocolException;
}
