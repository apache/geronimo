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
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:59:31 $
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
