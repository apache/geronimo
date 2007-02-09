/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.security.config;

import org.omg.CORBA.ORB;

import org.apache.geronimo.corba.CORBABean;
import org.apache.geronimo.corba.CSSBean;


/**
 * Translates TSS and CSS configurations into CORBA startup args and properties.
 *
 * @version $Revision: 477622 $ $Date: 2006-11-21 03:03:24 -0800 (Tue, 21 Nov 2006) $
 */
public interface ConfigAdapter {

    /**
     * Create an ORB for a CORBABean server context.
     *
     * @param server The CORBABean that owns this ORB's configuration.
     *
     * @return An ORB instance configured for the CORBABean.
     * @exception ConfigException
     */
    public ORB createServerORB(CORBABean server)  throws ConfigException;
    /**
     * Create an ORB for a CSSBean nameservice client context.
     *
     * @param client The configured CSSBean used for access.
     *
     * @return An ORB instance configured for this client access.
     * @exception ConfigException
     */

    public ORB createNameServiceClientORB(CSSBean client)  throws ConfigException;
    /**
     * Create an ORB for a CSSBean client context.
     *
     * @param client The configured CSSBean used for access.
     *
     * @return An ORB instance configured for this client access.
     * @exception ConfigException
     */
    public ORB createClientORB(CSSBean client)  throws ConfigException;

    /**
     * Create a transient name service instance using the
     * specified host name and port.
     *
     * @param host   The String host name.
     * @param port   The port number of the listener.
     *
     * @return An opaque object that represents the name service.
     * @exception ConfigException
     */
    public Object createNameService(String host, int port) throws ConfigException;
    /**
     * Destroy a name service instance created by a
     * prior call to createNameService().
     *
     * @param ns     The opaque name service object returned from a
     *               prior call to createNameService().
     */
    public void destroyNameService(Object ns);
}
