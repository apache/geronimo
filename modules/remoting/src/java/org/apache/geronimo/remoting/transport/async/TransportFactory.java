/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
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
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.geronimo.remoting.transport.async;

import java.net.URI;

import org.apache.geronimo.remoting.transport.TransportClient;
import org.apache.geronimo.remoting.transport.TransportServer;
import org.apache.geronimo.remoting.transport.async.bio.BlockingChannel;
import org.apache.geronimo.remoting.transport.async.bio.BlockingServer;
import org.apache.geronimo.remoting.transport.async.nio.NonBlockingChannel;
import org.apache.geronimo.remoting.transport.async.nio.NonBlockingServer;

/**
 * @version $Revision: 1.2 $ $Date: 2003/11/19 11:15:03 $
 */
public class TransportFactory extends org.apache.geronimo.remoting.transport.TransportFactory {

    /** 
     * Should we used Blocking IO instead of Non-blocking IO.  We default
     *  to using Non-blocking IO
     */
    static public final boolean USE_BLOCKING_IO =
        new Boolean(System.getProperty("org.apache.geronimo.remoting.transport.async.use_blocking_io", "true"))
            .booleanValue();

    static final public TransportFactory instance = new TransportFactory();
    public TransportFactory() {
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportFactory#handles(java.net.URI)
     */
    protected boolean handles(URI uri) {
        return "async".equals(uri.getScheme());
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportFactory#createClient()
     */
    public TransportClient createClient() {
        return new AsyncClient();
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportFactory#createSever()
     */
    public TransportServer createSever() {
        if (USE_BLOCKING_IO)
            return new BlockingServer();
        return new NonBlockingServer();
    }

    /**
     * Factory method to create AsynchChannel instances.
     */
    public Channel createAsynchChannel() {
        if (USE_BLOCKING_IO)
            return new BlockingChannel();
        return new NonBlockingChannel();
    }

    /**
     * @see org.apache.geronimo.remoting.transport.TransportFactory#doUnexport(java.lang.Object)
     */
    public boolean doUnexport(Object object) {
        return Registry.instance.unexportObject(object);
    }

}
