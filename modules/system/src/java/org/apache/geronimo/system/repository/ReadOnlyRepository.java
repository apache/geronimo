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
package org.apache.geronimo.system.repository;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBean;
import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GConstructorInfo;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 08:03:53 $
 */
public class ReadOnlyRepository implements Repository, GBean {
    private static final Log log = LogFactory.getLog(ReadOnlyRepository.class);
    private final URI root;
    private final ServerInfo serverInfo;
    private URI rootURI;

    public ReadOnlyRepository(File root) {
        this(root.toURI());
    }

    public ReadOnlyRepository(URI rootURI) {
        this.root = null;
        this.serverInfo = null;
        this.rootURI = rootURI;
    }

    public ReadOnlyRepository(URI root, ServerInfo serverInfo) {
        this.root = root;
        this.serverInfo = serverInfo;
    }

    public boolean hasURI(URI uri) {
        uri = rootURI.resolve(uri);
        if ("file".equals(uri.getScheme())) {
            File f = new File(uri);
            return f.exists() && f.canRead();
        } else {
            try {
                uri.toURL().openStream().close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public URL getURL(URI uri) throws MalformedURLException {
        return rootURI.resolve(uri).toURL();
    }

    public void setGBeanContext(GBeanContext context) {
    }

    public void doStart() throws WaitingException, Exception {
        if(rootURI == null) {
            rootURI = serverInfo.resolve(root);
        }
        log.info("Repository root is "+rootURI);
    }

    public void doStop() throws WaitingException, Exception {
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory(ReadOnlyRepository.class);
        infoFactory.addAttribute(new GAttributeInfo("Root", true));
        infoFactory.addReference(new GReferenceInfo("ServerInfo", ServerInfo.class));
        infoFactory.addInterface(Repository.class);
        infoFactory.setConstructor(new GConstructorInfo(
                new String[]{"Root", "ServerInfo"},
                new Class[]{URI.class, ServerInfo.class}
        ));
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
