/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.web;

import org.apache.geronimo.common.Component;

/* -------------------------------------------------------------------------------------- */
/**
 * WebConnector
 * 
 * 
 * @version $Revision: 1.2 $ $Date: 2003/08/27 10:32:05 $
 */
public interface WebConnector extends Component
{

    /*-------------------------------------------------------------------------------- */
    /** Port number of connector
    * @param port number on which to listen
    */
    public void setPort(int port);

    public int getPort();

    /*-------------------------------------------------------------------------------- */
    /** Protocol of connector
    * @param protocol eg http, https, ftp etc
    */
    public void setProtocol(String protocol);

    public String getProtocol();

    /*-------------------------------------------------------------------------------- */
    /** Interface of connector
    * @param iface (hostname or IP) on which to listen
    */
    public void setInterface(String iface);

    public String getInterface();

    /*-------------------------------------------------------------------------------- */
    /** Maximum number of connections supported by connector
    * @param maxConnects
    */
    public void setMaxConnections(int maxConnects);

    public int getMaxConnections();

    /*-------------------------------------------------------------------------------- */
    /** Maximum time (in ms) that a connection can be idle
     * before the connector will close it.
    * @param maxIdleTime time in msec
    */
    public void setMaxIdleTime(int maxIdleTime);

    public int getMaxIdleTime();

    /*-------------------------------------------------------------------------------- */
    /** Names of contexts that must be registered and started
     * in the associated web container before this connector will
     * accept connections.
    * @param contexts
    */
    public void setContexts(String[] contexts);

    public String[] getContexts();
}
