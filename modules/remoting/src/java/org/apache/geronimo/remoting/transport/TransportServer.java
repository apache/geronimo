/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.remoting.transport;

import java.net.URI;

import org.apache.geronimo.core.service.Component;
import org.apache.geronimo.remoting.router.*;

/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:59:20 $
 */
public interface TransportServer extends Component {

    /**
     * Configures and otatains any resources needed to
     * start accepting client requests.  The bindURI argument
     * will configure the interface/port etc. that the server 
     * will use to service requests.
     * 
     * The sever should pass all requests and datagrams to the 
     * dispatcher.
     * 
     * @param bindURI
     * @throws Exception
     */
    void bind(URI bindURI, Router dispatcher) throws Exception;

    /**
     * Once the bind() call has been done, this method will 
     * return a URI that can be used by a client to connect 
     * to the server.
     * 
     * @return null if server has not been bound.
     */
    URI getClientConnectURI();

    /**
     * Enables the server to start accepting new client requests.
     * @throws Exception
     */
    void start() throws Exception;

    /**
     * Stops the server from accepting new client requests.
     * start() may be called at a later time to start processing
     * requests again.
     * 
     * @throws Exception
     */
    void stop() throws Exception;

    /**
     * Rleases all resources that were obtained during the life of
     * the server.  Once disposed, the sever instance cannot be used 
     * again.
     * @throws Exception
     */
    void dispose() throws Exception;

}
