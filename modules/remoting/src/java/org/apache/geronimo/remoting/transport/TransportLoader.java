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
package org.apache.geronimo.remoting.transport;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.remoting.router.Router;

/**
 * @version $Revision: 1.5 $ $Date: 2004/01/25 21:07:04 $
 */
public class TransportLoader implements GBean {
    private URI bindURI;
    private TransportServer transportServer;
    private Router router;

    public URI getBindURI() {
        return bindURI;
    }

    public void setBindURI(URI bindURI) {
        this.bindURI = bindURI;
    }

    public Router getRouter() {
        return router;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public URI getClientConnectURI() {
        if (transportServer == null)
            return null;
        return transportServer.getClientConnectURI();
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws Exception {
        if (router == null) {
            throw new IllegalStateException("Target router was not set.");
        }
        TransportFactory tf = TransportFactory.getTransportFactory(bindURI);
        transportServer = tf.createSever();
        transportServer.bind(bindURI, router);
        transportServer.start();
    }

    public void doStop() throws Exception {
        transportServer.stop();
        transportServer.dispose();
        transportServer = null;
    }

    public void doFail() {
        // @todo do your best to clean up after a failure
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        Set attributes = new HashSet();
        attributes.add(new GAttributeInfo("ClientConnectURI"));
        attributes.add(new GAttributeInfo("BindURI", true));
        Set endpoints = new HashSet();
        endpoints.add(new GReferenceInfo("Router", "org.apache.geronimo.remoting.router.Router"));
        GBEAN_INFO = new GBeanInfo(TransportLoader.class.getName(), attributes, null, null, endpoints, null);
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
